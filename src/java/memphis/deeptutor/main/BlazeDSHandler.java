package memphis.deeptutor.main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import memphis.deeptutor.gui.commands.DTCommands;
import memphis.deeptutor.gui.model.Components;
import memphis.deeptutor.gui.model.DTResponse;
import memphis.deeptutor.log.DTLogger;
import memphis.deeptutor.model.BusinessModel.DTMode;
import memphis.deeptutor.model.DTSession;
import memphis.deeptutor.model.SessionData;
import memphis.deeptutor.model.Student;
import memphis.deeptutor.singleton.ConfigManager;

import flex.messaging.FlexContext;
import flex.messaging.FlexSession;

public class BlazeDSHandler
{
	DTLogger logger;

	public BlazeDSHandler()
	{
		// This is required for the Blaze DS to instantiate the class
	}

	// =====================================================================
	public String getResults(String inputText)
	{
		// get previous session if it exists
		FlexSession mySession = FlexContext.getFlexSession();

		DTSession dtSession = new DTSession();
		RemoteServiceHandler rsHandler = new RemoteServiceHandler();

		if (mySession != null)
		{
			rsHandler.data = (SessionData) mySession.getAttribute(DTSession
					.getSESSION_DATA_ID());
			rsHandler.student = (Student) mySession.getAttribute(DTSession
					.getSESSION_STUDENT_ID());
			rsHandler.tasksStr = (String) mySession.getAttribute(DTSession
					.getSESSION_TASKS_ID());

			DateFormat df = new SimpleDateFormat("MMddyy");
			logger = new DTLogger(rsHandler.student.getGivenId() + "-"
					+ df.format(Calendar.getInstance().getTime()));

			// Dan: If rsHandler.data is null, the student just started working
			// and so we need to read the possible tasks for him (her)
			if (rsHandler.data == null)
			{
				rsHandler.tasksStr = rsHandler.student.getTaskString();
				logger.log(DTLogger.Actor.SYSTEM, DTLogger.Level.ONE,
						rsHandler.student.getDtMode() + " ("
								+ rsHandler.student.getKnowledgeLevel() + ")"
								+ ": " + rsHandler.tasksStr);
				logger.saveLogInHTML();
			}
		}

		dtSession.setDTMode(rsHandler.student.getDtMode());
		rsHandler.mySession = dtSession;
		String response = rsHandler.getResults(inputText);

		if (mySession != null)
		{
			mySession.setAttribute(DTSession.getSESSION_DATA_ID(),
					rsHandler.data);
			mySession.setAttribute(DTSession.getSESSION_STUDENT_ID(),
					rsHandler.student);
			mySession.setAttribute(DTSession.getSESSION_TASKS_ID(),
					rsHandler.tasksStr);
		}

		return response;
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
}