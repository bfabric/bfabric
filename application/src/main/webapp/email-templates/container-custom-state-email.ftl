<html lang="en">
<body>
<p>Your ${container.showScreenHrefLink} is now in state</p>
<p><b>${state}</b></p>
<#if container.userDecisionRequired>
    <#if container.samplesUserDecisionRequiredNotEmpty>
    <p>for samples with following tube ids:</p>
    <hr />
    <ul>
        <#list container.samplesUserDecisionRequired as sample>
        <li>${sample.tubeIdOrId}</li>
        </#list>
    </ul>
    <hr />
    </#if>
</#if>
<#if container.sequencingDone>
    <p>We are now reviewing the output and will announce when the data is ready for download.</p>
</#if>
<p>Go to ${container.showScreenHrefLinkWithoutClassName} to see ${containerLabel} details
    <#if container.userDecisionRequired>and decide how to proceed</#if>
</p>
<#include "fragments/contact-short.ftl"/>
</body>
</html>