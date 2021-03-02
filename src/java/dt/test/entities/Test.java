/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.test.entities;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dt.config.ConfigManager;

/**
 *
 * @author nobal
 */
@WebServlet(name = "Test", urlPatterns = {"/test"})
public class Test extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            /* TODO output your page here. You may use following sample code. */
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Test</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Test at " + request.getContextPath() + "</h1>");

            testResourceLoding(out);

            out.println("</body>");
            out.println("</html>");

        } finally {
            out.close();
        }
    }

    private void testResourceLoding(PrintWriter out) {

        ServletContext servletContext = getServletContext();
        String contextPath = servletContext.getRealPath(File.separator);
        out.println("<br/>File system context path (in TestServlet): " + contextPath);
        ConfigManager.init(servletContext);
        out.println("<br/>Local Root:  " + ConfigManager.getLocalRoot());
        out.println("<br/>Resource Path in web: " + ConfigManager.getConfigPath());
        out.println("<br/>Log Path : " + ConfigManager.getLogPath());
        out.println("<br/>Media Path : " + ConfigManager.getMediaPath());

        out.println("<br/>I'm inside the load resource file nobal");

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
