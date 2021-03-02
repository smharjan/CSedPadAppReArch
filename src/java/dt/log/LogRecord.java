/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.log;

import dt.log.LogRecordAttribute.AttributeType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rajendra
 */
public class LogRecord {

    private ArrayList<LogRecordAttribute> attributeList;
    
    public LogRecord() {
        attributeList = new ArrayList<>();
    } 
    
    public void addAttribute(AttributeType type, String name, String value) {
        LogRecordAttribute attribute = new LogRecordAttribute();
        attribute.setType(type);
        attribute.setName(name);
        attribute.setStringValue(value);       
    }
    
    public void addAttribute(AttributeType type, String name, List<String> value) {
        LogRecordAttribute attribute = new LogRecordAttribute();
        attribute.setType(type);
        attribute.setName(name);
        attribute.setListValue(value);
    }

    public ArrayList<LogRecordAttribute> getAttributeList() {
        return attributeList;
    }

}
