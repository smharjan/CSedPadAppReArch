/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.dialog;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
/**
 *
 * @author nobal
 */ 
@XmlRootElement(name="dm")
public class Dialog {
     private List<State> states; 
     private List<Transition> transitions;
    /**
     * @return the states
     */
    @XmlElementWrapper(name = "states")
    @XmlElement(name = "state")
    public List<State> getStates() {
        return states;
    }
    /**
     * @param states the states to set
     */
    public void setStates(List<State> states) {
        this.states=states;
    }

    /**
     * @return the transitions
     */
    @XmlElementWrapper(name = "transitions")
    @XmlElement(name = "transition")
    public List<Transition> getTransitions() {
        return transitions;
    }

    /**
     * @param transitions the transitions to set
     */
    public void setTransitions(List<Transition> transitions) {
        this.transitions = transitions;
    }
}
