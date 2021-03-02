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
 * Created on Jan 28, 2013, 3:26:55 PM 
 */
@XStreamAlias("FCI")
public class FCIQuestions {
    @XStreamImplicit(itemFieldName="Context")
    List<FCIContext> contexts;
    
    public FCIQuestions() {
        contexts = new ArrayList<FCIContext>();
    }
}
