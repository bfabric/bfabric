<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>you were registered twice as user in ${configuration.applicationName}. The account ${merged.login} was therefore merged into ${userLeft.login}."/></p>
<p>Please use the account with login&#160;<a href="${userLeft.showScreenUrl}">${userLeft.login}</a> to access all the services and data at the ${configuration.deployer.value}.</p>
<#include "fragments/contact-short.ftl"/>
</body>
</html>