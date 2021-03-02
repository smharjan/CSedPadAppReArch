package memphis.deeptutor.dialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import memphis.deeptutor.singleton.ConfigManager;
import memphis.semanticmatcher.SemanticRepresentation;
import memphis.semanticmatcher.SemanticRepresentation.LexicalTokenStructure;

public class QuestionAnswering
{
	private static Hashtable<String, Definition> definitions = null;
	private static List<String> defintionsTemplates = null;

	private static void initialize(String fileName) throws IOException
	{
		definitions = new Hashtable<String, Definition>();
		File fileIn = new File(fileName);
		BufferedReader rdr = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileIn)));
		String line;

		while ((line = rdr.readLine()) != null)
		{
			String[] parts = line.trim().split("\t");
			if (parts.length == 4 && !definitions.containsKey(parts[0]))
			{
				definitions.put(parts[0], new Definition(parts[1], parts[2],
						parts[3]));
			}
		}
		rdr.close();

		defintionsTemplates = new ArrayList();
		defintionsTemplates.add("what do you mean by");
		defintionsTemplates.add("what do#mean");
		defintionsTemplates.add("what is#mean");
		defintionsTemplates.add("what do#state");
		defintionsTemplates.add("what is#state");
		defintionsTemplates.add("what do#say");
		defintionsTemplates.add("what is#say");
		defintionsTemplates.add("how be#define");
		defintionsTemplates.add("definition of");
		defintionsTemplates.add("meaning of");
		defintionsTemplates.add("define");
		defintionsTemplates.add("what be");
		defintionsTemplates.add("what");
		defintionsTemplates.add("who be");
		defintionsTemplates.add("whos");
	}

	public static String findDefinitionAnswer(SemanticRepresentation semText,
			String verbExistence) throws IOException
	{
		List<String> occurences = new ArrayList<String>();
		List<String> lemmas = new ArrayList<String>();
		String firstNoun = null;

		for (int i = 0; i < semText.tokens.size(); i++)
		{
			LexicalTokenStructure lts = semText.tokens.get(i);
			lemmas.add(lts.baseForm);
			occurences.add(lts.rawForm);

			if (firstNoun == null && lts.POS.startsWith("N"))
			{
				firstNoun = lts.baseForm;
			}
		}

		if (definitions == null)
			initialize(ConfigManager.GetDefinitionsPath());

		String answer = "I don't know or I don't understand your question...";

		String focus = null;
		// Dan: we don't know if there is a main verb or not
		if (verbExistence == null)
		{
			focus = extractFocusByTemplate(firstNoun, occurences, lemmas,
					semText, defintionsTemplates);
		}
		// Dan: there is no main verb
		//else if (verbExistence == "0")
		//{
		//	focus = extractFocus(semText, 0, semText.tokens.size() - 1);
		//}

		if (focus != null && !focus.equals(""))
		{
			focus = focus.toLowerCase();
			if (definitions.containsKey(focus))
			{
				answer = formulateAnswer(focus, definitions.get(focus));
			}
		}

		return answer;
	}

	private static String formulateAnswer(String focus, Definition definition)
	{
		String def = definition.getText();
		String focusCap = focus.substring(0, 1).toUpperCase()
				+ focus.substring(1);

		String connection = " means ";

		if (definition.getType().equals("Proposition"))
		{
			if (def.startsWith("law") || def.startsWith("principle")
					|| def.startsWith("generalisation")
					|| def.startsWith("theory"))
			{
				connection = " is the ";
			}
			else if (def.startsWith("a law") || def.startsWith("the law")
					|| def.startsWith("a principle")
					|| def.startsWith("the principle")
					|| def.startsWith("a generalisation")
					|| def.startsWith("the generalisation")
					|| def.startsWith("a theory")
					|| def.startsWith("the theory"))
			{
				connection = " is ";
			}
			else if (def.startsWith("one of "))
			{
				connection = " refers to ";
			}
			else
			{
				connection = " states that ";
			}
		}
		else if (definition.getType().equals("Man")
				|| definition.getType().equals("Woman"))
		{
			// Dan: all the focus word must start with Upper case letters
			String[] tokens = focusCap.split(" ");
			if (tokens.length > 1)
			{
				StringBuilder sb = new StringBuilder(tokens[0]);
				for (int i = 1; i < tokens.length; i++)
				{
					sb.append(" " + tokens[i].substring(0, 1).toUpperCase()
							+ tokens[i].substring(1));
				}
				focusCap = sb.toString();
			}

			connection = " is a ";
			if ("aeiou".contains(def.substring(0, 1).toLowerCase()))
			{
				connection = " is an ";
			}

			if (def.startsWith("wife"))
			{
				connection = " is the ";
			}
		}

		String source = "";
		if (definition.getSource().equals("wn"))
		{
			source = " (from Princeton WordNet 3.0)";
		}

		String answer = focusCap + connection + def + source;
		return answer;
	}

	private static String extractFocusByTemplate(String firstNoun,
			List<String> occurenceForms, List<String> lemmaForms,
			SemanticRepresentation semText, List<String> templates)
	{
		for (String template : templates)
		{
			String focus = extractFocusByTemplate(occurenceForms, lemmaForms,
					semText, template);
			if (focus != null)
			{
				return focus;
			}
		}

		return firstNoun;
	}

	private static String extractFocusByTemplate(List<String> occurenceForms,
			List<String> lemmaForms, SemanticRepresentation semText,
			String template)
	{
		String lemmaQuestion = join(lemmaForms, " ");
		String[] tParts = template.split("#");

		if (tParts.length == 1 && lemmaQuestion.contains(tParts[0]))
		{
			int idxStart = findAfterIndex(tParts[0], lemmaForms);
			return extractFocus(semText, idxStart, lemmaForms.size() - 1);
		}
		else if (tParts.length == 2 && lemmaQuestion.contains(tParts[0])
				&& lemmaQuestion.contains(tParts[1]))
		{
			int idxStart = findAfterIndex(tParts[0], lemmaForms);
			int idxEnd = findBeforeIndex(tParts[1], lemmaForms);
			return extractFocus(semText, idxStart, idxEnd);
		}

		return null;
	}

	private static String join(List<String> tokens, String separator)
	{
		if (tokens == null)
			return null;
		if (tokens.size() == 0)
			return "";

		StringBuilder ret = new StringBuilder(tokens.get(0));

		if (tokens.size() > 1)
		{
			for (int i = 1; i < tokens.size(); i++)
				ret.append(" " + tokens.get(i));
		}

		return ret.toString();
	}

	private static String extractFocus(SemanticRepresentation semText,
			int idxStart, int idxEnd)
	{
		List<String> endingPoses = Arrays.asList(new String[] { ".", ",", ":",
				"(", ")", "\"", "\'" });

		StringBuilder sb = new StringBuilder();
		boolean start = false;
		boolean startsWithVerb = false;

		for (int i = idxStart; i <= idxEnd; i++)
		{
			LexicalTokenStructure lts = semText.tokens.get(i);
			if (!start && lts.POS.startsWith("V"))
			{
				startsWithVerb = true;
			}

			if (!start
					&& (lts.POS.startsWith("N") || startsWithVerb || lts.POS
							.startsWith("J")))
			{
				start = true;
			}
			else if (endingPoses.contains(lts.POS)
					|| (!startsWithVerb && (lts.POS.equals("CC") || lts.POS
							.equals("IN"))))
			{
				break;
			}

			if (start)
			{
				sb.append(lts.rawForm + " ");
			}
		}

		return sb.toString().trim();
	}

	private static int findBeforeIndex(String text, List<String> lemmaForms)
	{
		String[] tokens = text.split(" ");
		int j = 0;
		int answer = -1;

		for (int i = 1; i < lemmaForms.size(); i++)
		{
			if (lemmaForms.get(i).equals(tokens[j]))
			{
				if (answer == -1)
				{
					answer = i - 1;
				}

				j++;
				if (tokens.length == j)
				{
					return answer;
				}
			}
			else if ((lemmaForms.size() - i) >= (tokens.length - j))
			{
				j = 0;
				answer = -1;
			}
			else
			{
				break;
			}
		}

		return -1;
	}

	private static int findAfterIndex(String text, List<String> lemmaForms)
	{
		String[] tokens = text.split(" ");
		int j = 0;
		for (int i = 0; i < lemmaForms.size(); i++)
		{
			if (lemmaForms.get(i).equals(tokens[j]))
			{
				j++;
				if (tokens.length == j)
				{
					int answer = i + 1;
					if (answer >= lemmaForms.size())
						answer = -1;
					return answer;
				}
			}
			else if ((lemmaForms.size() - i) >= (tokens.length - j))
			{
				if (j > 0)
				{
					i--;
					j = 0;
				}
			}
			else
			{
				break;
			}
		}

		return -1;
	}

	// public static void main(String[] args)
	// {
	// List<String> lemmaForms = new ArrayList<String>();
	// lemmaForms.add("this"); // 0
	// lemmaForms.add("is"); // 1
	// lemmaForms.add("a"); // 2
	// lemmaForms.add("very"); // 3
	// lemmaForms.add("important"); // 4
	// lemmaForms.add("test"); // 5
	// lemmaForms.add("."); // 6
	//
	// String text = ".";
	//
	// int idx = findBeforeIndex(text, lemmaForms);
	// System.out.println(idx);
	// }
}
