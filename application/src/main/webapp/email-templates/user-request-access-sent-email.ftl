<html lang="en">
<body>
<pre>
Name________________ :    ${accessRequest.lastName}<br />
Vorname_____________ :    ${accessRequest.firstName}<br />
Strasse_____________ :    ${accessRequest.address.street}<br />
PLZ/Ort_____________ :    ${accessRequest.address.zip} ${accessRequest.address.city} ${accessRequest.address.country.id}<br />
Institut____________ :    ${accessRequest.user.affiliation}<br />
Schliessung_________ :    UzhCard<br />
Kartennummer________ :    ${accessRequest.accessCardNumber}<br />
Zutrittsprofil______ :    ${accessRequest.accessProfile}<br />
Berechtigen_________ :    ${accessRequest.accessGranted}<br />
Datum_______________ :    ${accessRequestManagerDate}<br />
E-Mail Antragsteller :    ${configuration.accessRequestManagerEmail}<br />
Berechtigungscode___ :    ${configuration.accessRequestPassword}<br />
Bemerkungen_________ :    ${accessRequest.comment}<br />
</pre>
</body>
</html>