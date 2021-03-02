/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.dialog;

import dt.core.dialogue.SAClassifier;
import dt.core.processors.StudentContributionProcessor;
import dt.entities.database.Student;
import dt.entities.plain.DTInput;
import dt.entities.xml.DTResponse;
import dt.persistent.xml.Components;
import dt.persistent.xml.DTCommands;
import dt.persistent.xml.DTResponseOld;
import dt.persistent.xml.TaskManager;
import dt.task.DTHint;
import dt.task.DTPrompt;
import dt.task.ExpProgressInfo;
import dt.task.TaskProgressInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Rajendra
 */
public class STActionHandler {

    /**
     * Run action commands.
     *
     * @param actions
     * @param student
     * @param input
     * @param dialogContext
     */
    public static void executeActions(List<Action> actions, Student student, DTInput input, HashMap<String, Object> dialogContext) {
        for (Action action : actions) {
            executeAction(action, student, input, dialogContext);
        }
    }

    /**
     * Run an action command.
     *
     * @param action
     * @param student
     * @param input
     * @param dialogContext
     */
    public static void executeAction(Action action, Student student, DTInput input, HashMap<String, Object> dialogContext) {
        String actionName = action.getValue();
        System.out.println("Running action: " + actionName);
        if (actionName.equalsIgnoreCase(Ids.ACTION_LOAD_TASK)) {
            executeLoadTaskAction(student, dialogContext);
        } else if (actionName.equalsIgnoreCase(Ids.ACTION_CHECK_ANSWER_COVERS_EXPS)) {
            checkIfAnswerCoversAnyExpectations(input, dialogContext);
        } else if (actionName.equalsIgnoreCase(Ids.ACTION_GET_NEXT_UNCOVERED_EXP)) {
            getNextUncoveredExpectation(input, dialogContext);
        } else if (actionName.equalsIgnoreCase(Ids.ACTION_GET_FEEDBACK)) {
            //what to do,?? nothing.
        } else if (actionName.equalsIgnoreCase(Ids.ACTION_CREATE_TASK_EXP_SUMMARY)) {
            createTaskExpectationsSummary(input, dialogContext);
        } else if (actionName.equalsIgnoreCase(Ids.ACTION_SHOW_PUMP)) {
            addPumpInTheResponse(input, dialogContext);
        } else if (actionName.equalsIgnoreCase(Ids.ACTION_GIVE_HINT)) {
            giveHint(student, dialogContext);
        } else if (actionName.equalsIgnoreCase(Ids.ACTION_SHOW_PROMPT)) {
            showPrompt(student, dialogContext);
        } else if (actionName.equalsIgnoreCase(Ids.ACTION_MARK_CURRENT_EXP_COVERED)) {
            markCurrentExpCovered(student, dialogContext);
        }
    }

    /**
     * Give hint
     *
     * @param student
     * @param dialogContext
     */
    public static void giveHint(Student student, HashMap<String, Object> dialogContext) {
        TaskProgressInfo taskProgressInfo = (TaskProgressInfo) dialogContext.get(Ids.VAR_TASK_PROGRESS_INFO);
        DTHint hint = taskProgressInfo.getNextHint();
        //TODO: if there is no more hint, it shouldn't come to this place.
        if (hint == null) {
            return;
        }
        DTResponseOld resp = taskProgressInfo.getDTResponseOld();
        resp.addResponseText("" + hint.getText());
        taskProgressInfo.getCurrentExpProgressInfo().setWorkingOnHint(true); //set working on hint true. 
        DTResponse response = convertToNewDTResponseFormat(resp);
        dialogContext.put(Ids.VAR_DT_RESPONSE, response);
    }

    public static void showPrompt(Student student, HashMap<String, Object> dialogContext) {
        TaskProgressInfo taskProgressInfo = (TaskProgressInfo) dialogContext.get(Ids.VAR_TASK_PROGRESS_INFO);

        DTPrompt prompt = taskProgressInfo.getCurrentExpProgressInfo().getPrompt();
        //TODO: if there is no prompt, it shouldn't come to this place.
        if (prompt == null) {
            return;
        }
        DTResponseOld resp = taskProgressInfo.getDTResponseOld();
        resp.addResponseText("" + prompt.getText());
        taskProgressInfo.getCurrentExpProgressInfo().setWorkingOnPrompt(true); //set working on hint true. 
        taskProgressInfo.getCurrentExpProgressInfo().setWorkingOnHint(false);
        DTResponse response = convertToNewDTResponseFormat(resp);
        dialogContext.put(Ids.VAR_DT_RESPONSE, response);
    }

    public static void markCurrentExpCovered(Student student, HashMap<String, Object> dialogContext) {
        TaskProgressInfo taskProgressInfo = (TaskProgressInfo) dialogContext.get(Ids.VAR_TASK_PROGRESS_INFO);
        ExpProgressInfo currentExpProgressInfo = taskProgressInfo.getCurrentExpProgressInfo();
        if (currentExpProgressInfo != null) {
            currentExpProgressInfo.setCoverd(true);
            DTResponseOld resp = taskProgressInfo.getDTResponseOld();
            resp.addResponseText("" + currentExpProgressInfo.getExpectation().getAssertion() + "#WAIT#");
            System.out.println("Marking " + currentExpProgressInfo.getId() + " as covered.");
        }
    }

    /**
     * Load new task
     *
     * @param student
     * @param dialogContext
     */
    public static void executeLoadTaskAction(Student student, HashMap<String, Object> dialogContext) {
        String nextTaskId = student.getNextTaskId();

        //DTResponse response1 = DTTaskManager.loadTask(student);
        TaskManager taskManager = new TaskManager(nextTaskId);

        TaskProgressInfo taskProgressInfo = TaskFormatConverter.convert(taskManager);

        Components c = taskManager.CreateLoadTaskCommand();
        // always clear history when moving to a new problem, ??
        c.clearHistory = true;
        DTResponse response = new DTResponse();
        response.setXmlResponse(DTCommands.getCommands(c));
        System.out.println("Sending to the client: " + response.getXmlResponse());

        //TaskProgressInfo taskProgressInfo = new TaskProgressInfo(task);
        dialogContext.put(Ids.VAR_TASK_PROGRESS_INFO, taskProgressInfo);

        //create a response containing the task description and set to the dialog context.
        dialogContext.put(Ids.VAR_DT_RESPONSE, response);
        dialogContext.put(Ids.VAR_TASK_FINISHED, false);
    }

    /**
     * Check if the current input covers ANY (not just the current expectation).
     * So it might be called everytime the student answer is received.
     *
     * @param input
     * @param dialogContext
     */
    public static void checkIfAnswerCoversAnyExpectations(DTInput input, HashMap<String, Object> dialogContext) {
        TaskProgressInfo taskProgressInfo = (TaskProgressInfo) dialogContext.get(Ids.VAR_TASK_PROGRESS_INFO);
        StudentContributionProcessor.checkIfAnswerCoversAnyExpectations(input, taskProgressInfo);
    }

    /**
     * Get and set next uncovered expectation to handle.
     *
     * @param student
     * @param input
     * @param dialogContext
     */
    public static void getNextUncoveredExpectation(DTInput input, HashMap<String, Object> dialogContext) {
        TaskProgressInfo taskProgressInfo = (TaskProgressInfo) dialogContext.get(Ids.VAR_TASK_PROGRESS_INFO);
        ExpProgressInfo exp = taskProgressInfo.getNextUncoveredExpectaion();
        taskProgressInfo.setCurrentExpProgressInfo(exp);
        System.out.println("Started working on expectation: " + exp.getId());
        //response?
    }

    /**
     * Add pump in the response. There might be some feedback of the previous
     * answer in the response object so append it.
     *
     * @param student
     * @param input
     * @param dialogContext
     */
    public static void addPumpInTheResponse(DTInput input, HashMap<String, Object> dialogContext) {
        TaskProgressInfo taskProgressInfo = (TaskProgressInfo) dialogContext.get(Ids.VAR_TASK_PROGRESS_INFO);
        StudentContributionProcessor.getPump(input, taskProgressInfo);
        DTResponseOld respOld = taskProgressInfo.getDTResponseOld();
        DTResponse response = convertToNewDTResponseFormat(respOld);
        dialogContext.put(Ids.VAR_DT_RESPONSE, response);
    }

    /**
     * Create a summary of task expectations.
     *
     * @param input
     * @param dialogContext
     */
    public static void createTaskExpectationsSummary(DTInput input, HashMap<String, Object> dialogContext) {
        TaskProgressInfo taskProgressInfo = (TaskProgressInfo) dialogContext.get(Ids.VAR_TASK_PROGRESS_INFO);
        taskProgressInfo.summarizeExpectations();
        DTResponseOld respOld = taskProgressInfo.getDTResponseOld();
        DTResponse response = convertToNewDTResponseFormat(respOld);
        dialogContext.put(Ids.VAR_DT_RESPONSE, response);
        taskProgressInfo.setTaskFinished(true);
    }

    /**
     * Convert old DTResponse object to DTResponse.
     *
     * @param resp
     * @return
     */
    public static DTResponse convertToNewDTResponseFormat(DTResponseOld resp) {
        DTResponse response = new DTResponse();
        Components c = new Components();
        c.setResponse(resp);

        //set leftover (if any).
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
                response.addLeftoverText(c.getResponse().getResponseText(i));
            }
        }
        if (save2leftover) {
            c.getResponse().setResponseArray(newResponseText);
            c.inputShowContinue = true;
        }
        response.setXmlResponse(DTCommands.getCommands(c));
        return response;
    }

    public static boolean getLeftOverResponse(DTInput input, HashMap<String, Object> dialogContext) {
        DTResponse resp = (DTResponse) dialogContext.get(Ids.VAR_DT_RESPONSE);
        if (resp == null || resp.getLeftOverText().size() <= 0) {
            return false;
        }

        //TODO: for now, moving text from here to there.
        DTResponseOld respOld = new DTResponseOld();
        respOld.setResponseArray(resp.getLeftOverText());

        //set the next leftover chunk as response, and update the leftover text
        DTResponse updatedResp = convertToNewDTResponseFormat(respOld);
        dialogContext.put(Ids.VAR_DT_RESPONSE, updatedResp);
        return true;
    }

    /**
     * Execute any common function required to set condition variables for
     * potential transitions. For example: evaluating student answer
     *
     * TODO: now, some variables are set in TaskProgressInfo to know at which
     * step we are. For example: working on hint, pump, prompt etc. We could
     * identify the current state from the name/id of current state.Need to
     * evaluate which option is better - set some variables in Task progress
     * info or know where it is from the ID/Name of current state.
     *
     * @param input
     * @param dialogContext
     */
    static void executeCommonPreActions(DTInput input, HashMap<String, Object> dialogContext) {
        TaskProgressInfo taskProgressInfo = (TaskProgressInfo) dialogContext.get(Ids.VAR_TASK_PROGRESS_INFO);
        //for the first time, there will not be any taskProgressInfo (created only when a new task is loaded).
        //input will be null or some command, but not contribution.
        if (input != null && input.getSpeechAct() == SAClassifier.SPEECHACT.Contribution) {
            //call this function only when the student responded for main task answer, or pump. Right??
            ExpProgressInfo currentExpProgressInfo = taskProgressInfo.getCurrentExpProgressInfo();

            //if working on a specific expectation
            if (currentExpProgressInfo != null) {
                if (currentExpProgressInfo.isWorkingOnHint()) {
                    StudentContributionProcessor.evaluateHintAnswer(input, taskProgressInfo);
                    return;
                    //TODO: now, if it is working on hint, do not see if it covers some expectation. In the existing code, if there is no hint answer,
                    // it tries to compare with the current expectation.
                } else if (currentExpProgressInfo.isWorkingOnPrompt()) {
                    StudentContributionProcessor.evaluatePromptAnswer(input, taskProgressInfo);
                    return;
                }
            }
            // if not working on hint, prompt - evaluate the answer.
            StudentContributionProcessor.checkIfAnswerCoversAnyExpectations(input, taskProgressInfo);
        }
        //TODO: how to change the image, i.e. multimedia.
    }
}
