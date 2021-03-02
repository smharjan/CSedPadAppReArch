package memphis.semanticmatcher.features;

import java.util.ArrayList;

import memphis.semanticmatcher.SemanticRepresentation;
import memphis.semanticmatcher.tools.HungarianAlgorithm;
import memphis.semanticmatcher.tools.SMUtils;
import memphis.semanticmatcher.wordmetrics.AbstractWordMetric;

public class OptimumComparer extends PairwiseComparer{

	static final String ComparerID = "OPTLEX";
	
	@Override
	public String getComparerID() {
		return ComparerID + super.getComparerID();
	}

	@Override
	public String getSerializable() {
		return ComparerID + "\t" + super.getSerializable(); 
	}

	public OptimumComparer(AbstractWordMetric _wordMetric, float _w2wSimThreshold, boolean _useBaseForm,
			 String _wordWeighting, String _normalize){
		
		wordMetric = _wordMetric;
		useBaseForm = _useBaseForm;
		w2wSimThreshold = _w2wSimThreshold;
		wordWeighting = WordWeightType.valueOf(_wordWeighting);
		normalizeType = NormalizeType.valueOf(_normalize);
	}

	@Override
	public float ComputeSimilarity(SemanticRepresentation textA, SemanticRepresentation textB) {

		InitializeComparerOutput();

		LogComparerOutput("Optimum lexical matching:");

		//take out the punctuation
		ArrayList<String> setA = new ArrayList<String>(); 
		ArrayList<String> setB = new ArrayList<String>(); 
		ArrayList<String> posA = new ArrayList<String>(); 
		ArrayList<String> posB = new ArrayList<String>(); 
		
		for (int j=0;j<textA.tokens.size();j++)
		{
			String pos = textA.tokens.get(j).POS;
			String token = useBaseForm?textA.tokens.get(j).baseForm:textA.tokens.get(j).rawForm;

			//ignore punctuation
			if (pos.length()>0 && ((!Character.isUpperCase(pos.charAt(0)) && pos.charAt(0) != '#') || (pos.charAt(0) == '#' && token.startsWith("#"))))
				continue;
			
			setA.add(token);
			posA.add(pos);
		}
		for (int j=0;j<textB.tokens.size();j++)
		{
			String pos = textB.tokens.get(j).POS;
			String token = useBaseForm?textB.tokens.get(j).baseForm:textB.tokens.get(j).rawForm;

			if (pos.length()>0 && ((!Character.isUpperCase(pos.charAt(0)) && pos.charAt(0) != '#') || (pos.charAt(0) == '#' && token.startsWith("#"))))
				continue;

			setB.add(token);
			posB.add(pos);
		}

		//compute the similarity matrix
		int maxDim = setA.size();
		if (setB.size() > maxDim) maxDim = setB.size();
		
		double[][] simMatrix = new double[maxDim][maxDim];
		
		for (int i=0; i<maxDim; i++)
			for (int j=0; j<maxDim; j++)
			{
				if (i < setA.size() && j < setB.size())
				{
					simMatrix[i][j] = ComputeW2WSimilarity(setA.get(i), setB.get(j), posA.get(i));
					if (simMatrix[i][j] < w2wSimThreshold) simMatrix[i][j] = 0;
				}
				else
				{
					simMatrix[i][j] = 0;
				}
			}
		
		int[][] assignment = new int[maxDim][2];
		assignment = HungarianAlgorithm.hgAlgorithm(simMatrix, "max");	//Call Hungarian algorithm.
		
		double sum = 0;
		for (int i=0; i<assignment.length; i++)
		{
			String tokenA = "NA";
			if (assignment[i][0] < setA.size()) tokenA = setA.get(assignment[i][0]);
			String tokenB = "NA";
			if (assignment[i][1] < setB.size()) tokenB = setB.get(assignment[i][1]);
	
			Double pairedSim = simMatrix[assignment[i][0]][assignment[i][1]];
			if (pairedSim > 0)
			{
				LogComparerOutput(tokenA + " = " + tokenB + "(" + SMUtils.ShortFloatDisplay(pairedSim) + ")");
				sum = sum + pairedSim;
			}
		}
		LogComparerOutput("Total Sum of Matched Tokens = " + SMUtils.ShortFloatDisplay(sum));
		LogComparerOutput("Normalizing Factor = " + maxDim);
		
		return (float)(sum/maxDim);
	}
}
