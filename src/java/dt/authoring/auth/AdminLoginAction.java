/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.authoring.auth;

import com.opensymphony.xwork2.ActionSupport;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.apache.struts2.interceptor.SessionAware;
import dt.constants.Result;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.struts2.ServletActionContext;

/**
 *
 * @author suraj
 */

public class AdminLoginAction extends ActionSupport implements SessionAware{
    
    private String adminUsername;
    private String adminPassword;
//    private String message;
    
    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

//    public String getMessage() {
//        return message;
//    }
//
//    public void setMessage(String message) {
//        this.message = message;
//    }
//    
    

    private Map<String, Object> session;
    @Override
    public void setSession(Map<String, Object> map) {
        this.session = map;
        this.session.put("Access-Control-Allow-Origin", "*");
    }

     public String execute() throws UnsupportedEncodingException {
         
         // connect to database and check if user are valid or not
         // if authenticated sucess
//        return Result.SUCCESS;
         
         
//        String givenUername = request.getParameter("adminUsername");
//        String password = request.getParameter("adminPassword");
//        // if this user is visiting first time,
//        HttpSession session = request.getSession();
//        System.out.print("doPost: " + session.getId());
//        if (givenUername.trim().equals("dtadmin") && password.trim().equals("dtauthor310")){
//                String adminPage = "/admin.jsp";
//                session.setAttribute("loginSuccesfull", "true");
//                RequestDispatcher rd = getServletContext().getRequestDispatcher(adminPage);
//                rd.forward(request, response);
//        } else {
//                session.setAttribute("error", "Admin credentials failed");
//                String destinationOnError = "/adminlogin.jsp";
//                RequestDispatcher rd = getServletContext().getRequestDispatcher(destinationOnError);
//                rd.forward(request, response);
//        }
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpSession session = request.getSession();
        if(this.adminUsername.equals("dtadmin") && this.adminPassword.equals("dtauthor310")){
//			setMessage("Welcome");
                        session.setAttribute("loginSuccesfull", "true");
			return Result.SUCCESS;
			
		}else 
			return Result.ERROR;
         //if fail stay at same page
         //return Result.INPUT;
         
     }
    
}
