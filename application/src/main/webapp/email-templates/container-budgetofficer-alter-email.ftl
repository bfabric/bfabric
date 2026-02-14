<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>the budget officer of ${container.showScreenHrefLink} has been changed. The new budget officer is ${container.getShowScreenUserHrefLink(cachedUser)}.</p>
<#include "fragments/contact-short.ftl" />
</body>
</html>