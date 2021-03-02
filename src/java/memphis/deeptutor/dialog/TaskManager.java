package memphis.deeptutor.dialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Hashtable;

import memphis.deeptutor.gui.commands.DTCommands;
import memphis.deeptutor.gui.model.Avatar;
import memphis.deeptutor.gui.model.Components;
import memphis.deeptutor.gui.model.DTResponse;
import memphis.deeptutor.gui.model.Multimedia;
import memphis.deeptutor.gui.model.Notice;
import memphis.deeptutor.gui.model.QImage;
import memphis.deeptutor.gui.model.Question;
import memphis.deeptutor.model.BusinessModel.DTMode;
import memphis.deeptutor.model.ExpectAnswer;
import memphis.deeptutor.model.Expectation;
import memphis.deeptutor.model.Task;
import memphis.deeptutor.singleton.ConfigManager;
import memphis.deeptutor.singleton.XMLFilesManager;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TaskManager
{
	final String newLine = System.getProperty("line.separator");
	// this list stores all the tasks referred by the linked
	// expectations/misconceptions
	// - a linked expectation/misconception has a link in its first text variant
	// to another expectation, as follows:
	// #task-id#expectation-id
	public Hashtable<String, TaskManager> refTasks = new Hashtable<String, TaskManager>();

	public Element taskNode = null;
	public String problemText = "";
	public String problemText2 = "";
	private String taskID = "";

	public boolean disableReferences = false;
	public boolean isEditedFolder = false;

	// to correctly set order for expectations imported from LP99
	public boolean needToSetOrder = false;
	public int localOrder = -1;

	// Dan: this is needed for CreateLoadTaskCommand
	private int howManyTasks = 0;
	private int sessionNumber = 1;

	public TaskManager(String _taskID, boolean edited)
	{
		taskID = _taskID;
		isEditedFolder = edited;
		if (edited)
			taskNode = XMLFilesManager.getInstance().GetEditedTaskElement(
					taskID);
		else
			taskNode = XMLFilesManager.getInstance().GetTaskElement(taskID);
	}

	// DTMode - readonly, or Interactive. Select folder accordingly
	public TaskManager(String _taskID, boolean edited, DTMode dtMode)
	{
		taskID = _taskID;
		if (dtMode == DTMode.SHOWANSWERS)
		{
			StringBuilder sb = new StringBuilder();
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(
						ConfigManager.GetTasksPath()
								+ ConfigManager.GetTaskFileName(taskID, true)));

				String line = null;
				while ((line = reader.readLine()) != null)
				{
					sb.append(line + newLine);
				}

				reader.close();

			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			this.problemText = sb.toString().trim();
		}
		else
		{
			isEditedFolder = edited;
			if (edited)
				taskNode = XMLFilesManager.getInstance().GetEditedTaskElement(
						taskID);
			else
				taskNode = XMLFilesManager.getInstance().GetTaskElement(taskID);
		}

		// taskNode = XMLFilesManager.getInstance().GetTaskElement(taskID);
	}

	private int CountTaskExpectations()
	{
		Element expectationList = (Element) taskNode.getElementsByTagName(
				"ExpectationList").item(0);
		NodeList expectations = expectationList
				.getElementsByTagName("Expectation");
		return expectations.getLength();
	}

	public Expectation[] GetExpectations(boolean getMisconception)
	{
		if (taskNode == null)
			return null;

		// go through the lists of expectations
		Element expectationList = (Element) taskNode.getElementsByTagName(
				getMisconception ? "MisconceptionList" : "ExpectationList")
				.item(0);
		NodeList expectations = expectationList
				.getElementsByTagName(getMisconception ? "Misconception"
						: "Expectation");

		Expectation[] result = new Expectation[expectations.getLength()];
		for (int i = 0; i < expectations.getLength(); i++)
		{
			Element e = (Element) expectations.item(i);
			result[i] = GetExpectationInfo(e, getMisconception);
			result[i].isMisconception = getMisconception;
			if (needToSetOrder)
			{
				// System.out.println("Setting order for " + result[i].getId() +
				// " - " + result[i].getOrder() + " to " + localOrder + "\n");
				result[i].setOrder(localOrder);
				localOrder = -1;
				needToSetOrder = false;
			}
			System.out.println("Order is " + result[i].getId() + " - "
					+ result[i].getOrder() + "\n");
		}

		return result;
	}

	public Expectation FindExpectation(String id, boolean getMisconception)
	{
		// go through the lists of expectations
		Element expectationList = (Element) taskNode.getElementsByTagName(
				getMisconception ? "MisconceptionList" : "ExpectationList")
				.item(0);
		NodeList expectations = expectationList
				.getElementsByTagName(getMisconception ? "Misconception"
						: "Expectation");
		Element eFound = null;
		for (int i = 0; i < expectations.getLength(); i++)
		{
			Element e = (Element) expectations.item(i);
			if (e.getAttribute("id").equals(id))
				eFound = e;
		}

		if (eFound != null)
		{
			return GetExpectationInfo(eFound, getMisconception);
		}

		// we have an invalid link; create an expectation that will report this
		Expectation result = new Expectation(id);
		result.assertion = "[expectation reference #" + id + " was not found]";
		result.variants = new String[1];
		result.variants[0] = "";
		result.isMisconception = getMisconception;

		return result;
	}

	public Expectation GetExpectationInfo(Element e, boolean getMisconception)
	{

		Expectation exp = new Expectation(e.getAttribute("id"));

		// first, get the texts variants and see if we have linked expectations
		Element[] eTexts = XMLFilesManager.GetXMLChildrenElements(e, "Text");
		exp.variants = new String[eTexts.length];
		for (int j = 0; j < eTexts.length; j++)
			exp.variants[j] = eTexts[j].getTextContent();

		// handle linked expectations
		if (exp.variants[0].startsWith("#") && !disableReferences)
		{
			String[] params = exp.variants[0].split("#");
			// first string is empty since # appears as the first character
			String taskID = params[1];
			String expID = params[2];

			// check if the task is already loaded
			TaskManager task = refTasks.get(taskID);
			if (task == null)
			{
				task = new TaskManager(taskID, isEditedFolder);
				task.disableReferences = true;
				refTasks.put(taskID, task);
			}

			System.out.println("Loading abstract expectation " + taskID + "-"
					+ expID + "\n");

			String expType = e.getAttribute("type");
			if (expType != null && expType.length() > 0)
			{
				expType = expType.split(" ")[0].toUpperCase();
				exp.type = Expectation.EXPECT_TYPE.valueOf(expType);
			}

			String order = e.getAttribute("order");
			if (expType != null && expType.length() > 0)
			{
				expType = expType.split(" ")[0].toUpperCase();
				try
				{
					needToSetOrder = true;
					localOrder = Integer.parseInt(order);
					System.out.println("Local order is " + localOrder + "\n");
				}
				catch (Exception ec)
				{

				}
			}

			// Dan: for an abstract expectation, use the alternatePump as pump
			// if it exists
			NodeList pumps = e.getElementsByTagName("AltPump");
			String altPump = null;
			if (pumps != null && pumps.getLength() > 0)
				altPump = pumps.item(0).getTextContent();

			Expectation ret = task.FindExpectation(expID, getMisconception);

			if (altPump != null && altPump != "")
				ret.pump = altPump;
			return ret;

		}

		String expType = e.getAttribute("type");
		if (expType != null && expType.length() > 0)
		{
			expType = expType.split(" ")[0].toUpperCase();
			exp.type = Expectation.EXPECT_TYPE.valueOf(expType);
		}

		String order = e.getAttribute("order");
		if (expType != null && expType.length() > 0)
		{
			expType = expType.split(" ")[0].toUpperCase();
			try
			{
				exp.setOrder(Integer.parseInt(order));
			}
			catch (Exception ec)
			{
			}
		}

		if (e.getElementsByTagName("Description").getLength() > 0)
			exp.description = e.getElementsByTagName("Description").item(0)
					.getTextContent();

		if (e.getElementsByTagName("Assertion").getLength() > 0)
			exp.assertion = e.getElementsByTagName("Assertion").item(0)
					.getTextContent().trim();

		if (e.getElementsByTagName("Pump").getLength() > 0)
			exp.pump = e.getElementsByTagName("Pump").item(0).getTextContent()
					.trim();

		// Dan: added code for the alternative pump
		if (e.getElementsByTagName("AltPump").getLength() > 0)
			exp.alternatePump = e.getElementsByTagName("AltPump").item(0)
					.getTextContent().trim();

		if (e.getElementsByTagName("HintSequence").getLength() > 0)
		{
			Element hintSeq = (Element) e.getElementsByTagName("HintSequence")
					.item(0);

			NodeList hintNodes = hintSeq.getElementsByTagName("Hint");

			exp.hints = new String[hintNodes.getLength()];
			exp.hintsAnswer = new ExpectAnswer[hintNodes.getLength()];
			exp.hintsType = new String[hintNodes.getLength()];
			exp.hintsCorrection = new String[hintNodes.getLength()];

			for (int i = 0; i < hintNodes.getLength(); i++)
			{
				Element hint = (Element) hintNodes.item(i);
				exp.hints[i] = XMLFilesManager.GetXMLChildrenElements(hint,
						"Text")[0].getTextContent().trim();
				exp.hintsType[i] = hint.getAttribute("type");
				if (hint.getElementsByTagName("Answer").getLength() > 0)
					exp.hintsAnswer[i] = new ExpectAnswer((Element) hint
							.getElementsByTagName("Answer").item(0));
				if (hint.getElementsByTagName("Negative").getLength() > 0)
					exp.hintsCorrection[i] = hint
							.getElementsByTagName("Negative").item(0)
							.getTextContent();
			}

		}
		if (e.getElementsByTagName("Prompt").getLength() > 0)
		{
			Element prompt = (Element) e.getElementsByTagName("Prompt").item(0);
			exp.prompt = XMLFilesManager.GetXMLChildrenElements(prompt, "Text")[0]
					.getTextContent().trim();
			exp.promptAnswer = new ExpectAnswer((Element) prompt
					.getElementsByTagName("Answer").item(0));
			if (prompt.getElementsByTagName("Negative").getLength() > 0)
				exp.promptCorrection = prompt.getElementsByTagName("Negative")
						.item(0).getTextContent().trim();
		}

		if (e.getElementsByTagName("YokedExpectation").getLength() > 0)
		{
			exp.yokedExpectation = e.getElementsByTagName("YokedExpectation")
					.item(0).getTextContent();
		}

		if (e.getElementsByTagName("PostImage").getLength() > 0)
		{
			Element eimg = (Element) e.getElementsByTagName("PostImage")
					.item(0);
			exp.postImage = eimg.getAttribute("source");
			exp.postImageSizeHeight = Integer.parseInt(eimg
					.getAttribute("height"));
			exp.postImageSizeWidth = Integer.parseInt(eimg
					.getAttribute("width"));
		}

		if (e.getElementsByTagName("Required").getLength() > 0)
			exp.required = new ExpectAnswer((Element) e.getElementsByTagName(
					"Required").item(0));

		if (e.getElementsByTagName("Forbidden").getLength() > 0)
			exp.forbidden = e.getElementsByTagName("Forbidden").item(0)
					.getTextContent();

		if (e.getElementsByTagName("Bonus").getLength() > 0)
			exp.bonus = e.getElementsByTagName("Bonus").item(0)
					.getTextContent();
		// }

		return exp;
	}

	public Components CreateLoadTaskCommand(int taskIndex)
	{
		// Dan: add additional Intro
		String additionalIntro = null;
		if (taskIndex == 0)
		{
			additionalIntro = getIntro(sessionNumber == 1);
		}

		if (taskNode == null)
			return null;

		int totalExpectations = CountTaskExpectations();
		Element textElement = (Element) taskNode.getElementsByTagName("Text")
				.item(0);
		Element textElement2 = (Element) taskNode.getElementsByTagName("Text2")
				.item(0);
		Element imgElement = null;
		if (taskNode.getElementsByTagName("Image").getLength() > 0)
			imgElement = (Element) taskNode.getElementsByTagName("Image").item(
					0);
		Element multimediaElement = null;
		if (taskNode.getElementsByTagName("Multimedia").getLength() > 0)
			multimediaElement = (Element) taskNode.getElementsByTagName(
					"Multimedia").item(0);

		// new String(new
		// char[Integer.parseInt(textElement.getAttribute("leadingSpaces"))]).replace('\0',
		// ' ')
		String text = textElement.getTextContent();
		String text2 = textElement2.getTextContent();

		problemText = text;
		problemText2 = text2;

		Components c = new Components();
		// Create to command to load the task
		Question q = new Question();
		q.setText(text);
		q.setText2(text2);

		if (imgElement != null)
		{
			String imageSrc = imgElement.getAttribute("source");
			String imageHeight = imgElement.getAttribute("height");
			String imageWidth = imgElement.getAttribute("width");

			QImage img = new QImage();
			img.setSource(ConfigManager.GetMediaWebPath() + imageSrc);
			img.setHeight(Integer.parseInt(imageHeight));
			img.setWidth(Integer.parseInt(imageWidth));
			q.setImage(img);
			MakeMediaAvailable(imageSrc);
		}
		c.setQuestion(q);

		if (multimediaElement != null)
		{
			String multimediaSrc = multimediaElement.getAttribute("source");
			String multimediaType = multimediaElement.getAttribute("type");

			Multimedia m = new Multimedia();
			m.setSource(ConfigManager.GetMediaWebPath() + multimediaSrc);
			m.setType(multimediaType);
			m.setHeight(10);
			m.setWidth(20);
			MakeMediaAvailable(multimediaSrc);
			c.setMultimedia(m);
		}

		Notice notice = new Notice();
		notice.setNotice("Covered Expectations for Current Task: 0 out of "
				+ totalExpectations);

		DTResponse resp = new DTResponse();

		Element welcomeElement = (Element) taskNode.getElementsByTagName(
				"Intro").item(0);

		// Dan: add additional Intro
		String welcomeMsg = welcomeElement.getTextContent();
		if (welcomeMsg.length() > 0)
		{
			if (additionalIntro != null)
				welcomeMsg = additionalIntro + welcomeMsg;
			resp.addResponseText(welcomeMsg);
		}
		else
		{
			resp.addResponseText("[introduction message is missing for current task]");
		}

		Avatar av = new Avatar();
		av.setSource("../DTAvatar/DTAvatar.swf");

		c.setAvatar(av);
		c.setNotice(notice);
		c.setResponse(resp);

		c.setProgress(Integer.toString(taskIndex + 1) + " / "
				+ Integer.toString(getHowManyTasks()));

		return c;
	}

	private String getIntro(boolean isFirstSession)
	{
		StringBuilder sb = new StringBuilder();
		if (isFirstSession)
		{
			sb.append("Welcome to the DeepTutor system!" + newLine + newLine);
			sb.append("DeepTutor will help you better understand Newtonian Physics."
					+ newLine + newLine);
			sb.append("DeepTutor is a dialogue-based tutoring system. That is, most of the time you will converse with DeepTutor as you would with a human tutor."
					+ newLine + newLine);
			sb.append("Please follow these hints below for a best learning experience:"
					+ newLine + newLine);
			sb.append("HINT 1: Please write your answers using complete sentences."
					+ newLine + newLine);
			sb.append("For instance, if DeepTutor asks What can you say about the velocity of the basketball? instead of replying with a short response such as constant please respond with a complete sentence such as The velocity of the basketball is constant."
					+ newLine + newLine);
			sb.append("HINT 2: When given a new problem, first try to provide a complete answer which should include the result or conclusion and a justification in terms of principles or definitions which justify or explain the result."
					+ newLine + newLine);
			sb.append("Lets work on a problem now." + newLine + newLine);
		}
		else
		{
			sb.append("Welcome back!" + newLine + newLine);
			sb.append("Remember these hints below for a best learning experience:"
					+ newLine + newLine);
			sb.append("HINT 1: Please write your answers using complete sentences."
					+ newLine + newLine);
			sb.append("For instance, if DeepTutor asks What can you say about the velocity of the basketball? instead of replying with a short response such as �constant� please respond with a complete sentence such as The velocity of the basketball is constant."
					+ newLine + newLine);
			sb.append("HINT 2: When given a new problem, first try to provide a complete answer which should include the result or conclusion and a justification in terms of principles or definitions which justify or explain the result."
					+ newLine + newLine);
			sb.append("Lets work on a problem now." + newLine + newLine);
		}
		return sb.toString();
	}

	// Dan: TaskManager can return the task summary
	public String getSummary()
	{
		Element summaryElement = (Element) taskNode.getElementsByTagName(
				"Summary").item(0);

		return (summaryElement != null) ? summaryElement.getTextContent()
				: null;
	}

	public Task LoadTask()
	{
		if (taskNode == null)
			return null;

		// temporary - this function is currently only run from the authoring
		// tool, where we are in editing mode (so don't load referenced
		// expectations)
		disableReferences = true;

		Element textElement = (Element) taskNode.getElementsByTagName("Text")
				.item(0);
		Element textElement2 = (Element) taskNode.getElementsByTagName("Text2")
				.item(0);
		Element imgElement = null;
		if (taskNode.getElementsByTagName("Image").getLength() > 0)
			imgElement = (Element) taskNode.getElementsByTagName("Image").item(
					0);
		Element multimediaElement = null;
		if (taskNode.getElementsByTagName("Multimedia").getLength() > 0)
			multimediaElement = (Element) taskNode.getElementsByTagName(
					"Multimedia").item(0);

		Element welcomeElement = (Element) taskNode.getElementsByTagName(
				"Intro").item(0);
		String welcomeMsg = welcomeElement.getTextContent();
		Element summaryElement = (Element) taskNode.getElementsByTagName(
				"Summary").item(0);

		String summaryMsg = (summaryElement != null) ? summaryElement
				.getTextContent() : null;
		// new String(new
		// char[Integer.parseInt(textElement.getAttribute("leadingSpaces"))]).replace('\0',
		// ' ')
		Task t = new Task();
		t.setTaskID(taskID);
		if (taskNode.getAttribute("creator") != null)
			t.setCreator(taskNode.getAttribute("creator"));
		t.setProblemText1(textElement.getTextContent().trim());
		t.setProblemText2(textElement2.getTextContent().trim());
		if (imgElement != null)
			t.setImage(imgElement.getAttribute("source"));
		if (multimediaElement != null)
			t.setMultimedia(multimediaElement.getAttribute("source"));
		t.setIntroduction(welcomeMsg.trim());

		t.setExpectations(this.GetExpectations(false));
		t.setMisconceptions(this.GetExpectations(true));
		if (summaryMsg != null)
		{
			t.setSummary(summaryMsg);
		}

		return t;
	}

	public void MakeMediaAvailable(String fileName)
	{
		// we want to make sure that the media file is accessible from the
		// browser
		String webPath = ConfigManager.GetResourcePath() + "\\Media\\";
		String mediaPath = ConfigManager.GetMediaPath() + "\\";

		// we need to copy the file from the real folder to a web accessible
		// folder
		File realFile = new File(mediaPath + fileName);
		File webFile = new File(webPath + fileName);

		if ((!webFile.exists())
				|| (realFile.lastModified() != webFile.lastModified()))
		{
			try
			{
				org.apache.commons.io.FileUtils.copyFile(realFile, webFile,
						true);
				System.out
						.print("File temporarily copied in web accesible folder: "
								+ fileName);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out
						.print("Error while copying file web accesible folder: "
								+ fileName);
			}
		}
	}

	public String getRelevantText()
	{

		StringBuilder result = new StringBuilder();
		NodeList expectations = taskNode.getElementsByTagName("Text");
		for (int i = 0; i < expectations.getLength(); i++)
		{
			String text = ((Element) expectations.item(i)).getTextContent()
					.toLowerCase();
			// if expectation referenced from the current task, get the texts
			// from them. Handling only the single level of reference.
			if (text.startsWith("#") && !disableReferences)
			{
				String[] params = text.split("#");
				// first string is empty since # appears as the first character
				String taskID = params[1];
				String expID = params[2];
				// check if the task is already loaded
				TaskManager task = refTasks.get(taskID);
				if (task == null)
				{
					task = new TaskManager(taskID, isEditedFolder); /*
																	 * Note:
																	 * assumed
																	 * that the
																	 * referenced
																	 * task
																	 * exists,
																	 * otherwise
																	 * ?
																	 */
					// Not sure what the effect of disabling reference (next two
					// lines).. so, not caching from this point.
					// task.disableReferences = true;
					// refTasks.put(taskID, task);
				}

				NodeList refTaskTextNodes = task.taskNode
						.getElementsByTagName("Text");
				for (int j = 0; j < refTaskTextNodes.getLength(); j++)
				{
					String textFromRefTask = ((Element) refTaskTextNodes
							.item(j)).getTextContent().toLowerCase();
					result.append(" " + textFromRefTask);
				}

			}
			else
			{
				result.append(" " + text);
			}
		}
		return result.toString();
	}

	// DEPRECATED
	// Mihai: this function is here just to see how the initialize task commands
	// are created
	public static String initialize(String taskID)
	{

		// String commandString;
		/*
		 * ="	<DTCommands>" + "<command type=\"loadNewQuestion\">" +
		 * "<question><text>Suppose a truck (2000 kg) is towing a car (1000 kg), and the truck is picking up speed.\n How do the amounts of these two forces compare?\na) the force of the truck pulling the car, and \nb) the force of the car pulling the truck\n\n2. By itself, when not towing anything, the truck can accelerate at 3 meters per second per second.  What acceleration can the truck attain while towing the car?</text>"
		 * +
		 * "<image><src>../includes/FCI-RV95_withInstr-2_page7_image1.gif</src>"
		 * + "		<width>100</width>" +"<height>100</height>" + "</image>" +
		 * "<multimedia>" +
		 * "<src>http://www.cs.memphis.edu/~vrus/DeepTutor/DeepT-Proto-NewSim3.swf</src>"
		 * + "<type>flash</type>" + "<height>10</height>" + "<width>20</width>"
		 * + "</multimedia>" + "</question>" + "</command>" +
		 * "<command type=\"responseToStudent\">" +
		 * "<response>Sample response from xml </response>" + "</command>" +
		 * "<command type=\"changeAvatar\">" +
		 * "<source>http://localhost:8080/CSedPadAppReArch/includes/Avatar.swf</source>"
		 * + "</command></DTCommands>";
		 */

		String path = ConfigManager.GetResourcePath();
		System.out.println("The log file path:" + path);

		TaskManager tl = new TaskManager(taskID, false);
		return new DTCommands().getCommands(tl.CreateLoadTaskCommand(-1));

		/*
		 * Question q= new Question(); q.setText(
		 * "Suppose a truck (2000 kg) is towing a car (1000 kg), and the truck is picking up speed.\n How do the amounts of these two forces compare?\na) the force of the truck pulling the car, and \nb) the force of the car pulling the truck\n\n2. By itself, when not towing anything, the truck can accelerate at 3 meters per second per second.  What acceleration can the truck attain while towing the car?"
		 * ); QImage img=new QImage();
		 * img.setSource("../includes/FCI-RV95_withInstr-2_page7_image1.gif");
		 * img.setHeight(100); img.setWidth(100); q.setImage(img);
		 * 
		 * Multimedia m=new Multimedia(); m.setSource(
		 * "http://www.cs.memphis.edu/~vrus/DeepTutor/DeepT-Proto-NewSim3.swf");
		 * m.setType("flash"); m.setHeight(10); m.setWidth(20);
		 * 
		 * DTResponse resp= new DTResponse();
		 * resp.setResponseText("Sample response from xml");
		 * 
		 * Avatar av= new Avatar(); av.setSource("../includes/Avatar.swf");
		 * 
		 * Components c= new Components(); c.setQuestion(q); c.setMultimedia(m);
		 * c.setAvatar(av); c.setResponse(resp);
		 * 
		 * DTCommands dtc= new DTCommands(); commandString=dtc.getCommands(c);
		 * return commandString;
		 */
	}

	public int getHowManyTasks()
	{
		return howManyTasks;
	}

	public void setHowManyTasks(int howManyTasks)
	{
		this.howManyTasks = howManyTasks;
	}

	public int getSessionNumber()
	{
		return sessionNumber;
	}

	public void setSessionNumber(int sessionNumber)
	{
		this.sessionNumber = sessionNumber;
	}

}
