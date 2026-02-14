<html lang="en">
<body>
<p>Dear Access Manager,</p>
<#if user == currentUser>
<p>user ${user.name} requested access to the ${configuration.deployerName}.</p>
</#if>
<#if user != currentUser>
<p>user ${currentUser.name} has requested access to the ${configuration.deployerName} for the user ${user.name}.</p>
</#if>
<p>Go to&#160;<a
        href="${configuration.baseUrl}accessrequest/process.html?id=${accessRequest.idString}">${configuration.baseUrl}accessrequest/process.html?id=${accessRequest.idString}</a> to proceed with this request.
</p>
<#if user.accessCardNumber??>
<p>Please check the contact details carefully before you approve access by clicking on the SEND TO UZH button, which will instruct the SU department of UZH to reprogram the UZH card of the user.</p>
</#if>
<#if !user.accessCardNumber??>
<p>Please check and complete the UZH guest card request details carefully and click on SAVE before you can print the fully filled-out UZH guest card application form.</p>
</#if>
<#include "fragments/mail-style.ftl"/>
</body>
</html>