/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.dialog;

/**
 *
 * @author Rajendra
 */
public class Ids {

    /* variable names */
    public static String VAR_CURRENT_STATE = "currentState";
    public static String VAR_DT_RESPONSE = "dtResponse";
    public static String VAR_STDIALOG_MANAGER = "stDialogueManager";
    public static String VAR_CURRENT_TASK = "currentTask";
    public static String VAR_TASK_PROGRESS_INFO = "taskProgressInfo";
    public static String VAR_TASK_FINISHED = "taskFinished";
    
    /* conditions */
    public static String COND_HAS_MORE_TASKS = "hasMoreTasks";
    public static String COND_EXPECTATIONS_HIT = "expectationsHit";
    public static String COND_ANY_UNCOVERED_EXP = "isAnyUncoveredExpectation";
    public static String COND_ANSWER_TOO_SHORT = "answerTooShort";
    public static String COND_ANSWER_IRRELEVANT = "answerIrrelevant";
    public static String COND_CORRECT_ANSWER_TO_PUMP = "correctAnswerToThePump";
    public static String COND_NEXT_HINT_IS_SEQ = "nextHintIsSequenceHint";
    public static String COND_NEXT_HINT_IS_COND = "nextHintIsConditionalHint";
    public static String COND_NEXT_HINT_IS_FINAL = "nextHintIsFinalHint";
    public static String COND_HINT_ANSWER_CORRECT = "correctAnswerToTheHint";
    public static String COND_NO_MORE_HINTS = "noMoreHints";
    public static String COND_FIRST_UNCOVERED_EXP = "firstUncoveredExpectation";
    public static String COND_MISCONCEPTION_HIT = "misconceptionHit";
    
    
    /* action names */
    public static String ACTION_LOAD_TASK = "loadTask";
    public static String ACTION_CHECK_ANSWER_COVERS_EXPS = "checkAnswerCoversExpectations";
    public static String ACTION_GET_FEEDBACK = "getFeedback";
    public static String ACTION_GET_NEXT_UNCOVERED_EXP = "getNextUncoveredExpectation";
    public static String ACTION_SHOW_PUMP = "showPump";
    public static String ACTION_GIVE_HINT = "giveHint";
    public static String ACTION_SHOW_PROMPT = "showPrompt";
    public static String ACTION_CREATE_TASK_EXP_SUMMARY = "createTaskExpSummary";
    public static String ACTION_MARK_CURRENT_EXP_COVERED = "markCurrentExpAsCovered";
}
