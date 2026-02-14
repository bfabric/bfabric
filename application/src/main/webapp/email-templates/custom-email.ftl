<html lang="en">
<body>
<#noautoesc>
    ${mail.messageSafeHtml}
</#noautoesc>
<#if mail.recipient??>
    <#if !(mail.recipient.hasRoleEmployee())>
    <p>Note: If you no longer wish to receive news emails via ${configuration.applicationName}, please&#160;<a href="${mail.recipient.unsubscribeUrl}">unsubscribe</a>.</p>
    </#if>
</#if>
<#include "fragments/mail-style.ftl">
</body>
</html>