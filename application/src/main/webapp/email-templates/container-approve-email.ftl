<html lang="en">
<body>
<p>Dear ${container.memberSalutation},</p>
<p>we are pleased to inform you about the activation of your ${container.showScreenHrefLink}.</p>
<p>You will be able to manage your ${containerLabel}, its members, and all produced and annotated data using ${configuration.applicationName}.</p>
<p>Please see the ${containerLabel} page on ${container.showScreenHrefLink}.</p>
<p>Note that all members added to the ${containerLabel} need to agree to the ${configuration.deployer.value}&#160;<a href="${configuration.urlTermsAndConditions}" target="_blank"
                                                                                                                     rel="noopener">Terms and Conditions</a>.</p>
<p>For all ${containerLabel}-related questions, please contact the coach of your ${containerLabel} via the ${containerLabel} page. For collaboration inquiries and feedback, please contact us at the address provided below.</p>
<p>With kind regards,<br /> ${currentUser.name}</p>
<#include "fragments/contact.ftl"/>
</body>
</html>