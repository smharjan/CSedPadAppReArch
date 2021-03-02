/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.task;

/**
 *
 * @author Rajendra
 */
public class DTPrompt {

    private String text; //question (fill in the blanks?)
    private String expectedWords; //expected answer (words, in regular expression).
    private String negative; //Response when the answer is not correct?

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getExpectedWords() {
        return expectedWords;
    }

    public void setExpectedWords(String expectedWords) {
        this.expectedWords = expectedWords;
    }

    public String getNegative() {
        return negative;
    }

    public void setNegative(String negative) {
        this.negative = negative;
    }
}
