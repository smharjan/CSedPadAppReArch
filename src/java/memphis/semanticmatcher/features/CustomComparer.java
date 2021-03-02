package memphis.semanticmatcher.features;

import java.util.HashSet;
import java.util.Hashtable;

import memphis.semanticmatcher.SemanticRepresentation;
import memphis.semanticmatcher.tools.WordNetSimilarity;

public class CustomComparer extends AbstractComparer{

	static final String ComparerID = "CUSTOM";

	//Custom Feature
	public String customDefine = "";
	
	@Override
	public String getComparerID() {
		String shortDefine = customDefine.replaceAll(" ","");
		if (shortDefine.length()>10) shortDefine = shortDefine.substring(0,10);
		return ComparerID+ "-" + shortDefine;
	}

	@Override
	public String getSerializable() {
		return ComparerID+ "\t" + customDefine;
	}

	public CustomComparer(String _customDefine){
		
		customDefine = _customDefine;
	}

	@Override
	public float ComputeSimilarity(SemanticRepresentation textA, SemanticRepresentation textB) {
		float featureValue = 0;

		InitializeComparerOutput();

		if (customDefine.startsWith("wan-")) featureValue = ComputeWanFeatures(textA, textB);

		return featureValue;
	}
	
	private float ComputeWanFeatures(SemanticRepresentation textA, SemanticRepresentation textB)
	{
		LogComparerOutput("Wan-based Features:" +customDefine);

		if (customDefine.endsWith("diff"))
		{
			boolean useLemma = customDefine.startsWith("wan-l");
			
			HashSet<String> uniqueTokensA = new HashSet<String>();
			for(int i=0;i<textA.tokens.size();i++)
				uniqueTokensA.add(useLemma?textA.tokens.get(i).baseForm:textA.tokens.get(i).rawForm);
			
			HashSet<String> uniqueTokensB = new HashSet<String>();
			for(int i=0;i<textB.tokens.size();i++)
				uniqueTokensB.add(useLemma?textB.tokens.get(i).baseForm:textB.tokens.get(i).rawForm);

/*			if (feature.customDefine.endsWith("adiff"))
				return Math.abs(uniqueTokensA.size() - uniqueTokensB.size());
			else
				return uniqueTokensA.size() - uniqueTokensB.size();*/  

			if (customDefine.endsWith("adiff"))
				return Math.abs(textA.tokens.size() - textB.tokens.size());
			else
				return textA.tokens.size() - textB.tokens.size();
		}
		//************************
		if (customDefine.startsWith("wan-1"))
		{
			boolean useLemma = customDefine.startsWith("wan-1l");
			boolean useWN = customDefine.startsWith("wan-1lwn");
			WordNetSimilarity wntools = WordNetSimilarity.getInstance();

			HashSet<String> uniqueTokensA = new HashSet<String>();
			Hashtable<String, String> posHash = new Hashtable<String, String>(); 
			for(int i=0;i<textA.tokens.size();i++)
			{
				uniqueTokensA.add(useLemma?textA.tokens.get(i).baseForm:textA.tokens.get(i).rawForm);
				posHash.put(useLemma?textA.tokens.get(i).baseForm:textA.tokens.get(i).rawForm, textA.tokens.get(i).POS);
			}
			
			HashSet<String> uniqueTokensB = new HashSet<String>();
			for(int i=0;i<textB.tokens.size();i++){
				uniqueTokensB.add(useLemma?textB.tokens.get(i).baseForm:textB.tokens.get(i).rawForm);
				posHash.put(useLemma?textB.tokens.get(i).baseForm:textB.tokens.get(i).rawForm, textB.tokens.get(i).POS);
			}

			float commonTokens = 0;
			for(String s:uniqueTokensA){
				for (String sB:uniqueTokensB){
					if (s.equalsIgnoreCase(sB)) {commonTokens++;break;}
					if (useWN)
					{
						double sim = wntools.GetWNSimilarity(WordNetSimilarity.WNSimMeasure.LIN, false, s.toLowerCase(), sB.toLowerCase(), posHash.get(s));
						if (sim > 0.9) {
							commonTokens++;break;
						}
					}
				}
				//if (uniqueTokensB.contains(s)) commonTokens++;
			}

			float recall =  commonTokens/uniqueTokensB.size();
			float precision =  commonTokens/uniqueTokensA.size();
			
			if (customDefine.endsWith("p")) return precision;
			if (customDefine.endsWith("r")) return recall;
			if (customDefine.endsWith("f")) return 2*(precision * recall)/(precision+recall);

			return 0;
		}

		if (customDefine.startsWith("wan-dep"))
		{
			boolean useLemma = customDefine.startsWith("wan-depl");
		
			textA.FillInDependencyTokens(useLemma, true);
			textB.FillInDependencyTokens(useLemma, true);
			
			HashSet<String> uniqueTokensA = new HashSet<String>();
			for(int i=0;i<textA.dependencies.size();i++)
				uniqueTokensA.add(textA.dependencies.get(i).strHead + "+" + textA.dependencies.get(i).strModifier);
			
			HashSet<String> uniqueTokensB = new HashSet<String>();
			for(int i=0;i<textB.dependencies.size();i++)
				uniqueTokensB.add(textB.dependencies.get(i).strHead + "+" + textB.dependencies.get(i).strModifier);
			
			float commonTokens = 0;
			for(String s:uniqueTokensA) if (uniqueTokensB.contains(s)) commonTokens++;

			float recall =  commonTokens/uniqueTokensB.size();
			float precision =  commonTokens/uniqueTokensA.size();
			
			if (customDefine.endsWith("p")) return precision;
			if (customDefine.endsWith("r")) return recall;
			if (customDefine.endsWith("f")) return 2*(precision * recall)/(precision+recall+0.00001f);
			
			//return commonTokens/(uniqueTokensA.size() + uniqueTokensB.size()); 
			
			//return 0;
		}
		
		if (customDefine.startsWith("wan-blue"))
		{
			boolean useLemma = customDefine.startsWith("wan-bluel");
			
			int maxN = 3;
			if (customDefine.contains("1")) maxN = 1; 
			if (customDefine.contains("2")) maxN = 2; 
			if (customDefine.contains("3")) maxN = 3; 
			if (customDefine.contains("4")) maxN = 4; 

			float p[] = new float[maxN];
			for (int n=0;n<maxN;n++)
			{
				HashSet<String> uniqueTokensA = new HashSet<String>();
				for(int i=0;i<textA.tokens.size()-n;i++)
				{
					String ngram = "";
					for (int j=0;j<=n;j++) ngram = " " + (useLemma?textA.tokens.get(i+j).baseForm:textA.tokens.get(i+j).rawForm);
					uniqueTokensA.add(ngram);
				}
				
				HashSet<String> uniqueTokensB = new HashSet<String>();
				for(int i=0;i<textB.tokens.size()-n;i++)
				{
					String ngram = "";
					for (int j=0;j<=n;j++) ngram = " " + (useLemma?textB.tokens.get(i+j).baseForm:textB.tokens.get(i+j).rawForm);
					uniqueTokensB.add(ngram);
				}
	
				float commonTokens = 0;
				for(String s:uniqueTokensA)	if (uniqueTokensB.contains(s)) commonTokens++;
				
				if (customDefine.endsWith("p"))	p[n] = commonTokens/uniqueTokensA.size();
				else p[n] = commonTokens/uniqueTokensB.size();
			}

			double logSum = 0;
			for (int n=0;n<maxN;n++) logSum+= Math.log(p[n]);
			logSum = logSum/maxN;
			double bleu = Math.exp(logSum);
			
			return (float)bleu;
		}

		return 0;
	}
}
