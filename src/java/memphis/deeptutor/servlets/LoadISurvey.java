package memphis.deeptutor.servlets;

import java.io.IOException;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import memphis.deeptutor.fci.FciXmlParser;
import memphis.deeptutor.model.BusinessModel;
import memphis.deeptutor.model.LPModel;
import memphis.deeptutor.model.Student;
import memphis.deeptutor.model.BusinessModel.DTState;
import memphis.deeptutor.singleton.DerbyConnector;
import noNamespace.ContextDocument;
import noNamespace.ContextDocument.Context;

/**
 * Servlet implementation class LoadImpressionsSurvey
 */
@WebServlet("/LoadISurvey")
public class LoadISurvey extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LoadISurvey()
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
		loadISurvey(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		loadISurvey(request, response);
	}

	@SuppressWarnings("unchecked")
	private void loadISurvey(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
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

			String fileName = "iSurvey.questions.xml";

			Map<Integer, ContextDocument.Context> contextMap = null;

			if (nextCid != null)
			{
				nextContextId = Integer.parseInt(nextCid);
				contextMap = (Map<Integer, Context>) session
						.getAttribute("contextMap");
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

				BusinessModel.getInstance()
						.logThisInfo(s, "IMPRESSIONSSTART");

				// you come here if your dialogue is finished. change the state
				if (s.getDTState() == DTState.DEMOGPHY)
				{
					BusinessModel.getInstance().setStudentState(s,
							DTState.ISURVEY);
				}

				 DerbyConnector.getInstance().getStudentEvaluation(s, "isurvey");
				
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
						.getRealPath("/FCI") + "\\" + fileName;
				contextMap = FciXmlParser.getContexts(xmlPath);

				session.setAttribute("contextMap", contextMap);
				session.setAttribute("nextFcicontextId", nextContextId);
			}
			// 2. If Finished, forward to post FCI page

			// LMC - TODO - I temporarily removed the FCI for student users
			if (nextContextId > contextMap.size())
			{
				session.removeAttribute("nextFcicontextId");
				
				Student s = (Student) session.getAttribute("student");
				LPModel lp = new LPModel();

				BusinessModel.getInstance().logThisInfo(s, "IMPRESSIONSEND");
				
				//session.invalidate();

				String nextPage = "/sOpinions1.jsp";
				BusinessModel.getInstance()
						.setStudentState(s, DTState.OPINIONS);
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

				// 4. Go to Impressions Survey
				String gotoIS = "/iSurvey.jsp";
				RequestDispatcher rd = getServletContext()
						.getRequestDispatcher(gotoIS);
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
}
