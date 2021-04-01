/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.authoring.auth.dashboard;

import static com.opensymphony.xwork2.Action.ERROR;
import dt.constants.Result;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.struts2.ServletActionContext;

/**
 *
 * @author suraj
 */
public class AdminTaskSequenceUploader {
    
    private static final long serialVersionUID = 1L;

    private static final String TMP_DIR_PATH = System
                    .getProperty("java.io.tmpdir");
    private File tmpDir;
    private File taskSeqDestinationFolder = new File(memphis.deeptutor.singleton.ConfigManager.GetDataPath()+"\\");
    
    private File experimentFile;
    private String experimentFileContentType;
    private String experimentFileFileName;

    public File getExperimentFile() {
        return experimentFile;
    }

    public void setExperimentFile(File experimentFile) {
        this.experimentFile = experimentFile;
    }

    public String getExperimentFileContentType() {
        return experimentFileContentType;
    }

    public void setExperimentFileContentType(String experimentFileContentType) {
        this.experimentFileContentType = experimentFileContentType;
    }

    public String getExperimentFileFileName() {
        return experimentFileFileName;
    }

    public void setExperimentFileFileName(String experimentFileFileName) {
        this.experimentFileFileName = experimentFileFileName;
    }
    
    
    private Map<String, Object> session;
    
    public void setSession(Map<String, Object> map) {
        this.session = map;
    }
    
    public String execute() throws Exception {
        System.out.println("asddddddddddddddddddddddddddddddddddddddddddddddddd"+taskSeqDestinationFolder);
        HttpServletRequest request = ServletActionContext.getRequest();

      
                HttpSession session = request.getSession();
                
                try {
                 
                    if (experimentFileFileName.endsWith(".xml")) {
                        File destFile  = new File(taskSeqDestinationFolder, "experimentConfig.xml");
                        FileUtils.copyFile(experimentFile, destFile);
                        session.setAttribute("status", experimentFileFileName + " is uploaded succesfully.");
                    } else {
                        session.setAttribute("status", "Failed! It can only upload XML files.");
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                    return ERROR;
                }
        return Result.SUCCESS;
    }
    
}
