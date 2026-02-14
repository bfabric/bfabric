<html lang="en">
<body>
<p>Dear ${mail.recipient.firstName},</p>
<p>you have been assigned to coach the ${container.showScreenHrefLink}. Please carefully check the ${containerLabel} request<#if container.pending> and provide your review</#if>.</p>
<#include "fragments/mail-style.ftl"/>
</body>
</html>