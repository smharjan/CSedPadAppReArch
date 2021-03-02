/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.actions.pretest;

import com.opensymphony.xwork2.ActionSupport;
import dt.config.ConfigManager;
import dt.constants.Result;
import dt.entities.database.Evaluation;
import noNamespace.ContextDocument;
import noNamespace.ContextDocument.Context;
import java.io.IOException;
import java.util.Map;
import dt.entities.database.Student;
import dt.parser.xml.FciXmlParser;
import dt.persistent.DataManager;
import org.apache.struts2.interceptor.SessionAware;

/**
 *
 * @author Rajendra
 */
public class PreTestAction extends ActionSupport implements SessionAware {

    private String questionAnswers;

    public String getQuestionAnswers() {
        return questionAnswers;
    }

    public void setQuestionAnswers(String questionAnswers) {
        this.questionAnswers = questionAnswers;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    private String explanation = null;
    private Map<String, Object> session;

    /*
     * If invoked from the pretest page, the question Answer text must have something.
     */
    public boolean isInvokedFromItself() {
        if (questionAnswers != null) {
            return true;
        }
        return false;
    }

    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public PreTestAction() {
    }

    /**
     * Possible Cases: A: No need to show pretest to the currently logged in
     * user (developer, guest etc)
     *
     * B: action is invoked from other action/page: 1. Student has not finished
     * any questions yet (new) 2. Student has finished some questions, and need
     * to load the next context.
     *
     * C: action is invoked from the page itself 1. Student has finished some
     * questions, and need to load the next context. 2. Student has just
     * finished the questions, in the last submit (it's time to redirect to
     * another page)
     *
     * @return
     * @throws Exception
     */
    @Override
    public String execute() throws Exception {

        System.out.println("[ENTERING PRETEST ACTION]");
        Student s = (Student) session.get("student");

        //don't need to show pretest to the special user, shouldn't come here
        //if incase, double check.
        if (s.isIsSpecialStudent()) {
            return Result.FINISHEDALL;
        }

        String result = run(s);
        System.out.println("[RETURNING FROM PRETEST ACTION]");
        return result;
    }

    /**
     * Do the pretest...
     */
    private String run(Student s) throws Exception {
        boolean hasFinishedAll;
        //is this action invoked from the pretest page itself? or it is here
        // because some other page redirected to this action?
        if (isInvokedFromItself()) {
            handleRespose(s);
            hasFinishedAll = loadFci(s);
            if (hasFinishedAll) {
                doPostProcess(s);
                //finished, update session 
                session.put("student", s);
                return Result.FINISHEDALL;
            }
            // continue same page (off course with different questions)
            session.put("student", s);
            return Result.CONTINUE;
        } else {
            hasFinishedAll = loadFci(s);
            // if already finished (i.e. in the previous session), just don't need
            // to do anything.
            if (hasFinishedAll) {
                return Result.FINISHEDALL;
            }
            session.put("student", s);
            return Result.CONTINUE;
        }
    }

    /**
     * Handle the student response
     *
     * @param s
     * @return true - has finished all questions, false - otherwise
     */
    private boolean handleRespose(Student s) {
        String studentId = s.getStudentId();
        if (questionAnswers != null) {
            System.out.println(questionAnswers);
            String qAPairs[] = questionAnswers.split(":");  // pipe delimeted q/a pair.., patchy solution for now. 
            String contextId = session.get("nextFcicontextId").toString();
            for (String qAPair : qAPairs) {
                qAPair = qAPair.trim();
                if (qAPair == null || qAPair.length() == 0) {
                    continue;
                }

                String qId = qAPair.trim().split(" ")[0];  //question number, string??
                qId = qId.replace("a", ""); // had to add 'a' for the question id as one weird issue found in javascript.
                String answer = qAPair.trim().split(" ")[1];  // Answer..
                //build evaluation object.. and save to student.
                Evaluation eval = new Evaluation();
                eval.setContextId(contextId);
                eval.setEvaluationId("pretest");
                eval.setExplanation(this.explanation);
                eval.setQuestionId(qId);
                eval.setAnswer(answer);
                s.addEvaluation(eval);
                String textToLog = "pretest" + "--" + qId + "--" + answer + "--" + studentId + "--" + contextId + "--" + this.explanation;
                System.out.println(textToLog);
                //DTLogger.logThisInfo(s, textToLog);
            }
            DataManager.updateStudent(s);
        }
        return true;
    }

    /*
     * Load FCI questions..
     * 
     */
    private boolean loadFci(Student s) throws IOException, Exception {
        // 1. Check if any FCI context are loaded. If not, load them now
        int nextContextId = -1;
        String nextCid = null;
        try {
            nextCid = session.get("nextFcicontextId").toString();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        Map<Integer, ContextDocument.Context> contextMap = null;

        if (nextCid != null) {
            // what context id is now in the session has been finished.
            nextContextId = Integer.parseInt(nextCid) + 1;
            contextMap = (Map<Integer, Context>) session.get("contextMap");
        } else {
            String fciPretestFileName = ConfigManager.getFciFileName(s.getPreTest());
            nextContextId = s.getHighestContextId("pretest");
            nextContextId = nextContextId == -1 ? 1 : nextContextId + 1;
            contextMap = FciXmlParser.getContexts(fciPretestFileName);
            session.put("contextMap", contextMap);
            session.put("nextFcicontextId", nextContextId);
        }

        // if finished, return true - means, all questions finished.
        if (nextContextId > contextMap.size()) {
            return true;
        } else {
            // 3. If already loaded, load the next context now
            Context toBeLoaded = contextMap.get(nextContextId);
            session.put("context", toBeLoaded);
            session.put("nextFcicontextId", nextContextId);
            if (toBeLoaded.getContextdescription().trim().length() > 1) {
                String contextDescription = toBeLoaded.getContextdescription()
                        .replaceAll("\\n", "<br/>");
                session.put("contextDesc", contextDescription);
                session.put("contextPics",
                        toBeLoaded.getContextpicture());
            } else {
                session.put("contextDesc", null);
                session.put("contextPics", null);
            }
        }
        return false;
    }

    /**
     * when test is finished, do it only one time.
     */
    private boolean doPostProcess(Student s) {
        // rajendra: 
        // TODO: port LP model and add it here.. the xml handling, and LP model both
        // needs to be reviewed.. and re-designed where needed.
        // For now, the LP is not recorded in log file (however, we can calculate it later).
        //LPModel lp = new LPModel();
        //lp.LogStudentLP(s.getGivenId());

        calculateAndLogPercentCorrect(s);

        session.remove("nextFcicontextId");
        session.remove("context");
        session.remove("contextMap");
        return true;
    }

    /* Once the pretest is complete, log the percent correct answers for quick observation */
    private void calculateAndLogPercentCorrect(Student s) {
        // Before moving to the dialogue, calculate the percentage correct answer and log.
        String fciFile = ConfigManager.getFciFileName(s.getPreTest());
        Map<String, String> expectedAnswers = FciXmlParser.getFCIAnswers(fciFile);
        Map<String, String> studentAnswers = s.getStudentAnswers("pretest");
        int correctCount = 0;
        String expected = "";
        String actual = "";
        for (String qid : studentAnswers.keySet()) {
            actual = studentAnswers.get(qid);
            expected = expectedAnswers.get(qid);
            if (actual.equalsIgnoreCase(expected)) {
                correctCount++;
            }
        }
        //double pctCorrect = studentAnswers.isEmpty() ? 0.0 : 100.0 * correctCount / studentAnswers.size();
        //NEWDTLoggerBackup.logThisInfo(s, " CORRECTLY ANSWERED: " + correctCount + "/" + studentAnswers.size() + "(=" + String.format("%2.2f", pctCorrect) + "%)");
        //NEWDTLoggerBackup.logThisInfo(s, "FCI QUESTION FILE USED:  " + (new File(fciFile)).getName()); // just get the file name.
    }
}
