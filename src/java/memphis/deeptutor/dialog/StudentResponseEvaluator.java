package memphis.deeptutor.dialog;

import java.util.ArrayList;
import java.util.Arrays;

import memphis.deeptutor.model.Expectation;
import memphis.deeptutor.singleton.NLPManager;
import memphis.semanticmatcher.SemanticRepresentation;
import memphis.semanticmatcher.tools.SMUtils;

public class StudentResponseEvaluator
{
	float SIMILARITY_THRESHOLD = 0.8f;

	int studentTurn = 0;

	public TaskManager task = null;

	public StudentResponseEvaluator(TaskManager _task)
	{
		task = _task;
	}

	public boolean hasTaskNode()
	{
		if (task.taskNode == null)
			return false;
		return true;
	}

	// this is only for testing purposes
	public String GetStudentTurn()
	{
		studentTurn++;
		return studentTurn + "";
	}

	/*
	 * public String[] GetIdealResponses(String taskID){
	 * 
	 * try{ DocumentBuilderFactory docBuilderFactory =
	 * DocumentBuilderFactory.newInstance(); DocumentBuilder docBuilder =
	 * docBuilderFactory.newDocumentBuilder(); Document doc = docBuilder.parse
	 * (new File(path + "\\BillQuestion.xml"));
	 * 
	 * // normalize text representation doc.getDocumentElement().normalize();
	 * //System.out.println ("Root element of the doc is " +
	 * doc.getDocumentElement().getNodeName());
	 * 
	 * NodeList listOfAnswerTasks = doc.getElementsByTagName("Task");
	 * 
	 * int totalAnswerTasks = listOfAnswerTasks.getLength();
	 * //System.out.println("Total no of tasks : " + totalAnswerTasks);
	 * 
	 * Element myTask = null;
	 * 
	 * for(int s=0; s<totalAnswerTasks ; s++){
	 * 
	 * Element answerTaskNode = (Element)listOfAnswerTasks.item(s); if
	 * (answerTaskNode.getAttribute("id").equals(taskID)) { myTask =
	 * (Element)answerTaskNode; } }
	 * 
	 * if(myTask != null){
	 * 
	 * NodeList expectations = myTask.getElementsByTagName("IdealResponse");
	 * String[] results = new String[expectations.getLength()]; for(int
	 * s=0;s<expectations.getLength();s++) { results[s] =
	 * expectations.item(s).getFirstChild().getTextContent().trim(); }//end of
	 * if clause
	 * 
	 * return results; }//end of for loop with s var
	 * 
	 * }catch (SAXParseException err) { System.out.println ("** Parsing error" +
	 * ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
	 * System.out.println(" " + err.getMessage ()); return null; }catch
	 * (SAXException e) { Exception x = e.getException (); ((x == null) ? e :
	 * x).printStackTrace (); }catch (Throwable t) { t.printStackTrace (); }
	 * return null; }
	 * 
	 * public String[] GetExpectations4Task(String taskID){
	 * 
	 * try{ DocumentBuilderFactory docBuilderFactory =
	 * DocumentBuilderFactory.newInstance(); DocumentBuilder docBuilder =
	 * docBuilderFactory.newDocumentBuilder(); Document doc = docBuilder.parse
	 * (new File(path + "\\DT_AnswersToTasks.xml"));
	 * 
	 * // normalize text representation doc.getDocumentElement().normalize();
	 * //System.out.println ("Root element of the doc is " +
	 * doc.getDocumentElement().getNodeName());
	 * 
	 * NodeList listOfAnswerTasks = doc.getElementsByTagName("AnswerTask");
	 * 
	 * int totalAnswerTasks = listOfAnswerTasks.getLength();
	 * //System.out.println("Total no of tasks : " + totalAnswerTasks);
	 * 
	 * Element myTask = null;
	 * 
	 * for(int s=0; s<totalAnswerTasks ; s++){
	 * 
	 * Element answerTaskNode = (Element)listOfAnswerTasks.item(s); if
	 * (answerTaskNode.getAttribute("id").equals(taskID)) { myTask =
	 * (Element)answerTaskNode; } }
	 * 
	 * if(myTask != null){
	 * 
	 * NodeList expectations = myTask.getElementsByTagName("Expectation");
	 * String[] results = new String[expectations.getLength()]; for(int
	 * s=0;s<expectations.getLength();s++) { results[s] =
	 * expectations.item(s).getFirstChild().getTextContent().trim(); }//end of
	 * if clause
	 * 
	 * return results; }//end of for loop with s var
	 * 
	 * }catch (SAXParseException err) { System.out.println ("** Parsing error" +
	 * ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
	 * System.out.println(" " + err.getMessage ()); return null; }catch
	 * (SAXException e) { Exception x = e.getException (); ((x == null) ? e :
	 * x).printStackTrace (); }catch (Throwable t) { t.printStackTrace (); }
	 * return null; }
	 */

	// extract all the expectations that have all required, but not forbidden
	// words in their input
	public Expectation[] ExtractValidExpectations(String text,
			boolean getMisconception)
	{
		ArrayList<Expectation> resultList = new ArrayList<Expectation>();

		String[] sentences = NLPManager.getInstance().SplitIntoSentences(text);

		// go through the lists of expectations
		Expectation[] expectations = task.GetExpectations(getMisconception);
		if (expectations == null)
			return new Expectation[0];

		for (int i = 0; i < expectations.length; i++)
		{
			expectations[i].isMisconception = getMisconception; // not sure if
																// we really
																// need this

			for (int j = 0; j < sentences.length; j++)
			{
				if ((expectations[i].required == null || expectations[i].required
						.HasAllTheRequiredWords(sentences[j], false))
						&& (expectations[i].forbidden == null || !ValidatesExpression(
								sentences[j], expectations[i].forbidden)))
				{
					resultList.add(expectations[i]);
					break;
				}
			}
		}
		Expectation[] result = new Expectation[resultList.size()];
		return resultList.toArray(result);
	}

	// extract all the expectations that have all required, but not forbidden
	// words in their input
	public Expectation[] ExtractBonusExpectations(String text,
			boolean getMisconception)
	{
		ArrayList<Expectation> resultList = new ArrayList<Expectation>();

		// go through the lists of expectations
		Expectation[] expectations = task.GetExpectations(getMisconception);
		if (expectations == null)
			return new Expectation[0];

		for (int i = 0; i < expectations.length; i++)
		{
			if (ValidatesExpression(text, expectations[i].bonus)
					&& !ValidatesExpression(text, expectations[i].forbidden))
			{
				resultList.add(expectations[i]);
				break;
			}
		}
		Expectation[] result = new Expectation[resultList.size()];
		return resultList.toArray(result);
	}

	// the input can have multiple sentences
	public Expectation[] ComputeSimilarityAndSort(String text,
			Expectation[] expectations)
	{
		String[] sentences = NLPManager.getInstance().SplitIntoSentences(text);

		for (int i = 0; i < expectations.length; i++)
			CompareToExpectation(sentences, expectations[i]);

		// sort the array
		Arrays.sort(expectations);

		return expectations;
	}

	// the input can have multiple sentences
	public float CompareToExpectation(String[] sentences,
			Expectation expectation)
	{
		float maxSim = 0;
		String maxStr = "";
		int sentenceIndex = -1;
		for (int j = 0; j < sentences.length; j++)
		{
			for (int k = 0; k < expectation.variants.length; k++)
			{
				float sim = NLPManager.getInstance().ComputeT2TWNSimilarity(
						sentences[j], expectation.variants[k]);

				if ((j == 0 && k == 0) || (Math.abs(sim) > Math.abs(maxSim)))
				{
					maxSim = sim;
					maxStr = expectation.variants[k];
					sentenceIndex = j;
				}
			}
		}
		expectation.similarity = maxSim;
		expectation.mostSimilarText = maxStr;
		expectation.sentence = sentenceIndex;
		expectation.setMatchedSentenceInInputText(sentences[sentenceIndex]);

		return maxSim;
	}

	public boolean ValidatesExpression(String text, String list)
	{
		if (list == null)
			return false;

		SemanticRepresentation semText = new SemanticRepresentation(text);
		NLPManager.getInstance().PreprocessText(semText);
		return NLPManager.getInstance().MatchRegularExpression(semText, list,
				false);
	}

	public boolean AnswerTooBrief(SemanticRepresentation text)
	{
		int contentCount = 0;
		// Count content words
		for (int i = 0; i < text.tokens.size(); i++)
			if (SMUtils.getWordNetPOS(text.tokens.get(i).POS) != null)
				contentCount++;

		if (contentCount > 2)
			return false;
		else
			return true;
	}

	public boolean AnswerNotRelevant(SemanticRepresentation text)
	{
		String relevantText = task.getRelevantText();
		int totalContent = 0;
		int matchedContent = 0;

		// at least half of the content words have to be in the relevant text
		for (int i = 0; i < text.tokens.size(); i++)
			if (SMUtils.getWordNetPOS(text.tokens.get(i).POS) != null)
			{
				totalContent++;
				if (relevantText.contains(text.tokens.get(i).rawForm
						.toLowerCase()))
					matchedContent++;
			}

		if (totalContent > matchedContent * 2)
			return true;
		return false;
	}

}
