/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.entities.plain;

import dt.core.dialogue.SAClassifier;
import dt.core.processors.InputTextPreprocessor.InputTextCategory;
import dt.log.DTLogger;

/**
 * The user input in the dialogue system.
 *
 * @author Rajendra Created on Feb 1, 2013, 4:54:46 PM
 */
public class DTInput {

    //the category of input text.
    InputTextCategory category;
    //the header - typically the first token in the input text, that has determines
    //the input category (eg. \\debug, hi, hello) or null (when the student is answering).
    String header;
    //Input text except the header (actionable part).
    String data;
    //the input as is (no header separated, no spelled checked, no trimming etc.
    String rawText;
    //parsing error, or some error detected in the input text (usually set, when the 
    // input text category is unknown).
    String errorMessage;
    //SpeechAct
    SAClassifier.SPEECHACT speechAct;

    public DTInput() {
        this.category = InputTextCategory.UNKNOWN;
        this.data = null;
        this.header = null;
        this.rawText = null;
        this.speechAct = SAClassifier.SPEECHACT.DummyClass; //TODO: is it default??
    }

    public SAClassifier.SPEECHACT getSpeechAct() {
        return speechAct;
    }

    public void setSpeechAct(SAClassifier.SPEECHACT speechAct) {
        this.speechAct = speechAct;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public InputTextCategory getCategory() {
        return category;
    }

    public void setCategory(InputTextCategory category) {
        this.category = category;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    /*
     * Log into the student log file..
     */
    public void logPreprocessedInput(DTLogger logger) {
        //logger.log(DTLogger.Actor.NONE, DTLogger.Level.ONE, this.getData());
    }

    /*
     * Log into the student log file..
     */
    public void logRawInput(DTLogger logger) {
        //logger.log(DTLogger.Actor.NONE, DTLogger.Level.ONE, this.getRawText());
    }
}
