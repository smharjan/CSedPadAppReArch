package dt.dialog;

import dt.persistent.xml.ExpectAnswer;
import dt.persistent.xml.Expectation;
import dt.persistent.xml.Task;
import dt.persistent.xml.TaskManager;
import dt.task.DTExpectation;
import dt.task.DTHint;
import dt.task.DTHint.HintType;
import dt.task.DTPrompt;
import dt.task.DTRequired;
import dt.task.DTTask;
import dt.task.ExpProgressInfo;
import dt.task.TaskProgressInfo;
import java.util.ArrayList;

/**
 *
 * @author Rajendra Created on Jun 20, 2014, 4:10:43 PM
 */
public class TaskFormatConverter {

    public static TaskProgressInfo convert(TaskManager tm) {
        TaskProgressInfo taskProgress = null;
        DTTask dtTask = new DTTask();
        Task task = tm.LoadTask();
        dtTask.setId(task.getTaskID());

        System.out.println("********************************Loading task:   " + dtTask.getId() + "********************************");

        dtTask.setText(task.getProblemText1());
        dtTask.setText2(task.getProblemText2());
        Expectation[] expectations = task.getExpectations();

        ArrayList<ExpProgressInfo> expInfoList = new ArrayList<ExpProgressInfo>();
        ArrayList<DTExpectation> dtExpectations = new ArrayList<DTExpectation>();

        int expCounter = 0;
        for (Expectation e : expectations) {
            System.out.println("exp id: " + e.id);
            DTExpectation exp = new DTExpectation();
            exp.setPump(e.getPump());
            exp.setOrder(e.getOrder());
            exp.setId(e.id);
            exp.setAssertion(e.getAssertion());
            exp.setType(e.getType());
            ArrayList<String> variants = new ArrayList<String>();
            for (String v : e.getVariants()) {
                System.out.println("Variant: " + v);
                if (v.contains("#")) {  //referring to LP99 tak
                    if (expCounter % 3 == 0) {
                        v = "the meteor will move in the direction of the rocket with constant acceleration";
                    } else if (expCounter % 3 == 1) {
                        v = "The newtons second law says that force equals mass times acceleration.";
                    } else if (expCounter % 3 == 2) {
                        v = "Newtons second law is relevant.";
                    }
                }
                variants.add(v);
            }

            //copy hints.
            ArrayList<DTHint> hints = new ArrayList<DTHint>();
            if (e.hintsAnswer != null) {
                for (int j = 0; j < e.hintsAnswer.length; j++) {
                    DTHint hint = new DTHint();
                    if (e.hintsType[j].equalsIgnoreCase("sequence")) {
                        hint.setHintType(HintType.SEQUENCE);
                    } else if (e.hintsType[j].equalsIgnoreCase("conditional")) {
                        hint.setHintType(HintType.CONDITIONAL);
                    }
                    if (e.hintsType[j].equalsIgnoreCase("final")) {
                        hint.setHintType(HintType.FINAL);
                    }
                    hint.setText(e.hints[j]);
                    hint.setNegative(e.hintsCorrection[j]);

                    if (e.hintsAnswer[j] != null) {
                        hint.setExpectedWords(e.hintsAnswer[j].acceptedAnswer);
                    } else {
                        hint.setExpectedWords(null);
                    }
                    //Hint, required key words??
                    hints.add(hint);
                }
            }
            exp.setHints(hints);

            //set prompt.
            DTPrompt prompt = null;
            if (e.prompt != null) {
                prompt = new DTPrompt();
                prompt.setText(e.prompt);
                prompt.setExpectedWords(e.promptAnswer.getAcceptedAnswer());
                prompt.setNegative(e.promptCorrection);
            }
            exp.setPrompt(prompt);

            //
            expCounter++;

            exp.setVariants(variants);
            DTRequired req = new DTRequired();
            ExpectAnswer expAnswer = e.required;
            req.setAcceptedAnswer(expAnswer == null ? null : expAnswer.acceptedAnswer);
            exp.setRequired(req);
            
            //forbidden?
            exp.setForbidden(e.forbidden);

            ExpProgressInfo epInfo = new ExpProgressInfo(exp);

            dtExpectations.add(exp);
            expInfoList.add(epInfo);
        }


        // copy misconceptions too.
        ArrayList<DTExpectation> miscs = new ArrayList<DTExpectation>();
        ArrayList<ExpProgressInfo> miscsProgressInfoList = new ArrayList<ExpProgressInfo>();
        if (task.getMisconceptions() != null) {

            for (Expectation misc : task.getMisconceptions()) {
                System.out.println("Misconception ID: " + misc.id);
                DTExpectation misconception = new DTExpectation();
                misconception.setId(misc.id);
                misconception.setAssertion(misc.getAssertion());

                ArrayList<String> variants = new ArrayList<String>();
                for (String v : misc.getVariants()) {
                    System.out.println("Misconception, variant for ID " + misc.getId() + ": " + v);
                    variants.add(v);
                }
                System.out.println("correction:" + misconception.getAssertion());
                misconception.setVariants(variants);
                miscs.add(misconception);
                ExpProgressInfo miscInfo = new ExpProgressInfo(misconception);
                miscsProgressInfoList.add(miscInfo);
            }
        }

        taskProgress = new TaskProgressInfo(dtTask);
        taskProgress.setExpProgressInfoList(expInfoList);
        dtTask.setExpectations(dtExpectations);
        dtTask.setRelevantText(tm.getRelevantText());

        //misc
        dtTask.setMisconceptions(miscs);
        taskProgress.setMisconceptionsInfoList(miscsProgressInfoList);

        return taskProgress;
    }

    public static void main(String[] args) {
        TaskFormatConverter converter;
    }
}
