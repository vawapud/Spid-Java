package it.spid.http;

import it.spid.model.LogoutRequestDecoded;
import it.spid.service.SPIDService;
import it.spid.service.impl.SPIDServiceImpl;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/lcs")
public class LogoutConsumingService extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String SamlResponse = request.getParameter("SAMLResponse").toString();
        SPIDService spidService = new SPIDServiceImpl();

        LogoutRequestDecoded logoutRequestDecoded = spidService.processLogoutResponse(SamlResponse);

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head><title>Response</title></head>");
        out.println("<body>");
        request.setAttribute("In Response To", logoutRequestDecoded.getInResponseTo());
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("logoutconsumerservice.jsp");
        requestDispatcher.forward(request, response);


        //out.println("<h6>"  + printer +     "</h6>");
        //out.println("<h6>"  + logoutRequestDecoded.getInResponseTo() +     "</h6>");
       // out.println("<h6>"  + logoutRequestDecoded.getStatusSuccess() +     "</h6>");



        out.println("</body>");

        out.println("</html>");



    }
}
