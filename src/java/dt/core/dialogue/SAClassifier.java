package dt.core.dialogue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

// Speech Act Classifier version 1.0.5
public class SAClassifier {

    private String text;

    public static enum SPEECHACT {
        Contribution,
        DummyClass,
        Greeting,
        Goodbye,
        MetaCognitive,
        MetaCommunicative,
        QuestionDefinitional,
        QuestionOther,
        QuestionVerification,
        YesNoAnswer,
        Formula
    };

    public SAClassifier() {
        this.text = "_NONE_";
    }

    public SAClassifier(String inputString) {
        this.text = inputString.trim();
    }

    public void setText(String t) {
        this.text = t.trim();
    }

    public SPEECHACT SAClassify() {
        String t = this.text + " ";

        if (t.matches("_NONE_")) {
            return SPEECHACT.DummyClass;
        }

        if (t.contains("+") || t.contains("- ") || t.contains("*") || t.contains("/") || t.contains("=")) {
            return SPEECHACT.Formula;
        }

        if (t.matches("(?i)(yes|no|yep|nope|nay|negative|true|positive|false|ya|not true)(!|\\.)?")) {
            return SPEECHACT.YesNoAnswer;
        }

        if (t.matches("(?i)(hello |heya |hey |hi |greetings |how (are|r) (u|you)( doin| doing)?).*")) {
            return SPEECHACT.Greeting;
        }

        if (t.matches("(?i)(goodbye |cya |(see|c) (you|u|ya)( later)?|bye bye|bye|good bye).*")) {
            return SPEECHACT.Goodbye;
        }

        if (t.matches(".*(?i)i (still )?(don't|dont|do not) (know|remember).*")
                || t.matches(".*(?i)(i'm|im|i am) not sure.*")
                || t.matches(".*(?i)i never knew.*")
                || (t.matches(".*(?i)(not|aint|ain't) sure.*") && !t.matches(".*(?i)but.*"))
                || (t.matches(".*(?i)too hard.*") && !t.matches(".*(?i)not.*"))
                || t.matches(".*(?i)you (to )?tell me.*")
                || t.matches(".*(?i)have (no|a|any) clue.*")
                || t.matches(".*(?i)(i |^)(still )?need .*help(ed)?.*")
                || t.matches(".*(?i)(please|plz) help.*")
                || t.matches(".*(?i)help (please|plz).*")
                || t.matches(".*(?i)(i'm|im|i am) lost.*")
                || t.matches(".*(?i)no idea .*")
                || t.matches(".*(?i)can't .*")
                || t.trim().equalsIgnoreCase("idk")
                || t.trim().equals("?")
                || t.trim().contains("good question")) {
            return SPEECHACT.MetaCognitive;
            /* Covers phrases like:
             dont know
             i do not know
             i never knew
             i am not sure
             i am afraid i don't remember
             this is too hard
             you tell me
             Have no clue, baby!
             I need help
             */
        }

        if ((t.matches(".*(?i)repeat.*") && !t.matches(".*(?i)(^|\\s)i .*"))
                || t.matches("(?i)pardon me.*")
                || t.matches("(?i)pardon\\?.*")
                || t.matches("(?i)say (that)? again.*")
                || t.matches(".*(?i)(don't|dont|do not|did not|didn't|didnt) (understand|comprehend).*")
                || t.matches(".*(?i)(haven't|havent|have not|'ve not) (understood|comprehended).*")
                || t.matches(".*(?i)what do (you|u) mean.*")
                || t.matches("(?i)(what|huh)\\?.*") // this goes before the text is compared to questions!
                || t.matches("(s|S)o\\s*(what)*\\?\\s*")
                || t.matches("(?i)(sorry|sry)\\?.*")
                || t.matches("\\s*\\?+\\s*")) {
            return SPEECHACT.MetaCommunicative;
            /* Covers phrases like:
             repeat
             i am afraid i don't understand
             pardon me
             say again
             didnt understand the question
             i have not understood, could you repeat
             what do u mean?
             ?? 
             so?
             */
        }
        if (t.matches("(?i)(what|who|where|how|when|why|is|was|were|are|does|doesn't|doesnt|do|can|could|should|would)\\s+(is|are|does|doesn't|doesnt|do|did|have).*")
                || t.matches(".*(?i)(what|who|where|how|when|why|is|was|were|are|does|did|didn't|didnt|doesn't|doesnt|do|can|could|should|would).*\\?.*")
                || t.matches("(?i)\\w+\\s*\\?+.*")) {
            if (t.matches(".*(?i)(definition|define).*")
                    || t.matches(".*(?i)(what does).*(mean).*")
                    || t.matches(".*(?i)(what is|whats).*")) {
                return SPEECHACT.QuestionDefinitional;
                /* Covers phrases like:
                 what does that mean
                 what is risk technology
                 what is a gui receive order?
                 What is newton's third law of motion?
                 What is scalor?	
                 whats newtons second law state?
                 */
            } else if (t.matches(".*(?i)(is this|are these|is the).*")
                    || t.matches(".*(?i)(can I|could I|should I).*")
                    || t.matches("(?i)(does|doesn't|doesnt|^did ).*")
                    || t.matches(".*(?i)(answer).*(wrong|right|correct|incorrect).*\\?")) {
                return SPEECHACT.QuestionVerification;
                /* Covers phrases like:
                 Is this not the same as intial velocities in the Y direction?
                 can I call you Quaid?
                 doesn't air resistance affect the two objects and make one fall faster than the other?
                 I'm sorry, was my last answer wrong?
                 */
            } else {
                return SPEECHACT.QuestionOther;
                /* Covers all other question formulations
                 */
            }
        }
        return SPEECHACT.Contribution;
    }

    public SPEECHACT SAClassify(String s) {
        this.text = s.trim();
        return this.SAClassify();
    }

    public static void main(String[] args) {
        System.out.println("Welcome to the Speech Act Classifier tester! Please input utterances. To exit, input \"stop\"");

        //Scanner input = new Scanner(System.in);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("d:\\javaprojects\\out.txt"));
            Scanner input = new Scanner(new File("d:\\javaprojects\\input.txt"));

            String str = input.nextLine();
            while (!(str.matches("(?i)stop"))) {
                SAClassifier myC = new SAClassifier(str);

                //System.out.println(myC.SAClassify());
                out.write(myC.SAClassify() + "\n");
                str = input.nextLine();
            }
            out.close();
            input.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
//end
}