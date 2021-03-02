package dt.dialog;

import dt.config.ConfigManager;
import dt.core.processors.InputTextPreprocessor;
import dt.core.processors.InputTextPreprocessor.InputTextCategory;
import dt.entities.database.Student;
import dt.entities.plain.DTInput;
import dt.entities.xml.DTResponse;
import dt.task.TaskProgressInfo;
import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * This class is the main entry point for the state transition based dialogue management. Basically, it provides an
 * interface which takes an input from the client, Student information (including the dialogue policy) and returns the
 * output. It has nothing to do about client interactions. It just process the input and returns the output.
 *
 * The Name ST stands for State Transition
 *
 * @author Rajendra Created on Jun 10, 2014, 8:01:45 PM
 */
public class STDialogueManager {

    DialogPolicy dialogPolicy = null;
    HashMap<String, Object> dialogContext = null;

    public STDialogueManager() {
        dialogPolicy = new DialogPolicy();
        dialogContext = new HashMap<String, Object>();
    }

    /**
     * Initialize dialogue manager.
     *
     * @return
     */
    public boolean initialize(Student student) {
        try {
            //TODO: the dialog policy may depend on the student. So, student object is passed here but not used now. 
            //String dialogPolicyFilePath = "C:\\Users\\Administrator\\Documents\\NetBeansProjects\\csedpad\\web\\WEB-INF\\dt_dialog_policy.xml";
            String dialogPolicyFilePath = "D:\\workspace\\csedpad\\web\\WEB-INF\\dt_dialog_policy.xml";
            File dialogPolicyFile = new File(dialogPolicyFilePath);
            //If developer mode (i.e. source code folder exists and the dialog policy file is there, use that).
            // It allows developer to modify, update the dialog policy file in SVN and use the most upto date file.
            if (dialogPolicyFile.exists()) {
                dialogPolicy.parsePolicyFile(dialogPolicyFilePath);
            } else {
                dialogPolicy.parsePolicyFile(ConfigManager.getDialogPolicyFilePath());
            }
            dialogContext.put(Ids.VAR_CURRENT_STATE, dialogPolicy.getStartState());
        } catch (Exception exe) {
            exe.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Proces the input and prepare some response. Dialog policy is parsed (once) and set in the student object.
     *
     * @param student
     * @param input
     * @return
     */
    public DTResponse process(Student student, DTInput input) {
        DTResponse resp = null;
        State currentState = null;

        System.out.println("ENTERING ST Dialogue Manager ==============");

        //Check if there is anyleftover response..
        boolean leftOver = STActionHandler.getLeftOverResponse(input, dialogContext);

        if (leftOver || (input != null && input.getCategory() == InputTextCategory.DT_COMMAND)) {
            if (leftOver) {
                System.out.println("Returning leftover data..");
                resp = (DTResponse) dialogContext.get(Ids.VAR_DT_RESPONSE);
                cleanup();
                return resp;
            }
            //TODO: we can probably find a better solution. This is just working as
            // to differentialte the continue command at the end of task or at the middle.
            // The continue command at the end of the task should load another task. So, just return nulll
            // if there is no leftover and task is not finished.
            if (!ConditionEvaluator.isTaskFinished(dialogContext)) {
                return null;
            }
        }

        //get the current state. for the first time, the starting state is set in it (during initialization).
        currentState = (State) dialogContext.get(Ids.VAR_CURRENT_STATE);
        System.out.println("CURRENT STATE: " + currentState.getName());

        //if there are some actions required for the possible transitions, execute them
        // once and in each ocondition evaluation, just check some flags/variables.
        // The common actions are - evaluate first short essay answer, prompt answer, hint answer etc.              
        STActionHandler.executeCommonPreActions(input, dialogContext);

        //evaluate conditions and move to next state. If next state doesn't require user intervention, keep on moving.
        boolean success = completeOneTransition(currentState, student, input);
        while (success) {
            currentState = (State) dialogContext.get(Ids.VAR_CURRENT_STATE);
            success = false;
            if (!currentState.isWaitForInput()) {
                success = completeOneTransition(currentState, student, input);
            }
        }

        //see if there is some response to the student.
        if (dialogContext.containsKey(Ids.VAR_DT_RESPONSE)) {
            resp = (DTResponse) dialogContext.get(Ids.VAR_DT_RESPONSE);
        }

        //do cleanup - such as, reset temporary variables. etc.
        cleanup();

        return resp;
    }

    private boolean completeOneTransition(State currentState, Student student, DTInput input) {

        System.out.println("CURRENT STATE: ================== " + currentState.getName() + " =================================      ");
        boolean success = false;
        List<Transition> possibleTransitions = dialogPolicy.getOutgoingTransitions(currentState);

        //if there are no outgoing transitions for a state s and s is not a final node
        if (!currentState.isEndState() && possibleTransitions.isEmpty()) {
            System.out.println("No actions are specified for this given state.");
            //TODO: return null??? How to handle error cases.
            return success;
        }
        Transition firedTransition = null;
        for (Transition t : possibleTransitions) {
            System.out.println("Evaluating conditions for: " + t.getFrom() + "\t" + t.getTo() + " +++++++++++++++++++++++++++++++++ ");
            boolean tranEval = ConditionEvaluator.evaluate(t, student, input, dialogContext);
            if (tranEval) {
                firedTransition = t;
                //TODO: assuming that at most one transition is satisfied. It reduces the text in console.
                break;
            }
        }

        if (firedTransition == null) {
            System.out.println("None of the transition satisfied. Check the script please. ");
            //TODO: ??
            return success;
        } else {
            //process actions
            //List<Action> exitStateActions = currentState.getExitActions();
            List<Action> transitionActions = firedTransition.getActions();

            // execute all the actions of that transition.
            STActionHandler.executeActions(transitionActions, student, input, dialogContext);

            currentState = dialogPolicy.getState(firedTransition.getTo());
            dialogContext.put(Ids.VAR_CURRENT_STATE, currentState);
            success = true;
        }
        return success;
    }

    private void cleanup() {
        TaskProgressInfo taskProgressInfo = (TaskProgressInfo) dialogContext.get(Ids.VAR_TASK_PROGRESS_INFO);
        taskProgressInfo.cleanup();

        //only remove if there is no leftover text.
        DTResponse resp = (DTResponse) dialogContext.get(Ids.VAR_DT_RESPONSE);
        if (resp != null && resp.getLeftOverText().size() <= 0) {
            dialogContext.remove(Ids.VAR_DT_RESPONSE);
        }
        //TODO: Destroy condition variables, so that they will not affect for the next round.
    }

    public void reset() {
        dialogContext.remove(Ids.VAR_DT_RESPONSE);
        dialogContext.put(Ids.VAR_CURRENT_STATE, dialogPolicy.getStartState());
        dialogContext.remove(Ids.VAR_TASK_PROGRESS_INFO);
    }
}
