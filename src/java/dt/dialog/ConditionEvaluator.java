package dt.dialog;

import dt.entities.database.Student;
import dt.entities.plain.DTInput;
import dt.persistent.xml.TaskManager;
import dt.task.DTHint;
import dt.task.TaskProgressInfo;
import java.util.HashMap;
import java.util.Random;

//import memphis.deeptutor.tools.TaskParser;
public class ConditionEvaluator {

    public static boolean evaluateTemp(Transition t, TaskManager task, String s) {
        boolean condition = false;
        for (Condition c : t.getConditions()) {
        }
        //For now return true or false randomly
        Random r = new Random();
        if (r.nextInt() % 2 == 0) {
            condition = true;
        } else {
            condition = false;
        }
        return condition;
    }

    public static boolean evaluate(Transition t, Student s, DTInput input, HashMap<String, Object> dialogContext) {
        boolean result = true;
        boolean condition;
        TaskProgressInfo taskProgressInfo = (TaskProgressInfo) dialogContext.get(Ids.VAR_TASK_PROGRESS_INFO);
        for (Condition c : t.getConditions()) {
            System.out.println("Cond Oper = " + c.getOper() + (c.isNegated() ? " Negated" : ""));
            condition = false;
            String op = c.getOper();
            if (op.equalsIgnoreCase(Ids.COND_HAS_MORE_TASKS)) {
                condition = hasMoreTasks();
            } else if (op.equalsIgnoreCase(Ids.COND_EXPECTATIONS_HIT)) {
                condition = taskProgressInfo.isExpectationsHit();
            } else if (op.equalsIgnoreCase(Ids.COND_ANY_UNCOVERED_EXP)) {
                condition = taskProgressInfo.isAnyUncoveredExpectation();
            } else if (op.equalsIgnoreCase(Ids.COND_CORRECT_ANSWER_TO_PUMP)) {
                condition = taskProgressInfo.isWorkingExpCovered();
            } else if (op.equalsIgnoreCase(Ids.COND_NEXT_HINT_IS_SEQ)) {
                condition = taskProgressInfo.isNextHintType(DTHint.HintType.SEQUENCE);
            } else if (op.equalsIgnoreCase(Ids.COND_NEXT_HINT_IS_COND)) {
                condition = taskProgressInfo.isNextHintType(DTHint.HintType.CONDITIONAL);
            } else if (op.equalsIgnoreCase(Ids.COND_NEXT_HINT_IS_FINAL)) {
                condition = taskProgressInfo.isNextHintType(DTHint.HintType.FINAL);
            } else if (op.equalsIgnoreCase(Ids.COND_HINT_ANSWER_CORRECT)) {
                condition = taskProgressInfo.getCurrentExpProgressInfo().isCorrectAnswerToTheHint();
            } else if (op.equalsIgnoreCase(Ids.COND_NO_MORE_HINTS)) {
                condition = taskProgressInfo.getCurrentExpProgressInfo().hasNoMoreHints();
            } else if (op.equalsIgnoreCase(Ids.COND_FIRST_UNCOVERED_EXP)) {
                condition = taskProgressInfo.workingOnFirstExpectation();
            } else if (op.equalsIgnoreCase(Ids.COND_MISCONCEPTION_HIT)) {
                condition = taskProgressInfo.isMisconceptionHit();
            }
            System.out.println("Condition Evaluation: " + (c.isNegated() ^ condition)); //XOR
            condition = c.isNegated() ^ condition;
            result = result && condition;
        }
        System.out.println("Transition from << " + t.getFrom() + " >> to << " + t.getTo() + (result ? " SATISFIED!!!!!!" : " DIDN't SATISFY "));
        return result;
    }

    public static boolean isTaskFinished(HashMap<String, Object> dialogContext) {
        TaskProgressInfo taskProgressInfo = (TaskProgressInfo) dialogContext.get(Ids.VAR_TASK_PROGRESS_INFO);
        if (taskProgressInfo != null) {
            return taskProgressInfo.isTaskFinished();
        }
        return false;
    }

    public static boolean hasMoreTasks() {
        //TODO: implement it.
        return true;
    }
}
