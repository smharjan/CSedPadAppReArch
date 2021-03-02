package dt.core.semantic.wordmetrics;
import dt.core.semantic.tools.SMUtils;
import dt.core.semantic.tools.WordNetSimilarity;

public class DeepTutorWordMetric extends AbstractWordMetric{

	public DeepTutorWordMetric()
	{
	}

	@Override
	public String getID() {
		return "DeepTutor";
	}
	
	@Override
	public String getSerializable() {
		return "DeepTutor";
	}
	
	public static DeepTutorWordMetric GetInstance(String serialized)
	{
		if (serialized.startsWith("DeepTutor"))
		{
			//String[] tokens = serialized.split("-");
			return new DeepTutorWordMetric();
		}
		else return null;
	}

	@Override
	public double ComputeWordSimilarity(String word1, String word2, String pos) {
		
		String wnpos = SMUtils.getWordNetPOS(pos);
		
		if (wnpos == null) return word1.equalsIgnoreCase(word2)?1:0;
			
		double value = 0;
		
		if (wnpos.equals("n")||wnpos.equals("v"))
			value = WordNetSimilarity.getInstance().GetWNSimilarity(WordNetSimilarity.WNSimMeasure.LIN, false, word1.toLowerCase(), word2.toLowerCase(), pos);
		
		if (wnpos.equals("a")||wnpos.equals("r"))
			value = WordNetSimilarity.getInstance().GetWNSimilarity(WordNetSimilarity.WNSimMeasure.LESK_TANIM, false, word1.toLowerCase(), word2.toLowerCase(), pos);

		return value;
	}
}
