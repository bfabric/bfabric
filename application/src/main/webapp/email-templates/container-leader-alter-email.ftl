<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>the leader of ${container.showScreenHrefLink} has been changed. The new leader is ${container.getShowScreenUserHrefLink(cachedUser)}.</p>
<#include "fragments/contact-short.ftl" />
</body>
</html>