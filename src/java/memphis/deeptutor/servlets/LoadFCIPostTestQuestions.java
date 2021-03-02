package memphis.deeptutor.servlets;

import java.io.File;
import java.io.IOException;
import java.util.Map;

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
import memphis.deeptutor.model.BusinessModel;
import memphis.deeptutor.model.BusinessModel.DTState;
import memphis.deeptutor.model.LPModel;
import memphis.deeptutor.model.Student;
import memphis.deeptutor.singleton.DerbyConnector;

/**
 * Servlet implementation class LoadFCIQuestions
 */
@WebServlet("/LoadFCIPostTestQuestions")
public class LoadFCIPostTestQuestions extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LoadFCIPostTestQuestions()
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

			// Dan: posttest file is only one
			String fciFileName = "FCI-Blended-B.xml";
			// choose the type of pre/post test
			// if (s.getPostTest().equalsIgnoreCase("A"))
			// {
			// fciFileName = "FCI-Blended-A.xml";
			// }

			// Dan: April 2013 PostTest
			//String fciFileName = "PostTest.Spring.2013.xml";

			String xmlPath = session.getServletContext().getRealPath("/FCI")
					+ "/" + fciFileName;

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

				BusinessModel.getInstance().logThisInfo(s, "POSTTESTSTART");

				// you come here if your dialogue is finished. change the state
				if (s.getDTState() == DTState.DIALOGUE)
				{
					BusinessModel.getInstance().setStudentState(s,
							DTState.POSTTEST);
				}

				DerbyConnector.getInstance()
						.getStudentEvaluation(s, "posttest");

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
				contextMap = FciXmlParser.getContexts(xmlPath);

				session.setAttribute("contextMap", contextMap);
				session.setAttribute("nextFcicontextId", nextContextId);
			}

			contextMap = (Map<Integer, Context>) session
					.getAttribute("contextMap");
			// 2. If Finished, forward to post FCI page

			// LMC - TODO - I temporarily removed the FCI for student users
			if (nextContextId > contextMap.size())
			{
				session.removeAttribute("nextFcicontextId");
				
				Student s = (Student) session.getAttribute("student");
				LPModel lp = new LPModel();
				lp.LogStudentLP(s.getGivenId(), "posttest");

				calculateAndLogPercentCorrect(xmlPath, session, s);
				BusinessModel.getInstance().logThisInfo(s, "POSTTESTEND");

				String nextPage = "/demography.jsp";
				BusinessModel.getInstance()
						.setStudentState(s, DTState.DEMOGPHY);
				response.sendRedirect(request.getContextPath() + nextPage);
				return;
			}
			else
			{
				// 3. If already loaded, load the next context now
				Context toBeLoaded = contextMap.get(nextContextId);
				session.setAttribute("context", toBeLoaded);
				if (toBeLoaded.getContextdescription().trim().length() > 1)
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
				String gotoFCI = "/posttest.jsp";
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
	 * Once the Posttest is complete, log the percent correct answers for quick
	 * observation
	 */
	private void calculateAndLogPercentCorrect(String xmlPath,
			HttpSession session, Student s)
	{
		// Before moving to the dialogue, calculate the percentage correct
		// answer and log.
		DerbyConnector.getInstance().getStudentEvaluation(s, "posttest");
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
		BusinessModel.getInstance().logThisInfo(
				s,
				"CORRECTLY ANSWERED: " + correctCount + "/"
						+ s.evaluationData.size() + "(="
						+ String.format("%2.2f", pctCorrect) + "%)");
		BusinessModel.getInstance().logThisInfo(s,
				"FCI QUESTION FILE USED:  " + (new File(xmlPath)).getName());
	}

}
