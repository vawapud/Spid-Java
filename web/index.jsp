<%--
  Created by IntelliJ IDEA.
  User: vawap
  Date: 24/08/2020
  Time: 11:43
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="ISO-8859-1">
  <title>Spid Form Test</title>
</head>
<body>
<form action="<%=request.getContextPath()+"/ssologin" %>" method="post">

  <button type="submit" name="invia">Loggati</button>

</form>
</body>
</html>