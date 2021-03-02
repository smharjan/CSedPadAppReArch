/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.authoring.auth.dashboard;

import com.opensymphony.xwork2.ActionSupport;
import dt.constants.Result;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.FileUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;

/**
 *
 * @author suraj
 */
public class AdminTaskUploader extends ActionSupport implements SessionAware {
    
    private String message;

    private Map<String, Object> session;
        
    private File myFile;
    private String myFileContentType;
    private String myFileFileName;
    private File destinationDirTasks = new File(memphis.deeptutor.singleton.ConfigManager.GetTasksPath());
    private File destinationDirMedia = new File(memphis.deeptutor.singleton.ConfigManager.GetMediaPath());
    
    public File getMyFile() {
      return myFile;
   }
   
   public void setMyFile(File myFile) {
      this.myFile = myFile;
   }
   
   public String getMyFileContentType() {
      return myFileContentType;
   }
   
   public void setMyFileContentType(String myFileContentType) {
      this.myFileContentType = myFileContentType;
   }
   
   public String getMyFileFileName() {
      return myFileFileName;
   }
   
   public void setMyFileFileName(String myFileFileName) {
      this.myFileFileName = myFileFileName;
   }
   
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    
    public void setSession(Map<String, Object> map) {
        this.session = map;
    }

    @Override
    public String execute () throws Exception{
        HttpServletRequest request = ServletActionContext.getRequest();
//        HttpServletResponse response = ServletActionContext.getResponse();
        HttpSession session = request.getSession();

        try {
            System.out.println("Src File name: " + myFile);
            System.out.println("Dst File name: " + myFileFileName);
            if (myFileFileName.endsWith(".xml")) {
                File destFile  = new File(destinationDirTasks, myFileFileName);
                FileUtils.copyFile(myFile, destFile);
            } else {
                File destFile  = new File(destinationDirMedia, myFileFileName);
                FileUtils.copyFile(myFile, destFile);
            }
        } catch(IOException e) {
            e.printStackTrace();
            return ERROR;
        }
        session.setAttribute("upload_status","Last file succesfully uploaded: " + myFileFileName);
        setMessage("Welcome to actionhome after upload button clicked");
        return Result.SUCCESS;
    }
    
    
}
