<html lang="en">
<body>
<p>Please note the following absence&#160;<a href="${event.showScreenUrl}">${event.fullEventInfo}</a>.</p>
<#if mail.message??>
<hr />
    <#noautoesc>
        ${mail.messageSafeHtml}
    </#noautoesc>
</#if>
<#include "fragments/recipients-names.ftl">
<#include "fragments/mail-style.ftl">
</body>
</html>