/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.log;

import dt.entities.database.Student;
import dt.config.ConfigManager;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Rajendra
 */
public class NEWDTLoggerBackup {
    
    /**
     * Make html and save.. to corresponding student's log file.
     *
     * @param s
     * @param info
     */
    public static void logThisInfo(Student s, String info) {
        String logName = ConfigManager.getLogName(s);
        Date date = new Date();
        Logger logger = new Logger(logName);
        logger.log(Logger.Actor.NONE, Logger.Level.ONE, info + " AT: " + date.toString());
        logger.saveLogInHTML();
    }

    /**
     * Make html and save.. to corresponding student's log file.
     *
     * @param s
     * @param info
     */
    public static void logThisInfo(Student s, StringBuilder info) {
        String logName = ConfigManager.getLogName(s);
        Date date = new Date();
        Logger logger = new Logger(logName);
        logger.log(Logger.Actor.NONE, Logger.Level.ONE, info.toString() + " AT: " + date.toString());
        logger.saveLogInHTML();
    }

    /**
     * Make html and save.. to corresponding student's log file.
     *
     * @param s
     * @param info
     */
    public static void logThisInfo(Student s, List<String> lines) {
        String logName = ConfigManager.getLogName(s);
        Date date = new Date();
        Logger logger = new Logger(logName);
        for (String line : lines) {
            if (line == null || line.length() == 0) {
                continue;
            }
            logger.log(Logger.Actor.NONE, Logger.Level.ONE, line + " AT: " + date.toString());
        }
        logger.saveLogInHTML();
    }

}
