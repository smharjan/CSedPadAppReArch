package memphis.semanticmatcher.features;

import memphis.semanticmatcher.SemanticRepresentation;
import memphis.semanticmatcher.tools.LSATasa;

public class LSAComparer extends AbstractComparer {

	static final String ComparerID = "LSA";
	
	//LSAText Feature
	public Boolean useBaseForm = true; //use the base form of the words when doing comparisons
	public LSATasa.GLOBAL_WEIGHTING lsaGlobalWeight = LSATasa.GLOBAL_WEIGHTING.NONE;
	public LSATasa.LOCAL_WEIGHTING lsaLocalWeight = LSATasa.LOCAL_WEIGHTING.NONE;

	@Override
	public String getComparerID() {
		return ComparerID + (useBaseForm?"b":"w") + "-" +
					lsaGlobalWeight.toString() + "-" + lsaLocalWeight.toString();
	}

	@Override
	public String getSerializable() {
		return ComparerID+ "\t" + Boolean.toString(useBaseForm) + "\t" +
					lsaGlobalWeight.toString() + "\t" + lsaLocalWeight.toString();
	}

	public LSAComparer(boolean _useBaseForm, String _globalWeight, String _localWeight){
		lsaGlobalWeight = LSATasa.GLOBAL_WEIGHTING.valueOf(_globalWeight);
		lsaLocalWeight = LSATasa.LOCAL_WEIGHTING.valueOf(_localWeight);
		useBaseForm = _useBaseForm;
	}
	
	@Override
	public float ComputeSimilarity(SemanticRepresentation textA, SemanticRepresentation textB) {
		//for this function we assume that the LSA vectors are already loaded in memory
		
		String[] words1 = new String[textA.tokens.size()];
		String[] words2 = new String[textB.tokens.size()];
		LSATasa lsaTool = LSATasa.getInstance();
		
		if (useBaseForm)
		{
			for (int j=0; j<words1.length;j++) words1[j] = textA.tokens.get(j).baseForm.toLowerCase(); 
			for (int j=0; j<words2.length;j++) words2[j] = textB.tokens.get(j).baseForm.toLowerCase();
		}
		else
		{
			for (int j=0; j<words1.length;j++) words1[j] = textA.tokens.get(j).rawForm.toLowerCase(); 
			for (int j=0; j<words2.length;j++) words2[j] = textB.tokens.get(j).rawForm.toLowerCase();
		}
		
		float featureValue = 0;
		try {
			featureValue = lsaTool.getTokenizedTextLSACosine(words1, words2, lsaLocalWeight, lsaGlobalWeight, useBaseForm);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return featureValue;
	}

}
