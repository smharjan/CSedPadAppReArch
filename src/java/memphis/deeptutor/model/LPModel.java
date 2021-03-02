package memphis.deeptutor.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import memphis.deeptutor.log.DTLogger;
import memphis.deeptutor.singleton.DerbyConnector;
import memphis.deeptutor.singleton.XMLFilesManager;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class LPModel {
	
	public class LPTopic{
		public String id = null;
		public String description = null;
		public ArrayList<LPLevel> levels = new ArrayList<LPLevel>();
		
		public LPLevel GetLPLevel(int level)
		{
			for (int i=0;i<levels.size();i++) if (levels.get(i).level==level) return levels.get(i);
			return null;
		}
	}
	
	public class LPLevel{
		String id = null;
		public int level = -1;
		int nextLevel = -1;
		
		public ArrayList<String> concepts = new ArrayList<String>();
		ArrayList<String> conceptID = new ArrayList<String>();
		public ArrayList<String> fci = new ArrayList<String>();
		ArrayList<String> fciRefs = new ArrayList<String>();
	}
	
	public ArrayList<LPTopic> topics = new ArrayList<LPModel.LPTopic>();
	public ArrayList<String> fciMappings = new ArrayList<String>();
	
	//create the LPInformation from the XML file
	public LPModel()
	{
		Element root = XMLFilesManager.getInstance().GetLPRoot();
		NodeList tlist = root.getElementsByTagName("Topic");
		
		for (int i=0;i<tlist.getLength();i++)
		{
			Element topic = (Element)tlist.item(i);
			LPTopic tnode = new LPTopic();
			tnode.id = topic.getAttribute("id");
			tnode.description = topic.getElementsByTagName("Description").item(0).getTextContent();
			
			NodeList levels = topic.getElementsByTagName("LP");
			for (int j=0;j<levels.getLength();j++)
			{
				LPLevel lp = new LPLevel();
				lp.level = Integer.parseInt(((Element)levels.item(j)).getAttribute("level"));
				NodeList list = levels.item(j).getChildNodes();
				for (int k=0;k<list.getLength();k++)
				{
					if (!(list.item(k) instanceof Element)) continue;
					
					Element e = (Element)list.item(k);
					if (e.getNodeName().equals("FCI"))
					{
						lp.fci.add("," + e.getTextContent().toUpperCase());
						if (e.getAttribute("ref")!= null) lp.fciRefs.add("," + e.getAttribute("ref"));
						else lp.fciRefs.add("");
					}
					if (e.getNodeName().equals("Concept"))
					{
						lp.concepts.add(e.getTextContent());
						lp.conceptID.add(e.getAttribute("id"));
					}
				}
				tnode.levels.add(lp);
			}
			topics.add(tnode);
		}
	}
	
	public String toString()
	{
		String result = "";
		for (int i=0; i<topics.size();i++)
		{
			for (int j=0; j<topics.get(i).levels.size(); j++)
			{
				LPLevel lp = topics.get(i).levels.get(j);
				for (int k=0;k<lp.fci.size();k++) result = result + "<br/>" + lp.fci.get(k);
			}
		}
		return result;
	}
	
	public int[][] BuildStudentLP(String studentID, Map<String, String> answers,String evaluation)
	{
		Student s = new Student(studentID);
		
		DerbyConnector.getInstance().getStudentEvaluation(s, evaluation);
		
		int[][] lpmatrix = new int[topics.size()][GetMaxLPCount()]; 
		for (int i=0;i<topics.size();i++){
			for (int j=0;j<GetMaxLPCount();j++) lpmatrix[i][j] = -1;
			for (int j=0;j<topics.get(i).levels.size();j++) lpmatrix[i][topics.get(i).levels.get(j).level]=0;
		}
		
		Enumeration<String> e = s.evaluationData.keys();
		while (e.hasMoreElements())
		{
			String qid = e.nextElement();
			
			if (answers!=null && !s.evaluationData.get(qid).equalsIgnoreCase(answers.get(qid))) continue;
			
			qid = qid + "-" + s.evaluationData.get(qid).toUpperCase();
			
			for (int i=0;i<topics.size();i++) for (int j=0;j<topics.get(i).levels.size();j++)
			{
				LPLevel level = topics.get(i).levels.get(j);
				for (int k=0;k<level.fci.size();k++) {
					String fci = level.fci.get(k).toUpperCase();
					if (fci.contains(","+qid))
						lpmatrix[i][level.level]++;
				}
			}
		}
		
		return lpmatrix;
	}
	
	
	
	/**
	 * Build the LP for the given evaluation Id (pretest, posttest, fci??)
	 * @param studentID
	 * @param answers
	 * @param evaluationId
	 * @author Rajendra
	 * @return
	 */
	public int[][] BuildStudentLP(String studentID, Map<String, String> answers, String evaluationId, boolean correct)
	{
		Student s = new Student(studentID);
		
		DerbyConnector.getInstance().getStudentEvaluation(s, evaluationId);
		
		int[][] lpmatrix = new int[topics.size()][GetMaxLPCount()]; 
		for (int i=0;i<topics.size();i++){
			for (int j=0;j<GetMaxLPCount();j++) lpmatrix[i][j] = -1;
			for (int j=0;j<topics.get(i).levels.size();j++) lpmatrix[i][topics.get(i).levels.get(j).level]=0;
		}
		
		Enumeration<String> e = s.evaluationData.keys();
		while (e.hasMoreElements())
		{
			String qid = e.nextElement();
			
			if (answers!=null) {
				if (correct && !s.evaluationData.get(qid).equalsIgnoreCase(answers.get(qid))) continue;
				if (!correct && s.evaluationData.get(qid).equalsIgnoreCase(answers.get(qid))) continue;
			}
			
			qid = qid + "-" + s.evaluationData.get(qid).toUpperCase();
			
			for (int i=0;i<topics.size();i++) for (int j=0;j<topics.get(i).levels.size();j++)
			{
				LPLevel level = topics.get(i).levels.get(j);
				for (int k=0;k<level.fci.size();k++) {
					String fci = level.fci.get(k).toUpperCase();
					if (fci.contains(","+qid))
						lpmatrix[i][level.level]++;
				}
			}
		}
		
		return lpmatrix;
	}
	
	/* returns the table of question id, and LP (given the question and answer, so it can be used for pretest, posttest, correct..)
	 *  rbanjade
	 */
	public Map<String, String> BuildStudentLP(Map<String, String> answers)
	{
		Map<String, String> questionLPMap = new HashMap<String, String> ();
		
		int[][] lpmatrix = new int[topics.size()][GetMaxLPCount()]; 
		for (int i=0;i<topics.size();i++){
			for (int j=0;j<GetMaxLPCount();j++) lpmatrix[i][j] = -1;
			for (int j=0;j<topics.get(i).levels.size();j++) lpmatrix[i][topics.get(i).levels.get(j).level]=0;
		}
		
		Set<String> e = answers.keySet();
		for (String qid : e)
		{
			String qidOrg = qid;
			qid = qid + "-" + answers.get(qid).toUpperCase();
			String strandLP = "";
			
			for (int i=0;i<topics.size();i++) {   // rows (topics..)
				boolean topicAdded = false;
				for (int j=0;j<topics.get(i).levels.size();j++) // levels.. (...)
				{
					LPLevel level = topics.get(i).levels.get(j);
					for (int k=0;k<level.fci.size();k++) { 
						String fci = level.fci.get(k).toUpperCase();
						if (fci.contains("," + qid)) {  //, ??, it seems, it ignores the first one.
							if (!topicAdded) {
								strandLP += " " + topics.get(i).id + ": "; // or description..
								topicAdded = true;
							} else
							{
								strandLP += ",";
							}
							strandLP +=  j; 
						}
					}
				}
			}
			if (strandLP == null || strandLP == "") {
				strandLP = "--";
			}
			questionLPMap.put(qidOrg, strandLP);
		}
		return questionLPMap;
	}
	
	
	public void LogStudentLP(String studentID, String evaluation)
	{
		int [][]lpmatrix = BuildStudentLP(studentID, null,evaluation);

		DateFormat df = new SimpleDateFormat("MMddyy");
		DTLogger logger = new DTLogger(studentID + "-" + df.format(Calendar.getInstance().getTime()));// + clientAddress);
		logger.log(DTLogger.Actor.NONE, DTLogger.Level.ONE, "");
		
		logger.log(DTLogger.Actor.NONE, DTLogger.Level.ONE, "TOPIC, LOW, HIGH, TOP, HITS/TOP-HITS");
		for (int i=0;i<topics.size();i++){
			int low = -1;
			int high = -1;
			int top = topics.get(i).levels.size()-1;
			String hits = "";
			
			for (int j=0;j<topics.get(i).levels.size();j++)
			{
				if (lpmatrix[i][j]>0) hits += ", " + lpmatrix[i][j];
				else hits += ", 0";
				
				hits += "/" + countFCIperLP(i,j);

				if(lpmatrix[i][j]>0)
				{
					if (low<0 || low>j) low=j;
					if (high<j) high=j;
				}
			}
			logger.log(DTLogger.Actor.NONE, DTLogger.Level.ONE, topics.get(i).id+", "+low+", "+high+", "+top+hits);
		}
		logger.saveLogInHTML();
	}
	
	public int countFCIperLP(int topicIntex, int level)
	{
		int count = 0;
		LPLevel lp = (topics.get(topicIntex)).GetLPLevel(level);
		if (lp==null) return 0;
			
		for (int k=0;k<lp.fci.size();k++) count += lp.fci.get(k).split(",").length-1;
		return count;
	}
	
	public String GetStudentAnswers(String studentID, Map<String, String> answers, int topicIndex, int lpLevel)
	{
		Student s = new Student(studentID);
		DerbyConnector.getInstance().getStudentEvaluation(s, "pretest");
		String result = "";
		
		Enumeration<String> e = s.evaluationData.keys();
		while (e.hasMoreElements())
		{
			String qid = e.nextElement();
			String qidv = qid + "-" + s.evaluationData.get(qid).toUpperCase();
			//System.out.println(qid);
			
			LPLevel level = topics.get(topicIndex).GetLPLevel(lpLevel); 
			if (level != null) 
			{
				for (int k=0;k<level.fci.size();k++) if (level.fci.get(k).contains(","+qidv))
				{
					result += qidv;
					if (s.evaluationData.get(qid).equalsIgnoreCase(answers.get(qid))) result += "(Correct)";
					result += "; ";
				}
			}
		}
		
		if (result.length()==0) result = "none";
		return result;
	}
	
	
	/*
	 *  Get the student answers in map...
	 */
	
	public Hashtable<String, String> GetStudentAnswers(String studentID, String evaluationId)
	{
		Student s = new Student(studentID);
		DerbyConnector.getInstance().getStudentEvaluation(s, evaluationId);
		return s.evaluationData;
	}
		
	
	/*
	 * This version of GetStudentAnswwers() takes evaluation Id as well. Previously, it was taking fci, now pretest, posttest..
	 */
	
	public String GetStudentAnswers(String studentID, Map<String, String> answers, int topicIndex, int lpLevel, String evaluationId)
	{
		Student s = new Student(studentID);
		DerbyConnector.getInstance().getStudentEvaluation(s, evaluationId);
		String result = "";
		
		Enumeration<String> e = s.evaluationData.keys();
		while (e.hasMoreElements())
		{
			String qid = e.nextElement();
			String qidv = qid + "-" + s.evaluationData.get(qid).toUpperCase();
			//System.out.println(qid);
			
			LPLevel level = topics.get(topicIndex).GetLPLevel(lpLevel); 
			if (level != null) 
			{
				for (int k=0;k<level.fci.size();k++) if (level.fci.get(k).contains(","+qidv))
				{
					result += qidv;
					if (s.evaluationData.get(qid).equalsIgnoreCase(answers.get(qid))) result += "(Correct)";
					result += "; ";
				}
			}
		}
		
		if (result.length()==0) result = "none";
		return result;
	}
	
	
	
	
	
	public int GetMaxLPCount()
	{
		//TODO
		int max = 0;
		for (int i=0;i<topics.size();i++) if (topics.get(i).levels.size()>max) max = topics.get(i).levels.size();
		return max;
	}
}
