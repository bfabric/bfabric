<html lang="en">
<body>
<p>Dear Agenda Manager,</p>
<p>the agenda year ${closeAgendaYear} has been closed.</p>
<p>All remaining vacation credits have been transferred to ${nextAgendaYear}.</p>
<p>New credits have been assigned to all employees for ${nextAgendaYear}.</p>
<br />
<#list closeAgendaYearResult as item>
<p>${item}</p>
<br />
</#list>
<#include "fragments/mail-style.ftl" />
</body>
</html>