/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dt.core.processors;

import dt.entities.database.Student;
import dt.entities.plain.DTInput;
import dt.entities.xml.DTResponse;

/**
 *
 * @author Rajendra
 * Created on Feb 1, 2013, 6:47:28 PM 
 */
public class GreetingProcessor {
    
    static 
    {
        //load lookup table.. etc.
    }
            
    public static DTResponse process(Student s, DTInput input)
    {
        ///NOT in use..
        DTResponse response = new DTResponse();
        response.setXmlResponse("Hi " + s.getStudentId());
        return response;
    }

}
