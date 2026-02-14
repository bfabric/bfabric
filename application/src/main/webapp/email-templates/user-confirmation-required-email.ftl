<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>the email address of your ${configuration.applicationName} account has been changed. Please click on the link below to verify your email address.</p>
<p><a href="${mail.recipient.activateUrl}">${mail.recipient.activateUrl}</a></p>
<#include "fragments/contact-short.ftl"/>
</body>
</html>