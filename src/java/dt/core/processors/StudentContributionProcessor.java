/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.core.processors;

import dt.core.dialogue.SAClassifier;
import dt.core.managers.DialogManager;
import dt.core.managers.NLPManager;
import dt.core.semantic.SemanticRepresentation;
import dt.entities.database.Student;
import dt.entities.plain.DTInput;
import dt.entities.plain.SessionData;
import dt.entities.xml.DTResponse;
import dt.log.DTLogger;
import dt.persistent.xml.Components;
import dt.persistent.xml.DTCommands;
import dt.persistent.xml.DTResponseOld;
import dt.persistent.xml.Expectation;
import dt.persistent.xml.XMLFilesManager;
import dt.task.DTExpectation;
import dt.task.DTHint;
import dt.task.DTPrompt;
import dt.task.DTTask;
import dt.task.ExpProgressInfo;
import dt.task.TaskProgressInfo;
import java.util.ArrayList;

/**
 *
 * @author Rajendra
 */
public class StudentContributionProcessor {

    final static float MATCH_THRESHOLD = 0.4f; //TODO: change it. originally 0.4f
    final static float GOOD_MATCH_THRESHOLD = 0.5f; //originally 0.5f 

    /**
     * Not sure if it is a good name, but the dialogue manager function called from here has similar name.
     *
     * @param data
     * @param sa
     * @param input
     * @return
     */
    public static DTResponse processContribution(Student s, SessionData data, SAClassifier.SPEECHACT sa, DTInput input) {
        DTLogger logger = s.getLogger();
        DTResponse response = new DTResponse();
        Components c = (new DialogManager()).ProcessContribution(sa, data, logger, input.getData());

        // Look for leftover text --------------------------
        // Save the leftover text (if required). I don't fully understand
        data.leftoverText = new ArrayList<String>();
        ArrayList<String> newResponseText = new ArrayList<String>();
        boolean save2leftover = false;
        for (int i = 0; i < c.getResponse().getResponseCount(); i++) {
            if (!save2leftover) {
                if (c.getResponse().getResponseText(i).equals("#WAIT#")) {
                    save2leftover = true;
                } else {
                    newResponseText.add(c.getResponse().getResponseText(i));
                }
            } else {
                data.leftoverText.add(c.getResponse().getResponseText(i));
            }
        }
        if (save2leftover) {
            c.getResponse().setResponseArray(newResponseText);
            c.inputShowContinue = true;
        }

        response.setXmlResponse(DTCommands.getCommands(c));
        return response;
    }

    /**
     *
     * @param task
     * @param taskProgressInfo
     * @return
     */
    public static void checkIfAnswerCoversAnyExpectations(DTInput input, TaskProgressInfo taskProgressInfo) {
        //Components c = new Components();
        DTResponseOld response = new DTResponseOld();
        StudentContributionEvaluator answerEvaluator = new StudentContributionEvaluator();
        ArrayList<DTExpectation> validExpectations = null;
        //String responseData = "T" + /* TODO: data.turnNumber ?? + */ ", "; // more structured feedback containing all the data elements that were used.
        taskProgressInfo.setRespDataForLogging("T" + /* TODO: data.turnNumber ?? + */ ", ");

        //extract all expectations from task such that the input answer has required keywoards (and so)
        //to satisfy the minimum requirement of expectations.
        validExpectations = answerEvaluator.extractValidExpectations(input, false, taskProgressInfo);

        //measure the similarity and sort based on the score.
        // why to compute similarity without filtering out previously covered expectations? It is sometimes useful.
        // if the answer doesn't cover any uncovered expectations, misconceptions, but covers previously covered expectation, we need to give feedback
        // saying that we already assed that.
        validExpectations = answerEvaluator.computeSimilarityAndSort(input.getData(), validExpectations, taskProgressInfo);
        //TODO: how to log
        //LogExpectationsFound(logger,"Expectations", expectations);
        //LogExpectationsFound(logger,"Uncovered Expectations",uncoveredExpectations);

        ArrayList<DTExpectation> previouslyUncoveredExpectations = taskProgressInfo.getPreviouslyUncoveredExpectations(validExpectations);

        //if the first uncovered expectation is covered by the student answer
        if (previouslyUncoveredExpectations.size() > 0
                && answerEvaluator.matches(previouslyUncoveredExpectations.get(0), input.getData(), taskProgressInfo, MATCH_THRESHOLD) //TODO: ?? || (currentExpectation == null || (currentExpectation != null && !currentExpectation.hintSuggested))
                ) {

            DTExpectation firstExpectationCovered = previouslyUncoveredExpectations.get(0);
            ExpProgressInfo expProgressInfo = taskProgressInfo.getExpectationProgressInfo(firstExpectationCovered.getId());
            float similarity = expProgressInfo.getSimilarity();
            String forbiddenWords = firstExpectationCovered.getForbidden();
            taskProgressInfo.setExpectationRejected(false);

            // If forbidden words are in the input, we give negative feedback.
            // similarity can be less than or equal to (-1)*GOOD_MATCH_THRESHOLD??
            if ((firstExpectationCovered.getRequired() != null)
                    && (similarity <= (-1) * GOOD_MATCH_THRESHOLD)
                    || (forbiddenWords != null && answerEvaluator.validatesExpression(input.getData(), forbiddenWords))) {
                taskProgressInfo.setExpectationRejected(true);
                firstExpectationCovered = null;
                response.addResponseText(XMLFilesManager.getInstance().GetSomeFeedback("NegativeFeedback"));
                taskProgressInfo.appRespDataForLogging("ShortNegF");

            } else {
                //
                if ((firstExpectationCovered.getRequired() != null) && expProgressInfo.getMatchedVariant() >= 0) {
                    response.addResponseText(firstExpectationCovered.getRequired().getAnswerFeedback(expProgressInfo));
                    taskProgressInfo.appRespDataForLogging("GoodWithF#" + expProgressInfo.getMatchedVariant() + "#" + firstExpectationCovered.getId());
                } else if (similarity >= GOOD_MATCH_THRESHOLD) {
                    response.addResponseText(XMLFilesManager.getInstance().GetSomeFeedback("PositiveFeedback"));
                    taskProgressInfo.appRespDataForLogging("PositiveF");

                } else {
                    response.addResponseText(XMLFilesManager.getInstance().GetSomeFeedback("PositiveNeutral"));
                    taskProgressInfo.appRespDataForLogging("NeutralF");
                }
                taskProgressInfo.setExpectationsHit(true);
            }

            //
            if (!taskProgressInfo.isExpectationRejected()) {
                taskProgressInfo.appRespDataForLogging(", Cover(");
                for (int i = 0; i < previouslyUncoveredExpectations.size(); i++) {
                    if (answerEvaluator.matches(previouslyUncoveredExpectations.get(i), input.getData(), taskProgressInfo, MATCH_THRESHOLD)) //TODO: always index 0??
                    {
                        response.addResponseText(previouslyUncoveredExpectations.get(i).getAssertion());
                        taskProgressInfo.appRespDataForLogging((i > 0 ? "#" : "") + previouslyUncoveredExpectations.get(i).getId()); //TODO: i > 0?
                        taskProgressInfo.setExpectationCovered(previouslyUncoveredExpectations.get(i).getId());
                        //data.CoverExpectation(uncoveredExpectations[i].id);
                        // Vasile: System.out.println("COVERING EXPECTATION: " + uncoveredExpectations[0].assertion + " vs " + uncoveredExpectations[i].assertion + "\n.");
                    }
                }
                taskProgressInfo.appRespDataForLogging("), ");
                //TODO: expectationsWereHit = true; what to do with this? see below.
                taskProgressInfo.setExpectationsHit(true);
                response.addResponseText("#WAIT#");

                //check if the current expectation is covered; if it is then we must get a new expectation
                //TODO: see below, is it correct? if (currentExpectation != null && data.FindExpectation(currentExpectation.id).covered) currentExpectation = null;
                ExpProgressInfo currentExpProgressInfo = taskProgressInfo.getCurrentExpProgressInfo();
                if (currentExpProgressInfo != null) {
                    if (currentExpProgressInfo.isCoverd()) {
                        taskProgressInfo.setCurrentExpProgressInfo(null);
                        taskProgressInfo.setWorkingExpCovered(true);
                    }
                }
            }
        }
        //set the response. We are overridding if there is any response as the response varible was created here (in old code also).
        taskProgressInfo.setDTResponseOld(response);

        //TODO: if no exp is hit, what to do. Short anser, irrelevant answer, answer to the alredy covered exp etc.
        if (!taskProgressInfo.isExpectationsHit() && !taskProgressInfo.isExpectationRejected()) {
            handleNoExpectationsWereHit(input, taskProgressInfo, validExpectations, previouslyUncoveredExpectations);
        }
    }

    /**
     * Hits misconception, short answer, already assessed etc.
     */
    private static void handleNoExpectationsWereHit(DTInput input, TaskProgressInfo taskProgressInfo, ArrayList<DTExpectation> validExps, ArrayList<DTExpectation> previouslyUncoveredExps) {

        DTResponseOld respOld = taskProgressInfo.getDTResponseOld();
        respOld.clearResponse();
        boolean misconceptionHit = checkIfUncoveredMisconceptionsWereHit(input, taskProgressInfo, validExps, previouslyUncoveredExps);
        if (misconceptionHit) {
            taskProgressInfo.setMisconceptionHit(true);
            return;
        }

        // check if the input covers previously covered expectations.
        if (validExps.size() > 0) {
            ExpProgressInfo expProgressInfo = taskProgressInfo.getExpectationProgressInfo(validExps.get(0).getId());
            if (expProgressInfo.isCoverd()) {
                respOld.addResponseText("Yes. We already assessed that:");
                respOld.addResponseText(expProgressInfo.getExpectation().getAssertion());
                //TODO: anything to do with hint??
                taskProgressInfo.setAlreadyAssessed(true);
                return;
            }
        }

        SemanticRepresentation semText = new SemanticRepresentation(input.getData());
        NLPManager.getInstance().PreprocessText(semText);
        //check for bonus words
        //TODO:
        //Expectation[] bonusExpectations = se.ExtractBonusExpectations(inputText, false);
        // if (bonusExpectations.length>0 && !taskProgressInfo.isAnswerTooBrief()) {
        //     respOld.addResponseText("You are on the right track. Can you think more and be more specific?");	
        // }

        //check for too short.
        //TODO:
        // Vasile added on Oct 25, 2012: <&& se.AnswerNotRelevant (semText)>
        // Vasile: the reason is that a student input should be classified as too short only if the student input does not contain relevant content
        if (StudentContributionEvaluator.answerTooBrief(semText) && StudentContributionEvaluator.answerNotRelevant(semText, taskProgressInfo.getTask())) {
            respOld.addResponseText(XMLFilesManager.getInstance().GetSomeFeedback("AnswerTooShort"));
            //TODO: what else?
            taskProgressInfo.setAnswerTooBrief(true);
            return;
        }

        //Just irrelevant..
        if (StudentContributionEvaluator.answerNotRelevant(semText, taskProgressInfo.getTask())) {
            respOld.addResponseText(XMLFilesManager.getInstance().GetSomeFeedback("AnswerIrrelevant"));
            taskProgressInfo.setAnswerIrrelevant(true);
            //TODO: what else?
            return;
        }

        //TODO: is negative similarity possible?
        if (previouslyUncoveredExps.size() > 0) {
            ExpProgressInfo expProgressInfo = taskProgressInfo.getExpectationProgressInfo(previouslyUncoveredExps.get(0).getId());

            if (expProgressInfo.getSimilarity() <= -MATCH_THRESHOLD) {
                respOld.addResponseText(XMLFilesManager.getInstance().GetSomeFeedback("NegativeFeedback"));
            } else {
                respOld.addResponseText(XMLFilesManager.getInstance().GetSomeFeedback("NegativeNeutral"));
            }
        } else {
            respOld.addResponseText("Let me try again.");
        }

        //TODO: "Okay. Let me ask you this.", when ??
        
        //TODO: what else? satype specific response, see old dm code. Does it come to this place for other than contribution?
    }

    private static boolean checkIfUncoveredMisconceptionsWereHit(DTInput input, TaskProgressInfo taskProgressInfo, ArrayList<DTExpectation> validExps, ArrayList<DTExpectation> previouslyUncoveredExps) {
        StudentContributionEvaluator answerEvaluator = new StudentContributionEvaluator();

        ArrayList<DTExpectation> validMisconceptions = answerEvaluator.extractPotentialMisconceptions(input, false, taskProgressInfo);

        if (validMisconceptions == null || validMisconceptions.size() <= 0) {
            return false;
        }
        ArrayList<DTExpectation> previouslyUncoveredMisconceptions = taskProgressInfo.getPreviouslyUncoveredMisconceptions(validMisconceptions);
        if (previouslyUncoveredMisconceptions.isEmpty()) {
            return false;
        }
        //measure the similarity and sort based on the score.
        previouslyUncoveredMisconceptions = answerEvaluator.computeSimilarityAndSortForMisconception(input.getData(), previouslyUncoveredMisconceptions, taskProgressInfo);

        DTResponseOld respOld = taskProgressInfo.getDTResponseOld();
        respOld.clearResponse(); //clear if there is anything.

        ExpProgressInfo miscProgressInfo = taskProgressInfo.getMisconceptionProgressInfo(previouslyUncoveredMisconceptions.get(0).getId());

        if (miscProgressInfo.getSimilarity() >= MATCH_THRESHOLD) {
            miscProgressInfo.setCoverd(true);
            String miscAssertion = miscProgressInfo.getMisconception().getAssertion();
            if (miscAssertion == null) {
                miscAssertion = "[Correction missing for " + miscProgressInfo.getId() + "].";
            }
            respOld.addResponseText(miscAssertion);
            //TODO: What about yoked expectation? The yoked expectation is handled next. What is that?? exp related to the misconception??
            return true; //returning from here, no harm?
        }
        return false;
    }

    /**
     * Get pump of the current expectation.
     *
     * @param input
     * @param task
     * @param taskProgressInfo
     */
    public static void getPump(DTInput input, TaskProgressInfo taskProgressInfo) {
        ExpProgressInfo currentExpProgressInfo = taskProgressInfo.getCurrentExpProgressInfo();
        DTResponseOld response = taskProgressInfo.getDTResponseOld();
        String pumpText = currentExpProgressInfo.getExpectation().getPump();
        if (pumpText == null) {
            response.addResponseText("[pump missing for " + currentExpProgressInfo.getId() + "]");
        } else {
            response.addResponseText(pumpText);
        }
        taskProgressInfo.appRespDataForLogging("Pump");
    }

    /**
     * Evaluate the hint response.
     *
     * @param input
     * @param taskProgressInfo
     */
    public static void evaluateHintAnswer(DTInput input, TaskProgressInfo taskProgressInfo) {
        ExpProgressInfo currentExpProgressInfo = taskProgressInfo.getCurrentExpProgressInfo();
        DTHint hint = currentExpProgressInfo.getCurrentHint();
        boolean isHintAnswerCorrect = StudentContributionEvaluator.hasAllExpectedWords(input.getData(), hint.getExpectedWords(), false); //TODO: handleNegation: false?
        DTResponseOld response = taskProgressInfo.getDTResponseOld();

        if (isHintAnswerCorrect) {
            response.addResponseText(XMLFilesManager.getInstance().GetSomeFeedback("PositiveFeedback"));  //Give positive feedback.
            currentExpProgressInfo.setCorrectAnswerToTheHint(true);
        } else {
            response.addResponseText(hint.getNegative()); //TODO: add something else before?
            currentExpProgressInfo.setCorrectAnswerToTheHint(false);
        }
        currentExpProgressInfo.advanceHintIndex(); //point to next hint.

        //TODO: what if it hits some misconception?? or answer too short etc.
    }

    public static void evaluatePromptAnswer(DTInput input, TaskProgressInfo taskProgressInfo) {
        ExpProgressInfo currentExpProgressInfo = taskProgressInfo.getCurrentExpProgressInfo();
        DTPrompt prompt = currentExpProgressInfo.getPrompt();
        boolean isPromptAnswerCorrect = StudentContributionEvaluator.hasAllExpectedWords(input.getData(), prompt.getExpectedWords(), false); //TODO: handleNegation: false?
        DTResponseOld response = taskProgressInfo.getDTResponseOld();

        if (isPromptAnswerCorrect) {
            response.addResponseText(XMLFilesManager.getInstance().GetSomeFeedback("PositiveFeedback"));  //Give positive feedback.
        } else {
            response.addResponseText(prompt.getNegative()); //TODO: add something else before?
        }
    }
}
