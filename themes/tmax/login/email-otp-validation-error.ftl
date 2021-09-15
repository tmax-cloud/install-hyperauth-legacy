<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "header">
         <div class="header-icon otp-send-failed">
            ${msg("otpCodeSendFailedTitle")}
        </div>
    <#elseif section = "form">
        <div id="otp-send-failed">
            <div class="${properties.kcFormGroupClass!}">
                <p id="instruction" >
                ${msg("otpCodeSendFailedMessage1")}
                <br>
                ${msg("otpCodeSendFailedMessage2")}
                </p>
            </div>
            <div class="${properties.kcFormGroupClass!}">
                <div id="info" >
                <#--  <div class="call-icon"></div>  -->
                <div class="info-message">${msg("blockedMessagePhoneNumber")}</div>
                <br>
                <#--  <div class="email-icon"></div>  -->
                <div class="info-message">${msg("blockedMessageEmailAddress")}</div>
                </div>
            </div>
            <div class="${properties.kcFormGroupClass!}" >
                <button id = "otp-send-faild-button" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!}" <#if client.baseUrl?? && client.baseUrl!="">onclick="location.href='${client.baseUrl}'"<#else>onclick="location.href=document.location.origin"</#if>>${msg("sessionExpiredMsg3")}</button>
            </div>
        </div>
        <#if properties.scripts_security_policy_hyperauth?has_content>
            <#list properties.scripts_security_policy_hyperauth?split(' ') as script>
                <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
            </#list>
        </#if>
    </#if>
</@layout.registrationLayout>