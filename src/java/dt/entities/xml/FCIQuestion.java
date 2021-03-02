/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dt.entities.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rajendra
 * Created on Jan 28, 2013, 3:37:23 PM 
 */
@XStreamAlias("question")
public class FCIQuestion {

    @XStreamAlias("id")
    @XStreamAsAttribute    
    String id;

    String questionpic;
    
    String answer;
    
    String text;
    
    //@XStreamImplicit(itemFieldName="choice")
    List<FCIChoice> choices;
    
    public FCIQuestion() {
        choices = new ArrayList<FCIChoice>();
    }
}
