package it.spid.http;

import it.spid.model.ResponseDecoded;
import it.spid.service.SPIDService;
import it.spid.service.impl.SPIDServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@WebServlet("/acs")
public class Acs extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public Acs() {
        super();
        // TODO Auto-generated constructor stub
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        response.getWriter().append("Served at: ").append(request.getContextPath());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        String SamlResponse = request.getParameter("SAMLResponse").toString();

        String relayState = request.getParameter("RelayState").toString();


        SPIDService spidService = new SPIDServiceImpl();
        ResponseDecoded responseDecoded = spidService.processAuthenticationResponse(SamlResponse, relayState);


        PrintWriter out = response.getWriter();


        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head><title>Response</title></head>");
        out.println("<body>");

        out.println("<h6>"  + responseDecoded.getNome() +     "</h6>");
        out.println("<h6>"  + responseDecoded.getCognome() +     "</h6>");
        out.println("<h6>"  + responseDecoded.getCodiceFiscale() +     "</h6>");
        out.println("<h6>"  + responseDecoded.getSesso() +     "</h6>");
        out.println("<h6>"  + responseDecoded.getNumeroTelefono() +     "</h6>");
        out.println("<h6>"  + responseDecoded.getEmailAddress() +     "</h6>");
        out.println("<h6>"  + responseDecoded.getIndirizzoDomicilio() +     "</h6>");
        out.println("<h6>"  + responseDecoded.getCodiceIdentificativo() +     "</h6>");
        out.println("<h6>"  + responseDecoded.getIndirizzoSedeLegale() +     "</h6>");
        out.println("<h6>"  + responseDecoded.getPartitaIva() +     "</h6>");
        out.println("<h6>"  + responseDecoded.getDataNascita() +     "</h6>");
        out.println("<h6>"  + responseDecoded.getDataScadenzaIdentita() +     "</h6>");
        out.println("<h6>"  + responseDecoded.getInResponseTo() +     "</h6>");
        out.println("<h6>"  + responseDecoded.getRelayState() +     "</h6>");
        out.println("<h6>"  + SamlResponse +     "</h6>");


        out.println("</body>");

        out.println("</html>");

    }
}
