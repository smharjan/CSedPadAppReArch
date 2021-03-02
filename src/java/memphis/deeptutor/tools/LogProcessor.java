package memphis.deeptutor.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.joda.time.DateTime;

public class LogProcessor
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		LogProcessor lp = new LogProcessor();
		lp.process(null);

	}

	// Dan: reading pretest results for a certain user
	public static Hashtable<String, String> readPretestForStudent(
			String logDir, String studentID) throws IOException
	{
		Hashtable<String, String> ret = new Hashtable<String, String>();

		DateFormat df = new SimpleDateFormat("MMddyy");
		Date dt = null;
		File firstFile = null;

		// Dan: find the first log file containing the pretest
		File folder = new File(logDir);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++)
		{
			String fileName = listOfFiles[i].getName().substring(4);

			if (listOfFiles[i].isFile() && fileName.startsWith(studentID))
			{
				String dateStr = fileName.substring(studentID.length() + 1,
						studentID.length() + 7);
				try
				{
					Date date = df.parse(dateStr);
					if (dt == null || date.before(dt))
					{
						dt = date;
						firstFile = listOfFiles[i];
					}
				}
				catch (ParseException e)
				{
					System.out.println(e.getMessage());
				}
			}
		}

		// Dan: read the pretest data
		if (firstFile != null)
		{
			Pattern pretestPattern = Pattern.compile("pretest--\\S+");

			FileInputStream fstream = new FileInputStream(firstFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = null;
			while ((strLine = br.readLine()) != null)
			{
				Matcher matcher = pretestPattern.matcher(strLine);
				if (matcher.find())
				{
					String[] matched = matcher.group().split("--");
					String questionId = matched[1];
					String answer = matched[2];
					ret.put(questionId, answer);
					// String context = matched[4];
					// String explanation = matched[5];
				}
				else if (strLine.contains("FCI QUESTION FILE USED:"))
				{
					break;
				}
			}

			br.close();
			in.close();
			fstream.close();
		}

		return ret;
	}

	public boolean process(String logDir)
	{

		String fileName = "";
		String path = "C:\\Users\\Rajendra\\Desktop\\Log-Files-1228";
		String outputFile = "C:\\Users\\Rajendra\\Desktop\\Log-Files-1228\\Output\\log-";

		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		File folder = null;

		int counter = 0;
		int fileNumber = 0;
		ArrayList<String> lines = new ArrayList<String>();
		try
		{
			folder = new File(path);
			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++)
			{

				if (!listOfFiles[i].isFile())
					continue;

				String fileNameOnly = listOfFiles[i].getName();
				fileName = listOfFiles[i].getAbsolutePath();

				fstream = new FileInputStream(fileName);
				in = new DataInputStream(fstream);
				br = new BufferedReader(new InputStreamReader(in));
				String strLine = null;
				String query = null;

				// get the user id
				String studentId = fileNameOnly.split("-")[1];
				lines.add("delete from APP.EVALUATION where givenId='"
						+ studentId + "';");

				while ((strLine = br.readLine()) != null)
				{

					// pretest--1--C--pdtl37--1--null
					// "pretest"+"--"+ q+"--"+ a +"--"+studentId+"--"+
					// nextContextId+"--"+ explanation;
					if (strLine.contains("pretest--"))
					{

						String[] splits = strLine.split("-");

						query = "insert into App.Evaluation (givenId, evaluationId, contextId, questionId, answer, explanation) values "
								+ "('"
								+ splits[6]
								+ "', '"
								+ "pretest"
								+ "', '" + splits[8] // context
								+ "', '" + splits[2] // question
								+ "', '" + splits[4] + "', '" + "null" + "');";

						lines.add(query.trim());
						counter++;
					}
					else if (strLine.contains("posttest--"))
					{
						String[] splits = strLine.split("-");
						query = "insert into App.Evaluation (givenId, evaluationId, contextId, questionId, answer, explanation) values "
								+ "('"
								+ splits[6]
								+ "', '"
								+ "posttest"
								+ "', '" + splits[8] // context
								+ "', '" + splits[2] // question
								+ "', '" + splits[4] + "', '" + "null" + "');";

						lines.add(query.trim());
						counter++;
					}
				} // file ends
				br.close();
				in.close();
				if (counter < 601)
					continue;
				writeToFile(lines, outputFile + fileNumber + ".sql");
				lines = new ArrayList<String>();
				counter = 0;
				fileNumber++;

			}

			if (lines.size() > 0)
			{
				System.out.println("Writing remaining logs..");
				writeToFile(lines, outputFile + fileNumber + ".sql");
			}

		}
		catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
		return true;
	}

	public boolean writeToFile(ArrayList<String> lines, String fileName)
	{

		try
		{
			// Create file
			FileWriter fstream = new FileWriter(fileName);
			BufferedWriter out = new BufferedWriter(fstream);
			for (String line : lines)
			{
				out.write(line + "\r\n");
			}
			// Close the output stream
			out.close();
			fstream.close();
		}
		catch (Exception e)
		{// Catch exception if any
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
		return true;
	}

}
