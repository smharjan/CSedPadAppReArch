/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.core.processors;

import dt.core.dialogue.SAClassifier;
import dt.core.dialogue.SAClassifier.SPEECHACT;
import dt.core.managers.DialogManager;
import dt.dialog.Ids;
import dt.dialog.STDialogueManager;
import dt.entities.database.Student;
import dt.entities.plain.DTInput;
import dt.entities.plain.SessionData;
import dt.entities.xml.DTResponse;
import dt.log.DTLogger;
import dt.persistent.xml.Components;
import dt.persistent.xml.DTCommands;
import dt.persistent.xml.DTResponseOld;
import dt.persistent.xml.XMLFilesManager;
import java.util.ArrayList;

/**
 *
 * @author Rajendra Created on Feb 1, 2013, 10:52:56 PM
 */
public class StudentInputProcessor {

    public static DTResponse process(Student s, DTInput input) {

        DTResponse response = null;
        SessionData data = (SessionData) s.getFromContextData("data");
        SPEECHACT sa = classifySpeechAct(s, data, input);
        input.setSpeechAct(sa);
        if (requiresFeedback(sa)) {
            response = getFeedback(s, sa);
        } else {
            //use State transition dialogue manager ? or use the general one.
            if (s.isUseStateTransitionDialogManager()) {
                STDialogueManager dialogManager = (STDialogueManager)s.getFromContextData(Ids.VAR_STDIALOG_MANAGER);
                response = dialogManager.process(s, input);  //SessionData can be retrieved from the student object. SpeechAct? put in the input.
                return response;
            }
            // Now it is supposed to be a good answer from the student. Tutor needs to check thoroughly, 
            // Here comes the semantic similarity things and all.
            response = StudentContributionProcessor.processContribution(s, data, sa, input);
        }
        return response;
    }

    /**
     * It does more than classification.. needs refactoring! This is first
     * refactoring move.., so needs another round of work to organize better.
     *
     * @return
     */
    private static SPEECHACT classifySpeechAct(Student s, SessionData data, DTInput input) {
        DTLogger logger = s.getLogger();
        SAClassifier myClassifier = new SAClassifier(input.getData());
        SPEECHACT sa = myClassifier.SAClassify();
        logger.log(DTLogger.Actor.SYSTEM, DTLogger.Level.TWO, "Speech Act Classifier: " + myClassifier.SAClassify());

        if (sa == SAClassifier.SPEECHACT.YesNoAnswer) {
            sa = SAClassifier.SPEECHACT.Contribution;
        }

        if (sa == SAClassifier.SPEECHACT.Contribution
                || sa == SAClassifier.SPEECHACT.MetaCognitive
                || (sa == SAClassifier.SPEECHACT.MetaCommunicative
                && data.expectExpectation != null && data.expectExpectation.sugestedHintIndex >= 0)) {
            // we give metacognitive feedback for
            if (sa == SAClassifier.SPEECHACT.MetaCommunicative) {
                sa = SAClassifier.SPEECHACT.MetaCognitive;
            }
        }
        return sa;
    }

    /**
     * Determines whether the student requires feedback or not.
     *
     * @param data
     * @return
     */
    private static boolean requiresFeedback(SPEECHACT sa) {

        //NOTE: the MetaCommunicative is set to meta cognitive in certain conditions (see classifySpeechAct function).
        // so, feedback is not required only when sa is contribution or metacognitive.
        if (sa == SAClassifier.SPEECHACT.Contribution
                || sa == SAClassifier.SPEECHACT.MetaCognitive) {
            return false;
        }
        return true;
    }

    /**
     * Give some feedback (get from the script file...).
     *
     * @param sa
     * @return
     */
    private static DTResponse getFeedback(Student s, SPEECHACT sa) {
        DTLogger logger = s.getLogger();
        Components c = new Components();
        DTResponseOld resp = new DTResponseOld();
        DTResponse response = new DTResponse();
        String tutorResponse = XMLFilesManager
                .getInstance().GetSomeFeedback(
                sa + "Feedback");
        //logger.log(DTLogger.Actor.TUTOR,
        //        DTLogger.Level.ONE, tutorResponse);
        resp.addResponseText(tutorResponse);
        c.inputShowContinue = false;
        c.setResponse(resp);
        response.setXmlResponse(DTCommands.getCommands(c));
        return response;
    }
}
