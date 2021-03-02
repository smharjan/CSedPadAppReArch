package dt.core.semantic.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;

public class StopWords {

	TreeSet<String> stopWords = null;
	boolean stopWordsLoaded = false;
	
	public StopWords() {}
	
	void LoadStopWords()
	{
		if (stopWords != null) return;
		
		try {
			String currentPath = (new File(".")).getCanonicalPath();
			
			BufferedReader swFile = new BufferedReader(new FileReader(currentPath+"\\stop-words.txt"));
			String stopword = null;
			stopWords = new TreeSet<String>();
			while((stopword = swFile.readLine()) != null)
			{
				stopWords.add(stopword);
			}
			swFile.close();
			stopWordsLoaded = true;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isStopWord(String word)
	{
		if (!stopWordsLoaded) LoadStopWords();
		return stopWords.contains(word.toLowerCase());
	}
}
