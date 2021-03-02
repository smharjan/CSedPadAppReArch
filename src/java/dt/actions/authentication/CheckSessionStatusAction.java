/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.actions.authentication;

import com.opensymphony.xwork2.ActionSupport;
import dt.constants.Result;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.apache.struts2.interceptor.SessionAware;

/**
 *
 * @author admin
 */
public class CheckSessionStatusAction extends ActionSupport implements SessionAware {

    Map<String, Object> session = null;

    public CheckSessionStatusAction() {
    }
    private InputStream inputStream;

    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public String execute() throws Exception {

        if (session.containsKey("student")) {
            sendResponse("<Response><Status>Valid</Status><message>Student session is still valid </message></Response>");
        } else {
            sendResponse("<Response><Status>Invalid</Status><message>Student session is not valid </message></Response>");
        }
        return Result.SUCCESS;
    }

    @Override
    public void setSession(Map<String, Object> map) {
        session = map;
    }

    /**
     * Write response to stream.
     *
     * @param responseText
     * @throws UnsupportedEncodingException
     */
    private void sendResponse(String responseText) throws UnsupportedEncodingException {
        // Now the response is formed using the old DTResponseOld, and Components object..,
        // this function is not being used.
        //response.formXMLResponse();
        System.out.println("WRITING TO CLIENT: ");
        System.out.println(responseText);
        inputStream = new ByteArrayInputStream(responseText.getBytes("UTF8"));
    }
}
