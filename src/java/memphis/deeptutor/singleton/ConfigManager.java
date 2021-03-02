package memphis.deeptutor.singleton;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.servlet.ServletContext;

public class ConfigManager
{

	static Properties properties = null;
	static String webResourcePath = null;

	// This assumes we are in a Flex environment
	public static String GetResourcePath()
	{
		// ServletContext sc = FlexContext.getServletContext();

		if (properties == null)
			LoadProperties(null);
		return webResourcePath;
	}

	public static boolean LoadProperties(ServletContext sc)
	{
		if (properties == null)
		{
			if (sc == null)
				webResourcePath = "D:\\Summer 2020\\csedpad-lasang\\CSedPadAppReArch\\web\\DTResources\\";
			else
				webResourcePath = sc.getRealPath("/DTResources") + "\\";
                                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+webResourcePath);

			String filename = webResourcePath + "DTConfig.prop";
			FileInputStream in;
			try
			{
				in = new FileInputStream(filename);
				properties = System.getProperties();
				properties.load(in);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public static String GetDataPath()
	{
		if (properties == null)
			LoadProperties(null);
		return properties.getProperty("deeptutor.datapath");
	}

	// Dan:
	public static String GetDefinitionsPath()
	{
		return GetDataPath() + "dict\\definitions";
	}

	public static String GetLogPath()
	{

		String datapath = GetDataPath();

		String logPath = datapath + "Logs";
		java.io.File logDir = new File(logPath);
		if (!logDir.exists())
			logDir.mkdir();

		return logPath + "\\";
	}

	public static String GetMediaWebPath()
	{
		return "../DTResources/Media/";
	}

	public static String GetMediaPath()
	{

		String datapath = GetDataPath();

		String mediaPath = datapath + "Media";
		java.io.File mediaDir = new File(mediaPath);
		if (!mediaDir.exists())
			mediaDir.mkdir();

		return mediaPath + "\\";
	}

	public static String GetTasksPath()
	{

		String datapath = GetDataPath();
                System.out.println("datapath>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>)777777777777777777"+datapath);

		String logPath = datapath + "Tasks";
                System.out.println("datapath>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>)777777777777777777"+logPath);
		java.io.File logDir = new File(logPath);
		if (!logDir.exists())
			logDir.mkdir();

		return logPath + "\\";
	}

	public static String GetEditedTasksPath()
	{

		String datapath = GetDataPath();

		String logPath = datapath + "EditedTasks";
		java.io.File logDir = new File(logPath);
		if (!logDir.exists())
			logDir.mkdir();

		return logPath + "\\";
	}

	/*
	 * Gives the folder containing the tasks, that user will see the answers of
	 * them.
	 */
	public static String GetShowAnswerTasksPath()
	{

		String dataPath = GetDataPath();

		dataPath = dataPath + "ShowAnswerTasks";
		java.io.File logDir = new File(dataPath);
		if (!logDir.exists())
			logDir.mkdir();

		return dataPath + "\\";
	}

	public static String GetTaskFileName(String taskID, boolean isShowanswers)
	{
		if (isShowanswers)
			return taskID + ".html";
		return taskID + ".xml";
	}
	
	public static String GetTaskFileName(String taskID)
	{
		return taskID + ".xml";
	}
}
