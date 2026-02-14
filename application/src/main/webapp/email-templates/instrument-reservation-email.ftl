<html lang="en">
<body>
<p>Please note the following instrument reservation that is related to you:
    <#if instrumentReservation.approvalPending>
    <span style="color: #FFFFFF; background-color: #ff9900; padding: 4px;">APPROVAL REQUIRED</span>
    </#if>
</p>
<p><a href="${instrumentReservation.showScreenUrl}">${instrumentReservation.fullEventInfo}</a></p>
<#include "fragments/contact-short.ftl">
<#include "fragments/recipients-names.ftl">
</body>
</html>