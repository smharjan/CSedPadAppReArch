package dt.persistent.xml;

import dt.core.managers.NLPManager;
import dt.core.semantic.SemanticRepresentation;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//This class will allow to handle multiple types of answers, with specific feedback on them
public class ExpectAnswer {

	public String acceptedAnswer = null;
	public String wrongAnswer = null;
	public String[] goodAnswerVariants = null;
	public String[] goodFeedbackVariants = null;
	
	public int matchedVariant = -1;
	
	public ExpectAnswer(Element node)
	{
		Element[] answerNodes = XMLFilesManager.GetXMLChildrenElements(node, "Text"); 
		if (answerNodes.length>0)
		{
			acceptedAnswer = answerNodes[0].getTextContent();
			//get the list of accepted answer; we could possibly have BadWithFeedback also
			NodeList variants = node.getElementsByTagName("GoodWithFeedback");
			goodAnswerVariants = new String[variants.getLength()];
			goodFeedbackVariants = new String[variants.getLength()];
			for (int i=0;i<variants.getLength();i++)
			{
				Element variant = (Element)variants.item(i); 
				goodAnswerVariants[i] = variant.getElementsByTagName("Text").item(0).getTextContent();
				goodFeedbackVariants[i] = variant.getElementsByTagName("Feedback").item(0).getTextContent();
			}
			if (node.getElementsByTagName("Wrong").getLength()>0)
				wrongAnswer = node.getElementsByTagName("Wrong").item(0).getTextContent();;
		}
		else 
		{
			acceptedAnswer = node.getTextContent();
			goodAnswerVariants = new String[0];
			goodFeedbackVariants = new String[0];
		}
	}
	
	public ExpectAnswer(String accepted)
	{
		acceptedAnswer = accepted;
		goodAnswerVariants = new String[0];
		goodFeedbackVariants = new String[0];
	}

	public boolean HasAllTheRequiredWords(String text, Boolean negationCheck) 
	{
		matchedVariant = -1;
		
		if (acceptedAnswer.trim().length() == 0) return true;

		SemanticRepresentation semText = new SemanticRepresentation(text);
		NLPManager.getInstance().PreprocessText(semText);
		
		if (NLPManager.getInstance().MatchRegularExpression(semText, acceptedAnswer, negationCheck)) return true;
		
		//check the answer variants if the initial answer requirements are not met
		for (int i=0;i<goodAnswerVariants.length; i++)
			if (NLPManager.getInstance().MatchRegularExpression(semText, goodAnswerVariants[i], negationCheck)) 
			{
				matchedVariant = i;
				return true;
			}
		
		return false;
	}
	
	public boolean HasForbbidenWords(String text) 
	{
		if (wrongAnswer == null) return false;

		SemanticRepresentation semText = new SemanticRepresentation(text);
		NLPManager.getInstance().PreprocessText(semText);
		
		// Vasile: wrongAnswer should be replaced with checking forbidden words; anyhow this function is not called at all as of Oct 31, 2012
		if (NLPManager.getInstance().MatchRegularExpression(semText, wrongAnswer, true)) return true;
		
		return false;
	}

	public boolean HasWrongWords(String text) 
	{
		if (wrongAnswer == null) return false;

		SemanticRepresentation semText = new SemanticRepresentation(text);
		NLPManager.getInstance().PreprocessText(semText);
		
		if (NLPManager.getInstance().MatchRegularExpression(semText, wrongAnswer, true)) return true;
		
		return false;
	}

	public String GetAnswerFeedback()
	{
		if (matchedVariant<0) return XMLFilesManager.getInstance().GetSomeFeedback("PositiveFeedback");
		else return goodFeedbackVariants[matchedVariant];
	}

	public String GetAnswerFeedbackData()
	{
		if (matchedVariant<0) return "PF";
		else return "GF#" + matchedVariant;
	}

	public String getAcceptedAnswer() {
		return acceptedAnswer;
	}

	public void setAcceptedAnswer(String acceptedAnswer) {
		this.acceptedAnswer = acceptedAnswer;
	}

	public String getWrongAnswer() {
		return wrongAnswer;
	}

	public void setWrongAnswer(String wrongAnswer) {
		this.wrongAnswer = wrongAnswer;
	}

	public String[] getGoodAnswerVariants() {
		return goodAnswerVariants;
	}

	public void setGoodAnswerVariants(String[] goodAnswerVariants) {
		this.goodAnswerVariants = goodAnswerVariants;
	}

	public String[] getGoodFeedbackVariants() {
		return goodFeedbackVariants;
	}

	public void setGoodFeedbackVariants(String[] goodFeedbackVariants) {
		this.goodFeedbackVariants = goodFeedbackVariants;
	}

	public int getMatchedVariant() {
		return matchedVariant;
	}

	public void setMatchedVariant(int matchedVariant) {
		this.matchedVariant = matchedVariant;
	}

	
}
