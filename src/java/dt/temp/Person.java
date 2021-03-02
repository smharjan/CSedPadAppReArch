/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dt.temp;

/**
 *
 * @author Rajendra
 * Created on Jan 28, 2013, 3:16:56 PM 
 */
public class Person {
 private String firstname;
  private String lastname;
  private PhoneNumber phone;
  private PhoneNumber fax;

    Person(String first, String last) {
        firstname = first;
        lastname = last;
    }

    void setPhone(PhoneNumber phoneNumber) {
        phone = phoneNumber;
    }

    void setFax(PhoneNumber phoneNumber) {
        fax = phoneNumber;
    }
  
}
