/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.core.processors;

import com.sun.faces.util.CollectionsUtils;
import dt.core.managers.NLPManager;
import dt.core.semantic.SemanticRepresentation;
import dt.core.semantic.tools.SMUtils;
import dt.entities.plain.DTInput;
import dt.task.DTExpectation;
import dt.task.DTRequired;
import dt.task.DTTask;
import dt.task.ExpProgressInfo;
import dt.task.TaskProgressInfo;
import dt.utilities.MapUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Rajendra
 */
public class StudentContributionEvaluator {

    /**
     * Extract all expectations that have all required, but not forbidden words
     * in their input
     *
     * @param task
     * @param input
     * @param getMisconception
     * @return
     */
    public ArrayList<DTExpectation> extractValidExpectations(DTInput input, boolean getMisconception, TaskProgressInfo taskProgressInfo) {
        ArrayList<DTExpectation> resultList = new ArrayList<DTExpectation>();

        String[] sentences = NLPManager.getInstance().SplitIntoSentences(input.getData());

        // go through the lists of expectations
        //TODO: keep them in the list.
        DTTask task = taskProgressInfo.getTask();
        ArrayList<DTExpectation> expectations = task.getExpectations();
        if (expectations == null) {
            return new ArrayList<DTExpectation>();
        }

        for (DTExpectation exp : expectations) {
            exp.setMisconception(getMisconception); //TODO: not sure if we really need this

            for (int j = 0; j < sentences.length; j++) {
                //TODO: what is requuired?
                DTRequired required = exp.getRequired();
                ExpProgressInfo expProgressInfo = taskProgressInfo.getExpectationProgressInfo(exp.getId());
                if ((required == null || required.hasAllTheRequiredWords(sentences[j], false, expProgressInfo))
                        && (exp.getForbidden() == null || !validatesExpression(sentences[j], exp.getForbidden()))) {
                    resultList.add(exp);
                    break;
                }
            }
        }
        return resultList;
    }

    /**
     * Compare student input with the expected answers and sort based on the
     * similarity score.
     *
     * @param text
     * @param expectations
     * @return
     */
    public ArrayList<DTExpectation> computeSimilarityAndSort(String text, ArrayList<DTExpectation> expectations, TaskProgressInfo taskProgressInfo) {
        String[] sentences = NLPManager.getInstance().SplitIntoSentences(text);

        Map<String, Float> expIdSimMap = new HashMap<String, Float>();
        float maxSim;
        for (int i = 0; i < expectations.size(); i++) {
            maxSim = compareToExpectation(sentences, expectations.get(i), taskProgressInfo.getExpectationProgressInfo(expectations.get(i).getId()));
            expIdSimMap.put(expectations.get(i).getId(), maxSim);
        }

        //sort expectations by similarity score.. descending order.
        Map<String, Float> sortedMap = MapUtils.sortByComparatorFloat(expIdSimMap, false); //ascending? false.

        ArrayList<DTExpectation> sortedExpsBySimScore = new ArrayList<DTExpectation>();

        for (String key : sortedMap.keySet()) {
            for (DTExpectation exp : expectations) {
                if (exp.getId().equalsIgnoreCase(key)) {
                    sortedExpsBySimScore.add(exp);
                }
            }
        }
        return sortedExpsBySimScore;
    }

    /**
     * compare the input sentences with the expectations.
     *
     * @param sentences
     * @param expectation
     * @return
     */
    public float compareToExpectation(String[] sentences, DTExpectation expectation, ExpProgressInfo expProgressInfo) {
        float maxSim = 0;
        String maxStr = "";
        int sentenceIndex = -1;
        for (int j = 0; j < sentences.length; j++) {
            for (int k = 0; k < expectation.getVariants().size(); k++) {
                float sim = NLPManager.getInstance().ComputeT2TWNSimilarity(sentences[j], expectation.getVariants().get(k));

                if (Math.abs(sim) > Math.abs(maxSim)) {
                    maxSim = sim;
                    maxStr = expectation.getVariants().get(k);
                    sentenceIndex = j;
                }
            }
        }

        //TODO: where to set them? in the task itself or somewhere else???
        //expectation.similarity = maxSim;
        expProgressInfo.setSimilarity(maxSim);
        //expectation.mostSimilarText = maxStr;
        expProgressInfo.setMaxSimilarText(maxStr);
        //expectation.sentence = sentenceIndex;let	
        expProgressInfo.setMaxSimilarSentenceIndex(sentenceIndex); // index starts from 0.

        System.out.println("Expectation ID: " + expectation.getId() + " max similarity: " + maxSim);

        return maxSim;
    }

    /**
     *
     * @param exp
     * @param input
     * @param threshold
     * @return
     */
    public boolean matches(DTExpectation exp, String input, TaskProgressInfo taskProgressInfo, float threshold) {
        //System.out.println(input + "req:" + required.acceptedAnswer + "|th=" + threshold);

        DTRequired required = exp.getRequired();
        if (required != null && required.getAcceptedAnswer().trim().length() > 0) {
            int relen = required.getAcceptedAnswer().trim().split(",").length;
            int inputlen = input.split(" ").length;
            if (relen * 2 >= inputlen) {
                return true;
            }
        }

        // BEGIN: Vasile added the next lines on Oct 25, 2012
        // System.out.println("SIMILARITY SCORE BETWEEN STUDENT INPUT AND CURRENT EXPECTATION: " + similarity);
        float max = 0;
        ArrayList<String> variants = exp.getVariants();
        float similarity = 0.0f; //TODO: where to store this, setting in the expectation progress??

        for (int k = 0; k < variants.size(); k++) {
            similarity = NLPManager.getInstance().ComputeT2TWNSimilarity(input, variants.get(k));
            if (max < similarity) {
                max = similarity;
            }
            similarity = max;
        }
        // System.out.println("SIMILARITY SCORE BETWEEN STUDENT INPUT AND CURRENT EXPECTATION: " + similarity);
        // END: Vasile added the above lines on Oct 25, 2012
        ExpProgressInfo expProgressInfo = taskProgressInfo.getExpectationProgressInfo(exp.getId());
        expProgressInfo.setSimilarity(similarity);
        return similarity >= threshold;

    }

    /**
     * Check whether the text has some words in the list.
     *
     * @param text
     * @param list
     * @return
     */
    public boolean validatesExpression(String text, String list) {
        if (list == null) {
            return false;
        }
        SemanticRepresentation semText = new SemanticRepresentation(text);
        NLPManager.getInstance().PreprocessText(semText);
        return NLPManager.getInstance().MatchRegularExpression(semText, list, false);
    }

    /**
     * Check if the given text has the expected words (in regex format).
     *
     * negationcheck? no idea.
     *
     * @param text
     * @param expectedWordsRegex
     * @param negationCheck
     * @return
     */
    public static boolean hasAllExpectedWords(String text, String expectedWordsRegex, boolean negationCheck) {

        if (expectedWordsRegex == null || expectedWordsRegex.isEmpty()) {
            return true;
        }
        SemanticRepresentation semText = new SemanticRepresentation(text);
        NLPManager.getInstance().PreprocessText(semText);

        if (NLPManager.getInstance().MatchRegularExpression(semText, expectedWordsRegex, negationCheck)) {
            return true;
        }
        return false;
    }

    public static boolean answerTooBrief(SemanticRepresentation text) {
        int contentCount = 0;
        //Count content words
        for (int i = 0; i < text.tokens.size(); i++) {
            if (SMUtils.getWordNetPOS(text.tokens.get(i).POS) != null) {
                contentCount++;
            }
        }
        if (contentCount > 2) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean answerNotRelevant(SemanticRepresentation text, DTTask task) {
        String relevantText = task.getRelevantText();
        int totalContent = 0;
        int matchedContent = 0;

        //at least half of the content words have to be in the relevant text
        for (int i = 0; i < text.tokens.size(); i++) {
            if (SMUtils.getWordNetPOS(text.tokens.get(i).POS) != null) {
                totalContent++;
                if (relevantText.contains(text.tokens.get(i).rawForm.toLowerCase())) {
                    matchedContent++;
                }
            }
        }

        if (totalContent > matchedContent * 2) {
            return true;
        }
        return false;
    }

    //********************** for misconception handling.. similar to the above code but
    /**
     * Extract all expectations that have all required, but not forbidden words
     * in their input
     *
     * @param task
     * @param input
     * @param getMisconception
     * @return
     */
    public ArrayList<DTExpectation> extractPotentialMisconceptions(DTInput input, TaskProgressInfo taskProgressInfo) {
        ArrayList<DTExpectation> resultList = new ArrayList<DTExpectation>();

        String[] sentences = NLPManager.getInstance().SplitIntoSentences(input.getData());

        // go through the lists of expectations
        //TODO: keep them in the list.
        DTTask task = taskProgressInfo.getTask();
        ArrayList<DTExpectation> expectations = task.getExpectations();
        if (expectations == null) {
            return new ArrayList<DTExpectation>();
        }

        for (DTExpectation exp : expectations) {
            for (int j = 0; j < sentences.length; j++) {
                //TODO: what is requuired?
                DTRequired required = exp.getRequired();
                ExpProgressInfo expProgressInfo = taskProgressInfo.getMisconceptionProgressInfo(exp.getId());
                if ((required == null || required.hasAllTheRequiredWords(sentences[j], false, expProgressInfo))
                        && (exp.getForbidden() == null || !validatesExpression(sentences[j], exp.getForbidden()))) {
                    resultList.add(exp);
                    break;
                }
            }
        }
        return resultList;
    }

    /**
     * Compare student input with the expected answers and sort based on the
     * similarity score.
     *
     * @param text
     * @param expectations
     * @return
     */
    public ArrayList<DTExpectation> computeSimilarityAndSortForMisconception(String text, ArrayList<DTExpectation> expectations, TaskProgressInfo taskProgressInfo) {
        String[] sentences = NLPManager.getInstance().SplitIntoSentences(text);

        Map<String, Float> expIdSimMap = new HashMap<String, Float>();
        float maxSim;
        for (int i = 0; i < expectations.size(); i++) {
            maxSim = compareToExpectation(sentences, expectations.get(i), taskProgressInfo.getMisconceptionProgressInfo(expectations.get(i).getId()));
            expIdSimMap.put(expectations.get(i).getId(), maxSim);
        }

        //sort expectations by similarity score.. descending order.
        Map<String, Float> sortedMap = MapUtils.sortByComparatorFloat(expIdSimMap, false); //ascending? false.

        ArrayList<DTExpectation> sortedExpsBySimScore = new ArrayList<DTExpectation>();

        for (String key : sortedMap.keySet()) {
            for (DTExpectation exp : expectations) {
                if (exp.getId().equalsIgnoreCase(key)) {
                    sortedExpsBySimScore.add(exp);
                }
            }
        }
        return sortedExpsBySimScore;
    }

    /**
     *
     * @param exp
     * @param input
     * @param threshold
     * @return
     */
    public boolean matchesMisconception(DTExpectation exp, String input, TaskProgressInfo taskProgressInfo, float threshold) {
        //System.out.println(input + "req:" + required.acceptedAnswer + "|th=" + threshold);

        DTRequired required = exp.getRequired();
        if (required != null && required.getAcceptedAnswer().trim().length() > 0) {
            int relen = required.getAcceptedAnswer().trim().split(",").length;
            int inputlen = input.split(" ").length;
            if (relen * 2 >= inputlen) {
                return true;
            }
        }

        // BEGIN: Vasile added the next lines on Oct 25, 2012
        // System.out.println("SIMILARITY SCORE BETWEEN STUDENT INPUT AND CURRENT EXPECTATION: " + similarity);
        float max = 0;
        ArrayList<String> variants = exp.getVariants();
        float similarity = 0.0f; //TODO: where to store this, setting in the expectation progress??

        for (int k = 0; k < variants.size(); k++) {
            similarity = NLPManager.getInstance().ComputeT2TWNSimilarity(input, variants.get(k));
            if (max < similarity) {
                max = similarity;
            }
            similarity = max;
        }
        // System.out.println("SIMILARITY SCORE BETWEEN STUDENT INPUT AND CURRENT EXPECTATION: " + similarity);
        // END: Vasile added the above lines on Oct 25, 2012
        ExpProgressInfo expProgressInfo = taskProgressInfo.getMisconceptionProgressInfo(exp.getId());
        expProgressInfo.setSimilarity(similarity);
        return similarity >= threshold;

    }

    ArrayList<DTExpectation> extractPotentialMisconceptions(DTInput input, boolean b, TaskProgressInfo taskProgressInfo) {
        ArrayList<DTExpectation> resultList = new ArrayList<DTExpectation>();

        String[] sentences = NLPManager.getInstance().SplitIntoSentences(input.getData());

        // go through the lists of expectations
        //TODO: keep them in the list.
        DTTask task = taskProgressInfo.getTask();
        ArrayList<DTExpectation> misconceptions = task.getMisconceptions();
        if (misconceptions == null || misconceptions.size() <= 0) {
            return new ArrayList<DTExpectation>();
        }

        for (DTExpectation exp : misconceptions) {
            for (int j = 0; j < sentences.length; j++) {
                //TODO: what is requuired?
                DTRequired required = exp.getRequired();
                ExpProgressInfo expProgressInfo = taskProgressInfo.getMisconceptionProgressInfo(exp.getId());
                if ((required == null || required.hasAllTheRequiredWords(sentences[j], false, expProgressInfo))
                        && (exp.getForbidden() == null || !validatesExpression(sentences[j], exp.getForbidden()))) {
                    resultList.add(exp);
                    break;
                }
            }
        }
        return resultList;
    }
}
