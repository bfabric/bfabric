<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>the coach of ${container.showScreenHrefLink} has been changed. The new coach is ${container.getShowScreenUserHrefLink(cachedUser)}. </p>
<#include "fragments/contact-short.ftl" />
</body>
</html>