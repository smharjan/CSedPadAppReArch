/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.dialog;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 * @author nobal
 */
public class Action {

    private String value;

    /**
     * @return the action
     */
    @XmlValue 
    public String getValue() {
        return value;
    }

    /**
     * @param action the action to set
     */
    public void setValue(String action) {
        this.value = action;
    }
    
}
