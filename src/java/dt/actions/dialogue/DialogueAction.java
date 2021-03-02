/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.actions.dialogue;

import com.opensymphony.xwork2.ActionSupport;
import dt.constants.Result;
import dt.core.managers.DialogueController;
import dt.entities.database.Student;
import dt.entities.xml.DTResponse;
import dt.log.DTLogger;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.apache.struts2.interceptor.SessionAware;

/**
 *
 * @author Rajendra
 */
public class DialogueAction extends ActionSupport implements SessionAware {

    private InputStream inputStream;
    private Map<String, Object> session;
    private String studentInputText;

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setStudentInputText(String studentInputText) {
        this.studentInputText = studentInputText;
    }

    public String getStudentInputText() {
        return this.studentInputText;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public DialogueAction() {
    }

    /*
     * In execute() method, just get the student, call run and do some pre/post
     *  processing such as logging, debug message printing etc.
     */
    @Override
    public String execute() throws Exception {

        System.out.println("ENTERING DIALOGUE ACTION");
        System.out.println("FROM THE CLIENT: =====>> " + getStudentInputText());
        Student s = (Student) session.get("student");

        String result = run(s);
        s.getLogger().saveLogInHTML();
        System.out.println("RETURNING FROM DIALOGUE ACTION");
        return result;
    }

    /**
     * This should be a clean method. Do some initialization, logging
     * before/after.. in the execute() method.
     *
     * @param s
     * @return
     * @throws Exception
     */
    private String run(Student s) throws Exception {
        DTResponse response = null;
        DTLogger logger = s.getLogger();
        logger.log(DTLogger.Actor.STUDENT, DTLogger.Level.ONE, this.getStudentInputText());
        //if all tasks has been finished..., and need to go to another screen..
        response = DialogueController.process(s, this.getStudentInputText());
        if (response.isAllTasksFinished()) {
            response = gendRedirectResponse();
        }
        sendResponse(response);
        logger.log(DTLogger.Actor.TUTOR, DTLogger.Level.ONE, response.getXmlResponse());

        // rajendra: 
        // Special note: In this case, success takes NOWHERE
        // we just returning stream.. and this is just to make our AJAX page to work.              
        return Result.SUCCESS;
    }

    /**
     * Sends Xml response to the client. NO NEED to close the input stream, the
     * system itself closes it when will the response is sent to the client.
     *
     * @param reponse
     * @throws UnsupportedEncodingException
     */
    private void sendResponse(DTResponse response) throws UnsupportedEncodingException {
        // Now the response is formed using the old DTResponseOld, and Components object..,
        // this function is not being used.
        //response.formXMLResponse();
        System.out.println("WRITING TO CLIENT: ");
        System.out.println(response.getXmlResponse());
        inputStream = new ByteArrayInputStream(response.getXmlResponse().getBytes("UTF8"));
    }

    /**
     * Send redirect response.
     *
     */
    private DTResponse gendRedirectResponse() {
        DTResponse response = new DTResponse();
        response.setXmlResponse("<DTCommands><command type=\"redirectToPostDialogue\"/></DTCommands>");
        return response;
    }
}
