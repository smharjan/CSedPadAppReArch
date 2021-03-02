/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dt.dialog;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author nobal
 */
public class DialogueParser {

    public void justParseDialogPolicyFile(String fileName) {
        //DP should be saved in session
        DialogPolicy dp = new DialogPolicy();
        try {
            dp.parsePolicyFile(fileName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void parsePolicyFileAndPrint(String fName) {
        try {
            //String fName = "C:\\Users\\nobal\\Dropbox\\GA\\DialogManager\\dt_dialog_stn_vr_1.xml";
            File file = new File(fName);
            JAXBContext jaxbContext = JAXBContext.newInstance(Dialog.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Dialog dm = (Dialog) jaxbUnmarshaller.unmarshal(file);
            List<State> sts = dm.getStates();
            List<Transition> transitions = dm.getTransitions();
            System.out.println("Total states:" + sts.size());
            for (State s : sts) {
                System.out.println("Name:" + s.getName());
                System.out.println("Desc:" + s.getDesc());
                if (s.getExitActions() != null) {
                    for (Action a : s.getExitActions()) {
                        System.out.println("Exit Desc:" + a.getValue());
                    }
                }
            }


            for (Transition t : transitions) {
                System.out.println("From:" + t.getFrom() + " " + "To:"
                        + t.getTo() + ", Actions: " + t.getActions().size()
                        + " Conditions:" + t.getConditions().size());
                List<Action> acts = t.getActions();
                for (Action a : acts) {
                    System.out.println(a.getValue());
                }
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        
        String dialogPolicyFile = "C:\\Users\\Rajendra\\Dropbox\\WithNobal\\DialogManager\\dt_dialog_stn_vr_1-RB-4.xml";
        dialogPolicyFile = "C:\\Users\\Rajendra\\Dropbox\\LabWork\\DialogueManager\\dt_dialog_stn_vr_1-RB-6.xml";
        DialogueParser parser = new DialogueParser();
        parser.justParseDialogPolicyFile(dialogPolicyFile);
    }
}