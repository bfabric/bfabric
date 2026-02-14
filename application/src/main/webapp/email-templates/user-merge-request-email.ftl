<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>it looks like that you are registered twice in ${configuration.applicationName} as the users&#160;<a href="${user1.showScreenUrl}">${user1.login}</a> and&#160;<a
        href="${user2.showScreenUrl}">${user2.login}</a>.</p>
<p>To improve service quality, ${configuration.deployer.value} tries to avoid duplicate user accounts and (communication) problems arising from this redundancy.</p>
<p>If you are actually two different persons, or you have an important argument why you need two separate user accounts, please contact us.</p>
<p>Otherwise, the two user accounts will automatically be merged into one account from today on in 90 days.</p>
<#include "fragments/contact-short.ftl"/>
</body>
</html>