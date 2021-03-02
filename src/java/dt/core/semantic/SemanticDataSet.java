package dt.core.semantic;

import java.util.ArrayList;
import java.util.Collections;

import dt.core.semantic.DataRepresentation;

public class SemanticDataSet {

	public Boolean useTestData = true;
	public Boolean projectSaved = false;
	public Boolean dataPreprocessed = false;
	
	public ArrayList<DataRepresentation> trainData = null;
	public ArrayList<DataRepresentation> testData = null;
	
	public ArrayList<SemanticSimilarityFeature> features = new ArrayList<SemanticSimilarityFeature>();

	//------------
	public void ShuffleData(boolean test)
	{
		ArrayList<DataRepresentation> list = test?testData:trainData;
		Collections.shuffle(list);
	}
	
	public void IgnoreNegativeInstances(boolean test)
	{
		ArrayList<DataRepresentation> list = test?testData:trainData;
		for (int i=0;i<list.size();i++)	
		{
			DataRepresentation item = list.get(i);
			if (!item.goldClass) item.ignore = !item.ignore;
		}
	}
	
	public void RemoveIgnoredInstances(boolean test)
	{
		ArrayList<DataRepresentation> list = test?testData:trainData;
		int i=0;
		while (i<list.size())	
		{
			if (list.get(i).ignore) list.remove(i);
			else i++;
		}
	}
	
	public int CountEnabledInstances(boolean test)
	{
		ArrayList<DataRepresentation> list = test?testData:trainData;
		int c = 0;
		
		for (int i=0;i<list.size();i++)
		{
			DataRepresentation item =list.get(i);
			if (item.ignore == false) c++;
		}
		return c;
	}

	public int CountEnabledPositiveInstances(boolean test)
	{
		ArrayList<DataRepresentation> list = test?testData:trainData;
		int c = 0;
		
		for (int i=0;i<list.size();i++)
		{
			DataRepresentation item =list.get(i);
			if (item.ignore == false && item.goldClass == true) c++;
		}
		return c;
	}
}
