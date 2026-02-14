<html lang="en">
<body>
<p>Dear Access Manager,</p>
<#if user == currentUser>
<p>user ${user.name} requested an extension of the UZH guest card.</p>
</#if>
<#if user != currentUser>
<p>user ${currentUser.name} has requested an extension of the UZH guest card for the user ${user.name}.</p>
</#if>
<p>Go to&#160;<a
        href="${configuration.baseUrl}accessrequest/process.html?id=${accessRequest.idString}">${configuration.baseUrl}accessrequest/process.html?id=${accessRequest.idString}</a> to proceed with this request.
</p>
<#include "fragments/mail-style.ftl"/>
</body>
</html>