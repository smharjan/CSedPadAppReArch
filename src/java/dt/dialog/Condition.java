/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.dialog;

import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author nobal
 */
public class Condition {

    private String oper;
    private String expr1;
    private String expr2;
    private boolean negated;

    public boolean isNegated() {
        return negated;
    }

    public void setNegated(boolean negated) {
        this.negated = negated;
    }

    /**
     * @return the oper
     */
    @XmlAttribute
    public String getOper() {
        return oper;
    }

    /**
     * @param oper the oper to set
     */
    public void setOper(String oper) {
        String op = oper.replaceFirst("~", "");
        if (oper.contains("!")) {
            this.negated = true;
            op = op.replace("!", "");
            this.oper = op;
            return;
        }
        this.oper = op;
    }

    /**
     * @return the expr1
     */
    @XmlAttribute
    public String getExpr1() {
        return expr1;
    }

    /**
     * @param expr1 the expr1 to set
     */
    public void setExpr1(String expr1) {
        this.expr1 = expr1;
    }

    /**
     * @return the expr2
     */
    @XmlAttribute
    public String getExpr2() {
        return expr2;
    }

    /**
     * @param expr2 the expr2 to set
     */
    public void setExpr2(String expr2) {
        this.expr2 = expr2;
    }
}
