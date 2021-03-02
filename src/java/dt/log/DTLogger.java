package dt.log;

import dt.config.ConfigManager;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DTLogger {

    public enum Actor {

        STUDENT, TUTOR, SYSTEM, NONE
    };

    public enum Level {

        ONE, TWO, THREE
    };
    String htmlHeader = null;
    String htmlFooter = "\n</body>\n\n</html>";
    StringBuilder allLogs = null;
    String logName;

    /**
     * Rajendra: This has been slightly changed. Now, the log name is created
     * here.. no need to construct log name everywhere it is used (it was
     * annoying).
     *
     * @param _studentId
     */
    public DTLogger(String _studentId) {
        DateFormat df = new SimpleDateFormat("MMddyy");
        allLogs = new StringBuilder();

        logName = _studentId + "-" + df.format(Calendar.getInstance().getTime());

        htmlHeader = "<html>\n\n<head><title>Log</title></head>"
                + "\n<body>"
                + //"\n\t<b>Session</b> : 23452<br/>"+
                "\n\t<b>StudentID-Date(MMDDYY)-IP_ADDRESS</b> : " + logName + "<br/>"
                + //"\n\t<b>Task</b> : demo1<br/>"+
                "---------------------------------------<br/>";
    }

//	public DTLogger(String _logName)
//	{
//		allLogs = new StringBuilder();
//		
//		logName = _logName;
//
//		htmlHeader="<html>\n\n<head><title>Log</title></head>" +
//		"\n<body>"+
//		//"\n\t<b>Session</b> : 23452<br/>"+
//		"\n\t<b>StudentID-Date(MMDDYY)-IP_ADDRESS</b> : " + logName + "<br/>"+
//		//"\n\t<b>Task</b> : demo1<br/>"+
//		"---------------------------------------<br/>";
//	}        
    public void log(Actor actor, Level level, String message) {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
        System.out.println((level == Level.TWO ? "\t" : (level == Level.THREE ? "\t\t" : "")) + (actor == Actor.NONE ? "" : actor + ": ") + timeStamp + " " + message);
        String level1 = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
        String level2 = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
        String level3 = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
        switch (actor) {
            case STUDENT:
                allLogs.append("\n\t<b>" + actor + "</b>: <font color=\"#FF0080\">" + timeStamp + " " + message + "</font><br/>");
                break;
            case TUTOR:
                allLogs.append("\n\t<b>" + "DEEP" + actor + "</b>: <font color=\"#0000FF\">" + timeStamp + " " + message + "</font><br/>");
                break;
            case SYSTEM:
                allLogs.append("\n\t<b>" + actor + "</b>: <font color=\"#008000\">" + timeStamp + " " + message + "</font><br/>");
                break;
            case NONE:
                if (level == Level.ONE) {
                    allLogs.append("\n\t" + level1 + " " + timeStamp + " " + message + "<br/>");
                } else if (level == Level.TWO) {
                    allLogs.append("\n\t" + level2 + " " + timeStamp + " " + message + "<br/>");
                } else {
                    allLogs.append("\n\t" + level3 + " " + timeStamp + " " + message + "<br/>");
                }
        }
    }

    public void saveLogInHTML() {

        String filename = ConfigManager.getLogPath() + "Log-" + logName + ".html";
        File f = new File(filename);

        try {
            RandomAccessFile raf = null;
            if (f.exists()) {
                raf = new RandomAccessFile(filename, "rw");
                raf.seek(raf.length() - htmlFooter.length());
            } else {
                raf = new RandomAccessFile(filename, "rw");
                raf.writeBytes(htmlHeader);
            }
            raf.writeBytes(allLogs.toString());
            raf.writeBytes(htmlFooter);
            raf.close();

            //rajendra: 2/3/2013, clear the all logs., reuse the dt logger object..
            allLogs.setLength(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        //DTLogger.log(Actor.TUTOR, Level.ONE, "Hey start answering question");
        //DTLogger.log(Actor.STUDENT, Level.ONE, "Hey start answering question");
        //DTLogger.log(Actor.SYSTEM, Level.ONE, "Hey start answering question");
        //DTLogger.log(Actor.NONE, Level.ONE, "Hey start answering question");
        //DTLogger.log(Actor.NONE, Level.TWO, "Hey start answering question");
        //DTLogger.log(Actor.NONE, Level.THREE, "Hey start answering question");
        //DTLogger.log(Actor.NONE, Level.ONE, "Hey start answering question");
        //DTLogger.saveLogInHTML("src/log.html");
    }
}
