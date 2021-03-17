package com.tmax.hyperauth.rest;

import com.tmax.hyperauth.authenticator.AuthenticatorConstants;
import com.tmax.hyperauth.authenticator.AuthenticatorUtil;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.spi.HttpResponse;
import org.keycloak.common.ClientConnection;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.forms.account.AccountPages;
import org.keycloak.forms.account.AccountProvider;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resources.RealmsResource;
import org.keycloak.services.util.ResolveRelative;
import org.keycloak.services.validation.Validation;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author taegeon_woo@tmax.co.kr
 * Console Provider contains restAPI for Custom Account Console
 */

public class ConsoleProvider implements RealmResourceProvider {

    public static final String STATE_CHECKER_ATTRIBUTE = "state_checker";
    public static final String STATE_CHECKER_PARAMETER = "stateChecker";

    @Context
    private KeycloakSession session;

    @Context
    private HttpResponse response;

    @Context
    private ClientConnection clientConnection;

//    private final AuthenticationManager.AuthResult auth;

    public ConsoleProvider(KeycloakSession session) {
        this.session = session;
    }

    private AuthenticationManager.AuthResult resolveAuthentication( KeycloakSession session) {
        AppAuthManager appAuthManager = new AppAuthManager();
        RealmModel realm = session.getContext().getRealm();
        if (realm == null){
            System.out.println("realm is null!!");
            realm = session.realms().getRealmByName("tmax");
        }
        AuthenticationManager.AuthResult authResult = appAuthManager.authenticateIdentityCookie(session, realm);
        if (authResult != null) {
            return authResult;
        }
        return null;
    }


    @Override
    public Object getResource() {
        return this;
    }

	String out = null;

    protected Response badRequest() {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    private boolean isValidStateChecker(MultipartFormDataInput input) {
        try {
            String actualStateChecker = input.getFormDataPart(STATE_CHECKER_PARAMETER, String.class, null);
            String requiredStateChecker = (String) session.getAttribute(STATE_CHECKER_ATTRIBUTE);

            return Objects.equals(requiredStateChecker, actualStateChecker);
        } catch (Exception ex) {
            return false;
        }
    }

    @POST
    @NoCache
    @Path("withdrawal")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response withdrawal( MultipartFormDataInput input ) {
        System.out.println("***** post /USER WITHDRAWAL");
        AuthenticationManager.AuthResult auth = resolveAuthentication(session);
        if (auth == null) {
            return badRequest();
        }

        RealmModel realm = session.getContext().getRealm();
        AccountProvider account = session.getProvider(AccountProvider.class).setRealm(realm).setUriInfo(session.getContext().getUri()).setHttpHeaders(session.getContext().getRequestHeaders());
        UserModel userModel = auth.getUser();
        System.out.println("userName : " + userModel.getUsername());

        account.setUser(userModel);
        account.setStateChecker((String) session.getAttribute(STATE_CHECKER_ATTRIBUTE));
        setReferrerOnPage( account);

        if (!isValidStateChecker(input)) {
            System.out.println("State Checker Error, User [ " + userModel.getUsername() + " ]");
            return account.setError(Response.Status.BAD_REQUEST, Messages.INTERNAL_SERVER_ERROR).createResponse(AccountPages.ACCOUNT);
        }

        clientConnection = session.getContext().getConnection();
        EventBuilder event = new EventBuilder(realm, session, clientConnection); // FIXME

        try {
            // 유저 탈퇴 신청 API
            // Withdrawal Qualification Validation
            boolean isQualified = true;
            String unQualifiedReason = null;
            if(userModel.getAttributes()!=null) {
                for (String key : userModel.getAttributes().keySet()) {
                    if ( key.startsWith( "withdrawal_unqualified_") && userModel.getAttribute(key).get(0).equalsIgnoreCase("t")){
                        isQualified = false;
                        unQualifiedReason = key.substring(23);
                        break;
                    }
                }
            }
            if (isQualified){
                //Deletion Date Calculate
                Date currentDate = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(currentDate);
                cal.add(Calendar.DATE, 30);
                Date deletionDate = cal.getTime();
                SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
                String deletionDateString = transFormat.format(deletionDate);

                if(userModel.getAttributes()!=null) userModel.removeAttribute(AuthenticatorConstants.USER_ATTR_DELETION_DATE);
                userModel.setAttribute(AuthenticatorConstants.USER_ATTR_DELETION_DATE, Arrays.asList(deletionDateString));
//                        userModel.setEnabled(false);  //유저 탈퇴 철회 시나리오로 인해서 삭제
                String email = userModel.getEmail();
                String subject = "[Tmax 통합계정] 고객님의 계정 탈퇴 신청이 완료되었습니다.";
                String body = Util.readLineByLineJava8("/opt/jboss/keycloak/themes/tmax/email/html/etc/account-withdrawal-request.html");

                Util.sendMail(session, email, subject, body, null );
                event.event(EventType.UPDATE_PROFILE).user(userModel).realm("tmax").detail("username", userModel.getUsername()).detail("userWithdrawal","t").success(); //FIXME
            } else{
                out = "User [" + userModel.getUsername() + "] is Unqualified to Withdraw from Account due to [" + unQualifiedReason + "] Policy, Check Withdrawal Policy or Contact Administrator";
                return account.setError(Status.FORBIDDEN, out).createResponse(AccountPages.ACCOUNT);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return account.setError(Response.Status.BAD_REQUEST, Messages.INTERNAL_SERVER_ERROR).createResponse(AccountPages.ACCOUNT);
        } catch (Throwable throwable) {
            System.out.println(Arrays.toString(throwable.getStackTrace()));
            return account.setError(Response.Status.BAD_REQUEST, "Mail Send Failed").createResponse(AccountPages.ACCOUNT);
        }
        return account.setSuccess(Messages.ACCOUNT_UPDATED).createResponse(AccountPages.ACCOUNT);
//        return Response.seeOther(RealmsResource.accountUrl(session.getContext().getUri().getBaseUriBuilder()).build(realm.getDisplayName())).build();
    }


    @POST
    @NoCache
    @Path("agreement")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response agreementUpdate( MultipartFormDataInput input ) {
        System.out.println("***** put /USER AGREEMENT");

        AuthenticationManager.AuthResult auth = resolveAuthentication(session);
        if (auth == null) {
            return badRequest();
        }

        RealmModel realm = session.getContext().getRealm();
        AccountProvider account = session.getProvider(AccountProvider.class).setRealm(realm).setUriInfo(session.getContext().getUri()).setHttpHeaders(session.getContext().getRequestHeaders());
        UserModel userModel = auth.getUser();
        System.out.println("userName : " + userModel.getUsername());

        account.setUser(userModel);
        account.setStateChecker((String) session.getAttribute(STATE_CHECKER_ATTRIBUTE));
        setReferrerOnPage( account);

        if (!isValidStateChecker(input)) {
            System.out.println("State Checker Error, User [ " + userModel.getUsername() + " ]");
            return account.setError(Response.Status.BAD_REQUEST, Messages.INTERNAL_SERVER_ERROR).createResponse(AccountPages.AGREEMENT);
        }

        clientConnection = session.getContext().getConnection();
        EventBuilder event = new EventBuilder(realm, session, clientConnection); // FIXME

        try {
            // 유저 이용약관 업데이트 API
            for (String key : input.getFormDataMap().keySet()) {
                if(!key.equalsIgnoreCase(STATE_CHECKER_PARAMETER)){
                    userModel.setAttribute(key, Collections.singletonList(input.getFormDataPart(key, String.class, null)));
                }
            }
            event.event(EventType.UPDATE_PROFILE).user(userModel).realm("tmax").detail("username", userModel.getUsername()).success(); //FIXME
        } catch (Exception e) {
            System.out.println("Failed to Update Agreement Attribute, User [ " + userModel.getUsername() + " ]");
            out = "Failed to Update Agreement Attribute, User [ " + userModel.getUsername() + " ]";
            return account.setError(Response.Status.BAD_REQUEST, out).createResponse(AccountPages.AGREEMENT);
        }
        return account.setSuccess(Messages.ACCOUNT_UPDATED).createResponse(AccountPages.AGREEMENT);
    }

    private void setReferrerOnPage(AccountProvider account) {
        String[] referrer = getReferrer();
        if (referrer != null) {
            account.setReferrer(referrer);
        }
    }

    private String[] getReferrer() {
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = session.getContext().getClient();
        String referrer = session.getContext().getUri().getQueryParameters().getFirst("referrer");
        if (referrer == null) {
            return null;
        }

        String referrerUri = session.getContext().getUri().getQueryParameters().getFirst("referrer_uri");

        ClientModel referrerClient = realm.getClientByClientId(referrer);
        if (referrerClient != null) {
            if (referrerUri != null) {
                referrerUri = RedirectUtils.verifyRedirectUri(session, referrerUri, referrerClient);
            } else {
                referrerUri = ResolveRelative.resolveRelativeUri(session, referrerClient.getRootUrl(), referrerClient.getBaseUrl());
            }

            if (referrerUri != null) {
                String referrerName = referrerClient.getName();
                if (Validation.isBlank(referrerName)) {
                    referrerName = referrer;
                }
                return new String[]{referrerName, referrerUri};
            }
        } else if (referrerUri != null) {
            if (client != null) {
                referrerUri = RedirectUtils.verifyRedirectUri(session, referrerUri, client);

                if (referrerUri != null) {
                    return new String[]{referrer, referrerUri};
                }
            }
        }
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        System.out.println("***** GET /test");
        AuthenticationManager.AuthResult auth = resolveAuthentication(session);
        if (auth == null) {
            return badRequest();
        }
        System.out.println("good!!");
        return Util.setCors( Status.OK, null);
    }

    @OPTIONS
    @Path("{path : .*}")
    public Response other() {
        System.out.println("***** OPTIONS /test");
        return Util.setCors( Status.OK, null);
    }

    @Override
    public void close() {
    }

    private EntityManager getEntityManager() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }
}