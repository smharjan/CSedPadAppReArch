/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dt.task;

import java.util.ArrayList;


/**
 *
 * @author Rajendra
 * Created on Jan 28, 2013, 11:14:22 AM 
 */
public class DTTask {
    
    private String id;
    private String description;
    private String level;
    private String text;
    private String text2;
    private String intro;
    
    private DTImage image;
    //private DTMultimedia multimedia;
    ArrayList<DTExpectation> expectations;
    //TODO: Since the content in misconception (though less) is common to expectation and the way they are filtered, or evaluated against the student answer is same
    // DTExpectation class is  used for misconception too.
    ArrayList<DTExpectation> misconceptions;   
    
    private String relevantText;
   
    public DTTask() {
        expectations = new ArrayList<DTExpectation>();
        relevantText = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public DTImage getImage() {
        return image;
    }

    public void setImage(DTImage image) {
        this.image = image;
    }

    public ArrayList<DTExpectation> getExpectations() {
        return expectations;
    }

    public void setExpectations(ArrayList<DTExpectation> expectations) {
        this.expectations = expectations;
    }

    public ArrayList<DTExpectation> getMisconceptions() {
        return misconceptions;
    }

    public void setMisconceptions(ArrayList<DTExpectation> misconceptions) {
        this.misconceptions = misconceptions;
    }

   
    public DTExpectation getExpectationById(String id) {
        for (DTExpectation exp : this.expectations) {
            if (exp.getId().equalsIgnoreCase(id)) {
                return exp;
            }
        }
        return null;
    }

    public String getRelevantText() {
        return relevantText;
    }

    public void setRelevantText(String relevantText) {
        this.relevantText = relevantText;
    }

}
