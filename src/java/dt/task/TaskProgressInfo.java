/* Keep the task progress information. The task object is just to read task but 
 * TaskProgressInfo can store some progress information. Some variables should be set
 * until the object remains but some information are temporary and applicable
 * for one utterance or just for a couple of utterances. this is tricky to handle.
 * Now, all variables are in this class.
 * 
 */
package dt.task;

import dt.persistent.xml.DTResponseOld;
import dt.persistent.xml.Expectation;
import dt.persistent.xml.Expectation.EXPECT_TYPE;
import dt.task.DTHint.HintType;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Rajendra
 */
public class TaskProgressInfo {

    /* TODO: 
     * Keep the task progress information. The task object is just to read task but 
     * TaskProgressInfo can store some progress information. Some variables should be set
     * until the object remains but some information are temporary and applicable
     * for one utterance or just for a couple of utterances. this is tricky to handle.
     * Now, all variables are in this class.
     */
    private ArrayList<ExpProgressInfo> expProgressInfoList;
    private HashMap<String, ExpProgressInfo> expPgrogressInfoMap;
    //TODO: Due to similar nature of expectation and misconception (though misconception has less content), we are
    //using ExpectionInfo for misconception also. its easier. 
    //Howerver, it may create confusion.. using DTExpectation class for Misconception as well.
    private ArrayList<ExpProgressInfo> misconceptionProgressInfoList;
    private HashMap<String, ExpProgressInfo> misconceptionProgressInfoMap;
    DTTask task;
    private String taskId;
    private ExpProgressInfo currentExpProgressInfo;
    /* TODO: reset following variables just before evaluating the student input */
    private boolean expectationRejected; //maybe due to forbidden words.    
    private boolean expectationsHit; //whether the current student response hit at least one expectaton? reset it each time we go for student answer evaluation.
    private String respDataForLogging; //TODO: seems to be logging information, review it, why I am putting it here. 
    DTResponseOld response = null;
    private boolean workingExpCovered;
    private boolean taskFinished;
    private boolean misconceptionHit;
    private boolean answerTooBrief;
    private boolean answerIrrelevant;
    private boolean alreadyAssessed;
    /* TODO:
     * set/append response in this object. If multiple actions are called how to manage it? 
     * it would be better if every action returns something and the response is merged somewhere else.
     * For now, response is set/appended in this object.
     */
    private DTResponseOld dtResponseOld;

    public void cleanup() {
        expectationRejected = false;
        expectationsHit = false;
        respDataForLogging = "";
        dtResponseOld = new DTResponseOld();
        workingExpCovered = false;
        misconceptionHit = false;
        answerTooBrief = false;
        answerIrrelevant = false;
        alreadyAssessed = false;
        if (currentExpProgressInfo != null) {
            currentExpProgressInfo.cleanup();
        }
    }

    public TaskProgressInfo(DTTask task) {
        expProgressInfoList = new ArrayList<ExpProgressInfo>();
        expPgrogressInfoMap = new HashMap<String, ExpProgressInfo>();

        misconceptionProgressInfoList = new ArrayList<ExpProgressInfo>();
        misconceptionProgressInfoMap = new HashMap<String, ExpProgressInfo>();

        this.task = task;

        dtResponseOld = new DTResponseOld();
        this.taskId = task.getId();
    }

    public ExpProgressInfo getExpectationProgressInfo(String id) {
        ExpProgressInfo expProgressinfo = null;
        if (expPgrogressInfoMap.containsKey(id)) {
            expProgressinfo = expPgrogressInfoMap.get(id);
        }
        return expProgressinfo;
    }

    public DTTask getTask() {
        return task;
    }

    public ExpProgressInfo getCurrentExpProgressInfo() {
        return currentExpProgressInfo;
    }

    public void setCurrentExpProgressInfo(ExpProgressInfo currentExpProgressInfo) {
        this.currentExpProgressInfo = currentExpProgressInfo;
    }

    public boolean isExpectationsHit() {
        return expectationsHit;
    }

    public void setExpectationsHit(boolean expectationsHit) {
        this.expectationsHit = expectationsHit;
    }

    public boolean isExpectationRejected() {
        return expectationRejected;
    }

    public void setExpectationRejected(boolean expectationRejected) {
        this.expectationRejected = expectationRejected;
    }

    public String getRespDataForLogging() {
        return respDataForLogging;
    }

    public void setRespDataForLogging(String responseData) {
        this.respDataForLogging = responseData;
    }

    public void appRespDataForLogging(String text) {
        this.respDataForLogging += text;
    }

    public boolean isWorkingExpCovered() {
        return workingExpCovered;
    }

    public void setWorkingExpCovered(boolean workingExpCovered) {
        this.workingExpCovered = workingExpCovered;
    }

    public boolean isTaskFinished() {
        return taskFinished;
    }

    public void setTaskFinished(boolean taskFinished) {
        this.taskFinished = taskFinished;
    }

    public boolean isAnswerIrrelevant() {
        return answerIrrelevant;
    }

    public void setAnswerIrrelevant(boolean answerIrrelevant) {
        this.answerIrrelevant = answerIrrelevant;
    }

    /**
     * get the list of uncovered expectations from the given valid (i.e. candidate expectations, which passed the
     * minimum requiredment etc.)
     *
     * @param expectations
     * @return
     */
    public ArrayList<DTExpectation> getPreviouslyUncoveredExpectations(ArrayList<DTExpectation> expectations) {
        ArrayList<DTExpectation> uncoveredExps = new ArrayList<DTExpectation>();
        for (int i = 0; i < expectations.size(); i++) {
            if (!isExpectationCovered(expectations.get(i).getId())) {
                uncoveredExps.add(expectations.get(i));
            }
        }
        return uncoveredExps;
    }

    public String getTaskId() {
        return taskId;
    }

    /**
     * is the expectation already covered?
     *
     * @param expId
     * @return
     */
    boolean isExpectationCovered(String expId) {
        ExpProgressInfo expProgressInfo = getExpectationProgressInfo(expId);
        return expProgressInfo.isCoverd();
    }

    public void setExpectationCovered(String expId) {
        ExpProgressInfo expProgressInfo = getExpectationProgressInfo(expId);
        expProgressInfo.setCoverd(true);
    }

    public boolean isAnyUncoveredExpectation() {
        //TODO: 
        for (ExpProgressInfo expProgressInfo : this.expProgressInfoList) {
            if (!expProgressInfo.isCoverd()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return next uncovered expectation
     *
     * @return
     */
    public ExpProgressInfo getNextUncoveredExpectaion() {
        ExpProgressInfo result = null;

        for (ExpProgressInfo exp : this.expProgressInfoList) {
            if (!exp.isCoverd()) {
                result = exp;
            }
        }
        return result;
    }

    /**
     * Add pump text in the response.
     */
    public void appendPumpText() {
        this.response.addResponseText(this.currentExpProgressInfo.getExpectation().getPump());
    }

    /**
     * Get Hint text in the response.
     */
    public String getNextHintText(HintType type) {
        //TODO: get the next hing of given type
        //this.response.addResponseText(this.currentExpProgressInfo.getExpectation().getPump()); 
        return null;
    }

    /**
     * Check if the next hint is of given type (conditional, sequence, final). TODO: move it to ExpProgressInfo class.
     *
     * @param hintType
     * @return
     */
    public boolean isNextHintType(HintType hintType) {
        //if there is no current expectation in progress.
        if (this.currentExpProgressInfo == null) {
            return false;
        }
        DTHint nextHint = currentExpProgressInfo.getNextHint();
        if (nextHint == null) {
            return false;
        }
        if (nextHint.getHintType() == hintType) {
            return true;
        }
        return false;
    }

    /**
     * TODO: move it to the ExpProgressInfo class. Get next hint.
     *
     * @return
     */
    public DTHint getNextHint() {
        //if there is no current expectation in progress.
        if (this.currentExpProgressInfo == null) {
            System.out.println("The working expectation is null????");
            return null;
        }
        DTHint nextHint = currentExpProgressInfo.getNextHint();
        if (nextHint == null) {
            System.out.println("Next hint, Null??");
            return null;
        }
        this.currentExpProgressInfo.setCurrentHintIndex(currentExpProgressInfo.getCurrentHintIndex() + 1);
        return nextHint;
    }

    public DTResponseOld getDTResponseOld() {
        return dtResponseOld;
    }

    public void setDTResponseOld(DTResponseOld DTResponseOld) {
        this.dtResponseOld = DTResponseOld;
    }

    public ArrayList<ExpProgressInfo> getExpProgressInfoList() {
        return expProgressInfoList;
    }

    public void setExpProgressInfoList(ArrayList<ExpProgressInfo> expProgressInfoList) {
        this.expProgressInfoList = expProgressInfoList;
        for (ExpProgressInfo expInfo : expProgressInfoList) {
            //ExpProgressInfo expProgressInfo = new ExpProgressInfo(exp);
            expPgrogressInfoMap.put(expInfo.getId(), expInfo);
        }
    }

    public void summarizeExpectations() {
        //convert to DTResponse format.
        dtResponseOld.addResponseText("Okay. Let's summarize the correct answer to this problem.#WAIT#");

        //TODO: is type set? how? string? optional, short etc.

        String expSummary = "";
        for (ExpProgressInfo epi : expProgressInfoList) {
            if (epi.getExpectation().getType() != EXPECT_TYPE.OPTIONAL && epi.getExpectation().getType() != EXPECT_TYPE.SHORT) {
                expSummary = expSummary + " " + epi.getExpectation().getAssertion() + "#WAIT#";
            }
        }

        for (ExpProgressInfo epi : expProgressInfoList) {
            if (epi.getExpectation().getType() == EXPECT_TYPE.SHORT) {
                expSummary = expSummary + " " + epi.getExpectation().getAssertion() + "#WAIT#";
            }
        }
        dtResponseOld.addResponseText(expSummary);
        dtResponseOld.addResponseText("Let's move now to the next task.");
        dtResponseOld.addResponseText("#WAIT#");
    }

    public boolean workingOnFirstExpectation() {
        int uncoveredExpCount = 0;
        for (ExpProgressInfo expProgressInfo : expProgressInfoList) {
            if (!expProgressInfo.isCoverd()) {
                uncoveredExpCount++;
            }
        }
        if (uncoveredExpCount == expProgressInfoList.size()) {
            return true;
        }
        return false;
    }

    /**
     * ******========For misconception ======================== **
     */
    public boolean isMisconceptionHit() {
        return misconceptionHit;
    }

    public void setMisconceptionHit(boolean misconceptionHit) {
        this.misconceptionHit = misconceptionHit;
    }

    boolean isMisconceptionCovered(String miscId) {
        ExpProgressInfo miscProgressInfo = getMisconceptionProgressInfo(miscId);
        return miscProgressInfo.isCoverd();
    }

    public void setMisconceptionCovered(String miscId) {
        ExpProgressInfo miscProgressInfo = getMisconceptionProgressInfo(miscId);
        miscProgressInfo.setCoverd(true);
    }

    public boolean isAnyUncoveredMisconception() {
        for (ExpProgressInfo miscInfo : this.misconceptionProgressInfoList) {
            if (!miscInfo.isCoverd()) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<DTExpectation> getPreviouslyUncoveredMisconceptions(ArrayList<DTExpectation> misconceptions) {
        ArrayList<DTExpectation> uncoveredMisconceptions = new ArrayList<DTExpectation>();
        for (int i = 0; i < misconceptions.size(); i++) {
            if (!isMisconceptionCovered(misconceptions.get(i).getId())) {
                uncoveredMisconceptions.add(misconceptions.get(i));
            }
        }
        return uncoveredMisconceptions;
    }

    public ExpProgressInfo getMisconceptionProgressInfo(String id) {
        ExpProgressInfo expProgressinfo = null;
        if (misconceptionProgressInfoMap.containsKey(id)) {
            expProgressinfo = misconceptionProgressInfoMap.get(id);
        }
        return expProgressinfo;
    }

    public boolean isAnswerTooBrief() {
        return answerTooBrief;
    }

    public void setAnswerTooBrief(boolean answerTooBrief) {
        this.answerTooBrief = answerTooBrief;
    }

    public boolean isAlreadyAssessed() {
        return alreadyAssessed;
    }

    public void setAlreadyAssessed(boolean alreadyAssessed) {
        this.alreadyAssessed = alreadyAssessed;
    }

    public void setMisconceptionsInfoList(ArrayList<ExpProgressInfo> miscsProgressInfoList) {
        this.misconceptionProgressInfoList = miscsProgressInfoList;
        for (ExpProgressInfo miscInfo : misconceptionProgressInfoList) {
            misconceptionProgressInfoMap.put(miscInfo.getId(), miscInfo);
        }
    }
}
