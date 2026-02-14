<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>your ${container.showScreenHrefLink} has been completed. Go to the ${container.showScreenHrefLink}.</p>
<p>Your opinion is important to us. Please take a few moments and send us your feedback by answering this&#160;<a
        href="${configuration.baseUrl}feedback/submit.html?containerId=#{mail.parentId}&amp;defaultFeedbackTemplateId=#{container.defaultFeedbackTemplateId}">survey</a>.</p>
<#include "fragments/contact-short.ftl"/>
</body>
</html>