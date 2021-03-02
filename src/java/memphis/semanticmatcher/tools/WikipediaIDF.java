package memphis.semanticmatcher.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import memphis.semanticmatcher.DataRepresentation;
import memphis.semanticmatcher.SemanticDataSet;
import memphis.semanticmatcher.SemanticRepresentation;

public class WikipediaIDF {
	
	private static WikipediaIDF instance = null;
	 
    public static WikipediaIDF getInstance() {
    	if (instance == null) instance = new WikipediaIDF();
        return instance;
    }
    
    private WikipediaIDF() {}
    //-------------------------------------------------------------------

    public static final String WIKIPEDIA_DICT_PATH = "WikipediaDF\\sorted_short_wiki";
	public long TOTAL_WIKIPEDIA_DOCS = 2225726;
	
	public static long MAX_DF_VALUE = 2300000;
	public static float NORM_VALUE = 6.5f;  

	public Hashtable<String, Double> idfWeights = null;
	
	Boolean dictionaryCreated = false;
	Boolean idfWeightsLoaded = false;
	
	public void ExtractIDFWeights(SemanticDataSet data)
	{
		if (idfWeightsLoaded) return;
		
		//logger.Log("Extracting IDF Weights...");
		CreateDictionary(data);
		try{
			LoadIDFWeights();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//logger.Log("Exception encountered:" + e.getMessage());
		}
		//logger.LogOverwrite("Extracting IDF Weights...Done");
	}

	public void ExtractIDFWeights4Instance(DataRepresentation data)
	{
		if (idfWeightsLoaded) return;
		
		//logger.Log("Extracting IDF Weights...");
		idfWeights = new Hashtable<String, Double>();
		ArrayList<SemanticRepresentation.LexicalTokenStructure> tokens = null;
		
		tokens = data.textA.tokens;
		for(int j=0;j<tokens.size();j++)
		{
			idfWeights.put(tokens.get(j).rawForm.toLowerCase(), new Double(1));
		}
		tokens = data.textB.tokens;
		for(int j=0;j<tokens.size();j++)
		{
			idfWeights.put(tokens.get(j).rawForm.toLowerCase(), new Double(1));
		}
		dictionaryCreated = true;
	
		try{
			LoadIDFWeights();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			//logger.Log("Exception encountered:" + e.getMessage());
		}
		//logger.LogOverwrite("Extracting IDF Weights...Done");
	}

	public void CreateDictionary(SemanticDataSet data)
	{
		if (data == null) return;
		
		idfWeights = new Hashtable<String, Double>();
		
		for(int s=1;s<=2;s++)
		{
			if (s==2 && !data.useTestData) continue;
			
			ArrayList<DataRepresentation> myData = (s==1?data.trainData:data.testData);
			
			for (int i=0;i<myData.size();i++)
			{
				ArrayList<SemanticRepresentation.LexicalTokenStructure> tokens = null;
				
				tokens = myData.get(i).textA.tokens;
				for(int j=0;j<tokens.size();j++)
				{
					idfWeights.put(tokens.get(j).rawForm.toLowerCase(), new Double(1));
				}
				tokens = myData.get(i).textB.tokens;
				for(int j=0;j<tokens.size();j++)
				{
					idfWeights.put(tokens.get(j).rawForm.toLowerCase(), new Double(1));
				}
			}
		}
		dictionaryCreated = true;
	}
	
    public void LoadIDFWeights() throws IOException 
    {
        char[] letters = {'_','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};

        for (char l: letters)
        {
            String fileName = WIKIPEDIA_DICT_PATH + "\\sorted_short_dict_" + l + ".061";
            BufferedReader f = new BufferedReader(new FileReader(fileName));
			String line;

			while ((line = f.readLine())!= null) 
			{
				String[] param = line.split("\t");
				if (idfWeights.containsKey(param[0]))
				{
					double df = Double.parseDouble(param[1]);
					//if (df > MAX_DF_VALUE) df = MAX_DF_VALUE;
					
					double idf = Math.log10(MAX_DF_VALUE/df);
					
					idf = idf / NORM_VALUE;
					
					idfWeights.put(param[0], new Double(idf));
				}
			}
			f.close();
        }
        idfWeightsLoaded = true;
    }
    
    public void ResetDictionary()
    {
    	dictionaryCreated = false;
    	idfWeightsLoaded = false;
    	idfWeights = null;
    }

}
