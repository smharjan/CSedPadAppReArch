/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.entities.database;

import dt.config.ConfigManager;
import dt.log.DTLogger;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 *
 * @author nobal
 */
@Entity
@Table(name = "Student")
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;
    private String studentId;
    private String password;
    boolean hasAcceptedTermsAndConditions;
    boolean isSpecialStudent;
    String dialogPolicyFile;
    boolean useStateTransitionDialogManager;
    String dtState;
    String preTest;
    String postTest;
    String dtMode1;
    String dtMode2;
    String dtMode3;
    private List<Evaluation> evaluations;
    Demographics demography;
    String assignedTasks; //should be comma separated task ids or so
    String finishedTasks; //should have comma separated task ids or so.
    //TRANSIENT variables (not to be saved in database).
    //There should be separate field, String (with comma separated list of tasks) 
    //for hibernate, and at the first time (just after login), need to construct
    //array lists spliting the remaining/assigned tasks etc.
    ArrayList<String> unfinishedTaskIds;
    String currentTaskId;
    //contextData: It's like session.. can be cleared at the middle, load contextual
    //data. As student object is going to be in the session, it will also be.
    Map<String, Object> contextData = new HashMap<String, Object>();
    // Since each student has it's own html log file, it will be easy if a logger is 
    // set to student, initialize the logger object only once and use whenever needed.
    // otherwise, need to initialize logger everywhere.., The problem was that, once created
    // and logged some message (upto this stage, it keeps in the memory), user had to
    // make sure that the html is saved before returning from that function (sometime missed because
    // program returned before saving to html). Now, it can be done just before returning from the action.
    // Initialize in setup method, called when student is logged in.
    DTLogger logger = null;
    //Has finished all tasks?
    boolean finishedAllTasks = false;

    private String currentUserClientType;

    @Transient
    public boolean isFinishedAllTasks() {
        return finishedAllTasks;
    }

    public void setFinishedAllTasks(boolean finishedAllTasks) {
        this.finishedAllTasks = finishedAllTasks;
    }

    @Transient
    public DTLogger getLogger() {
        return logger; /* return mutable object */

    }

    public String getAssignedTasks() {
        return assignedTasks;
    }

    @Transient
    public String getNextTaskId() {
        if (unfinishedTaskIds.isEmpty()) {
            this.setCurrentTaskId(null);
            return null;
        }
        String taskId = this.unfinishedTaskIds.remove(0);
        this.setCurrentTaskId(taskId);
        return taskId;
    }

    public void addFinishedTaskId(String taskId) {
        if (this.finishedTasks == null || "".equals(this.finishedTasks)) {
            this.finishedTasks = taskId;
        } else {
            this.finishedTasks += "," + taskId;
        }
        this.currentTaskId = null;
    }

    public void addUnFinishedTaskId(String taskId) {
        this.unfinishedTaskIds.add(taskId);
    }

    public void setAssignedTasks(String assignedTasks) {
        this.assignedTasks = assignedTasks;
    }

    public String getFinishedTasks() {
        return finishedTasks;
    }

    public void setFinishedTasks(String finishedTasks) {
        this.finishedTasks = finishedTasks;
    }

    public void addInContextData(String key, Object value) {
        this.contextData.put(key, value);
    }

    public Object getFromContextData(String key) {
        return this.contextData.get(key);
    }

    public void removeFromContextData(String key) {
        this.contextData.remove(key);
    }

    public void clearAllContextData() {
        this.contextData.clear();
    }

    @Transient
    public boolean isUseStateTransitionDialogManager() {
        return useStateTransitionDialogManager;
    }

    @Transient
    public String getCurrentUserClientType() {
        return currentUserClientType;
    }

    public void setCurrentUserClientType(String currentUserClientType) {
        this.currentUserClientType = currentUserClientType;
    }

    public void setUseStateTransitionDialogManager(boolean useStateTransitionDialogManager) {
        this.useStateTransitionDialogManager = useStateTransitionDialogManager;
    }

    /*
     * Its like constructor. Since this object is created using hibernate, we don't
     * know much about how constructor is executed from hibernate. So,
     * when object is loaded from the database, needs to process someting. 
     * Such as, split the comma separated list of tasks and put them in the arraylist
     * etc.
     */
    public boolean setup() {
        // if regular user (i.e. NOT special user, load assigned/unfinished tasks from database
        //student is special student (i.e. demo user), assign demo taks, whatever available in the
        // Tasks/demo folder.
        if (!setupUnFinishedTasks()) {
            return false;
        }
        //TODO: make it configurable or put it somewhere in database.
        //use State Transition based dialog manager (i.e. policy specified externally) or use the traditional dialog manager.
        this.useStateTransitionDialogManager = false;

        //Initialize logger...
        logger = new DTLogger(this.getStudentId());
        logger.log(DTLogger.Actor.SYSTEM, DTLogger.Level.ONE, "Logged in: ");
        logger.saveLogInHTML();
        return true;
    }

    /**
     * 
     * @return 
     */
    public boolean setupUnFinishedTasks() {
        // if regular user (i.e. NOT special user, load assigned/unfinished tasks from database
        if (!isIsSpecialStudent()) {
            setupUnfinishedTasksReadFromDB();
        } else {
            //student is special student (i.e. demo user), assign demo taks, whatever available in the
            // Tasks/demo folder.
            if (!assignDemoTaskIds()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Setup unfinished tasks. For regular student. NOT for the demo user. For
     * demo user, read them from the demo folder.
     */
    private boolean setupUnfinishedTasksReadFromDB() {
        String[] assignedIds = this.getAssignedTasks().split(",");
        this.unfinishedTaskIds = new ArrayList<String>();
        ArrayList<String> finishedIds = new ArrayList<String>();
        if (this.getFinishedTasks() != null && !"".equals(this.getFinishedTasks())) {
            String[] ids = this.getFinishedTasks().split(",");
            finishedIds.addAll(Arrays.asList(ids));
        }
        for (String assignedId : assignedIds) {
            if (!finishedIds.contains(assignedId)) {
                this.addUnFinishedTaskId(assignedId);
            }
        }
        return true;
    }

    /**
     * Set demo task IDs for the special user (i.e. for the demo user). Read
     * files from the demo directory and add the IDs as unassigned (exclude the
     * LP99_PR99).
     */
    public boolean assignDemoTaskIds() {
        this.unfinishedTaskIds = new ArrayList<String>();
        String demoFolder = ConfigManager.getDemoTaskFolder();
        String taskId;
        File folder = new File(demoFolder);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                taskId = listOfFiles[i].getName();
                //now replacing all occurences of .xml, hopefully there is only one, which is the
                // file extension.
                taskId = taskId.replaceAll(".xml", "");
                //exclude LP99_
                if (!taskId.equalsIgnoreCase("LP99_PR99")) {
                    this.unfinishedTaskIds.add(taskId);
                }
            }
        }
        return true;
    }

    /**
     * Return the task to task pool, useful when reset session. Just return to
     * task pool, and then get it again as it is being loaded first time. Put it
     * at the beginning.
     *
     * @param currentTaskId
     */
    public void returnToTaskPool(String currentTaskId) {
        //just to make sure that there is no duplication.
        if (this.unfinishedTaskIds.contains(currentTaskId)) {
            this.unfinishedTaskIds.remove(currentTaskId);
        }
        //put it at the beginning.
        this.unfinishedTaskIds.add(0, currentTaskId);
    }

    @Transient
    public ArrayList<String> getUnfinishedTaskIds() {
        return unfinishedTaskIds;
    }

    public void setUnfinishedTaskIds(ArrayList<String> unfinishedTaskIds) {
        this.unfinishedTaskIds = unfinishedTaskIds;
    }

    @Transient
    public String getCurrentTaskId() {
        return currentTaskId;
    }

    public void setCurrentTaskId(String currentTaskId) {
        this.currentTaskId = currentTaskId;
    }

    @Id
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    @OneToMany(targetEntity = Evaluation.class, mappedBy = "student", cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    public List<Evaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(List<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }

    /*
     * Given evaluation id (such as, pretest, posttest), returns the list of Evaluations.
     * @author Rajendra 1/25/2013
     */
    public List<Evaluation> getEvaluationsByEvaluationId(String evaluationId) {
        List<Evaluation> evalList = new ArrayList<Evaluation>();
        for (Evaluation eval : this.evaluations) {
            if (eval.getEvaluationId().equalsIgnoreCase(evaluationId)) {
                evalList.add(eval);
            }
        }
        return evalList;
    }

    /*
     * Given evaluation id (such as, pretest, posttest), returns the highest context id.
     * @author Rajendra 1/25/2013
     */
    public int getHighestContextId(String evaluationId) {
        int highest = -1;
        for (Evaluation eval : this.evaluations) {
            if (!eval.getEvaluationId().equalsIgnoreCase(evaluationId)) {
                continue;
            }
            int contextId = Integer.valueOf(eval.getContextId());
            if (contextId > highest) {
                highest = contextId;
            }
        }
        return highest;
    }

    /**
     * Get Student Answers for the given evaluation id
     *
     * @return
     */
    public Map<String, String> getStudentAnswers(String evaluationId) {
        Map<String, String> map = new HashMap<String, String>();
        for (Evaluation eval : this.evaluations) {
            if (!eval.getEvaluationId().equalsIgnoreCase(evaluationId)) {
                continue;
            }
            map.put(eval.getQuestionId(), eval.getAnswer());
        }
        return map;
    }

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    public Demographics getDemography() {
        return demography;
    }

    public void setDemography(Demographics demography) {
        this.demography = demography;
    }

    public String getDtMode1() {
        return dtMode1;
    }

    public void setDtMode1(String dtMode1) {
        this.dtMode1 = dtMode1;
    }

    public String getDtMode2() {
        return dtMode2;
    }

    public void setDtMode2(String dtMode2) {
        this.dtMode2 = dtMode2;
    }

    public String getDtMode3() {
        return dtMode3;
    }

    public void setDtMode3(String dtMode3) {
        this.dtMode3 = dtMode3;
    }

    public void addEvaluation(Evaluation e) {
        if (this.evaluations == null) {
            this.evaluations = new ArrayList<Evaluation>();
        }
        this.evaluations.add(e);
        e.setStudent(this);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isHasAcceptedTermsAndConditions() {
        return hasAcceptedTermsAndConditions;
    }

    public void setHasAcceptedTermsAndConditions(boolean hasAcceptedTermsAndConditions) {
        this.hasAcceptedTermsAndConditions = hasAcceptedTermsAndConditions;
    }

    public boolean isIsSpecialStudent() {
        return isSpecialStudent;
    }

    public void setIsSpecialStudent(boolean isSpecialStudent) {
        this.isSpecialStudent = isSpecialStudent;
    }

    public String getDtState() {
        return dtState;
    }

    public void setDtState(String dtState) {
        this.dtState = dtState;
    }

    public String getPreTest() {
        return preTest;
    }

    public void setPreTest(String preTest) {
        this.preTest = preTest;
    }

    public String getPostTest() {
        return postTest;
    }

    public void setPostTest(String postTest) {
        this.postTest = postTest;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (studentId != null ? studentId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Student)) {
            return false;
        }
        Student other = (Student) object;
        if ((this.studentId == null && other.studentId != null) || (this.studentId != null && !this.studentId.equals(other.studentId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "uom.entities.Users[ id=" + this.studentId + " ]";
    }

}
