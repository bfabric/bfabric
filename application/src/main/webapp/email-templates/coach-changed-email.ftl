<html lang="en">
<body>
<p>Please note that the following updates in the coaching of&#160;<a href="${mail.parent.showScreenUrl}">${entityDisplayLabel} ${entity.displayName}</a>:</p>
<#if coachChanged><p><#if mail.parent.coach??>New Coach: ${mail.parent.coach.name}<#else>Removed Coach</#if></p></#if>
<#if coachBackupChanged><p><#if mail.parent.coachBackup??>New Coach Backup: ${mail.parent.coachBackup.name}<#else>Removed Coach Backup</#if></p></#if>
<#if bioinformaticianChanged><p><#if mail.parent.bioinformatician??>New Bioinformatician: ${mail.parent.bioinformatician.name}<#else>Removed Bioinformatician</#if></p></#if>
<#include "fragments/recipients-names.ftl">
<#include "fragments/mail-style.ftl"/>
</body>
</html>