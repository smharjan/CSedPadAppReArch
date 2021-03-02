/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dt.persistent;

import java.util.Hashtable;
import java.util.Map;

/**
 *  This class should be used by data manager only!!
 *  That's why, the class modifier is not public. However, it is accessible from
 *  within the package (so it is accessible from data manager).
 * @author Rajendra
 * Created on Jan 30, 2013, 7:06:23 PM 
 */
class DTCache {
    
    private static Map<String, Object> cache = new Hashtable<String, Object>();
    private static int MAX_ENTRIES = 1000;
    
    public static Object get(String key) {
        return cache.get(key);
    }

    /**
     *  This operation is synchronized. The Hashtable itself synchronizes and we
     *  don't need to guard them.
     * @param key
     * @param value
     * @return 
     */
    public static boolean put(String key, Object value) {
        if (cache.size() > MAX_ENTRIES) {
            // just returning false doesn't harm, as next time, the query to cache
            // will be missed and object has to be created. The cache user should
            // be careful that, there can be miss.
            return false;
        }
        cache.put(key, value);
        return true;
    }
    
    /*
     * Remove data item from cache. It synchronizes itself (Hashtable is synchronized 
     * data structure).
     */
    public static void remove(String key) {
        cache.remove(key);
    }
    
    /**
     * Clear the cache.
     */
    public static void clear() {
        cache.clear();
    }
}
