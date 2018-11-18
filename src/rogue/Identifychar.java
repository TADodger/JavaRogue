package rogue;

/**
 *
 */
public class Identifychar {
    static char characterCmd[] = new char[48];
    static String characterDesc[] = new String[48];
    static {
        characterCmd[0] = '?'; characterDesc[0] = "?       prints help";
        characterCmd[1] = 'r'; characterDesc[1] = "r       read scroll";
        characterCmd[2] = '/'; characterDesc[2] = "/       identify object";
        characterCmd[3] = 'e'; characterDesc[3] = "e       eat food";
        characterCmd[4] = 'h'; characterDesc[4] = "h       left ";
        characterCmd[5] = 'w'; characterDesc[5] = "w       wield a weapon";
        characterCmd[6] = 'j'; characterDesc[6] = "j       down";
        characterCmd[7] = 'W'; characterDesc[7] = "W       wear armor";
        characterCmd[8] = 'k'; characterDesc[8] = "k       up";
        characterCmd[9] = 'T'; characterDesc[9] = "T       take armor off";
        characterCmd[10] = 'l'; characterDesc[10] = "l       right";
        characterCmd[11] = 'P'; characterDesc[11] = "P       put on ring";
        characterCmd[12] = 'y'; characterDesc[12] = "y       up & left";
        characterCmd[13] = 'R'; characterDesc[13] = "R       remove ring";
        characterCmd[14] = 'u'; characterDesc[14] = "u       up & right";
        characterCmd[15] = 'd'; characterDesc[15] = "d       drop object";
        characterCmd[16] = 'b'; characterDesc[16] = "b       down & left";
        characterCmd[17] = 'c'; characterDesc[17] = "c       call object";
        characterCmd[18] = 'n'; characterDesc[18] = "n       down & right";
        characterCmd[19] = '\0'; characterDesc[19] = "<SHIFT><dir>: run that way";
        characterCmd[20] = ')'; characterDesc[20] = ")       print current weapon";
        characterCmd[21] = '\0'; characterDesc[21] = "<CTRL><dir>: run till adjacent";
        characterCmd[22] = ']'; characterDesc[22] = "]       print current armor";
        characterCmd[23] = 'f'; characterDesc[23] = "f<dir>  fight till death or near death";
        characterCmd[24] = '='; characterDesc[24] = "=       print current rings";
        characterCmd[25] = 't'; characterDesc[25] = "t<dir>  throw something";
        characterCmd[26] = '\001'; characterDesc[26] = "^A      print Hp-raise average";
        characterCmd[27] = 'm'; characterDesc[27] = "m<dir>  move onto without picking up";
        characterCmd[28] = 'z'; characterDesc[28] = "z<dir>  zap a wand in a direction";
        characterCmd[29] = 'o'; characterDesc[29] = "o       examine/set options";
        characterCmd[30] = '^'; characterDesc[30] = "^<dir>  identify trap type";
        characterCmd[31] = '\022'; characterDesc[31] = "^R      redraw screen";
        // c_cmd[32]='&'; c_desc[32]= "& save screen into 'rogue.screen'";
        characterCmd[33] = 's'; characterDesc[33] = "s       search for trap/secret door";
        characterCmd[34] = '\020'; characterDesc[34] = "^P      repeat last message";
        characterCmd[35] = '>'; characterDesc[35] = ">       go down a staircase";
        characterCmd[36] = '\033'; characterDesc[36] = "^[      cancel command";
        characterCmd[37] = '<'; characterDesc[37] = "<       go up a staircase";
        // c_cmd[38]='S'; c_desc[38]= "S save game";
        characterCmd[39] = '.'; characterDesc[39] = ".       rest for a turn";
        characterCmd[40] = 'Q'; characterDesc[40] = "Q       quit";
        characterCmd[41] = ','; characterDesc[41] = ",       pick something up";
        // c_cmd[42]='!'; c_desc[42]= "! shell escape";
        characterCmd[43] = 'i'; characterDesc[43] = "i       inventory";
        characterCmd[44] = 'F'; characterDesc[44] = "F<dir>  fight till either of you dies";
        // c_cmd[45]='I'; c_desc[45]= "I inventory single item";
        characterCmd[46] = 'v'; characterDesc[46] = "v       print version number";
        characterCmd[47] = 'q'; characterDesc[47] = "q       quaff potion";
        characterDesc[19] = "";
        characterDesc[21] = "";
        characterDesc[26] = "";
        characterDesc[38] = "";
        characterDesc[42] = "";
        characterDesc[45] = "";
    }

    static void cmdsList(char ch, Message msg) {
        if (ch == '*' || ch == '?') {
            msg.rightlist(characterDesc, false);
        } else {
            String[] desc = new String[1];
            desc[0] = "No such command: " + ch;
            for (int k = 0; k < characterCmd.length; k++) {
                if (ch == characterCmd[k]) {
                    desc[0] = characterDesc[k];
                    break;
                }
            }
            msg.rightlist(desc, false);
        }
    }
}
