package it.spid.http;

import it.spid.model.AuthRequest;
import it.spid.service.SPIDService;
import it.spid.service.impl.SPIDServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@WebServlet("/ssologin")
public class SsoLogin extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public SsoLogin() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String relayState = "resourcessajkjfsaifiasiabsoabaohfaihfiahwoibfaoi";

        SPIDService spidService = new SPIDServiceImpl();
        AuthRequest authRequest = spidService.buildAuthenticationRequest("http://localhost:8088", 0, relayState);

        String id = authRequest.getId();
        String destination = authRequest.getDestination();

        String authnRequest = authRequest.getXmlAuthRequest();





        response.setHeader("Location", destination);
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();


        String html ="<html>\n"
                + "<body onload=\"javascript:document.forms[0].submit()\">\n"
                +"<form method=\"POST\" action=\""+ destination + "\">\n"
                +"<input type=\"hidden\" name=\"SAMLRequest\" value=\""+ authnRequest + "\">\n"
                +"<input type=\"hidden\" name=\"RelayState\" value=\""+ relayState + "\">\n"
                +"<input type=\"submit\" value=\"go\">\n"
                +"</form>\n"
                +"</body>\n"
                +"</html>\n";

        out.print(html);

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head><title>Response</title></head>");
        out.println("<body>");

        out.println("<h6>"  + relayState +     "</h6>");
        out.println("<h6>"  + authnRequest +     "</h6>");
        out.println("<h6>"  + id +     "</h6>");


        out.println("</body>");

        out.println("</html>");

    }

}
