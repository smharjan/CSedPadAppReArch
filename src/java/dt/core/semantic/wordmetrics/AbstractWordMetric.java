package dt.core.semantic.wordmetrics;

public abstract class AbstractWordMetric {

	abstract public String getID();
	abstract public String getSerializable();

	//pos - in case the two words have the same part-of-speech, this is useful for wordnet measures
	abstract public double ComputeWordSimilarity(String word1, String word2, String pos);
}
