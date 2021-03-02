package memphis.deeptutor.servlets;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javax.servlet.RequestDispatcher;

import memphis.deeptutor.log.DTLogger;
import memphis.deeptutor.model.BusinessModel.DTMode;
import memphis.deeptutor.model.BusinessModel.DTState;
import memphis.deeptutor.model.Student;
import memphis.deeptutor.singleton.DerbyConnector;

/**
 * Servlet implementation class DTConditionEvaluator
 */
@WebServlet("/DTConditionEvaluator")
public class DTConditionEvaluator extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DTConditionEvaluator()
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

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		// TODO Auto-generated method stub

		HttpSession session = request.getSession();
		Student student = (Student) session.getAttribute("student");
		// BusinessModel model = new BusinessModel();
		// Student student = model.getStudentFromDatabase(s);

		DateFormat df = new SimpleDateFormat("MMddyy");
		DTLogger logger = new DTLogger(student.getGivenId() + "-"
				+ df.format(Calendar.getInstance().getTime()));

		String taskFinishedString = request.getParameter("hasReadTask");
		String introFinishedString = request.getParameter("hasReadIntro");

		String introName = null;

		String watchTutorial = (String) session.getAttribute("watchTutorial");
		String nextPage = "/GUI/gui.jsp";

		int introFinished = 0;
		if (introFinishedString != null)
		{
			introFinished = Integer.parseInt(introFinishedString);
		}

		if (introFinished == 9000)
		{
			nextPage = "/thankyou.jsp";
			response.sendRedirect(request.getContextPath() + nextPage);
			return;
		}
		// Dan: if previous finished session is in the same day; go directly to
		// the end; this is implemented as it is, because it's very easy to
		// disable it (if commenting, be sure to comment also the code in
		// roadMap)
		else if (introFinished == 9899)
		{
			nextPage = "/thankyou.jsp";
			student.setStopCode(9899);
			// student.setLoggedInSameDay(true);
			response.sendRedirect(request.getContextPath() + nextPage);
			return;
		}

		if (student.getDTState() == DTState.FINISHED)
		{
			nextPage = "/thankyou.jsp";
		}
		else if (student.getDTState() == DTState.PRETEST)
		{
			if (introFinished != 9999)
			{
				request.getSession().removeAttribute("nextFcicontextId");
				nextPage = "/loadPretest";
				if (watchTutorial != null)
				{
					DerbyConnector.getInstance().setHasSeenTutorial(student);
					session.removeAttribute("watchTutorial");
					session.setAttribute("nextPage", "verify");
					nextPage = "/tutorial.jsp";
					// student.setTooLateToday(false);
				}
			}
			else
			{
				nextPage = "/thankyou.jsp";
				student.setStopCode(9999);
				// student.setTooLateToday(true);
			}
		}
		else if (student.getDTState() == DTState.POSTTEST)
		{
			if (introFinished == 0)
			{
				nextPage = "/loadPosttest";
				// student.setTooLateToday(false);
			}
			else
			{
				nextPage = "/thankyou.jsp";
				student.setStopCode(9999);
				// student.setTooLateToday(true);
			}
		}
		else if (student.getDTState() == DTState.MPOSTTEST)
		{
			nextPage = "/loadMicroPostTest";
		}
		else if (student.getDTState() == DTState.DEMOGPHY)
		{
			nextPage = "/demography.jsp";
		}
		else if (student.getDTState() == DTState.ISURVEY)
		{
			nextPage = "/loadISurvey";
		}
		else if (student.getDTState() == DTState.OPINIONS)
		{
			nextPage = "/sOpinions1.jsp";
		}
		else if (student.getDTState() == DTState.DIALOGUE)
		{
			// Dan: logging data about intro pages

			if (!student.comesThroughDispatcher())
			{
				introFinishedString = "0";
			}

			if (introFinishedString != null)
			{
				switch (introFinished)
				{
				case 0:
					introName = "Student's Progress";
					nextPage = "/intro1.jsp";
					break;
				// case 1:
				// introName = "Welcome";
				// nextPage = "/intro2.jsp";
				// break;
				case 1:
					introName = "General";
					nextPage = "/intro2.jsp";
					break;
				case 2:
					introName = "Forces";
					nextPage = "/intro3.jsp";
					break;
				case 3:
					introName = "Newton-Laws";
					nextPage = "/intro4.jsp";
					break;
				case 4:
					introName = "Problem-Solving";
					nextPage = "/intro5.jsp";
					break;
				case 5:
					introName = "Chatting";
					if (student.getDtMode() != DTMode.SHOWANSWERS)
					{
						nextPage = "/tutorial.jsp";
					}
					break;
				case 6:
					introName = "Tutorial";
					break;
				// case 9999:
				// introName = "Student's Progress";
				// nextPage = "/thankyou.jsp";
				// break;
				}
				logger.log(DTLogger.Actor.SYSTEM, DTLogger.Level.ONE,
						new Date() + ": " + student.getGivenId() + " finished "
								+ introName + " intro.");
				logger.saveLogInHTML();
			}

			if (introFinishedString == null
					|| (introName != null && (introName.equals("Tutorial") || (introName
							.equals("Chatting") && (student.getDtMode() == DTMode.SHOWANSWERS)))))
			{
				DerbyConnector.getInstance().getStudentLearningModel(student);

				if (taskFinishedString != null)
				{
					if (student.completedTasks == null)
					{
						student.completedTasks = taskFinishedString;
					}
					else
					{
						student.completedTasks += " " + taskFinishedString;
					}

					student.currentTask = findNextTask(student.getTaskString(),
							student.completedTasks);
					DerbyConnector.getInstance().saveStudentLearningModel(
							student);
				}

				if (student.getDtMode().name()
						.equals(DTMode.SHOWANSWERS.name()))
				{
					String taskSequence = student.getTaskString();

					if (student.completedTasks == null)
					{
						student.currentTask = findNextTask(
								student.getTaskString(), student.completedTasks);

						nextPage = "/ReadCondition.jsp";
					}
					else
					{
						String remainingTaskSequence = taskSequence.replace(
								student.completedTasks, "").trim();

						logger.log(DTLogger.Actor.SYSTEM, DTLogger.Level.ONE,
								new Date() + ": " + student.getGivenId()
										+ " finished reading "
										+ taskFinishedString);
						logger.saveLogInHTML();

						if (!remainingTaskSequence.isEmpty())
						{
							nextPage = "/ReadCondition.jsp";
						}
						else
						{
							nextPage = "/loadMicroPostTest";
							RequestDispatcher rd = getServletContext()
									.getRequestDispatcher(nextPage);
							rd.forward(request, response);
							return;
						}
					}
				}
			}
		}

		response.sendRedirect(request.getContextPath() + nextPage);
		return;
	}

	private String findNextTask(String taskString, String completedTasks)
	{
		String ret = taskString;
		if (completedTasks != null)
		{
			ret = taskString.replace(completedTasks, "").trim();
		}
		if (ret.contains(" "))
		{
			ret = ret.substring(0, ret.indexOf(" "));
		}
		return ret;
	}
}
