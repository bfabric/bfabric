<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>you have been added as member to ${container.showScreenMemberTabHrefLink}.</p>
<#if !mail.recipient.computerLoginActivated>
<p>Your full access to the ${containerLabel} data will take effect the next time you log in.</p>
</#if>
<p>Read the&#160;<a href="${configuration.urlTermsAndConditions}" target="_blank" rel="noopener">Terms and Conditions</a> carefully since every ${containerLabel} member implicitly agrees on it.
</p>
<#include "fragments/contact-short.ftl"/>
</body>
</html>