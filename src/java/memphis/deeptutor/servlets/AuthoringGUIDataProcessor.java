package memphis.deeptutor.servlets;

import java.io.File;
import java.io.IOException;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import memphis.deeptutor.dialog.TaskManager;
import memphis.deeptutor.model.ExpectAnswer;
import memphis.deeptutor.model.Expectation;
import memphis.deeptutor.model.Expectation.EXPECT_TYPE;
import memphis.deeptutor.model.Task;
import memphis.deeptutor.singleton.ConfigManager;

/**
 * Servlet implementation class AuthoringGUIDataProcessor
 */
@WebServlet("/AuthoringGUIDataProcessor")
public class AuthoringGUIDataProcessor extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AuthoringGUIDataProcessor()
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

		String destination = "/DTAuthoring/Tasks.jsp";

		// Misconception Data holders
		String miscId = "";
		List<String> miscTexts = new ArrayList<String>();
		String miscAssertion = "";
		String miscPump = "";
		String miscBonus = "";
		String miscReqd = "";
		String miscForbidden = "";
		String miscYoked = "";

		// Expectation Data Holder
		String expId = "";
		String expOrder = "";
		String expType = "";
		String expDesc = "";
		String expBonus = "";
		String expRequired = "";
		String expWrong = "";
		List<String> expRequiredGwF = null;

		String expForbidden = "";
		List<String> expTextList = new ArrayList<String>();
		String expAssertion = "";
		String expPostImg = "";
		String expPump = "";
		String expAltPump = "";

		String expPromptTxt = "";
		String expPromptAns = "";
		String expPromptWrong = "";
		List<String> expPromptGwF = null;
		String expPromptNeg = "";
		List<String> expHintList = new ArrayList<String>();
		List<List<String>> expHintGwF = new ArrayList<List<String>>();

		List<String> tempGwF = new ArrayList<String>();

		// Get all request parameters
		Enumeration<String> keys = request.getParameterNames();

		// Iterate through all the input data to populate the variables
		while (keys.hasMoreElements())
		{
			String key = (String) keys.nextElement();
			// System.out.println("key:"+key);
			if (key.matches("gwf.*"))
				tempGwF.add(request.getParameter(key).trim());

			// Get data related to misconceptions
			if (key.toLowerCase().contains("misc"))
			{
				// out.print("<br>MISC-->"+key);
				if (key.matches("txtMiscText.*"))
				{
					if (request.getParameter(key).trim().length() > 0)
						miscTexts.add(request.getParameter(key).trim());
				}
				if (key.matches("misc.*id"))
					miscId = request.getParameter(key).trim();
				if (key.matches("txtMiscAssert"))
					miscAssertion = request.getParameter(key).trim();
				if (key.matches("txtMiscBump"))
					miscPump = request.getParameter(key).trim();
				if (key.matches("txtMiscBonus"))
					miscBonus = request.getParameter(key).trim();
				if (key.matches("txtMiscReqd"))
					miscReqd = request.getParameter(key).trim();
				if (key.matches("txtMiscForbidden"))
					miscForbidden = request.getParameter(key).trim();
				if (key.matches("txtMiscYoked"))
					miscYoked = request.getParameter(key).trim();
			}

			// Get data related to Expectations
			if (key.toLowerCase().startsWith("exp"))
			{
				if (key.matches("expId"))
					expId = request.getParameter(key).trim();
				if (key.matches("expOrder"))
					expOrder = request.getParameter(key).trim();
				if (key.matches("expType"))
					expType = request.getParameter(key).trim();
				if (key.matches("expDesc"))
					expDesc = request.getParameter(key).trim();
				if (key.matches("expBonus"))
					expBonus = request.getParameter(key).trim();
				if (key.matches("expRequired"))
					expRequired = request.getParameter(key).trim();
				if (key.matches("expWrong"))
					expWrong = request.getParameter(key).trim();
				if (key.matches("expForbidden"))
				{
					// safe required GwF
					expRequiredGwF = tempGwF;
					tempGwF = new ArrayList<String>();

					expForbidden = request.getParameter(key).trim();
				}

				// list
				if (key.matches("expText.*")
						&& request.getParameter(key).trim().length() > 0)
				{
					// System.out.println("EXP:"+request.getParameter(key));
					expTextList.add(request.getParameter(key).trim());
				}
				if (key.matches("expAssertion"))
					expAssertion = request.getParameter(key).trim();
				if (key.matches("expPostImg"))
					expPostImg = request.getParameter(key).trim();
				if (key.matches("expPump"))
					expPump = request.getParameter(key).trim();
				if (key.matches("expAltPump"))
					expAltPump = request.getParameter(key).trim();

				// Prompt
				if (key.matches("expPromptTxt"))
					expPromptTxt = request.getParameter(key).trim();
				if (key.matches("expPromptAns"))
					expPromptAns = request.getParameter(key).trim();
				if (key.matches("expPromptWrong"))
					expPromptWrong = request.getParameter(key).trim();

				if (key.matches("expPromptNeg"))
				{
					// safe prompt GwF
					expPromptGwF = tempGwF;
					tempGwF = new ArrayList<String>();

					expPromptNeg = request.getParameter(key).trim();
				}

				// Hint List
				if (key.matches("expHint.*"))
				{

					if (key.matches("expHintNeg.*"))
					{
						// safe hint GwF
						expHintGwF.add(tempGwF);
						tempGwF = new ArrayList<String>();
					}

					expHintList.add(request.getParameter(key).trim());
				}

			}
		}

		// Task Data Holders
		String commandType = request.getParameter("commandType");
		String taskName = request.getParameter("tskFileName");
		String taskID = request.getParameter("tskId");
		String taskText = request.getParameter("tskText1");
		String taskText2 = request.getParameter("tskText2");
		String taskImage = request.getParameter("tskImage");
		String taskMultimedia = request.getParameter("tskMultimedia");
		String taskIntroduction = request.getParameter("tskIntroduction");
		String taskSummary = request.getParameter("tskSummary");

		HttpSession session = request.getSession();
		// session.setAttribute("misConceptionTextsCount", value);
		// session.setAttribute("currentTaskId", value);
		// session.setAttribute("misConceptionTextsCount", value);
		// session.setAttribute("expTextsCount", value);
		// session.setAttribute("hintId", value);

		session.setAttribute("authoringMsg", "");

		// validate data
		if (expId.length() > 0 && expAssertion.trim().length() == 0)
		{
			expAssertion = "[assertion missing]";
			session.setAttribute("authoringMsg",
					"You must define the assertion for the expectation.");
		}

		if (miscId.length() != 0 && miscTexts.size() == 0)
		{
			miscTexts.add("[text missing]");
			session.setAttribute("authoringMsg",
					"You must define at least one text for the missconception.");
		}

		if (expId.length() > 0 && expPromptTxt.length() > 0
				&& expPromptAns.length() == 0)
		{
			expPromptAns = "[answer missing]";
			session.setAttribute("authoringMsg",
					"If you have a prompt, then you must also define an answer for it.");
		}

		// put by default the assertion, if no text is defined
		if (expTextList.size() == 0)
			expTextList.add(expAssertion);

		if (commandType == null)
		{
			commandType = ""; // empty command; just reload the stored session
								// data
			this.clearSession(session);
		}

		if (commandType.equals("switchMode"))
		{
			boolean expertMode = false;
			if (session.getAttribute("expertMode") != null)
				expertMode = (Boolean) session.getAttribute("expertMode");
			session.setAttribute("expertMode", !expertMode);
		}

		if (commandType.equals("createNewTask"))
		{
			// remove all session data
			this.clearSession(session);
			// Create an empty task to reset the values
			Task task = new Task();
			// save that task in the session
			session.setAttribute("t", task);
			session.setAttribute("misConceptionTextsCount", 0);// next possible
																// id that can
																// be given to
																// misconception
																// text
			session.setAttribute("expTextsCount", 0); // next possible id that
														// can be given to
														// expectation text
			session.setAttribute("currentTabId", 1);// 1 exp, 2 for misc
			session.setAttribute("hintsCount", 0); // next possible id that can
													// be given to hints
			session.setAttribute("currentExp", new Expectation(""));
			session.setAttribute("currentMisc", new Expectation(""));

			// Redirect to the tasks page
			response.sendRedirect(request.getContextPath() + destination);
			return;

		}

		if (commandType.equals("loadData"))
		{
			System.out.println("Loading file " + taskName);
			// taskName = "LP00_PR00";
			Task task = (new TaskManager(taskName, true)).LoadTask();
			if (task == null)
				System.out.println("Task IS : NULL");
			if (task != null)
			{
				// we need to copy the file from the real folder to a web
				// accessible folder
				File realFile = new File(ConfigManager.GetEditedTasksPath()
						+ ConfigManager.GetTaskFileName(task.getTaskID()));
				File webFile = new File(getServletContext().getRealPath(
						"/DTResources")
						+ "\\EditedTasks\\"
						+ ConfigManager.GetTaskFileName(task.getTaskID()));

				org.apache.commons.io.FileUtils.copyFile(realFile, webFile,
						true);
				System.out.print("Edited Task " + task.getTaskID()
						+ " temporarily copied to web accesible folder.");
				// ==========================================================================================

				// Setting following session values are extremely important.
				session.setAttribute("t", task);
				Expectation[] ms = task.getMisconceptions();
				Expectation[] es = task.getExpectations();

				// Sort the expectations (Abstract first, Concrete then, then
				// sort ans
				ArrayList<Expectation> listAbs = new ArrayList<Expectation>();
				ArrayList<Expectation> listConc = new ArrayList<Expectation>();
				ArrayList<Expectation> listShort = new ArrayList<Expectation>();
				for (Expectation e : es)
				{
					if (e.getType() == EXPECT_TYPE.ABSTRACT)
					{
						listAbs.add(e);
					}
					else if (e.getType() == EXPECT_TYPE.CONCRETE)
					{
						listConc.add(e);
					}
					else
					{
						listShort.add(e);
					}
				}
				listAbs.addAll(listConc);
				listAbs.addAll(listShort);
				es = listAbs.toArray(new Expectation[listAbs.size()]);
				// set the sorted expectations back
				task.setExpectations(es);

				session.setAttribute("misConceptionTextsCount",
						(ms.length > 0) ? ms[0].getVariants().length + 1 : 0);// next
																				// possible
																				// id
																				// that
																				// can
																				// be
																				// given
																				// to
																				// misconception
																				// text
				session.setAttribute("expTextsCount",
						(es.length > 0) ? es[0].getVariants().length + 1 : 0); // next
																				// possible
																				// id
																				// that
																				// can
																				// be
																				// given
																				// to
																				// expectation
																				// text
				session.setAttribute("currentTabId", 1);// 1 exp, 2 for misc
				session.setAttribute("hintsCount", (es.length > 0 && es[0]
						.getHints() != null) ? es[0].getHints().length + 1 : 0); // next
																					// possible
																					// id
																					// that
																					// can
																					// be
																					// given
																					// to
																					// hints

				if (es.length > 0)
					session.setAttribute("currentExp", es[0]);
				else
					session.setAttribute("currentExp", new Expectation(""));

				if (ms.length > 0)
					session.setAttribute("currentMisc", ms[0]);
				else
					session.setAttribute("currentMisc", new Expectation(""));

				session.setAttribute("authoringMsg", "Task " + task.getTaskID()
						+ " Succesfully Loaded.");

				// RequestDispatcher rd =
				// getServletContext().getRequestDispatcher(destination);
				// rd.forward(request, response);
				System.out.println("Text = " + task.getProblemText1());
			}
			else
			{
				session.setAttribute("authoringMsg", "Error loading. Task "
						+ taskName + " was not found.");

				String s = ConfigManager.GetTasksPath()
						+ ConfigManager.GetTaskFileName(taskName);
				System.out.println("s:" + s);
			}
			session.setAttribute("btnSaveDisabled", "");

			response.sendRedirect(request.getContextPath() + destination);

			return;
		}

		Task task = (Task) session.getAttribute("t");
		if (task == null)
		{
			task = new Task();
			// for safety reasons, if session is lost, reload all the data again
			commandType = "";
		}

		Expectation exp = new Expectation(expId);
		Expectation misc = new Expectation(miscId);
		misc.isMisconception = true;

		if (!commandType.equals(""))
		{
			// Section 1: Tasks main data
			task.setTaskID(taskID);
			task.setProblemText1(taskText);
			task.setProblemText2(taskText2);
			task.setImage(taskImage);
			task.setMultimedia(taskMultimedia);
			task.setIntroduction(taskIntroduction);
			task.setSummary(taskSummary.trim());

			try
			{
				exp.setOrder(Integer.parseInt(expOrder.trim()));
			}
			catch (Exception expec)
			{
				exp.setOrder(-1);
			}
			exp.setAssertion(expAssertion);
			if (expType.trim().length() > 0)
				exp.setType(Expectation.EXPECT_TYPE.valueOf(expType));
			exp.setDescription(expDesc);
			exp.setBonus(expBonus);
			exp.setRequired(BuildExpectedAnswer(expRequired, expWrong,
					expRequiredGwF));
			exp.setForbidden(expForbidden);
			String[] expVariants = new String[expTextList.size()];
			for (int i = 0; i < expTextList.size(); i++)
			{
				expVariants[i] = expTextList.get(i);
			}
			exp.setVariants(expVariants);
			exp.setAssertion(expAssertion);
			exp.setPostImage(expPostImg);
			exp.setPump(expPump);
			exp.setAlternatePump(expAltPump);
			exp.setPrompt(expPromptTxt);
			exp.setPromptAnswer(BuildExpectedAnswer(expPromptAns,
					expPromptWrong, expPromptGwF));
			exp.setPromptCorrection(expPromptNeg);
			if (expHintList.size() > 0)
			{
				// remove the empty hints (at least the hint text has to exist)
				int i = 0;
				while (i < expHintList.size())
				{
					if (expHintList.get(i + 1).trim().length() == 0)
					{
						expHintList.remove(i);// type
						expHintList.remove(i);// text
						expHintList.remove(i);// answer
						expHintList.remove(i);// wrong
						expHintList.remove(i);// negative
						expHintGwF.remove(i / 5);
					}
					else
						i += 5;
				}
				int hintCount = expHintList.size() / 5;
				// System.out.println("Size of hint:"+hintCount);
				String[] expHintTypes = new String[hintCount];
				String[] expHints = new String[hintCount];
				ExpectAnswer[] expHintAnswers = new ExpectAnswer[hintCount];
				String[] expHintCorrection = new String[hintCount];
				for (i = 0; i < hintCount; i++)
				{
					expHintTypes[i] = expHintList.get(i * 5);
					expHints[i] = expHintList.get(i * 5 + 1);

					expHintAnswers[i] = BuildExpectedAnswer(
							expHintList.get(i * 5 + 2),
							expHintList.get(i * 5 + 3), expHintGwF.get(i));

					expHintCorrection[i] = expHintList.get(i * 5 + 4);
				}
				exp.setHintsType(expHintTypes);
				exp.setHints(expHints);
				exp.setHintsAnswer(expHintAnswers);
				exp.setHintsCorrection(expHintCorrection);
			}

			// find the expectation
			int expIndex = -1;
			for (int i = 0; i < task.getExpectations().length; i++)
				if (task.getExpectations()[i].getId().equals(exp.getId()))
					expIndex = i;

			misc.variants = new String[miscTexts.size()];
			for (int i = 0; i < miscTexts.size(); i++)
				misc.variants[i] = miscTexts.get(i);
			misc.setAssertion(miscAssertion);
			misc.setPump(miscPump);
			misc.setBonus(miscBonus);
			misc.setRequired(new ExpectAnswer(miscReqd));
			misc.setForbidden(miscForbidden);
			misc.setYokedExpectation(miscYoked);

			// find the misconception
			int miscIndex = -1;
			for (int i = 0; i < task.getMisconceptions().length; i++)
				if (task.getMisconceptions()[i].getId().equals(misc.getId()))
					miscIndex = i;

			if (expIndex >= 0)
				task.getExpectations()[expIndex] = exp;
			if (miscIndex >= 0)
				task.getMisconceptions()[miscIndex] = misc;
		}

		session.setAttribute("t", task);

		if (commandType.equals("saveData"))
		{
			// Nobal says: See following line how the variables are named. This
			// will give you the names of variables
			// http://localhost:8080/DeeptutorApp/authoring?currentTask=LP05_PR00&currentTab=1&commandType=addExpectation&expIdToBeLoaded=&miscIdToBeLoaded=&sel_task=&tskFileName=LP05_PR00&tskId=LP05_PR00&tskText1=You+are+driving+in+a+traffic+circle+when+you+come+upon+an+icy+patch+of+the+road%2C+with+essentially+no+friction.+Describe+your+subsequent+motion.&tskText2=Please+begin+by+briefly+answering+the+above+question.+After+briefly+answering+the+above+question%2C+please+go+on+to+explain+your+answer+in+as+much+detail+as+you+can.&tskImage=circularMotion.png&tskMultimedia=circularMotion.png&tskIntroduction=We+will+work+on+a+problem+inspired+from+a+real+world+scenario+during+winter+times.+While+snow+is+fun+for+making+snowmen+and+skating%2C+it+makes+it+difficult+to+drive+on+icy+roads.+Read+the+problem+carefully+and+follow+the+instructions+about+how+to+provide+the+answer.&expId=1A&expType=1A&expDesc=result+%2F+conclusion+&expBonus=&expRequired=straight%7Cstraightforward%7Cline%7Clinear&expForbidden=circular%7Ccircle%7Ccurve&expText_0=straight.&expText_1=straight+line.&expText_2=I+would+move+straightforward.&expText_3=The+car+will+move+in+a+straight+line+with+a+constant+speed.&expAssertion=The+car+will+move+in+a+straight+line+with+constant+speed.&expPostImg=circularMotion.png&expPump=So%2C+what+will+your+motion+path+be+like%3F&expPromptTxt=According+to+Newton%27s+first+law%2C+the+car+will+be+moving+in+a+_____+line+with+a+constant+speed+after+hitting+the+ice+patch+as+no+net+forces+are+acting+on+it+%28remember+that+there+is+no+friction+on+ice%29.&expPromptAns=straight&expPromptNeg=The+key+word+is+%22straight.%22&expHintType_0=Conditional&expHintText_0=Which+of+Newton%27s+laws+have+we+just+discussed+as+being+most+relevant+to+this+situation%3F&expHintAns_0=first&expHintNeg_0=It%27s+Newton%27s+first+law.++Recall+that+Newton%27s+first+law+refers+to+the+motion+of+objects+on+which+no+net+forces+are+being+applied.+&expHintType_1=Conditional&expHintText_1=What+does+Newton%27s+first+law+tell+us+about+car%27s+motion+path+after+hitting+the+ice+patch%3F&expHintAns_1=&expHintNeg_1=+&miscid=m-1B&expText_0=The+car+will+continue+to+move+in+a+circular+path.&txtMiscAssert=It+might+be+helpful+to+think+about+what+net+force+is+acting+on+the+car+while+on+the+ice+patch+and+which+law+of+motion+%28Newton%27s+law%29+applies+in+this+case.&txtMiscBump=&txtMiscBonus=&txtMiscReqd=circular&txtMiscForbidden=straight

			// Section 2: Tasks Misconception Data
			// Note: Only one misconception at a time will be loaded in the GUI.
			// So, the data below is for that misconception.
			// Variables that are already populated from GUI: miscId,
			// miscAssertion, miscPump, miscBonus, miscReqd, miscForbidden,
			// miscTexts
			// TODO: 1. If the task has misconception with miscId, update that
			// misconception. Otherwise add this misconception

			// Section 3: Tasks Expectation Data
			// Note: As in misconception, only one expectation at a time will be
			// loaded in the GUI. So, the data below is for that expectation.
			// Variables that are already populated from GUI: See the variable
			// declaration at the top
			// TODO: 1. If the task has expectation with expId, update that
			// Expectation. Otherwise add this Expectation
			// Note2: The hints are stored in "expHintList" Arraylist. Each hint
			// has four things its type, text, answer and negative. [0,1,2,3]
			// index of this array list is Hint1, [4,5,6,7] hint2 and so on

			// Section 4: Thank you Mihai. Hope my instructions are clear
			// enough. :) Thanks a lot for your help. Take care :)
			// Create and Save task
			String result = task.CreateXML();

			if (result.equals("OK"))
			{
				session.setAttribute("authoringMsg", "Task " + task.getTaskID()
						+ " Succesfully Saved.");

				// we need to copy the file from the real folder to a web
				// accessible folder
				File realFile = new File(ConfigManager.GetEditedTasksPath()
						+ ConfigManager.GetTaskFileName(task.getTaskID()));
				File webFile = new File(getServletContext().getRealPath(
						"/DTResources")
						+ "\\EditedTasks\\"
						+ ConfigManager.GetTaskFileName(task.getTaskID()));

				org.apache.commons.io.FileUtils.copyFile(realFile, webFile,
						true);
				System.out.print("Edited Task " + task.getTaskID()
						+ " temporarily copied to web accesible folder.");
			}
			else
				session.setAttribute("authoringMsg", "Error Saving Task "
						+ task.getTaskID() + ": " + result);

			// String destination = "/DTAuthoring/Tasks.jsp";
			// RequestDispatcher rd =
			// getServletContext().getRequestDispatcher(destination);
			// rd.forward(request, response);
			// response.sendRedirect(request.getContextPath() + destination);
		}

		/*
		 * if (commandType.equals("viewData")) { String result =
		 * task.CreateXML();
		 * 
		 * if (!result.equals("OK")) { session.setAttribute("authoringMsg",
		 * "Task " + task.getTaskID() + " Succesfully Saved."); } else {
		 * 
		 * String destination = webFileStr; //RequestDispatcher rd =
		 * getServletContext().getRequestDispatcher(destination);
		 * //rd.forward(request, response);
		 * 
		 * response.sendRedirect(request.getContextPath() + destination);
		 * 
		 * out.close(); return; } }
		 */

		if (commandType.equals("loadExpectation"))
		{
			Expectation[] es = task.getExpectations();
			int indexOfNewExpectation = Integer.parseInt(request.getParameter(
					"expIdToBeLoaded").trim());
			exp = es[indexOfNewExpectation];
		}

		if (commandType.equals("loadMisconception"))
		{
			Expectation[] ms = task.getMisconceptions();
			int indexOfNewMisconception = Integer.parseInt(request
					.getParameter("miscIdToBeLoaded").trim());
			misc = ms[indexOfNewMisconception];
		}

		// Remove expectation from the task
		if (commandType.equals("remExpectation"))
		{
			Expectation[] es = task.getExpectations();
			List<Expectation> newEs = new ArrayList<Expectation>();

			for (int i = 0; i < es.length; i++)
			{
				if (es[i].getId() != exp.getId())
					newEs.add(es[i]);
			}

			es = new Expectation[newEs.size()];
			task.setExpectations(newEs.toArray(es));
			exp = new Expectation("");
		}

		// Remove Misconception from the task
		if (commandType.equals("remMisconception"))
		{
			Expectation[] ms = task.getMisconceptions();
			List<Expectation> newMs = new ArrayList<Expectation>();

			for (int i = 0; i < ms.length; i++)
			{
				if (ms[i].getId() != misc.getId())
					newMs.add(ms[i]);
			}

			ms = new Expectation[newMs.size()];
			task.setMisconceptions(newMs.toArray(ms));
			misc = new Expectation("");
		}

		// Add Misconception for the task
		if (commandType.equals("addMisconception"))
		{
			Expectation[] ms = task.getMisconceptions();
			Expectation[] newMs = new Expectation[ms.length + 1];

			for (int i = 0; i < ms.length; i++)
				newMs[i] = ms[i];
			newMs[ms.length] = misc;
			task.setMisconceptions(newMs);
		}

		// Add Expectation for the task
		if (commandType.equals("addExpectation"))
		{
			Expectation[] es = task.getExpectations();
			Expectation[] newEs = new Expectation[es.length + 1];

			for (int i = 0; i < es.length; i++)
				newEs[i] = es[i];
			newEs[es.length] = exp;
			task.setExpectations(newEs);
			session.setAttribute("btnSaveDisabled", true);
		}

		// Add Misconception for the task
		if (commandType.equals("newMisconception"))
		{
			misc = new Expectation("");
		}

		// Add Expectation for the task
		if (commandType.equals("newExpectation"))
		{
			exp = new Expectation("");
			session.setAttribute("btnSaveDisabled",
					"disabled title='Add the New Expectation before saving!'");
		}
		else
		{
			session.setAttribute("btnSaveDisabled", "");
		}

		session.setAttribute("currentExp", exp);
		session.setAttribute("currentMisc", misc);
		session.setAttribute("misConceptionTextsCount",
				(misc.getVariants() != null) ? misc.getVariants().length + 1
						: 0);// next possible id that can be given to
								// misconception text
		session.setAttribute("expTextsCount",
				(exp.getVariants() != null) ? exp.getVariants().length + 1 : 0); // next
																					// possible
																					// id
																					// that
																					// can
																					// be
																					// given
																					// to
																					// expectation
																					// text
		session.setAttribute("hintsCount",
				(exp.getHints() != null) ? exp.getHints().length + 1 : 0); // next
																			// possible
																			// id
																			// that
																			// can
																			// be
																			// given
																			// to
																			// hints
		session.setAttribute("currentTabId", request.getParameter("currentTab"));// 1
																					// exp,
																					// 2
																					// for
																					// misc
		// System.out.println("Current Tab:"+request.getParameter("currentTab"));

		response.sendRedirect(request.getContextPath() + destination);

		return;

		/*
		 * out.print("<html><body>");
		 * 
		 * 
		 * out.print("<br>Command Type: " + commandType);
		 * out.print("<br>Task Name:" + taskName); // out.print("<br>TaskId:" +
		 * taskId); // out.print("<br>Task Text1:" + taskText1); //
		 * out.print("<br>Task Text2:" + taskText2); //
		 * out.print("<br>Task Image:" + tskImage); //
		 * out.print("<br>Task Multimedia:" + tskMultimedia); //
		 * out.print("<br>Task Introduction:" + tskIntroduction);
		 * 
		 * out.print("<br>----Misconception----"); out.print("<br>Id:"+miscId);
		 * out.print("<br>Assertion:"+miscAssertion);
		 * out.print("<br>Bump:"+miscPump); out.print("<br>Bonus:"+miscBonus);
		 * out.print("<br>Reqd:"+miscReqd);
		 * out.print("<br>Forbid:"+miscForbidden); for(String txt:miscTexts){
		 * out.print("<br>Txt-->"+txt); } out.print("<br>--------------------");
		 * 
		 * out.print("<br>----Expectations----"); out.print("<br>expId :"+expId
		 * ); out.print("<br>expType :"+expType );
		 * out.print("<br>expDesc :"+expDesc );
		 * out.print("<br>expBonus :"+expBonus );
		 * out.print("<br>expRequired :"+expRequired );
		 * out.print("<br>expForbidden :"+expForbidden ); out.print("<br>Text :"
		 * ); for(int i=0;i<expTextList.size();i++){
		 * out.print("<br>"+i+". "+expTextList.get(i) ); }
		 * out.print("<br>ExpAssertion:"+expAssertion );
		 * out.print("<br>ExpPostImg:"+expPostImg ); out.print("<br>expPump :"+
		 * expPump);
		 * 
		 * out.print("<br> expPromptTxt:"+expPromptTxt );
		 * out.print("<br> expPromptAns:"+expPromptAns );
		 * out.print("<br> expPromptNeg:"+expPromptNeg );
		 * out.print("<br> Hints:"); for(int i=0;i<expHintList.size();i++){
		 * out.print("<br>"+i+". "+expHintList.get(i) ); }
		 * 
		 * out.print("</body></html>"); out.close();
		 */
	}

	ExpectAnswer BuildExpectedAnswer(String accepted, String wrong,
			List<String> variants)
	{
		ExpectAnswer result = new ExpectAnswer(accepted);
		if (wrong != null)
			result.wrongAnswer = wrong;

		if (variants == null)
			return result;

		int varCount = variants.size() / 2;
		result.goodAnswerVariants = new String[varCount];
		result.goodFeedbackVariants = new String[varCount];
		for (int j = 0; j < varCount; j++)
		{
			result.goodAnswerVariants[j] = variants.get(2 * j);
			result.goodFeedbackVariants[j] = variants.get(2 * j + 1);
		}
		return result;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		// TODO Auto-generated method stub

		PrintWriter out = response.getWriter();
		out.print("Hello how are you ...");
		out.close();
	}

	private void clearSession(HttpSession s)
	{
		if (s != null)
		{
			s.removeAttribute("t");
			s.removeAttribute("currentExpec");
			s.removeAttribute("misConceptionTextsCount");
			s.removeAttribute("expTextsCount");
			s.removeAttribute("currentTabId");
			s.removeAttribute("hintsCount");
			s.removeAttribute("currentExp");
			s.removeAttribute("currentMisc");
		}
	}

}
