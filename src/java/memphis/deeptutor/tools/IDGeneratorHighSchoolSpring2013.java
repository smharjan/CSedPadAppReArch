package memphis.deeptutor.tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import memphis.deeptutor.model.BusinessModel.DTState;

public class IDGeneratorHighSchoolSpring2013 {

	/**
	 * @param args
	 * @throws NoSuchAlgorithmException
	 * @throws IOException 
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {

		List<String> uniqueIds = new ArrayList<String>();
		List<String> uniquePasswords = new ArrayList<String>();

		/*
		 * The six modes: R= Read, I = Interactive, A = Adaptive T1 T2 T3 1 R I
		 * A 2 R A I 3 I R A 4 I A R 5 A I R 6 A R I
		 */
		List<String> modes = new ArrayList<String>();
		modes.add("SHOWANSWERS	INTERACTIVE	ADAPTIVE");
		modes.add("SHOWANSWERS	ADAPTIVE	INTERACTIVE");
		modes.add("INTERACTIVE	SHOWANSWERS	ADAPTIVE");
		modes.add("INTERACTIVE	ADAPTIVE	SHOWANSWERS");
		modes.add("ADAPTIVE	INTERACTIVE	SHOWANSWERS");
		modes.add("ADAPTIVE	SHOWANSWERS	INTERACTIVE");

		List<String> tests = new ArrayList<String>();
		tests.add("A");
		tests.add("B");

		// Generate the ids and passwords now
		int N = 360;
		String schoolCode = "dh";
		DecimalFormat df2 = new DecimalFormat( "000" );
		for (int i = 1; i <= N; i++) {
			uniqueIds.add(schoolCode + df2.format(i));
			uniquePasswords.add(getRandom(4));
		}
		
		//Generate N jobs (cycle through 12= size of modes x two tests)
		List<String> studentJobs = new ArrayList<String>();		
		here:
		for(int i = 1; i <= N; i++){
			int count = 1;
			for (String mode : modes) {
				for (String test : tests) {
					String preTest= test;
					String postTest= test.equals("A") ? "B":"A";
					String assignment= mode + "\t" + preTest +"\t"+postTest;;
					//System.out.println(count + ".\t " + assignment);
					count++;
					studentJobs.add(assignment);
					//stop when the job size is same as student size
					if(studentJobs.size()==N)
						break here;					
				}
			}
		}
		
		//Assign student the jobs (randomly)
		Random r = new Random();
		List<String> studentJobsAssignment = new ArrayList<String>();
		for(int i = 1; i <= N; i++){
			int jobId=r.nextInt(studentJobs.size());
			String job = studentJobs.remove(jobId);
			String temp = uniqueIds.get(i-1) + "\t" + uniquePasswords.get(i-1)+"\t"+job;
			studentJobsAssignment.add(temp);
		}
		
		System.out.println("-- Generate queries --");
		BufferedWriter bw = new BufferedWriter(new FileWriter("ids.txt"));
		for(int i = 1; i <= N; i++){
			String info[] = studentJobsAssignment.get(i-1).split("\\s");
			String id = info[0];
			String pass = info[1];
			String mode1= info[2];
			String mode2= info[3];
			String mode3= info[4];
			String preTest = info[5];
			String postTest = info[6];
			String query="insert into \"APP\".\"STUDENT\" (\"GIVENID\", \"PASSWORD\", \"HASACCEPTEDTERMSANDCONDITIONS\", \"ISSPECIALSTUDENT\", \"WAIT4WOZ\", \"DTSTATE\", \"PRETEST\", \"POSTTEST\", \"DTMODE\",\"DTMODE2\",\"DTMODE3\")"+
					" values('"+id+"', '"+pass+"', null, null, null, '"+DTState.PRETEST+"', '"+preTest+"', '"+postTest+"', '"+mode1+"', '"+mode2+"', '"+mode3+"');";
			bw.write(query+"\n");
			//System.out.println(query);
		}
		bw.close();
	}

	/*
	 * Get random string of length length
	 */
	static String getRandom(int length) {
		UUID uuid = UUID.randomUUID();
		String myRandom = uuid.toString();
		return myRandom.substring(0, length);
	}

}
