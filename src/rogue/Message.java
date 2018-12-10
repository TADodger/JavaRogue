package rogue;

import java.io.Serializable;

/**
 *
 */
public class Message implements Serializable {
    private static final long serialVersionUID = -3233497156787715360L;

    private View view;
    private static final int NMESSAGES = 5;
    private static final int MIN_ROW = 1;
    private static String msgs[] = new String[NMESSAGES];
    private int msgCol = 0;
    private int imsg = -1;
    private boolean msgCleared = true;
    private boolean rmsg = false;
    private static String more = " -more-";
    private boolean didInt;
    private boolean flush;

    /**
     * @param view
     */
    public Message(View view) {
        this.view = view;
        msgCol = 0;
        imsg = -1;
        for (int k = 0; k < NMESSAGES; k++) {
            msgs[k] = null;
        }
        msgCleared = true;
        rmsg = false;
    }

    /**
     * @param msg
     * @param intrpt
     */
    public void message(String msg, boolean intrpt) {
        if (view == null) {
            return;
        }

        if (intrpt) {
            view.self.interrupted = true;
            if (flush) {
                view.self.mdSlurp();
            }
        }
        if (!msgCleared) {
            view.addch(MIN_ROW - 1, msgCol, more);
            view.refresh();
            view.self.waitForAck();
            checkMessage();
        }
        if (!rmsg) {
            imsg = (imsg + 1) % NMESSAGES;
            msgs[imsg] = new String(msg);
        }
        view.addch(MIN_ROW - 1, 0, msg + ' ');
        view.refresh();
        msgCleared = false;
        msgCol = msg.length();


        if (didInt) {
            didInt = false;
            // onintr();
        }
    }

    /**
     * @param msg
     */
    public void message(String msg) {
        message(msg, false);
    }

    /**
     * @param c
     */
    public void remessage(int c) {
        if (imsg != -1 && view != null) {
            checkMessage();
            rmsg = true;
            while (c > imsg) {
                c -= NMESSAGES;
            }
            message(msgs[((imsg - c) % NMESSAGES)], false);
            rmsg = false;
            view.refresh();
        }
    }

    /**
     * 
     */
    public void checkMessage() {
        /* Erase the message line */
        if (msgCleared) {
            return;
        }
        String erase = new String();
        for (int i = 0; i < view.ncol; i++) {
            erase += ' ';
        }
        view.addch(MIN_ROW - 1, 0, erase);
        view.refresh();
        msgCleared = true;
    }

    /**
     * @param prompt
     * @param insert
     * @param ifCancelled
     * @param addBlank
     * @param doEcho
     * @return the input buffer
     */
    public String getInputLine(String prompt, String insert, String ifCancelled, boolean addBlank, boolean doEcho) {
        int ch;
        int i = 0, n = 0;
        String buffer = new String(insert);

        if (prompt != null) {
            message(prompt, false);
            n = prompt.length();
        }
        if (insert != null) {
            view.addch(0, n + 1, insert);
            i = insert.length();
        }
        view.refresh();
        while (((ch = view.self.rgetchar()) != '\r') && ch != '\n' && ch != '\033') {
            if (ch >= ' ' && ch <= '~') {
                if (ch != ' ' || i > 0) {
                    buffer = buffer + ((char) ch);
                    ++i;
                    if (doEcho) {
                        view.addch(MIN_ROW - 1, n + i, (char) ch);
                    }
                }
            }
            if (ch == '\b' && i > 0) {
                if (doEcho) {
                    view.addch(0, i + n, ' ');
                }
                i--;
                buffer = buffer.substring(0, i);
            }
            view.refresh();
        }
        checkMessage();
        buffer = addBlank ? buffer + ' ' : buffer.trim();

        if (ch == '\033' || i == 0 || (i == 1 && addBlank)) {
            if (ifCancelled != null) {
                message(ifCancelled, false);
            }
        
            return null;
        }

        return buffer;
    }

    /**
     * @return the message for which hand
     */
    public int leftOrRight() {
        int ch;
        do {
            message("left or right hand?");
            ch = view.self.rgetchar();
        } while (ch != '\033' && ch != 'l' && ch != 'r' && ch != '\n' && ch != '\r');
        if (ch != 'l' && ch != 'r') {
            ch = 0;
        }
        
        return ch;
    }

    /**
     * @param s
     * @return yes or no with the message
     */
    public boolean yesOrNo(String s) {
        int ch;
        do {
            message(s + " [y or n] ");
            ch = view.self.rgetchar();
        } while (ch != '\033' && ch != 'y' && ch != 'n' && ch != '\n' && ch != '\r');
        
        return ch == 'y';
    }

    /**
     * @param list
     * @param asking
     * @return ???
     */
    public int rightlist(String[] list, boolean asking) {
        int i;
        int n;
        int ch = '\033';
        String lastline = " --press space to continue-- ";
        int nmax = lastline.length();
        String[] keep = new String[view.nrow - 1];
        char chmin = 'z';
        char chmax = 'a';

        for (i = 0; i < list.length; i++) {
            if (list[i] != null) {
                n = list[i].length();
                if (nmax < n) {
                    nmax = n;
                }
            }
        }
        int col = view.ncol - nmax - 2;
        if (col < 0) {
            col = 0;
        }

        int row = 0;
        --i;
        for (int k = 0; k < view.nrow - 1; k++) { // Save
            keep[k] = new String(view.terminal[k], col, view.ncol - col);
        }

        for (int k = 0; k <= i; k++) {
            if (list[k] != null) {
                if (list[k].length() > 1) {
                    char c = list[k].charAt(1);
                    if (chmin > c) {
                        chmin = c;
                    }
                    if (chmax < c) {
                        chmax = c;
                    }
                }
                view.addch(row, col, list[k]);
                for (n = list[k].length(); n + col < view.ncol; n++) {
                    view.addch(row, n + col, ' ');
                }
                ++row;
                if (k == i || row >= view.nrow - 2) {
                    if (asking & k == i) {
                        lastline = " --Choose " + chmin + "-" + chmax + ", or ESC--";
                    }
                    view.addch(row, col, lastline);
                    view.refresh();
                    do {
                        ch = view.self.rgetchar();
                    } while (ch != ' ' && ch != '\033' && (!asking || !('a' <= ch && ch <= 'z')));
                    for (int j = 0; j <= row; j++) { // Restore
                        view.addch(j, col, keep[j]);
                    }
                    row = 0;
                    if (ch >= 'a') {
                        break;
                    }
                }
            }
        }
        for (int j = 0; j < view.nrow - 1; j++) { // Restore
            view.addch(j, col, keep[j]);
        }
        view.refresh();
        
        return ch;
    }

    /**
     * @return the chosen direction
     */
    public int keyboardDirection() {
        int ch = 0;
        int dir = -1;
        do {
            message(ch == 0 ? "direction?" : "direction (one of hjklyubn or ESC)?");
            ch = view.self.rgetchar();
            checkMessage();
        } while (-2 == (dir = Id.isDirection(ch)));

        return dir;
    }

    /*
     * static void pad(String s, int n){ int i;
     * 
     * for(i= strlen(s); i < n; i++){ addch(' '); } } static void save_screen(){
     * FILE *fp; int i, j; char buf[DCOLS+2]; boolean found_non_blank;
     * 
     * if((fp= fopen("rogue.screen", "w")) != NULL){ for(i= 0; i < DROWS; i++){
     * found_non_blank= 0; for(j= (DCOLS - 1); j >= 0; j--){ buf[j]= charat(i,
     * j); if(!found_non_blank){ if((buf[j] != ' ') || (j == 0)){ buf[j + ((j ==
     * 0) ? 0 : 1)]= 0; found_non_blank= 1; } } } fputs(buf, fp); putc('\n',
     * fp); } fclose(fp); } else { sound_bell(); } }
     * 
     */
    private static String[] bnr = new String[5];
    static {
        bnr[0] = " @@@ @@@@  @@@ @@@@ @@@@@@@@@@ @@@ @   @  @  @@@  @   @@    @   @@   @ @@@ @@@@  @@@ @@@@  @@@ @@@@@@   @@   @@   @@   @@   @@@@@@";
        bnr[1] = "@   @@   @@   @@   @@    @    @    @   @  @    @  @  @ @    @@ @@@@  @@   @@   @@   @@   @@      @  @   @@   @@   @ @ @  @ @    @ ";
        bnr[2] = "@@@@@@@@@ @    @   @@@@  @@@  @ @@@@@@@@  @    @  @@@  @    @ @ @@ @ @@   @@@@@ @   @@@@@  @@@   @  @   @ @ @ @ @ @  @    @    @  ";
        bnr[3] = "@   @@   @@   @@   @@    @    @   @@   @  @  @ @  @  @ @    @   @@  @@@   @@    @  @@@  @     @  @  @   @ @ @ @@ @@ @ @   @   @   ";
        bnr[4] = "@   @@@@@  @@@ @@@@ @@@@@@     @@@ @   @  @   @   @   @@@@@@@   @@   @ @@@ @     @@ @@   @ @@@   @   @@@   @  @   @@   @  @  @@@@@";
    }

    /**
     * @param row
     * @param col
     * @param s
     */
    public void banner(int row, int col, String s) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < s.length(); j++) {
                int c = (int) s.charAt(j);
                if (c >= 'a' && c <= 'z') {
                    c -= 'a';
                } else if (c >= 'A' && c <= 'Z') {
                    c -= 'A';
                } else {
                    continue;
                }
                try {
                    c *= 5;
                    view.addch(i + row, col + 6 * j, bnr[i].substring(c, c + 5));
                } catch (Exception e) {
                }
            }
        }
    }
}
