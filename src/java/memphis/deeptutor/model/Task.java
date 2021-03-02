package memphis.deeptutor.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import memphis.deeptutor.singleton.ConfigManager;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Task
{
	private String fileName = "";
	private String creator = "";
	private String taskID = "";
	private String problemText1 = "";
	private String problemText2 = "";
	private String image = "";
	private String multimedia = "";
	private String introduction = "";
	private String summary = "";

	private Expectation[] expectations = new Expectation[0];
	private Expectation[] misconceptions = new Expectation[0];

	public String getSummary()
	{
		return summary;
	}

	public void setSummary(String summary)
	{
		this.summary = summary;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public String getCreator()
	{
		return creator;
	}

	public void setCreator(String creator)
	{
		this.creator = creator;
	}

	public String getTaskID()
	{
		return taskID;
	}

	public void setTaskID(String taskID)
	{
		this.taskID = taskID;
	}

	public String getProblemText1()
	{
		return problemText1;
	}

	public void setProblemText1(String problemText1)
	{
		this.problemText1 = problemText1;
	}

	public String getProblemText2()
	{
		return problemText2;
	}

	public void setProblemText2(String problemText2)
	{
		this.problemText2 = problemText2;
	}

	public String getImage()
	{
		return image;
	}

	public void setImage(String image)
	{
		this.image = image;
	}

	public String getMultimedia()
	{
		return multimedia;
	}

	public void setMultimedia(String multimedia)
	{
		this.multimedia = multimedia;
	}

	public String getIntroduction()
	{
		return introduction;
	}

	public void setIntroduction(String introduction)
	{
		this.introduction = introduction;
	}

	public Expectation[] getExpectations()
	{
		return expectations;
	}

	public void setExpectations(Expectation[] expectations)
	{
		this.expectations = expectations;
	}

	public Expectation[] getMisconceptions()
	{
		return misconceptions;
	}

	public void setMisconceptions(Expectation[] misconceptions)
	{
		this.misconceptions = misconceptions;
	}

	public String CreateXML()
	{
		try
		{

			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("Tasks");
			doc.appendChild(rootElement);

			// staff elements
			Element task = doc.createElement("Task");
			Attr attr = doc.createAttribute("id");
			attr.setValue(this.taskID);
			task.setAttributeNode(attr);
			attr = doc.createAttribute("description");
			attr.setValue("");
			task.setAttributeNode(attr);
			attr = doc.createAttribute("level");
			attr.setValue("1");
			task.setAttributeNode(attr);

			Element elem = doc.createElement("Text");
			elem.appendChild(doc.createTextNode(this.problemText1));
			task.appendChild(elem);

			elem = doc.createElement("Text2");
			elem.appendChild(doc.createTextNode(this.problemText2));
			task.appendChild(elem);

			elem = doc.createElement("Intro");
			elem.appendChild(doc.createTextNode(this.introduction));
			task.appendChild(elem);

			elem = doc.createElement("Summary");
			elem.appendChild(doc.createTextNode(this.summary));
			task.appendChild(elem);

			if (this.image != null && this.image.trim().length() > 0)
			{
				elem = doc.createElement("Image");
				attr = doc.createAttribute("source");
				attr.setValue(this.image);
				elem.setAttributeNode(attr);
				attr = doc.createAttribute("width");
				attr.setValue("100");
				elem.setAttributeNode(attr);
				attr = doc.createAttribute("height");
				attr.setValue("100");
				elem.setAttributeNode(attr);
				task.appendChild(elem);
			}

			if (this.multimedia != null && this.multimedia.trim().length() > 0)
			{
				elem = doc.createElement("Multimedia");
				attr = doc.createAttribute("source");
				attr.setValue(this.multimedia);
				elem.setAttributeNode(attr);
				attr = doc.createAttribute("width");
				attr.setValue("100");
				elem.setAttributeNode(attr);
				attr = doc.createAttribute("height");
				attr.setValue("100");
				elem.setAttributeNode(attr);
				task.appendChild(elem);
			}

			// expectations
			Element expectationList = doc.createElement("ExpectationList");
			for (int i = 0; i < this.expectations.length; i++)
			{
				expectationList.appendChild(CreateExpectationNode(doc,
						this.expectations[i]));
			}
			task.appendChild(expectationList);

			// misconceptions
			Element misconceptionList = doc.createElement("MisconceptionList");
			for (int i = 0; i < this.misconceptions.length; i++)
			{
				misconceptionList.appendChild(CreateExpectationNode(doc,
						this.misconceptions[i]));
			}
			task.appendChild(misconceptionList);

			rootElement.appendChild(task);

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);

			// OutputStream outres = new ByteArrayBuffer();
			// StreamResult result = new StreamResult(outres);
			// transformer.transform(source, result);
			// return outres.toString();

			StreamResult result = new StreamResult(new FileWriter(new File(
					ConfigManager.GetEditedTasksPath()
							+ ConfigManager.GetTaskFileName(taskID))));
			transformer.transform(source, result);
			return "OK";

		}
		catch (ParserConfigurationException pce)
		{
			pce.printStackTrace();
			return "Error creating the XML script.";
		}
		catch (TransformerException tfe)
		{
			tfe.printStackTrace();
			return "Error creating the XML script.";
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return "Error creating the XML script.";
		}
	}

	Element CreateExpectationNode(Document doc, Expectation e)
	{
		Element exp = e.isMisconception ? doc.createElement("Misconception")
				: doc.createElement("Expectation");
		Element elem = null;

		Attr attr = doc.createAttribute("id");
		attr.setValue(e.id);
		exp.setAttributeNode(attr);

		if (!e.isMisconception)
		{
			attr = doc.createAttribute("order");
			attr.setValue(String.valueOf(e.getOrder()));
			exp.setAttributeNode(attr);
			attr = doc.createAttribute("type");
			attr.setValue(e.type.toString().toLowerCase());
			exp.setAttributeNode(attr);

			elem = doc.createElement("Description");
			elem.appendChild(doc.createTextNode(e.description));
			exp.appendChild(elem);

			if (e.postImage != null && e.postImage.trim().length() > 0)
			{
				elem = doc.createElement("PostImage");

				attr = doc.createAttribute("source");
				attr.setValue(e.postImage);
				elem.setAttributeNode(attr);
				attr = doc.createAttribute("width");
				attr.setValue("" + e.postImageSizeWidth);
				elem.setAttributeNode(attr);
				attr = doc.createAttribute("height");
				attr.setValue("" + e.postImageSizeHeight);
				elem.setAttributeNode(attr);

				exp.appendChild(elem);
			}

			if (e.prompt != null && e.prompt.trim().length() > 0)
			{
				Element promptElem = doc.createElement("Prompt");

				elem = doc.createElement("Text");
				elem.appendChild(doc.createTextNode(e.prompt));
				promptElem.appendChild(elem);

				if (e.promptAnswer != null
						&& e.promptAnswer.acceptedAnswer.trim().length() > 0)
					promptElem.appendChild(BuildNodeExpectAnswer("Answer", doc,
							e.promptAnswer));

				if (e.promptCorrection != null)
				{
					elem = doc.createElement("Negative");
					elem.appendChild(doc.createTextNode(e.promptCorrection));
					promptElem.appendChild(elem);
				}

				exp.appendChild(promptElem);
			}

			if (e.hints != null && e.hints.length > 0)
			{
				Element hsElem = doc.createElement("HintSequence");

				Element hintElem = null;
				for (int i = 0; i < e.hints.length; i++)
				{
					hintElem = doc.createElement("Hint");

					attr = doc.createAttribute("type");
					attr.setValue(e.hintsType[i]);
					hintElem.setAttributeNode(attr);

					elem = doc.createElement("Text");
					elem.appendChild(doc.createTextNode(e.hints[i]));
					hintElem.appendChild(elem);

					if (e.hintsAnswer[i] != null
							&& e.hintsAnswer[i].acceptedAnswer.trim().length() > 0)
						hintElem.appendChild(BuildNodeExpectAnswer("Answer",
								doc, e.hintsAnswer[i]));

					if (e.hintsCorrection[i] != null)
					{
						elem = doc.createElement("Negative");
						elem.appendChild(doc
								.createTextNode(e.hintsCorrection[i]));
						hintElem.appendChild(elem);
					}

					hsElem.appendChild(hintElem);
				}
				exp.appendChild(hsElem);
			}
		}
		else
		{
			if (e.yokedExpectation != null
					&& e.yokedExpectation.trim().length() > 0)
			{
				elem = doc.createElement("YokedExpectation");
				elem.appendChild(doc.createTextNode(e.yokedExpectation));
				exp.appendChild(elem);
			}
		}

		// save the text variants
		for (int i = 0; i < e.variants.length; i++)
		{
			elem = doc.createElement("Text");

			attr = doc.createAttribute("id");
			attr.setValue("" + (i + 1));
			elem.setAttributeNode(attr);

			elem.appendChild(doc.createTextNode(e.variants[i]));
			exp.appendChild(elem);

		}

		if (e.assertion != null && e.assertion.trim().length() > 0)
		{
			elem = doc.createElement("Assertion");
			elem.appendChild(doc.createTextNode(e.assertion));
			exp.appendChild(elem);
		}

		if (e.pump != null && e.pump.trim().length() > 0)
		{
			elem = doc.createElement("Pump");
			elem.appendChild(doc.createTextNode(e.pump));
			exp.appendChild(elem);
		}

		if (e.getAlternatePump() != null
				&& e.getAlternatePump().trim().length() > 0)
		{
			elem = doc.createElement("AltPump");
			elem.appendChild(doc.createTextNode(e.getAlternatePump().trim()));
			exp.appendChild(elem);
		}
		if (e.bonus != null && e.bonus.trim().length() > 0)
		{
			elem = doc.createElement("Bonus");
			elem.appendChild(doc.createTextNode(e.bonus));
			exp.appendChild(elem);
		}

		if (e.required != null && e.required.acceptedAnswer.trim().length() > 0)
			exp.appendChild(BuildNodeExpectAnswer("Required", doc, e.required));

		if (e.forbidden != null && e.forbidden.trim().length() > 0)
		{
			elem = doc.createElement("Forbidden");
			elem.appendChild(doc.createTextNode(e.forbidden));
			exp.appendChild(elem);
		}

		return exp;
	}

	Element BuildNodeExpectAnswer(String name, Document doc, ExpectAnswer e)
	{
		Element result = doc.createElement(name);

		Element elem = doc.createElement("Text");
		elem.appendChild(doc.createTextNode(e.acceptedAnswer));
		result.appendChild(elem);

		if (e.wrongAnswer != null && e.wrongAnswer.trim().length() > 0)
		{
			elem = doc.createElement("Wrong");
			elem.appendChild(doc.createTextNode(e.wrongAnswer));
			result.appendChild(elem);
		}

		if (e.goodAnswerVariants != null)
		{
			for (int i = 0; i < e.goodAnswerVariants.length; i++)
			{
				Element elemGwF = doc.createElement("GoodWithFeedback");

				elem = doc.createElement("Text");
				elem.appendChild(doc.createTextNode(e.goodAnswerVariants[i]));
				elemGwF.appendChild(elem);

				elem = doc.createElement("Feedback");
				elem.appendChild(doc.createTextNode(e.goodFeedbackVariants[i]));
				elemGwF.appendChild(elem);

				result.appendChild(elemGwF);
			}
		}

		return result;
	}
}
