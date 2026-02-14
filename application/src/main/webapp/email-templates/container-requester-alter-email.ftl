<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>the requester of ${container.showScreenHrefLink} has been changed. The new requester is ${container.getShowScreenUserHrefLink(cachedUser)}.</p>
<#include "fragments/contact-short.ftl" />
</body>
</html>