/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.entities.xml;

import java.util.ArrayList;

/**
 *
 * @author Rajendra Created on Feb 1, 2013, 4:22:56 PM
 */
public class DTResponse {

    //TODO: refine it..., finally the response to client will be xml.
    // it should be annotated to generate the xml using XStream.
    String responseText;
    String xmlResponse;
    String taskId;
    String taskDiscription;
    String multiMedia;
    boolean errorFlagged;
    boolean allTasksFinished;
    boolean hasContent;

    ArrayList<String> leftOverText;
    
    public DTResponse() {
        leftOverText = new ArrayList<String>();
    }

    /**
     * If response has something (i.e. if the content length is greater than zero).
     * @return 
     */
    public boolean hasContent() {
        return hasContent;
    }
   
    public boolean isAllTasksFinished() {
        return allTasksFinished;
    }

    public void setAllTasksFinished(boolean allTasksFinished) {
        this.allTasksFinished = allTasksFinished;
    }

    public boolean isErrorFlagged() {
        return errorFlagged;
    }

    public void setErrorFlagged(boolean errorFlagged) {
        this.errorFlagged = errorFlagged;
    }

    public void formXMLResponse() {
        String xmlResponse = "<DTCommands><command type=\"loadNewQuestion\"><question><text>A mover pushes a box"
                + "     horizontally so that it slides straight across the rough floor at a constant speed. Describe the forces that are acting on the box and "
                + "    indicate which forces balance. </text><text2>Please begin by briefly answering the above question. After briefly answering the above question,"
                + "     please go on to explain your answer in as much detail as you can.</text2><image><src>/DeeptutorApp/tests/images/Bao_question_10.PNG</src><height>100</height>"
                + "    <width>100</width></image><multimedia><src>/DeeptutorApp/tests/images/Bao_question_10.PNG</src><type></type><height>10</height><width>20</width>"
                + "    </multimedia></question></command><command type=\"changeMultimedia\"><multimedia><src>/DeeptutorApp/tests/images/Bao_question_10.PNG</src><type></type>"
                + "    <height>10</height><width>20</width></multimedia></command><command type=\"clearHistory\"/><command type=\"setStudentID\">ss1</command>"
                + "    <command type=\"responseToStudent\"><response> The current task will improve your knowledge about forces acting on objects being pushed over "
                + "    a surface with constant velocity. Please read the problem and instructions and then let's solve it.</response>"
                + "    </command><command type=\"changeAvatar\"><source>../DTAvatar/DTAvatar.swf</source></command><command type=\"changeInformation\">"
                + "    <information>Covered Expectations for Current Task: 0 out of 5</information></command></DTCommands>";
        this.setXmlResponse(xmlResponse);
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public String getXmlResponse() {
        return xmlResponse;
    }

    public void setXmlResponse(String xmlResponse) {
        this.xmlResponse = xmlResponse;
        this.hasContent = true;
    }
    String note;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public ArrayList<String> getLeftOverText() {
        return leftOverText;
    }

    public void setLeftOverText(ArrayList<String> leftOverText) {
        this.leftOverText = leftOverText;
    }

    public void addLeftoverText(String leftOverTxt) {
        this.leftOverText.add(leftOverTxt);
    }
}
