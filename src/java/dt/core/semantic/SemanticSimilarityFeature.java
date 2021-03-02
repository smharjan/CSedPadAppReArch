package dt.core.semantic;

import dt.core.semantic.features.AbstractComparer;

public class SemanticSimilarityFeature {
	
	public AbstractComparer comparer;
	public String featureName = "";
	
	public float[] trainData = null;
	public float[] testData = null;
	
	public boolean featureExtractedOnTrain = false;
	public boolean featureExtractedOnTest = false;
	
	public SemanticSimilarityFeature(String name, AbstractComparer _comparer)
	{
		featureName = name;
		comparer = _comparer;
	}
	
	public String getValuesToString()
	{
		return featureName + "\n" + comparer.getSerializable();
	}
}
