/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.actions.misc;

import com.opensymphony.xwork2.ActionSupport;
import dt.constants.Result;
import dt.core.managers.DTTaskManager;
import dt.entities.database.Student;
import dt.entities.plain.SessionData;
import dt.entities.xml.DTResponse;
import dt.log.DTLogger;
import dt.persistent.DataManager;
import java.io.InputStream;
import java.util.Map;
import org.apache.struts2.interceptor.SessionAware;

/**
 *
 * @author Rajendra
 */
public class ResetTasksAction extends ActionSupport implements SessionAware {

    private InputStream inputStream;
    private Map<String, Object> session;

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public String execute() throws Exception {
        System.out.println("[ENTERING RESETTASKS ACTION]");
        Student s = (Student) session.get("student");
        DTLogger logger = s.getLogger();
        //Save app ratings..
        logger.log(DTLogger.Actor.STUDENT, DTLogger.Level.ONE, "Resetting tasks: ");
        logger.saveLogInHTML();
        handleResetTasksCommand(s);
        System.out.println("[RETURNING FROM RESETTASKS ACTION]");
        return Result.SUCCESS;
    }

    /**
     * Reset all tasks so that user can begin with.
     *
     * @param s
     */
    private void handleResetTasksCommand(Student s) {

        s.setCurrentTaskId(null); // in the process of erasing the finished tasks in database. Load task will set it.
        s.setFinishedTasks(null); // will be saved
        //for special user (i.e. demo user), no need to update in the database.
        // Now, rather then checking everywhere, the datamanager filters out
        // if demo user, doesn't save in database, else it does.
        DataManager.updateStudent(s);
        s.setupUnFinishedTasks(); //set all tasks as unfinished (similar to the first time).

        SessionData data = new SessionData();
        s.addInContextData("data", data);

        //Use state transition based dialogue manager?
        if (s.isUseStateTransitionDialogManager()) {
            //TODO: how the state transition based DM will work when user wants to reset all tasks?
            System.err.println("TODO: how the state transition based DM will work when user wants to reset all tasks?");
        }
        //ready to load tasks.
    }
}
