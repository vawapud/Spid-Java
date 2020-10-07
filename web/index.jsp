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
  <link type="text/css" rel="stylesheet" href="css/spid-sp-access-button.min.css" />

</head>
<body>

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
  .spid-button-wrapper{
    margin-top: 40px;
  }
  .spid-button-access{
    padding-left: 50px;
    display: block;
    width: 30%;
    float: left;
  }
  .spid-button-logout{
    display: block;
    float: left;
    width: 50%;
  }

  .logout-button{
    background-color: #06C;
    color: #FFFFFF;
    font-size: 15px;
    width: 220px;

    display: inline-block;
    position: relative;
    font-family: "Titillium Web", HelveticaNeue, Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;
    font-weight: 600;
    line-height: 1em;
    text-decoration: none;
    border: 0;
    text-align: center;
    cursor: pointer;
    overflow: hidden;
    padding-top: 17px;
    padding-bottom: 17px;
  }
  .logout-button:hover{
    background-color: #003366;
  }

  .logout-text{
    padding: 18px;
    margin-bottom: 18px;
    margin-top: 20px;
    font-size: 1.15em;
    text-align: center;
  }

</style>

<div class="container" >
  <div class="title-spid">

    <h1 class="title-1">Test SPID</h1>

  </div>
  <div class="header-spid"></div>
  <div class="spid-button-wrapper">
    <div class ="spid-button-access">
        <form name="spid_idp_access" action="<%=request.getContextPath()+"/ssologin" %>" method="post">
          <input type="hidden" name="param_001" value="" />
          <input type="hidden" name="param_002" value="" />
          <input type="hidden" name="param_003" value="" />
          <a href="#" class="italia-it-button italia-it-button-size-m button-spid" spid-idp-button="#spid-idp-button-medium-post" aria-haspopup="true" aria-expanded="false">
            <span class="italia-it-button-icon"><img src="img/spid-ico-circle-bb.svg" onerror="this.src='img/spid-ico-circle-bb.png'; this.onerror=null;" alt="" /></span>
            <span class="italia-it-button-text">Entra con SPID</span>
          </a>
          <div id="spid-idp-button-medium-post" class="spid-idp-button spid-idp-button-tip spid-idp-button-relative">
            <ul id="spid-idp-list-medium-root-post" class="spid-idp-button-menu" aria-labelledby="spid-idp">
              <li class="spid-idp-button-link" data-idp="arubaid">
                <button class="idp-button-idp-logo" name="aruba_id" type="submit"><span class="spid-sr-only">Aruba ID</span><img class="spid-idp-button-logo" src="img/spid-idp-arubaid.svg" onerror="this.src='img/spid-idp-arubaid.png'; this.onerror=null;" alt="Aruba ID" /></button>
              </li>
              <li class="spid-idp-button-link" data-idp="infocertid">
                <button class="idp-button-idp-logo" name="infocert_id" type="submit"><span class="spid-sr-only">Infocert ID</span><img class="spid-idp-button-logo" src="img/spid-idp-infocertid.svg" onerror="this.src='img/spid-idp-infocertid.png'; this.onerror=null;" alt="Infocert ID" /></button>
              </li>
              <li class="spid-idp-button-link" data-idp="intesaid">
                <button class="idp-button-idp-logo" name="intesa_id" type="submit"><span class="spid-sr-only">Intesa ID</span><img class="spid-idp-button-logo" src="img/spid-idp-intesaid.svg" onerror="this.src='img/spid-idp-intesaid.png'; this.onerror=null;" alt="Intesa ID" /></button>
              </li>
              <li class="spid-idp-button-link" data-idp="lepidaid">
                <button class="idp-button-idp-logo" name="lepida_id" type="submit"><span class="spid-sr-only">Lepida ID</span><img class="spid-idp-button-logo" src="img/spid-idp-lepidaid.svg" onerror="this.src='img/spid-idp-lepidaid.png'; this.onerror=null;" alt="Lepida ID" /></button>
              </li>
              <li class="spid-idp-button-link" data-idp="namirialid">
                <button class="idp-button-idp-logo" name="namirial_id" type="submit"><span class="spid-sr-only">Namirial ID</span><img class="spid-idp-button-logo" src="img/spid-idp-namirialid.svg" onerror="this.src='img/spid-idp-namirialid.png'; this.onerror=null;" alt="Namirial ID" /></button>
              </li>
              <li class="spid-idp-button-link" data-idp="posteid">
                <button class="idp-button-idp-logo" name="poste_id" type="submit"><span class="spid-sr-only">Poste ID</span><img class="spid-idp-button-logo" src="img/spid-idp-posteid.svg" onerror="this.src='img/spid-idp-posteid.png'; this.onerror=null;" alt="Poste ID" /></button>
              </li>
              <li class="spid-idp-button-link" data-idp="sielteid">
                <button class="idp-button-idp-logo" name="sielte_id" type="submit"><span class="spid-sr-only">Sielte ID</span><img class="spid-idp-button-logo" src="img/spid-idp-sielteid.svg" onerror="this.src='img/spid-idp-sielteid.png'; this.onerror=null;" alt="Sielte ID" /></button>
              </li>
              <li class="spid-idp-button-link" data-idp="spiditalia">
                <button class="idp-button-idp-logo" name="spiditalia" type="submit"><span class="spid-sr-only">SPIDItalia Register.it</span><img class="spid-idp-button-logo" src="img/spid-idp-spiditalia.svg" onerror="this.src='img/spid-idp-spiditalia.png'; this.onerror=null;" alt="SpidItalia" /></button>
              </li>
              <li class="spid-idp-button-link" data-idp="timid">
                <button class="idp-button-idp-logo" name="tim_id" type="submit"><span class="spid-sr-only">Tim ID</span><img class="spid-idp-button-logo" src="img/spid-idp-timid.svg" onerror="this.src='img/spid-idp-timid.png'; this.onerror=null;" alt="Tim ID" /></button>
              </li>
              <li class="spid-idp-button-link" data-idp="localhost">
                <button class="idp-button-idp-logo" name="localhost" type="submit"><span class="spid-sr-only">Tim ID</span><img class="spid-idp-button-logo" src="img/spid-idp-timid.svg" onerror="this.src='img/spid-idp-timid.png'; this.onerror=null;" alt="Tim ID" /></button>
              </li>
              <li class="spid-idp-support-link" data-spidlink="info">
                <a href="https://www.spid.gov.it">Maggiori informazioni</a>
              </li>
              <li class="spid-idp-support-link" data-spidlink="rich">
                <a href="https://www.spid.gov.it/richiedi-spid">Non hai SPID?</a>
              </li>
              <li class="spid-idp-support-link" data-spidlink="help">
                <a href="https://www.spid.gov.it/serve-aiuto">Serve aiuto?</a>
              </li>
            </ul>
          </div>
        </form>


        <span>


        </span>
    </div>
    <div class="spid-button-logout">
        <form name="spid_logout" action="<%=request.getContextPath()+"/slo" %>" method="post">

          <button type="submit" class="logout-button">

            <span class="logout-text">Sloggati con SPID</span>

          </button>



        </form>
    </div>
  </div>
</div>
</body>

<script type="text/javascript" src="js/jquery.min.js"></script>
<script type="text/javascript" src="js/spid-sp-access-button.min.js"></script>
<script>
  $(document).ready(function(){
    var rootList = $("#spid-idp-list-small-root-get");
    var idpList = rootList.children(".spid-idp-button-link");
    var lnkList = rootList.children(".spid-idp-support-link");
    while (idpList.length) {
      rootList.append(idpList.splice(Math.floor(Math.random() * idpList.length), 1)[0]);
    }
    rootList.append(lnkList);
  });

  $(document).ready(function(){
    var rootList = $("#spid-idp-list-medium-root-get");
    var idpList = rootList.children(".spid-idp-button-link");
    var lnkList = rootList.children(".spid-idp-support-link");
    while (idpList.length) {
      rootList.append(idpList.splice(Math.floor(Math.random() * idpList.length), 1)[0]);
    }
    rootList.append(lnkList);
  });

  $(document).ready(function(){
    var rootList = $("#spid-idp-list-large-root-get");
    var idpList = rootList.children(".spid-idp-button-link");
    var lnkList = rootList.children(".spid-idp-support-link");
    while (idpList.length) {
      rootList.append(idpList.splice(Math.floor(Math.random() * idpList.length), 1)[0]);
    }
    rootList.append(lnkList);
  });

  $(document).ready(function(){
    var rootList = $("#spid-idp-list-xlarge-root-get");
    var idpList = rootList.children(".spid-idp-button-link");
    var lnkList = rootList.children(".spid-idp-support-link");
    while (idpList.length) {
      rootList.append(idpList.splice(Math.floor(Math.random() * idpList.length), 1)[0]);
    }
    rootList.append(lnkList);
  });

  $(document).ready(function(){
    var rootList = $("#spid-idp-list-small-root-post");
    var idpList = rootList.children(".spid-idp-button-link");
    var lnkList = rootList.children(".spid-idp-support-link");
    while (idpList.length) {
      rootList.append(idpList.splice(Math.floor(Math.random() * idpList.length), 1)[0]);
    }
    rootList.append(lnkList);
  });

  $(document).ready(function(){
    var rootList = $("#spid-idp-list-medium-root-post");
    var idpList = rootList.children(".spid-idp-button-link");
    var lnkList = rootList.children(".spid-idp-support-link");
    while (idpList.length) {
      rootList.append(idpList.splice(Math.floor(Math.random() * idpList.length), 1)[0]);
    }
    rootList.append(lnkList);
  });

  $(document).ready(function(){
    var rootList = $("#spid-idp-list-large-root-post");
    var idpList = rootList.children(".spid-idp-button-link");
    var lnkList = rootList.children(".spid-idp-support-link");
    while (idpList.length) {
      rootList.append(idpList.splice(Math.floor(Math.random() * idpList.length), 1)[0]);
    }
    rootList.append(lnkList);
  });

  $(document).ready(function(){
    var rootList = $("#spid-idp-list-xlarge-root-post");
    var idpList = rootList.children(".spid-idp-button-link");
    var lnkList = rootList.children(".spid-idp-support-link");
    while (idpList.length) {
      rootList.append(idpList.splice(Math.floor(Math.random() * idpList.length), 1)[0]);
    }
    rootList.append(lnkList);
  });
</script>
</html>