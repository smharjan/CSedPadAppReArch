// Dan: Servlet which takes care of the mode a user is in... especially designed for the new SHOWANSWER display

package memphis.deeptutor.servlets;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import memphis.deeptutor.log.DTLogger;
import memphis.deeptutor.model.BusinessModel;
import memphis.deeptutor.model.Student;
import memphis.deeptutor.model.BusinessModel.DTState;
import memphis.deeptutor.servlets.DTConstants.Status;
import memphis.deeptutor.singleton.DerbyConnector;

/**
 * Servlet implementation class DTStudentVerifier
 */
@WebServlet(description = "Servlet that verifies whether a student is a legitimate one", urlPatterns = { "/DTStudentVerifier" })
public class DTStudentVerifier extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DTStudentVerifier()
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
		HttpSession session = request.getSession(true);
		Enumeration<String> attributes = session.getAttributeNames();

		boolean fromAgreementPage = request.getParameter("agreementPage") != null;

		if (!fromAgreementPage)
		{
			while (attributes.hasMoreElements())
			{
				String attribute = attributes.nextElement();
				session.removeAttribute(attribute);
			}
		}

		// session.removeAttribute("isBill");
		// session.removeAttribute("error");

		String givenId = request.getParameter("txtGivenId");
		Student s = null;
		if (givenId != null) // if this user is visiting first time,
		{
			if (request.getParameter("enterDemographics") != null)
				session.setAttribute("enterDemographics", "true");
			// else
			// session.removeAttribute("enterDemographics");

			if (request.getParameter("watchTutorial") != null)
				session.setAttribute("watchTutorial", "true");
			// else
			// session.removeAttribute("watchTutorial");

			if (request.getParameter("html5Mode") != null)
				session.setAttribute("html5Mode", "true");
			// else
			// session.removeAttribute("html5Mode");

			// means we are on the login page
			String password = request.getParameter("txtPassword");
			// System.out.println(givenId + " " + password);

			s = new Student(givenId);
			s.setPassword(password);
		}
		else
		{
			s = (Student) session.getAttribute("student");
			// we must be at the terms and agreements page
		}

		String watchTutorial = (String) session.getAttribute("watchTutorial");

		BusinessModel model = new BusinessModel();

		// check if we have a database user
		Student s1 = s;
		if (s.getSession_1_start() == null)
		{
			s1 = model.getStudentThroughDispatcher(s);
		}
		// Student s1 = model.getStudentFromDatabase(s);

		if (s1 == null)
		{
			// this is special cases for guests
			if (model.getStudentStatus(s) == Status.GUEST)
				s1 = s;
		}
		else
		// student was found in the database
		{
			DateFormat df = new SimpleDateFormat("MMddyy");
			// Dan: initially it was s instead of s1
			DTLogger logger = new DTLogger(s1.getGivenId() + "-"
					+ df.format(Calendar.getInstance().getTime()));
			String userAgent = request.getHeader("User-Agent");
			logger.log(DTLogger.Actor.SYSTEM, DTLogger.Level.ONE,
					new Date() + ": " + s1.getGivenId()
							+ " logged in; condition: " + s1.getDtMode() + "; browser: " + userAgent);
			logger.saveLogInHTML();
			
			if (fromAgreementPage)
			{
				DerbyConnector.getInstance().saveStudentTermsAndAgreements(
						s1.getGivenId(), s1.comesThroughDispatcher());
				s1.setHasAcceptedTermsAndConditions(true);
			}

			DerbyConnector.getInstance().getStudentLearningModel(s1);
		}
		session.setAttribute("student", s1);
		System.out.println("Is valid student =" + (s1 != null));

		if (s1 != null)
		{
			session.setAttribute("isValidUser", true);
			session.setAttribute("isValidStudent", true);

			// check if student must wait for woz
			if (s1.wait4woz
					&& !InitDTServlet.wozHandler.isTutorConnected(s1
							.getGivenId()))
			{
				session.setAttribute(
						"error",
						"Opps. Seems like DeepTutor is currently busy. Please inform your assigned experimenter and try login in again later.");
				String destinationOnError = "/login.jsp";
				RequestDispatcher rd = getServletContext()
						.getRequestDispatcher(destinationOnError);
				rd.forward(request, response);
				return;
			}

			// Dan: initially it was s instead of s1
			DTConstants.Status stat = model.getStudentStatus(s1);
			RequestDispatcher rd = null;

			System.out.println("status:" + stat);
			switch (stat)
			{
			case NEW:
				if (!s1.hasAcceptedTermsAndConditions())
				{
					String nextPage = "/agreement.jsp";
					rd = getServletContext().getRequestDispatcher(nextPage);
					rd.forward(request, response);
					return;
				}
				// Now assign student mode,pretest and post test
				if (!BusinessModel.getInstance()
						.hasAssignedModePretestAndPostTest(s1))
				{
					BusinessModel.getInstance()
							.assignModePretestAndPostTest(s1);
					s1 = BusinessModel.getInstance()
							.getStudentFromDatabaseByID(s.getGivenId());
				}
				// Add time to track the time elapsed
				session.setAttribute("sessionStartTime", new Date());
				session.setAttribute("student", s1);
				if (s1.comesThroughDispatcher())
				{
					String nextPage = "/roadMap.jsp";

					if (s1.getGivenId().endsWith("_1")
							|| (!s1.getGivenId().endsWith("_0")
									&& !s1.getGivenId().endsWith("_4") && s1
									.getLastLogInDate() == null))
					{
						nextPage = "/welcome.jsp";
					}

					response.sendRedirect(request.getContextPath() + nextPage);
				}
				else
				{
					String nextPage = "/continueBC";
					rd = getServletContext().getRequestDispatcher(nextPage);
					rd.forward(request, response);
				}
				return;

			case BILL:
				session.setAttribute("isBill", true);
			case GUEST:
			case TEACHERS:
				s1.setSpecialStudent(true);

				session.setAttribute("isValidStudent", false);
				session.setAttribute("student", s1);

				if (!s1.hasAcceptedTermsAndConditions())
				{
					String nextPage = "/agreement.jsp";
					rd = getServletContext().getRequestDispatcher(nextPage);
					rd.forward(request, response);
					return;
				}
				else
				{
					String html5Mode = (String) session
							.getAttribute("html5Mode");
					String nextPage = "GUI/gui.jsp";
					if (html5Mode != null)
						nextPage = "GUI/html5gui.jsp";

					if (watchTutorial != null)
					{
						DerbyConnector.getInstance().setHasSeenTutorial(s);
						session.setAttribute("nextPage", nextPage);
						nextPage = "tutorial.jsp";
					}
					response.sendRedirect(request.getContextPath() + "/"
							+ nextPage);
				}
				break;
			}
		}
		else
		{
			session.setAttribute("isValidUser", false);
			session.setAttribute("error",
					"Something is wrong with your input. Try again!");
			String destinationOnError = "/login.jsp";
			RequestDispatcher rd = getServletContext().getRequestDispatcher(
					destinationOnError);
			rd.forward(request, response);
		}
	}

}
