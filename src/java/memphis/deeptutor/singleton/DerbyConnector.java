package memphis.deeptutor.singleton;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
//import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
//import java.util.HashSet;
import java.util.Date;

import memphis.deeptutor.model.BusinessModel.DTMode;
import memphis.deeptutor.model.BusinessModel.DTState;
import memphis.deeptutor.model.Student;
import memphis.deeptutor.model.TodoItem;

//drop table restaurants
//create table restaurants(id int primary key,name varchar(20), city varchar (20))

public class DerbyConnector
{
	// Dan: Derby connector creates a dataBase if non existent: potential bug.
	// private static String dbURL =
	// "jdbc:derby://localhost:1527/DeeptutorDB;create=true";//
	// "jdbc:derby://localhost:1527/myDB;create=true;user=me;password=mine";
	private static String dbURL = "jdbc:derby://localhost:1527/DeeptutorDB";

	// private static String tableName = "Student";
	// jdbc Connection
	private Connection conn = null;
	private Statement stmt = null;

	private static DerbyConnector instance = null;

	public static DerbyConnector getInstance()
	{
		// make sure we have an open connection to the database
		if (instance == null)
			instance = new DerbyConnector();

		instance.createConnection();

		return instance;
	}

	public static void main(String[] args)
	{
		// createConnection();
		// shutdown();
	}

	public void createConnection()
	{
		try
		{
			if (conn != null && !conn.isClosed())
				return;

			Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
			// Get a connection
			conn = DriverManager.getConnection(dbURL);
		}
		catch (Exception except)
		{
			except.printStackTrace();
		}
	}

	/*
	 * public static void updateStudent(Student s){
	 * 
	 * try { stmt = conn.createStatement(); stmt.execute("update Student set " +
	 * "age='"+s.getAge() +"'," + "gpa='"+s.getGpa() +"'," +
	 * "major='"+s.getMajor() +"'," + "currentcourses='"+s.getCurrentCourses()
	 * +"'," + "priorcourses='"+s.getPriorCourses() +"'," +
	 * "educationlevel='"+s.getEducationLevel() +"'," +
	 * "ethnicity='"+s.getEthnicity() +"'," +
	 * "familiarareas='"+s.getFamiliarAreas() +"'," +
	 * "hasacceptedtermsandconditions='"+s.hasAcceptedTermsAndConditions() +"',"
	 * + "isspecialstudent='"+s.isSpecialStudent() +"'," +
	 * "isvalidstudent='"+s.isValidStudent() +"' "+
	 * " Where givenid='"+s.getGivenId()+"')"); stmt.close(); } catch
	 * (SQLException sqlExcept) { sqlExcept.printStackTrace(); } }
	 */

	public synchronized void updateDemographics(Student s)
	{

		try
		{
			stmt = conn.createStatement();

			stmt.execute("delete from Demographics where givenId='"
					+ s.getGivenId() + "'");

			stmt.execute("insert into Demographics (givenId, gender, ethnicity, school, age, major, education, gpa, priorCourses, currentCourses, familiarAreas) values "
					+ "('"
					+ s.getGivenId()
					+ "', "
					+ "'"
					+ s.getGender()
					+ "', "
					+ "'"
					+ s.getEthnicity()
					+ "', "
					+ "'"
					+ s.getSchool()
					+ "', "
					+ s.getAge()
					+ ", "
					+ "'"
					+ ((s.getMajor() != "") ? s.getMajor() : s
							.getMostAdvancedClass())
					+ "', "
					+ "'"
					+ s.getEducationLevel()
					+ "', "
					+ "'"
					+ s.getGpa()
					+ "', "
					+ "'"
					+ s.getPriorCourses()
					+ "', "
					+ "'"
					+ s.getCurrentCourses()
					+ "', "
					+ "'"
					+ s.getFamiliarAreas() + "')");
			stmt.close();

		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	public boolean checkDemographics(Student s)
	{
		Boolean hasDemographics = false;
		try
		{
			stmt = conn.createStatement();

			String query = "select * from Demographics where givenId='"
					+ s.getGivenId() + "'";
			ResultSet results = stmt.executeQuery(query);
			hasDemographics = results.next();
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
		return hasDemographics;
	}

	public boolean checkHasSeenTutorial(Student s)
	{
		Boolean hasSeen = false;
		try
		{
			stmt = conn.createStatement();

			String query = "select * from Student where givenId='"
					+ s.getGivenId() + "'";
			ResultSet results = stmt.executeQuery(query);
			while (results.next())
			{
				hasSeen = (results.getInt("hasSeenTutorial") == 1);
			}
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
		return hasSeen;
	}

	public synchronized boolean setHasSeenTutorial(Student s)
	{
		Boolean hasSeen = false;
		try
		{
			stmt = conn.createStatement();
			stmt.execute("update student set hasSeenTutorial = 1 where givenId='"
					+ s.getGivenId() + "'");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
		return hasSeen;
	}

	public void getStudentEvaluation(Student s, String evaluationId)
	{
		try
		{
			stmt = conn.createStatement();

			String query = "select * from Evaluation where givenId='"
					+ s.getGivenId() + "' and evaluationId='" + evaluationId
					+ "'";
			ResultSet results = stmt.executeQuery(query);
			Hashtable<String, String> data = new Hashtable<String, String>();
			HashSet<String> context = new HashSet<String>();

			while (results.next())
			{
				String questionId = results.getString("questionId");
				String answer = results.getString("answer").toUpperCase();
				data.put(questionId, answer);
				String contextdata = results.getString("contextId");
				context.add(contextdata);
			}

			s.evaluationData = data;
			s.evaluationContext = context;
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	// public void saveStudentEvaluation(String studentId, String evaluationId,
	// String contextId, String questionId, String answer,
	// String explanation) {
	// try {
	// stmt = conn.createStatement();
	//
	// stmt.execute("delete from Evaluation where givenId='" + studentId
	// + "' and evaluationId='" + evaluationId
	// + "' and questionId='" + questionId + "'");
	//
	// String query =
	// "insert into Evaluation (givenId, evaluationId, contextId, questionId, answer, explanation) values "
	// + "('"
	// + studentId
	// + "', '"
	// + evaluationId
	// + "', '"
	// + contextId
	// + "', '"
	// + questionId
	// + "', '"
	// + answer
	// + "', '" + explanation + "')";
	// stmt.execute(query);
	// stmt.close();
	// } catch (SQLException sqlExcept) {
	// sqlExcept.printStackTrace();
	// }
	// }

	/**
	 * 
	 * @param studentId
	 * @param evaluationId
	 * @param contextId
	 * @param questionId
	 * @param answer
	 * @param explanation
	 */
	public synchronized void insertStudentEvaluation(String studentId, String evaluationId,
			String contextId, String questionId, String answer,
			String explanation)
	{
		try
		{
			stmt = conn.createStatement();
			String query = "insert into Evaluation (givenId, evaluationId, contextId, questionId, answer, explanation) values "
					+ "('"
					+ studentId
					+ "', '"
					+ evaluationId
					+ "', '"
					+ contextId
					+ "', '"
					+ questionId
					+ "', '"
					+ answer
					+ "', '" + explanation + "')";
			stmt.executeUpdate(query);
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	public void getStudentLearningModel(Student s)
	{
		try
		{
			stmt = conn.createStatement();

			String query = "select * from LearningModel where givenId='"
					+ s.getGivenId() + "'";
			ResultSet results = stmt.executeQuery(query);

			while (results.next())
			{
				if (results.getString("completedTasks") != null)
					s.completedTasks = results.getString("completedTasks");
				if (results.getString("currentTask") != null)
					s.currentTask = results.getString("currentTask");
			}
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	public synchronized void saveStudentLearningModel(Student s)
	{
		try
		{
			stmt = conn.createStatement();

			stmt.execute("delete from LearningModel where givenId='"
					+ s.getGivenId() + "'");

			String query = "insert into LearningModel (givenId, completedTasks, currentTask) values "
					+ "('"
					+ s.getGivenId()
					+ "', '"
					+ s.completedTasks
					+ "', '" + s.currentTask + "')";
			stmt.execute(query);
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	public synchronized void saveStudentTermsAndAgreements(String givenId)
	{
		try
		{
			stmt = conn.createStatement();
			stmt.execute("update student set hasAcceptedTermsAndConditions = 1 where givenId='"
					+ givenId + "'");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}
	
	public void saveStudentTermsAndAgreements(String givenId, boolean forAllStudentEntries)
	{
		if (forAllStudentEntries)
		{
			givenId = givenId.substring(0, givenId.length() - 1);
			for (int i = 0; i < 5; i++)
			{
				saveStudentTermsAndAgreements(givenId + i);
			}
		}
		else
		{
			saveStudentTermsAndAgreements(givenId);
		}
	}

	public synchronized void saveStudentPreTestScore(String givenId, double score)
	{
		try
		{
			stmt = conn.createStatement();
			stmt.execute("update student set PRETESTSCORE = " + score
					+ " where givenId='" + givenId + "'");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	public void saveStudentPreTestScore(String givenId, double score,
			boolean forAllStudentEntries)
	{
		if (forAllStudentEntries)
		{
			givenId = givenId.substring(0, givenId.length() - 1);
			for (int i = 0; i < 5; i++)
			{
				saveStudentPreTestScore(givenId + i, score);
			}
		}
		else
		{
			saveStudentPreTestScore(givenId, score);
		}
	}

	public synchronized void saveStudentTMode(String givenId, DTMode d)
	{
		try
		{
			stmt = conn.createStatement();
			stmt.execute("update student set DTMODE  = " + d.toString()
					+ " where givenId='" + givenId + "'");
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
	}

	/*
	 * public static Set<String> getStudentIds() { Set<String> ids = new
	 * HashSet<String>(); try { stmt = conn.createStatement(); ResultSet results
	 * = stmt.executeQuery("select * from Student"); //ResultSetMetaData rsmd =
	 * results.getMetaData(); //int numberCols = rsmd.getColumnCount(); while
	 * (results.next()) { String id = results.getString(1); ids.add(id); }
	 * results.close(); stmt.close(); } catch (SQLException sqlExcept) {
	 * sqlExcept.printStackTrace(); } return ids;
	 * 
	 * }
	 */

	public Student findStudent(String givenId)
	{
		Student s = null;
		try
		{
			stmt = conn.createStatement();
			String query = "select * from Student where givenId='" + givenId
					+ "'";
			ResultSet results = stmt.executeQuery(query);
			// ResultSetMetaData rsmd = results.getMetaData();
			// int numberCols = rsmd.getColumnCount();
			// for (int i = 1; i <= numberCols; i++) {
			// String colName = rsmd.getColumnLabel(i);
			// System.out.println(i+" "+colName);
			// }

			while (results.next())
			{
				s = new Student(givenId);
				s.setPassword(results.getString("password"));
				s.setHasAcceptedTermsAndConditions(results
						.getInt("hasAcceptedTermsAndConditions") == 1);
				s.wait4woz = (results.getInt("wait4woz") == 1);
				String isSpecialStudent = results.getString("isspecialstudent");
				s.setSpecialStudent(false);
				try
				{
					if (Integer.parseInt(isSpecialStudent) == 1)
					{
						s.setSpecialStudent(true);
					}
				}
				catch (Exception e)
				{
				}
				s.setPostTest(results.getString("PRETEST"));
				s.setPreTest(results.getString("POSTTEST"));
				s.setPreTestScore(results.getDouble("PRETESTSCORE"));

				String temp = "";
				try
				{
					temp = results.getString("dtstate").toUpperCase();
					s.setDTState(DTState.valueOf(temp));
				}
				catch (Exception e)
				{
					s.setDTState(null);
				}
				try
				{
					temp = results.getString("DTMODE").toUpperCase();
					s.setDtMode(DTMode.valueOf(temp));
				}
				catch (Exception e)
				{
					s.setDtMode(null);
				}
			}

			results.close();
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}

		return s;

	}

	public Student findStudentInDispatcher(String givenId)
	{
		Student s = null;
		try
		{
			stmt = conn.createStatement();
			String query = "select * from Dispatcher where givenId='" + givenId
					+ "'";
			ResultSet results = stmt.executeQuery(query);

			while (results.next())
			{
				s = new Student(givenId);
				s.setPassword(results.getString("password"));
				s.setSession_0_start(results.getDate("session_0_start"));
				s.setSession_0_end(results.getDate("session_0_end"));
				s.setSession_1_start(results.getDate("session_1_start"));
				s.setSession_1_end(results.getDate("session_1_end"));
				s.setSession_2_start(results.getDate("session_2_start"));
				s.setSession_2_end(results.getDate("session_2_end"));
				s.setSession_3_start(results.getDate("session_3_start"));
				s.setSession_3_end(results.getDate("session_3_end"));
				s.setSession_4_start(results.getDate("session_4_start"));
				s.setSession_4_end(results.getDate("session_4_end"));

				if (results.getTimestamp("FINISHED_0") != null)
					s.setFinishedDate0(results.getTimestamp("FINISHED_0"));
				if (results.getTimestamp("FINISHED_1") != null)
					s.setFinishedDate1(results.getTimestamp("FINISHED_1"));
				if (results.getTimestamp("FINISHED_2") != null)
					s.setFinishedDate2(results.getTimestamp("FINISHED_2"));
				if (results.getTimestamp("FINISHED_3") != null)
					s.setFinishedDate3(results.getTimestamp("FINISHED_3"));
				if (results.getTimestamp("FINISHED_4") != null)
					s.setFinishedDate4(results.getTimestamp("FINISHED_4"));

				if (results.getTimestamp("lastlogin") != null)
					s.setLastLogInDate(results.getTimestamp("lastlogin"));
			}

			results.close();
			stmt.close();
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}

		return s;

	}

	public String getStudents(
			@SuppressWarnings("rawtypes") Collection alreadyConnected)
	{
		String result = "";

		try
		{
			stmt = conn.createStatement();
			String query = "select givenId from Student";
			ResultSet results = stmt.executeQuery(query);
			while (results.next())
			{
				String myID = results.getString(1);
				if (!alreadyConnected.contains(myID))
					result += " " + myID;
			}
			return result.trim();

		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}

		return result;
	}

	public TodoItem[] GetTodoItems()
	{
		ArrayList<TodoItem> todoItems = new ArrayList<TodoItem>();

		try
		{
			stmt = conn.createStatement();
			String query = "select todoID, creator, assignee, text, response, dateCreated, dateClosed from TodoItems where dateClosed is null order by dateCreated desc";
			ResultSet results = stmt.executeQuery(query);
			while (results.next())
			{
				TodoItem item = new TodoItem();
				item.setTodoID(results.getInt(1));
				item.setCreator(results.getString(2));
				item.setAssignee(results.getString(3));
				item.setText(results.getString(4).replaceAll("`", "'"));
				item.setResponse(results.getString(5).replaceAll("`", "'"));
				item.setDateCreated(results.getTimestamp(6));
				item.setDateClosed(results.getTimestamp(7));

				todoItems.add(item);
			}

		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}

		TodoItem[] result = new TodoItem[todoItems.size()];
		return todoItems.toArray(result);
	}

	public int CreateTodoItem(TodoItem item)
	{
		// delete, if exists and recreate again
		try
		{
			stmt = conn.createStatement();
			int taskID = 1;

			ResultSet results = stmt
					.executeQuery("select max(todoID) from TodoItems");
			if (results.next())
			{
				taskID = results.getInt(1) + 1;
			}

			String query = "insert into TodoItems (todoId, creator, assignee, text, response, datecreated, dateclosed) values "
					+ "('"
					+ taskID
					+ "', '"
					+ item.getCreator()
					+ "', '"
					+ item.getAssignee()
					+ "','"
					+ item.getText()
					+ "', '', '"
					+ new Timestamp((new Date()).getTime()) + "', null)";
			stmt.execute(query);
			stmt.close();
			return taskID;

		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}

		return -1;
	}

	public synchronized boolean UpdateTodoItem(int itemID, String response)
	{
		// delete, if exists and recreate again
		try
		{
			stmt = conn.createStatement();
			stmt.execute("update TodoItems set response = '" + response
					+ "' where todoId=" + itemID);
			stmt.close();
			return true;

		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}

		return false;
	}

	public boolean CloseTodoItem(int itemID, String creator)
	{
		// delete, if exists and recreate again
		try
		{
			stmt = conn.createStatement();
			System.out.println("Closing: " + itemID + " created by = "
					+ creator);
			ResultSet results = stmt
					.executeQuery("select creator from TodoItems where todoID = "
							+ itemID);
			if (results.next())
			{
				String dbcreator = results.getString(1);
				if (dbcreator.equals(creator))
				{
					System.out.println("Found.");
					stmt.execute("update TodoItems set dateClosed = '"
							+ new Timestamp((new Date()).getTime())
							+ "' where todoId=" + itemID);
					stmt.close();
					return true;
				}
			}

			stmt.close();
			return false;

		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}

		return false;
	}

	public void shutdown()
	{
		try
		{
			if (stmt != null)
			{
				stmt.close();
			}
			if (conn != null)
			{
				DriverManager.getConnection(dbURL + ";shutdown=true");
				conn.close();
			}
		}
		catch (SQLException sqlExcept)
		{

		}
	}

	public synchronized boolean setStudentLastLogIn(Student student)
	{
		try
		{
			stmt = conn.createStatement();

			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date now = new Date();

			String query = "UPDATE APP.DISPATCHER SET LASTLOGIN = '"
					+ df.format(now) + "' WHERE givenid='"
					+ student.getGivenId() + "'";
			System.out.println(query);
			stmt.execute(query);
			stmt.close();
			return true;
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
		return false;
	}

	public synchronized boolean setStudentState(Student student, DTState state)
	{
		try
		{
			stmt = conn.createStatement();
			String query = "UPDATE student SET dtstate = '" + state
					+ "' WHERE givenid='" + student.getGivenId() + "'";
			//System.out.println(query);
			stmt.execute(query);

			if (state == DTState.FINISHED)
			{
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date now = new Date();
				String session = student.getGivenId().substring(
						student.getGivenId().length() - 1);

				query = "UPDATE APP.DISPATCHER SET FINISHED_"
						+ session + " = '" + df.format(now)
						+ "' WHERE givenid='" + student.getGivenId().substring(0, student.getGivenId().length() - 2) + "'";
				System.out.println(query);
				stmt.execute(query);
			}
			stmt.close();
			return true;
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
		return false;
	}

	public synchronized boolean setStudentMode(Student student, DTMode mode)
	{
		try
		{
			stmt = conn.createStatement();
			stmt.execute("UPDATE student SET dtmode = '" + mode
					+ "' WHERE givenid='" + student.getGivenId() + "'");
			stmt.close();
			return true;
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
		return false;
	}

	public synchronized boolean setCounter(String counterName, int value)
	{
		try
		{
			stmt = conn.createStatement();
			String query = "UPDATE APP.counter SET " + counterName + "="
					+ value;
			return stmt.execute(query);
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
		return false;
	}

	public int getCounter(String counterName)
	{
		int c1 = 0;
		try
		{
			stmt = conn.createStatement();
			String query = "select * from counter";
			ResultSet results = stmt.executeQuery(query);
			try
			{
				results.next();
				c1 = Integer.parseInt(results.getString(counterName).trim());
			}
			catch (Exception e)
			{
			}
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
		return c1;
	}

	public synchronized boolean assignTests(Student s, String preTest, String postTest)
	{
		try
		{
			stmt = conn.createStatement();
			String query = "UPDATE student SET pretest='" + preTest
					+ "', posttest='" + postTest + "' WHERE givenid='"
					+ s.getGivenId() + "'";
			return stmt.execute(query);
		}
		catch (SQLException sqlExcept)
		{
			sqlExcept.printStackTrace();
		}
		return false;
	}
}