/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.entities.database;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 *
 * @author nobal
 */
@Entity
public class Demographics implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private Student student;
    private String gender;
    private String ethnicity;
    private String school;
    private int age;
    private String major;
    private String education;
    private String gpa;
    private String priorCourses;
    private String currentCourses;
    private String familiarareas;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "studentid")
    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getGpa() {
        return gpa;
    }

    public void setGpa(String gpa) {
        this.gpa = gpa;
    }

    public String getPriorCourses() {
        return priorCourses;
    }

    public void setPriorCourses(String priorCourses) {
        this.priorCourses = priorCourses;
    }

    public String getCurrentCourses() {
        return currentCourses;
    }

    public void setCurrentCourses(String currentCourses) {
        this.currentCourses = currentCourses;
    }

    public String getFamiliarareas() {
        return familiarareas;
    }

    public void setFamiliarareas(String familiarareas) {
        this.familiarareas = familiarareas;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Demographics)) {
            return false;
        }
        Demographics other = (Demographics) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "uom.entities.Demographics[ id=" + id + " ]";
    }
}
