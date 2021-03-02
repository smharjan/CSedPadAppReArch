package memphis.deeptutor.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class PostTutoringHandler
 */
@WebServlet("/PostTutoringHandler")
public class PostTutoringHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PostTutoringHandler() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handlePostTutoring(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handlePostTutoring(request,response);
	}
	
	private void handlePostTutoring(HttpServletRequest request, HttpServletResponse response) throws IOException{
		System.out.println("I'm in post tutoring handling...");
		//String nextPage = "/loadPosttest";
		String nextPage = "/loadMicroPostTest";
		response.sendRedirect(request.getContextPath() + nextPage);

	}

}
