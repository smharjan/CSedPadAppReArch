package dt.persistent.xml;

import dt.config.ConfigManager;
import java.io.File;
import java.util.Hashtable;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TaskManager {

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

	public TaskManager(String _taskID) {
		taskID = _taskID;
		taskNode = XMLFilesManager.getInstance().GetTaskElement(taskID);
	}


//	public TaskManager(String _taskID, boolean edited) {
//		taskID = _taskID;
//		isEditedFolder = edited;
//		if (edited)
//			taskNode = XMLFilesManager.getInstance().GetEditedTaskElement(
//					taskID);
//		else
//			taskNode = XMLFilesManager.getInstance().GetTaskElement(taskID);
//	}

	// DTMode - readonly, or Interactive. Select folder accordingly
//	public TaskManager(String _taskID, boolean edited, DTMode dtMode) {
//		taskID = _taskID;
//		if (dtMode == DTMode.SHOWANSWERS) {
//			taskNode = XMLFilesManager.getInstance().GetShowAnswerTaskElement(
//					taskID);
//		} else {
//			isEditedFolder = edited;
//			if (edited)
//				taskNode = XMLFilesManager.getInstance().GetEditedTaskElement(taskID);
//			else
//				taskNode = XMLFilesManager.getInstance().GetTaskElement(taskID);
//		}
//	}

	/*
	 * public TaskManager(String _taskID, ServletContext sc) { taskID = _taskID;
	 * 
	 * try{ DocumentBuilderFactory docBuilderFactory =
	 * DocumentBuilderFactory.newInstance(); DocumentBuilder docBuilder =
	 * docBuilderFactory.newDocumentBuilder(); Document docTasks =
	 * docBuilder.parse(new File(ConfigManager.GetTasksPath() + taskID +
	 * ".xml")); docTasks.getDocumentElement().normalize();
	 * 
	 * NodeList listOfAnswerTasks = docTasks.getElementsByTagName("Task");
	 * taskNode = (Element)listOfAnswerTasks.item(0); } catch(Exception
	 * e){e.printStackTrace();taskNode = null;} }
	 */

	private int CountTaskExpectations() {
		Element expectationList = (Element) taskNode.getElementsByTagName(
				"ExpectationList").item(0);
		NodeList expectations = expectationList
				.getElementsByTagName("Expectation");
		return expectations.getLength();
	}

	public Expectation[] GetExpectations(boolean getMisconception) {
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
		for (int i = 0; i < expectations.getLength(); i++) {
			Element e = (Element) expectations.item(i);
			result[i] = GetExpectationInfo(e, getMisconception);
			result[i].isMisconception = getMisconception;
			if (needToSetOrder){
				// System.out.println("Setting order for " + result[i].getId() + " - " + result[i].getOrder() + " to " + localOrder + "\n");
				result[i].setOrder(localOrder);
				localOrder = -1;
				needToSetOrder = false;
			}
			System.out.println("Order is " + result[i].getId() + " - " + result[i].getOrder() + "\n");
		}

		return result;
	}

	public Expectation FindExpectation(String id, boolean getMisconception) {
		// go through the lists of expectations
		Element expectationList = (Element) taskNode.getElementsByTagName(
				getMisconception ? "MisconceptionList" : "ExpectationList")
				.item(0);
		NodeList expectations = expectationList
				.getElementsByTagName(getMisconception ? "Misconception"
						: "Expectation");
		Element eFound = null;
		for (int i = 0; i < expectations.getLength(); i++) {
			Element e = (Element) expectations.item(i);
			if (e.getAttribute("id").equals(id))
				eFound = e;
		}

		if (eFound != null){
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

	public Expectation GetExpectationInfo(Element e, boolean getMisconception) {

		Expectation exp = new Expectation(e.getAttribute("id"));
		
		// first, get the texts variants and see if we have linked expectations
		Element[] eTexts = XMLFilesManager.GetXMLChildrenElements(e, "Text");
		exp.variants = new String[eTexts.length];
		for (int j = 0; j < eTexts.length; j++)
			exp.variants[j] = eTexts[j].getTextContent();

		// handle linked expectations
		if (exp.variants[0].startsWith("#") && !disableReferences) {
			String[] params = exp.variants[0].split("#");
			// first string is empty since # appears as the first character
			String taskID = params[1];
			String expID = params[2];
			
			// check if the task is already loaded
			TaskManager task = refTasks.get(taskID);
			if (task == null) {
				//task = new TaskManager(taskID, isEditedFolder);
                                task = new TaskManager(taskID);
				task.disableReferences = true;
				refTasks.put(taskID, task);
			}

			System.out.println("Loading abstract expectation " + taskID + "-" + expID + "\n");
			
			String expType = e.getAttribute("type");
			if (expType != null && expType.length() > 0) {
				expType = expType.split(" ")[0].toUpperCase();
				exp.type = Expectation.EXPECT_TYPE.valueOf(expType);
			}
			
			String order = e.getAttribute("order");
			if (expType != null && expType.length() > 0) {
				expType = expType.split(" ")[0].toUpperCase();
				try{
					needToSetOrder = true;
					localOrder = Integer.parseInt(order);
					System.out.println("Local order is " + localOrder + "\n");
				}catch(Exception ec){
					
				}
			}

			return task.FindExpectation(expID, getMisconception);

		}

		String expType = e.getAttribute("type");
		if (expType != null && expType.length() > 0) {
			expType = expType.split(" ")[0].toUpperCase();
			exp.type = Expectation.EXPECT_TYPE.valueOf(expType);
		}
		
		String order = e.getAttribute("order");
		if (expType != null && expType.length() > 0) {
			expType = expType.split(" ")[0].toUpperCase();
			try{
				exp.setOrder( Integer.parseInt(order));
					}catch(Exception ec){
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

		if (e.getElementsByTagName("HintSequence").getLength() > 0) {
			Element hintSeq = (Element) e.getElementsByTagName("HintSequence")
					.item(0);

			NodeList hintNodes = hintSeq.getElementsByTagName("Hint");

			exp.hints = new String[hintNodes.getLength()];
			exp.hintsAnswer = new ExpectAnswer[hintNodes.getLength()];
			exp.hintsType = new String[hintNodes.getLength()];
			exp.hintsCorrection = new String[hintNodes.getLength()];

			for (int i = 0; i < hintNodes.getLength(); i++) {
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
		if (e.getElementsByTagName("Prompt").getLength() > 0) {
			Element prompt = (Element) e.getElementsByTagName("Prompt").item(0);
			exp.prompt = XMLFilesManager.GetXMLChildrenElements(prompt, "Text")[0]
					.getTextContent().trim();
			exp.promptAnswer = new ExpectAnswer((Element) prompt
					.getElementsByTagName("Answer").item(0));
			if (prompt.getElementsByTagName("Negative").getLength() > 0)
				exp.promptCorrection = prompt.getElementsByTagName("Negative")
						.item(0).getTextContent().trim();
		}

		if (e.getElementsByTagName("YokedExpectation").getLength() > 0) {
			exp.yokedExpectation = e.getElementsByTagName("YokedExpectation")
					.item(0).getTextContent();
		}

		if (e.getElementsByTagName("PostImage").getLength() > 0) {
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

	public Components CreateLoadTaskCommand() {
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

		if (imgElement != null) {
			String imageSrc = imgElement.getAttribute("source");
			String imageHeight = imgElement.getAttribute("height");
			String imageWidth = imgElement.getAttribute("width");

			QImage img = new QImage();
			img.setSource(ConfigManager.getMediaWebPath() + imageSrc);
			img.setHeight(Integer.parseInt(imageHeight));
			img.setWidth(Integer.parseInt(imageWidth));
			q.setImage(img);
			//MakeMediaAvailable(imageSrc);
		}
		c.setQuestion(q);

		if (multimediaElement != null) {
			String multimediaSrc = multimediaElement.getAttribute("source");
			String multimediaType = multimediaElement.getAttribute("type");

			Multimedia m = new Multimedia();
			m.setSource(ConfigManager.getMediaWebPath() + multimediaSrc);
			m.setType(multimediaType);
			m.setHeight(10);
			m.setWidth(20);
			//MakeMediaAvailable(multimediaSrc);
			c.setMultimedia(m);
		}

		Notice notice = new Notice();
		notice.setNotice("Covered Expectations for Current Task: 0 out of "
				+ totalExpectations);

		DTResponseOld resp = new DTResponseOld();
		Element welcomeElement = (Element) taskNode.getElementsByTagName(
				"Intro").item(0);
		String welcomeMsg = welcomeElement.getTextContent();
		if (welcomeMsg.length() > 0)
			resp.addResponseText(welcomeMsg);
		else
			resp.addResponseText("[introduction message is missing for current task]");

		Avatar av = new Avatar();
		av.setSource("../DTAvatar/DTAvatar.swf");

		c.setAvatar(av);
		c.setNotice(notice);
		c.setResponse(resp);

		return c;
	}

	public Task LoadTask() {
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

		return t;
	}

	public void MakeMediaAvailable(String fileName) {
		
            /* rajendra: disabled for now... should seek alternative or make it work.
             * // we want to make sure that the media file is accessible from the
		// browser
		String webPath = ConfigManager.GetResourcePath() + "\\Media\\";
		String mediaPath = ConfigManager.GetMediaPath() + "\\";

		// we need to copy the file from the real folder to a web accessible
		// folder
		File realFile = new File(mediaPath + fileName);
		File webFile = new File(webPath + fileName);

		if ((!webFile.exists())
				|| (realFile.lastModified() != webFile.lastModified())) {
			try {
				org.apache.commons.io.FileUtils.copyFile(realFile, webFile,
						true);
				System.out
						.print("File temporarily copied in web accesible folder: "
								+ fileName);
			} catch (Exception e) {
				e.printStackTrace();
				System.out
						.print("Error while copying file web accesible folder: "
								+ fileName);
			}
		}
                */
	}

	public String getRelevantText() {

		StringBuilder result = new StringBuilder();
		NodeList expectations = taskNode.getElementsByTagName("Text");
		for (int i = 0; i < expectations.getLength(); i++) {
			String text = ((Element) expectations.item(i)).getTextContent()
					.toLowerCase();
			// if expectation referenced from the current task, get the texts
			// from them. Handling only the single level of reference.
			if (text.startsWith("#") && !disableReferences) {
				String[] params = text.split("#");
				// first string is empty since # appears as the first character
				String taskID = params[1];
				String expID = params[2];
				// check if the task is already loaded
				TaskManager task = refTasks.get(taskID);
				if (task == null) {
                                        //task = new TaskManager(taskID, isEditedFolder);
					task = new TaskManager(taskID); /*
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
				for (int j = 0; j < refTaskTextNodes.getLength(); j++) {
					String textFromRefTask = ((Element) refTaskTextNodes
							.item(j)).getTextContent().toLowerCase();
					result.append(" " + textFromRefTask);
				}

			} else {
				result.append(" " + text);
			}
		}
		return result.toString();
	}


}
