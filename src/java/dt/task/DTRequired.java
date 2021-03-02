/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.task;

import dt.core.managers.NLPManager;
import dt.core.semantic.SemanticRepresentation;
import dt.persistent.xml.XMLFilesManager;
import java.util.ArrayList;

/**
 * What is this in Expectation?? see task file, such as: LP99_PR99 In the previous version of code, the class is named
 * 'ExpectAnswer'
 *
 * @author Rajendra
 */
public class DTRequired {

    //TODO: xml element 'text'
    private String acceptedAnswer;
    ArrayList<DTGoodWithFeedback> goodWithFeedbacks;
    //private String wrongAnswer = null;
    //TODO: The following two are covered in goodWithFeedbacks??
    //private String[] goodAnswerVariants = null;
    //private String[] goodFeedbackVariants = null;
    //private int matchedVariant = -1;

    //TODO: see the constructor 'ExpectAnswer(Node node)'.
    public DTRequired() {
        goodWithFeedbacks = new ArrayList<DTGoodWithFeedback>();
    }

    public ArrayList<DTGoodWithFeedback> getGoodWithFeedbacks() {
        return goodWithFeedbacks;
    }

    public void setGoodWithFeedbacks(ArrayList<DTGoodWithFeedback> goodWithFeedbacks) {
        this.goodWithFeedbacks = goodWithFeedbacks;
    }

    public String getAcceptedAnswer() {
        return acceptedAnswer;
    }

    public void setAcceptedAnswer(String acceptedAnswer) {
        this.acceptedAnswer = acceptedAnswer;
    }

    /**
     * Check whethere the answer contains all the required words Also, checks if it matches with the good with feedback
     * answers.
     *
     * @param text
     * @param negationCheck
     * @return
     */
    public boolean hasAllTheRequiredWords(String text, Boolean negationCheck, ExpProgressInfo expProgressInfo) {
        expProgressInfo.setMatchedVariant(-1);

        if (acceptedAnswer.trim().length() == 0) {
            return true;
        }

        SemanticRepresentation semText = new SemanticRepresentation(text);
        NLPManager.getInstance().PreprocessText(semText);

        if (NLPManager.getInstance().MatchRegularExpression(semText, acceptedAnswer, negationCheck)) {
            return true;
        }

        //check the answer variants if the initial answer requirements are not met
        for (int i = 0; i < goodWithFeedbacks.size(); i++) {
            DTGoodWithFeedback gwf = goodWithFeedbacks.get(i);
            if (NLPManager.getInstance().MatchRegularExpression(semText, gwf.getText(), negationCheck)) {
                expProgressInfo.setMatchedVariant(i);  //TODO: why is it saved??
                return true;
            }
        }
        return false;
    }

    public String getAnswerFeedback(ExpProgressInfo expProgressInfo) {
        if (expProgressInfo.getMatchedVariant() < 0) {
            return XMLFilesManager.getInstance().GetSomeFeedback("PositiveFeedback");
        } else {
            return goodWithFeedbacks.get(expProgressInfo.getMatchedVariant()).getFeedback();
        }
    }
}
