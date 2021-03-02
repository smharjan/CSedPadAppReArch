/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.core.processors;

import dt.constants.DTCommandID;
import dt.core.dialogue.DTSpellChecker;
import dt.core.managers.DTTaskManager;
import dt.core.managers.NLPManager;
import dt.core.processors.InputTextPreprocessor.InputTextCategory;
import dt.dialog.Ids;
import dt.dialog.STDialogueManager;
import dt.entities.database.Student;
import dt.entities.plain.DTInput;
import dt.entities.plain.SessionData;
import dt.entities.xml.DTResponse;
import dt.persistent.DataManager;
import dt.persistent.xml.Components;
import dt.persistent.xml.DTCommands;
import dt.persistent.xml.DTResponseOld;
import dt.persistent.xml.TaskManager;
import dt.persistent.xml.XMLFilesManager;
import java.util.ArrayList;

/**
 *
 * @author Rajendra Created on Feb 1, 2013, 10:20:56 PM
 */
public class DTCommandProcessor {

    public static DTResponse process(Student s, DTInput input) {
        DTResponse response = null;
        String command = input.getHeader(); //.toLowerCase();, now working case sensitive way.

        //if dialogue form loaded..., this is the first time the user sees
        //dialogue. So, do some initialization, loading, etc.
        if (command.equals(DTCommandID.INITIALIZE)) {
            return handleInitializeCommand(s);
        }

        //Continue command..
        if (command.equals(DTCommandID.CONTINUE)) {

            return handleContinueCommand(s);
        }

        //Continue command..
        if (command.equals(DTCommandID.MOVETONEXT)) {
            return handleMoveToNextCommand(s);
        }

        //reset command..
        if (command.equals(DTCommandID.RESETSESSION)) {
            return handleResetSessionCommand(s);
        }

        //change dialog manager
        if (command.equals(DTCommandID.CHANGEDM)) {
            return handleChangeDMCommand(s);
        }

        return response;
    }

    /**
     * Temporary: Should move to some other place.. and initialize when DT is deployed in Glassfish.
     *
     * Do some initialization, checking etc. Singleton classes should be initialized at DT startup. For now, they are
     * here.
     *
     * @param s
     * @param input
     * @return
     */
    private static DTResponse init() {

        DTResponse response = new DTResponse();
        // Initialize the classes required for next steps; any possible errors
        // should pop up here
        // check if all static classes are initialized
        if (XMLFilesManager.getInstance() == null) {
            response.setErrorFlagged(true);
            response.setResponseText("Failed to initialize XMLFileManager");
            return response;
        }
        if (NLPManager.getInstance() == null) {
            response.setErrorFlagged(true);
            response.setResponseText("Failed to initialize NLP Manager");
            return response;
        }
        if (DTSpellChecker.getInstance() == null) {
            response.setErrorFlagged(true);
            response.setResponseText("Failed to initialize Spell Chceker.. Most probably the dictionary file was not found");
            return response;
        }
        return response;
    }

    /**
     * Handle initialize command.
     *
     * @param s
     * @return
     */
    private static DTResponse handleInitializeCommand(Student s) {

        DTResponse response = init();
        if (response.isErrorFlagged()) {
            return response;
        }
        s.clearAllContextData();
        s.addInContextData("data", new SessionData());
        //if timer is enabled, start timer for dialogue..

        //Use state transition based dialogue manager?
        if (s.isUseStateTransitionDialogManager()) {
            STDialogueManager stDialogueManager = new STDialogueManager();
            stDialogueManager.initialize(s);
            response = stDialogueManager.process(s, null); // no input, null.
            s.addInContextData(Ids.VAR_STDIALOG_MANAGER, stDialogueManager);
            return response;
        }

        // NOTE: get the next task for the student and form the response, what if the tasks are finished??
        // loadTask() should be refactored, a) load task b) if there is task, load task in response
        response = DTTaskManager.loadTask(s);
        return response;
    }

    /**
     * Handle the continue command, if there is no leftover message, then load new task and send the response.
     *
     * @param s
     * @return
     */
    private static DTResponse handleContinueCommand(Student s) {
        //what if there is some leftover text => Get a chunk, update the leftover text (may or may not be left).
        //what if there is no leftover text but all expectations are not covered.
        //what if there is no leftover text and all expectations are covered => The task is finished.
        if (s.isUseStateTransitionDialogManager()) {
            STDialogueManager dialogManager = (STDialogueManager) s.getFromContextData(Ids.VAR_STDIALOG_MANAGER);
            DTInput input = new DTInput();
            input.setData("continue");
            input.setCategory(InputTextCategory.DT_COMMAND);
            DTResponse resp = dialogManager.process(s, input);  //get any leftover, or new task loaded response.
            return resp;
        }

        DTResponse response = getLeftOverResponse(s);
        if (response.hasContent()) {
            return response;
        }

        //if there is no leftover..
        SessionData data = (SessionData) s.getFromContextData("data");
        if (isCurrentTaskFinished(data)) {
            s.addFinishedTaskId(s.getCurrentTaskId());
            //for special user (i.e. demo user), no need to update in the database.
            // Now, rather then checking everywhere, the datamanager filters out
            // if demo user, doesn't save in database, else it does.
            DataManager.updateStudent(s);
            data = new SessionData();
            s.addInContextData("data", data);
            return DTTaskManager.loadTask(s);
        }
        //if nothing, return empty response.
        return response;
    }

    /**
     * When user presses move to next task button. Cases: 1. Might be working on some task and user presses next. 2.
     * There is some task to move on 3. There is no task(s) to move on. 4. User continue moving to next task without
     * working on or working just for some time.
     *
     * @param s
     * @return
     */
    private static DTResponse handleMoveToNextCommand(Student s) {
        SessionData data = new SessionData();
        s.addInContextData("data", data);
        DTResponse response = null;

        //Use state transition based dialogue manager?
        if (s.isUseStateTransitionDialogManager()) {
            STDialogueManager dialogManager = (STDialogueManager) s.getFromContextData(Ids.VAR_STDIALOG_MANAGER);
            dialogManager.reset();
            response = dialogManager.process(s, null);  //SessionData can be retrieved from the student object. SpeechAct? put in the input.
            return response;
        }

        response = DTTaskManager.loadTask(s);
        // For demo user, If all tasks finished, rotate again.
        if (s.isIsSpecialStudent() && response.isAllTasksFinished()) {
            s.assignDemoTaskIds(); //again.
            response = DTTaskManager.loadTask(s);
        }
        return response;
    }

    /**
     * Source, when user presses the Reset session button.
     *
     * @param s
     * @return
     */
    private static DTResponse handleResetSessionCommand(Student s) {
        s.returnToTaskPool(s.getCurrentTaskId());
        SessionData data = new SessionData();
        s.addInContextData("data", data);

        //Use state transition based dialogue manager?
        if (s.isUseStateTransitionDialogManager()) {
            //notice that the current working task is returned back to the task pool (see above - s.returnToTaskPool(s.getCurrentTaskId());).
            STDialogueManager dialogManager = (STDialogueManager) s.getFromContextData(Ids.VAR_STDIALOG_MANAGER);
            dialogManager.reset();
            DTResponse response = dialogManager.process(s, null);  //SessionData can be retrieved from the student object. SpeechAct? put in the input.
            return response;
        }

        //There will be task to load for sure because, one task just returned the 
        //task pool.
        return DTTaskManager.loadTask(s);
    }

    /**
     * Toggle the dialog manager - use state transition based dialog manager or use previous dialog manager.
     *  And reset the task.
     * @param s
     * @return 
     */
    private static DTResponse handleChangeDMCommand(Student s) {
        boolean isStateTransitionDM = s.isUseStateTransitionDialogManager();
        s.setUseStateTransitionDialogManager(!isStateTransitionDM);
        return handleResetSessionCommand(s);
    }

    /**
     * Is current task finished?
     */
    private static boolean isCurrentTaskFinished(SessionData data) {
        if (!data.hasAnythingLeftOver() && data.AllExpectationsCovered()) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param s
     * @return
     */
    private static DTResponse getLeftOverResponse(Student s) {
        DTResponse response = new DTResponse();
        Components c = new Components();
        DTResponseOld r = new DTResponseOld();
        SessionData data = (SessionData) s.getFromContextData("data");

        // if there is no leftover.. then the hasContent() will return false
        // by default. So, just return the empty response.
        if (!data.hasAnythingLeftOver()) {
            return response;
        }

        r.setResponseArray(data.leftoverText);
        c.setResponse(r);

        // look for leftover text -------------, Not sure if it is required for 
        // This code is redundant, another place is just after handling the student response.
        // The response text can have leftover text, and that is set to data.leftoverText
        // Now serving the lefover text, again there can be some leftover..
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

        response.setXmlResponse(DTCommands.getCommands(c));
        return response;
    }
}
