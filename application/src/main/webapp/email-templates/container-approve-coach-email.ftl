<html lang="en">
<body>
<p>Dear ${mail.recipient.firstName},</p>
<p>as coach of ${container.showScreenHrefLink} be informed that this ${containerLabel} has been approved.</p>
<#include "fragments/recipients-names.ftl">
<#include "fragments/mail-style.ftl"/>
</body>
</html>