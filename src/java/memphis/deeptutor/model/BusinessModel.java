package memphis.deeptutor.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import memphis.deeptutor.log.DTLogger;
import memphis.deeptutor.servlets.DTConstants.Status;
import memphis.deeptutor.singleton.ConfigManager;
import memphis.deeptutor.singleton.DerbyConnector;

public class BusinessModel
{

	public enum DTMode
	{
		INTERACTIVE, SHOWANSWERS, ADAPTIVE
	};

	public enum DTState
	{
		PRETEST, DIALOGUE, MPOSTTEST, POSTTEST, DEMOGPHY, FINISHED, ISURVEY, OPINIONS
	};

	/**
	 * the Singleton instance
	 */
	private static BusinessModel instance = new BusinessModel();

	/**
	 * This EntityManagerFactory provides EntityManagers to control persistence
	 * using JPA 2.0
	 */
	// @PersistenceUnit
	// private EntityManagerFactory emf;

	/**
	 * The constructor of the class is private to implement Singleton pattern.
	 */
	public BusinessModel()
	{
		// emf = Persistence.createEntityManagerFactory("DeeptutorApp");
	}

	/**
	 * To get the Singleton instance
	 * 
	 * @return the Singleton instance
	 */
	public static BusinessModel getInstance()
	{
		return instance;
	}

	/**
	 * to get a EntityManager from the factory.
	 * 
	 * @return a EntityManager from the factory.
	 */
	// public EntityManager getEntityManager() {
	// return emf.createEntityManager();
	// }

	public Student getStudentFromDatabase(Student s)
	{
		Student s1 = DerbyConnector.getInstance().findStudent(s.getGivenId());
		if (s1 != null && s.getPassword().equals(s1.getPassword()))
			return s1;
		else
			return null;
	}

	public Student getStudentThroughDispatcher(Student s)
	{
		DerbyConnector dc = DerbyConnector.getInstance();
		Student s1 = dc.findStudentInDispatcher(s.getGivenId());
		if (s1 != null && s.getPassword().equals(s1.getPassword()))
		{
			dc.setStudentLastLogIn(s1);

			Date now = new Date();
			int hourOfTheDay = now.getHours();
			now = new Date(now.getYear(), now.getMonth(), now.getDate());

			if (hourOfTheDay < 3)
			{
				Calendar cal = Calendar.getInstance();
				cal.setTime(now);
				cal.add(Calendar.DAY_OF_MONTH, -1);
				now = cal.getTime();
			}

			Student s2 = null;
			if (now.compareTo(s1.getSession_0_end()) <= 0 || s1.getFinishedDate0() == null)
			{
				s2 = DerbyConnector.getInstance().findStudent(
						s1.getGivenId() + "_0");
				s2.setSessionNumber(0);
			}
			else if ((now.compareTo(s1.getSession_1_start()) >= 0
					&& now.compareTo(s1.getSession_1_end()) <= 0) || s1.getFinishedDate1() == null)
			{
				s2 = DerbyConnector.getInstance().findStudent(
						s1.getGivenId() + "_1");
				s2.setSessionNumber(1);
			}
			else if ((now.compareTo(s1.getSession_2_start()) >= 0
					&& now.compareTo(s1.getSession_2_end()) <= 0) || s1.getFinishedDate2() == null)
			{
				s2 = DerbyConnector.getInstance().findStudent(
						s1.getGivenId() + "_2");
				s2.setSessionNumber(2);
			}
			else if ((now.compareTo(s1.getSession_3_start()) >= 0
					&& now.compareTo(s1.getSession_3_end()) <= 0) || s1.getFinishedDate3() == null)
			{
				s2 = DerbyConnector.getInstance().findStudent(
						s1.getGivenId() + "_3");
				s2.setSessionNumber(3);
			}
			else if (now.compareTo(s1.getSession_4_start()) >= 0 || s1.getFinishedDate4() == null)
			{
				s2 = DerbyConnector.getInstance().findStudent(
						s1.getGivenId() + "_4");
				s2.setSessionNumber(4);
			}

			if (s2 != null)
			{
				s2.setSession_0_start(s1.getSession_0_start());
				s2.setSession_1_start(s1.getSession_1_start());
				s2.setSession_2_start(s1.getSession_2_start());
				s2.setSession_3_start(s1.getSession_3_start());
				s2.setSession_4_start(s1.getSession_4_start());

				s2.setSession_0_end(s1.getSession_0_end());
				s2.setSession_1_end(s1.getSession_1_end());
				s2.setSession_2_end(s1.getSession_2_end());
				s2.setSession_3_end(s1.getSession_3_end());
				s2.setSession_4_end(s1.getSession_4_end());

				s2.setFinishedDate0(s1.getFinishedDate0());
				s2.setFinishedDate1(s1.getFinishedDate1());
				s2.setFinishedDate2(s1.getFinishedDate2());
				s2.setFinishedDate3(s1.getFinishedDate3());
				s2.setFinishedDate4(s1.getFinishedDate4());

				s2.setLastLogInDate(s1.getLastLogInDate());
				s2.setComesThroughDispatcher(true);

				loadSequenceOfTasks(ConfigManager.GetDataPath()
						+ "experimentConfig.xml", s2);
			}

			return s2;
		}
		else if (s1 == null)
		{
			s1 = getStudentFromDatabase(s);
			if (s1 != null)
			{
				loadSequenceOfTasks(ConfigManager.GetDataPath()
						+ "experimentConfig.xml", s1);
				s1.setComesThroughDispatcher(false);
			}
			return s1;
		}

		return null;
	}

	// Dan: load task sequence from the config file
	private void loadSequenceOfTasks(String configFile, Student student)
	{
		String givenId = student.getGivenId();
		DTMode mode = student.getDtMode();
		double preTestScore = student.getPreTestScore();

		String sessionId = "1";
		if (givenId.length() > 2)
		{
			String ending = givenId.substring(givenId.length() - 2);
			if (ending.startsWith("_"))
			{
				sessionId = ending.substring(1);
			}
		}

		// Dan: the default level should be ML
		String studentKLevel = "ML";
		System.out.println(preTestScore);
		boolean inAdaptiveMode = mode.name().equals(DTMode.ADAPTIVE.name());
		if (/* inAdaptiveMode && */preTestScore > 0)
		{
			if (preTestScore < 33)
			{
				studentKLevel = "LL";
			}
			else
			{
				if (preTestScore >= 45)
				{
					studentKLevel = "MH";
				}
				if (preTestScore >= 60)
				{
					studentKLevel = "HH";
				}
			}
		}

		student.setKnowledgeLevel(studentKLevel);

		try
		{
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

			Document configDoc = docBuilder.parse(configFile);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile("//Session[@id='" + sessionId
					+ "']/TaskSequence/" + mode.name());

			if (inAdaptiveMode)
			{
				expr = xpath.compile("//Session[@id='" + sessionId
						+ "']/TaskSequence/" + mode.name() + "[@type='"
						+ studentKLevel + "']");
			}

			Object result = expr.evaluate(configDoc, XPathConstants.NODESET);
			NodeList seqNodes = (NodeList) result;
			if (seqNodes.getLength() == 1)
			{
				student.setTaskString(seqNodes.item(0).getTextContent());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Get student details by student ID. Password is not checked!!
	 */
	public Student getStudentFromDatabaseByID(String id)
	{

		Student student = DerbyConnector.getInstance().findStudent(id);
		return student;
	}

	/**
	 * Get DT mode, read answers only or Interactive..
	 */
	// public DTMode getDTMode(String id) {
	// Student s= getStudentFromDatabaseByID(id);
	// return s.getDtMode();
	// }

	/**
	 * A valid teacher id is one that doesn't have a digit or starts with t
	 * 
	 * @param givenId
	 * @return
	 */
	private boolean isTeacherId(String givenId)
	{
		char[] chars = givenId.toCharArray();
		// If given Id doesn't contain a number, consider them as Teachers
		boolean hasDigit = false;
		for (char c : chars)
		{
			if (Character.isDigit(c))
			{
				hasDigit = true;
				break;
			}
		}

		if (givenId.startsWith("t") || !hasDigit)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/*
	 * private Set<String> getValidStudentIds() {
	 * 
	 * Set<String> validIds = new HashSet<String>();
	 * DerbyConnector.createConnection(); validIds =
	 * DerbyConnector.getStudentIds();
	 * 
	 * // // * //validIds.add("s1"); //validIds.add("s2");
	 * //validIds.add("guest"); // * //validIds.add("bill"); List<model.St>
	 * studentList= new // * ArrayList<model.St>(); // * // * EntityManager em =
	 * null; // * // * try { model.St s1= new model.St(); s1.setGivenid("5"); //
	 * * s1.setFirstname("JPT"); em = getEntityManager(); // *
	 * em.getTransaction().begin(); System.out.println(" Mihai is :"); Query //
	 * * q = em.createQuery("SELECT s FROM St s"); studentList // *
	 * =q.getResultList(); em.merge(s1); em.getTransaction().commit(); // * // *
	 * Map m=em.getProperties(); for(Object o:m.keySet()){ // *
	 * System.out.println(o+" --"+m.get(o)); } // * // * } catch(Exception e){
	 * System.out.println(e); // * // * } finally { if (em != null) {
	 * em.close(); } } // * // * System.out.println("Total items:"+
	 * studentList.size()); for(model.St // * s:studentList){
	 * validIds.add(s.getGivenid()); // *
	 * System.out.println("Valid ID:"+s.getGivenid()); } //
	 * 
	 * return validIds; }
	 */
	public Status getStudentStatus(Student s)
	{
		// TODO Auto-generated method stub

		Status stat = Status.NEW;
		String givenId = s.getGivenId();

		if (givenId.toLowerCase().equals("guest")
				|| givenId.toLowerCase().equals("wozguest"))
		{
			return Status.GUEST;
		}

		if (givenId.toLowerCase().startsWith("bill"))
		{
			return Status.BILL;
		}

		if (this.isTeacherId(givenId))
		{
			return Status.TEACHERS;
		}

		return stat;
	}

	// /**
	// * It removes the entries and then tries to save the info
	// *
	// * @param studentId
	// * @param contextId
	// * @param question
	// * @param answer
	// * @param evaluationId
	// * @param explanation
	// */
	// public void saveFCIAnswer(String studentId, int contextId, String
	// question,
	// String answer, String evaluationId, String explanation)
	// {
	// // TODO Auto-generated method stub
	//
	// // Mihai, please save <studentId,q,a> tuple to database
	// DerbyConnector.getInstance().saveStudentEvaluation(studentId,
	// evaluationId, String.valueOf(contextId), question, answer,
	// explanation);
	// System.out.println("Answer for " + question + " is -->" + answer);
	// }

	/**
	 * This always inserts a row to the table
	 * 
	 * @param studentId
	 * @param contextId
	 * @param question
	 * @param answer
	 * @param evaluationId
	 * @param explanation
	 */
	public void insertFCIAnswer(String studentId, int contextId,
			String question, String answer, String evaluationId,
			String explanation)
	{
		DerbyConnector.getInstance().insertStudentEvaluation(studentId,
				evaluationId, String.valueOf(contextId), question, answer,
				explanation);
		System.out.println("Answer for " + studentId + "--" + question
				+ " is -->" + answer);
	}

	public DTState getStudentState(Student s)
	{
		Student s1 = DerbyConnector.getInstance().findStudent(s.getGivenId());
		return s1.getDTState();
	}

	public void setStudentState(Student student, DTState state)
	{
		boolean success = DerbyConnector.getInstance().setStudentState(student,
				state);

		if (!success)
		{
			success = DerbyConnector.getInstance().setStudentState(student,
					state);
		}

		if (success)
		{
			student.setDTState(state);
		}
	}

	public DTMode getStudentMode(Student s)
	{
		Student s1 = DerbyConnector.getInstance().findStudent(s.getGivenId());
		return s1.getDtMode();
	}

	public void setStudentMode(Student student, DTMode mode)
	{
		boolean success = DerbyConnector.getInstance().setStudentMode(student,
				mode);
		if (success)
		{
			student.setDtMode(mode);
		}
	}

	public boolean hasAssignedModePretestAndPostTest(Student s)
	{
		Student s1 = DerbyConnector.getInstance().findStudent(s.getGivenId());
		boolean m, pre, post;
		m = pre = post = false;
		try
		{
			if (s1.getDtMode() != null)
			{
				m = true;
			}
			if (s1.getPostTest() != null)
			{
				pre = true;
			}
			if (s1.getPreTest() != null)
			{
				post = true;
			}
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		return (m && pre && post);
	}

	public synchronized void assignModePretestAndPostTest(Student s)
	{
		int c1 = DerbyConnector.getInstance().getCounter("C1");
		DTMode mode = null;
		// if even, mode is interactive
		// if odd, mode is show answers
		// first two get pretest A and post test B =>
		// then next two get pretest B and post test A
		// the cycle repeats
		String preTest = null, postTest = null;
		// Assign the mode
		// Dan:
		switch (c1 % 3)
		{
		case (0):
			mode = DTMode.INTERACTIVE;
			break;
		case (1):
			mode = DTMode.SHOWANSWERS;
			break;
		case (2):
			mode = DTMode.ADAPTIVE;
			break;
		}

		// old code
		// if (c1 % 2 == 0) {
		// mode = DTMode.INTERACTIVE;
		// } else {
		// mode = DTMode.SHOWANSWERS;
		// }

		// Assign the tests
		// Dan:
		switch (c1 % 3)
		{
		case (0):
			preTest = "A";
			postTest = "B";
			break;
		case (1):
			preTest = "B";
			postTest = "A";
			break;
		case (2):
			preTest = "A";
			postTest = "B";
			break;
		}

		// old code
		// if ((c1 / 2) % 2 == 0)
		// {
		// preTest = "A";
		// postTest = "B";
		// }
		// else
		// {
		// preTest = "B";
		// postTest = "A";
		// }

		// System.out.println(mode+"-"+preTest+"-"+postTest);
		// save these assignment to database
		DerbyConnector.getInstance().setStudentMode(s, mode);
		s.setDtMode(mode);
		DerbyConnector.getInstance().assignTests(s, preTest, postTest);
		// increment the counter
		DerbyConnector.getInstance().setCounter("C1", c1 + 1);
		// Initialize state to pretest
		BusinessModel.getInstance().setStudentState(s, DTState.PRETEST);

	}

	/**
	 * Returns the difference of two dates in minutes
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 */
	public long getDateDifferenceInMins(Date d1, Date d2)
	{
		long diff = d2.getTime() - d1.getTime(); // in millisconds
		// long diffSeconds = diff / 1000 ;
		long diffMinutes = diff / (60 * 1000);
		return diffMinutes;
	}

	/**
	 * Generates the student log file name using date
	 * 
	 * @param s
	 * @return
	 */
	public String getLogFileName(Student s)
	{
		DateFormat df = new SimpleDateFormat("MMddyy");
		String logName = s.getGivenId() + "-"
				+ df.format(Calendar.getInstance().getTime());
		return logName;
	}

	public void logThisInfo(Student s, String info)
	{
		String fileName = getLogFileName(s);
		Date date = new Date();
		DTLogger logger = new DTLogger(fileName);
		logger.log(DTLogger.Actor.NONE, DTLogger.Level.ONE, info + " AT: "
				+ date.toString());
		logger.saveLogInHTML();
	}
}
