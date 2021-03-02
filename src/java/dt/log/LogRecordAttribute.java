/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.log;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rajendra
 */
public class LogRecordAttribute {
    
    public enum AttributeType { String, List, Map }
    private AttributeType type;
    private String name;
    private String stringValue;
    private List<String> listValue;
    
    public LogRecordAttribute () {
        listValue = new ArrayList<>();
    }

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType type) {
        this.type = type;
    }

    public java.lang.String getName() {
        return name;
    }

    public void setName(java.lang.String name) {
        this.name = name;
    }

    public java.lang.String getStringValue() {
        return stringValue;
    }

    public void setStringValue(java.lang.String stringValue) {
        this.stringValue = stringValue;
    }

    public List<java.lang.String> getListValue() {
        return listValue;
    }

    public void setListValue(List<java.lang.String> listValue) {
        this.listValue = listValue;
    }    
}
