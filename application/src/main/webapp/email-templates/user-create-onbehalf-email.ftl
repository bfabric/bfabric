<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>welcome to ${configuration.applicationName}! You were registered as user ${mail.recipient.login} at&#160;<a href="${configuration.baseUrl}">${configuration.deployerName}</a></p>
<p>Click <a href="${mail.recipient.passwordResetUrl}">here</a> to set your password. Please note that the link will be valid for 24 hours only.</p>
<p>At any time, you can ask for a new password reset link <a href="${configuration.baseUrl}user/password-lost.html">here</a>.</p>
<#include "fragments/contact.ftl"/>
</body>
</html>