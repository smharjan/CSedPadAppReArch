package memphis.deeptutor.main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import memphis.deeptutor.dialog.DialogManager;
import memphis.deeptutor.dialog.SAClassifier;
import memphis.deeptutor.dialog.TaskManager;
import memphis.deeptutor.gui.commands.DTCommands;
import memphis.deeptutor.gui.model.Components;
import memphis.deeptutor.gui.model.DTResponse;
import memphis.deeptutor.gui.model.Question;
import memphis.deeptutor.log.DTLogger;
import memphis.deeptutor.log.DTLogger.Actor;
import memphis.deeptutor.log.DTLogger.Level;
import memphis.deeptutor.model.BusinessModel;
import memphis.deeptutor.model.BusinessModel.DTMode;
import memphis.deeptutor.model.DTSession;
import memphis.deeptutor.model.Expectation;
import memphis.deeptutor.model.SessionData;
import memphis.deeptutor.model.Student;
import memphis.deeptutor.servlets.InitDTServlet;
import memphis.deeptutor.singleton.DerbyConnector;
import memphis.deeptutor.singleton.NLPManager;
import memphis.deeptutor.singleton.XMLFilesManager;
import memphis.deeptutor.tools.StringTools;

public class RemoteServiceHandler
{

	String tasksStr = null;
	SessionData data = null;
	Student student = null;
	DTSession mySession = null;
	Date startDate = null; // will be set from BlazeDSHandler
	private static boolean isTimerEnabled = false;
	private int MAX_TIME_FOR_DIALOGUE_IN_MINS = -1;
	int taskCounter = 0;
	boolean isTimerActive = false;
	boolean hasSentTimeoutMessage = false;
	final String newLine = System.getProperty("line.separator");

	public RemoteServiceHandler()
	{
		// This is required for the Blaze DS to instantiate the class
	}

	// =====================================================================
	public String getResults(String inputText)
	{
		// Initialize logger
		DateFormat df = new SimpleDateFormat("MMddyy");
		DTLogger logger = new DTLogger(student.getGivenId() + "-"
				+ df.format(Calendar.getInstance().getTime()));// +
																// clientAddress);

		System.out.println(" From Student: " + inputText);
		long elapsedTime = -1;

		inputText = inputText.trim();
		
		//Dan: shortcut for moveToNextTask
		if (inputText.equals("\\next"))
		{
			inputText = "\\moveToNextTask";
		}

		List<String> TASKS_IDS = null;

		if (tasksStr != null)
		{
			TASKS_IDS = Arrays.asList(tasksStr.split(" "));
		}

		if (student == null)
		{
			student = new Student("guest");
		}
		else
		{
			DerbyConnector.getInstance().getStudentLearningModel(student);
		}

		// ///////////////////////////////////////////////////
		if (isTimerEnabled && !student.isSpecialStudent())
		{
			try
			{
				if (startDate != null)
				{
					elapsedTime = BusinessModel.getInstance()
							.getDateDifferenceInMins(startDate, new Date());
				}
				System.out.println("Time elapsed so far: " + elapsedTime
						+ ": Max was:" + MAX_TIME_FOR_DIALOGUE_IN_MINS);

				// Q: How do i know when a task is done and has to move to next
				// one?
				// For show answers only mode:
				boolean isCurrentTaskFinished = false;
				if (data != null && inputText.startsWith("\\continue")
						&& (data.leftoverText.size() == 0)
						&& mySession.getDTMode() == DTMode.SHOWANSWERS)
				{
					isCurrentTaskFinished = true;
				}
				// what is for the interactive?
				else if (data != null && data.AllExpectationsCovered()
						&& data.leftoverText.size() == 0)
				{
					isCurrentTaskFinished = true;
				}

				// if time elapsed is exceeded, go ahead to
				if (elapsedTime >= MAX_TIME_FOR_DIALOGUE_IN_MINS)
				{
					// made true to disable following if as we now decided to
					// show a new page
					// rather than showing notice in the history
					hasSentTimeoutMessage = true;
					if (isCurrentTaskFinished && !hasSentTimeoutMessage)
					{
						hasSentTimeoutMessage = true;
						Components c = new Components();
						DTResponse resp = new DTResponse();
						resp.addResponseText("NOTICE: The time for INTERACTION is FINISHED. "
								+ "Please press the button to go to the POST-TEST.");
						c.setResponse(resp);
						logger.log(
								Actor.SYSTEM,
								Level.ONE,
								"NOTICE: I found that the time for INTERACTION is FINISHED. Please press the button to go to the POST-TEST");
						logger.saveLogInHTML();
						c.inputShowContinue = true;
						return (new DTCommands()).getCommands(c);
					}

					else if (isCurrentTaskFinished && hasSentTimeoutMessage)
					{
						// take the control to the post test
						Components c = new Components();
						c.setFinishedAllTasks(true);
						System.out
								.println("TIME EXCEEDED, GOING TO THE POST TEST");
						logger.log(Actor.NONE, Level.ONE,
								"TIME EXCEEDED, GOING TO THE POST TEST");
						// Log anything remaining remaining:
						logger.saveLogInHTML();
						// save his learning model
						if (!student.isSpecialStudent())
							DerbyConnector.getInstance()
									.saveStudentLearningModel(student);

						return (new DTCommands()).getCommands(c);
					}

				}
				else if (elapsedTime < MAX_TIME_FOR_DIALOGUE_IN_MINS)
				{
					System.out.println("IS Timer Active (Before)? "
							+ isTimerActive);

					if (isTimerActive
							|| (TASKS_IDS != null && data != null
									&& isAllTasksFinishedDan(TASKS_IDS) && data.leftoverText
									.size() == 0))
					{

						System.out.println("MOVE TO NEXT TASK ?? "
								+ isCurrentTaskFinished);
						if (isTimerActive && isCurrentTaskFinished)
						{
							// data = null;
							student.completedTasks = null;
							// Dan: work on tasks to be assigned!!!!!
							taskCounter = 1;

							int next = taskCounter % TASKS_IDS.size();
							data = null;
							// data.currentTaskID = TASKS_IDS[next];
							// student.currentTask = TASKS_IDS[next];
							if (mySession != null)
							{
								mySession.setAttribute(
										DTSession.getSESSION_DATA_ID(), null);
							}
							inputText = "\\initialize " + TASKS_IDS.get(next);

							// data.taskExpectations =
							// task.GetExpectations(false);
							// data.currentTaskID = taskID[1];
							// data.debugMode =
							// (inputText.startsWith("\\debugtask")?true:false);
							taskCounter++;
						}
						if (!isTimerActive)
						{
							isTimerActive = true;
						}
						System.out
								.println("INSIDE: INITIALIZING AGAIN ..in RSH with (taskCounter,input text) ("
										+ taskCounter + "," + inputText + ")");
						System.out.println("IS Timer Active (After)? "
								+ isTimerActive);

					}
					mySession.setAttribute("isTimerActive", isTimerActive);

				}
			}
			catch (Exception e)
			{
				System.out
						.println("Exception while computing the elapsed time in remote server handler"
								+ e.toString());
				e.printStackTrace();
			}
		}
		// ///////////////////////////////////////////////////////

		logger.log(DTLogger.Actor.NONE, DTLogger.Level.ONE, "");
		logger.log(DTLogger.Actor.SYSTEM, DTLogger.Level.ONE, "Time: "
				+ Calendar.getInstance().getTime());
		logger.log(DTLogger.Actor.STUDENT, DTLogger.Level.ONE, inputText);

		// Initialize the classes required for next steps; any possible errors
		// should pop up here
		// check if all static classes are initialized
		boolean initFailed = false;
		if (XMLFilesManager.getInstance() == null)
		{
			logger.log(DTLogger.Actor.SYSTEM, DTLogger.Level.ONE, "Error ("
					+ Calendar.getInstance().getTime()
					+ "): Cannot load XMLFilesManager");
			initFailed = true;
		}
		if (NLPManager.getInstance() == null)
		{
			logger.log(DTLogger.Actor.SYSTEM, DTLogger.Level.ONE, "Error ("
					+ Calendar.getInstance().getTime()
					+ "): Cannot load NLPManager");
			initFailed = true;
		}

		if (initFailed)
		{
			logger.saveLogInHTML();
			return ResponseToClient(
					"Internal Server Error. Please check the log files for more details.",
					true);
		}

		// ------------------------------------------------------------
		// general try statement to make sure we catch and report on all errors

		String response = "";
		try
		{

			Components c = new Components();

			// somewhat patchy solution :)
			boolean completeAnswerShown = false;
			if (data != null && inputText.startsWith("\\continue")
					&& (data.leftoverText.size() == 0)
					&& mySession.getDTMode() == DTMode.SHOWANSWERS)
			{
				completeAnswerShown = true;

			}

			if (data != null
					&& (((data.AllExpectationsCovered() && data.leftoverText
							.size() == 0) || inputText
							.equals("\\moveToNextTask"))
							|| completeAnswerShown || data.postFeedback))
			{
				completeAnswerShown = false;
				if (!data.debugMode)
				{
					if (!inputText.equals("\\moveToNextTask"))
					{
						// if student have not completed any task, assign the
						// just finished task as the completed task
						if (student.completedTasks == null)
						{
							student.completedTasks = data.currentTaskID;
						}
						// add the currently completed task to the existing list
						else if (!student.completedTasks
								.contains(data.currentTaskID))
						{
							student.completedTasks += ", " + data.currentTaskID;
						}
					}

					// find the next task id
					// int taskIndex = 0;
					// for (taskIndex = 0; taskIndex < TASKS_IDS.size();
					// taskIndex++)
					// if (TASKS_IDS.get(taskIndex).equals(data.currentTaskID))
					// break;

					int newTaskIndex = data.currentTaskIndex + 1;
					// cycle through all completed tasks
					if (TASKS_IDS.size() == 1)
					{
						newTaskIndex = data.currentTaskIndex;
					}
					else
					{
						// Dan:
						if (newTaskIndex >= TASKS_IDS.size())
						{
							// Dan: if the user is special then cycle the tasks,
							// else end with the last task
							if (student.isSpecialStudent())
							{
								newTaskIndex = 0;
							}
							else
							{
								newTaskIndex = data.currentTaskIndex;
							}
						}
					}

					if (newTaskIndex == data.currentTaskIndex)
					{

						Question q = new Question();
						q.setText("");
						q.setText2("");
						c.setQuestion(q);

						DTResponse resp = new DTResponse();
						
						if (!student.isSpecialStudent())
							DerbyConnector.getInstance()
									.saveStudentLearningModel(student);
						mySession.removeAttribute(DTSession
								.getSESSION_DATA_ID());
						resp.addResponseText("You finished all the tasks in this session. Press continue now to move forward.");
						c.inputShowContinue = true;
						student.currentTask = null;
						data = null;

						// if (data.postFeedback)
						// {
						// mySession.removeAttribute(DTSession
						// .getSESSION_DATA_ID());
						// resp.addResponseText("Thank you again for you feedback. You can press continue now to move forward.");
						// c.inputShowContinue = true;
						// // Where is feedback saved?
						// // data.currentTaskID = TASKS_IDS.get(taskIndex);
						// student.currentTask = null; /*
						// * provided that.. the
						// * current task has been
						// * added in the
						// * completed task list..
						// */
						// if (!student.isSpecialStudent())
						// DerbyConnector.getInstance()
						// .saveStudentLearningModel(student);
						// data = null;
						// // logger.log(Actor.TUTOR, Level.ONE,
						// // "[STUDENT FEEDBACK] " + inputText);
						// }
						// else
						// {
						// // Dan: learning model should be updated
						// if (!student.isSpecialStudent())
						// DerbyConnector.getInstance()
						// .saveStudentLearningModel(student);
						//
						// data.postFeedback = true;
						// resp.addResponseText("Congratulations. You have finished all the tasks in the current session."
						// + newLine
						// + newLine
						// +
						// "At this moment, the DeepTutor team would like to ask for your impressions of the system. Please type in the input box your impressions. It will help the developers tremendously with improving the system. Thank you very much!");
						// c.clearHistory = true;
						// logger.log(Actor.TUTOR, Level.ONE,
						// "[ASKING FOR STUDENT FEEDBACK]");
						// }
						c.setResponse(resp);

						logger.saveLogInHTML();

						return (new DTCommands()).getCommands(c);
					}

					// taskIndex = newTaskIndex;
					data.currentTaskID = TASKS_IDS.get(newTaskIndex);
					data.currentTaskIndex = newTaskIndex;
					data.totalNumberOfTasks = TASKS_IDS.size();
					if (!inputText.equals("\\moveToNextTask"))
					{
						student.currentTask = data.currentTaskID;
						if (!student.isSpecialStudent())
							DerbyConnector.getInstance()
									.saveStudentLearningModel(student);
					}
				}

				inputText = "\\reset";
			}

			// restart session from the same browser
			if (inputText.equals("\\reset") && data != null)
			{
				if (mySession != null)
				{
					mySession
							.setAttribute(DTSession.getSESSION_DATA_ID(), null);
				}
				c.clearHistory = true;
				inputText = (data.debugMode ? "\\debugtask " : "\\initialize ")
						+ data.currentTaskID;
			}

			// added a shortcut for the debugtask command (\dt)
			inputText = inputText.replace("\\dt ", "\\debugtask ");

			// initialize GUI Components
			if (inputText.startsWith("\\initialize")
					|| inputText.startsWith("\\debugtask"))
			{
				String[] taskID = inputText.split(" ");
				int taskIndex = 0;

				if (taskID.length < 2)
					return ResponseToClient(
							"There is no task specified by the client. Check the code please.",
							true);

				// demo1 is the initial/default task received from the FLEX
				// client
				if (taskID[1].equals("demo1"))
				{
					// if there is already a current task in the database,
					// start
					// with that...otherwise start with the first task
					if (student.currentTask != null)
					{
						taskID[1] = student.currentTask;
						taskIndex = TASKS_IDS.indexOf(taskID[1]);
					}
					else
					{
						taskID[1] = TASKS_IDS.get(0);
					}
				}
				else
				{
					taskIndex = TASKS_IDS.indexOf(taskID[1]);
				}

				logger.log(DTLogger.Actor.NONE, DTLogger.Level.ONE, "[MODE:"
						+ mySession.getDTMode() + "]"
				// old code
				/*
				 * mySession.getDTMode() == DTMode.SHOWANSWERS ?
				 * "[MODE:SHOWANSWERS]" : "[MODE:INTERACTIVE]"
				 */);

				logger.log(DTLogger.Actor.SYSTEM, DTLogger.Level.ONE,
						"Begin Task: " + taskID[1]);

				TaskManager task = new TaskManager(taskID[1],
						inputText.startsWith("\\debugtask") ? true : false);
				task.setHowManyTasks(TASKS_IDS.size());
				System.out.println("The task to be loaded next is: "
						+ taskID[1]);
				if (task.taskNode == null)
				{
					if (!inputText.startsWith("\\debugtask"))
						return ResponseToClient(
								" You already have finished your tasks.", true); // TODO:
																					// shows
																					// continue
																					// button..,
																					// should
																					// have
																					// something
																					// like
																					// Exit/Close.
					else
						return ResponseToClient(
								"I could not find any task id #"
										+ taskID[1]
										+ "#. Please make sure such a task exists in the resource files.",
								true);
				}

				data = new SessionData();
				data.taskExpectations = task.GetExpectations(false);
				data.taskSummary = task.getSummary();
				data.currentTaskID = taskID[1];
				data.currentTaskIndex = taskIndex;
				data.totalNumberOfTasks = TASKS_IDS.size();
				data.debugMode = (inputText.startsWith("\\debugtask") ? true
						: false);

				// ?? debug mode, what is the difference here?
				TaskManager tl = new TaskManager(data.currentTaskID,
						data.debugMode, mySession.getDTMode());
				tl.setHowManyTasks(TASKS_IDS.size());
				
				String studentEnding = student.getGivenId().substring(student.getGivenId().length() - 2);
				if (studentEnding.matches("_\\d") && student.getLastLogInDate() != null)
				{
					tl.setSessionNumber(Integer.parseInt(studentEnding.substring(1)));
				}

				Components newC = tl.CreateLoadTaskCommand(taskIndex);

				// Dan: add the problem description in the log once the student
				// starts working on it
				logger.log(DTLogger.Actor.SYSTEM, DTLogger.Level.ONE,
						"Task Description: " + tl.problemText);
				logger.log(DTLogger.Actor.TUTOR, DTLogger.Level.ONE,
						tl.problemText2);

				// always clear history when moving to a new problem
				newC.clearHistory = true;

				// if read only mode, change the intro & instruction text text2.
				// if (mySession.getDTMode() == DTMode.SHOWANSWERS) {
				// newC.getQuestion().setText2("");
				// DTResponse resp = newC.getResponse();
				// ArrayList <String> respTextArray = new ArrayList<String>();
				// respTextArray.add("Please read the problem carefully and Click \"Show Answer\" button to see the correct answer");
				// resp.setResponseArray(respTextArray);
				// newC.setResponse(resp);
				// }

				newC.studentID = student.getGivenId();

				System.out.println("Looking for Student...");
				if (InitDTServlet.wozHandler.connectedUsers.contains(student
						.getGivenId()))
				// if (student.getGivenId().equals("wozguest")) //this condition
				// previously served to activate the WoZ only for the wozguest
				// id
				{
					System.out.println("Student Found");
					newC.listen4woz = true;
					newC.studentID = student.getGivenId();
					WizardofOzServiceHandler woz = InitDTServlet.wozHandler;
					woz.BroadcastText(student.getGivenId(), "Problem: "
							+ tl.problemText.trim() + newLine + newLine
							+ tl.problemText2.trim(), null);
				}

				c = newC;
			}
			else
			// if not initialize or debug command
			{
				// =======================================================================
				if (data == null)
				{

					// return
					// ResponseToClient("Internal error. There is no session data. Checking the logs for more info might be a good idea.");

					// in this case we want to tell the client to automatically
					// go back to the login page
					// c.requestLogin = true;
					c.setFinishedAllTasks(true);
					// Log anything remaining remaining:
					logger.saveLogInHTML();
					return (new DTCommands()).getCommands(c);
				}

				if (inputText.equals("\\pong"))
				{
					InitDTServlet.wozHandler.BroadcastText(
							student.getGivenId(), "MessageReceived", null);
					return "wait4TutorResponse";
				}
				else if (inputText.startsWith("\\mlt"))
				{
					String mode = student.getDtMode().name();
					if (mode.equals(DTMode.ADAPTIVE.name()))
					{
						mode += " (" + student.getKnowledgeLevel() + ")";
					}
					
					String message = "Task Sequence for " + mode
							+ ": " + student.getTaskString().replace(" ", ", ")
							+ "; Working on: " + data.currentTaskID;
					DTResponse resp = new DTResponse();
					resp.addResponseText(message);
					c.setResponse(resp);
				}
				/*
				 * ShowMeAnswer command comes only once for a task.. follows
				 * Continue
				 */
				else if (inputText.startsWith("\\showMeTheAnswer"))
				{
					c = (new DialogManager()).getAnswerSummary(data, logger,
							inputText);

					// Dan: add Tutor moves in the 'Show answers' condition as
					// well
					logger.log(DTLogger.Actor.TUTOR, DTLogger.Level.ONE,
							"Okay. Let's summarize the correct answer to this problem.");
				}
				else if (inputText.startsWith("\\continue"))
				{
					c = new Components();
					DTResponse r = new DTResponse();
					r.setResponseArray(data.leftoverText);
					c.setResponse(r);

					// Dan: add Tutor moves in the 'Show answers' condition as
					// well
					if (r.getResponseCount() > 0)
					{
						logger.log(DTLogger.Actor.TUTOR, DTLogger.Level.ONE,
								r.getResponseText(0));
					}
				}
				else
				{
					// Mihai - student contribution
					if (data.AllExpectationsCovered())
					{
						logger.saveLogInHTML();
						return ResponseToClient(
								"You have covered all expectation in this session. This session is completed.",
								true);
					}
					else
					{
						// Mihai - special case for pronoun resolution
						if (data.inputReplaceIt != null)
						{
							inputText = data.inputReplaceIt.replaceAll(
									"<<IT>>", inputText);
							data.inputReplaceIt = null;
						}

						c = new Components();
						DTResponse resp = new DTResponse();
						c.setResponse(resp);
						c.inputShowContinue = false;

						// extract any comments that the user said
						String commentedText = inputText;
						inputText = StringTools.RemoveComments(commentedText);
						if (inputText.trim().length() == 0)
						{
							resp.addResponseText("Comment acknowledged.");
						}
						else
						{
							if (inputText.trim().startsWith("\\exp "))
							{
								String expID = inputText.trim().split(" ")[1];
								Expectation currExp = data
										.FindExpectation(expID);
								if (currExp == null)
								{
									return ResponseToClient(
											"I'm sorry but I cannot find expectation "
													+ expID + " in the script.",
											false);
								}
								else
								{
									currExp.covered = false;
									currExp.hintSuggested = false;
									currExp.sugestedHintIndex = -1;
									data.expectExpectation = currExp;
									inputText = "?"; // force a cognitive
														// feedback
								}
							}

							// Cristian
							SAClassifier myClassifier = new SAClassifier(
									inputText);
							SAClassifier.SPEECHACT sa = myClassifier
									.SAClassify();

							logger.log(
									DTLogger.Actor.NONE,
									DTLogger.Level.TWO,
									"Speech Act Classifier: "
											+ myClassifier.SAClassify());

							// Dan: this should all be done in SAClassifier
							if (sa == SAClassifier.SPEECHACT.YesNoAnswer)
								sa = SAClassifier.SPEECHACT.Contribution;

							if (sa == SAClassifier.SPEECHACT.Contribution
									|| sa == SAClassifier.SPEECHACT.MetaCognitive
									|| (sa == SAClassifier.SPEECHACT.MetaCommunicative
											&& data.expectExpectation != null && data.expectExpectation.sugestedHintIndex >= 0))
							{
								// we give metacognitive feedback for
								// "I don't understand"
								if (sa == SAClassifier.SPEECHACT.MetaCommunicative)
									sa = SAClassifier.SPEECHACT.MetaCognitive;

								c = (new DialogManager()).ProcessContribution(
										sa, data, logger, inputText);

								// Dan: add a line in the log that shows the
								// current expectation that is being worked on;
								// this should be recorded only once immediately
								// after it is being selected to be work on next
								if (data.expectExpectation != null
										&& (data.previousExpectation == null || data.previousExpectation
												.getId() != data.expectExpectation
												.getId()))
								{
									logger.log(
											DTLogger.Actor.SYSTEM,
											DTLogger.Level.ONE,
											"Working Expectation "
													+ data.expectExpectation
															.getId()
													+ ": "
													+ data.expectExpectation.assertion);
									data.previousExpectation = data.expectExpectation;
								}
							}
							// Dan: questions answer
							else if (sa == SAClassifier.SPEECHACT.QuestionDefinitional
									|| sa == SAClassifier.SPEECHACT.QuestionVerification
									|| sa == SAClassifier.SPEECHACT.QuestionOther)
							{
								c = (new DialogManager()).ProcessQuestion(sa,
										data, logger, inputText);
							}
							else
							{
								String tutorResponse = XMLFilesManager
										.getInstance().GetSomeFeedback(
												sa + "Feedback");
								logger.log(DTLogger.Actor.TUTOR,
										DTLogger.Level.ONE, tutorResponse);
								resp.addResponseText(tutorResponse);
							}
						}
					}
				}
				// =======================================================================

				// Let us check if there is a WoZ tutor connected to this
				// student
				WizardofOzServiceHandler woz = InitDTServlet.wozHandler;
				if (woz.connectedUsers.contains(student.getGivenId()))
				// if (student.getGivenId().equals("wozguest"))
				{
					woz.BroadcastText(student.getGivenId(), "Student: "
							+ inputText.trim(), c);
					return "wait4TutorResponse";
				}
				// end WoZ ------------------------------------------

			}

			// look for leftover text --------------------------
			data.leftoverText = new ArrayList<String>();
			ArrayList<String> newResponseText = new ArrayList<String>();

			boolean save2leftover = false;
			for (int i = 0; i < c.getResponse().getResponseCount(); i++)
			{
				if (!save2leftover)
				{
					if (c.getResponse().getResponseText(i).equals("#WAIT#"))
						save2leftover = true;
					else
						newResponseText.add(c.getResponse().getResponseText(i));
				}
				else
					data.leftoverText.add(c.getResponse().getResponseText(i));
			}

			if (save2leftover)
			{
				c.getResponse().setResponseArray(newResponseText);
				c.inputShowContinue = true;
			}
			// end leftover ------------------------------------

			if (mySession != null)
				mySession.setAttribute(DTSession.getSESSION_DATA_ID(), data);
			logger.saveLogInHTML();

			if (mySession.getDTMode() == DTMode.SHOWANSWERS)
			{
				c.setShowAnswersMode(true);
			}

			return (new DTCommands()).getCommands(c);

			// END of general try statement

		}
		catch (Exception e)
		{
			// StringWriter writerStr = new StringWriter();
			// PrintWriter myPrinter = new PrintWriter(writerStr);
			// e.printStackTrace(myPrinter);
			// String stackTraceStr = writerStr.toString();

			e.printStackTrace();
			response = e.toString();

			logger.saveLogInHTML();
			return ResponseToClient(response, true);

			// useful command to get the current directory
			// String currentDir = new File(".").getAbsolutePath();
		}

	}

	public String ResponseToClient(String response, Boolean cont)
	{
		Components c = new Components();
		DTResponse resp = new DTResponse();
		resp.addResponseText(response);
		c.setResponse(resp);
		c.inputShowContinue = cont;

		return (new DTCommands()).getCommands(c);
	}

	// ------------------- MAIN function: use for debugging
	// ---------------------
	// This main function is used to test the web service functions
	// --------------------------------------------------------------------------
	public static void main(String args[])
	{

		// RemoteServiceHandler r = new RemoteServiceHandler();
		//
		// r.data = new SessionData();
		// r.data.currentTaskID = "LP00_PR00";
		// r.data.currentTaskIndex = 0;
		// r.data.taskExpectations = (new TaskManager(r.data.currentTaskID))
		// .GetExpectations(false);

		// first demo scenario
		/*
		 * r.getResults(
		 * "The two forces are different. The truck pulls with a greater force as it is heavier."
		 * ); r.getResults(
		 * "The truck pulls with a greater force because it is more active.");
		 * r.getResults("The two forces are equal.");
		 * r.getResults("I don't know what you mean."); r.getResults(
		 * "To each action force there is an equal and opposite reaction force."
		 * ); r.getResults("Pardon me!");
		 * r.getResults("The car pulls the truck with an equal force.");
		 */

		// r.getResults("According to Newton's second law, force equals mass times acceleration.  In the scenario where the truck is not towing anything, we know the truck's mass and it's acceleration, so we can solve for the force it can generate.  The force the truck can generate equals 2000 kg times 3 meters per second per second equals 6000 Newtons.  Now we consider the scenario where the truck tows the car.  The truck can");
		// r.getResults("Hi");
		// r.getResults("Bye");
		// r.getResults("Who are you?");
		// r.getResults("The first acceleration is twice the other, which makes the first mass half the other.");
		// r.getResults("the forces are equal");

		// r.getResults("the two forces are equal");
		// r.getResults("the two forces are equal");

		// System.out.println("\nValue of datapath is " +
		// ConfigManager.GetLogPath());

		// StudentResponseEvaluator se = new StudentResponseEvaluator(new
		// TaskManager("demo"));
		// System.out.println(se.HasAllTheRequiredWords("Doesn't actually have all required words","words,(all|require|match),(test|have)"));

		// Expectation[] expectations =
		// se.GetExpectationsSortedAndMatched(taskID,
		// "action and reaction forces");

		// Expectation e = se.GetExpectationInfo(expectations[0].id, taskID);

		// int j = 0;

		// String[] expectations = se.GetIdealResponses("2");
		// if (expectations == null)
		// System.out.println("No expectations found.");
		// else for (int i=0;i<expectations.length;i++)
		// System.out.println("|"+expectations[i].toString()+"|");
	}

	private boolean isAllTasksFinishedDan(List<String> TASKS_IDS)
	{
		if (student.completedTasks.split(" ").length == TASKS_IDS.size())
			return true;
		return false;
	}
}