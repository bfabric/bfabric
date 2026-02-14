<#if configuration.environmentProduction != true>
<p>WARNING: This email comes from the unattended ${configuration.deploymentBranchVersion} system. Go to the productive ${configuration.applicationName} system if you are not testing on purpose!</p>
</#if>

<#noautoesc>
    ${mail_style}
</#noautoesc>