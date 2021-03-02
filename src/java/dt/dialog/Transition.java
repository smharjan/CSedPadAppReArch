/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.dialog;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 *
 * @author nobal
 */
public class Transition {
    private String from;
    private String to;
    private String pause;
    private List<Action> actions;
    private List<Condition> conditions;

    /**
     * @return the from
     */
    public String getFrom() {
        return from;
    }

    /**
     * @param from the from to set
     */
    @XmlAttribute 
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * @return the to
     */
    @XmlAttribute 
    public String getTo() {
        return to;
    }

    /**
     * @param to the to to set
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * @return the pause
     */
    @XmlAttribute 
    public String getPause() {
        return pause;
    }

    /**
     * @param pause the pause to set
     */
    public void setPause(String pause) {
        this.pause = pause;
    }

    /**
     * @return the actions
     */
    @XmlElementWrapper(name = "actions")
    @XmlElement(name = "action")
    public List<Action> getActions() {
        return actions;
    }

    /**
     * @param actions the actions to set
     */
    public void setActions(List<Action> actions) {
        this.actions = actions;
    }
    
    
    
    /**
     * @return the actions
     */
    @XmlElementWrapper(name = "conditions")
    @XmlElement(name = "cond")    
    public List<Condition> getConditions() {
        return conditions;
    }

    /**
     * @param actions the actions to set
     */
    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }
    
}
