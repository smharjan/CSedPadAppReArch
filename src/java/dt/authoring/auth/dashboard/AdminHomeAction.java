/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.authoring.auth.dashboard;

/**
 *
 * @author suraj
 */
import com.opensymphony.xwork2.ActionSupport;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.apache.struts2.interceptor.SessionAware;
import dt.constants.Result;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.TreeMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts2.ServletActionContext;
import static org.apache.struts2.ServletActionContext.getServletContext;

/**
 *
 * @author suraj
 */

public class AdminHomeAction extends ActionSupport implements SessionAware{
    
    private String message;
    private String DESTINATION_DIR_PATH = "/DTResources";
    private Map<String, Object> session;
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
//    private boolean isInvokedFromLogin = false;
//    private Map<String, Object> session;
//
//    public boolean isIsInvokedFromLogin() {
//        return isInvokedFromLogin;
//    }
//
//    public void setIsInvokedFromLogin(boolean isInvokedFromLogin) {
//        this.isInvokedFromLogin = isInvokedFromLogin;
//    }
//    
    
    public void setSession(Map<String, Object> map) {
        this.session = map;
    }
    
    @Override
    public String execute() throws Exception{
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        HttpSession session = request.getSession();
		
		Boolean viewingLogs = false;
		Boolean viewingMedia = false;
		String query = (String)request.getParameter("get_files");
		if (query!=null && query.equals("logs")) viewingLogs = true;
		if (query!=null && query.equals("media")) viewingMedia = true;
		
		String realPath = null;// = getServletContext().getRealPath(DESTINATION_DIR_PATH) + "\\";
		if (viewingLogs) realPath = memphis.deeptutor.singleton.ConfigManager.GetLogPath();
		else if (viewingMedia) realPath = memphis.deeptutor.singleton.ConfigManager.GetMediaPath();
		else realPath = memphis.deeptutor.singleton.ConfigManager.GetTasksPath();

		File destinationDir = new File(realPath);
		if (!destinationDir.isDirectory()) {
			throw new ServletException(realPath	+ " is not a directory");
		} 

		String queryView = (String)request.getParameter("view_file");

		File[] files = destinationDir.listFiles();
		Arrays.sort(files);
		Map<String,String> fileNames = new TreeMap<String,String>();
		for (File f : files) {
			if (!f.isDirectory())
			{
				String filePath = "";
				if (query!=null && query.equals("true")) filePath = realPath+"/"+f.getName();
				else filePath = getServletContext().getContextPath()+DESTINATION_DIR_PATH+"/Tasks/"+f.getName();
				if (viewingLogs) fileNames.put(f.getName(), f.getName());
				else fileNames.put(f.getName(), filePath);
			}
		}
		session.setAttribute("files", fileNames);
		session.setAttribute("get_files", query);
		
		if (queryView!=null)
		{
			if (viewingLogs)
			{
				StringBuilder contents = new StringBuilder();
				BufferedReader input =  new BufferedReader(new FileReader(realPath + queryView));
				String line = null;
				while (( line = input.readLine()) != null){
			          contents.append(line);
			          contents.append(System.getProperty("line.separator"));
			    }
				input.close();
				
				String s = contents.toString();
				session.setAttribute("file_content", s);
				session.setAttribute("file_name", queryView);
			}
			else{
				if(viewingMedia)
				{
					//we need to copy the file from the real folder to a web accessible folder
					File realFile = new File(realPath + queryView);
					File webFile = new File(getServletContext().getRealPath(DESTINATION_DIR_PATH) + "\\Media\\" + queryView);
					String webFileStr = getServletContext().getContextPath()+DESTINATION_DIR_PATH+"/Media/" + queryView;
					
					if ((!webFile.exists()) || (realFile.lastModified() != webFile.lastModified()) )
					{
						org.apache.commons.io.FileUtils.copyFile(realFile, webFile, true);
						System.out.print("File temporarily copied in web accesible folder: " + queryView);
					}
					
					if (queryView.endsWith(".jpg") || queryView.endsWith(".png"))
						session.setAttribute("file_content", "<img width=\"100%\" src=\""+webFileStr+"\"/>");
					else
						session.setAttribute("file_content", "<iframe width=\"700\" height=\"400\" src=\""+webFileStr+"\">Sorry, but your browser does not support iframes.</iframe>");
					session.setAttribute("file_name", webFileStr);
				}
				else{
					if (queryView.endsWith(".xml"))
					{
						//we need to copy the file from the real folder to a web accessible folder
						File realFile = new File(realPath + queryView);
						File webFile = new File(getServletContext().getRealPath(DESTINATION_DIR_PATH) + "\\Tasks\\" + queryView);
						String webFileStr = getServletContext().getContextPath()+DESTINATION_DIR_PATH+"/Tasks/" + queryView;
						
						if ((!webFile.exists()) || (realFile.lastModified() != webFile.lastModified()) )
						{
							org.apache.commons.io.FileUtils.copyFile(realFile, webFile, true);
							System.out.print("File temporarily copied in web accesible folder: " + queryView);
						}
						
						String xmlView = "<div id='XMLHolder' > </div>"+
						" <LINK href=\'XMLDisplay.css\' type=\'text/css\' rel=\'stylesheet\'>"+
						" <script type=\'text/javascript\' src=\'XMLDisplay.jsp\'></script>"+
						" <script>LoadXML(\'XMLHolder\',\'"+webFileStr+"\'); </script>";
	
						session.setAttribute("file_content", xmlView);
						session.setAttribute("file_name", webFileStr);
					}
					else
					{
						//session.setAttribute("file_content", "<iframe width=\"100%\" height=\"100%\" src=\""+queryView+"\">Sorry, but your browser does not support iframes.</iframe>");
						session.setAttribute("file_content", "Only valid XML files are accessible from the Tasks folder.");
					}
				}
			}
		}
		else session.setAttribute("file_content", "Click on a file to view or download.");
		setMessage("Welcome to actionhome ascasc");
                System.out.println("\n\n\n\\n\n\n\n\\n\nn\n\n\n\\n\n\n YOU are HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//		String adminPage = "/admin.jsp";
//		RequestDispatcher rd = getServletContext().getRequestDispatcher(adminPage);
//		rd.forward(request, response);
        
        return Result.SUCCESS;
    }
//    private String fileName1;
//    private String fileName2;
//
//    public String getFileName1() {
//        return fileName1;
//    }
//
//    public void setFileName1(String fileName1) {
//        this.fileName1 = fileName1;
//    }
//
//    public String getFileName2() {
//        return fileName2;
//    }
//
//    public void setFileName2(String fileName2) {
//        this.fileName2 = fileName2;
//    }
//
//    
//    
//    private Map<String, Object> session;
//    @Override
//    public void setSession(Map<String, Object> map) {
//        this.session = map;
//        this.session.put("Access-Control-Allow-Origin", "*");
//    }
//
//     public String execute() throws UnsupportedEncodingException {
//         
//         // connect to database and check if user are valid or not
//         // if authenticated sucess
//         this.fileName1 = "a.xml";
//         this.fileName1 = "b.xml";
//         return Result.SUCCESS;
//         
//         //if fail stay at same page
//         //return Result.INPUT;
//         
//     }
    
}
