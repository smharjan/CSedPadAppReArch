/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.temp;

/**
 *
 * @author Rajendra Created on Jan 30, 2013, 9:47:23 AM
 */
public class DevelopersTODOList {
    /* Rajendra:
     * 1. instead of introducing many modes, just have one (A, B, C, D or so)
     * 2. Have one more method in each action, that makes sure that student is 
     *   get from session at the beginning and saved before going anywhere else.
     *   Updating student in session from anywhere is error prone.
     * 3. Send some user command from dialogue input to change the server behaviour in runtime
     *    such as, changing the time out, enabling/disabling some feature etc.
     * 4.why not using jar for edu.sussex.nlp.jws?? is there any local change.
     * 5. Don't do save log to html, just create logger when user is logged in and keep 
     *    that logger in the student object (as each student does have their own log file..) and save to html just before returning from that action.
     *    Think about a good design..
     * 6. Some class members are are public and are accessed without using getter and setter.
     *    Do some refactoring.
     * 7. Implement state manager..
     */
    /* Nobal:
     * 1. instead of introducing many modes, just have one (A, B, C, D or so)
     * 
     * 
     */
    /* Dan:
     * 1. instead of introducing many modes, just have one (A, B, C, D or so)
     * 
     * 
     */
    
    
    /**
     * Meeting: August 8, 2014 (Dr. Rus, Rajendra, and Nobal)
     * 
     * 1. Correct spelling in dialog policy file.
     * 2. getFeedback (confused with give feedback). Better to rename with getFeedbackForInitialResponse (accumulate.. etc).
     * 3. !~firstUncoveredExpectation. is confusing. Change it to: someExpectationsCoveredAlready, NoExpectationCoveredYet.
     *    someExpCovered.
     * 4. correctAnswerToThePump - isCurrentExpectCovered, currentExpectationCoveredAtPump.
     * 5. When to mention and when to not. giveFeedBack.
     * 7. Readability.
     * 8. If the conditional hint is correct, jump to the final hint.
     */
    
    
}
