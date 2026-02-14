<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>your account ${mail.recipient.login} at&#160;<a href="${configuration.baseUrl}">${configuration.baseUrl}</a> has expired.</p>
<#include "fragments/contact-short.ftl"/>
</body>
</html>