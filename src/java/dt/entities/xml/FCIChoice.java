/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dt.entities.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

/**
 *
 * @author Rajendra
 * Created on Jan 28, 2013, 3:40:55 PM 
 */
@XStreamAlias("choice")
@XStreamConverter(value=ToAttributedValueConverter.class, strings={"content"})
public class FCIChoice {
    
    @XStreamAlias("id")
    @XStreamAsAttribute 
    String id;
    
    private String content;

}
