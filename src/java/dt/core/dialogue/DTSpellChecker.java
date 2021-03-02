/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.core.dialogue;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;
import com.swabunga.spell.event.WordTokenizer;
import dt.config.ConfigManager;
import java.io.File;

/**
 * Wrapper for spell checker.. singleton.
 *
 * @author Rajendra
 */
public class DTSpellChecker {

    private static DTSpellChecker instance = null;
    private static SpellChecker spellChecker = null;

    /**
     * Should be locked.. but now it is assumed that there will be no coincidence 
     *  that multiple users try to instantiate it at once.
     * @return 
     */
    public static DTSpellChecker getInstance() {
        if (instance == null) {
            try {
                instance = new DTSpellChecker();
            } catch (Exception e) {
                return null;
            }
        }
        return instance;
    }

    /**
     * Initialize spell checker.
     */
    private DTSpellChecker() {
        System.out.println("Loading dictionary.. for spell checker");
    	try {
    	SpellDictionary dictionary = new SpellDictionaryHashMap(new File(ConfigManager.getDictionaryFile()), null);
    	//SpellDictionary dictionary = new SpellDictionaryHashMap(new File("D:/DTNew/dict/english.0"), null);
    	spellChecker = new SpellChecker(dictionary);
    	}catch (Exception e) {
	e.printStackTrace();
	System.out.print("SpellChecker dictionary... failed to load.");
     }        
    }
    
    /**
     * Spell check.
     * @param text 
     */
    public static String spellCheck(String text) {
        return spellChecker.autoSpelling(new StringWordTokenizer(text));
    }
    
}