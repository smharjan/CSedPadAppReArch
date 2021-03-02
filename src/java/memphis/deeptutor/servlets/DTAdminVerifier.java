package memphis.deeptutor.servlets;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * Servlet implementation class DTAdminVerifier
 */
@WebServlet(description = "Servlet that verifies whether the admin password", urlPatterns = { "/DTAdminVerifier" })
public class DTAdminVerifier extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DTAdminVerifier() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String givenId = request.getParameter("txtAdminID");
		String password = request.getParameter("txtAdminPass");
		// if this user is visiting first time,
		HttpSession session = request.getSession();
		System.out.print("doPost: " + session.getId());
		if (givenId.trim().equals("dtadmin") && password.trim().equals("dtauthor310")){
			String adminPage = "/admin.jsp";
			session.setAttribute("loginSuccesfull", "true");
			RequestDispatcher rd = getServletContext().getRequestDispatcher(adminPage);
			rd.forward(request, response);
		} else {
			session.setAttribute("error", "Admin credentials failed");
			String destinationOnError = "/adminlogin.jsp";
			RequestDispatcher rd = getServletContext().getRequestDispatcher(destinationOnError);
			rd.forward(request, response);
		}
	}
        
    

}
