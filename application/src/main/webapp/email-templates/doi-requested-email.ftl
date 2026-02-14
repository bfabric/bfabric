<html lang="en">
<body>
<p>Dear ${container.memberSalutation},</p>
<p>a Digital Object Identifier (DOI) has been requested for this ${containerLabel}. The DOI for this ${containerLabel} is ${configuration.doiPrefix}${mail.parentId}. You may use this DOI, for instance, in publications to refer to your ${containerLabel} data.</p>
<#include "fragments/doi-explanation.ftl" />
<#include "fragments/contact-short.ftl"/>
</body>
</html>