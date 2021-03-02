package dt.dialog;

import dt.core.dialogue.SAClassifier.SPEECHACT;
import dt.core.semantic.StudentResponseEvaluator;
import dt.entities.database.Student;
import dt.entities.plain.DTInput;
import dt.entities.plain.SessionData;
import dt.entities.xml.DTResponse;
import dt.log.DTLogger;
import dt.persistent.xml.TaskManager;
import java.util.List;

import javax.xml.bind.JAXBException;

//import memphis.deeptutor.dialog.SAClassifier.SPEECHACT;
//import memphis.deeptutor.dialog.StudentResponseEvaluator;
//import memphis.deeptutor.dialog.TaskManager;
//import memphis.deeptutor.gui.model.DTResponse;
//import memphis.deeptutor.log.DTLogger;
//import memphis.deeptutor.model.SessionData;
//import memphis.deeptutor.model.Student;
//import memphis.deeptutor.model.Task;
//import memphis.deeptutor.tools.TaskParser;

public class StudentInputProcessor {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		StudentInputProcessor sp = new StudentInputProcessor();
		Student s1;
		//sp.processContributionNew(s, data, sa, input)
		
		//DTConfig.prop
		//C:\DeepTutor\ServerData
		String currentTaskID="C:\\Users\\nobal\\Dropbox\\GA\\DeepTutor\\LP02_PR00.xml";	
		//TaskParser task = new TaskParser(currentTaskID);
		TaskManager task = new TaskManager(currentTaskID);

        String fName="C:\\Users\\nobal\\Dropbox\\GA\\DialogManager\\dt_dialog_stn_vr_1.xml";
		fName="C:\\Users\\nobal\\Dropbox\\Shared\\withRajendra\\DialogManager\\dt_dialog_stn_vr_1-RB-2.xml";

		//DP should be saved in session
		DialogPolicy dp = new DialogPolicy();
		dp.parsePolicyFile(fName);
		
		State currentState=dp.getCurrentState();
		
		//StudentResponseEvaluator se = new StudentResponseEvaluator(new TaskManager(currentTaskID));
        StudentResponseEvaluator se = new StudentResponseEvaluator(task);
		List<Transition> possibleTransitions =dp.getOutgoingTransitions(currentState);
		
		//if there are no outgoing transitions for a state s and s is not a final node
		if(!currentState.isEndState() && possibleTransitions.size()==0){
			System.out.println("No actions are specified for this given state.");			
		}
		Transition firedTransition=null;
		for(Transition t:possibleTransitions){
			System.out.println(t.getFrom()+"\t"+t.getTo());
			boolean tranEval=ConditionEvaluator.evaluateTemp(t,task, "test");
			if(tranEval){
				firedTransition=t; 
			}
		}
		
		if(firedTransition!=null){
			System.out.println("None of the transition satisfied. Check the script please. ");
		}else{			
			//process actions
			List<Action> exitStateActions=currentState.getExitActions();
			List<Action> transitionActions=firedTransition.getActions();
			
			currentState = dp.getState(firedTransition.getTo()); 
		}
		
		//Save current state to session
		

		

		
		
		/*
		
        System.out.println("Total states:"+dp.getStates().size());
        for(State s:dp.getStates()){
           System.out.println("Name:"+s.getName());
           System.out.println("Desc:"+s.getDesc());
           if(s.getExitActions()!=null){
           for(Action a:s.getExitActions()){
               System.out.println("Exit Desc:"+a.getValue());
            }
           }
        }
        
        for(Transition t:dp.getTransitions()){
           System.out.println("From:"+ t.getFrom()+" "+"To:"+ t.getTo()+", Actions: "+t.getActions().size()+" Conditions:"+t.getConditions().size());
           List<Action> acts = t.getActions();
           for(Action a:acts){
               System.out.println(a.getValue());
           }
        }
        */
	}
	
	 
    private static DTResponse processContributionNew(Student s, SessionData data, SPEECHACT sa, DTInput input) {
        DTLogger logger = null;
        DTResponse response = new DTResponse();

        return response;
    }

}
