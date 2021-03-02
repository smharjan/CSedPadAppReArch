package memphis.semanticmatcher.features;

import memphis.semanticmatcher.SemanticRepresentation;
import memphis.semanticmatcher.wordmetrics.AbstractWordMetric;

public class CountDiffComparer extends PairwiseComparer{

	static final String ComparerID = "CDIF";
	
	public String diffType = "total"; //total, max, cd
	
	@Override
	public String getComparerID() {
		return ComparerID + super.getComparerID() + "-" + diffType;
	}

	@Override
	public String getSerializable() {
		return ComparerID + "\t" + super.getSerializable() + "\t" +	diffType; 
	}

	public CountDiffComparer(AbstractWordMetric _wordMetric, float _w2wSimThreshold, boolean _useBaseForm, 
			 String _wordWeighting, String _normalize, String _diffType){
		
		wordMetric = _wordMetric;
		useBaseForm = _useBaseForm;
		w2wSimThreshold = _w2wSimThreshold;
		wordWeighting = WordWeightType.valueOf(_wordWeighting);
		diffType = _diffType;
		normalizeType = NormalizeType.valueOf(_normalize);
	}

	@Override
	public float ComputeSimilarity(SemanticRepresentation textA, SemanticRepresentation textB) {
		float featureValue = 0;
		
		InitializeComparerOutput();

		if (diffType.equals("cd")) featureValue = ComputeCDDiff(textA, textB);
		if (diffType.equals("total")) featureValue = ComputeTotalLexDiff(textA, textB);
		if (diffType.equals("max")) featureValue = ComputeMaxLexDiff(textA, textB);
		
		return featureValue;
	}
	
	private float ComputeTotalLexDiff(SemanticRepresentation textA, SemanticRepresentation textB)
	{
		float result = 0;
		
		int diffNumbersA = 0;
		int diffNumbersB = 0;
		
		LogComparerOutput("Difference in uncommon Tokens:");
		for (int i=0;i<textA.tokens.size();i++)
		{
			if (textA.tokens.get(i).POS.equals("CD"))
			{
				Boolean foundNumber = false;
				for (int j=0;j<textB.tokens.size();j++)
				{
					double simValue = ComputeW2WSimilarity(useBaseForm?textA.tokens.get(i).baseForm:textA.tokens.get(i).rawForm,
							useBaseForm?textB.tokens.get(j).baseForm:textB.tokens.get(j).rawForm, textA.tokens.get(i).POS);
					if (simValue >= w2wSimThreshold) 
					{
						foundNumber = true;
						break;
					}
				}
				if (!foundNumber) diffNumbersA++;
			}
		}
			
		for (int j=0;j<textB.tokens.size();j++)
		{
			if (textB.tokens.get(j).POS.equals("CD"))
			{
				Boolean foundNumber = false;
				for (int i=0;i<textA.tokens.size();i++)
				{
					double simValue = ComputeW2WSimilarity(useBaseForm?textA.tokens.get(i).baseForm:textA.tokens.get(i).rawForm,
							useBaseForm?textB.tokens.get(j).baseForm:textB.tokens.get(j).rawForm, textA.tokens.get(i).POS);
					if (simValue >= w2wSimThreshold) 
					{
						foundNumber = true;
						break;
					}
				}
				if (!foundNumber) diffNumbersB++;
			}
		}
		
		result = Math.abs(diffNumbersA - diffNumbersB);
		float totalAverage = (textA.tokens.size()+textB.tokens.size()) / 2;
		
		return (1-result/totalAverage);
	}
	
	private float ComputeMaxLexDiff(SemanticRepresentation textA, SemanticRepresentation textB)
	{
		float result = 0;
		
		LogComparerOutput("Max different substring in A:");
		int maxDiffSubstringA = 0;
		String strDiff = "";
		String strMaxDiff = "";
		int diffSubstring = 0;
		int i = 0;
		int j = 0;
		while(i<textA.tokens.size())
		{
			Boolean pairFound = false;
			for (j=0;j<textB.tokens.size();j++)
			{
				double simValue = ComputeW2WSimilarity(useBaseForm?textA.tokens.get(i).baseForm:textA.tokens.get(i).rawForm,
						useBaseForm?textB.tokens.get(j).baseForm:textB.tokens.get(j).rawForm, textA.tokens.get(i).POS);
				if (simValue >= w2wSimThreshold) 
				{
					pairFound = true;
					break;
				}
			}
			if (pairFound) 
			{
				if (maxDiffSubstringA < diffSubstring) {
					maxDiffSubstringA = diffSubstring;
					strMaxDiff = strDiff;
				}
				diffSubstring = 0;
				strDiff = "";
			}
			else
			{
				diffSubstring++;
				strDiff += " " + textA.tokens.get(i).rawForm;
			}
			i++;
		}
		LogComparerOutput(strMaxDiff + "(" + maxDiffSubstringA + ")");
		LogComparerOutput("Max different substring in B:");
		int maxDiffSubstringB = 0;
		diffSubstring = 0;
		strMaxDiff = "";
		j = 0;
		while(j<textB.tokens.size())
		{
			Boolean pairFound = false;
			for (i=0;i<textA.tokens.size();i++)
			{
				double simValue = ComputeW2WSimilarity(useBaseForm?textA.tokens.get(i).baseForm:textA.tokens.get(i).rawForm,
						useBaseForm?textB.tokens.get(j).baseForm:textB.tokens.get(j).rawForm, textA.tokens.get(i).POS);
				if (simValue >= w2wSimThreshold) 
				{
					pairFound = true;
					break;
				}
			}
			if (pairFound) 
			{
				if (maxDiffSubstringB < diffSubstring) {
					maxDiffSubstringB = diffSubstring;
					strMaxDiff = strDiff;
				}
				diffSubstring = 0;
				strDiff = "";
			}
			else
			{
				diffSubstring++;
				strDiff += " " + textB.tokens.get(j).rawForm;
			}
			j++;
		}
		LogComparerOutput(strMaxDiff + "(" + maxDiffSubstringB + ")");

		if (maxDiffSubstringB > maxDiffSubstringA) maxDiffSubstringA = maxDiffSubstringB;
		
		result = (textA.tokens.size() + textB.tokens.size()) / 2; 
		
		return 1-((float)maxDiffSubstringA/result);
	}

	private float ComputeCDDiff(SemanticRepresentation textA, SemanticRepresentation textB)
	{
		float result = 0;
		
		int commonNumbers = 0;
		int totalNumbers = 0;
		String commonTokens = "";
		String diffTokens = "A:";
		
		LogComparerOutput("Common in Number Tokens:");
		for (int i=0;i<textA.tokens.size();i++)
		{
			if (textA.tokens.get(i).POS.equals("CD"))
			{
				Boolean foundNumber = false;
				for (int j=0;j<textB.tokens.size();j++)
				{
					if (textA.tokens.get(i).baseForm.equals(textB.tokens.get(j).baseForm))
					{
						commonTokens += " " + textA.tokens.get(i).baseForm; 
						foundNumber = true;
						break;
					}
				}
				totalNumbers++;
				if (foundNumber) commonNumbers++;
				else diffTokens += " " + textA.tokens.get(i).baseForm;
			}
		}
			
		diffTokens += "; B:";
		for (int i=0;i<textB.tokens.size();i++)
		{
			if (textB.tokens.get(i).POS.equals("CD"))
			{
				Boolean foundNumber = false;
				for (int j=0;j<textA.tokens.size();j++)
				{
					if (textA.tokens.get(j).baseForm.equals(textB.tokens.get(i).baseForm))
					{
						commonTokens += " " + textA.tokens.get(j).baseForm; 
						foundNumber = true;
						break;
					}
				}
				totalNumbers++;
				if (foundNumber) commonNumbers++;
				else diffTokens += " " + textB.tokens.get(i).baseForm;
			}
		}
		LogComparerOutput(commonTokens);
		LogComparerOutput(diffTokens);
		
		if (totalNumbers==0) result = 0.5f;
		else result = ((float)commonNumbers)/totalNumbers;
		
		return result;
	}
}
