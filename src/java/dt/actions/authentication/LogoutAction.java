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
 *
 */
public class LogoutAction extends ActionSupport implements SessionAware {

    Map<String, Object> session = null;
    private InputStream inputStream;

    public InputStream getInputStream() {
        return inputStream;
    }

    public LogoutAction() {
    }

    public String execute() throws Exception {
        if (session instanceof org.apache.struts2.dispatcher.SessionMap) {
            try {
                ((org.apache.struts2.dispatcher.SessionMap) session).invalidate();
                sendResponse("<Response><Status>Success</Status><message>Successfully logged out. </message></Response>");
            } catch (IllegalStateException e) {
                System.err.print("Logout failed" + e);
                sendResponse("<Response><Status>Failed</Status><message>Failed to log out. </message></Response>");
            }
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
