<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>please note that the instrument reservation</p>
<p><a href="${instrumentReservation.showScreenUrl}">${instrumentReservation.fullEventInfo}</a></p>
<p>has been ${instrumentReservation.approvalLabel} by ${instrumentReservation.approvedBy} at ${instrumentReservation.approvalDate}:</p>
<#if instrumentReservation.approvalNote??>
<p>${instrumentReservation.approvalNote}</p>
</#if>
</body>
</html>