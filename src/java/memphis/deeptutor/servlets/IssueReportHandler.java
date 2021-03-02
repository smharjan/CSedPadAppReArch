package memphis.deeptutor.servlets;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import memphis.deeptutor.log.DTLogger;
import memphis.deeptutor.log.DTLogger.Actor;
import memphis.deeptutor.log.DTLogger.Level;
import memphis.deeptutor.singleton.ConfigManager;

/**
 * Servlet implementation class IssueReportHandler
 */
@WebServlet(description = "Servlet that hendles the issue report", urlPatterns = { "/IssueReportHandler" })
public class IssueReportHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public IssueReportHandler() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		System.out.println("Now action handling...");
		String logFileName = "Issue-Report";
		String nextPage = "/ThankYouIssueReporter.html";
		String issueText = request.getParameter("userInputText");
		//logger creates file itself.
		DTLogger logger = new DTLogger(logFileName);
		logger.log(Actor.NONE, Level.ONE, issueText);
		logger.saveLogInHTML();		
		response.sendRedirect(request.getContextPath() + nextPage);
		
	}

}
