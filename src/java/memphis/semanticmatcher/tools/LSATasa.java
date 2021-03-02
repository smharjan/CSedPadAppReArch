package memphis.semanticmatcher.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import memphis.deeptutor.singleton.ConfigManager;
import memphis.semanticmatcher.DataRepresentation;
import memphis.semanticmatcher.SemanticDataSet;
import memphis.semanticmatcher.SemanticRepresentation;

public class LSATasa {
	
	private static LSATasa instance = null;
	 
    public static LSATasa getInstance() {
    	if (instance == null) 
    	{
    		try{
    			instance = new LSATasa();
    			//All that the LSA class contains for now is the index list of the LSA terms
    			instance.InitLSASpace();
    		}
    		catch(IOException e) { return null;}
    	}
        return instance;
    }
    
    private LSATasa () {}
    //-------------------------------------------------------------------

	public static enum LOCAL_WEIGHTING {NONE, FREQUENCY, LOG_FREQUENCY};
	public static enum GLOBAL_WEIGHTING {NONE, ENTROPY, IDF};
	
	public static final String LSA_SPACE_DIR = ConfigManager.GetDataPath() + "LSA-TasaCorpus\\";
	public static final String LSA_SPACE_NAME = "tasa414";
	
	public class LSATerm{
		public int lsaIndex;
		public float entropyWeight;
		public float idfWeight;
	}
	
	Hashtable<String, HashSet<Integer>> baseForm2LSAs = null;
	Hashtable<String, Float> baseFormWeights = null;
	Hashtable<Integer, Float> LSAWeights = null;
	SortedSet<Integer> LSAs2Extract = null;
	
	Hashtable<Integer,float[]> extractedLSAs = null;
	Hashtable<String,float[]> computedBaseLSAs = null;
	
	public Boolean lsaExtracted = false;
	public Boolean baseLSAComputed = false;
	
    public Hashtable<String, LSATerm> lsaTerms = null;
    int nTerms = 0;
    int lsaSpaceDimension = 0;
    int nDocs = 0;
    
    boolean initialized = false;
    
    //--------------------------------------------------------------------------------------------------
	public void ExtractLSAVectors4Representation(SemanticRepresentation instance)
	{
		Reset4LSAProcessing();
		markTokens4Extraction(instance.tokens);
		
		try{
			ExtractLSAVectors4StoredTerms();
		}
		catch(IOException e)
		{
			//logger.LogOverwrite("Error -- While extracting LSA vectors during preprocessing");
			e.printStackTrace();
		}
		ComputeLSAVectors4BaseForm(LSATasa.GLOBAL_WEIGHTING.ENTROPY);
		lsaExtracted = true;
	}

	public void ExtractLSAVectors4Instance(DataRepresentation instance)
	{
		Reset4LSAProcessing();
		markTokens4Extraction(instance.textA.tokens);
		markTokens4Extraction(instance.textB.tokens);
		
		try{
			ExtractLSAVectors4StoredTerms();
		}
		catch(IOException e)
		{
			//logger.LogOverwrite("Error -- While extracting LSA vectors during preprocessing");
			e.printStackTrace();
		}
		ComputeLSAVectors4BaseForm(LSATasa.GLOBAL_WEIGHTING.ENTROPY);
		lsaExtracted = true;
	}

	public void ExtractLSAVectors4ProjectData(SemanticDataSet data)
	{
		//logger.Log("Extracting LSA Vectors for current data project... ");

		Reset4LSAProcessing();
		
		for(int s=1;s<=2;s++)
		{
			if (s==2 && !data.useTestData) continue;
			
			ArrayList<DataRepresentation> myData = (s==1?data.trainData:data.testData);
			
			for (int i=0;i<myData.size();i++)
			{
				markTokens4Extraction(myData.get(i).textA.tokens);
				markTokens4Extraction(myData.get(i).textB.tokens);
			}
		}
		
		try{
			ExtractLSAVectors4StoredTerms();
		}
		catch(IOException e)
		{
			//logger.LogOverwrite("Error -- While extracting LSA vectors during preprocessing");
			e.printStackTrace();
		}
		lsaExtracted = true;
		//logger.LogOverwrite("Extracting LSA Vectors for current data project...Done");
	}
	
	void markTokens4Extraction(ArrayList<SemanticRepresentation.LexicalTokenStructure> tokens)
	{
		for(int i=0;i<tokens.size();i++)
		{
			StoreTerm4LSAProcessing(tokens.get(i).rawForm.toLowerCase(), tokens.get(i).baseForm.toLowerCase());
		}
	}
    // -------------------------------------------------------------------------------------------------
    
    public float GetEntropyWeight(String word)
    {
    	float result = 1;
    	
    	if (lsaTerms.containsKey(word))
    	{
    		result = lsaTerms.get(word).entropyWeight;
    	}
    	
    	return result;
    }
    
    public void LoadIDFWeights(WikipediaIDF source)
    {
    	for (String term: lsaTerms.keySet())
    	{
    		lsaTerms.get(term).idfWeight = source.idfWeights.get(term).floatValue();
    	}
    }
    
	public void InitLSASpace() throws IOException
	{
        //read all the terms first
		//IMPORTANT: we assume the LSA index for the terms starts at 1 
        String termFile = LSA_SPACE_DIR + "\\terms.dat";
        BufferedReader termReader = new BufferedReader(new FileReader(termFile));

        lsaTerms = new Hashtable<String, LSATasa.LSATerm>();
        extractedLSAs = new Hashtable<Integer,float[]>();
        
        String line = null;
        while((line = termReader.readLine())!= null)
        {
        	String[] items = line.split("\t");
        	LSATerm term = new LSATerm();
        	term.lsaIndex = Integer.parseInt(items[1]);
        	term.entropyWeight = Float.parseFloat(items[2]);
        	term.idfWeight = 1;
        	
        	lsaTerms.put(items[0], term);
        }
        termReader.close();
        
        //read the space dimensions
        String matrixFile = LSA_SPACE_DIR + "\\matrix.dat";
        FileInputStream f = new FileInputStream(matrixFile);
        byte b[] = new byte[12];
        f.read(b);
        ByteBuffer buf = ByteBuffer.allocate(12);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(b);
        buf.rewind();

        nDocs = buf.asIntBuffer().get(0);
        nTerms = buf.asIntBuffer().get(1);
        lsaSpaceDimension = buf.asIntBuffer().get(2);
        
        f.close();
        
        initialized = true;
	}
	
    public float getWordLSACosine(String word1, String word2) throws IOException
    {
        //the LSA dimensions remain fixed for our experiment
        int startDim = 0;
        int maxDim = lsaSpaceDimension;

        float[] v1 = extractLSAVector(word1, false);
        if (v1 == null) return 0;
        
        float[] v2 = extractLSAVector(word2, false);
        if (v2 == null) return 0;
        
        double product = 0;
        for (int k = startDim; k < maxDim; k++)
        {
            product += v1[k] * v2[k];
        }
        
        //negative values have no meaning, we can safely assume that words are not related
        if (product < 0) product = 0;
        
        return (float)product;
    }

    public float getTokenizedTextLSACosine(String[] words1, String[] words2, LOCAL_WEIGHTING localWeight, GLOBAL_WEIGHTING globalWeight, Boolean useBaseLSA) throws Exception
    {
        //the LSA dimensions remain fixed for our experiment
        int startDim = 0;
        int maxDim = lsaSpaceDimension;

        //initialize vectors
        float[] v0 = new float[lsaSpaceDimension];
        for (int i = 0; i < v0.length; i++) v0[i] = 0;
        float[] v1 = new float[lsaSpaceDimension];
        for (int i = 0; i < v1.length; i++) v1[i] = 0; 

        //initialize token frequency sets
        Hashtable<String, Integer> tokens1 = new Hashtable<String, Integer>();
        Hashtable<String, Integer> tokens2 = new Hashtable<String, Integer>();
        for (int i = 0; i < words1.length; i++)
        {
        	if (tokens1.containsKey(words1[i])) tokens1.put(words1[i], new Integer(tokens1.get(words1[i])+1));
        	else tokens1.put(words1[i], new Integer(1));
        }
        for (int i = 0; i < words2.length; i++)
        {
        	if (tokens2.containsKey(words2[i])) tokens2.put(words2[i], new Integer(tokens2.get(words2[i])+1));
        	else tokens2.put(words2[i], new Integer(1));
        }

        //compute product
        for (int s=0;s<2;s++)
        {
            Enumeration<String> e = (s==0)?tokens1.keys():tokens2.keys();
            
            while (e.hasMoreElements())
            {
            	String word = e.nextElement();
            	float value = (s==0)?tokens1.get(word):tokens2.get(word);
            	
                float[] vv = extractLSAVector(word, useBaseLSA);
                if (vv == null) continue; //ignore words that don't have LSA values
                
                float weight = 1;
                if (useBaseLSA){
                	if (globalWeight == GLOBAL_WEIGHTING.ENTROPY) weight = baseFormWeights.get(word);
                	//if (globalWeight == GLOBAL_WEIGHTING.IDF) weight = (float)al.idfWeight;
                }
                else
                {
                	LSATerm al = lsaTerms.get(word);
                	if (globalWeight == GLOBAL_WEIGHTING.ENTROPY) weight = (float)al.entropyWeight;
                	if (globalWeight == GLOBAL_WEIGHTING.IDF) weight = (float)al.idfWeight;
                }
       
                float localw = 1;
                if (localWeight == LOCAL_WEIGHTING.LOG_FREQUENCY) localw = 1 + (float)Math.log(value);
                if (localWeight == LOCAL_WEIGHTING.FREQUENCY) localw = value;
                
                for (int k = startDim; k < maxDim; k++)
                {
                	if (s==0) v0[k] += vv[k] * weight * localw;
                	else v1[k] += vv[k] * weight * localw;
                }
            }
        }
       
        v0 = normalize(v0, startDim, maxDim);
        v1 = normalize(v1, startDim, maxDim);

        double product = 0;
        for (int k = startDim; k < maxDim; k++)
        {
            product += v0[k] * v1[k];
        }
        
        //negative values have no meaning, we can safely assume that texts are not related
        if (product < 0) product = 0;

        return (float)product;
    }

    public float[] getCompoundLSAVector(String[] words1, LOCAL_WEIGHTING localWeight, GLOBAL_WEIGHTING globalWeight, Boolean useBaseLSA) throws Exception
    {
        //the LSA dimensions remain fixed for our experiment
        int startDim = 0;
        int maxDim = lsaSpaceDimension;

        //initialize vectors
        float[] v0 = new float[lsaSpaceDimension];
        for (int i = 0; i < v0.length; i++) v0[i] = 0;

        //initialize token frequency sets
        Hashtable<String, Integer> tokens1 = new Hashtable<String, Integer>();
        for (int i = 0; i < words1.length; i++)
        {
        	if (tokens1.containsKey(words1[i])) tokens1.put(words1[i], new Integer(tokens1.get(words1[i])+1));
        	else tokens1.put(words1[i], new Integer(1));
        }

        //compute product
        Enumeration<String> e = tokens1.keys();
            
        while (e.hasMoreElements())
        {
        	String word = e.nextElement();
        	float value = tokens1.get(word);
        	
            float[] vv = extractLSAVector(word, useBaseLSA);
            if (vv == null) continue; //ignore words that don't have LSA values
            
            float weight = 1;
            if (useBaseLSA){
            	if (globalWeight == GLOBAL_WEIGHTING.ENTROPY) weight = baseFormWeights.get(word);
            	//if (globalWeight == GLOBAL_WEIGHTING.IDF) weight = (float)al.idfWeight;
            }
            else
            {
            	LSATerm al = lsaTerms.get(word);
            	if (globalWeight == GLOBAL_WEIGHTING.ENTROPY) weight = (float)al.entropyWeight;
            	if (globalWeight == GLOBAL_WEIGHTING.IDF) weight = (float)al.idfWeight;
            }
   
            float localw = 1;
            if (localWeight == LOCAL_WEIGHTING.LOG_FREQUENCY) localw = 1 + (float)Math.log(value);
            if (localWeight == LOCAL_WEIGHTING.FREQUENCY) localw = value;
            
            for (int k = startDim; k < maxDim; k++) v0[k] += vv[k] * weight * localw;
        }
       
        v0 = normalize(v0, startDim, maxDim);

        return v0;
    }

    private float[] normalize(float[] v, int startDim, int maxDim)
    {
        double norm = 0;
        for (int i = 0; i < startDim; i++) v[i] = 0;
        for (int i = maxDim; i < v.length; i++) v[i] = 0;
        for (int i = startDim; i < maxDim; i++) norm += v[i] * v[i];
        norm = Math.sqrt(norm);
        if (norm > 0.000000001)
            for (int i = startDim; i < maxDim; i++) v[i] = (float)(v[i] / norm);
        return v;
    }
    
    public void Reset4LSAProcessing()
    {
    	baseForm2LSAs = new Hashtable<String, HashSet<Integer>>();
    	baseFormWeights = new Hashtable<String, Float>();
    	LSAWeights = new Hashtable<Integer,Float>();
    	LSAs2Extract = new TreeSet<Integer>();
    	lsaExtracted = false;
    	baseLSAComputed = false;
    }
    
    public int StoreTerm4LSAProcessing(String rawForm, String baseForm) //return the corresponding LSA index
    {
    	int myRawLSAIndex = 0; 
    	int myBaseLSAIndex = 0; 
    	//get the LSA index for the raw form
    	if (lsaTerms.containsKey(rawForm)){
    		LSATerm term = lsaTerms.get(rawForm);
    		myRawLSAIndex = term.lsaIndex;
    		LSAWeights.put(myRawLSAIndex, term.entropyWeight);
    	}
		//get the LSA index for the raw form
    	if (rawForm.equals(baseForm)){
    		myBaseLSAIndex = myRawLSAIndex;
    	}
    	else{
    		if (lsaTerms.containsKey(baseForm)){
    			LSATerm term = lsaTerms.get(baseForm);
    			myBaseLSAIndex = term.lsaIndex;
    			LSAWeights.put(myBaseLSAIndex, term.entropyWeight);
    		}
    	}
    	
    	//check if we have any LSA values (for raw or base) 
    	if (myBaseLSAIndex + myRawLSAIndex == 0) return 0;
    	
    	HashSet<Integer> myBaseSet = null;
    	if (baseForm2LSAs.containsKey(baseForm))
    	{
    		myBaseSet = baseForm2LSAs.get(baseForm);
    	}
    	else
    	{
    		myBaseSet = new HashSet<Integer>();
    		if (myBaseLSAIndex>0) myBaseSet.add(myBaseLSAIndex);
    		baseForm2LSAs.put(baseForm, myBaseSet);
    	}
    	if (myBaseLSAIndex>0) LSAs2Extract.add(myBaseLSAIndex);
    	if (myRawLSAIndex>0)
    	{
    		myBaseSet.add(myRawLSAIndex);
    		LSAs2Extract.add(myRawLSAIndex);
    		return myRawLSAIndex;
    	}
    	else
    	{
    		//if rawLSA is 0 then baseLSA must be >0
    		return myBaseLSAIndex;
    	}
    }
    
	public float[] extractLSAVector(String word, Boolean useBaseLSA) throws IOException
	{
		word = word.toLowerCase();
		
		if (useBaseLSA){
			if (computedBaseLSAs != null && computedBaseLSAs.contains(word))
				return computedBaseLSAs.get(word);
		}
	
		if (!lsaTerms.containsKey(word)) return null;
		
		Integer lsaIndex = new Integer(lsaTerms.get(word).lsaIndex);
		
		if (extractedLSAs.containsKey(lsaIndex))
			return extractedLSAs.get(lsaIndex);
		
		long offset = 12 + (lsaIndex.intValue()-1) * lsaSpaceDimension * 4;
		
		float[] v = new float[lsaSpaceDimension];
		byte b[] = new byte[lsaSpaceDimension * 4];
        
        String matrixFile = LSA_SPACE_DIR + "\\matrix.dat";
        FileInputStream f = new FileInputStream(matrixFile);
		f.skip(offset);
		f.read(b);
        f.close();
        
        ByteBuffer buf = ByteBuffer.allocate(lsaSpaceDimension * 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(b);
        buf.rewind();		
        for (int i=0; i<lsaSpaceDimension; i++)
        {
        	v[i] = buf.asFloatBuffer().get(i);
        }
        
        v = normalize(v, 0, lsaSpaceDimension);
        extractedLSAs.put(lsaIndex, v);
        
		return v;
	}

    public void ExtractLSAVectors4StoredTerms() throws IOException
    {
    	//First, I want to get all LSAs in one shot

    	//the LSA array contains the sorted values; so we can parse the LSA file in one single pass and get all the vectors
        Object[] myLSAs = LSAs2Extract.toArray();
    	extractedLSAs = new Hashtable<Integer,float[]>();
    	byte b[] = new byte[lsaSpaceDimension * 4];
  
        ByteBuffer buf = ByteBuffer.allocate(lsaSpaceDimension * 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        String matrixFile = LSA_SPACE_DIR + "\\matrix.dat";
        FileInputStream f = new FileInputStream(matrixFile);
        
    	f.skip(12);
    	int previousLsaIndex = 0;
    	for (int i=0;i<myLSAs.length;i++)
    	{
        	Integer lsaIndex = ((Integer)myLSAs[i]).intValue();
        	long offset = (lsaIndex.intValue()-previousLsaIndex-1) * lsaSpaceDimension * 4;
        	f.skip(offset);
        	
        	f.read(b);
            buf.put(b);
            buf.rewind();
            
            float []v = new float[lsaSpaceDimension];
            for (int j=0; j<lsaSpaceDimension; j++)
            {
            	v[j] = buf.asFloatBuffer().get(j);
            }
            extractedLSAs.put(lsaIndex, normalize(v, 0, lsaSpaceDimension));

            previousLsaIndex = lsaIndex;
        }
        f.close();
    	lsaExtracted = true;
    }

    public void ComputeLSAVectors4BaseForm(GLOBAL_WEIGHTING globalWeight)
    {
    	computedBaseLSAs = new Hashtable<String, float[]>();
    	
    	Enumeration<String> e = baseForm2LSAs.keys();
    	while(e.hasMoreElements())
    	{
    		String baseForm = e.nextElement();
    		HashSet<Integer> lsaList = baseForm2LSAs.get(baseForm);
            
    		float[] v0 = new float[lsaSpaceDimension];
            for (int i = 0; i < v0.length; i++) v0[i] = 0;
            
            Iterator<Integer> it = lsaList.iterator();
            float baseWeight = 0;
            while (it.hasNext())
            {
            	Integer lsaIndex = it.next();
            	float[] lsa = extractedLSAs.get(lsaIndex);
            	
                float weight = 1;
                if (globalWeight == GLOBAL_WEIGHTING.ENTROPY) weight = LSAWeights.get(lsaIndex);
                //if (globalWeight == GLOBAL_WEIGHTING.IDF) weight = (float)al.idfWeight;
               
                baseWeight = baseWeight +  LSAWeights.get(lsaIndex);
                for (int k = 0; k < v0.length; k++)
                {
                	v0[k] = v0[k] + weight * lsa[k];
                }
            }
            v0 = normalize(v0, 0, lsaSpaceDimension);
            baseWeight = baseWeight/lsaList.size();
            
            computedBaseLSAs.put(baseForm, v0);
            baseFormWeights.put(baseForm, new Float(baseWeight));
    	}
    	
    	baseLSAComputed = true;
    }
 }
