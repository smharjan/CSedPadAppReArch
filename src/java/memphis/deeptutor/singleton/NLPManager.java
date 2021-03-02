package memphis.deeptutor.singleton;

import java.util.ArrayList;
import java.util.Arrays;

import memphis.semanticmatcher.Preprocessor;
import memphis.semanticmatcher.SemanticRepresentation;
import memphis.semanticmatcher.features.LexicalComparer;
import memphis.semanticmatcher.features.PairwiseComparer.NormalizeType;
import memphis.semanticmatcher.tools.LSATasa;
import memphis.semanticmatcher.tools.SMUtils;
import memphis.semanticmatcher.tools.StopWords;
import memphis.semanticmatcher.tools.WordNetSimilarity;
import memphis.semanticmatcher.wordmetrics.DeepTutorWordMetric;
import memphis.semanticmatcher.wordmetrics.LSAWordMetric;

public class NLPManager {
	Preprocessor preprocessor = null;
	
	private static NLPManager instance = null;
	
	String[] strNegTerms = new String[]{"not","n't","cannot","neither","no","never","nobody","none","nothing","shan't","never"};
	
    public static NLPManager getInstance() {
    	if (instance == null) 
    	{
    		try{
    			instance = new NLPManager();
    			//All that the LSA class contains for now is the index list of the LSA terms
    			instance.InitNLPManager();
    			//load any WordNet cache available
    			WordNetSimilarity.getInstance().LoadCache();
    		}
    		catch(Exception e) {return null;}
    	}
        return instance;
    }
    
    private NLPManager() {}
    
    private void InitNLPManager() throws Exception
    {
		preprocessor = new Preprocessor();

		//preprocessor.initialize(Preprocessor.TokenizerType.OPENNLP, Preprocessor.TaggerType.OPENNLP_PERCEPTRON,
		//						Preprocessor.StemmerType.WORDNET, Preprocessor.ParserType.NONE);
		//preprocessor.initOpenNLP();
		
		preprocessor.initialize(Preprocessor.TokenizerType.STANFORD, Preprocessor.TaggerType.STANFORD,
										Preprocessor.StemmerType.STANFORD, Preprocessor.ParserType.NONE);
		preprocessor.initStanford();
    }

	public String[] SplitIntoSentences(String text)
	{
		return preprocessor.GetSentences(text);
	}
	
	public void PreprocessText(SemanticRepresentation text)
	{
		//Mihai: quick fix - in case of answers that contains the token "no." remove the period from the negation
		text.text = text.text.replaceAll("\\b(?i)(no\\.)(?!\\w)","no .");

		preprocessor.PreprocessRepresentation(text);
	}
	
	public float ComputeT2TLSASimilarity(String textA, String textB)
	{
		float result = 0;

		SemanticRepresentation a = new SemanticRepresentation(textA);
		SemanticRepresentation b = new SemanticRepresentation(textB);
		
		PreprocessText(a);
		PreprocessText(b);
		
		LSATasa lsaTool = LSATasa.getInstance(); 
		lsaTool.ExtractLSAVectors4Representation(a);
		lsaTool.ExtractLSAVectors4Representation(b);
		
		//Compute similarity
		LSAWordMetric wmetric = new LSAWordMetric(true);
		LexicalComparer lexcomp = new LexicalComparer(new StopWords());
		lexcomp.wordMetric = wmetric;
		lexcomp.w2wSimThreshold = 0.1f;
		lexcomp.useBaseForm = true;
		
		result = lexcomp.ComputeSimilarity(a, b);

		return result;
	}
	
	public float ComputeT2TWNSimilarity(String textA, String textB)
	{
		float result = 0;
		
		if (textB.length() == 0) return 1;

		SemanticRepresentation a = new SemanticRepresentation(textA);
		SemanticRepresentation b = new SemanticRepresentation(textB);
		
		PreprocessText(a);
		PreprocessText(b);

		//Compute similarity
		DeepTutorWordMetric wmetric = new DeepTutorWordMetric();
		LexicalComparer lexcomp = new LexicalComparer(new StopWords());
		lexcomp.wordMetric = wmetric;
		lexcomp.w2wSimThreshold = 0.2f;
		lexcomp.useBaseForm = true;
		lexcomp.ignorePunctuation = true;
		lexcomp.normalizeType = NormalizeType.TEXTB;
		
		result = lexcomp.ComputeSimilarity(a, b);

		//LMC detect negation and eliminate the match if textA has a negation, but textB doesn't
		boolean negA = DetectNegation(a); 
		boolean negB = DetectNegation(b);
		//System.out.println(negA + " - " + negB + " : " + result);
		if (negA == true && negB == false) return (-result);

		return result;
	}
	
	public boolean DetectNegation(SemanticRepresentation text)
	{
		boolean result = false;//by default there is no negation
		ArrayList<String> negTerms = new ArrayList<String>(Arrays.asList(strNegTerms)); 
		for (int i=0;i<text.tokens.size();i++)
		{
			if (negTerms.contains(text.tokens.get(i).baseForm.toLowerCase().trim())) result = !result;
		}
		return result;
	}
	
	//Mihai: This checks whether there are any negations in front of keywords
	public boolean IsTermNegated(SemanticRepresentation text, int index)
	{
		ArrayList<String> negTerms = new ArrayList<String>(Arrays.asList(strNegTerms));
		
		if (index == 0) return false;
		
		SemanticRepresentation.LexicalTokenStructure prevToken = text.tokens.get(index-1);
		
		if (negTerms.contains(prevToken.baseForm.toLowerCase().trim())) return true;
		
		if (index == 1) return false;
		
		//check if prev token is punctuation; 
		if (prevToken.POS.length()>0 && ((!Character.isUpperCase(prevToken.POS.charAt(0)) && prevToken.POS.charAt(0) != '#') || (prevToken.POS.charAt(0) == '#' && prevToken.baseForm.startsWith("#"))))
			return false;
		
		//check if previous token is content word
		if (SMUtils.getWordNetPOS(prevToken.POS) != null && SMUtils.getWordNetPOS(prevToken.POS).matches("n|v")) return false;

		if (negTerms.contains(text.tokens.get(index-2).baseForm.toLowerCase().trim())) return true;
		
		return false;
	}
	
	public boolean MatchRegularExpression(SemanticRepresentation text, String expr, Boolean negationCheck)
	{
		//process all expression in the brackets 
		while (expr.contains("("))
		{
			int openingBracket = expr.indexOf('(');
			int closingBracket = FindClosingBracket(expr.toCharArray(), openingBracket);
			if (closingBracket == -1) closingBracket = expr.length(); //we assume that the closing bracket at the end of the text is missing
			boolean valueInBracket = MatchRegularExpression(text, expr.substring(openingBracket+1, closingBracket), negationCheck);
			expr = expr.substring(0, openingBracket) + (valueInBracket?"#TRUE#":"#FALSE#") + (closingBracket<expr.length()?expr.substring(closingBracket+1, expr.length()):"");
		}
		
		//at this point we should have only tokens separated by | and ,
		String[] itemSets = new String[1];
		if (expr.contains("|"))	itemSets = expr.split("\\|");
		else itemSets[0] = expr;
		
		for (int i = 0; i < itemSets.length; i++) {
			String[] items = itemSets[i].split(",");
			boolean itemSetCovered=true; 
			for (int j = 0; j < items.length; j++) {
				
				boolean contains = false;
				
				if (items[j].trim().equals("#TRUE#")) contains = true;
				else if (items[j].trim().equals("#FALSE#")) contains = false;
				else
				{
					String item = items[j].trim();
					
					boolean negation = false;
					boolean strictCheck = true;
					if (item.length()>0 && item.charAt(0)=='!'){
						negation = false;
						item = item.substring(1);
					}

					if (item.length()>0 && item.charAt(0)=='*'){
						strictCheck = true;
						item = item.substring(1);
					}

					for (int k = 0; k < text.tokens.size(); k++) {

					 	if (!strictCheck)
						{
							String token = text.tokens.get(k).baseForm;
							float simValue = (float)WordNetSimilarity.getInstance().GetWNSimilarityNoPOS(WordNetSimilarity.WNSimMeasure.LIN,
																										 false, token, item.trim());
							if (simValue > 0.8) contains = true;
						}
						
						if (text.tokens.get(k).baseForm.equalsIgnoreCase(item)|| text.tokens.get(k).rawForm.equalsIgnoreCase(item)) 
						{
							if (!negationCheck || IsTermNegated(text, k) == negation) 
								contains = true;
						}
					}
				}
				if (!contains) itemSetCovered = false;
			}
			if (itemSetCovered) return true;
		}
		
		return false;
	}

	private int FindClosingBracket(char[] text, int openingBracket)
	{
		int level = 1;
		for(int i=openingBracket+1;i<text.length;i++)
		{
			if (text[i]==')') {
				level--;
				if (level ==0) return i;
			}
			if (text[i]=='(') level++;
		}
		return -1;
	}
	
	public boolean ContainsIt(SemanticRepresentation text)
	{
		for(int i=0;i<text.tokens.size();i++)
		{
			if (text.tokens.get(i).rawForm.equalsIgnoreCase("it")) return true;
		}
		return false;
	}

	public String ConstructReplaceIt(SemanticRepresentation text)
	{
		String result = "";
		for(int i=0;i<text.tokens.size();i++)
		{
			if (text.tokens.get(i).rawForm.equalsIgnoreCase("it")) result += " <<IT>>";
			else result += " " + text.tokens.get(i).rawForm;
		}
		return result.trim();
	}
}
