/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.authoring.auth.dashboard;

import java.util.Map;
import dt.constants.Result;

/**
 *
 * @author suraj
 */
public class AdminWizardOfOZ {
    private Map<String, Object> session;
    
    public void setSession(Map<String, Object> map) {
        this.session = map;
    }
    
    public String execute() throws Exception {
    
        return Result.SUCCESS;
    }
}
