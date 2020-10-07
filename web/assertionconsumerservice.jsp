<%--
  Created by IntelliJ IDEA.
  User: vawap
  Date: 06/10/2020
  Time: 10:45
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Assertion Consumer Service</title>
    <style>
        html, body{
            height: 100%;
            margin: 0px;
            font-family: "Titillium Web", HelveticaNeue, Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;
        }
        .container{
            min-height: 100%;
        }
        .title-spid{
            height: 50px;
            margin-left: 50px;
            margin-top: 30px;

        }
        .header-spid{

            background-color: #0066CC;
            height: 50px;
            width: 100%;
        }
        .spid-data{
            width: 100%;
            margin-top: 50px;
            text-align: left;
        }
        .spid-attribute{
            width: 100%;
            font-size: 15px;
            font-weight: 600;
            text-decoration: none;
            list-style: none;
        }

    </style>
</head>
<body>

<div class="container" >
    <div class="title-spid">

        <h1 class="title-1">Test SPID</h1>

    </div>
    <div class="header-spid"></div>

    <div class="spid-data">
        <ul>
            <li class="spid-attribute"> <%= request.getAttribute("Nome") %> </li></br>
            <li class="spid-attribute"> <%= request.getAttribute("Cognome") %> </li></br>
            <li class="spid-attribute"> <%= request.getAttribute("Codice Fiscale") %> </li></br>
            <li class="spid-attribute"> <%= request.getAttribute("Sesso") %> </li></br>
            <li class="spid-attribute"> <%= request.getAttribute("Telefono") %> </li></br>
            <li class="spid-attribute"> <%= request.getAttribute("Email") %> </li></br>
            <li class="spid-attribute"> <%= request.getAttribute("Email PEC") %> </li></br>
            <li class="spid-attribute"> <%= request.getAttribute("Ragione Sociale") %> </li></br>
            <li class="spid-attribute"> <%= request.getAttribute("Domicilio") %> </li></br>
            <li class="spid-attribute"> <%= request.getAttribute("Sede Legale") %> </li></br>
            <li class="spid-attribute"> <%= request.getAttribute("Partita Iva") %> </li></br>
            <li class="spid-attribute"> <%= request.getAttribute("Data di Nascita") %> </li></br>
            <li class="spid-attribute"> <%= request.getAttribute("Luogo di Nascita") %> </li></br>
            <li class="spid-attribute"> <%= request.getAttribute("Nome Azienda") %> </li></br>
        </ul>
    </div>


</div>
</body>
</html>
