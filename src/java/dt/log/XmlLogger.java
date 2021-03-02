/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.log;

import com.sun.grizzly.util.buf.TimeStamp;
import dt.config.ConfigManager;
import dt.entities.database.Student;
import dt.log.LogRecordAttribute.AttributeType;
import java.io.File;


/**
 *
 * @author Rajendra August 13, 2014
 */
public class XmlLogger {

    public enum SOURCE {

        TUTOR, STUDENT, DM, SYSTEM
    }
    String logFile = null;
    private Student student = null;

    public XmlLogger(Student student) {
        String logFilePath = ConfigManager.getConfigPath() + student.getStudentId() + ".xml";   //TODO: log folder??
        File file = new File(logFilePath);

        boolean isNewLogFile = false;
        //create log file if it doesn't exists.
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception exp) {
                System.err.println("Failed to create log file at :" + logFilePath);
                exp.printStackTrace();
            }
            isNewLogFile = true;
        }
        this.student = student;

        //log general information.
        logSystemInfo(isNewLogFile);
        logConfigurationDetails(isNewLogFile);
    }

    /**
     * Common function.. needed by all students. Logger should be clean and
     * general but the couple of following functions are placed here for ease.
     * We can move these functions somewhere and call logger.
     */
    public final void logSystemInfo(boolean isNewLogFile) {
        writeSectionStart("SystemInfo", "System information section");


        writeSectionEnd("SystemInfo");
    }

    public final void logConfigurationDetails(boolean isNewLogFile) {
        writeSectionStart("Configuration", "Application configuration");


        writeSectionEnd("Configuration");
    }

    public void writeSectionStart(String name, String label) {
        StringBuilder sb = new StringBuilder();
        sb.append("<section name=\"" + name + "\" label=\"" + label + "\" position =\"start\">\n");
        //write to log..

    }

    public void writeSectionEnd(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("<section name=\"" + name + "\"  position =\"end\">\n");
    }

    public void writeRecord(SOURCE src, String label, LogRecord record) {
        String timeStamp;
        TimeStamp ts = new TimeStamp();
        timeStamp = ts.toString();

        //
        StringBuilder sb = new StringBuilder();
        sb.append("<record src=\"" + src.toString() + "\" timestamp=\"" + timeStamp + "\" label =\"" + label + "\">\n");

        //
        for (LogRecordAttribute att : record.getAttributeList()) {
            if (att.getType() == AttributeType.String) {
                sb.append("<attribute type=\"string\" name=\"" + att.getName() + "\" value =\"" + att.getStringValue() + "\">\n");
            } else if (att.getType() == AttributeType.List) {
                sb.append("<attribute type=\"list\" name=\"" + att.getName() + "\" value =\"" + att.getListValue() + "\">\n");
            }
        }
        sb.append("</record>");

        //TODO: append in the file.

    }
    
    /**
     * Append log in the log file.
     * @param sb 
     */
    private void appendToLogFile(StringBuilder sb) {
        
        
    }
}
