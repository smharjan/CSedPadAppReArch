package dt.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Rajendra Created on Jun 17, 2014, 1:38:28 PM
 */
public class ExpProgressInfo {

    private String id;
    private float maxSimilarityWithInput; //with a sentence in the answer.
    private String maxSimilarText;
    private int maxSimilarSentenceIndex;
    private boolean coverd;
    private int matchedVariant = -1;
    private boolean workingOnHint;
    private boolean workingOnPrompt;
    private float similarity;  //evaluating the whole input text, with any variant of expected answer.
    private int currentHintIndex;
    private DTExpectation expectation;
    private boolean correctAnswerToTheHint;

    public ExpProgressInfo(DTExpectation exp) {
        this.expectation = exp;
        this.id = exp.getId();
        this.currentHintIndex = -1;
    }

    public void cleanup() {
        //TODO: clean, if something is not useful (or makes any harm) for the next epoch.
        correctAnswerToTheHint = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getMaxSimilarityWithInput() {
        return maxSimilarityWithInput;
    }

    public void setMaxSimilarityWithInput(float maxSimilarityWithInput) {
        this.maxSimilarityWithInput = maxSimilarityWithInput;
    }

    public String getMaxSimilarText() {
        return maxSimilarText;
    }

    public void setMaxSimilarText(String maxSimilarText) {
        this.maxSimilarText = maxSimilarText;
    }

    public int getMaxSimilarSentenceIndex() {
        return maxSimilarSentenceIndex;
    }

    public void setMaxSimilarSentenceIndex(int maxSimilarSentenceIndex) {
        this.maxSimilarSentenceIndex = maxSimilarSentenceIndex;
    }

    public boolean isCoverd() {
        return coverd;
    }

    public void setCoverd(boolean coverd) {
        this.coverd = coverd;
    }

    public float getSimilarity() {
        return similarity;
    }

    public void setSimilarity(float similarity) {
        this.similarity = similarity;
    }

    public int getMatchedVariant() {
        return matchedVariant;
    }

    public void setMatchedVariant(int matchedVariant) {
        this.matchedVariant = matchedVariant;
    }

    public boolean isWorkingOnHint() {
        return workingOnHint;
    }

    public void setWorkingOnHint(boolean workingOnHint) {
        this.workingOnHint = workingOnHint;
    }

    public int getCurrentHintIndex() {
        return currentHintIndex;
    }

    public void setCurrentHintIndex(int currentHintIndex) {
        this.currentHintIndex = currentHintIndex;
    }

    public boolean isCorrectAnswerToTheHint() {
        return correctAnswerToTheHint;
    }

    public void setCorrectAnswerToTheHint(boolean correctAnswerToTheHint) {
        this.correctAnswerToTheHint = correctAnswerToTheHint;
    }

    public DTHint getNextHint() {
        ArrayList<DTHint> hints = expectation.getHints();
        if (hints == null) {
            System.out.println("No hints for the expectation " + this.id + " ??");
            return null;
        }
        if (currentHintIndex < hints.size() - 1) {
            return hints.get(currentHintIndex + 1);
        }
        return null;
    }

    public DTExpectation getExpectation() {
        return expectation;
    }

    //expectation is used for misconception. Just named differently.
    public DTExpectation getMisconception() {
        return expectation;
    }

    public DTHint getCurrentHint() {
        ArrayList<DTHint> hints = expectation.getHints();
        if (hints == null) {
            System.out.println("No current hint? " + this.id + " ??");
            return null;
        }
        if (currentHintIndex >= 0 && currentHintIndex < hints.size()) {
            return hints.get(currentHintIndex);
        }
        return null;
    }

    public boolean hasNoMoreHints() {
        DTHint hint = getNextHint();
        if (hint == null) {
            return true;
        }
        return false;
    }

    /**
     * Point to the next hint.
     */
    public void advanceHintIndex() {
        this.currentHintIndex++;
    }

    public DTPrompt getPrompt() {
        return expectation.getPrompt();
    }

    public boolean isWorkingOnPrompt() {
        return workingOnPrompt;
    }

    public void setWorkingOnPrompt(boolean workingOnPrompt) {
        this.workingOnPrompt = workingOnPrompt;
    }
}
