/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.actions.misc;

import com.opensymphony.xwork2.ActionSupport;

/**
 *
 * @author admin
 */
public class FeedbackAction extends ActionSupport {

    private String userAction;

    public String getUserAction() {
        return userAction;
    }

    public void setUserAction(String userAction) {
        this.userAction = userAction;
    }

    public String getUserInputText() {
        return userInputText;
    }

    public void setUserInputText(String userInputText) {
        this.userInputText = userInputText;
    }
    private String userInputText;

    public FeedbackAction() {
    }

    @Override
    public String execute() throws Exception {
        System.out.println("[ENTERING FEEDBACK ACTION]");
        String result = "";


        System.out.println("[RETURNING FROM FEEDBACK ACTION]");
        return result;
    }
}
