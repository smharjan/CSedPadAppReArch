/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.actions.misc;

import com.opensymphony.xwork2.ActionSupport;
import dt.constants.Result;
import dt.entities.database.Student;
import dt.log.DTLogger;
import java.io.InputStream;
import java.util.Map;
import org.apache.struts2.interceptor.SessionAware;

/**
 *
 * @author Rajendra
 */
public class UserRatingAction extends ActionSupport implements SessionAware {

    private String rating;  //mainly targeted for rating our APP.
    private InputStream inputStream;
    private Map<String, Object> session;

    public String getRating() {
        return rating;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    @Override
    public String execute() throws Exception {
        System.out.println("[ENTERING RATING ACTION]");
        System.out.println("User  rating:" + rating);
        Student s = (Student) session.get("student");
        DTLogger logger = s.getLogger();
        //Save app ratings..
        logger.log(DTLogger.Actor.STUDENT, DTLogger.Level.ONE, "Rating: " + rating);
        logger.saveLogInHTML();
        System.out.println("[RETURNING FROM RATING ACTION]");
        return Result.SUCCESS;
    }
}
