<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.agilewiki.content.i18n.*" %>
<html dir="ltr">
<head>
<title>Language&nbsp;Setup</title>
</head>
<body>
<bdo dir="ltr">
<form>
<% String[] languages = Localizer.getLanguages(); %>
<% for (int i=0; i<languages.length; ++i) { %>
<input class = "lang-select" type="radio" name="lang" value="<%=languages[i]%>" /> <%=languages[i]%> 
<br />
<% } %>
<!--<input type="button" value="Cancel" />
<input type="button" value="Apply" />-->
</form>
</bdo>
</body>
</html>
