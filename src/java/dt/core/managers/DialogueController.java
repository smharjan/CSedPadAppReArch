/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.core.managers;

import dt.core.processors.InputTextPreprocessor;
import dt.core.processors.InputTextPreprocessor.InputTextCategory;
import dt.core.processors.StudentInputProcessor;
import dt.core.processors.DTCommandProcessor;
import dt.core.processors.GreetingProcessor;
import dt.entities.database.Student;
import dt.entities.plain.DTInput;
import dt.entities.xml.DTResponse;
import dt.log.DTLogger;

/**
 * NOTE: it was named DialogueManager but the old code also has the class named DialogueManager.
 *       so renamed to DialogueController.. still similar name.. might be confusing.
 * @author Rajendra Created on Feb 1, 2013, 3:29:57 PM
 */
public class DialogueController {

    /**
     * Main entry point of input handler.
     *
     * @param s
     * @param inputText
     * @return
     */
    public static DTResponse process(Student s, String inputText) {

        //preprocess the text, categorize, spell correction, triming etc.
        DTInput input = InputTextPreprocessor.process(s, inputText);
        input.logRawInput(s.getLogger());
        input.logPreprocessedInput(s.getLogger());

        DTResponse resp = new DTResponse();

        //temporary if else, if else..
        if (input.getCategory() == InputTextCategory.UNKNOWN) {
            // unknown command, unknwon format etc. For instance, user may not
            // input the proper command for debugging etc.
            resp.setErrorFlagged(true);
            resp.setResponseText("Failed to process your input : " + input.getErrorMessage());
            s.getLogger().log(DTLogger.Actor.SYSTEM, DTLogger.Level.ONE, "Failed to process the input/command : " + input.getErrorMessage());
            return resp;
        }

        // Handle the dt command (i.e. sent by DT itself)
        // Debug command (eg. \\debugtask ) fall in to USER_COMMAND
        if (input.getCategory() == InputTextCategory.DT_COMMAND) {
            return DTCommandProcessor.process(s, input);
        }

        if (input.getCategory() == InputTextCategory.USER_COMMAND) {
            return DTCommandProcessor.process(s, input);
        }

        //
        if (input.getCategory() == InputTextCategory.STUDENT_INPUT) {
            return StudentInputProcessor.process(s, input);
        }
        return null;
    }
}
