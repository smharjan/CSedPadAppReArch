/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.log;

import dt.entities.database.Student;

/**
 *
 * @author Rajendra
 */
public class XmlLoggerTest {

    public static void main(String[] args) {

        Student student = new Student();
        student.setStudentId("xml-logger-test");

        XmlLogger logger = new XmlLogger(student);
        
        
    }
}
