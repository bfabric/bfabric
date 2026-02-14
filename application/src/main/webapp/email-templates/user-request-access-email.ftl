<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<#if user == currentUser>
<p>you have requested access to our lab.</p>
</#if>
<#if user != currentUser>
<p>user&#160;<a href="${currentUser.showScreenUrl}">${currentUser.name}</a> has requested for you access to the ${configuration.deployerAbbreviation}.</p>
</#if>
<p>Please pass by the ${configuration.deployerAbbreviation} administration office (${configuration.accessRequestManagerAddress}) for the ID check. </p>
<#if user.accessCardNumber??>
<p>In fact, you are kindly requested to bring along your UZH Access Card AND your ID, passport or permit. Our access profile will be loaded onto your existing UZH card.</p>
</#if>
<#if !user.accessCardNumber??>
<p>In fact, you are kindly requested to bring along your ID, passport or permit. We will also need a picture (a selfie with a white background is fine)
which can be sent by e-mail to the ${configuration.deployerAbbreviation} administration office after having performed the ID check.</p>
<p>${accessRequestPickUpAccessCard}</p>
</#if>
<p>${accessRequestManagerOfficeTimes}</p>
<#if user != currentUser>
<p>Note: If this access request is not in your interest, please ignore this email and nothing will happen.</p>
</#if>
<#include "fragments/contact-short.ftl"/>
</body>
</html>