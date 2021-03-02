/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.actions.authentication;

import com.opensymphony.xwork2.ActionSupport;
//com.opensymphony.xwork2.ActionSupport;
import dt.constants.Result;
import dt.entities.database.Student;
import dt.persistent.DataManager;
import dt.persistent.database.Students;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.struts2.interceptor.SessionAware;

/**
 * This action is invoked from login page or from the agreement page itself (when user accepts
 *  the agreement)
 *
 * @author Rajendra
 */
public class UserAgreementAction extends ActionSupport implements SessionAware {

    /* Rajendra: When redirected after successful authentication, this parameter 
     * should be set to true (from struts configuratin) but it will not be set to
     * true from the agreement page itself. So, if the agreement has not been signed 
     * yet, this parameter should be set to false before showing the user agreement
     * (to know that when this action is invoked next time, we can be sure that it
     * has come from the agreement page itself).
     */
    private boolean isInvokedFromLogin = false;
    private Map<String, Object> session;

    public boolean isIsInvokedFromLogin() {
        return isInvokedFromLogin;
    }

    public void setIsInvokedFromLogin(boolean isInvokedFromLogin) {
        this.isInvokedFromLogin = isInvokedFromLogin;
    }

    public UserAgreementAction() {
    }

    public String execute() throws Exception {
        System.out.println("[ENTERING UserAgreementAction ACTION]");
        boolean hasSignedAgreement = false;
        Student s = (Student) session.get("student");
        
//        if (s.isIsSpecialStudent()) {
//            return Result.SUCCESS;
//        }

        if (isIsInvokedFromLogin()) {
            hasSignedAgreement = s.isHasAcceptedTermsAndConditions();
            setIsInvokedFromLogin(false);
        } else {
            // if is has come from the agreement page itself, it is safe to assume 
            // that user has signed (clicked the accept button).            
            hasSignedAgreement = true;
            s.setHasAcceptedTermsAndConditions(true);
            DataManager.updateStudent(s);
            session.put("student", s);
        }

        if (hasSignedAgreement) {
            return Result.SUCCESS;
        }
        return Result.INPUT;
    }

    @Override
    public void setSession(Map<String, Object> map) {
        this.session = map;
    }
}
