<#import "template.ftl" as layout>

<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        <#if message.summary == msg('emailVerifiedMessage')>
        <div class="header-icon email-sent">
            ${msg("welcome")}
        </div>
        <#--  action 유형 구별할 기준이 없어서 하드코딩으로 구분중 (message_ko.properties 등의 내용을 수정할 땐 이부분도 바꿔줘야 정상동작 함)  -->
        <#elseif message.summary?contains("이 유효한지 확인하십시오.") || message.summary?contains("Confirm validity of e-mail address") || message.summary?contains("귀하의 계정과 연결되었는지 확인하십시오.") || message.summary?contains("Confirm linking the account ")>
         <div class="header-icon email-sent">
            ${msg("emailVerification")}
        </div>
        <#elseif message.summary == msg('identityProviderLinkSuccess')>
        <div class="header-icon email-sent">
            ${msg("finishIdpUserVerification")}
        </div>
        <#elseif messageHeader??>
        ${messageHeader}
        <#else>
        ${message.summary}
        </#if>
    <#elseif section = "form">
        <#if message.summary == msg('emailVerifiedMessage')>
            <div id="email-sent">
                <div class="${properties.kcFormGroupClass!}" style="height: 250px;">
                    <div class="welcome-img"></div>
                    <p id="welcome-instruction">
                        ${msg("welcomeInstruction1")?no_esc}
                        <#if isBrokerLogin?has_content && isBrokerLogin == "true" && brokerEmail?has_content && brokerVendor?has_content>
                            ${msg("finishIdpUserVerificationMessage3", brokerVendor, brokerEmail)?no_esc}
                        </#if>
                    </p>
                </div>
                <div class="${properties.kcFormGroupClass!}">
                    <button class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!}" style="margin-top: 100px;" <#if url.loginRestartFlowUrl?? && url.loginRestartFlowUrl!="">onclick="location.href='${url.loginRestartFlowUrl}'"<#else>onclick="location.href=document.location.origin"</#if>>${msg("doLogIn")}</button>
                </div>
            </div>
        <#elseif message.summary?contains("이 유효한지 확인하십시오.") || message.summary?contains("Confirm validity of e-mail address") || message.summary?contains("귀하의 계정과 연결되었는지 확인하십시오.") || message.summary?contains("Confirm linking the account ")>
            <div id="email-sent">
                <div class="${properties.kcFormGroupClass!}">
                    <div class="email-confirm-img"></div>
                    <p id="email-sent-instruction">
                        ${msg("emailVerificationComplete")}
                    </p>
                </div>
                <div class="${properties.kcFormGroupClass!}">
                    <button class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!}" style="margin-top: 95px;" onclick="location.href='${actionUri}'">${msg("doNext")}</button>
                </div>
            </div>
        <#elseif message.summary == msg('identityProviderLinkSuccess')>
            <div id="email-sent">
                <div class="${properties.kcFormGroupClass!}">
                    <#if identityProviderVendor??>
                        <div class="welcome-img-${identityProviderVendor}"></div>
                    <#else>
                        <div class="welcome-img"></div>
                    </#if>
                    <div id="welcome-instruction">
                        ${msg("finishIdpUserVerificationMessage1")}
                        <span id = 'connect-complete-span'> ${msg("finishIdpUserVerificationMessage1_2")}</span>
                        <span> ${msg("finishIdpUserVerificationMessage1_3", hyperauthUserName)?no_esc}</span>
                        <span> ${msg("${identityProviderVendor}")}${msg("finishIdpUserVerificationMessage1_4")}</span> 
                 
                    </div>
                    <div id="welcome-connect-info">
                       <span id = 'connect-info-span'>
                       ${msg("finishIdpUserVerificationMessage2")} ${msg("${identityProviderVendor}")}
                       ${msg("finishIdpUserVerificationMessage2_2", identityProviderUserName)?no_esc}
                       </span>
                       </div>
                    
                </div>
                <div class="${properties.kcFormGroupClass!}">
                    <div id = 'connent-complete-bt'>
                    <button class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!}" <#if actionUri?has_content && (client.baseUrl)?has_content>onclick="finishIdpSuccess('${client.baseUrl}', '${actionUri}')"</#if>>${msg("finishIdpUserVerificationButton")}</button>
                    </div>
                </div>
            </div>
        <#else>
            <div id="kc-info-message">
                <p class="instruction">${message.summary}<#if requiredActions??><#list requiredActions>: <b><#items as reqActionItem>${msg("requiredAction.${reqActionItem}")}<#sep>, </#items></b></#list><#else></#if></p>
                <#if skipLink??>
                <#else>
                    <#if pageRedirectUri?has_content>
                        <p><a href="${pageRedirectUri}">${kcSanitize(msg("backToApplication"))?no_esc}</a></p>
                    <#elseif actionUri?has_content>
                        <p><a href="${actionUri}">${kcSanitize(msg("proceedWithAction"))?no_esc}</a></p>
                    <#elseif (url.loginRestartFlowUrl)?has_content>
                        <p><a href="${url.loginRestartFlowUrl}">${kcSanitize(msg("backToApplication"))?no_esc}</a></p>
                    </#if>
                </#if>
            </div>
        </#if>
        <script type="text/javascript" src="${url.resourcesPath}/js/axios.min.js"></script>
        <script type="text/javascript" src="${url.resourcesPath}/js/info.js?${properties.version}"></script>
        <#--  <script>
            let vendor = "${msg(identityProviderVendor)}"
            console.log(vendor)
            let text  = document.getElementById('connect-info-span');
            text.innerHTML = "${msg("finishIdpUserVerificationMessage2", vendor, identityProviderUserName)?no_esc}";
        </script>  -->
    </#if>
</@layout.registrationLayout>