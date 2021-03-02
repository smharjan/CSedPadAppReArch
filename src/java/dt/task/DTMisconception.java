/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.task;

import java.util.ArrayList;

/**
 *
 * @author Rajendra
 */
public class DTMisconception {
    
    private String id;
    
    ArrayList<String> texts;
    
    private String assertion;
    private DTRequired required;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssertion() {
        return assertion;
    }

    public void setAssertion(String assertion) {
        this.assertion = assertion;
    }

    public DTRequired getRequired() {
        return required;
    }

    public void setRequired(DTRequired required) {
        this.required = required;
    }

    public ArrayList<String> getTexts() {
        return texts;
    }

    public void setTexts(ArrayList<String> texts) {
        this.texts = texts;
    }   
}
