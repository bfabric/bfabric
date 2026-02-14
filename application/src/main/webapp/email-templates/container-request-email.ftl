<html lang="en">
<body>
<p>Dear ${container.memberSalutation},</p>
<p>we received a request for a ${containerLabel} in which you are involved.</p>
<hr />
<p>
Id: ${mail.parentId}<br />
Name: ${container.name}<br />
Requester: ${container.requester.name}<br />
Budget Officer: ${container.budgetOfficer.name}<br />
Leader: ${container.leader.name}<br />
Contact: ${container.contact.name}<br />
Start Date: ${container.startDate}<br />
End Date: ${container.endDate}<br />
Summary:
    <#noautoesc>
        ${container.summarySafeHtml}
    </#noautoesc>
</p>
<hr />
<p>For more details, go to ${container.showScreenHrefLink}.</p>
<p>Please check the ${configuration.deployer.value}&#160;<a href="${configuration.urlTermsAndConditions}" target="_blank"
                                                            rel="noopener">Terms and Conditions</a> as they are an essential part of the agreement.
</p>
<p>The coach of this ${containerLabel} will contact the ${containerLabel} requester if further information is needed to proceed with this ${containerLabel} request.</p>
<p>For communication related to this ${containerLabel}, please use the ${container.getShowScreenHrefLink('comment feature', 'comments')} of ${configuration.applicationName}.</p>
<p>With kind regards</p>
<#include "fragments/contact.ftl"/>
<p>PS: This email is sent to all persons listed above in a specific function.</p>
</body>
</html>