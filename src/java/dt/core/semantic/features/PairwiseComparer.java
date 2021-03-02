package dt.core.semantic.features;

import dt.core.semantic.tools.LSATasa;
import dt.core.semantic.tools.SMUtils;
import dt.core.semantic.tools.WikipediaIDF;
import dt.core.semantic.wordmetrics.AbstractWordMetric;


public abstract class PairwiseComparer extends AbstractComparer{

	public static enum WordWeightType {NONE, ENTROPY, IDF}; //apply a global weight to each word
	public static enum NormalizeType {MAX, MIN, TEXTA, TEXTB, AVERAGE}; //apply a global weight to each word

	//pairing parameters
	public AbstractWordMetric wordMetric = null;
	public float w2wSimThreshold = 0.5f;
	public Boolean useBaseForm = true; //use the base form of the words when doing comparisons
	public WordWeightType wordWeighting = WordWeightType.NONE;
	public NormalizeType normalizeType = NormalizeType.AVERAGE;

	double WordWeight(String word, WordWeightType weightType)
	{
		word = word.toLowerCase();
		
		if (weightType == WordWeightType.ENTROPY)
		{
			LSATasa lsa = LSATasa.getInstance();
			if (lsa.lsaTerms.containsKey(word)) return lsa.lsaTerms.get(word).entropyWeight;
		}
		if (weightType == WordWeightType.IDF)
		{
			WikipediaIDF wikiIDF = WikipediaIDF.getInstance();
			if (wikiIDF.idfWeights.containsKey(word)) return wikiIDF.idfWeights.get(word);
		}
		return 1;
	}
	
	double ComputeW2WSimilarity(String tokenA, String tokenB, String pos)
	{
		if (tokenA.equals(tokenB)) return 1;
		
		if (wordMetric == null) return 0;

		return wordMetric.ComputeWordSimilarity(tokenA, tokenB, pos);
	}
	
	@Override
	public String getComparerID() {
		return GetNormalizeShortID(normalizeType) + "-" + (wordMetric==null?"NA":wordMetric.getID()) + SMUtils.ShortFloatDisplay(w2wSimThreshold) + (useBaseForm?"b":"w") + 
		(wordWeighting==WordWeightType.NONE?"n":(wordWeighting==WordWeightType.ENTROPY?"e":"i")); 		
	}

	@Override
	public String getSerializable() {
		return (wordMetric==null?"NA":wordMetric.getSerializable()) + "\t" + w2wSimThreshold + "\t" + 
			Boolean.toString(useBaseForm) + "\t" + wordWeighting.toString() + "\t" + normalizeType.toString();
	}
	
	String GetNormalizeShortID(NormalizeType norm)
	{
		switch (norm)
		{
			case AVERAGE: return "avg";
			case MAX: return "max";
			case MIN: return "min";
			case TEXTA: return "tA";
			case TEXTB: return "tB";
		}
		return "NA";
	}
}
