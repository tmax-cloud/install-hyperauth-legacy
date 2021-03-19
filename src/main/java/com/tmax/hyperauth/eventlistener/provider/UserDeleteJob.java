package com.tmax.hyperauth.eventlistener.provider;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.tmax.hyperauth.authenticator.AuthenticatorConstants;
import com.tmax.hyperauth.caller.Constants;
import com.tmax.hyperauth.caller.HyperAuthCaller;

import com.tmax.hyperauth.rest.Util;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.representations.account.UserRepresentation;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import javax.transaction.Transactional;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class UserDeleteJob implements Job {
    public static final int USER_COUNT_UNIT = 1000;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        KeycloakSessionFactory keycloakSessionFactory = (KeycloakSessionFactory) context.getJobDetail().getJobDataMap().get("keycloakSessionFactory");
        KeycloakSession session = keycloakSessionFactory.create();

        // CronJob 선언부는 HyperauthEventListenerPropviderFactory PostInit()에 존재
        System.out.println(" [UserDelete Job] User Deletion Job Start !! ");
        Date currentDate = new Date();
        System.out.println( "Now : " + currentDate);
        JsonArray users = null;
        String accessToken = null;
        try{
            // Get Admin AccessToken
            accessToken = HyperAuthCaller.loginAsAdmin();

            // Get User Count
            int userCount = HyperAuthCaller.getUserCount(accessToken);
            System.out.println(" [UserDelete Job] User Count : " + userCount);

            // Get UserList per 1000 Person for preventing memory problem
            for (int i = 0; i < (userCount / USER_COUNT_UNIT) + 1; i++){
                int first = 0;
                if (i != 0) first = i * USER_COUNT_UNIT;
                JsonArray userListGet = HyperAuthCaller.getUserList(accessToken,first, USER_COUNT_UNIT);
                users.addAll(userListGet);
            }
        }catch( Exception e){
            e.printStackTrace();
            System.out.println(" [UserDelete Job] HyperAuth Not Ready yet ");
        }

        if ( users != null) {
            for( JsonElement user : users) {
                Gson gson = new Gson();
                UserRepresentation userRepresentation = gson.fromJson(user, UserRepresentation.class);
                try {
                    if ( userRepresentation.getAttributes() != null && userRepresentation.getAttributes().get("deletionDate") != null){
                        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date deletionDate = transFormat.parse(userRepresentation.getAttributes().get(AuthenticatorConstants.USER_ATTR_DELETION_DATE).get(0)); // FIXME

                        if ( currentDate.after(deletionDate)){
                            System.out.println(" [UserDelete Job] User [ " + userRepresentation.getUsername() + " ] Delete Start ");
                            HyperAuthCaller.deleteUser(userRepresentation.getId(), accessToken);

                            // Mail Send
                            String email = userRepresentation.getEmail();
                            String subject = "[Tmax 통합계정] 고객님의 계정 탈퇴가 완료되었습니다.";
//                            String msg = Constants.ACCOUNT_WITHDRAWAL_APPROVAL_BODY;
                            String body = Util.readLineByLineJava8("/opt/jboss/keycloak/themes/tmax/email/html/etc/account-withdrawal-completed.html");
                            Util.sendMail(session, email, subject, body, null);

                            // Topic Event Publish
                            TopicEvent topicEvent = TopicEvent.makeOtherTopicEvent("USER_DELETE", userRepresentation.getUsername(), currentDate.getTime() );
                            Producer.publishEvent("tmax", topicEvent);

                            System.out.println(" [UserDelete Job] User [ " + userRepresentation.getUsername() + " ] Delete Success ");
                        }
                    }
                } catch (ParseException | IOException e) {
                    e.printStackTrace();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
        System.out.println(" [UserDelete Job] User Deletion Job Finish !! ");
    }
}
