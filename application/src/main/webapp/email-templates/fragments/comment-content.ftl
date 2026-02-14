<p>To see the comment thread including attached files or to reply, follow the link&#160;<a
        href="${comment.parent.showScreenUrl}&tab=${comment.parentTab}">${comment.parent.showScreenUrl}&tab=${comment.parentTab}</a>.</p>
<hr />
<#noautoesc>
    ${comment.commentSafeHtml}
</#noautoesc>
<#if comment.attachments?size != 0>
<p>${comment.attachments?size} file(s) attached:</p>
    <#list comment.attachments as item>
    <p>${item.name} ${item.printSize}</p>
    </#list>
</#if>
<#if comment.workunits?size != 0>
<p>${comment.workunits?size} workunit(s) linked:</p>
    <#list comment.workunits as workunititem>
    <p>${workunititem.name} ${workunititem.printSize}</p>
    </#list>
</#if>
<#include "recipients-names.ftl">
<#include "mail-style.ftl"/>