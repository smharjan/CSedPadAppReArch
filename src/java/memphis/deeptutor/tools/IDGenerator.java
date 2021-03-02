package memphis.deeptutor.tools;

import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class IDGenerator {

	/**
	 * @param args
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException {
			
	
	  	
	  	Set<String> uniqueIds= new HashSet<String>();
	  	int totalIdRequired = 10;
	  	String schoolCode="hut-s";
	  	while(uniqueIds.size() < totalIdRequired){
	  		String aUserId= getRandom(4);	 
	  		uniqueIds.add(schoolCode+aUserId);
	  	}
	  	int i=0;
	  	for(String id: uniqueIds){
	  		System.out.println("id "+i+": "+ id);
	  		i++;
	  	}
	  	 
    }
	
	
	static String  getRandom(int length) {
		UUID uuid = UUID.randomUUID();
		String myRandom = uuid.toString();
		return myRandom.substring(0,length);
		}
	

}
