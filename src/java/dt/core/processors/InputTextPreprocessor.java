/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.core.processors;

import dt.constants.DTCommandID;
import dt.constants.UserCommands;
import dt.core.dialogue.DTSpellChecker;
import dt.entities.database.Student;
import dt.entities.plain.DTInput;
import dt.entities.plain.SessionData;

/**
 *
 * @author Rajendra Created on Feb 1, 2013, 4:56:32 PM
 */
public class InputTextPreprocessor {

    public enum InputTextCategory {

        DT_COMMAND, USER_COMMAND, STUDENT_INPUT, UNKNOWN;
    }

    /**
     * Do some preprocessing - parse, classify the input text, spell correction,
     * chomp etc.
     *
     * @param s
     * @param inputText
     * @return
     */
    public static DTInput process(Student s, String inputText) {
        DTInput dtInput = new DTInput();
        dtInput.setRawText(inputText); //put as is.
        if (inputText == null || inputText.trim().length() == 0) {
            return dtInput;
        }
        String text = cleanUp(inputText);
        text = doSomeReplacements(s, text);
        String[] splits = text.split(" ");
        // for now, the header is simply the first chunk of space separated data.
        String header = splits[0];

        //if it is command (starts with \\) AND it is valid. 
        // Some code seems to be redundant but kept there for legibility as parsing 
        // should be clean as much as possible.
        if (header.startsWith("\\")) {
            if (isValidDTCommand(header)) {
                dtInput.setCategory(InputTextCategory.DT_COMMAND);
                dtInput.setHeader(header);
                dtInput.setData(text.replaceFirst(header, "").trim()); //rest of header (trimmed).
                //TODO: check the payload.. if it is valid. (flag error, and error text as required).
                return dtInput;
            } else if (isValidUserCommand(header)) {
                dtInput.setCategory(InputTextCategory.USER_COMMAND);
                dtInput.setHeader(header);
                dtInput.setData(text.replaceFirst(header, "").trim()); //rest of header (trimmed).
                //TODO: check the payload.. if it is valid (valid task for this student etc..)
                //(flag error, and error text as required).
                return dtInput;
            }
            dtInput.setErrorMessage("Unknown command: " + header);
            return dtInput;
        }

        //else asssume the student answer. (no header text).
        dtInput.setCategory(InputTextCategory.STUDENT_INPUT);
        text = performSpellChecking(text);
        dtInput.setData(text);
        return dtInput;
    }

    /**
     *
     * @param inputText
     * @return
     */
    private static String cleanUp(String inputText) {
//        // extract any comments that the user said ?????
//        String commentedText = inputText;
//        inputText = StringTools.RemoveComments(commentedText);
//        if (inputText.trim().length() == 0) {
//            resp.addResponseText("Comment acknowledged.");
//
//        }
        return inputText.trim();
    }

    /**
     * Replace pronoun (it), ask user what doe he/she mean by it?
     *
     * @param s
     * @param inputText
     * @return
     */
    private static String doSomeReplacements(Student s, String inputText) {
        //NOTE: Here is something wrong..
        /*
        SessionData data = (SessionData) s.getFromContextData("data");
        if (data.inputReplaceIt != null) {
            inputText = data.inputReplaceIt.replaceAll(
                    "<<IT>>", inputText);
            data.inputReplaceIt = null;
        }
        */ 
        
        return inputText;
    }

    /**
     * Spell checking.. brought from dialogue manager of old DT.
     *
     * @param text
     * @return
     */
    private static String performSpellChecking(String text) {
        return DTSpellChecker.getInstance().spellCheck(text);        
    }

    /**
     * check if valid DT command.
     *
     * @param command
     * @return
     */
    private static boolean isValidDTCommand(String commandText) {

        if (commandText.equalsIgnoreCase(DTCommandID.INITIALIZE)
                || commandText.equalsIgnoreCase(DTCommandID.CONTINUE)
                || commandText.equalsIgnoreCase(DTCommandID.RESETSESSION)
                || commandText.equalsIgnoreCase(DTCommandID.CHANGEDM)
                || commandText.equalsIgnoreCase(DTCommandID.MOVETONEXT)) {
            return true;
        }
        return false;
    }

    /**
     * check if valid user command.
     *
     * @param command
     * @return
     */
    private static boolean isValidUserCommand(String commandText) {

        if (commandText.equalsIgnoreCase(UserCommands.DEBUGTASK)) {
            return true;
        }
        return false;
    }
}
