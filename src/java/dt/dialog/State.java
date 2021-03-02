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
public class State {
    private String name;
    private String start;
    private String end;
    private String desc;    
    private boolean waitForInput;
    private List<Action> exitActions;

    /**
     * @return the name
     */
     @XmlAttribute  
    public String getName() {
        return name;
    }

     
     
    /**
     * @param name the name to set
     */
     
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the start
     */
    @XmlAttribute 
    public String getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(String start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    @XmlAttribute 
    public String getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(String end) {
        this.end = end;
    }

    /**
     * @return the desc
     */
    @XmlAttribute  
    public String getDesc() {
        return desc;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isWaitForInput() {
        return waitForInput;
    }

    @XmlAttribute 
    public void setWaitForInput(boolean waitForInput) {
        this.waitForInput = waitForInput;
    }

    /**
     * @return the exitActions
     */
    @XmlElementWrapper(name = "exitactions")
    @XmlElement(name = "action")  
    public List<Action> getExitActions() {
        return exitActions;
    }

    /**
     * @param exitActions the exitActions to set
     */
    public void setExitActions(List<Action> exitActions) {
        this.exitActions = exitActions;
    }
    
    public boolean isStartState(){
    	if(this.start!=null && this.start.length()>1){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    public boolean isEndState(){
    	if(this.end!=null && this.end.length()>1){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    
}
