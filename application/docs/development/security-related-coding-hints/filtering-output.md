# B-Fabric — Filtering Output

In JSF components, HTML output is typically generated with an XHTML template and through JSF components.

The most commonly used JSF component for this purpose is \<h:outputText/\>. The following example prints the name request parameter value:

```
<h:outputText value="#{param.name}"/>
```

The JSF component will filter the output and escape dangerous characters as XHTML entities. For example, the character \< is escaped as &lt; automatically. In the JSF source code for this component,
you can find the following routine:

```
switch (c) {
    case '<': htmlEntity = "&lt;"; break;
    case '>': htmlEntity = "&gt;"; break;
    case '&': htmlEntity = "&amp;"; break;
    case '"': htmlEntity = "&quot;"; break;
}
```

The outputText component also has a switch to disable escaping of dangerous characters. The following example would open your application for an XSS security vulnerability:

```
<h:outputText value="#{param.name}" escape="false"/>  <!-- DON'T DO THIS! XSS SECURITY HOLE! -->
```

You should only use the escape=false switch when you are sure that the content you are printing into HTML does not contain any dangerous characters. For example, if your text content is already
filtered and stored escaped in the database, you might want to avoid double-escaping of the & symbol:

```
<h:outputText value="#{myBean.myTextContent}" escape="false"/>  <!-- Content contains &entity; and is already safe! -->
```

The following XHTML template will implicitly create an \<h:outputText/\> component and is also safe:

```
<p>This is my name: #{param.name}</p>
```

Other built-in JSF components such as \<h:outputLink/\> and \<h:message/\> are derived from the same parent component as \<h:outputText/\> and are therefore also filtering values automatically.