<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>your ${configuration.applicationName} account has been created successfully. Please click on the link below to fully enable your account by verifying your email address.</p>
<p><a href="${mail.recipient.activateUrl}">${mail.recipient.activateUrl}</a></p>
<#include "fragments/contact-short.ftl"/>
</body>
</html>