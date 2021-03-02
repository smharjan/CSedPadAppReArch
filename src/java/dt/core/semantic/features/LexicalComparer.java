package dt.core.semantic.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import dt.core.semantic.SemanticRepresentation;
import dt.core.semantic.tools.SMUtils;
import dt.core.semantic.tools.StopWords;
import dt.core.semantic.wordmetrics.AbstractWordMetric;

public class LexicalComparer extends PairwiseComparer{
	
	static final String ComparerID = "LEX";

	//Lexical Feature Attributes
	public Boolean caseSensitive = false; //this is only for tokens that are exact matches
	public Boolean useTokenFrequency = false; //compare the lists of tokens versus the sets of tokens
	public Boolean useBigrams = false; //instead of words compare with bigrams
	
	//filtering options
	public Boolean ignoreStopWords = false;
	public Boolean ignorePunctuation = false;
	public Boolean contentWordsOnly = false;
	
	//W2W pairing options
	public Boolean compareWithPOS = false;
	
	//------------------------------------------------------------------------
	//stopWords tool
	StopWords stopWords = null;
	
	@Override
	public String getComparerID() {
		return ComparerID + super.getComparerID() + "-" + (caseSensitive?"s":"i") + 
		(useTokenFrequency?"f":"n") + (useBigrams?"b":"u") + "-" + (ignoreStopWords?"s":"*") +
		(ignorePunctuation?"p":"*") + (contentWordsOnly?"c":"*") + "-" + (compareWithPOS?"p":"*");
	}

	@Override
	public String getSerializable() {
		return ComparerID + "\t" + super.getSerializable() + "\t" +
			Boolean.toString(caseSensitive) + "\t" + Boolean.toString(useTokenFrequency) + "\t" +
			Boolean.toString(useBigrams) + "\t" + Boolean.toString(ignoreStopWords) + "\t" +
			Boolean.toString(ignorePunctuation) + "\t" + Boolean.toString(contentWordsOnly) + "\t" +
			Boolean.toString(compareWithPOS); 
	}

	public LexicalComparer(AbstractWordMetric _wordMetric, StopWords _stopWords, float _w2wSimThreshold,
			 boolean _useBaseForm, String _wordWeighting, String _normalize,
			 boolean _caseSensitive, boolean _useTokenFrequency, boolean _useBigrams,  boolean _ignoreStopWords,
			 boolean _ignorePunctuation, boolean _contentWordsOnly, boolean _compareWithPOS)
	{
		
		wordMetric = _wordMetric;
		stopWords = _stopWords;
		caseSensitive = _caseSensitive;
		useTokenFrequency = _useTokenFrequency;
		useBigrams = _useBigrams;
		useBaseForm = _useBaseForm;
		ignoreStopWords = _ignoreStopWords;
		ignorePunctuation = _ignorePunctuation;
		contentWordsOnly = _contentWordsOnly;
		w2wSimThreshold = _w2wSimThreshold;
		compareWithPOS = _compareWithPOS;
		wordWeighting = WordWeightType.valueOf(_wordWeighting);
		normalizeType = NormalizeType.valueOf(_normalize);
	}
	
	public LexicalComparer(StopWords _stopwords) {
		stopWords = _stopwords;
	}

	@Override
	public float ComputeSimilarity(SemanticRepresentation textA, SemanticRepresentation textB) 
	{
		Collection<String> setA = null;
		Collection<String> setB = null;
		Hashtable<String, String> posA = new Hashtable<String, String>(); 
		Hashtable<String, String> posB = new Hashtable<String, String>();
		int j;
		
		InitializeComparerOutput();
		LogComparerOutput("Lexical");

		//TOKEN FREQUENCY
		if (useTokenFrequency || useBigrams){
			setA = new ArrayList<String>(); 
			setB = new ArrayList<String>(); 
		}
		else{
			setA = new TreeSet<String>(); 
			setB = new TreeSet<String>(); 
		}
		
		for (j=0;j<textA.tokens.size();j++)
		{
			String pos = textA.tokens.get(j).POS;
			String token = textA.tokens.get(j).rawForm;

			if (ignorePunctuation && pos.length()>0 && ((!Character.isUpperCase(pos.charAt(0)) && pos.charAt(0) != '#') || (pos.charAt(0) == '#' && token.startsWith("#"))))
				continue;

			if (contentWordsOnly && SMUtils.getWordNetPOS(pos) == null)
				continue;
			
			if (ignoreStopWords && stopWords.isStopWord(textA.tokens.get(j).rawForm)) continue;
			
			if (useBaseForm) token = textA.tokens.get(j).baseForm;
			if (!caseSensitive) token = token.toLowerCase();  
			if (compareWithPOS) token += " " + textA.tokens.get(j).POS;
			if (token.length()==0) continue;
			
			setA.add(token);
			posA.put(token, pos);
		}
		for (j=0;j<textB.tokens.size();j++)
		{
			String pos = textB.tokens.get(j).POS;
			String token = textB.tokens.get(j).rawForm;

			if (ignorePunctuation && pos.length()>0 && ((!Character.isUpperCase(pos.charAt(0)) && pos.charAt(0) != '#') || (pos.charAt(0) == '#' && token.startsWith("#"))))
				continue;

			if (contentWordsOnly && SMUtils.getWordNetPOS(pos) == null)
				continue;

			if (ignoreStopWords && stopWords.isStopWord(textB.tokens.get(j).rawForm)) continue;

			if (useBaseForm) token = textB.tokens.get(j).baseForm;
			if (!caseSensitive) token = token.toLowerCase();  
			if (compareWithPOS) token += " " + textB.tokens.get(j).POS;
			if (token.length()==0) continue;
			
			setB.add(token);
			posB.put(token, pos);
		}
		
		//for bi-grams make another sets
		if (useBigrams)
		{
			ArrayList<String> setAi = (ArrayList<String>)setA;
			ArrayList<String> setBi = (ArrayList<String>)setB;
			
			//TOKEN FREQUENCY
			if (useTokenFrequency){
				setA = new ArrayList<String>(); 
				setB = new ArrayList<String>(); 
			}
			else{
				setA = new TreeSet<String>(); 
				setB = new TreeSet<String>(); 
			}
			
			for (j=0;j<setAi.size()-1;j++)
				setA.add(setAi.get(j)+ " " + setAi.get(j+1));

			for (j=0;j<setBi.size()-1;j++)
				setB.add(setBi.get(j)+ " " + setBi.get(j+1));
		}

		//compute the normalizing factor
		double normalizeA = 0;
		double normalizeB = 0;
		Iterator<String> it=setA.iterator();
		while(it.hasNext())
		{
			double weight = 0;
			String ngram = it.next();
			if (useBigrams) 
			{
				String[] unigrams = ngram.split(" ");
				weight = WordWeight(unigrams[0], wordWeighting);
				double weight2 = WordWeight(unigrams[1], wordWeighting);
				if (weight2>weight) weight = weight2; 
				//weight = (weight + weight2)/2; //taking the average gives slightly worse results
			}
			else weight = WordWeight(ngram, wordWeighting);
			
			normalizeA +=weight;
		}
		it=setB.iterator();
		while(it.hasNext())
		{
			double weight = 0;
			String ngram = it.next();
			if (useBigrams) 
			{
				String[] unigrams = ngram.split(" ");
				weight = WordWeight(unigrams[0], wordWeighting);
				double weight2 = WordWeight(unigrams[1], wordWeighting);
				if (weight2>weight) weight = weight2;
				//weight = (weight + weight2)/2; //taking the average gives slightly worse results
			}
			else weight = WordWeight(ngram, wordWeighting);
			
			normalizeB +=weight;
		}
		
		//count common tokens with weighting included
		double commonTokens = 0;
		
		double totalTokens = 0;
		switch (normalizeType)
		{
			case AVERAGE: totalTokens = normalizeA + normalizeB; break;
			case MAX: totalTokens = normalizeA>normalizeB?2*normalizeA:2*normalizeB;break;
			case MIN: totalTokens = normalizeA<normalizeB?2*normalizeA:2*normalizeB;break;
			case TEXTA: totalTokens = 2*normalizeA;break;
			case TEXTB: totalTokens = 2*normalizeB;break;
		}
		
		//there is nothing to compare
		if (totalTokens == 0) return 0;
		
		ArrayList<String> deletedSetB = new ArrayList<String>();
		it=setA.iterator();
		while(it.hasNext())
		{
			String tokenA = it.next();
			if (setB.contains(tokenA)) 
			{
				deletedSetB.add(tokenA);
				setB.remove(tokenA);
				
				double weight = 0;
				if (useBigrams)
				{
					String[] unigrams = tokenA.split(" ");
					weight = WordWeight(unigrams[0], wordWeighting);
					double weight2 = WordWeight(unigrams[1], wordWeighting);
					if (weight2>weight) weight = weight2;					
					//weight = (weight + weight2)/2; //taking the average gives slightly worse results
				}
				else weight = WordWeight(tokenA, wordWeighting);
				commonTokens += weight;
				
				LogComparerOutput(tokenA + " * " +SMUtils.ShortFloatDisplay(weight));
			}
		}
		it = deletedSetB.iterator();
		while(it.hasNext())
		{
			String tokenB = it.next();
			setA.remove(tokenB);
		}
		
		if (wordMetric != null) 
		{
		//***************************************** W2W sim pairing and counting
		
		LogComparerOutput("Similar words:");
		//After this we have removed all the common tokens and we are left with the one who are different
		//let's try to pair them also
		it = setA.iterator();
		while(it.hasNext())
		{
			String tokenA = it.next();
			String pos = posA.get(tokenA);
			
			double maximumSim = 0;
			String pairedToken = null;
			
			Iterator<String> itB = setB.iterator();
			while(itB.hasNext())
			{
				String tokenB = itB.next();
				if (pos.equals(posB.get(tokenB)))
				{
					double value = this.wordMetric.ComputeWordSimilarity(tokenA, tokenB, pos);
					
					if (value > maximumSim) {
						maximumSim = value;
						pairedToken = tokenB;
					}
				}
			}
			
			if (maximumSim > w2wSimThreshold)
			{
				double weight = (WordWeight(tokenA, wordWeighting) + WordWeight(pairedToken, wordWeighting))/2; 
				LogComparerOutput(tokenA + "=" + pairedToken + " * " + SMUtils.ShortFloatDisplay(maximumSim) + " * " + SMUtils.ShortFloatDisplay(weight));

				commonTokens += weight * maximumSim;
				setB.remove(pairedToken);
			}
		}
		//***************************************** END: W2W sim pairing and counting
		}

		comparerOutput.set(0, "Common Lexical Tokens = " + commonTokens);
		LogComparerOutput("Normalizing Factor = " + totalTokens);

		float featureValue = 2*(float)commonTokens/(float)(totalTokens);

		return featureValue;
	}
	
}
