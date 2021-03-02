package memphis.semanticmatcher.features;

import java.util.ArrayList;

import memphis.semanticmatcher.SemanticRepresentation;

public abstract class AbstractComparer {
	
	ArrayList<String> comparerOutput = null;
	
	abstract public float ComputeSimilarity(SemanticRepresentation textA, SemanticRepresentation textB);
	abstract public String getComparerID();
	abstract public String getSerializable();
	
	protected void InitializeComparerOutput()
	{
		comparerOutput = new ArrayList<String>();
	}
	
	protected void LogComparerOutput(String output)
	{
		comparerOutput.add(output);
	}
	
	public ArrayList<String> ComparerOutput()
	{
		return comparerOutput;
	}
}
