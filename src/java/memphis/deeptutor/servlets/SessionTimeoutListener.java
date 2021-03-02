package memphis.deeptutor.servlets;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 *
 * @author Rajendra
 * Created on Aug 22, 2013, 1:51:48 PM 
 */
public class SessionTimeoutListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        long now = new java.util.Date().getTime();
        boolean timeout = (now - session.getLastAccessedTime()) >= ((long)session.getMaxInactiveInterval() * 1000L);
        if (timeout) {
            //ServletContext servletContext=se.getSession().getServletContext();
            //servletContext.
            System.out.println("Time out...");
        }
    }
}
