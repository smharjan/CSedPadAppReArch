package dt.core.semantic.wordmetrics;

import dt.core.semantic.tools.WordNetSimilarity;

public class WNWordMetric extends AbstractWordMetric{

	public WordNetSimilarity.WNSimMeasure wnSimMeasure;
	public boolean useFirstSense;
	
	public WNWordMetric(WordNetSimilarity.WNSimMeasure _wnSimMeasure, boolean _useFirstSense)
	{
		useFirstSense = _useFirstSense;
		wnSimMeasure = _wnSimMeasure;
	}

	@Override
	public String getID() {
		return wnSimMeasure.toString() + (useFirstSense?"b":"");
	}
	
	@Override
	public String getSerializable() {
		return "W2WWordNet-" + wnSimMeasure.toString() + "-" + Boolean.toString(useFirstSense);
	}
	
	public static WNWordMetric GetInstance(String serialized)
	{
		if (serialized.startsWith("W2WWordNet"))
		{
			String[] tokens = serialized.split("-");
			return new WNWordMetric(WordNetSimilarity.WNSimMeasure.valueOf(tokens[1]),Boolean.getBoolean(tokens[2]));
		}
		else return null;
	}

	@Override
	public double ComputeWordSimilarity(String word1, String word2, String pos) {
		
		double value = WordNetSimilarity.getInstance().GetWNSimilarity(wnSimMeasure, useFirstSense, word1.toLowerCase(), word2.toLowerCase(), pos);
		
		return value;
	}
	
}
