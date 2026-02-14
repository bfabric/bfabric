<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>a reset of the password of your ${configuration.applicationName} account ${mail.recipient.login} was requested.</p>
<p>Click <a href="${mail.recipient.passwordResetUrl}">here</a> to get to the web page where you can set your new password.</p>
<p>Please note that the link will be valid for 24 hours only. If you do not want to reset your password, you can ignore this email and nothing will happen.</p>
<#include "fragments/contact-short.ftl"/>
</body>
</html>