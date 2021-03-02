/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.persistent.database;

import dt.test.entities.*;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import dt.entities.database.Student;

/**
 *
 * @author nobal
 */
public class Students {

    public static boolean updateStudent(Student student) {
        SessionFactory sf = HibernateUtil.getSessionFactory();
        Transaction t = null;
        Session s = null;
        try {
            s = sf.openSession();
            t = s.beginTransaction();
            s.update(student);
            s.flush();
            t.commit();
            return true;
        } catch (Exception ex) {
            System.err.println("Error while saving student-->" + ex);
            if (t != null) {
                t.rollback();  // rollback transaction on exception 
            }
        } finally {
            if (s != null) {
                s.close();
            }
        }
        return false;
    }

    public static Student getStudent(String studentId) {
        Student s = new Student();
        s.setStudentId(studentId);
        return getStudent(s);
    }

    /**
     * 
     * @param studentId
     * @param password
     * @return 
     */
    public static Student getStudent(String studentId, String password) {
        Student s = new Student();
        s.setStudentId(studentId);
        s.setPassword(password);
        Student result = getStudent(s);
        if (result != null) {
            // if password is not null, try matching password.
            if (result.getPassword() != null) {
                if (result.getPassword().equals(s.getPassword())) {
                    return result;
                }
                else
                {
                    return null;
                }
            }
            return result;
        }
        return null;
    }

    /**
     * Returns student given its id
     *
     * @param student
     * @return
     */
    public static Student getStudent(Student student) {
        String studentId = student.getStudentId();
        Student s1 = null;
        SessionFactory sf = HibernateUtil.getSessionFactory();
        
        Transaction t = null;
        Session s = sf.getCurrentSession();
        //Try to insert the data
        try {
            t = s.beginTransaction();
            String hql = "FROM Student S WHERE S.studentId = '" + studentId + "'";
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

    /*
     * Insert's a demo user..
     * 
     */
    public static boolean insertDemoStudent(Student student) {
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
        return false;
    }
    public static boolean insertNewStudent(Student student) {
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
        return false;
    }
}
