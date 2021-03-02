/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.task;

/**
 *
 * @author Rajendra
 */
class DTAnswer {
    private String text;
    private DTGoodWithFeedback goodWithFeedback;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public DTGoodWithFeedback getGoodWithFeedback() {
        return goodWithFeedback;
    }

    public void setGoodWithFeedback(DTGoodWithFeedback goodWithFeedback) {
        this.goodWithFeedback = goodWithFeedback;
    }
}
