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
 * Created on Jan 28, 2013, 3:37:07 PM 
 */
@XStreamAlias("Context")
public class FCIContext {
    
    @XStreamAlias("cid")
    @XStreamAsAttribute
    String cid;
    
    String contextpicture;
    String contextdescription;
   
    @XStreamImplicit(itemFieldName="question")
    List<FCIQuestion> questions;
    
    public FCIContext() {
        questions = new ArrayList<FCIQuestion>();
    }
}
