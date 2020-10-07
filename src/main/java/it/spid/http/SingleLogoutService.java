package it.spid.http;


import it.spid.model.LogoutRequested;
import it.spid.service.SPIDService;
import it.spid.service.impl.SPIDServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/slo")
public class SingleLogoutService extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public SingleLogoutService() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String relayState = "logoutRequest";
        SPIDService spidService = new SPIDServiceImpl();

        LogoutRequested logoutRequested = spidService.buildLogoutRequest("http://localhost:8088", "_it53423523");
        String destination = logoutRequested.getDestination();
        String logoutRequest = logoutRequested.getEncodeLogoutRequest();


        PrintWriter out = response.getWriter();

        String html ="<html>\n"
                + "<body onload=\"javascript:document.forms[0].submit()\">\n"
                +"<form method=\"POST\" action=\""+ destination + "\">\n"
                +"<input type=\"hidden\" name=\"SAMLRequest\" value=\""+ logoutRequest + "\">\n"
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
        out.println("<h6>"  + logoutRequest +     "</h6>");



        out.println("</body>");

        out.println("</html>");


    }
}
