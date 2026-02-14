<html lang="en">
<body>
<p>Dear ${container.memberSalutation},</p>
<p>your ${container.showScreenHrefLink} has reached the planned (maximum 3-year) limit and therefore needs to be closed for data acquisition. All data that has been generated in the ${containerLabel} will remain accessible to the ${containerLabel} members, as will be the possibility to finalize data analysis.</p>
<p>Should there be important reasons why the ${containerLabel} could not be closed for data acquisition after 30 days from today, please ask your ${containerLabel} contact or budget officer to get in contact with us at&#160;<a
        href="mailto:${configuration.coordinatorEmail}">${configuration.coordinatorEmail}</a>.</p>
<p>With kind regards,<br /> ${currentUser.name}</p>
<#include "fragments/contact.ftl"/>
</body>
</html>