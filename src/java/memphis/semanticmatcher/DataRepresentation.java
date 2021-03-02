package memphis.semanticmatcher;

import java.util.ArrayList;

/**
 * The representation of a data instance composed of two texts ({@link #textA} and {@link #textA}) which are to be compared. 
 * This representation can store:
 * <ul>
 * 	<li> a binary golden classification of the relation between the two texts ({@link #goldClass})
 *  <li> a semantic similarity measure between the texts ({@link #semanticMeasure})  
 *  <li> an output of the previous two comparer metrics used ({@link #comparerOutput} and {@link #comparerOutputPrev})
 * </ul>
 *     
 * @author Mihai
 *
 */
public class DataRepresentation{
	
	/**
	 * Predefined separator between the tokens of a text
	 */
	public static char TOKEN_SEP = (char)204;
	public static char TOKENINFO_SEP = (char)193;
	
	public static char RELINFO_SEP = (char)194;
	public static char REL_SEP = (char)195;
	
	public int id;
	
	public SemanticRepresentation textA;
	public SemanticRepresentation textB;
	
	//this tells a classifier if this instance should be ignored (for training or classification)
	public boolean ignore = false;
	
	public boolean goldClass;
	public float semanticMeasure;
	
	public ArrayList<String> comparerOutput = null;
	public ArrayList<String> comparerOutputPrev = null;

	//debug messages for each instance
	public String logMessage = "";
	
	/**
	 * Fill in the dependency tokens for each text
	 * @param useLemma Specify whether to describe the dependency by the lemma forms 
	 * @param ignoreCase Not sure about this
	 * @see SemanticRepresentation#FillInDependencyTokens(Boolean, Boolean) 
	 */
	public void FillInDependencyTokens(Boolean useLemma, Boolean ignoreCase)
	{
		textA.FillInDependencyTokens(useLemma, ignoreCase);
		textB.FillInDependencyTokens(useLemma, ignoreCase);
	}
	

	public DataRepresentation()
	{
	}
	
	public DataRepresentation(String init, int _id)
	{
		this(init);
		id = _id;
	}
	
	public DataRepresentation(String _textA, String _textB)
	{
		textA = new SemanticRepresentation(_textA);
		textB = new SemanticRepresentation(_textB);
	}
	
	public DataRepresentation(String init)
	{
		String[] slist = init.split("\t");
		goldClass = slist[0].equals("1")?true:false;
		textA = new SemanticRepresentation(slist[1],slist[5],slist[7],slist[9],slist[11]);
		textB = new SemanticRepresentation(slist[2],slist[6],slist[8],slist[10],slist[12]);
		ignore = slist[3].equals("1")?true:false;
		semanticMeasure = Float.parseFloat(slist[4]);
	}
	
	public String ToString()
	{
		String itemsA[] = textA.ToString("\t").split("\t");
		String itemsB[] = textB.ToString("\t").split("\t");

		String rawData =  (goldClass?"1":"0") + "\t" + itemsA[0] + "\t" + itemsB[0] + "\t" + (ignore?"1":"0") + 
		"\t" + Float.toString(semanticMeasure);
		
		rawData = rawData + "\t" + itemsA[1] + "\t" + itemsB[1]
		                  + "\t" + itemsA[2] + "\t" + itemsB[2]
  		                  + "\t" + itemsA[3] + "\t" + itemsB[3]
   		                  + "\t" + itemsA[4] + "\t" + itemsB[4];
		
		return rawData; 
	}
}
