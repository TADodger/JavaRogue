package rogue;

import java.io.Serializable;

/**
 *
 */
public class Option implements Serializable {
    private static final long serialVersionUID = -5790756134061027698L;

    boolean flush = true;
    boolean jump = false;
    boolean passgo = false;
    boolean noSkull = false;
    boolean askQuit = true;
    String nickName = "Rogue";
    String fruit = "slime_mold";

    Message msg;

    static String prompt[] = { "Flush typeahead during battle (\"flush\"): ", "Show position only at end of run (\"jump\"): ", "Follow turnings in passageways (\"passgo\"): ",
            "Don't print skull when killed (\"noskull\" or \"notombstone\"): ", "Ask player before saying 'Okay, bye-bye!' (\"askquit\"): ", "Name (\"name\"): ", "Fruit (\"fruit\"): " };

    Option() {
    }

//    private boolean bool_opt(String prompt, boolean b) {
//        char c;
//        do {
//            c = '\033';
//            msg.check_message();
//            String s = msg.get_input_line(prompt + '[' + (b ? "true" : "false") + ']', "", "", false, true);
//            if (s != null && s.length() > 0) {
//                if (s.indexOf('\033') >= 0) {
//                    break;
//                }
//                c = s.charAt(0);
//                if (c >= 'a') {
//                    c -= 'a' - 'A';
//                }
//            }
//        } while (c != '\033' && c != 'T' && c != 'F');
//        if (c == 'T') {
//            return true;
//        }
//        if (c == 'F') {
//            return false;
//        }
//        
//        return b;
//    }

    private String stringOpt(String prompt, String v) {
        msg.checkMessage();
        String s = msg.get_input_line(prompt + '[' + v + ']', "", "", false, true);
        if (s == null || s.length() == 0 || s.indexOf('\033') >= 0) {
            return v;
        }
        
        return s;
    }

    void editOpts(Man man) {
        msg = man.view.msg;
        // flush= bool_opt(prompt[0], flush);
        // jump= bool_opt(prompt[1], jump);
        // passgo= bool_opt(prompt[2], passgo);
        // no_skull= bool_opt(prompt[3], no_skull);
        // ask_quit= bool_opt(prompt[4], ask_quit);
        nickName = stringOpt(prompt[5], nickName);
        fruit = stringOpt(prompt[6], fruit);
    }
}
