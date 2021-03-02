package dt.interceptors;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import dt.config.ConfigManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsStatics;

/**
 *
 * @author Nabin
 */
public class HttpHeaderResponseInterceptor extends AbstractInterceptor {

    @Override
    public String intercept(ActionInvocation ai) throws Exception {
//        HttpServletResponse response = (HttpServletResponse) ActionContext.getContext().get(StrutsStatics.HTTP_RESPONSE);
//        response.addHeader("Access-Control-Allow-Origin", "*");
//        response.addHeader("Access-Control-Allow-Credentials", "true");
//        return ai.invoke();

 HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(StrutsStatics.HTTP_REQUEST);
       
        //The value of "*" is special in that it does not allow requests to supply credentials, 
        //meaning it does not allow HTTP authentication, client-side SSL certificates, or cookies to be sent in the cross-domain request.
        //so we will instead maintain a list of website origins which are allowed to make cross website requests.
        String origin = request.getHeader("Origin");
       if(!ConfigManager.isInitialized()){
            ConfigManager.init(ServletActionContext.getServletContext());
        }
        if(ConfigManager.getAllowedSites().contains(origin)){
             HttpServletResponse response = (HttpServletResponse) ActionContext.getContext().get(StrutsStatics.HTTP_RESPONSE);
            response.addHeader("Access-Control-Allow-Origin", origin);
            response.addHeader("Access-Control-Allow-Credentials", "true");
        }
        return ai.invoke();
    }

}
