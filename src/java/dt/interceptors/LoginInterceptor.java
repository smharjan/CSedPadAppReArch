/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.interceptors;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import dt.entities.database.Student;
import java.util.Map;

/**
 *
 * @author Rajendra
 */
public class LoginInterceptor extends AbstractInterceptor {
    @Override
    public String intercept(final ActionInvocation invocation) throws Exception {
        Map<String, Object> session = ActionContext.getContext().getSession();

        // sb: feel free to change this to some other type of an object which
        // represents that the user is logged in. for this example, I am using
        // an integer which would probably represent a primary key that I would
        // look the user up by with Hibernate or some other mechanism.
        Student student = (Student) session.get("student");

        if (student != null) {
            return invocation.invoke();
        }

        //Object action = invocation.getAction();

        // sb: if the action doesn't require sign-in, then let it through.
        //if (!(action instanceof LoginRequired)) {
        //    return invocation.invoke();
        //}

        // sb: if this request does require login and the current action is
        // not the login action, then redirect the user
       // if (!(action instanceof LoginAction)) {
       //     return "loginRedirect";
        //}

        // sb: they either requested the login page or are submitting their
        // login now, let it through
        return invocation.invoke();
    }
}