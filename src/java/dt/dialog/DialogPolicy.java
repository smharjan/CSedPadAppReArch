/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dt.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * 
 * @author nobal
 */
public class DialogPolicy {
	private Map<String,State> stateMap;

	private List<State> states;
	private List<Transition> transitions;

	private State currentState;
	private State startState;

	public List<State> getStates() {
		return states;
	}
	
	public State getState(String stateName) {
		return stateMap.get(stateName);
	}

	public List<Transition> getTransitions() {
		return transitions;
	}

	public State getCurrentState() {
		return currentState;
	}

	public State getStartState() {
		return startState;
	}

	/**
	 * Parses the dialog policy
	 * @param fileName
	 * @throws Exception
	 */
	public void parsePolicyFile(String fileName) throws Exception {
		File file = new File(fileName);
		JAXBContext jaxbContext = JAXBContext.newInstance(Dialog.class);

		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Dialog dm = (Dialog) jaxbUnmarshaller.unmarshal(file);
		states = dm.getStates();
		transitions = dm.getTransitions();
		stateMap= new HashMap<String,State>();

		for (State s : states) {
			stateMap.put(s.getName(), s);
			if(s.isStartState()&& startState!=null){
				throw new Exception("Multiple start states in file:"+file.getName());
			}
			if (s.isStartState()) {
				startState = s;
			}
			//if there are no outgoing transitions from a state s and s is not a final node
			if(!s.isEndState() && this.getOutgoingTransitions(s).size()==0){
				throw new Exception("No transitions are specified from a non-final state:"+s.getName());			
			}
		}
		
		if (startState == null) {
			throw new Exception("There is no start state in the supplied file:"
					+ file.getName());
		} else {
			currentState=startState;
		}
		for(Transition t: transitions){
			if(stateMap.get(t.getFrom())==null){
				throw new Exception("Unknown State name: "+t.getFrom());
			}
			if(stateMap.get(t.getTo())==null){
				throw new Exception("Unknown State name: "+t.getTo());
			}
		}
		
	}
	
	/**
	 * Returns the transitions from a given state
	 * @param s
	 * @return
	 */
	public List<Transition> getOutgoingTransitions(State s){
		List<Transition> outgoingTransitions = new ArrayList<Transition>();
		for(Transition t:transitions){
			if(t.getFrom().equals(s.getName())){
                System.out.println(s.getName()+"\t"+t.getFrom());
				outgoingTransitions.add(t);
			}
		}
		return outgoingTransitions;
	}

}
