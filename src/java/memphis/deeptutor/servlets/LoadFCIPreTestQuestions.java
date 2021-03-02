package memphis.deeptutor.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import noNamespace.ContextDocument;
import noNamespace.ContextDocument.Context;

import memphis.deeptutor.fci.FciXmlParser;
import memphis.deeptutor.log.DTLogger;
import memphis.deeptutor.model.BusinessModel.DTState;
import memphis.deeptutor.model.BusinessModel;
import memphis.deeptutor.model.LPModel;
import memphis.deeptutor.model.Student;
import memphis.deeptutor.singleton.DerbyConnector;

/**
 * Servlet implementation class LoadFCIQuestions
 */
@WebServlet("/LoadFCIPretestQuestions")
public class LoadFCIPreTestQuestions extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LoadFCIPreTestQuestions()
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
		loadFCI(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		loadFCI(request, response);
	}

	@SuppressWarnings("unchecked")
	private void loadFCI(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		// Data will be saved by ProcessFCIQuestions, so no need to store here
		// Possible cases to be here:
		// i. FCI.jsp -> ProcessFCIQuestions -> LoadFCIQuestions
		// ii. LoadFCIQuestions -> LoadFCIQuestions
		try
		{

			HttpSession session = request.getSession();

			// 1. Check if any FCI context are loaded. If not, load them now
			int nextContextId = 1;
			String nextCid = null;
			if (session != null)
			{
				try
				{
					nextCid = session.getAttribute("nextFcicontextId")
							.toString();
				}
				catch (Exception e)
				{
					// e.printStackTrace();
				}
			}

			String fciFileName = "FCI-Blended-A.xml";

			Map<Integer, ContextDocument.Context> contextMap = null;

			if (nextCid != null)
			{
				nextContextId = Integer.parseInt(nextCid);
			}
			else
			{
				// if we don't have a context id, then we need to compute it
				// from the saved data
				Student s = (Student) session.getAttribute("student");
				if (s == null)
				{
					String gotoGUI = "/login.jsp";
					response.sendRedirect(request.getContextPath() + gotoGUI);
					return;
				}

				// choose the type of pre/post test
				if (s.getPreTest().equalsIgnoreCase("A"))
				{
					fciFileName = "FCI-Blended-A.xml";
				}
				else
				{
					fciFileName = "FCI-Blended-B.xml";
				}
				System.out.println("The pretest file is: " + fciFileName);
				// fciFileName ="Bao.xml";
				BusinessModel.getInstance().logThisInfo(s, "PRETESTSTART");
				DerbyConnector.getInstance().getStudentEvaluation(s, "pretest");

				boolean contextFound;
				// incrementally try to see which questions have been answered
				nextContextId = 0;
				do
				{
					contextFound = false;
					nextContextId++;

					String strNextContextId = "" + nextContextId;
					if (s.evaluationContext.contains(strNextContextId))
						contextFound = true;
				} while (contextFound);

				// "C:\\Nobal\\workspace\\DeeptutorApp\\WebContent\\FCI\\FCI_complete.xml"
				// String
				// xmlPath=session.getServletContext().getRealPath("/FCI")+"/FCI_partial.xml";
				String xmlPath = session.getServletContext()
						.getRealPath("/FCI") + "/" + fciFileName;
				contextMap = FciXmlParser.getContexts(xmlPath);

				session.setAttribute("contextMap", contextMap);
				session.setAttribute("nextFcicontextId", nextContextId);

				/*
				 * if showing first pre test screen, then should show a dialogue
				 * suggesting student to carefully read and answer
				 */
				/*
				 * if (session.getAttribute("firstPreTestScreen") == null) {
				 * session.setAttribute("firstPreTestScreen", true); }
				 */

				// check if we need to give test to student
				if (s.getDTState() != DTState.PRETEST)
				{
					session.removeAttribute("nextFcicontextId");
					String gotoGUI = "/GUI/gui.jsp";
					BusinessModel.getInstance().logThisInfo(s, "PRETESTEND");

					response.sendRedirect(request.getContextPath() + gotoGUI);
					return;
				}
			}

			contextMap = (Map<Integer, Context>) session
					.getAttribute("contextMap");
			// 2. If Finished, forward to post FCI page
			if (nextContextId > contextMap.size())
			{
				session.removeAttribute("nextFcicontextId");
				
				Student s = (Student) session.getAttribute("student");
				LPModel lp = new LPModel();
				lp.LogStudentLP(s.getGivenId(), "pretest");

				calculateAndLogPercentCorrect(session, s);

				BusinessModel.getInstance()
						.setStudentState(s, DTState.FINISHED);
				session.removeAttribute("nextFcicontextId");
				// String nextPage = "/IntroductionToNewtonsLaws.jsp";
				String nextPage = "/thankyou.jsp";
				BusinessModel.getInstance().logThisInfo(s, "PRETESTEND");
				response.sendRedirect(request.getContextPath() + nextPage);
				return;
			}
			else
			{
				// 3. If already loaded, load the next context now
				Context toBeLoaded = contextMap.get(nextContextId);
				session.setAttribute("context", toBeLoaded);
				if (toBeLoaded.getContextdescription().trim().length() > 1
						|| toBeLoaded.getContextpicture().getSrc()
								.getStringValue().length() > 1)
				{
					String contextDescription = toBeLoaded
							.getContextdescription().replaceAll("\\n", "<br/>");
					session.setAttribute("contextDesc", contextDescription);
					session.setAttribute("contextPics",
							toBeLoaded.getContextpicture());
				}
				else
				{
					session.setAttribute("contextDesc", null);
					session.setAttribute("contextPics", null);
				}

				// 4. Go to FCI
				String gotoFCI = "/pretest.jsp";
				RequestDispatcher rd = getServletContext()
						.getRequestDispatcher(gotoFCI);
				rd.forward(request, response);
				return;
			}

		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Once the pretest is complete, log the percent correct answers for quick
	 * observation
	 */
	private void calculateAndLogPercentCorrect(HttpSession session, Student s)
	{
		// Before moving to the dialogue, calculate the percentage correct
		// answer and log.
		DerbyConnector.getInstance().getStudentEvaluation(s, "pretest");
		String fciFile = s.getPreTest().equalsIgnoreCase("A") ? "FCI-Blended-A.xml"
				: "FCI-Blended-B.xml";
		String xmlPath = session.getServletContext().getRealPath("/FCI") + "/"
				+ fciFile;
		Map<String, String> expectedAnswers = FciXmlParser
				.getFCIAnswers(xmlPath);
		int correctCount = 0;
		String expected = "";
		String actual = "";
		for (String qid : s.evaluationData.keySet())
		{
			actual = s.evaluationData.get(qid);
			expected = expectedAnswers.get(qid);
			if (actual.equalsIgnoreCase(expected))
				correctCount++;
		}
		double pctCorrect = 100.0 * correctCount / s.evaluationData.size();
		
		DerbyConnector.getInstance().saveStudentPreTestScore(s.getGivenId(), pctCorrect, s.comesThroughDispatcher());
		
		BusinessModel.getInstance().logThisInfo(
				s,
				" CORRECTLY ANSWERED: " + correctCount + "/"
						+ s.evaluationData.size() + "(="
						+ String.format("%2.2f", pctCorrect) + "%)");
		BusinessModel.getInstance().logThisInfo(s,
				"FCI QUESTION FILE USED:  " + fciFile);
	}

}
