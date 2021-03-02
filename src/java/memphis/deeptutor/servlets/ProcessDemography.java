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
import memphis.deeptutor.model.Student;
import memphis.deeptutor.model.BusinessModel.DTState;
import memphis.deeptutor.singleton.DerbyConnector;

/**
 * Servlet implementation class ProcessDemography
 */
@WebServlet("/ProcessDemography")
public class ProcessDemography extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ProcessDemography()
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

		// System.out.println("I'm nobal in process demography.....");
		String skip = request.getParameter("buttonSkip");
		if (skip == null)
		{
			HttpSession session = request.getSession();
			Student s = (Student)session.getAttribute("student");

			DateFormat df = new SimpleDateFormat("MMddyy");
			logger = new DTLogger(s.getGivenId() + "-"
					+ df.format(Calendar.getInstance().getTime()));

			String gender = request.getParameter("gender");
			String race = request.getParameter("ethnicity");
			String age = request.getParameter("age");
			// String school = request.getParameter("school");

			// Dan: disable school and major and add mostAdvancedClass
			// String school = ""; // disable for now.
			String major = request.getParameter("major");

			String mostAdvancedClass = request
					.getParameter("mostAdvancedClass");
			String education = request.getParameter("educationLevel");
			String gpa = request.getParameter("gpa");
			// Boolean fm = (request.getParameter("fm") != null);
			// Boolean law = (request.getParameter("law") != null);
			// Boolean gravity = (request.getParameter("gravity") != null);
			// Boolean friction = (request.getParameter("friction") != null);
			// Boolean atomicstructure =
			// (request.getParameter("atomicstructure") != null);
			String priorPhysics = request.getParameter("txtPriorPhysics");
			// String currentPhysics =
			// request.getParameter("txtCurrentPhysics");

			// Dan: display only fields that we use
			// System.out.println("Demographics data: " + givenId + ", " +
			// gender + ", " + race + ", " + age + ", " + school + ", " + major
			// + ", " + education + ", "
			// + gpa + ", " + fm + ", " + law + ", " + gravity + ", " + friction
			// + ", " + atomicstructure + ", " + priorPhysics + ", " +
			// currentPhysics);

			String gpaString = gpa;
			if (gpaString == "")
				gpaString = "notSpecified";

			String priorPhysicsString = priorPhysics;
			if (priorPhysicsString == null)
				priorPhysicsString = "notSpecified";

			String demographicData = "Demographics: " + s.getGivenId() + ", " + gender
					+ ", " + race + ", " + age + ", " + mostAdvancedClass
					+ ", " + education + ", " + gpaString + ", " + ", "
					+ priorPhysicsString;

			System.out.println(demographicData);
			logger.log(DTLogger.Actor.SYSTEM, DTLogger.Level.ONE,
					demographicData);
			logger.saveLogInHTML();

			s.setGender(gender.charAt(0));
			s.setEthnicity(race);
			try
			{
				s.setAge(Integer.parseInt(age));
			}
			catch (Exception e)
			{
				s.setAge(-1);
			}

			// Dan: updates only fields that we use
			// s.setSchool(school);
			s.setMajor(major);
			s.setMostAdvancedClass(mostAdvancedClass);
			s.setEducationLevel(education);
			s.setGpa(gpa);
			// s.setFamiliarAreas((fm ? "T" : "*") + (law ? "T" : "*")
			// + (gravity ? "T" : "*") + (friction ? "T" : "*")
			// + (atomicstructure ? "T" : "*"));
			s.setPriorCourses(priorPhysics);
			// s.setCurrentCourses(currentPhysics);

			BusinessModel.getInstance().setStudentState(s, DTState.FINISHED);

			DerbyConnector.getInstance().updateDemographics(s);
		}

		// Dan: change application flow: go to end (thank you) instead of
		// String nextPage = "/login.jsp";
		
		HttpSession session = request.getSession();
		session.setAttribute("nextFcicontextId", null);
		String nextPage = "/thankyou.jsp";
		
		// HttpSession session = request.getSession();
		// session.invalidate();
		response.sendRedirect(request.getContextPath() + nextPage);

		/*
		 * Boolean watchTutorial; if
		 * (request.getSession().getAttribute("watchTutorial")!=null)
		 * watchTutorial = true; else watchTutorial =
		 * (request.getParameter("watchTutorial").equals("yes"));
		 * 
		 * request.getSession().removeAttribute("nextFcicontextId"); if
		 * (watchTutorial) { nextPage="/tutorial.jsp";
		 * request.getSession().setAttribute("nextPage","loadFCIQuestions");
		 * DerbyConnector.getInstance().setHasSeenTutorial(s); } else nextPage =
		 * "/loadFCIQuestions"; response.sendRedirect(request.getContextPath() +
		 * nextPage);
		 */
	}

}
