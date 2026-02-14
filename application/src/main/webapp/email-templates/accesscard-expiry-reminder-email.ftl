<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>your UZH card is going to expire in a month.</p>
<#if mail.recipient.hasGuestAccessCard()>
<p>If you still require access to the center, follow&#160;<a
        href="${configuration.baseUrl}accessrequest/request-access.html?id=${mail.recipient.id}">this link</a> to initiate the extension of the validity date of your UZH card.</p>
</#if>
<#if mail.recipient.hasPersonalAccessCard()>
<p>If you still require access to the center, please remember to validate your UZH card at one of the validation stations at Irchel.</p>
</#if>
<#include "fragments/contact-short.ftl"/>
</body>
</html>