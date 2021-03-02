package memphis.deeptutor.servlets;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import memphis.deeptutor.singleton.ConfigManager;

public class ExperimentAssistant extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		processRequest(request, response);
	}

	void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		String editedTasksPath = ConfigManager.GetEditedTasksPath();
		String tasksPath = ConfigManager.GetTasksPath();
		System.out.println("The edited path is:" + editedTasksPath);
		System.out.println("The task path is:" + tasksPath);

		HttpSession session = request.getSession();

		String gotoFCI = "/ExperimentAssistant.jsp";
		String action = request.getParameter("action");
		System.out.println("The action is:" + action);
		if (action != null && action.trim().startsWith("transfer"))
		{
			Enumeration<String> keys = request.getParameterNames();
			while (keys.hasMoreElements())
			{
				String param = keys.nextElement();
				String value = request.getParameter(param);
				if (param.matches("f\\d+"))
				{
					try
					{
						value = value.replaceAll("\\[.*\\]", "").trim();
						// Copy file name value from edited task to Tasks
						// we need to copy the file from the real folder to a
						// web accessible folder
						File eFile = new File(editedTasksPath + value);
						File tFile = new File(tasksPath + value);
						System.out.print("Trying to copy:"
								+ eFile.getAbsolutePath() + " to"
								+ tFile.getAbsolutePath());
						org.apache.commons.io.FileUtils.copyFile(eFile, tFile,
								true);
						session.setAttribute("status",
								"TRANSFER is completed !");

					}
					catch (Exception e)
					{
						session.setAttribute("status",
								"Failed:" + e.getMessage());
					}
				}
			}
		}

		File eDir = new File(editedTasksPath);
		File tDir = new File(tasksPath);
		File[] eFiles = eDir.listFiles();
		File[] tFiles = tDir.listFiles();
		Map<String, File> tFileNames = new HashMap<String, File>();

		for (File f : tFiles)
		{
			tFileNames.put(f.getName(), f);
		}
		String[][] taskInfo = new String[eFiles.length][3];
		int i = 0;
		for (File f : eFiles)
		{
			Date eD = new Date(f.lastModified());
			taskInfo[i][0] = f.getName() + " [LM: " + eD.toString() + "]";
			taskInfo[i][1] = "";
			taskInfo[i][2] = "";

			if (tFileNames.containsKey(f.getName()))
			{

				// System.out.println("Current path:"+f.getName());
				Date tD = new Date(tFileNames.get(f.getName()).lastModified());
				taskInfo[i][1] = f.getName() + " [LM: " + tD.toString() + "]";
				// System.out.println(taskInfo[i][0]);
				if (f.getName().equals("LP02_PR00.xml"))
				{
					System.out.println("eD:" + eD + ", tD:" + tD
							+ eD.before(tD));
				}
				if (eD.after(tD))
				{
					taskInfo[i][2] = "GREEN1";
				}
				else if (eD.before(tD))
				{
					taskInfo[i][2] = "RED";
				}
				else
				{
					taskInfo[i][2] = "";
				}
			}
			else
			{
				taskInfo[i][2] = "GREEN2";
			}
			i++;

		}
		session.setAttribute("expAssistantTasks", taskInfo);

		RequestDispatcher rd = getServletContext()
				.getRequestDispatcher(gotoFCI);
		rd.forward(request, response);

	}
}
