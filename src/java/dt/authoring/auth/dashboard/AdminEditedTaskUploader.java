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
public class AdminEditedTaskUploader extends ActionSupport implements SessionAware {
    
    private String message;

    private Map<String, Object> session;
        
    private File editedFile;
    private String fileContentType;
    private String editedFileFileName;    
    private File destinationDirEditedTasks = new File(memphis.deeptutor.singleton.ConfigManager.GetEditedTasksPath());

    public File getEditedFile() {
        return editedFile;
    }

    public void setEditedFile(File editedFile) {
        this.editedFile = editedFile;
    }    

    public String getFileContentType() {
        return fileContentType;
    }

    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }

    public String getEditedFileFileName() {
        return editedFileFileName;
    }

    public void setEditedFileFileName(String editedFileFileName) {
        this.editedFileFileName = editedFileFileName;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    
    public void setSession(Map<String, Object> map) {
        this.session = map;
    }
    
    @Override
    public String execute () throws Exception {
        
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpSession session = request.getSession();
        
        try {
            System.out.println("Src File name: " + editedFile);
            System.out.println("Dst File name: " + editedFileFileName);
            
            File destFile  = new File(destinationDirEditedTasks, editedFileFileName);
            FileUtils.copyFile(editedFile, destFile);
            
        } catch(IOException e) {
            e.printStackTrace();
            return ERROR;
        }
        session.setAttribute("upload_status2","Last file succesfully uploaded: " + editedFileFileName);
        setMessage("Welcome to actionhome after upload button clicked editeddddddddddddddddddddd");
        return Result.SUCCESS;
    }
    
}
