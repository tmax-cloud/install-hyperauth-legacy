<#--  <#import "template.ftl" as layout>

<@layout.registrationLayout displayMessage=false; section >

    <#if section = "header">
        ${msg("errorTitle")}
    <#elseif section = "form">
        <div id="kc-error-message">
            <p class="instruction">${message.summary?no_esc}</p>
            <#if client?? && client.baseUrl?has_content>
                <p><a id="backToApplication" href="${client.baseUrl}">${kcSanitize(msg("backToApplication"))?no_esc}</a></p>
            </#if>
        </div>
    </#if>
</@layout.registrationLayout>  -->
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" >

<head>
  
   <link href="./resources/css/error.css" rel="stylesheet"
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">
</head>

<script>
    function backToApplication(){

         <#if client?? && client.baseUrl?has_content>
        location.href ="${client.baseUrl}"
          </#if>
    } 
</script>

<style>
    .error-box{
        display: flex;
        justify-content: center;
        align-items: center;
     min-height: 80vh;
       
    }
    .error-text-box{
        float:left;
        margin-right:317px;
    }
    .error-title{
        font-family: NotoSansCJKkr-Medium;
        font-size: 30px;
        color: #233558;
        letter-spacing: -1px;
        margin: 0 0 20px 0; 
        font-weight:Medium;
    }
    .error-message{
        font-family: NotoSansCJKkr-Regular;
        font-size: 20px;
        color: #919AAB;
        letter-spacing: -0.8px;
        margin: 0 0 80px 0; 
    }
    button{
        background: #233558;
        border-radius: 30px;
        width:229px;
        height:50px;
        font-family: NotoSansCJKkr-Medium;
        font-size: 16px;
        color: #FFFFFF;
    }
    .illust{
        background: url("./resources/img/error_page.png") no-repeat;
     
        width: 580px;
        height:360px;
    }
      
</style>
<body>

<div class='error-box'>
    <div class ='error-text-box' >
        <p class ="error-title">${msg("errorTitle")}</P>
        <p class = 'error-message'>${message.summary?no_esc}</p>
        <button onclick="backToApplication()">${kcSanitize(msg("backToApplication"))?no_esc}</button>
    </div>
    <div class="illust"></div>

 </div>
</body>

</html>
