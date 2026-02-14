<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<#if user == currentUser>
<p>you have requested an extension of the UZH guest card. We will review your request as soon as possible and inform you about the decision.</p>
</#if>
<#if user != currentUser>
<p>user&#160;<a href="${currentUser.showScreenUrl}">${currentUser.name}</a> has requested for you extension to the ${configuration.deployerName}.</p>
</#if>
<#if user != currentUser>
<p>Note: If this extension request is not in your interest, please cancel it via&#160;<a href="${user.showScreenUrl}&amp;tab=accessrequests">${user.showScreenUrl}&amp;tab=accessrequests</a>.</p>
</#if>
<#include "fragments/contact-short.ftl"/>
</body>
</html>