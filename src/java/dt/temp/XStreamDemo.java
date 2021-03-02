/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.temp;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import dt.config.ConfigManager;
//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;
//import org.apache.log4j.xml.DOMConfigurator;

/**
 *
 * @author Rajendra Created on Jan 28, 2013, 3:18:08 PM
 */
public class XStreamDemo {
    
//    static final Logger logger = Logger.getLogger(XStreamDemo.class);
//
//    public static void main(String[] args) {
//        
//        // initialize logger..
//        DOMConfigurator.configure(ConfigManager.getDebugLogConfigFile());
//        logger.error("this is beginning of log ....");
//        //XStream xstream = new XStream();
//        XStream xstream = new XStream(new DomDriver()); // does not require XPP3 librar
//        xstream.alias("person", Person.class);
//        xstream.alias("phonenumber", PhoneNumber.class);
//
//        Person joe = new Person("Joe", "Walnes");
//        joe.setPhone(new PhoneNumber(123, "1234-456"));
//        joe.setFax(new PhoneNumber(123, "9999-999"));
//        
//        String xml = xstream.toXML(joe);
//        
//        System.out.println(xml);
//       
//        logger.debug(xml);
//    }
}
