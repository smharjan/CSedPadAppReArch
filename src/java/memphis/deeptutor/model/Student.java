package memphis.deeptutor.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;

import memphis.deeptutor.model.BusinessModel.DTMode;
import memphis.deeptutor.model.BusinessModel.DTState;

public class Student
{
	private String givenId;
	private String password = "";
	private char gender = '*';
	private String ethnicity = "";
	private int age = 0;

	private String school = "";
	private String major = "";
	
	//Dan: add mostAdvancedClass field 
	private String mostAdvancedClass = "";
	
	//Dan: add preTestScore field
	private double preTestScore = 0;
	
	private String educationLevel = "";
	private String gpa = "";

	private String familiarAreas = "";
	private String priorCourses = "";
	private String currentCourses = "";

	private DTState dtState = null; // Mihai - this is a general field to
									// customize the system's behavior for
									// certain students

	private boolean isSpecialStudent = false;

	private boolean hasAcceptedTermsAndConditions = false;
	public boolean wait4woz = false;

	public Hashtable<String, String> evaluationData = null;
	public HashSet<String> evaluationContext = null;
	public String completedTasks = null;
	public String currentTask = null;
	private String preTest = null;
	private String postTest = null;
	private DTMode dtMode = null;
	
	private String taskString = "";
	
	private Date session_0_start;
	private Date session_0_end;
	private Date session_1_start;
	private Date session_1_end;
	private Date session_2_start;
	private Date session_2_end;
	private Date session_3_start;
	private Date session_3_end;
	private Date session_4_start;
	private Date session_4_end;
	
	private Date finishedDate0;
	private Date finishedDate1;
	private Date finishedDate2;
	private Date finishedDate3;
	private Date finishedDate4;
	
	private Date lastLogInDate;
	
	private int stopCode = 9000;
//	private boolean tooLateToday = false;
//	private boolean loggedInSameDay = false;
	private boolean comesThroughDispatcher = false;
	private int sessionNumber = -1;
	private String knowledgeLevel;

	public String getGivenId()
	{
		return givenId;
	}

	public Student(String _givenId)
	{
		givenId = _givenId;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public void setPreTestScore(double score)
	{
		this.preTestScore = score;
	}
	
	public double getPreTestScore()
	{
		return this.preTestScore;
	}

	public char getGender()
	{
		return gender;
	}

	public void setGender(char gender)
	{
		this.gender = gender;
	}

	public String getEthnicity()
	{
		return ethnicity;
	}

	public void setEthnicity(String ethnicity)
	{
		this.ethnicity = ethnicity;
	}

	public int getAge()
	{
		return age;
	}

	public void setAge(int age)
	{
		this.age = age;
	}

	public String getSchool()
	{
		return school;
	}

	public void setSchool(String school)
	{
		this.school = school;
	}

	public String getMajor()
	{
		return major;
	}

	public void setMajor(String major)
	{
		this.major = major;
	}

	public String getEducationLevel()
	{
		return educationLevel;
	}

	public void setEducationLevel(String educationLevel)
	{
		this.educationLevel = educationLevel;
	}

	public String getGpa()
	{
		return gpa;
	}

	public void setGpa(String gpa)
	{
		this.gpa = gpa;
	}

	public String getFamiliarAreas()
	{
		return familiarAreas;
	}

	public void setFamiliarAreas(String familiarAreas)
	{
		this.familiarAreas = familiarAreas;
	}

	public String getPriorCourses()
	{
		return priorCourses;
	}

	public void setPriorCourses(String priorCourses)
	{
		this.priorCourses = priorCourses;
	}

	public String getCurrentCourses()
	{
		return currentCourses;
	}

	public void setCurrentCourses(String currentCourses)
	{
		this.currentCourses = currentCourses;
	}

	public boolean isSpecialStudent()
	{
		return isSpecialStudent;
	}

	public void setSpecialStudent(boolean isSpecialStudent)
	{
		this.isSpecialStudent = isSpecialStudent;
	}

	public DTState getDTState()
	{
		return dtState;
	}

	public void setDTState(DTState state)
	{
		if (state != null)
			this.dtState = state;
		else
			this.dtState = null;
	}

	public boolean hasAcceptedTermsAndConditions()
	{
		return hasAcceptedTermsAndConditions;
	}

	public void setHasAcceptedTermsAndConditions(
			boolean hasAcceptedTermsAndConditions)
	{
		this.hasAcceptedTermsAndConditions = hasAcceptedTermsAndConditions;
	}

	public String getPreTest()
	{
		return this.preTest;
	}

	public void setPreTest(String preTest)
	{
		this.preTest = preTest;
	}

	public String getPostTest()
	{
		return postTest;
	}

	public void setPostTest(String postTest)
	{
		this.postTest = postTest;
	}

	public DTMode getDtMode()
	{
		return dtMode;
	}

	public void setDtMode(DTMode dtMode)
	{
		this.dtMode = dtMode;
	}
	
	//Dan: getter and setter for mostAdvancedClass
	public String getMostAdvancedClass()
	{
		return mostAdvancedClass;
	}

	public void setMostAdvancedClass(String mostAdvancedClass)
	{
		this.mostAdvancedClass = mostAdvancedClass;
	}

	public void printDetails()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Given ID:" + givenId + "\n");
		sb.append("Age: " + age + "\n");
		sb.append("Current Course:" + currentCourses + "\n");
		sb.append("Education Level:" + educationLevel + "\n");
		sb.append("Ethnicity: " + ethnicity + "\n");
		sb.append("" + familiarAreas + "\n");
		sb.append("Gender: " + gender + "\n");
		sb.append("GPA: " + gpa + "\n");
		sb.append("Has Accepted T&C: " + hasAcceptedTermsAndConditions + "\n");
		sb.append("Is Special Student:" + isSpecialStudent + "\n");
		sb.append("DTState:" + dtState + "\n");
		sb.append("Major: " + major + "\n");
		
		//Dan: add mostAdvancedClass to print details
		sb.append("Most Advanced Class: " + mostAdvancedClass + "\n");
		
		sb.append("Priour Courses:" + priorCourses + "\n");
		sb.append("School: " + school + "\n");
		System.out.println(sb.toString());

	}

	public Date getSession_1_start()
	{
		return session_1_start;
	}

	public void setSession_1_start(Date session_1_start)
	{
		this.session_1_start = session_1_start;
	}

	public Date getSession_1_end()
	{
		return session_1_end;
	}

	public void setSession_1_end(Date session_1_end)
	{
		this.session_1_end = session_1_end;
	}

	public Date getSession_2_start()
	{
		return session_2_start;
	}

	public void setSession_2_start(Date session_2_start)
	{
		this.session_2_start = session_2_start;
	}

	public Date getSession_2_end()
	{
		return session_2_end;
	}

	public void setSession_2_end(Date session_2_end)
	{
		this.session_2_end = session_2_end;
	}

	public Date getSession_3_start()
	{
		return session_3_start;
	}

	public void setSession_3_start(Date session_3_start)
	{
		this.session_3_start = session_3_start;
	}

	public Date getSession_3_end()
	{
		return session_3_end;
	}

	public void setSession_3_end(Date session_3_end)
	{
		this.session_3_end = session_3_end;
	}

	public Date getLastLogInDate()
	{
		return lastLogInDate;
	}

	public void setLastLogInDate(Date lastLogInDate)
	{
		this.lastLogInDate = lastLogInDate;
	}

	public Date getFinishedDate1()
	{
		return finishedDate1;
	}

	public void setFinishedDate1(Date finishedDate1)
	{
		this.finishedDate1 = finishedDate1;
	}

	public Date getFinishedDate2()
	{
		return finishedDate2;
	}

	public void setFinishedDate2(Date finishedDate2)
	{
		this.finishedDate2 = finishedDate2;
	}

	public Date getFinishedDate3()
	{
		return finishedDate3;
	}

	public void setFinishedDate3(Date finishedDate3)
	{
		this.finishedDate3 = finishedDate3;
	}

	public Date getFinishedDate0()
	{
		return finishedDate0;
	}

	public void setFinishedDate0(Date finishedDate0)
	{
		this.finishedDate0 = finishedDate0;
	}

	public Date getFinishedDate4()
	{
		return finishedDate4;
	}

	public void setFinishedDate4(Date finishedDate4)
	{
		this.finishedDate4 = finishedDate4;
	}

	public Date getSession_0_start()
	{
		return session_0_start;
	}

	public void setSession_0_start(Date session_0_start)
	{
		this.session_0_start = session_0_start;
	}

	public Date getSession_0_end()
	{
		return session_0_end;
	}

	public void setSession_0_end(Date session_0_end)
	{
		this.session_0_end = session_0_end;
	}

	public Date getSession_4_start()
	{
		return session_4_start;
	}

	public void setSession_4_start(Date session_4_start)
	{
		this.session_4_start = session_4_start;
	}

	public Date getSession_4_end()
	{
		return session_4_end;
	}

	public void setSession_4_end(Date session_4_end)
	{
		this.session_4_end = session_4_end;
	}

	public String getTaskString()
	{
		return taskString;
	}

	public void setTaskString(String taskString)
	{
		this.taskString = taskString;
	}

	public boolean comesThroughDispatcher()
	{
		return comesThroughDispatcher;
	}

	public void setComesThroughDispatcher(boolean comesThroughDispatcher)
	{
		this.comesThroughDispatcher = comesThroughDispatcher;
	}

	public String getKnowledgeLevel()
	{
		return knowledgeLevel;
	}

	public void setKnowledgeLevel(String knowledgeLevel)
	{
		this.knowledgeLevel = knowledgeLevel;
	}

	public int getSessionNumber()
	{
		return sessionNumber;
	}

	public void setSessionNumber(int sessionNumber)
	{
		this.sessionNumber = sessionNumber;
	}

	public int getStopCode()
	{
		return stopCode;
	}

	public void setStopCode(int stopCode)
	{
		this.stopCode = stopCode;
	}
}
