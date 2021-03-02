package memphis.deeptutor.servlets;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import memphis.deeptutor.fci.FciXmlParser;
import memphis.deeptutor.log.DTLogger;
import memphis.deeptutor.model.LPModel;
import memphis.deeptutor.model.Student;
import memphis.deeptutor.model.LPModel.LPLevel;
import memphis.deeptutor.model.LPModel.LPTopic;
import memphis.deeptutor.singleton.DerbyConnector;


/**
 * Servlet implementation class TasksManager
 */
@WebServlet("/lpview")
public class LPModelViewer extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LPModelViewer() {
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
		String command = (String)request.getParameter("command");
		
		if (command == null || command.equals(""))
		{
			System.out.println("Loading student list...");

			//first time, load the todo items
			session.setAttribute("feedback", "Loading students.");
			session.setAttribute("students", DerbyConnector.getInstance().getStudents(new ArrayList<String>()));
		}
		else{
			LPModel lp = new LPModel();
			
			String xmlPath = session.getServletContext().getRealPath("/FCI")+"/FCI_complete.xml";
			Map<String, String> answers = FciXmlParser.getFCIAnswers(xmlPath);
			String studentID = (String)request.getParameter("viewid");

			if (command.equals("viewlp"))
			{
				/* for pretest */
				int[][] lpmatrix = lp.BuildStudentLP(studentID, answers,"pretest",false);
				int[][] lpmatrixCorrect = lp.BuildStudentLP(studentID, answers,"pretest",true);
				
				String result = "<table border='0' cellpadding='2px' style='text-align:left; border-spacing:0;'><tr><th style='border-bottom:thin solid; border-right:thin solid'>Level</th>";
				for (int i=0;i<lpmatrix.length;i++)
					result += "<th colspan='3' style='border-bottom:thin solid;border-right:thin solid'>" + lp.topics.get(i).description + "</th>";
				result += "</tr>";
				for (int i=lpmatrix[0].length-1;i>=0;i--){
					result += "<tr align='right'><td style='border-right:thin solid'>"+i+"</td>";
					for (int j=0;j<lpmatrix.length;j++) {
						if (lpmatrix[j][i]>=0)
						{
							int gradient = 255-lp.countFCIperLP(j,i)*10;
							if (gradient < 0) gradient = 0; 
							result += "<td onclick='ViewCellAllFCI("+j+","+i+",\"pretest\""+")' onmouseover='this.style.backgroundColor=\"#AAAAAA\"' onmouseout='this.style.backgroundColor=\"rgb("+gradient+",255, "+gradient+")\"' style='background-color:rgb(" + gradient+",255,"+gradient+")'>" + lp.countFCIperLP(j,i) + "</td>";
							gradient = 255-lpmatrixCorrect[j][i]*30;
							result += "<td onclick='ViewCell("+j+","+i+",\"pretest\""+")' onmouseover='this.style.backgroundColor=\"#AAAAAA\"' onmouseout='this.style.backgroundColor=\"rgb("+gradient+","+gradient+",255)\"' style='background-color:rgb(" + gradient+","+gradient+",255)'>" + lpmatrixCorrect[j][i] + "</td>";
							gradient = 255-lpmatrix[j][i]*30;
							result += "<td onclick='ViewCell("+j+","+i+",\"pretest\""+")' onmouseover='this.style.backgroundColor=\"#AAAAAA\"' onmouseout='this.style.backgroundColor=\"rgb(255,"+gradient+","+gradient+")\"' style='border-right:thin solid;background-color:rgb(255,"+gradient+","+gradient+")'>" + lpmatrix[j][i] + "</td>";
						}
						else result += "<td colspan='3' style='border-right:thin solid'></td>";
					}
					result += "</tr>";
				}
				result += "<tr><td colspan='" + (1+3*lpmatrix.length) + "' style='border-top:thin solid'>&nbsp;</td></table>";
						
				//session.setAttribute("feedback", "Viewing LP for student " + studentID);
				session.setAttribute("matrixpre", result);
				//session.setAttribute("lpdetail", "");
				//session.setAttribute("viewid", studentID);
				
				
				/* for posttest */
				int [][] lpmatrixP = lp.BuildStudentLP(studentID, answers, "posttest",false);
				int [] []lpmatrixCorrectP = lp.BuildStudentLP(studentID, answers, "posttest",true);
				
				result = "<table border='0' cellpadding='2px' style='text-align:left; border-spacing:0;'><tr><th style='border-bottom:thin solid; border-right:thin solid'>Level</th>";
				for (int i=0;i<lpmatrixP.length;i++)
					result += "<th colspan='3' style='border-bottom:thin solid;border-right:thin solid'>" + lp.topics.get(i).description + "</th>";
				result += "</tr>";
				for (int i=lpmatrixP[0].length-1;i>=0;i--){
					result += "<tr align='right'><td style='border-right:thin solid'>"+i+"</td>";
					for (int j=0;j<lpmatrixP.length;j++) {
						if (lpmatrixP[j][i]>=0)
						{
							int gradient = 255-lp.countFCIperLP(j,i)*10;
							if (gradient < 0) gradient = 0; 
							result += "<td onclick='ViewCellAllFCI("+j+","+i+",\"posttest\""+")' onmouseover='this.style.backgroundColor=\"#AAAAAA\"' onmouseout='this.style.backgroundColor=\"rgb("+gradient+",255, "+gradient+")\"' style='background-color:rgb(" + gradient+",255,"+gradient+")'>" + lp.countFCIperLP(j,i) + "</td>";
							gradient = 255-lpmatrixCorrectP[j][i]*30;
							result += "<td onclick='ViewCell("+j+","+i+",\"posttest\""+")' onmouseover='this.style.backgroundColor=\"#AAAAAA\"' onmouseout='this.style.backgroundColor=\"rgb("+gradient+","+gradient+",255)\"' style='background-color:rgb(" + gradient+","+gradient+",255)'>" + lpmatrixCorrectP[j][i] + "</td>";
							gradient = 255-lpmatrixP[j][i]*30;
							result += "<td onclick='ViewCell("+j+","+i+",\"posttest\""+")' onmouseover='this.style.backgroundColor=\"#AAAAAA\"' onmouseout='this.style.backgroundColor=\"rgb(255,"+gradient+","+gradient+")\"' style='border-right:thin solid;background-color:rgb(255,"+gradient+","+gradient+")'>" + lpmatrixP[j][i] + "</td>";
						}
						else result += "<td colspan='3' style='border-right:thin solid'></td>";
					}
					result += "</tr>";
				}
				result += "<tr><td colspan='" + (1+3*lpmatrixP.length) + "' style='border-top:thin solid'>&nbsp;</td></table>";

				session.setAttribute("matrixpost", result);

				// common to pre and post
				session.setAttribute("feedback", "Viewing LP for student " + studentID);
				session.setAttribute("lpdetail", "");
				session.setAttribute("viewid", studentID);
			}
			
			/* when user clicks on the cell - description to show - handle pretest or post test */
			
			if (command.startsWith("viewcell"))
			{
				String params[] = command.split(" ");
				//System.out.println("Viewing LP for student " + studentID);
			
				LPTopic lptopic = lp.topics.get(Integer.parseInt(params[1]));
				LPLevel lplevel = lptopic.GetLPLevel(Integer.parseInt(params[2]));
				String evaluationId = params[3]; // pre, post, fci (only)
				String result = "Viewing LP for topic '" + lptopic.description + "' on level " + lplevel.level + "<br/><hr/>";

				result += "<ul>";
				for (int i=0;i<lplevel.concepts.size();i++) result += "<li>" + lplevel.concepts.get(i) + "<br/>";
				result += "</ul>";
				
				if (params[0].endsWith("allfci"))
				{
					String fciList = "";
					for (int k=0;k<lplevel.fci.size();k++) fciList += " " + lplevel.fci.get(k);
					fciList = fciList.substring(2);
					result += "<hr/>FCI answers associated for this LP: " + fciList; 
				}
				else result += "<hr/>Student answers associated for this LP: " + lp.GetStudentAnswers(studentID, answers, Integer.parseInt(params[1]), Integer.parseInt(params[2]), evaluationId);
				
				session.setAttribute("lpdetail", result);
			}
			
			/* show the question.. and LP map table.. */
			createQuestionLPTable(session, studentID);

			/* show the topic, hits tables.., please check for the correctness.. */
			createTopicHitsTables(session, studentID);
		}
		
		String adminPage = "/lpview.jsp";
		RequestDispatcher rd = getServletContext().getRequestDispatcher(adminPage);
		rd.forward(request, response);
	}
	
	
	/* Creates a table of question, LP and then puts in session.
	 * 
	 */
	private void createQuestionLPTable(HttpSession session, String studentID){
		
		LPModel lp = new LPModel();
		Hashtable<String, String> pretestAnswers = lp.GetStudentAnswers(studentID, "pretest");
		Hashtable<String, String> posttestAnsweers = lp.GetStudentAnswers(studentID, "posttest");
		
		String xmlPath = session.getServletContext().getRealPath("/FCI")+"/FCI_complete.xml";
		Map<String, String> answers = FciXmlParser.getFCIAnswers(xmlPath);

		Map<String, String> strandLPPretest = lp.BuildStudentLP(pretestAnswers);
		Map<String, String> strandLPPosttest = lp.BuildStudentLP(posttestAnsweers);
		Map<String, String> strandLPCorrect = lp.BuildStudentLP(answers);
		
		StringBuilder sb = new StringBuilder(); 
				sb.append("<TABLE  BORDER=1 CELLPADDING=10 CELLSPACING=3 RULES=ROWS FRAME=HSIDES><tr><th style='border-bottom:thin solid; border-right:thin solid'>Q ID</th>");
				sb.append("<th style='border-bottom:thin solid; border-right:thin solid'>Pretest</th> <th style='border-bottom:thin solid; border-right:thin solid'>Post test</th>");
				sb.append("<th style='border-bottom:thin solid; border-right:thin solid'>Correct</th></tr>");

	    int pretestScore = 0;
	    int posttestScore = 0;
	    String row = null;
	    String strandLP = null;
		for (int i = 1; i<= pretestAnswers.size(); i++)
		{
			String qId = String.valueOf(i).toUpperCase();
			row = "<tr><td>" + qId + "</td>";
			if (pretestAnswers.containsKey(qId)) {
				strandLP = strandLPPretest.get(qId);
				if (pretestAnswers.get(qId).equalsIgnoreCase(answers.get(qId))) {
					pretestScore++;
					row += "<td><font color = 'blue'>" +pretestAnswers.get(qId) + "</font>" + " " + strandLP + "</td>";
				}
				else {
					row += "<td><font color = 'red'>" +pretestAnswers.get(qId) + "</font>" + " " + strandLP + "</td>";
				}
			} else {
				row += "<td> -- </td>";
			}
				

			if (posttestAnsweers.containsKey(qId)) {
				strandLP = strandLPPosttest.get(qId);
				if (posttestAnsweers.get(qId).equalsIgnoreCase(answers.get(qId))) {
					posttestScore++;
					row += "<td><font color = 'blue'>" +posttestAnsweers.get(qId) +"</font>" + " " + strandLP + "</td>";
				}
				else {
					row += "<td><font color = 'red'>" +posttestAnsweers.get(qId) +"</font>" + " " + strandLP + "</td>";
				}
			} else {
				row += "<td> -- </td>";
			}
				
			
			strandLP = strandLPCorrect.get(qId);
			row += "<td><font color = 'blue'>" +answers.get(qId).toUpperCase() +"</font>" + " " + strandLP + "</td>";
			
			sb.append(row);
		}
		row = "<tr><td>Correct</td><td>" + pretestScore + "</td><td>" + posttestScore + "</td><td><td></tr>";
		sb.append(row);

		sb.append("</table>");
		session.setAttribute("questionLPTable", sb.toString());
		
	}
	
	/*
	 *  build the summary... (as in the log file... ). I don't know the details... 
	 * 
	 */
	public void createTopicHitsTables(HttpSession session,String studentID)
	{
		LPModel lp = new LPModel();
		int [][]lpmatrixPretest = lp.BuildStudentLP(studentID, null, "pretest", false); // null - check true, false...
		int [][]lpmatrixPosttest = lp.BuildStudentLP(studentID, null, "posttest", false); // null - check true, false...
		
		StringBuilder sb = new StringBuilder();
		sb.append("<h2> Pretest </h2>");
		sb.append("<TABLE  BORDER=1 CELLPADDING=10 CELLSPACING=3 RULES=ROWS FRAME=HSIDES><tr><th style='border-bottom:thin solid; border-right:thin solid'>TOPIC</th>");
		sb.append("<th style='border-bottom:thin solid; border-right:thin solid'>LOW</th> <th style='border-bottom:thin solid; border-right:thin solid'>HIGH</th>");
		sb.append("<th style='border-bottom:thin solid; border-right:thin solid'>TOP</th><th style='border-bottom:thin solid; border-right:thin solid'>HITS/TOP-HITS</th></tr>");

		for (int i=0;i<lp.topics.size();i++){
			int low = -1;
			int high = -1;
			int top = lp.topics.get(i).levels.size()-1;
			String hits = "";
			
			for (int j=0;j<lp.topics.get(i).levels.size();j++)
			{
				if (lpmatrixPretest[i][j]>0) 
					hits += ", " + lpmatrixPretest[i][j];
				else hits += ", 0";
				
				hits += "/" + lp.countFCIperLP(i,j);

				if(lpmatrixPretest[i][j]>0)
				{
					if (low<0 || low>j) low=j;
					if (high<j) high=j;
				}
			}
			sb.append("<tr><td>" + lp.topics.get(i).id + "</td><td>"+low+"</td><td>"+high+"</td><td>"+top+ "</td><td>" + hits.replaceFirst(",", "") + "</td></tr>");
		}
		sb.append("</table>");
		
		sb.append("<h2> Post test </h2>");
		sb.append("<TABLE  BORDER=1 CELLPADDING=10 CELLSPACING=3 RULES=ROWS FRAME=HSIDES><tr><th style='border-bottom:thin solid; border-right:thin solid'>TOPIC</th>");
		sb.append("<th style='border-bottom:thin solid; border-right:thin solid'>LOW</th> <th style='border-bottom:thin solid; border-right:thin solid'>HIGH</th>");
		sb.append("<th style='border-bottom:thin solid; border-right:thin solid'>TOP</th><th style='border-bottom:thin solid; border-right:thin solid'>HITS/TOP-HITS</th></tr>");

		for (int i=0;i<lp.topics.size();i++){
			int low = -1;
			int high = -1;
			int top = lp.topics.get(i).levels.size()-1;
			String hits = "";
			
			for (int j=0;j<lp.topics.get(i).levels.size();j++)
			{
				if (lpmatrixPosttest[i][j]>0) hits += ", " + lpmatrixPosttest[i][j];
				else hits += ", 0";
				
				hits += "/" + lp.countFCIperLP(i,j);

				if(lpmatrixPosttest[i][j]>0)
				{
					if (low<0 || low>j) low=j;
					if (high<j) high=j;
				}
			}
			sb.append("<tr><td>" + lp.topics.get(i).id + "</td><td>"+low+"</td><td>"+high+"</td><td>"+top+ "</td><td>" + hits.replaceFirst(",", "") + "</td></tr>");
		}
		sb.append("</table>");		
		session.setAttribute("topicHitsTables", sb.toString());
	}	

}
