package dt.core.semantic;

import dt.config.ConfigManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import dt.core.semantic.tools.PorterStemmer;
import dt.core.semantic.tools.SimplePTBTokenizer;
import dt.core.semantic.tools.WordNetSimilarity;

import edu.stanford.nlp.ling.CoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;

import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class Preprocessor {
	
	public TextProcessingLogger logger = TextProcessingLogger.TextProcessingLogger_Null;
	
	public static final String OPEN_NLP_MODEL_PATH = ConfigManager.getLocalRoot() + "\\opennlp-tools-1.5.0\\Models-1.5\\"; 
	
	public enum TokenizerType {StanfordPTB, STANFORD, OPENNLP};
	public enum TaggerType {STANFORD, OPENNLP_ENTROPY, OPENNLP_PERCEPTRON};
	public enum StemmerType {STANFORD, PORTER, WORDNET_SIMPLE, WORDNET};
	public enum ParserType {NONE, STANFORD, OPENNLP};
	
	public TokenizerType tokenizerType;
	public TaggerType taggerType;
	public StemmerType stemmerType;
	public TaggerType loadedTaggerType;
	public ParserType parserType;
	
	//OpenNLP tools
	Object tokenizer = null; //the Tokenizer class cannot be used here without the OpenNLP library

	SentenceDetectorME sDetector = null;
	POSTaggerME tagger = null;
	Parser openNLPparser = null;

	PorterStemmer stemmer = new PorterStemmer();
	
	//Stanford tools
	StanfordCoreNLP stanfordPipeline = null;
	boolean stanfordParserLoaded = false;
	
	public Preprocessor()
	{
	}
	
	public void initialize(TokenizerType _tokenizerType, TaggerType _taggerType, StemmerType _stemmerType, ParserType _parserType) {
		tokenizerType = _tokenizerType;
		taggerType = _taggerType;
		stemmerType = _stemmerType;
		parserType = _parserType;
	}

	public void initOpenNLP() throws Exception
	{
		if (tagger != null) 
		{
			initOpenNLPTagger();
			if (this.parserType == ParserType.OPENNLP && openNLPparser == null) initOpenNLPParser();

			return;
		}
		
		String strTagger;
		InputStream modelIn = null;
		
		logger.Log("Initializing Open NLP...");

		if (taggerType == TaggerType.OPENNLP_ENTROPY) strTagger = "maxent";
		else strTagger = "perceptron";
	
		SentenceModel sModel = null;
		TokenizerModel tModel = null;
		POSModel pModel = null;
		
		//sR.ui.LogToConsole("Loading Sentence Model...");
		modelIn = new FileInputStream(OPEN_NLP_MODEL_PATH+"en-sent.bin");
		sModel = new SentenceModel(modelIn);
		modelIn.close();
			
		//sR.ui.LogToConsole("Loading Tokenizer Model...");
		modelIn = new FileInputStream(OPEN_NLP_MODEL_PATH+"en-token.bin");
		tModel = new TokenizerModel(modelIn);
		modelIn.close();
		
		//sR.ui.LogToConsole("Loading POS Tagger Model...");
		modelIn = new FileInputStream(OPEN_NLP_MODEL_PATH+"en-pos-"+strTagger+".bin");
		pModel = new POSModel(modelIn);
		modelIn.close();

		tagger = new POSTaggerME(pModel);
		sDetector = new SentenceDetectorME(sModel);
		tokenizer = new TokenizerME(tModel);
		
		loadedTaggerType = taggerType;
		
		if (this.parserType == ParserType.OPENNLP) initOpenNLPParser();

		logger.LogOverwrite("Initializing Open NLP...done");
	}
	
	void initOpenNLPParser() throws Exception
	{
		logger.Log("Loading OpenNLP Parser...");
		InputStream modelIn = new FileInputStream(OPEN_NLP_MODEL_PATH+"en-parser-chunking.bin");
		ParserModel parserModel = new ParserModel(modelIn);
		modelIn.close();
		openNLPparser = ParserFactory.create(parserModel);
		logger.LogOverwrite("Initializing OpenNLP Parser...done");
	}
	
	void initOpenNLPTagger() throws Exception
	{
		if (loadedTaggerType == taggerType) return;
		
		POSModel pModel = null;
		String strTagger;
		InputStream modelIn = null;
		
		logger.Log("Initializing Open NLP tagger...");

		if (taggerType == TaggerType.OPENNLP_ENTROPY) strTagger = "maxent";
		else strTagger = "perceptron";

		//sR.ui.LogToConsole("Loading POS Tagger Model...");
		modelIn = new FileInputStream(OPEN_NLP_MODEL_PATH+"en-pos-"+strTagger+".bin");
		pModel = new POSModel(modelIn);
		modelIn.close();

		tagger = new POSTaggerME(pModel);
		loadedTaggerType = taggerType;

		logger.LogOverwrite("Initializing Open NLP tagger...done");
	}

	public void initStanford()
	{
		if (stanfordPipeline != null) 
		{
			if (parserType == ParserType.STANFORD && stanfordParserLoaded == true) return;
			if (parserType != ParserType.STANFORD && stanfordParserLoaded == false) return;
		}
		
		Properties props = new Properties();
		if (parserType == ParserType.STANFORD)
		{
			props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
			stanfordParserLoaded = true;
		}
		else
		{
			props.put("annotators", "tokenize, ssplit, pos, lemma");
			stanfordParserLoaded = false;
		}
			
		logger.Log("Initializing Stanford NLP...");
		stanfordPipeline = new StanfordCoreNLP(props);
		logger.LogOverwrite("Initializing Stanford NLP...done");
	}
	
	private String[] Tokenize(String text)
	{
		return ((Tokenizer)tokenizer).tokenize(text);
	}

	public String[] GetSentences(String text)
	{
		if (sDetector != null) return sDetector.sentDetect(text);
		
		if (stanfordPipeline!=null)
		{
			Properties props = new Properties();
			props.put("annotators","tokenize, ssplit");
			StanfordCoreNLP sentPipeline = new StanfordCoreNLP(props);
			Annotation document = new Annotation(text);
			sentPipeline.annotate(document);
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			String[] result = new String[sentences.size()];
			for(int i=0;i<result.length;i++){
				result[i] = sentences.get(i).get(TextAnnotation.class);
			}
			return result;
		}
		
		return null;
	}
	
	private String[] TagSentence(String[] sentence)
	{
		return tagger.tag(sentence);
	}
	
	public void PreprocessStanfordText(SemanticRepresentation myData)
	{
		//input
		String text = myData.text;

		ArrayList<SemanticRepresentation.LexicalTokenStructure> tokens = new ArrayList<SemanticRepresentation.LexicalTokenStructure>();
		ArrayList<SemanticRepresentation.DependencyStructure> dependencies = new ArrayList<SemanticRepresentation.DependencyStructure>();
		ArrayList<String> syntacticTrees = new ArrayList<String>();
		ArrayList<String> dependencyTrees = new ArrayList<String>();
		
	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	    
	    // run all Annotators on this text
	    stanfordPipeline.annotate(document);
	    
		// these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);

	    int sentenceIndex = 0;
	    int firstIndexInSentence = 0;
        for(CoreMap sentence: sentences) {
	        // traversing the words in the current sentence
	        // a CoreLabel is a CoreMap with additional token-specific methods
	        for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	        	// this is the text of the token
	        	SemanticRepresentation.LexicalTokenStructure lex = new SemanticRepresentation.LexicalTokenStructure();
				lex.rawForm = token.get(TextAnnotation.class);
				lex.POS = token.get(PartOfSpeechAnnotation.class);
				lex.baseForm = (stemmerType==StemmerType.STANFORD)?token.getString(LemmaAnnotation.class):getLemma(lex.rawForm, lex.POS);
        		lex.sentenceIndex = sentenceIndex;
				tokens.add(lex);
	        }
	        
	        if (parserType == ParserType.STANFORD)
	        {
	        	Tree tree = sentence.get(TreeAnnotation.class);
	        	if (tree != null)
	        	{
	        		syntacticTrees.add(tree.toString());
	        	}
	        		
		        SemanticGraph dependencyTree = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		        if (dependencyTree != null)
		        {
		        	List<SemanticGraphEdge> mydeps = dependencyTree.edgeList();
		        	if (mydeps.size() > 0)
		        	{
		        		//dependencyTrees.add(dependencyTree.toCompactString()); //this is to check if my serialized tree is correct
			        	dependencyTrees.add(SerializeDependencyTree(dependencyTree, firstIndexInSentence));
		        	}
		        	else
		        		dependencyTrees.add("");
		        
		        	for (int i = 0;i<mydeps.size();i++){
		        		SemanticGraphEdge myedge = mydeps.get(i);
		        		IndexedWord depHead = myedge.getGovernor();
		        		IndexedWord depModif = myedge.getDependent();
		        		GrammaticalRelation depType = myedge.getRelation();
		        		List<IndexedWord> pathToRoot = dependencyTree.getPathToRoot(depHead);
		        		int depDepth = (pathToRoot!=null)?pathToRoot.size():0;
		        		
		        		SemanticRepresentation.DependencyStructure myDep = new SemanticRepresentation.DependencyStructure();
		        		myDep.head = firstIndexInSentence + depHead.index()-1;
		        		myDep.modifier = firstIndexInSentence + depModif.index()-1;
		        		myDep.type = depType.toString();
		        		myDep.depthInTree = depDepth;
		        		myDep.sentenceIndex = sentenceIndex;
		        		myDep.depthInTree = depDepth;
		        		
		        		dependencies.add(myDep);
		        	}
	        	}
	        }
	        firstIndexInSentence = tokens.size();
	        sentenceIndex ++;
	    }
        
        //output
        myData.tokens = tokens;
       	myData.dependencies = dependencies;
       	myData.syntacticTrees = syntacticTrees;
       	myData.dependencyTrees = dependencyTrees;
	}
	
	public String SerializeDependencyTree(SemanticGraph dependencyTree, int sentenceIndex)
	{
		Set<IndexedWord> nodes = new HashSet<IndexedWord>(dependencyTree.vertexSet());

		Collection<IndexedWord> roots = dependencyTree.getRoots();
	    java.util.Iterator<IndexedWord> iter = roots.iterator();
	    String result = "";
		while(iter.hasNext() || nodes.size() > 0)
		{
			IndexedWord rootNode = null; 
		    Set<IndexedWord> used = new HashSet<IndexedWord>();

		    if (iter.hasNext())
			{
				rootNode = iter.next();
			}
			else
			{
				java.util.Iterator<IndexedWord> iter2 = nodes.iterator();
				while (iter2.hasNext()) {
					rootNode = iter2.next();
					if (dependencyTree.getParent(rootNode) == null && dependencyTree.getChildList(rootNode).size()>0) break;
					used.add(rootNode);
				}
			}
			if (rootNode == null) break;
				
			Stack<IndexedWord> nodeStack = new Stack<IndexedWord>();
			Stack<String> edgeStack = new Stack<String>();
			Stack<String> closeBracketStack = new Stack<String>();
			nodeStack.push(rootNode);
			edgeStack.push("");
			closeBracketStack.push("");
		
			TreeSet<Integer> alreadyVisited = new TreeSet<Integer>();
		    while (!nodeStack.empty())
			{
				IndexedWord node = nodeStack.pop();
				String edge = edgeStack.pop();
				String closeBracket = closeBracketStack.pop();
		    	used.add(node);
				
				List<IndexedWord> children = dependencyTree.getChildList(node);
				
			    int wordIndex = sentenceIndex + node.index()-1;

			    if (children.size()==0 || alreadyVisited.contains(new Integer(wordIndex)))
				{
					result = result + (result.length()>0?" ":"") + edge + (edge.length()>0?":":"") + wordIndex + closeBracket;
				}
				else
				{
					alreadyVisited.add(new Integer(wordIndex));
					
					result = result + (result.length()>0?" ":"") + edge + (edge.length()>0?":":"") + "[" + wordIndex;
					for (int i=children.size()-1;i>=0;i--)
					{
						if (i == children.size()-1) closeBracketStack.push(closeBracket + "]");
						else closeBracketStack.push("");
						
						nodeStack.push(children.get(i));
						edgeStack.push(dependencyTree.getEdge(node, children.get(i))+"");
					}
				}
			}
		    nodes.removeAll(used);
		}
		return result;
		
	}

	public ArrayList<SemanticRepresentation.LexicalTokenStructure> PreprocessSimpleText(String text)
	{
		ArrayList<SemanticRepresentation.LexicalTokenStructure> result = new ArrayList<SemanticRepresentation.LexicalTokenStructure>();
		String[] mytokens = SimplePTBTokenizer.tokenize(text);
		for (int j=0;j<mytokens.length;j++)
		{
			SemanticRepresentation.LexicalTokenStructure lex = new SemanticRepresentation.LexicalTokenStructure();
			lex.rawForm = mytokens[j];
			lex.POS = "";
			lex.baseForm = getLemma(lex.rawForm, null);
			
			result.add(lex);
		}
		
		return result;
	}

	public void PreprocessOpenNLPText(SemanticRepresentation myData)
	{
		String text = myData.text;
		ArrayList<SemanticRepresentation.LexicalTokenStructure> tokens = new ArrayList<SemanticRepresentation.LexicalTokenStructure>();
		ArrayList<String> syntacticTrees = new ArrayList<String>();

		String sentences[] = GetSentences(text);
		for (int i=0; i<sentences.length;i++)
		{
			String[] mytokens = Tokenize(sentences[i]);
			String[] mytags = TagSentence(mytokens);
			
			String tokenizedSentence = "";
			for (int j=0;j<mytokens.length;j++)
			{
				SemanticRepresentation.LexicalTokenStructure lex = new SemanticRepresentation.LexicalTokenStructure();
				lex.rawForm = mytokens[j];
				lex.POS = mytags[j];
				lex.baseForm = getLemma(lex.rawForm, lex.POS);
				lex.sentenceIndex = i;
				
				tokens.add(lex);
				tokenizedSentence += " " + lex.rawForm;
			}
			
			if (parserType == ParserType.OPENNLP)
			{
				Parse[] topParses = ParserTool.parseLine(tokenizedSentence, openNLPparser, 1);
				StringBuffer sb = new StringBuffer();
				topParses[0].show(sb);
				syntacticTrees.add(sb.toString());
			}
		}
		
		myData.tokens = tokens;
		myData.syntacticTrees = syntacticTrees;
		myData.dependencyTrees = new ArrayList<String>();
		myData.dependencies = new ArrayList<SemanticRepresentation.DependencyStructure>();
		
	}
	
	String getLemma(String word, String pos)
	{
		if (stemmerType == StemmerType.PORTER)
		{
			return stemmer.stem(word);
		}
		if (stemmerType == StemmerType.WORDNET_SIMPLE)
		{
			return WordNetSimilarity.getInstance().getSimpleWordNetLemma(word);
		}
		if (stemmerType == StemmerType.WORDNET)
		{
			return WordNetSimilarity.getInstance().getWordNetLemma(word, pos);
		}
		
		//normally, the process flow should not reach this point
		return word;
	}
	
	public void ExtractPOSFromFolder(String inputFolder)
	{
		//TODO
		String inputPath = inputFolder;
		File dir = new File(inputPath);
		
		try {
			this.parserType = ParserType.NONE;
	   	    initOpenNLP();
			String[] list = dir.list();
			for (int i=0;i<list.length;i++)
			{
				logger.Log("Parsing file: " + i + " - " + list[i]);
				
				BufferedReader in = new BufferedReader(new FileReader(inputPath + "\\" + list[i]));
				String text = "";
				String line;
					while ((line = in.readLine())!= null)
					{
						text = text + " " + line;
					}
				in.close();
				
				BufferedWriter out = new BufferedWriter(new FileWriter(inputPath + "\\" + list[i]+".POS"));
				String sentences[] = GetSentences(text);
				for (int j=0; j<sentences.length;j++)
				{
					String[] mytokens = Tokenize(sentences[j]);
					String[] mytags = TagSentence(mytokens);
					
					for (int k=0;k<mytokens.length;k++)
					{
						if (mytags[k].equals("NNS")) out.write("XXS" + " ");
						else out.write(mytags[k] + " ");
					}
				}
				out.close();
			}
			logger.Log("Done. Output was saved with same filenames and a POS extension.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.Log("Error -- " + e.getMessage());
		}
	}
	
	public void PreprocessInstance(DataRepresentation myData)
	{
		//Initialize
		try{
			//initialize tools
			if (tokenizerType==TokenizerType.OPENNLP) initOpenNLP();
			else if (tokenizerType==TokenizerType.STANFORD) initStanford();
		}
		catch(Exception e)
		{
			logger.Log("Error -- " + e.getMessage());
	    	e.printStackTrace();
		}
		
		//Preprocess
		if (tokenizerType == TokenizerType.STANFORD){
			PreprocessStanfordText(myData.textA);
			PreprocessStanfordText(myData.textB);
		}
		else
		{
			if (tokenizerType == TokenizerType.OPENNLP){
				PreprocessOpenNLPText(myData.textA);
				PreprocessOpenNLPText(myData.textB);
			}
			else
			{
				myData.textA.tokens = PreprocessSimpleText(myData.textA.text);
				myData.textB.tokens = PreprocessSimpleText(myData.textB.text);					
			}
		}
	}
	
	public void PreprocessRepresentation(SemanticRepresentation myData)
	{
		//Initialize
		try{
			//initialize tools
			if (tokenizerType==TokenizerType.OPENNLP) initOpenNLP();
			else if (tokenizerType==TokenizerType.STANFORD) initStanford();
		}
		catch(Exception e)
		{
			logger.Log("Error -- " + e.getMessage());
	    	e.printStackTrace();
		}
		
		//Preprocess
		if (tokenizerType == TokenizerType.STANFORD) PreprocessStanfordText(myData);
		else
		{
			if (tokenizerType == TokenizerType.OPENNLP)	PreprocessOpenNLPText(myData);
			else myData.tokens = PreprocessSimpleText(myData.text);
		}
	}

	public void PreprocessData(SemanticDataSet data)
	{
		try{
			//initialize tools
			if (tokenizerType==TokenizerType.OPENNLP) initOpenNLP();
			else if (tokenizerType==TokenizerType.STANFORD) initStanford();
		}
		catch(Exception e)
		{
			logger.Log("Error -- " + e.getMessage());
	    	e.printStackTrace();
		}
		
		for(int s=1;s<=2;s++)
		{
			if (s==2 && !data.useTestData) continue;
			
			logger.Log("Preprocessing " + (s==1?"training":"testing") + " data... ");
			ArrayList<DataRepresentation> myData = (s==1?data.trainData:data.testData);
			
			for (int i=0;i<myData.size();i++)
			{
				logger.LogOverwrite("Preprocessing " + (s==1?"training":"testing") + " data... " + String.valueOf(i) + "/" + String.valueOf(myData.size()));
	
				if (tokenizerType == TokenizerType.STANFORD){
					PreprocessStanfordText(myData.get(i).textA);
					PreprocessStanfordText(myData.get(i).textB);
				}
				else
				{
					if (tokenizerType == TokenizerType.OPENNLP){
						PreprocessOpenNLPText(myData.get(i).textA);
						PreprocessOpenNLPText(myData.get(i).textB);
					}
					else
					{
						myData.get(i).textA.tokens = PreprocessSimpleText(myData.get(i).textA.text);
						myData.get(i).textB.tokens = PreprocessSimpleText(myData.get(i).textB.text);					
					}
				}
			}
			logger.LogOverwrite("Preprocessing " + (s==1?"training":"testing") + " data...Done");
		}
		
		data.dataPreprocessed = true;
	}
}
