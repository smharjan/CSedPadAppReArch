/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.task;

import dt.persistent.xml.Expectation;
import dt.persistent.xml.Expectation.EXPECT_TYPE;
import java.util.ArrayList;

/**
 *
 * @author Rajendra
 */
public class DTExpectation implements Comparable<DTExpectation> {

    private String id;
    private int order;
    private EXPECT_TYPE type = EXPECT_TYPE.NONE; 
    private String description;
    private String pump;
    //TODO: text id = 1, 2, 3 in the task file.
    private ArrayList<String> variants;  //text id = 1..
    private String assertion;
    private DTPrompt prompt;
    private ArrayList<DTHint> hints;
    private boolean misconception; //TODO: is it requred?
    private DTRequired required; //TODO: what is this???
    private String forbidden; //TODO: what is the xml element for this?

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public EXPECT_TYPE getType() {
        return type;
    }

    public void setType(EXPECT_TYPE type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getVariants() {
        return variants;
    }

    public void setVariants(ArrayList<String> variants) {
        this.variants = variants;
    }

    public String getAssertion() {
        return assertion;
    }

    public void setAssertion(String assertion) {
        this.assertion = assertion;
    }

    public String getPump() {
        return pump;
    }

    public void setPump(String pump) {
        this.pump = pump;
    }

    public DTRequired getRequired() {
        return required;
    }

    public void setRequired(DTRequired required) {
        this.required = required;
    }

    public DTPrompt getPrompt() {
        return prompt;
    }

    public void setPrompt(DTPrompt prompt) {
        this.prompt = prompt;
    }

    public ArrayList<DTHint> getHints() {
        return hints;
    }

    public void setHints(ArrayList<DTHint> hints) {
        this.hints = hints;
    }

    public boolean isMisconception() {
        return misconception;
    }

    public void setMisconception(boolean misconception) {
        this.misconception = misconception;
    }

    public String getForbidden() {
        return forbidden;
    }

    public void setForbidden(String forbidden) {
        this.forbidden = forbidden;
    }

    @Override
    public int compareTo(DTExpectation o) {

        //if (Math.abs(this.similarity) < Math.abs(o.similarity)) return 1;
        //if (Math.abs(this.similarity) > Math.abs(o.similarity)) return -1; 
        return 0;
    }
}
