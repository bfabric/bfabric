<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>the contact person of ${container.showScreenHrefLink} has been changed. The new contact is ${container.getShowScreenUserHrefLink(cachedUser)}.</p>
<#include "fragments/contact-short.ftl" />
</body>
</html>