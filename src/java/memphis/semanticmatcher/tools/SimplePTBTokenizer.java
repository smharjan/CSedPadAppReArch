package memphis.semanticmatcher.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;
import java.util.regex.*;

public class SimplePTBTokenizer {
	
	String text;
	
	//private static String[] abbrev = new String[]{"A.B.", "Inc.", "Co."};
	static TreeSet<String> abbrev = null;
	
	private SimplePTBTokenizer(String _text)
	{
		text = _text;
		
		if (abbrev != null) return;
		
		try {
			String currentPath = (new File(".")).getCanonicalPath();
			
			BufferedReader swFile = new BufferedReader(new FileReader(currentPath+"\\abbrev.txt"));
			String abbr = null;
			abbrev = new TreeSet<String>();
			while((abbr = swFile.readLine()) != null)
			{
				abbrev.add(abbr);
			}
			swFile.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		text = _text;
	}
	
	public static String[] tokenize(String text)
	{
		SimplePTBTokenizer tk = new SimplePTBTokenizer(" " + text + " ");
		
		tk.replace("^\\s\"", 0, 0," `` ");
		tk.replace("[ (\\[{<]\"", 1, 0, " `` ");

		tk.enclose("[,;:@#$%&]"," "," ");
		
		//using a period is a bit tricky, since sentence detection was not done yet
		tk.enclose("[?!]"," "," ");
		tk.enclose("[\\[\\](){}<>]"," "," ");
		tk.enclose("--"," "," ");

		//managing periods
		tk.separatePeriods();
		tk.replace("\\.\\.\\.",0, 0, "...");

		tk.replace("\"", 0, 0," '' ");

		//possessive or close-single-quote
		tk.replace("[^']'\\s", 1, 0," ' ");

		//as in it's, I'm, we'd
		tk.enclose("'[sSmMdD]\\s", " ", "");
		tk.enclose("'ll\\s", " ", "");
		tk.enclose("'re\\s", " ", "");
		tk.enclose("'ve\\s", " ", "");
		tk.enclose("n't\\s", " ", "");
		tk.enclose("'LL\\s", " ", "");
		tk.enclose("'RE\\s", " ", "");
		tk.enclose("N'T\\s", " ", "");
		
		tk.replace("[Cc]annot\\s", 1,0, "an not ");
		tk.replace("[Dd]'ye\\s", 1,0, "' ye ");
		tk.replace("[Gg]imme\\s", 1,0, "im me ");
		tk.replace("[Gg]onna\\s", 1,0, "on na ");
		tk.replace("[Gg]otta\\s", 1,0, "ot ta ");
		tk.replace("[Ll]emme\\s", 1,0, "em me ");
		tk.replace("[Mm]ore'n\\s", 1,0, "ore 'n ");
		tk.replace("[Tt]is\\s", 1,0, "' is ");
		tk.replace("[Tt]was\\s", 1,0, "' was ");
		tk.replace("[Ww]anna\\s", 1,0, "an na ");
		tk.replace("[Ww]haddya\\s", 1,0, "ha dd ya ");
		tk.replace("[Ww]hatcha\\s", 1,0, "ha t cha ");
		
		tk.replace("\\s+", 0, 0," ");
		tk.replace("^\\s", 0, 0,"");

		return tk.getText().split(" ");
	}
	
	//Do not use $ character in the pattern !!!
	public void replace(String pattern, int replaceStartIndex, int replaceEndIndex, String replacement)
	{
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);
		StringBuffer sb = new StringBuffer();
		
		boolean result = m.find();
		while (result)
		{
			m.appendReplacement(sb, m.group().substring(0, replaceStartIndex) + replacement + m.group().substring(m.group().length()-replaceEndIndex, m.group().length()));
			result = m.find();
		}
		m.appendTail(sb);
		 
		text = sb.toString();
	}
	
	//separating the periods are a special case
	public void separatePeriods()
	{
		String[] tokens = text.split(" ");
		StringBuilder sb = new StringBuilder();
		//HashSet<String> setAbbrev = new HashSet<String>(Arrays.asList(abbrev));
		
		//TreeSet<String> setAbbrev = new TreeSet(Collections.))
		
		for (int i=0;i<tokens.length;i++) 
		{
			if (tokens[i].endsWith(".") && tokens[i].length() > 2)
			{
				Boolean separate = true;
				String subtoken = tokens[i].substring(0, tokens[i].length()-1);
				
				if (subtoken.contains(".") && subtoken.contains("[a-ZA-Z]")) separate = false;
				if (abbrev.contains(tokens[i])) separate = false;
				
				if (separate) sb.append(" " + subtoken + " .");
				else sb.append(" " + subtoken + ".");
			}
			else sb.append(" " + tokens[i]);
		}
		text = sb.toString();
	}

	public void enclose(String pattern, String pre, String post)
	{
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);
		StringBuffer sb = new StringBuffer();
		
		boolean result = m.find();
		while (result)
		{
			String repl = m.group().replace("$","\\$");
			m.appendReplacement(sb, pre + repl + post);
			result = m.find();
		}
		m.appendTail(sb);
		 
		text = sb.toString();
	}

	public String getText()
	{
		return text;
	}
	
	//--------------------------------------------------
	public static void main(String[] args)
	{
		String input = "\"A.B. his [is] \"a, 23.46 I'm Cannot Gonna DON'T test\"...";
		input = "Retailers J.C. Penney Co. Inc. (JCP) and Walgreen Co. (WAG) kick things off on Monday.";
		
		String[] tokens = SimplePTBTokenizer.tokenize(input);
		System.out.print("<");
		for(int i=0; i<tokens.length-1;i++) System.out.print(tokens[i] + ">  <");
		System.out.print(tokens[tokens.length-1]+">");
	}
}
