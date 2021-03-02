package memphis.deeptutor.servlets;

import java.io.File;

import com.sun.grizzly.websockets.WebSocketEngine;
import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellChecker;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import memphis.deeptutor.main.WebSocketHandler;
import memphis.deeptutor.main.WizardofOzServiceHandler;
import memphis.deeptutor.singleton.ConfigManager;

public class InitDTServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2454863708244770534L; 
	public static WizardofOzServiceHandler wozHandler = new WizardofOzServiceHandler();
	
	//public static WebSocketHandler app = new WebSocketHandler();
	public static SpellChecker spellCheck = null;

    @Override
    public void init(ServletConfig config) throws ServletException {
    	
    	if (ConfigManager.LoadProperties(config.getServletContext())) System.out.print("Web App properties ... loaded.");
    	else System.out.print("Web App properties... failed to load.");
    	
    	try {
    		SpellDictionary dictionary = new SpellDictionaryHashMap(new File(ConfigManager.GetDataPath()+ "dict\\english.0"), null);
    		spellCheck = new SpellChecker(dictionary);
    	}catch (Exception e) {
			e.printStackTrace();
			System.out.print("SpellChecker dictionary... failed to load.");
		}
        
    	System.out.print("Registering WebSocket WoZ Service Handler...");
    	//WebSocketEngine.getEngine().register(app);
    }
    
    @Override
    public void destroy()
    {
    	System.out.print("Unregistering WebSocket WoZ Service Handler...");
    	//WebSocketEngine.getEngine().unregister(app);
    }
}
