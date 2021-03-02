/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dt.core.processors;

import dt.constants.UserCommands;
import dt.entities.database.Student;
import dt.entities.plain.DTInput;
import dt.entities.xml.DTResponse;

/**
 *
 * @author Rajendra
 * Created on Feb 2, 2013, 10:24:09 AM 
 */
public class UserCommandProcessor {

        public static DTResponse process(Student s, DTInput input) {
        DTResponse response = null;

        //if dialogue form loaded..., this is the first time the user sees
        //dialogue. So, do some initialization, loading, etc.
        if (input.getHeader().equalsIgnoreCase(UserCommands.DEBUGTASK)) {
            s.clearAllContextData();
            
            //load new question..., and return response.
        }
        return response;
    }
}
