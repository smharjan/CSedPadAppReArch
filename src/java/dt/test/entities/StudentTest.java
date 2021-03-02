/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.test.entities;

import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import dt.entities.database.Evaluation;
import dt.entities.database.Student;
import dt.persistent.database.HibernateUtil;
import java.util.ArrayList;

/**
 *
 * @author nobal
 */
public class StudentTest {

    public static void main(String args[]) {

        //    Session s  = sf.openSession();
        String givenId = "user_test";
        // boolean result = insertTest(givenId);
        //System.out.println("The operation is:" + result);
        //Create a student 
        Student s = new Student();
        s.setStudentId(givenId);
        s.setPassword("welcome");

       insertStudent(s);
        //       Student result = getStudent(s);
        //       printStudent(result);
        //       result.getEvaluations().get(0).setExplanation("Updated explanation!");
        //       printStudent(result);
        //       updateStudent(result);
        //      result = getStudent(s);
        //       printStudent(result);

        //SessionFactory sf = HibernateUtil.getSessionFactory();
        //incrementalDemo(s);
    }

    public static void findDemo() {
        String givenId = "100";
        Student s = new Student();
        s.setStudentId(givenId);
        Student result = getStudent(s);
        if (result != null) {
            printStudent(s);
        } else {
            System.out.println("The student is not found!");
        }

    }

    public static void incrementalDemo1(Student s) {
        Student result = getStudent(s);
        if (result == null) {
            insertStudent(s);
            result = getStudent(s);
            System.out.println("Student is inserted!");
        }
        List<Evaluation> el = result.getEvaluations();
        if (el == null) {
            el = new ArrayList<Evaluation>();
            result.setEvaluations(el);
        }

        for (int i = 1; i < 5; i++) {
            Evaluation e = new Evaluation();
            e.setEvaluationId("fci" + i);
            e.setQuestionId("q" + i);
            e.setAnswer("a" + i);
            e.setContextId("c1" + i);
            e.setStudent(result);
            el.add(e);
            printStudent(result);
            System.out.println("Updating the student..");
            updateStudent(result);
            printStudent(result);
        }

    }

        public static void incrementalDemo(Student s) {
        Student result = getStudent(s);
        if (result == null) {
            insertStudent(s);
            result = getStudent(s);
            System.out.println("Student is inserted!");
        }
        List<Evaluation> el = result.getEvaluations();
        if (el == null) {
            el = new ArrayList<Evaluation>();
            result.setEvaluations(el);
        }
    }
    
    public static void insertDemo(Student s) {
        Student result = getStudent(s);
        if (result == null) {
            insertStudent(s);
            result = getStudent(s);
            System.out.println("Student is inserted!");
        }
        List<Evaluation> el = result.getEvaluations();
        if (el == null) {
            el = new ArrayList<Evaluation>();
        }
        result.setEvaluations(el);
        for (int i = 0; i < 10; i++) {
            Evaluation e = new Evaluation();
            e.setEvaluationId("fci" + i);
            e.setQuestionId("q" + i);
            e.setAnswer("a" + i);
            e.setContextId("c1" + i);
            e.setStudent(result);
            el.add(e);
        }
        System.out.println("Updating the student..");
        updateStudent(result);
    }
    

    public static boolean updateStudent(Student student) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        Transaction t = null;
        try {
            Session s = sf.getCurrentSession();
            t = s.beginTransaction();
            s.update(student);
            t.commit();
            return true;
        } catch (Exception ex) {
            System.err.println("Error while saving student-->" + ex);
            if (t != null) {
                t.rollback();  // rollback transaction on exception 
            }
        }
        return false;
    }

    public static boolean insertStudent(Student student) {
//        Student sOld = getStudent(student);
//        if (sOld != null) {
//            System.out.println("Student Exists, cannt be inserted!");
//            return false;
//        } else {

            SessionFactory sf = HibernateUtil.getSessionFactory();
            Transaction t = null;
            Session s = sf.getCurrentSession();

            try {
                t = s.beginTransaction();
                s.save(student);
                t.commit();
                return true;
            } catch (Exception ex) {
                System.err.println("Error while saving student-->" + ex);
                if (t != null) {
                    t.rollback();  // rollback transaction on exception 
                }
            }

       // }
        return false;

    }

    public static Student getStudent(Student student) {
        String givenId = student.getStudentId();
        Student s1 = null;
        SessionFactory sf = HibernateUtil.getSessionFactory();
        Transaction t = null;
        Session s = sf.getCurrentSession();
        //Try to insert the data
        try {
            t = s.beginTransaction();
            String hql = "FROM Student S WHERE S.id = '" + givenId + "'";
            Query query = s.createQuery(hql);
            List<Student> results = (List<Student>) query.list();
            s1 = results.get(0);
            t.commit();  // commit transaction 

        } catch (Exception ex) {
            System.err.println("Error -->" + ex.getMessage());
            if (t != null) {
                t.rollback();  // rollback transaction on exception 
            }
        }
        return s1;
    }

    public static boolean insertTest(String givenId) {
        Student sr = new Student();
        sr.setStudentId(givenId);
        Student sOld = getStudent(sr);
        if (sOld != null) {
            System.out.println("Student Exists, cannt be inserted!");
            return false;
        }
        Student s1 = new Student();
        s1.setStudentId(givenId);
        s1.setPassword("hello1");
        boolean result = false;

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Transaction t = null;
        //Try to insert the data
        try {
            Session s = sf.getCurrentSession();
            t = s.beginTransaction(); // start a new transaction
            s.persist(s1);
            t.commit();  // commit transaction 
            result = true;
        } catch (Exception ex) {
            System.err.println("Error -->" + ex.getMessage());
            if (t != null) {
                t.rollback();  // rollback transaction on exception 
            }
            result = false;
        }

        return result;
    }

    private static void printStudent(Student s) {
        StringBuffer sb = new StringBuffer();
        sb.append("Given Id:" + s.getStudentId() + "\n");
        sb.append("Password: " + s.getPassword() + "\n");
        sb.append("Explanation: " + s.getEvaluations().get(0).getExplanation() + "\n");
        if (s.getEvaluations() != null) {
            sb.append("Evaluation Count: " + s.getEvaluations().size() + "\n");
        } else {
            sb.append("Evaluation Count: null" + "\n");
        }

        System.out.println("Student Info:" + sb.toString());

    }
}
