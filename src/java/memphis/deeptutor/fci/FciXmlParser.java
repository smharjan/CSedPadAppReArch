package memphis.deeptutor.fci;
import noNamespace.ContextDocument;
import noNamespace.FCIDocument;
import noNamespace.QuestionDocument.Question;

import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;


public class FciXmlParser {

	public static Map<Integer,ContextDocument.Context> contextMap=null;

    public static Map<Integer,ContextDocument.Context> getContexts(String xmlpath) throws Exception{
    	
    	contextMap = new TreeMap<Integer,ContextDocument.Context>();
    	
        File xmlFile = new File(xmlpath);
        ContextDocument.Context[] contexts=null;
        try {
            FCIDocument tld = FCIDocument.Factory.parse(xmlFile);
            FCIDocument.FCI fci=tld.getFCI() ;
            contexts = fci.getContextArray();
           
            for(ContextDocument.Context c: contexts)   {
                System.out.println("-->"+c.getCid() +"--"+c.getContextdescription());
                contextMap.put(c.getCid().intValue(),c);
            }
        } catch (XmlException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return contextMap;
    }
    
    public static Map<String, String> getFCIAnswers(String xmlpath){

    	File xmlFile = new File(xmlpath);
    	TreeMap<String, String> answersMap = new TreeMap<String,String>();
    	
    	try {
            FCIDocument tld = FCIDocument.Factory.parse(xmlFile);
            FCIDocument.FCI fci=tld.getFCI() ;
            ContextDocument.Context[] contexts=fci.getContextArray();
           
            for(ContextDocument.Context c: contexts)   {
            	Question[] questions = c.getQuestionArray();
            	for (int i=0;i<questions.length;i++) 
            	{
            		answersMap.put(questions[i].getId().toString(), questions[i].getAnswer());
                    //System.out.println("-->"+questions[i].getId().intValue() +"--"+questions[i].getAnswer());
            	}
            }
        } catch (XmlException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    	
    	return answersMap;
    	
    }

	public static void main(String args[]) throws  Exception
    {
        getContexts("C:\\Users\\ananta\\IdeaProjects\\FCIReader\\FCI_complete.xml");
    }

}
