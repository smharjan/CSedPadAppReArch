/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.core.managers;

import dt.entities.database.Student;
import dt.entities.plain.SessionData;
import dt.entities.xml.DTResponse;
import dt.log.DTLogger;
import dt.persistent.DataManager;
import dt.persistent.xml.Components;
import dt.persistent.xml.DTCommands;
import dt.persistent.xml.TaskManager;
import java.util.ArrayList;

/**
 *
 * @author Rajendra
 */
public class DTTaskManager {

    /**
     * Load the next task if any, and then form XML. It should be moved to some
     * common place so that debugtask and reset command can use this.
     */
    public static DTResponse loadTask(Student s) {

        DTResponse response = new DTResponse();
        String taskId = s.getNextTaskId(); //remember,getNextTaskId() updates the assigned task, and current task.

        System.out.println("Loading task: " + taskId);

        //if there is no task to load, just return empty response.
        if (taskId == null) {
            response.setAllTasksFinished(true);
            s.setFinishedAllTasks(true);

            if (s.getCurrentUserClientType() != null && s.getCurrentUserClientType().equalsIgnoreCase("android")) {
                s.setFinishedTasks("");
                DataManager.updateStudent(s);
            }
            return response;
        }
        s.getLogger().log(DTLogger.Actor.SYSTEM, DTLogger.Level.ONE, "Loading task: " + taskId);
        TaskManager task = new TaskManager(taskId);
        SessionData data = (SessionData) s.getFromContextData("data");
        data.taskExpectations = task.GetExpectations(false);
        data.currentTaskID = s.getCurrentTaskId();
        data.debugMode = false;
        Components c = task.CreateLoadTaskCommand();
        // always clear history when moving to a new problem, ??
        c.clearHistory = true;
        c.studentID = s.getStudentId();

        //setup any lefover text (such as question is left after intro.. in demo tasks.. etc). usually not.
        // rajendra: This is redundant.. see.. getleftover() function, 
        // These two things should be refactored..
        data.leftoverText = new ArrayList<String>();
        ArrayList<String> newResponseText = new ArrayList<String>();
        boolean save2leftover = false;
        for (int i = 0; i < c.getResponse().getResponseCount(); i++) {
            if (!save2leftover) {
                if (c.getResponse().getResponseText(i).equals("#WAIT#")) {
                    save2leftover = true;
                } else {
                    newResponseText.add(c.getResponse().getResponseText(i));
                }
            } else {
                data.leftoverText.add(c.getResponse().getResponseText(i));
            }
        }
        if (save2leftover) {
            c.getResponse().setResponseArray(newResponseText);
            c.inputShowContinue = true;
        }

        //packup the response, now.
        response.setXmlResponse(DTCommands.getCommands(c));
        return response;
    }
}
