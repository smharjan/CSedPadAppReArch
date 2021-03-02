package dt.core.semantic.tools;

import java.text.DecimalFormat;

public class SMUtils {

	public static String ShortFloatDisplay(double number)
	{
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(number);
	}

	public static String getWordNetPOS(String pos)
	{
		if (pos == null) return null;
		
		pos = " " + pos.toUpperCase() + " ";

		//adjectives
		if (" JJ JJR JJS ".indexOf(pos) >= 0) {return "a";}

		//adverbs
		if (" RB RBR RBS EX PDT RP WRB ".indexOf(pos)>=0) {return "r";}

		//verbs
		if (" VBG VBN VBD VBP VBZ VB ".indexOf(pos)>=0) {return "v";}

		//nouns
		if (" NNS NN FW NNPS NNP ".indexOf(pos)>=0) {return "n";}

		//#???? UH, MD - modal and interjection are not handled by wordnet
		//CC
		if ((" CC IN POS $ CD . , DT WP WDT PRP$ -LRB- -RRB- '' `` TO MD PRP ; : WP$ UH SYM LS ").indexOf(pos)>=0) return null;
		
		//this is a special char returned by the OPENNLP perceptron tagger
		if (pos.equals(" # ")) return null;
		
		//System.out.println(pos);
		
		return null;
	}

}
