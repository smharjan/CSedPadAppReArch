/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.authoring.auth.dashboard;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import memphis.deeptutor.model.Student;
//import memphis.deeptutor.singleton.DerbyConnector;

/**
 *
 * @author suraj
 */
public class AdminDerbyConnector {
    
    private static String dbURL = "jdbc:derby://localhost:1527/Csedpad";
    private Connection conn = null;
    private Statement stmt = null;

    private static AdminDerbyConnector instance = null;
    
    public static AdminDerbyConnector getInstance()
    {
            // make sure we have an open connection to the database
            if (instance == null)
                    instance = new AdminDerbyConnector();

            instance.createConnection();

            return instance;
    }
    
    public void createConnection()
    {
            try
            {
                    if (conn != null && !conn.isClosed())
                            return;

                    Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
                    // Get a connection
                    conn = DriverManager.getConnection(dbURL,"deeptutor","spring2013");
            }
            catch (Exception except)
            {
                    except.printStackTrace();
            }
    }
    
    public String getStudents(
			@SuppressWarnings("rawtypes") Collection alreadyConnected)
	{
		String result = "";

		try
		{
			stmt = conn.createStatement();
			String query = "select studentId from Student";
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
    
    public void getStudentEvaluation(Student s, String evaluationId)
    {
        try
        {
                stmt = conn.createStatement();

                String query = "select * from Evaluation where id="
                                + s.getGivenId() + " and evaluationId='" + evaluationId
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
}
