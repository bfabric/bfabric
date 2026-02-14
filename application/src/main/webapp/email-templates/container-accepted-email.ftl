<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>your ${container.showScreenHrefLink} is accepted.</p>
<p>Please send or bring your samples together with the signed&#160;<a href="${mail.parent.getReportPDFUrl('order-confirmation-form-fop')}">${containerLabel} confirmation form</a>
    <#if container.renderedOffers>
    &#160;and&#160;<a href="${mail.parent.showScreenUrl}&#38;tab=offers">offer</a>
    </#if>
.</p>
<#if container.dataDeliveryOnly>
<p>Note that you have selected the storage model "Data Delivery Only" which does not include bioinformatics data analysis. Data storage is granted only for 1 month after data generation. During this
period you have to download the data and confirm the successful download. No copy will be kept for you to download beyond this period.</p>
</#if>
<#include "fragments/contact-short.ftl"/>
</body>
</html>