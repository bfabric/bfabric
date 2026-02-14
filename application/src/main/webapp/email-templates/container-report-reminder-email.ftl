<html lang="en">
<body>
<p>Dear ${mail.recipient.name},</p>
<p>to ensure optimal support of projects at the ${configuration.deployer.value}, it is essential that the current and final state of a project is documented in the project records. The ${configuration.deployer.value} staff can then refer to this information for customized support. In order to ensure this and to achieve transparency about the current use of the ${configuration.deployer.value} by the various projects, I would ask you to submit a brief</p>
<#if reportYear != "Final">
<p>${reportYear} Year Project Extension Report</p>
<p>on the achieved progress and the current and future work within the scope of the project&#160;<a href="${project.showScreenUrl}">${project.displayName}</a>.</p>
</#if>
<#if reportYear == "Final">
<p>Final Project Report</p>
<p>on the achieved progress and the final state of the project&#160;<a href="${project.showScreenUrl}">${project.displayName}</a>.</p>
</#if>
<p>To upload your report of no more than a single text page, go to projects page&#160;<a href="${project.showScreenUrl}">${project.showScreenUrl}</a>.
</p>
<#if reportYear != "Final">
<p>After approval of the extension report, the project will be extended for another year. Please note that the total running time of a project is limited to three years after which the project will be closed for data acquisition. Continuation of experiments will then require the submission of a new or updated project request.</p>
</#if>
<#if reportYear == "Final">
<p>After approval of the final report, the project will later be announced for finishing as it has reached the support limit of three years. Continuation of experiments will then require the submission of a new project request for which the provided final project report can serve as a basis for the continuation of your research.</p>
</#if>
<p>Should you have questions about the procedure, please contact us at&#160;<a href="mailto:${configuration.coordinatorEmail}">${configuration.coordinatorEmail}</a>.</p>
<#include "fragments/contact-short.ftl"/>
</body>
</html>