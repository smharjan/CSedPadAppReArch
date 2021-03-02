package memphis.semanticmatcher.features;

import memphis.semanticmatcher.tools.StopWords;
import memphis.semanticmatcher.wordmetrics.AbstractWordMetric;
import memphis.semanticmatcher.wordmetrics.LSAWordMetric;
import memphis.semanticmatcher.wordmetrics.WNWordMetric;

//A class for instantiating various features
public class FeatureBuilder {
	
	StopWords stopWords;
	
	public FeatureBuilder(StopWords _stopWords)
	{
		stopWords = _stopWords;
	}

	public AbstractComparer CreateComparer(String strComparer)
	{
		AbstractComparer comparer = null;
		
		String[] parameters = strComparer.split("\t");
		
		//build a lexical comparer
		if (strComparer.startsWith(LexicalComparer.ComparerID))
		{
			//second parameter should be the word metric
			AbstractWordMetric wordMetric = null;
			wordMetric = LSAWordMetric.GetInstance(parameters[1]);
			if (wordMetric == null)	wordMetric = WNWordMetric.GetInstance(parameters[1]);
						
			comparer = new LexicalComparer(wordMetric, stopWords,
					Float.parseFloat(parameters[2]),Boolean.parseBoolean(parameters[3]),
					parameters[4],parameters[5], 
					Boolean.parseBoolean(parameters[6]), Boolean.parseBoolean(parameters[7]),
					Boolean.parseBoolean(parameters[8]), Boolean.parseBoolean(parameters[9]),
					Boolean.parseBoolean(parameters[10]), Boolean.parseBoolean(parameters[11]), 
					Boolean.parseBoolean(parameters[12]));
		}
		if (strComparer.startsWith(DependencyComparer.ComparerID))
		{
			//second parameter should be the word metric
			AbstractWordMetric wordMetric = null;
			wordMetric = LSAWordMetric.GetInstance(parameters[1]);
			if (wordMetric == null)	wordMetric = WNWordMetric.GetInstance(parameters[1]);
						
			comparer = new DependencyComparer(wordMetric, 
					Float.parseFloat(parameters[2]), Boolean.parseBoolean(parameters[3]),
					parameters[4], parameters[5], 
					Float.parseFloat(parameters[6]),Float.parseFloat(parameters[7]),
					Float.parseFloat(parameters[8]),Float.parseFloat(parameters[9]),
					Float.parseFloat(parameters[10]));
		}
		if (strComparer.startsWith(LSAComparer.ComparerID))
		{
			comparer = new LSAComparer(Boolean.parseBoolean(parameters[1]), parameters[2], parameters[3]); 
		}

		if (strComparer.startsWith(CountDiffComparer.ComparerID))
		{
			//second parameter should be the word metric
			AbstractWordMetric wordMetric = null;
			wordMetric = LSAWordMetric.GetInstance(parameters[1]);
			if (wordMetric == null)	wordMetric = WNWordMetric.GetInstance(parameters[1]);
			
			comparer = new CountDiffComparer(wordMetric, Float.parseFloat(parameters[2]),
					Boolean.parseBoolean(parameters[3]), 
					parameters[4], parameters[5], parameters[6]); 
		}

		if (strComparer.startsWith(OptimumComparer.ComparerID))
		{
			//second parameter should be the word metric
			AbstractWordMetric wordMetric = null;
			wordMetric = LSAWordMetric.GetInstance(parameters[1]);
			if (wordMetric == null)	wordMetric = WNWordMetric.GetInstance(parameters[1]);

			comparer = new OptimumComparer(wordMetric, Float.parseFloat(parameters[2]),
					Boolean.parseBoolean(parameters[3]), parameters[4], parameters[5]); 
		}

		if (strComparer.startsWith(CustomComparer.ComparerID))
		{
			comparer = new CustomComparer(parameters[1]); 
		}
		
		return comparer;
	}
}
