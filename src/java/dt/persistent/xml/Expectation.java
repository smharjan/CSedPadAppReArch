package dt.persistent.xml;

import dt.core.managers.NLPManager;

public class Expectation implements Comparable<Expectation>{
	
	public static enum EXPECT_TYPE{NONE, SHORT, ABSTRACT, CONCRETE, OPTIONAL};
	
	public EXPECT_TYPE type = EXPECT_TYPE.NONE; 

	public String id = "";
	private int order=-1; 
	public float similarity = 0;
	public String mostSimilarText = "";
	
	public boolean covered = false;
	
	public boolean pumpSuggested = false;
	public boolean hintSuggested = false;
	public int sugestedHintIndex = -1;
	public boolean promptSuggested = false;
	
	public boolean isMisconception = false;
	public int sentence = 0;
	
	//--------------------------------------------------------------------------------------
	//this data will be retrieved with a function
	public String description = "";
	public String assertion = null;
	public String postImage = null;
	public int postImageSizeWidth = 0; 
	public int postImageSizeHeight = 0; 

	public String yokedExpectation = "";
	
	public String pump = null;
	public String alternatePump=null;
	
	public String[] hints = null;
	public ExpectAnswer[] hintsAnswer = null;
	public String[] hintsType = null;
	public String[] hintsCorrection = null;
	
	public String prompt = null;
	public ExpectAnswer promptAnswer = null;
	public String promptCorrection = null;
	
	public String forbidden = null;
	public String bonus = null;
	public ExpectAnswer required = null;
	public String[] variants = null; //variants to express an expectation or a misconception
	//--------------------------------------------------------------------------------------

	public Expectation(String _id)
	{
		id = _id;
	}
	
	public boolean matches(String input, float threshold)
	{
		//System.out.println(input + "req:" + required.acceptedAnswer + "|th=" + threshold);
		
		if (required!=null && required.acceptedAnswer.trim().length()>0)
		{
			int relen = required.acceptedAnswer.split(",").length;
			int inputlen = input.split(" ").length;
			if (relen *2 >= inputlen) return true;
		}
				
		// BEGIN: Vasile added the next lines on Oct 25, 2012
		// System.out.println("SIMILARITY SCORE BETWEEN STUDENT INPUT AND CURRENT EXPECTATION: " + similarity);
		float max = 0;
		for (int k = 0; k < variants.length; k++) {
			similarity = NLPManager.getInstance().ComputeT2TWNSimilarity(input, variants[k]);
			if (max < similarity) max = similarity;
			similarity = max;
		};
		// System.out.println("SIMILARITY SCORE BETWEEN STUDENT INPUT AND CURRENT EXPECTATION: " + similarity);
		// END: Vasile added the above lines on Oct 25, 2012

		return similarity >= threshold;
		
	}

	@Override
	public int compareTo(Expectation o) {

		if (Math.abs(this.similarity) < Math.abs(o.similarity)) return 1;
		if (Math.abs(this.similarity) > Math.abs(o.similarity)) return -1; 
		return 0;
	}

	public EXPECT_TYPE getType() {
		return type;
	}

	public void setType(EXPECT_TYPE type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public float getSimilarity() {
		return similarity;
	}

	public void setSimilarity(float similarity) {
		this.similarity = similarity;
	}

	public String getMostSimilarText() {
		return mostSimilarText;
	}

	public void setMostSimilarText(String mostSimilarText) {
		this.mostSimilarText = mostSimilarText;
	}

	public boolean isCovered() {
		return covered;
	}

	public void setCovered(boolean covered) {
		this.covered = covered;
	}

	public boolean isPumpSuggested() {
		return pumpSuggested;
	}

	public void setPumpSuggested(boolean pumpSuggested) {
		this.pumpSuggested = pumpSuggested;
	}

	public boolean isHintSuggested() {
		return hintSuggested;
	}

	public void setHintSuggested(boolean hintSuggested) {
		this.hintSuggested = hintSuggested;
	}

	public int getSugestedHintIndex() {
		return sugestedHintIndex;
	}

	public void setSugestedHintIndex(int sugestedHintIndex) {
		this.sugestedHintIndex = sugestedHintIndex;
	}

	public boolean isPromptSuggested() {
		return promptSuggested;
	}

	public void setPromptSuggested(boolean promptSuggested) {
		this.promptSuggested = promptSuggested;
	}

	public boolean isMisconception() {
		return isMisconception;
	}

	public void setMisconception(boolean isMisconception) {
		this.isMisconception = isMisconception;
	}

	public int getSentence() {
		return sentence;
	}

	public void setSentence(int sentence) {
		this.sentence = sentence;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAssertion() {
		return assertion;
	}

	public void setAssertion(String assertion) {
		this.assertion = assertion;
	}

	public String getPostImage() {
		return postImage;
	}

	public void setPostImage(String postImage) {
		this.postImage = postImage;
	}

	public int getPostImageSizeWidth() {
		return postImageSizeWidth;
	}

	public void setPostImageSizeWidth(int postImageSizeWidth) {
		this.postImageSizeWidth = postImageSizeWidth;
	}

	public int getPostImageSizeHeight() {
		return postImageSizeHeight;
	}

	public void setPostImageSizeHeight(int postImageSizeHeight) {
		this.postImageSizeHeight = postImageSizeHeight;
	}

	public String getYokedExpectation() {
		return yokedExpectation;
	}

	public void setYokedExpectation(String yokedExpectation) {
		this.yokedExpectation = yokedExpectation;
	}

	public String getPump() {
		return pump;
	}

	public void setPump(String pump) {
		this.pump = pump;
	}

	public String[] getHints() {
		return hints;
	}

	public void setHints(String[] hints) {
		this.hints = hints;
	}

	public ExpectAnswer[] getHintsAnswer() {
		return hintsAnswer;
	}

	public void setHintsAnswer(ExpectAnswer[] hintsAnswer) {
		this.hintsAnswer = hintsAnswer;
	}

	public String[] getHintsType() {
		return hintsType;
	}

	public void setHintsType(String[] hintsType) {
		this.hintsType = hintsType;
	}

	public String[] getHintsCorrection() {
		return hintsCorrection;
	}

	public void setHintsCorrection(String[] hintsCorrection) {
		this.hintsCorrection = hintsCorrection;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public ExpectAnswer getPromptAnswer() {
		return promptAnswer;
	}

	public void setPromptAnswer(ExpectAnswer promptAnswer) {
		this.promptAnswer = promptAnswer;
	}

	public String getPromptCorrection() {
		return promptCorrection;
	}

	public void setPromptCorrection(String promptCorrection) {
		this.promptCorrection = promptCorrection;
	}

	public String getForbidden() {
		return forbidden;
	}

	public void setForbidden(String forbidden) {
		this.forbidden = forbidden;
	}

	public String getBonus() {
		return bonus;
	}

	public void setBonus(String bonus) {
		this.bonus = bonus;
	}

	public ExpectAnswer getRequired() {
		return required;
	}

	public void setRequired(ExpectAnswer required) {
		this.required = required;
	}

	public String[] getVariants() {
		return variants;
	}

	public void setVariants(String[] variants) {
		this.variants = variants;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getAlternatePump() {
		return alternatePump;
	}

	public void setAlternatePump(String alternatePump) {
		this.alternatePump = alternatePump;
	}

	
}
