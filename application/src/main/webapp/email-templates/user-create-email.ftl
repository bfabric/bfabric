<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>welcome to ${configuration.applicationName}! You are registered as user ${mail.recipient.login} at&#160;<a href="${configuration.baseUrl}">${configuration.deployerName}</a></p>
<#include "fragments/contact-short.ftl"/>
</body>
</html>
