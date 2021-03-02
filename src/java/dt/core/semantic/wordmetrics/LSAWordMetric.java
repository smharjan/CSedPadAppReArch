package dt.core.semantic.wordmetrics;

import dt.core.semantic.tools.LSATasa;

public class LSAWordMetric extends AbstractWordMetric  {

	public boolean useBaseForm;
	
	public LSAWordMetric(boolean _useBaseForm)
	{
		useBaseForm = _useBaseForm;
	}

	@Override
	public String getID() {
		return "LSA" + (useBaseForm?"b":"");
	}

	@Override
	public String getSerializable() {
		return "W2WLSA-" + Boolean.toString(useBaseForm);
	}
	
	public static LSAWordMetric GetInstance(String serialized)
	{
		if (serialized.startsWith("W2WLSA"))
		{
			String[] tokens = serialized.split("-");
			return new LSAWordMetric(Boolean.getBoolean(tokens[1]));
		}
		else return null;
	}


	@Override
	public double ComputeWordSimilarity(String word1, String word2, String pos) {
		
		double value = 0;
		try {
			value = (double)LSATasa.getInstance().getTokenizedTextLSACosine(word1.toLowerCase().split(" "),
					word2.toLowerCase().split(" "), 
					LSATasa.LOCAL_WEIGHTING.NONE, LSATasa.GLOBAL_WEIGHTING.NONE, useBaseForm);
		} catch (Exception e) {			
			e.printStackTrace();
		}
		return value;
	}

}
