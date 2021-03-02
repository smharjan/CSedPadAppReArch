package dt.core.semantic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

import dt.core.semantic.features.AbstractComparer;
import dt.core.semantic.features.LSAComparer;
import dt.core.semantic.features.PairwiseComparer;
import dt.core.semantic.features.PairwiseComparer.WordWeightType;
import dt.core.semantic.tools.LSATasa;
import dt.core.semantic.tools.WikipediaIDF;
import dt.core.semantic.wordmetrics.AbstractWordMetric;
import dt.core.semantic.wordmetrics.LSAWordMetric;

public class FeatureExtractor {
	
	public TextProcessingLogger logger = TextProcessingLogger.TextProcessingLogger_Null;
	
	SemanticDataSet projectData;
	Preprocessor preprocessor;
	
	TreeSet<String> stopWords = null;
	
	public FeatureExtractor(SemanticDataSet _projectData)
	{
		projectData = _projectData;
	}
	
	public String ExportFeaturesToCSV(String path)
	{
		if (projectData.features.size() == 0) return "Error -- There are no defined features to be exported";
		
		for (int i=1;i<projectData.features.size();i++)
		{
			if (projectData.features.get(i).trainData == null) return "Error -- Please extract all defined features before using the export function";
		}

		//project
		try {
			String headerLine = projectData.features.get(0).featureName;
			for (int i=1;i<projectData.features.size();i++)
			{
				headerLine = headerLine + "," + projectData.features.get(i).featureName;
			}
			headerLine = headerLine + ",class";

			BufferedWriter trainFile = new BufferedWriter(new FileWriter(path + "\\MyExportedFeatures_train.csv"));
			trainFile.write(headerLine + "\n");
			for(int i=0;i<projectData.trainData.size();i++)
			{
				String line = projectData.features.get(0).trainData[i] + "";
				
				for (int j=1;j<projectData.features.size();j++)
				{
					line = line + "," + projectData.features.get(j).trainData[i];
				}
				//also save the class
				line = line + "," + (projectData.trainData.get(i).goldClass?"T":"F");
				
				trainFile.write(line+"\n");
			}
			trainFile.close();
			
			if (projectData.useTestData)
			{
				BufferedWriter testFile = new BufferedWriter(new FileWriter(path + "\\MyExportedFeatures_test.csv"));
				testFile.write(headerLine + "\n");
				for(int i=0;i<projectData.testData.size();i++)
				{
					String line = projectData.features.get(0).testData[i] + "";
					
					for (int j=1;j<projectData.features.size();j++)
					{
						line = line + "," + projectData.features.get(j).testData[i];
					}
					//also save the class
					line = line + "," + (projectData.testData.get(i).goldClass?"T":"F");
					testFile.write(line+"\n");
				}
				testFile.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "Error -- " + e.getMessage();
		}
		
		return "OK -- Features have been exported to MyExportedFeatures_train.csv (and MyExportedFeatures_test.csv) in the current Data Projects folder";
	}
	
	public String ExtractFeatures(ArrayList<SemanticSimilarityFeature> features)
	{
		for (int i=0;i<features.size();i++)
		{
			String result = ExtractSingleFeature(features.get(i), false); //train
			if (!result.startsWith("OK")) return result;
			if (projectData.useTestData)
			{
				result = ExtractSingleFeature(features.get(i), true); //test
				if (!result.startsWith("OK")) return result;
			}
		}
		return "OK -- All features succesfully extracted";
	}
	
	public void InitializeNLPTools(AbstractComparer comparer)
	{
		boolean needLSA = false;
		boolean needBaseLSA = false;
		//boolean needWordNet = false;
		boolean needIDF = false;
		
		if (comparer.getClass().getSuperclass() == PairwiseComparer.class)
		{
			PairwiseComparer pwcomp = (PairwiseComparer)comparer;
			AbstractWordMetric wm = pwcomp.wordMetric;
			if (wm != null)
			{
				if (wm.getClass() == LSAWordMetric.class) 
				{
					needLSA = true;
					if (((LSAWordMetric)wm).useBaseForm) needBaseLSA = true;
				}
				//if (wm.getClass() == WNWordMetric.class) needWordNet = true;
			}
			if (pwcomp.wordWeighting == WordWeightType.IDF) needIDF = true;
		}
		if (comparer.getClass() == LSAComparer.class) needLSA = true;
		
		if (needLSA)
		{
			LSATasa.getInstance().ExtractLSAVectors4ProjectData(projectData);
			if (needBaseLSA) LSATasa.getInstance().ComputeLSAVectors4BaseForm(LSATasa.GLOBAL_WEIGHTING.ENTROPY);
		}
		if (needIDF) WikipediaIDF.getInstance().ExtractIDFWeights(projectData);
	}
	
	public String ExtractSingleFeature(SemanticSimilarityFeature feature, boolean useTestData)
	{
		if (projectData == null) return "Error: there is no data to parse.";

		if (useTestData?feature.featureExtractedOnTest:feature.featureExtractedOnTrain) return "OK";

		logger.Log("Processing feature \""+feature.featureName + "...");
		
		AbstractComparer comparer = feature.comparer;
		InitializeNLPTools(comparer);
		
		ArrayList<DataRepresentation> inputData = null;
		float[] outputData = null; 
		if(!useTestData)
		{
			inputData = projectData.trainData;
			outputData = new float[inputData.size()];
			feature.trainData = outputData;
		}
		else
		{
			inputData = projectData.testData;
			outputData = new float[inputData.size()];
			feature.testData = outputData;
		}

		for (int i=0;i<inputData.size();i++)
		{
			logger.LogOverwrite("Processing feature \""+feature.featureName + "\" " + (useTestData?"test":"train") + " (" + (i+1) + "/" + inputData.size()+")");

			outputData[i] = comparer.ComputeSimilarity(inputData.get(i).textA, inputData.get(i).textB);
			inputData.get(i).semanticMeasure = outputData[i];
			inputData.get(i).comparerOutputPrev = inputData.get(i).comparerOutput;
			inputData.get(i).comparerOutput = comparer.ComparerOutput();
		}

		if (useTestData) feature.featureExtractedOnTest = true;
		else feature.featureExtractedOnTrain = true;
		
		return "OK";
	}

}
