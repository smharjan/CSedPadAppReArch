/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dt.entities.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Rajendra
 * Created on Jan 28, 2013, 4:51:42 PM 
 */
public class XTreamTest {
    
    
    public static void main(String []args) throws FileNotFoundException, IOException
    {
        
        //XStream xstream = new XStream();
        XStream xstream = new XStream(new DomDriver()); // does not require XPP3 librar
        
        xstream.processAnnotations(FCIQuestions.class);
        xstream.processAnnotations(FCIContext.class);
        xstream.processAnnotations(FCIQuestion.class);
        xstream.processAnnotations(FCIChoice.class);
        
        //xstream.alias("FCI", FCIQuestions.class);
        //xstream.alias("Context", FCIContext.class);
        //xstream.alias("question", FCIQuestion.class);
        //xstream.alias("choice", FCIChoice.class);
        //xstream.addImplicitCollection(FCIQuestions.class, "contexts");
        //xstream.addImplicitCollection(FCIContext.class, "questions");
        //xstream.addImplicitCollection(FCIQuestion.class, "choices");
        
        //xstream.useAttributeFor(FCIContext.class, "cid");
        
        
        
        
        //xstream.alias("person", Person.class);
        //xstream.alias("phonenumber", PhoneNumber.class);
        //String xml = xstream.toXML(joe);
        
        String inputFile = "src/java/dt/entities/xml/FCI-Blended-A.xml";
        String outputFile = "D:\\workspace\\csedpad\\src\\java\\dt\\entities\\xml\\xml-test.xml";
        
        OutputStream os = new FileOutputStream(outputFile);
        
        
        FCIQuestions fciQuestions =  (FCIQuestions)xstream.fromXML(new File(inputFile));

        xstream.toXML(fciQuestions, os);
        os.close();
        //System.out.println(xml);        
        
    }
    

}
