<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>the contract&#160;<a href="${contract.showScreenUrl}">${contract.displayName}</a> is going to be expired on
<br />${contract.expiryDate}. Please check whether this contract needs to be renewed and initiate actions as needed.</p>
<#include "fragments/contact-short.ftl"/>
</body>
</html>