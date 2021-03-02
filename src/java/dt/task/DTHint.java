/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.task;

/**
 *
 * @author Rajendra
 */
public class DTHint {

    private String type; //final, sequence, conditional.
    private String text; //question
    private String negative; //feedback
    private String expectedWords; //words.
    private String wrong; //TODO: ??

    public enum HintType {

        SEQUENCE, CONDITIONAL, FINAL, NONE
    }
    private HintType hintType = HintType.NONE;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getNegative() {
        return negative;
    }

    public void setNegative(String negative) {
        this.negative = negative;
    }

    public HintType getHintType() {
        return hintType;
    }

    public void setHintType(HintType hintType) {
        this.hintType = hintType;
    }

    public String getExpectedWords() {
        return expectedWords;
    }

    public void setExpectedWords(String expectedWords) {
        this.expectedWords = expectedWords;
    }

    public String getWrong() {
        return wrong;
    }

    public void setWrong(String wrong) {
        this.wrong = wrong;
    }
}
