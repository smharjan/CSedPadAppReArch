package memphis.semanticmatcher;

import java.util.ArrayList;

//TODO Implement a function to extract a Semantic Representation for every sentence

public class SemanticRepresentation {

		public static char TOKEN_SEP = (char)204;
		public static char TOKENINFO_SEP = (char)193;
		public static char DATA_SEP = (char)197;
		
		public static class DependencyStructure{
			public String type;
			public int head;
			public int modifier;
			
			public String strHead;
			public String strModifier;
			public String strPOShead;
			public String strPOSmodifier;
			
			public int depthInTree;
			public int sentenceIndex;
			
			public DependencyStructure()
			{
				type = ".";
				head = 0;
				modifier = 0;
				depthInTree = 0;
				sentenceIndex = 0;
			}
			
			public DependencyStructure(String strData)
			{
				String[] items = strData.split(TOKENINFO_SEP+"");
				type = items[0];
				head = Integer.parseInt(items[1]);
				modifier = Integer.parseInt(items[2]);
				depthInTree = Integer.parseInt(items[3]);
				sentenceIndex = Integer.parseInt(items[4]);
			}
			
			@Override
			public String toString()
			{
				return type + TOKENINFO_SEP + head + TOKENINFO_SEP + modifier + TOKENINFO_SEP + depthInTree + TOKENINFO_SEP + sentenceIndex;
			}
			
			@Override
			public boolean equals(Object e)
			{
				Boolean result = true;
				DependencyStructure dep = ((DependencyStructure)e);
				if (!type.equals(dep.type)) result = false;
				if (!strPOShead.equals(dep.strPOShead)) result = false;
				if (!strPOSmodifier.equals(dep.strPOSmodifier)) result = false;
				return result;
			}
		}
		
		public static class LexicalTokenStructure{
			public String rawForm;
			public String baseForm;
			public String POS;
			//int WNSense; I won't be implementing this; is a hard problem of word sense disambiguation
			public int sentenceIndex;
			
			@Override
			public boolean equals(Object e)
			{
				return baseForm.equalsIgnoreCase(((LexicalTokenStructure)e).baseForm);
			}
		
			public LexicalTokenStructure()
			{
				rawForm = ".";
				baseForm = ".";
				POS = ".";
				sentenceIndex = 0;
			}
			
			public LexicalTokenStructure(String strData)
			{
				String[] items = strData.split(TOKENINFO_SEP+"");
				rawForm = items[0];
				baseForm = items[1];
				POS = items[2];
				sentenceIndex = Integer.parseInt(items[3]);
			}
			
			@Override
			public  String toString()
			{
				return rawForm + TOKENINFO_SEP + baseForm + TOKENINFO_SEP + POS + TOKENINFO_SEP + sentenceIndex;
			}
		}
		
		public String text;
		
		public ArrayList<LexicalTokenStructure> tokens = null;
		public ArrayList<DependencyStructure> dependencies = null;
		public ArrayList<String> syntacticTrees = null;
		public ArrayList<String> dependencyTrees = null;

		
		// *************************************************************************
		public String Dependency2ShortID(DependencyStructure dep)
		{
			String result = dep.type + "(";
			result += tokens.get(dep.head).baseForm + ";";
			result += tokens.get(dep.modifier).baseForm + ")";
			
			return result;
		}
		
		public void FillInDependencyTokens(Boolean useLemma, Boolean ignoreCase)
		{
			LexicalTokenStructure token;
			String str;
			int i;
			for(i=0; i < dependencies.size(); i++)
			{
				token = tokens.get(dependencies.get(i).head);
				str = (useLemma?token.baseForm:token.rawForm);
				if (ignoreCase) dependencies.get(i).strHead = str.toLowerCase(); 
				else dependencies.get(i).strHead = str;
				dependencies.get(i).strPOShead = token.POS;
				
				token = tokens.get(dependencies.get(i).modifier);
				str = (useLemma?token.baseForm:token.rawForm);
				if (ignoreCase) dependencies.get(i).strModifier = str.toLowerCase(); 
				else dependencies.get(i).strModifier = str;
				dependencies.get(i).strPOSmodifier = token.POS;
			}
		}

		public SemanticRepresentation(String txt)
		{
			text = txt;
		}
		
		public SemanticRepresentation(String txt, String toks, String synTrees, String depTrees, String deps)
		{
			text = txt;

			String strItems[];
			if (!toks.equals("null"))
			{
				tokens = new ArrayList<LexicalTokenStructure>();
				strItems = toks.split(TOKEN_SEP+"");
				for (int i=0;i<strItems.length;i++)
				{
					tokens.add(new LexicalTokenStructure(strItems[i]));
				}
			}
			if (!synTrees.equals("null"))
			{
				syntacticTrees = new ArrayList<String>();
				strItems = synTrees.split(TOKEN_SEP+"");
				for (int i=0;i<strItems.length;i++)
					syntacticTrees.add(strItems[i]);
			}
			if (!depTrees.equals("null"))
			{
				dependencyTrees = new ArrayList<String>();
				strItems = depTrees.split(TOKEN_SEP+"");
				for (int i=0;i<strItems.length;i++)
					dependencyTrees.add(strItems[i]);
			}
			if (!deps.equals("null"))
			{
				dependencies = new ArrayList<DependencyStructure>();
				strItems = deps.split(TOKEN_SEP+"");
				for (int i=0;i<strItems.length;i++)
					dependencies.add(new DependencyStructure(strItems[i]));
			}
		}
		
		public String ToString(String separator)
		{
			String rawData =  text;
			
			if (tokens == null)
			{
				rawData = rawData + separator +"null" + separator +"null" + separator +"null" + separator +"null";
			}
			else{
				StringBuilder tokenData = new StringBuilder("");
				for (int i=0;i<tokens.size();i++)
				{
					tokenData.append(tokens.get(i).toString()+TOKEN_SEP);
				}
				rawData = rawData + separator + tokenData.toString();
		
				tokenData.setLength(0);
				if (syntacticTrees != null)
				{
					for (int i=0;i<syntacticTrees.size();i++)
					{
						tokenData.append(syntacticTrees.get(i).toString()+TOKEN_SEP);
					}
				}
				rawData = rawData + separator + (tokenData.length()>0?tokenData.toString():"null");
				
				tokenData.setLength(0);
				if (dependencyTrees != null)
				{
					for (int i=0;i<dependencyTrees.size();i++)
					{
						tokenData.append(dependencyTrees.get(i).toString()+TOKEN_SEP);
					}
				}
				rawData = rawData + separator + (tokenData.length()>0?tokenData.toString():"null");

				if (dependencies != null)
				{
					tokenData.setLength(0);
					for (int i=0;i<dependencies.size();i++)
					{
						tokenData.append(dependencies.get(i).toString()+TOKEN_SEP);
					}
				}
				rawData = rawData + separator + (tokenData.length()>0?tokenData.toString():"null");

				rawData = rawData + "\t" + (tokenData.length()>0?tokenData.toString():"null");
			}

			return rawData; 
		}
	}


