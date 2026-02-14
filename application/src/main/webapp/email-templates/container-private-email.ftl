<html lang="en">
<body>
<p>Dear ${container.memberSalutation}",</p>
<p>your ${container.showScreenHrefLink} has been finished for data acquisition six months ago and is now planned to be completely closed. After closing of the ${containerLabel}, you will still be able to access and download all data annotated in ${configuration.applicationName}. However, additions and changes to the data will no longer be possible.</p>
<p>Should there be important reasons why the ${containerLabel} could not be closed after 30 days from today, please ask your ${containerLabel} contact or budget officer to get in contact with us at&#160;<a
        href="mailto:${configuration.coordinatorEmail}">${configuration.coordinatorEmail}</a>.</p>
<p>All data generated in the ${containerLabel} will remain accessible only to the members of the ${containerLabel}s. For ${containerLabel}s containing very large amounts of data, the long-term storage of the basic analytical data may incur costs. Should your ${containerLabel} fall under this category, you will receive further information from us concerning options and charges.</p>
<p>As we strive to constantly improve our research support, we would appreciate if you could provide us your feedback by answering this&#160;<a
        href="${configuration.baseUrl}feedback/submit.html?containerId=#{mail.parentId}&amp;defaultFeedbackTemplateId=#{container.defaultFeedbackTemplateId}">survey</a>.</p>
<p>With kind regards,<br />${currentUser.name}</p>
<#include "fragments/contact.ftl"/>
</body>
</html>