package memphis.deeptutor.servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * Servlet implementation class TasksManager
 */
@WebServlet("/TasksManager")
public class XMLTaskManager extends HttpServlet {
	private String DESTINATION_DIR_PATH = "/DTResources";
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public XMLTaskManager() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);

	}

	private void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

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
		
		String adminPage = "/admin.jsp";
		RequestDispatcher rd = getServletContext().getRequestDispatcher(adminPage);
		rd.forward(request, response);
	}

}
