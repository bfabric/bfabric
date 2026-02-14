<html lang="en">
<body>
<p>Dear ${container.memberSalutation},</p>
<p>the ${container.showScreenHrefLink} has been successfully created.</p>
<p>Using ${configuration.applicationName}, you will be able to manage this ${containerLabel} together with all its data. Please check out the ${containerLabel} page on ${container.showScreenHrefLink}.</p>
<p>To use the instruments and computers at the ${configuration.deployer.value}, ensure that you have once changed your password via ${configuration.applicationName}. Otherwise, the computer login is not be enabled. To change your password, login to ${configuration.applicationName} and go to the User Details screen (see right side of the header menu).</p>
<p>Should you need any further support in setting up the ${containerLabel} or want to discuss aspects of support, collaboration, or criticism, please contact us at the address provided below.</p>
<#include "fragments/contact.ftl"/>
</body>
</html>