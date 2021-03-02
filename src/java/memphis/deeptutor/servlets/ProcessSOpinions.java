package memphis.deeptutor.servlets;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import memphis.deeptutor.log.DTLogger;
import memphis.deeptutor.model.BusinessModel;
import memphis.deeptutor.model.BusinessModel.DTState;
import memphis.deeptutor.model.Student;

/**
 * Servlet implementation class ProcessSOpinions
 */
@WebServlet("/ProcessSOpinions")
public class ProcessSOpinions extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ProcessSOpinions()
	{
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		DTLogger logger;

		HttpSession session = request.getSession();
		Student s = (Student) session.getAttribute("student");

		DateFormat df = new SimpleDateFormat("MMddyy");
		logger = new DTLogger(s.getGivenId() + "-"
				+ df.format(Calendar.getInstance().getTime()));

		String opinions = request.getParameter("opinions");
		if (opinions != null && !opinions.equals("Write your answer here..."))
		{
			logMessage("Opinion:", opinions, logger);
		}

		String radio_2 = request.getParameter("radio_2");

		if (radio_2 != null)
		{
			if (radio_2.equals("A"))
			{
				logMessage("Preffered:", "READ-ONLY", logger);
			}
			else
			{
				logMessage("Preffered:", "DIALOGUE", logger);
			}
		}

		String interactionTypeComments = request
				.getParameter("interactionType");
		if (interactionTypeComments != null
				&& !interactionTypeComments
						.equals("Explain your answer here..."))
		{
			logMessage("Interaction Type Explanation:",
					interactionTypeComments, logger);
		}

		logger.saveLogInHTML();

		String from2 = request.getParameter("SubmitSOpinions2");

		String nextPage = "/thankyou.jsp";
		if (from2 != null)
		{
			BusinessModel.getInstance().setStudentState(s, DTState.FINISHED);
		}
		else
		{
			nextPage = "/sOpinions2.jsp";
		}

		response.sendRedirect(request.getContextPath() + nextPage);
	}

	private void logMessage(String headLine, String content, DTLogger logger)
	{
		String message = headLine + " " + content;
		System.out.println(message);
		logger.log(DTLogger.Actor.SYSTEM, DTLogger.Level.ONE, message);
	}
}
