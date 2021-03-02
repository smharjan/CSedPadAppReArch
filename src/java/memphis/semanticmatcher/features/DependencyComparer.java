package memphis.semanticmatcher.features;

import memphis.semanticmatcher.SemanticRepresentation;
import memphis.semanticmatcher.tools.SMUtils;
import memphis.semanticmatcher.wordmetrics.AbstractWordMetric;

public class DependencyComparer extends PairwiseComparer{
	
	static final String ComparerID = "DEP";

	//Dependency Feature
	public float depLevelWeight = 0;
	public float depHeadImportance = 0.5f;
	public float extraDepPenalty = 0;
	public float extraDepThreshold = 0;
	public float depNumWeight = 1;
	
	//log message - I think I used this for some kind of debugging
	public String logMessage = "";
	
	@Override
	public String getComparerID() {
		return ComparerID + super.getComparerID() + "-" + SMUtils.ShortFloatDisplay(depLevelWeight) + "-" +
		SMUtils.ShortFloatDisplay(depHeadImportance) + "-" + SMUtils.ShortFloatDisplay(extraDepPenalty) + "-" + 
		SMUtils.ShortFloatDisplay(extraDepThreshold) + "-" + SMUtils.ShortFloatDisplay(depNumWeight);
	}

	@Override
	public String getSerializable() {
		return ComparerID + "\t" + super.getSerializable() + "\t" +	depLevelWeight + "\t" + 
			depHeadImportance + "\t" + extraDepPenalty + "\t" +
			extraDepThreshold + "\t" + depNumWeight + "\t" ; 
	}

	public DependencyComparer(AbstractWordMetric _wordMetric, 
			float _w2wSimThreshold, boolean _useBaseForm, 
			String _wordWeighting, String _normalize,
			float _depLevelWeight,float _depHeadImportance,float _extraDepPenalty,
			float _extraDepThreshold, float _depNumWeight){
		
		wordMetric = _wordMetric;
		useBaseForm = _useBaseForm;
		w2wSimThreshold = _w2wSimThreshold;
		wordWeighting = WordWeightType.valueOf(_wordWeighting);
		depLevelWeight = _depLevelWeight;
		depHeadImportance = _depHeadImportance;
		extraDepPenalty = _extraDepPenalty;
		extraDepThreshold = _extraDepThreshold;
		depNumWeight = _depNumWeight;
		normalizeType = NormalizeType.valueOf(_normalize);
	}

	@Override
	public float ComputeSimilarity(SemanticRepresentation textA, SemanticRepresentation textB) {
		int j,k;
		SemanticRepresentation.DependencyStructure depA, depB, dep;
		
		logMessage = "";
		InitializeComparerOutput();
		LogComparerOutput("Common Dependencies:");
		
		if (textA.dependencies == null || textB.dependencies == null) return 0; //no dependencies to compare
		
		textA.FillInDependencyTokens(useBaseForm, true);
		textB.FillInDependencyTokens(useBaseForm, true);
		
		Boolean[] depAPaired = new Boolean[textA.dependencies.size()];
		Boolean[] depBPaired = new Boolean[textB.dependencies.size()];
		for(j=0;j<depAPaired.length;j++) depAPaired[j] = false;
		for(j=0;j<depBPaired.length;j++) depBPaired[j] = false;

		int pairedDep = 0;
		double depScore = 0;

		//first look for dependencies of same form: type(head, modifier)
		for (j=0;j<textA.dependencies.size();j++)
		{
			depA = textA.dependencies.get(j);
			for(k=0;k<textB.dependencies.size();k++)
			{
				if (depBPaired[k]) continue;
				depB = textB.dependencies.get(k);

				if(depA.type.equals(depB.type) && depA.strHead.equals(depB.strHead) && depA.strModifier.equals(depB.strModifier))
				{
					depBPaired[k] = true; depAPaired[j] = true;
					pairedDep++;
					
					//TODO: here we can weight the dependency based on: 1) IDF; 2) dependency level; 3) dependency type
					//depB = data.get(i).textB.dependencies.get(pairedDepBIndex);
					//maxSim -= 0.4 * (depA.depthInTree + depB.depthInTree) * feature.depLevelWeight;
					
					double detTypeWeight = 1;
					//if (dep.type.equalsIgnoreCase("num")) detTypeWeight = 10;
					
					detTypeWeight = detTypeWeight * (WordWeight(depA.strHead, wordWeighting) + WordWeight(depB.strHead, wordWeighting)
							+ WordWeight(depA.strModifier, wordWeighting) + WordWeight(depB.strModifier, wordWeighting)) / 4;
					
					depScore += detTypeWeight; 
					
					LogComparerOutput(depA.type + "(" + depA.strHead + "," + depA.strModifier + ") = " + depB.type + "(" + depB.head + "," + depB.strModifier + ")");
					
					continue;
				}
			}
		}
		
		logMessage += pairedDep + "(" + depScore + ") - ";
		
		//now try to pair the other dependencies
		for (j=0;j<textA.dependencies.size();j++)
		{
			if (depAPaired[j]) continue;
			
			depA = textA.dependencies.get(j);

			double maxSim = 0;
			int pairedDepBIndex = -1;
			
			for(k=0;k<textB.dependencies.size();k++)
			{
				if (depBPaired[k]) continue;
				
				depB = textB.dependencies.get(k);
				
				//compare only if dep type and words' pos are the same
				if (!depA.type.equals(depB.type) ||	!depA.strPOShead.equals(depB.strPOShead) ||	!depA.strPOSmodifier.equals(depB.strPOSmodifier))
				{
					continue;
				}
				
				double simValue = 0.001;
				if (depA.strHead.equals(depB.strHead)) simValue += depHeadImportance;
				else
				{
					if (wordMetric != null) simValue += wordMetric.ComputeWordSimilarity(depA.strHead, depB.strHead, depA.strPOShead) * depHeadImportance; 		
				}

				if (depA.strModifier.equals(depB.strModifier)) simValue += (1 - depHeadImportance);
				else
				{
					if (wordMetric != null) simValue += wordMetric.ComputeWordSimilarity(depA.strModifier, depB.strModifier, depA.strPOShead) * (1 - depHeadImportance); 		
				}
				
				if (simValue > maxSim)
				{
					maxSim = simValue;
					pairedDepBIndex = k;
				}
			}

			if(maxSim > w2wSimThreshold) //it means we can pair these dependencies
			{
				depBPaired[pairedDepBIndex] = true; depAPaired[j] = true;
				pairedDep++;
				
				depB = textB.dependencies.get(pairedDepBIndex);
				
				//TODO: here we can weight the dependency based on: 1) IDF; 2) dependency level; 3) dependency type
				//depB = data.get(i).textB.dependencies.get(pairedDepBIndex);
				//maxSim -= 0.4 * (depA.depthInTree + depB.depthInTree) * feature.depLevelWeight;
				
				double detTypeWeight = 1;
				if (depA.type.equalsIgnoreCase("num")) detTypeWeight = depNumWeight;
				
				detTypeWeight = detTypeWeight * (WordWeight(depA.strHead, wordWeighting) + WordWeight(depA.strModifier, wordWeighting)
						+ WordWeight(depB.strHead, wordWeighting) + WordWeight(depB.strModifier, wordWeighting)) / 4;

				depScore += maxSim * detTypeWeight; 

				LogComparerOutput(depA.type + "(" + depA.strHead + "," + depA.strModifier + ") = " + depB.type + "(" + depB.strHead + "," + depB.strModifier + ") " + SMUtils.ShortFloatDisplay(maxSim * detTypeWeight));
			}
		}

		logMessage += pairedDep + "(" + depScore + ") - ";

		double dissScoreA = 0;
		
		LogComparerOutput("---------------------------");
		LogComparerOutput("Different Dependencies in A:");
		
		//now compute the dissimilarity score based on the unpaired dependencies
		for (j=0;j<textA.dependencies.size();j++)
		{
			if (depAPaired[j]) continue;
			
			dep = textA.dependencies.get(j);
			
			double depValue = 1 - dep.depthInTree * depLevelWeight;
			if (depValue < 0) depValue = 0;
			
			//TODO: here I should use the word specificity weighting; and dependency type weighting
			double detTypeWeight = 1;
			if (dep.type.equalsIgnoreCase("num")) detTypeWeight = depNumWeight;
			
			detTypeWeight = detTypeWeight * (WordWeight(dep.strHead, wordWeighting) + WordWeight(dep.strModifier, wordWeighting))/2;

			dissScoreA += depValue * detTypeWeight;
			
			LogComparerOutput(dep.type + "(" + dep.strHead + "," + dep.strModifier + ") " + SMUtils.ShortFloatDisplay(depValue * detTypeWeight));
		}
		
		logMessage += (textA.dependencies.size() - pairedDep) + "(" + dissScoreA + ") - ";

		double dissScoreB = 0;
		
		LogComparerOutput("---------------------------");
		LogComparerOutput("Different Dependencies in B:");
		
		//now compute the dissimilarity score based on the unpaired dependencies
		for (j=0;j<textB.dependencies.size();j++)
		{
			if (depBPaired[j]) continue;
			
			dep = textB.dependencies.get(j);

			double depValue = 1 - dep.depthInTree * depLevelWeight;
			if (depValue < 0) depValue = 0;
			
			//TODO: here I should use the word specificity weighting; and dependency type weighting
			double detTypeWeight = 1;
			if (dep.type.equalsIgnoreCase("num")) detTypeWeight = depNumWeight;
			detTypeWeight = detTypeWeight * (WordWeight(dep.strHead, wordWeighting) + WordWeight(dep.strModifier, wordWeighting))/2;
			
			dissScoreB += depValue * detTypeWeight; 
			
			LogComparerOutput(dep.type + "(" + dep.strHead + "," + dep.strModifier + ") " + SMUtils.ShortFloatDisplay(depValue * detTypeWeight));
		}
		
		logMessage += (textB.dependencies.size() - pairedDep) + "(" + dissScoreB + ") - ";

		//use the difference in dependency penalty
		if (depBPaired.length - depAPaired.length > extraDepThreshold) dissScoreB += extraDepPenalty;
		
		logMessage += "p(" + dissScoreB + ")";

		float featureValue = (float) (depScore / ((dissScoreA + dissScoreB + 0.001)));
		
		return featureValue;
	}

}
