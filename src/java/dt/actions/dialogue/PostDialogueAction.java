/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.actions.dialogue;

import com.opensymphony.xwork2.ActionSupport;
import dt.constants.Result;

/**
 *
 * @author Rajendra
 */
public class PostDialogueAction extends ActionSupport {
    
    public PostDialogueAction() {
    }
    
    public String execute() throws Exception {
        return Result.FINISHEDALL;
    }
}
