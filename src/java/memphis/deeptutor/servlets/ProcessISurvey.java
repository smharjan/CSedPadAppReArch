package memphis.deeptutor.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import memphis.deeptutor.model.BusinessModel;
import memphis.deeptutor.model.Student;
import noNamespace.ContextDocument.Context;
import noNamespace.QuestionDocument.Question;

/**
 * Servlet implementation class ProcessImpressionsSurvey
 */
@WebServlet("/ProcessISurvey")
public class ProcessISurvey extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ProcessISurvey()
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
		try
		{
			HttpSession session = request.getSession();
			Student s = (Student) session.getAttribute("student");
			String studentId = s.getGivenId();

			// response.getOutputStream
			String solvedQuestions = request.getParameter("questions");
			if (solvedQuestions != null)
			{
				System.out.println(solvedQuestions);
				String questions[] = solvedQuestions.split("-");
				String contextId = session.getAttribute("nextFcicontextId")
						.toString().trim();
				int nextContextId = Integer.parseInt(contextId);
				Map<Integer, Context> contextMap = (Map<Integer, Context>) session
						.getAttribute("contextMap");
				Question question[] = contextMap.get(nextContextId)
						.getQuestionArray();
				Map<String, Question> qMap = new HashMap<String, Question>();
				for (Question q : question)
				{
					qMap.put(q.getId().intValue() + "", q);
				}

				String explanation = request.getParameter("explanation");
				for (String q : questions)
				{
					String a = request.getParameter("a" + q.trim());
					// model.insertFCIAnswer(studentId, nextContextId, q,
					// a,"posttest", explanation);
					Question qi = qMap.get(q.trim());

					// Dan: adding possibilities for questions with no exact
					// answer

					String type = qi.getDomNode().getAttributes()
							.getNamedItem("type").getNodeValue();
					if (type != null)
					{
						String textToSave = "satisfaction" + "--" + type + "--"
								+ a + "--" + studentId;

						System.out.println(qi.getId() + "--" + "***"
								+ textToSave);

						BusinessModel.getInstance().logThisInfo(s, textToSave);
					}
				}
				nextContextId++;
				session.setAttribute("nextFcicontextId", nextContextId);

			}

			String nextPage = "/loadISurvey";
			RequestDispatcher rd = getServletContext().getRequestDispatcher(
					nextPage);
			rd.forward(request, response);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
