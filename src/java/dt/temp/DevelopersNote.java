package dt.temp;

/**
 *
 * @author Rajendra Created on Jul 5, 2014, 8:23:36 PM
 */
public class DevelopersNote {
    /*
     * 
     * July 05, 2014. DeepTutor State transition based dialog manager. Rajendra Banjade.
     * Code Available at: http://dtdev.memphis.edu:8181/svn/deep_tutor/DeepTutorAppReArch
     http://deeptutor2.memphis.edu/DeepTutorAppReArch/

     Release Notes:

     http://deeptutor2.memphis.edu/DeepTutorAppReArch/
     (Source code available at http://dtdev.memphis.edu:8181/svn/deep_tutor/DeepTutorAppReArch)

     For testing: user1 through user5 are created and the password is welcome for everyone. 
     Please go through the notes below (also available in DevelopersNote.java in dt.temp package). The dialog policy file currently used is attached.

     Please let me know if you find logical issues.  

     -Rajendra

     Release Notes:

     1. The dialogue manager can be switched (toggled) with \\cdm command.
     2. The new attribute called waitForInput has been added in State. If it is true, the DM waits until it gets some input from the user. For example, at Pump, it expects some input from the user. There are some states such as ExpectationStart, ExpectationEnd, where the user input is not needed to move to another state. For example, the expectation starts with a Pump or a Hint but the ExpectationStart state has been added so that it adds some clarity (gives logical view) and some actions (getNextExpectation etc.) take place without user input.

     3. The newly created DTTask class has been used but needs some annotation and fixes to load task file directly as it's object. For now, the existing code loads the tasks and it is converted to DTTask object. The reference does not work so the referenced texts (i.e. referenced to LP99_PR99) are copied to the task files itself. Only two files are there for demo. LP02_PR00, and LP02_PR01.

     4. Whenever there is confusion or requires some review/fix, comments are put in the code with text //TODO:. So, it will be easier for code review. 

     5. It seems that the existing dialogue manager tries to match answer to hint with expectations also. For now, it tries to check answer to the pump or first short essay answer against the expectations, misconceptions, and also checks for irrelevant/short answer. However, the answer to the hint is matched against the expected answer of itself only. If answer to the hint should be checked against all expectations, it will require some more wires.

     6. To keep track of the progress of Task, a separate object called TaskProgressInfo has been created. It is kept in the dialog context. Some variables are reset just before returning response to the client. For example, a variable expectationsWereHit. Once the student answer is evaluated and this variable is used (if needed), we need to reset it. Some other variables, such as currentlyWorkingExpectation should be set/changed depending on the conditions. Not every time. 
     Some of the attributes in TaskProgressInfo can be reduced if the name of the current state is used. For example, if the current state is Sequence/Conditional/Final hint, it tells that we are working on hints. However, all variables can't be inferred from the name of current state.

     7. The short essay answer, pump answer, hint answer is evaluated as common pre-action (i.e. before evaluating conditions for potential transition). During the evaluation, certain variables in TaskProgressInfo are set. For example, expectationsHit, misconceptionHit etc. After then, variables are checked to evaluate whether the conditions are satisfied for the transition. Otherwise, if we evaluate the answer again and again as part of condition checking for different possible transitions, it will be a duplicate work. Also, as some variables need to be updated such as current working expectation, it will be difficult to keep track if the same answer is evaluated again. However, it is not impossible. We can keep the copy of task progress status, evaluate the answer and restore the original progress status and evaluate the answer again for evaluating conditions for other transitions. It will make the state transition more discrete or modular in nature but with some pay off.
 
     8. Moving ahead, the dialog policy may need to be tailored. So, during initialization of dialog manager (for individual user), dialog policy configuration (if any) can be extracted from the student object passed to it. Currently, the dialog policy is same for everyone. So, it takes the dialog policy file name and path from the configuration file (dtconfig) irrespective of the student.

     9. The multimedia update code has not been included yet.

     10. As we are planning to design the new logger, the data are not logged mostly. It will be done once we come up with the final logging format. 

     */
    /* August 11: Rajendra
     * 
     * In the old code, there is some comment: for the first hint in sequence we must check the pump answer and give positive feedback if it matches
     * Does it mean that, if the student answer doesn't cover any expectations, then first hint is to be suggested. But if the answer covers hint
     * (loosely saying: has all required words), then give positive feedback?
     * 
     * 
     */
}
