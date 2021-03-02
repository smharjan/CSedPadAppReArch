package memphis.semanticmatcher.tools;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;

import memphis.deeptutor.singleton.ConfigManager;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.IStemmer;
import edu.mit.jwi.morph.SimpleStemmer;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.sussex.nlp.jws.AdaptedLesk;
import edu.sussex.nlp.jws.AdaptedLeskTanimoto;
import edu.sussex.nlp.jws.AdaptedLeskTanimotoNoHyponyms;
import edu.sussex.nlp.jws.HirstAndStOnge;
import edu.sussex.nlp.jws.JWS;
import edu.sussex.nlp.jws.JiangAndConrath;
import edu.sussex.nlp.jws.LeacockAndChodorow;
import edu.sussex.nlp.jws.Lin;
import edu.sussex.nlp.jws.Path;
import edu.sussex.nlp.jws.Resnik;
import edu.sussex.nlp.jws.WuAndPalmer;


public class WordNetSimilarity {

	private static WordNetSimilarity instance = null;
	 
    public static WordNetSimilarity getInstance() {
    	if (instance == null) instance = new WordNetSimilarity();
        return instance;
    }
    //-------------------------------------------------------------------

	public static final String WORDNET_PATH = ConfigManager.GetDataPath() + "WordNet-JWI\\";
	public static final String WORDNET_VERSION = "3.0";
	
	public enum WNSimMeasure {LESK, LESK_TANIM, LESK_TANIM_NOHYP, HSO, JCN, LCH, LIN, PATH, RES, WUP};
	
	IDictionary wndict = null;
	IStemmer wnStemmer = null;
	SimpleStemmer simpleStemmer = null;
	JWS	ws = null;
	
	Hashtable<String, Double> cache = null;
	Boolean cacheModified = false;
	
	public int getCacheSize()
	{
		return cache.size();
	}
	
	private WordNetSimilarity()
	{
		//initialize the dictionary
		URL url;
		try{
			url = new URL("file", null, WORDNET_PATH + WORDNET_VERSION + "\\dict");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		wndict = new Dictionary(url);
		wndict.open();

		wnStemmer = new WordnetStemmer(wndict);
		
		simpleStemmer = new SimpleStemmer();
		
		cache = new Hashtable<String, Double>();
	}
	
	//pos can be null
	public String getWordNetLemma(String word, String pos) {
		
		String myPOS = SMUtils.getWordNetPOS(pos);
		if (myPOS == null) return word;
		
		POS wnpos = POS.getPartOfSpeech(SMUtils.getWordNetPOS(pos).charAt(0));
		List<String> stems;
		stems = wnStemmer.findStems(word, wnpos);
		
		// look up first sense of the word "dog"
		//IIndexWord idxWord = wndict.getIndexWord("dog", wnpos);
		//IWordID wordID = idxWord.getWordIDs().get(0);
		//IWord wnword = wndict.getWord(wordID);
		//System.out.println("Id = " + wordID);
		//System.out.println("Lemma = " + wnword.getLemma());
		//System.out.println("Gloss = " + wnword.getSynset().getGloss());

		if (stems.size() == 0) 
		{
			return word;
		}
		else return stems.get(0);
	}

	//pos can be null
	public String getWordNetLemma(String word, POS wnpos) {
		List<String> stems;
		stems = wnStemmer.findStems(word, wnpos);
		if (stems.size() == 0) return null;
		else return stems.get(0);
	}
	
	public String getSimpleWordNetLemma(String word) {

		List<String> stems;
		stems = simpleStemmer.findStems(word);

		if (stems.size() == 0) 
		{
			return word;
		}
		else return stems.get(0);
	}

	public static String getString4POS(POS pos)
	{
		switch (pos)
		{
			case ADJECTIVE: return "a";
			case ADVERB: return "r";
			case NOUN: return "n";
			case VERB: return "v";
		}
		
		//we assume the default POS is a noun 
		return "n";
	}
	
	public void SaveCache()
	{
		if (cacheModified)
		{
			try {
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(WORDNET_PATH + WORDNET_VERSION + "\\cache.dat"));
				out.writeObject(cache);
				out.close();
				cacheModified = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean LoadCache()
	{
		try {
			String fileName = WORDNET_PATH + WORDNET_VERSION + "\\cache.dat";
			File f = new File(fileName);
			if (f.exists()==false) return false;
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(WORDNET_PATH + WORDNET_VERSION + "\\cache.dat"));
			cache = (Hashtable<String, Double>)in.readObject();
			in.close();
			cacheModified = false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public double GetWNSimilarityNoPOS(WNSimMeasure wns, boolean firstSenseOnly, String word1, String word2)
	{
		Double maxSimValue = 0.0;
		Double simValue;
		
		simValue = GetWNSimilarityPOS(wns, firstSenseOnly, word1, word2, "n"); 
		if (maxSimValue < simValue) maxSimValue = simValue;
		
		simValue = GetWNSimilarityPOS(wns, firstSenseOnly, word1, word2, "v"); 
		if (maxSimValue < simValue) maxSimValue = simValue;
		
		simValue = GetWNSimilarityPOS(wns, firstSenseOnly, word1, word2, "a"); 
		if (maxSimValue < simValue) maxSimValue = simValue;

		simValue = GetWNSimilarityPOS(wns, firstSenseOnly, word1, word2, "r"); 
		if (maxSimValue < simValue) maxSimValue = simValue;
		
		return maxSimValue;
	}
	
	public double GetWNSimilarity(WNSimMeasure wns, boolean firstSenseOnly, String word1, String word2, String pos)
	{
		return GetWNSimilarityPOS(wns, firstSenseOnly, word1, word2, SMUtils.getWordNetPOS(pos));
	}

	public double GetWNSimilarityPOS( WNSimMeasure wns, boolean firstSenseOnly, String word1, String word2, String wnPOS)
	{
		if (wnPOS==null) return 0;
		POS myPOS = POS.getPartOfSpeech(wnPOS.charAt(0));
		
		word1 = getWordNetLemma(word1, myPOS);
		word2 = getWordNetLemma(word2, myPOS);

		if (word1 == null || word2 == null) return 0;

		String cacheKey = word1+"|"+word2+"|"+wnPOS+"|"+wns.toString()+(firstSenseOnly?"-F":"-M");
		if (cache.containsKey(cacheKey)) return cache.get(cacheKey);

		if (ws == null)
		{
			//initialize the WN similarity package for JAVA
			ws = new JWS(WORDNET_PATH, WORDNET_VERSION);
		}

		double value = 0;
		switch (wns)
		{
			case HSO:
				HirstAndStOnge hso= ws.getHirstAndStOnge();
				value = firstSenseOnly?hso.hso(word1, 1, word2, 1, wnPOS):hso.max(word1, word2, wnPOS);
				value = value / 16;
				break;
			case JCN:
				if (myPOS == POS.NOUN || myPOS == POS.VERB)
				{
					JiangAndConrath jcn= ws.getJiangAndConrath();
					value = firstSenseOnly?jcn.jcn(word1, 1, word2, 1, wnPOS):jcn.max(word1, word2, wnPOS);
				}
				if (value > 1) {
					value = 0.9999;
				}
				break;
			case LCH:
				LeacockAndChodorow lch= ws.getLeacockAndChodorow();
				try{
					value = firstSenseOnly?lch.lch(word1, 1, word2, 1, wnPOS):lch.max(word1, word2, wnPOS);
				}
				catch(Exception e)
				{
					value = 1;
				}
				if (myPOS == POS.NOUN) value = value/3.6889;
				if (myPOS == POS.VERB) value = value/3.3322;
				break;
			case LESK:
				AdaptedLesk lesk= ws.getAdaptedLesk();
				value = firstSenseOnly?lesk.lesk(word1, 1, word2, 1, wnPOS):lesk.max(word1, word2, wnPOS);
				//This value is not normalized
				break;
			case LESK_TANIM:
				AdaptedLeskTanimoto leskTA= ws.getAdaptedLeskTanimoto();
				value = firstSenseOnly?leskTA.lesk(word1, 1, word2, 1, wnPOS):leskTA.max(word1, word2, wnPOS);
				if (value > 1) {
					value = 1;
				}
				break;
			case LESK_TANIM_NOHYP:
				AdaptedLeskTanimotoNoHyponyms leskTANOH= ws.getAdaptedLeskTanimotoNoHyponyms();
				value = firstSenseOnly?leskTANOH.lesk(word1, 1, word2, 1, wnPOS):leskTANOH.max(word1, word2, wnPOS);
				if (value > 1) {
					value = 1;
				}
				break;
			case LIN:
				Lin lin= ws.getLin();
				value = firstSenseOnly?lin.lin(word1, 1, word2, 1, wnPOS):lin.max(word1, word2, wnPOS);
				break;
			case PATH:
				if (myPOS == POS.NOUN || myPOS == POS.VERB)
				{
					Path path= ws.getPath();
					value = firstSenseOnly?path.path(word1, 1, word2, 1, wnPOS):path.max(word1, word2, wnPOS);
				}
				break;
			case RES:
				Resnik res = ws.getResnik();
				value = firstSenseOnly?res.res(word1, 1, word2, 1, wnPOS):res.max(word1, word2, wnPOS);
				value = value /	11.76576; //this value was empirically determined
				break;
			case WUP:
				WuAndPalmer wup = ws.getWuAndPalmer();
				value = firstSenseOnly?wup.wup(word1, 1, word2, 1, wnPOS):wup.max(word1, word2, wnPOS);
				break;
		}
		
		//save to cache
		cache.put(cacheKey, value);
		cacheModified = true;

		return value;
	}

	public static String GetString4WNSim(WNSimMeasure wnsim)
	{
		switch (wnsim)
		{
			case HSO: return "Hirst And StOnge";
			case LESK: return "Adapted Lesk (not-normalized)";
			case LESK_TANIM: return "Adapted Lesk Tanimoto";
			case LESK_TANIM_NOHYP: return "Adapted Lesk Tanimoto No Hyponyms";
			
			case JCN: return "Jiang And Conrath";
			case LCH: return "Leacock And Chodorow";
			case PATH: return "Path";
			case LIN: return "Lin";
			case RES: return "Resnik";
			case WUP: return "Wu And Palmer";
			
			// case VECTOR
		}
		return null;
	}

	public static WNSimMeasure GetWNSim4String(String str)
	{
		if (str.equals("Hirst And StOnge")) return WNSimMeasure.HSO;
		if (str.equals("Jiang And Conrath")) return WNSimMeasure.JCN;
		if (str.equals("Leacock And Chodorow")) return WNSimMeasure.LCH;
		if (str.equals("Adapted Lesk (not-normalized)")) return WNSimMeasure.LESK;
		if (str.equals("Adapted Lesk Tanimoto")) return WNSimMeasure.LESK_TANIM;
		if (str.equals("Adapted Lesk Tanimoto No Hyponyms")) return WNSimMeasure.LESK_TANIM_NOHYP;
		if (str.equals("Path")) return WNSimMeasure.PATH;
		if (str.equals("Lin")) return WNSimMeasure.LIN;
		if (str.equals("Resnik")) return WNSimMeasure.RES;
		if (str.equals("Wu And Palmer")) return WNSimMeasure.WUP;

		//the default WN sim metric
		return WNSimMeasure.LIN;
	}
}
