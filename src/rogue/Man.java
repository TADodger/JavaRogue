package rogue;

import java.io.*;
import java.util.Date;
import java.awt.*;


/**
 *
 */
public class Man extends Persona implements Serializable {
    private static final long serialVersionUID = 6850118418880209819L;
    
    private static final int MOVED = 0;
    private static final int MOVE_FAILED = -1;
    private static final int STOPPED_ON_SOMETHING = -2;
    
    private static final int HUNGRY = 300;
    private static final int WEAK = 150;
    private static final int FAINT = 20;
    private static final int STARVE = 0;
    
    private static final int MAX_EXP_LEVEL = 21;
    private static final int MAX_EXP = 10000001;
    private static final int MAX_ARMOR = 99;
    private static final int MAX_HP = 999;
    private static final int MAX_STRENGTH = 99;
    
    private static final int MAX_PACK_COUNT = 24;

    private static final char[] WALLYS = { ' ', '|', '-', '+', '#', '%', '^' };
    /**
     * Set seen to 1(stairs-floor) 2(walls) 0(other)
     * Also 4=just seen 8=last seen
     * Bits 4-6 are the wally code above
     */
    public char[][] seen;

    /** */
    public Rogue rogue;
    /** */
    public View view;
    /** */
    public Option option;
    /** */
    public ItemList<Item> pack = null;
    /** */
    public int expPoints = 0; // Experience points
    /** */
    public int movesLeft = 1250; // Food counter
    private int mMoves = 0; // General move counter

    /** */
    public boolean sustainStrength = false;
    /** */
    public boolean detectMonster = false;
    private boolean passgo = false;
    private boolean rTeleport = false;
    private boolean jump = false;
    /** */
    public boolean seeInvisible;
    /** */
    public static boolean gameOver = false;
    /** */
    public static boolean savedGame = false;
    /** */
    public static String gameOverMessage = "";
    /** */
    public static int justLoaded = 0;

    private int regeneration = 0;
    /** */
    public boolean rSeeInvisible = false;
    private boolean maintainArmor = false;

    private static final int R_TELE_PERCENT = 8;

    private int autoSearch = 0; // Number of times to auto-search each turn

    /** */
    public String newLevelMessage;
    /** */
    public String hungerStr = "";
    private boolean trapDoor;

    /**
     * @param self
     * @param view
     */
    public Man(Rogue self, View view) {
        super(self);
        mt = Monster.MONSTER_TABLE[Monster.MONSTERS - 1];
        itemCharacter = (char) (mt.ichar | U_ROGUE);
        this.rogue = self;
        this.option = new Option();
        this.view = view;
        hpMax = hpCurrent = mt.hpCurrent;
        strMax = strCurrent = 16;
        exp = 1;
    }

    private void rest(int count) {
        rogue.interrupted = false;
        do {
            if (rogue.interrupted) {
                break;
            }
            regMove();
        } while (--count > 0);
    }

    private void turnPassage(int dir, boolean fast) {
        int crow = row, ccol = col, turns = 0;
        int ndir = 0;

        if ((dir != 'h') && level.canTurn(crow, ccol + 1)) {
            turns++;
            ndir = 'l';
        }
        if ((dir != 'l') && level.canTurn(crow, ccol - 1)) {
            turns++;
            ndir = 'h';
        }
        if ((dir != 'k') && level.canTurn(crow + 1, ccol)) {
            turns++;
            ndir = 'j';
        }
        if ((dir != 'j') && level.canTurn(crow - 1, ccol)) {
            turns++;
            ndir = 'k';
        }
        if (turns == 1) {
            playMove(ndir - (fast ? 32 : 96), 1);
        }
    }

    private boolean search(int n, boolean isAuto) {
        int found = 0, shown = 0;
        for (int r = row - 1; r <= row + 1; r++) {
            if (r >= MIN_ROW && r < level.numRow - 1) {
                for (int c = col - 1; c <= col + 1; c++) {
                    if (c >= 0 && c < level.numCol) {
                        if (0 != (level.map[r][c] & HIDDEN)) {
                            ++found;
                        }
                    }
                }
            }
        }
        do {
            for (int r = row - 1; r <= row + 1; r++) {
                if (r >= MIN_ROW && r < level.numRow - 1) {
                    for (int c = col - 1; c <= col + 1; c++) {
                        if (c >= 0 && c < level.numCol) {
                            if (0 != (level.map[r][c] & HIDDEN) && rogue.rand.percent(17 + exp + ringExp)) {
                                level.map[r][c] &= ~HIDDEN;
                                if (blind == 0 && (r != row || c != col)) {
                                    view.mark(r, c);
                                }
                                shown++;
                                if (0 != (level.map[r][c] & TRAP)) {
                                    Trap trap = level.levelTraps.itemAt(r, c);
                                    if (trap != null && trap.kind < Trap.name.length) {
                                        tell(Trap.name[trap.kind], true);
                                    } else {
                                        System.out.println("Err in search=flag=" + r + " " + c);
                                    }
                                }
                                if ((shown == found && found > 0) || rogue.interrupted) {
                                    break;
                                }
                                /* A search is half a move */
                                if (!isAuto && 0 == (n & 1)) {
                                    regMove();
                                }
                            }
                        }
                    }
                }
            }
        } while (--n > 0);

        return shown > 0;
    }

    private int playMove(int ch, int count) {
        System.out.println((char) ch + " " + ch + " " + count);
        switch (ch) {
            case '.':
            case '-':
                rest(count);
                break;
            case 's':
                if (search(count, false)) {
                    vizset();
                    moveSeen();
                }
                break;
            case 'i':
                pack.inventory(Id.ALL_TOYS, view.msg, false);
                break;
            case 'f':
                // fight(0);
                break;
            case 'F':
                // fight(1);
                break;
            case Event.HOME:
            case Event.END:
            case Event.PGUP:
            case Event.PGDN:
                if (ch == Event.HOME) {
                    ch = 'y';
                }
                if (ch == Event.END) {
                    ch = 'b';
                }
                if (ch == Event.PGUP) {
                    ch = 'u';
                }
                if (ch == Event.PGDN) {
                    ch = 'n';
                }
            case Event.DOWN:
            case Event.UP:
            case Event.RIGHT:
            case Event.LEFT:
                if (ch == Event.DOWN) {
                    ch = 'j';
                }
                if (ch == Event.UP) {
                    ch = 'k';
                }
                if (ch == Event.RIGHT) {
                    ch = 'l';
                }
                if (ch == Event.LEFT) {
                    ch = 'h';
                }
            case 'h':
            case 'j':
            case 'k':
            case 'l':
            case 'y':
            case 'u':
            case 'n':
            case 'b':
                System.out.println("play_move - 1");
                oneMoveRogue(ch, true);
                System.out.println("play_move - 2");
                break;
            case 'H':
            case 'J':
            case 'K':
            case 'L':
            case 'B':
            case 'Y':
            case 'U':
            case 'N':
                while (!rogue.interrupted && oneMoveRogue((ch + 32), true) == MOVED) {
                    if (!rogue.interrupted && passgo && 0 != (level.map[row][col] & TUNNEL)) {
                        turnPassage(ch + 32, true);
                    }
                }
                break;
            
            case '\010':
            case '\012':
            case '\013':
            case '\014':
            case '\031':
            case '\025':
            case '\016':
            case '\002':
                int rowCheck = 0;
                int colCheck = 0;
                int move = 0;
                do {
                    rowCheck = row;
                    colCheck = col;
                    move = oneMoveRogue(ch + 96, true);
                    if (move == MOVE_FAILED || move == STOPPED_ON_SOMETHING || rogue.interrupted)
                        break;
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                    }
                } while (!nextToSomething(rowCheck, colCheck));
                if (!rogue.interrupted && passgo && move == MOVE_FAILED && 0 != (level.map[row][col] & TUNNEL))
                    turnPassage(ch + 96, false);
                break;
             
            case 'e':
                eat();
                break;
            case 'q':
                quaff();
                break;
            case 'r':
                readScroll();
                break;
            case 'm':
                move_onto();
                break;
            case ',':
                kickIntoPack();
                break;
            case 'd':
                drop();
                break;
            case 'P':
                putOnRing();
                break;
            case 'R':
                removeRing();
                break;
            case 'P' - '@': /* Print old messages */
                do {
                    view.msg.remessage(count++);
                    ch = rogue.rgetchar();
                } while (ch == 'P' - '@');
                view.msg.checkMessage();
                count = playMove(ch, 0);
                break;
            case 'W' - '@':
                tell((wizard = !wizard) ? "Welcome, wizard!" : "not wizard anymore");
                Id.wizardIdentify();
                break;
            case 'R' - '@':
                view.repaint(30);
                break;
            case '>':
                if (wizard) {
                    return -1;
                }
                if (0 != (level.map[row][col] & STAIRS)) {
                    if (levitate != 0) {
                        tell("you're floating in the air!");
                    } else {
                        return -1;
                    }
                }
                return 0;
            case '<':
                if (!wizard) {
                    if (0 == (level.map[row][col] & STAIRS)) {
                        tell("I see no way up");
                        return 0;
                    }
                    if (!hasAmulet()) {
                        tell("your way is magically blocked");
                        return 0;
                    }
                }
                newLevelMessage = "you feel a wrenching sensation in your gut";
                if (level.currentLevel == 1) {
                    win();
                } else {
                    level.currentLevel -= 2;
                }
                
                return -1;
            case ')':
                tell(weapon == null ? "not wielding anything" : pack.singleInv(weapon.itemCharacter));
                break;
            case ']':
                tell(armor == null ? "not wearing anything" : pack.singleInv(armor.itemCharacter));
                break;
            case '=':
                if (leftRing == null && rightRing == null) {
                    tell("not wearing any rings");
                }
                if (leftRing != null) {
                    tell(pack.singleInv(leftRing.itemCharacter));
                }
                if (rightRing != null) {
                    tell(pack.singleInv(rightRing.itemCharacter));
                }
                break;
            case '^':
                idTrap();
                break;
            case '/':
                Id.idType(this);
                break;
            case '?':
                idCom();
                break;
            case '!':
                // do_shell();
                break;
            case 'o':
                option.editOpts(this);
                break;
            case 'I':
                // single_inv(0);
                break;
            case 'T':
                takeOff();
                break;
            case 'W':
                wear();
                break;
            case 'w':
                wield();
                break;
            case 'c':
                callIt();
                break;
            case 'z':
                zapp();
                break;
            case 't':
                throwMissile();
                break;
            case 'v':
                tell("java rogue clone", false);
                break;
            case 'Q':
                if (option.askQuit) {
                    if (!view.msg.yesOrNo("Really quit?"))
                        break;
                }
                killedBy(null, Monster.QUIT);
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                view.refresh();
                do {
                    if (count < 100)
                        count = 10 * count + ch - '0';
                    ch = rogue.rgetchar();
                } while ('0' <= ch && ch <= '9');
                if (ch != '\033') {
                    count = playMove(ch, count);
                }
                break;
            case ' ':
                break;
            case '\001':
                // show_average_hp();
                break;
            case 'S':
                saveGame();
                break;
            default:
                if (!wizard)
                    ch = 0;
                switch (ch) {
                    case '@':
                        System.out.println(this.toString());
                        for (int i = row - 1; i <= row + 1; i++) {
                            for (int j = col - 1; j <= col + 1; j++) {
                                try {
                                    System.out.print(Integer.toString(07000000 + level.map[i][j], 8) + view.buffer[i][j] + " ");
                                } catch (Exception e) {
                                    System.out.print("-------? ");
                                }
                            }
                            System.out.println("");
                        }
                        tell("At row " + row + ", column " + col);
                        break;
                    case 'B' - '@':
                        level.levelToys.inventory(Id.ALL_TOYS, view.msg, false);
                        break;
                    case 'D' - '@':
                        System.out.println("Monsters:");
                        for (int i = 0; i < level.levelMonsters.size(); i++)
                            System.out.println(level.levelMonsters.get(i));
                        break;
                    case 'F' - '@':
                        level.currentLevel += 19;
                        return -1; // plummet
                    case 'S' - '@':
                        level.drawMagicMap(this);
                        break;
                    case 'E' - '@':
                        level.showTraps(this);
                        break;
                    case 'O' - '@':
                        level.showToys(this);
                        break;
                    case 'C' - '@':
                        cToyForWizard();
                        break;
                    case 'X' - '@':
                        monsterForWizard();
                        break;
                    case 'Q' - '@':
                        level.showMonsters(this);
                        break;
                    case 'T' - '@':
                        tele();
                        break;
                    case 'W' - '@':
                        level.wanderer();
                        break;
                    case 'U' - '@':
                        level.unhide();
                        break;
                    default:
                        tell("unknown_command");
                        break;
                }
        }
        return count;
    }

    /**
     * 
     */
    public void play_level() {
        int ch;
        int count = 0;
        initSeen();
        view.mark(row, col);
        do {
            System.out.println("processing command");
            try {
                System.out.println("here - 1");
                rogue.interrupted = false;
                if (hitMessage.length() > 1) {
                    tell(hitMessage);
                    hitMessage = "";
                }
                System.out.println("here - 2");
                if (trapDoor) {
                    trapDoor = false;
                    break;
                }
                System.out.println("here - 3");
                showmap();
                System.out.println("here - 4");
                view.refresh();
                System.out.println("here - 5");

                ch = rogue.rgetchar();
                System.out.println("here - 6");
                if (!gameOver) {
                    view.msg.checkMessage();
                }
                System.out.println("here - 7");
                count = playMove(ch, 0);
                System.out.println("here - 8");
            } catch (Exception e) {
                if (e.getMessage() != null) {
                    System.out.println(e.getMessage());
                }
                e.printStackTrace();
            }
            System.gc();
            System.out.println(count);
        } while (count >= 0 && !gameOver);
    }

    private char showrc(int r, int c) { // What man sees at this position
        char ch = 0;

        if (r == row && c == col) {
            return itemCharacter;
        }

        if (blind == 0 && canSee(r, c)) {
            int mask = level.map[r][c];
            if (0 != (mask & DARK)) {
                if (r < row - 1 || r > row + 1 || c < col - 1 || c > col + 1) {
                    return ' ';
                }
            }
            if (0 != (mask & MONSTER)) {
                Monster monster = level.levelMonsters.itemAt(r, c);
                ch = (char) (monster != null ? monster.gmc(this) : '$');
            } else if (0 != (mask & TOY)) {
                if (halluc == 0) {
                    Toy toy = level.levelToys.itemAt(r, c);
                    if (toy == null) {
                        System.out.println("See no toy at " + new Rowcol(r, c));
                    } else {
                        ch = toy.itemCharacter;
                    }
                } else {
                    ch = (char) Id.getRandomObjectCharacter(rogue.rand);
                }
            } else {
                ch = level.getChar(r, c);
            }
        } else if (detectMonster && 0 != (level.map[r][c] & MONSTER) && blind == 0) {
            Monster monster = (Monster) level.levelMonsters.itemAt(r, c);
            ch = (char) (monster != null ? monster.gmc(this) : '$');
            if (rogue.rand.percent(30)) {
                detectMonster = false;
            }
        } else {
            ch = WALLYS[seen[r][c] >> 4];
        }
        // Error if blind and toy in tunnel or on door--see blank instead of
        // tunnel floor/door
        return ch;
    }

    private void showmap() {
        Rowcol pt;
        while ((pt = view.getmark()) != null) {
            if (pt.col == 0 && pt.row == 0) {
                for (int r = Level.MIN_ROW; r < level.numRow - 1; r++) {
                    for (int c = 0; c < level.numCol; c++) {
                        view.addch(r, c, showrc(r, c));
                    }
                }
                break;
            } else {
                char ch = showrc(pt.row, pt.col);
                view.addch(pt.row, pt.col, ch);
            }
        }
    }

    private boolean checkHunger(boolean messageOnly) {
        int i, n;
        boolean fainted = false;

        if (movesLeft == HUNGRY) {
            hungerStr = "hungry";
            tell("you feel " + hungerStr);
            printStat();
        }
        if (movesLeft == WEAK) {
            hungerStr = "weak  ";
            tell("you feel " + hungerStr, true);
            printStat();
        }
        if (movesLeft <= FAINT) {
            if (movesLeft == FAINT) {
                hungerStr = "faint ";
                tell("you " + hungerStr, true);
                printStat();
            }
            n = rogue.rand.get(FAINT - movesLeft);
            if (n > 0) {
                fainted = true;
                if (rogue.rand.percent(40)) {
                    movesLeft++;
                }
                tell("you faint", true);
                for (i = 0; i < n; i++) {
                    if (rogue.rand.coin()) {
                        level.moveMonsters(this);
                    }
                }
                tell("you can move again", true);
            }
        }
        if (messageOnly) {
            return fainted;
        }
        if (movesLeft <= STARVE) {
            killedBy(null, Monster.STARVATION);
            return false;
        }
        switch (eRings) {
            case -1:
                movesLeft -= (movesLeft % 2);
                break;
            case 0:
                movesLeft--;
                break;
            case 1:
                movesLeft--;
                checkHunger(true);
                movesLeft -= (movesLeft % 2);
                break;
            case 2:
                movesLeft--;
                checkHunger(true);
                movesLeft--;
                break;
        }
        
        return fainted;
    }

    protected boolean regMove() {
        boolean fainted = false;

        if ((movesLeft <= HUNGRY) || level.currentLevel >= level.maxLevel) {
            fainted = checkHunger(false);
        }
        level.moveMonsters(this);

        if (++mMoves >= 120) {
            mMoves = 0;
            level.wanderer();
        }
        super.regMove();
        heal();

        if (autoSearch > 0) {
            search(autoSearch, true);
        }
        
        return fainted;
    }

    private void move_onto() {
        int ch;
        if (-2 == Id.isDirection(ch = rogue.rgetchar())) {
            tell("direction? ");
            ch = rogue.rgetchar();
        }
        view.msg.checkMessage();
        if (ch != '\033') {
            oneMoveRogue(ch, false);
        }
    }

    private int oneMoveRogue(int dirch, boolean pickup) {
        System.out.println("oneMoveRogue - 1");
        if (confused != 0) {
            dirch = movConfused();
        }
        System.out.println("oneMoveRogue - 2");
        int d = Id.isDirection(dirch);
        System.out.println("oneMoveRogue - 3");
        Rowcol pto = level.getDirRowCol(d, row, col, true);
        System.out.println("oneMoveRogue - 4");

        if (!level.canMove(row, col, pto.row, pto.col)) {
            return MOVE_FAILED;
        }

        if (beingHeld || bearTrap > 0) {
            if (0 == (level.map[pto.row][pto.col] & MONSTER)) {
                if (beingHeld) {
                    tell("you are being held", true);
                } else {
                    tell("you are still stuck in the bear trap");
                    regMove();
                }

                return MOVE_FAILED;
            }
        }
        if (rTeleport) {
            if (rogue.rand.percent(R_TELE_PERCENT)) {
                tele();
                
                return STOPPED_ON_SOMETHING;
            }
        }
        if (0 != (level.map[pto.row][pto.col] & MONSTER)) {
            Monster monster = level.levelMonsters.itemAt(pto.row, pto.col);
            if (monster != null) {
                rogueHit(monster, false);
            }
            regMove();
            
            return MOVE_FAILED;
        }
        if (0 != (level.map[pto.row][pto.col] & DOOR) && 0 != (level.map[row][col] & TUNNEL)) {
            level.wakeRoom(this, true, pto.row, pto.col);
        } else if (0 != (level.map[pto.row][pto.col] & TUNNEL) && 0 != (level.map[row][col] & DOOR)) {
            level.wakeRoom(this, false, row, col);
        }
        //////////////////////////////////////////////////
        if (blind == 0) { // Basic tunnel view
            for (int r = row - 1; r <= row + 1; r++) {
                for (int c = col - 1; c <= col + 1; c++) {
                    //// if(0!=(level.map[r][c]&TUNNEL) &&
                    //// 0==(level.map[r][c]&HIDDEN))
                    view.mark(r, c);
                }
            }
        }
        placeAt(pto.row, pto.col, MAN); // Note--sets row,col to pto
        if (blind == 0) { // Basic tunnel view
            for (int r = row - 1; r <= row + 1; r++) {
                for (int c = col - 1; c <= col + 1; c++) {
                    //// if(0!=(level.map[r][c]&TUNNEL) &&
                    //// 0==(level.map[r][c]&HIDDEN))
                    view.mark(r, c);
                }
            }
        }
        vizset();
        moveSeen();
        if (!jump) {
            showmap();
            view.refresh();
        }
        //////////////////////////////////////////////////
        Toy toy = null;
        boolean sos = false; // Stopped on something
        if (0 != (level.map[row][col] & TOY)) {
            if (levitate > 0 && pickup) {
                return STOPPED_ON_SOMETHING;
            }
            if (pickup && 0 == levitate) {
                toy = pickUp();
            }
            if (toy == null) {
                toy = level.levelToys.itemAt(row, col);
                if (toy != null) {
                    tell("moved onto " + toy.getDesc());
                }
            } else if (toy.itemCharacter == 1) { // Not a dusted scroll
                return STOPPED_ON_SOMETHING;
            }
            sos = true;
        }
        if (0 != (level.map[row][col] & (Level.DOOR | Level.STAIRS | Level.TRAP))) {
            if (levitate == 0 && 0 != (level.map[row][col] & Level.TRAP)) {
                trapPlayer();
            }
            sos = true;
        }
        return (regMove() /* fainted from hunger */
                || sos /* already on something */
                || confused != 0) ? STOPPED_ON_SOMETHING : MOVED;
    }

    private boolean nextToSomething(int drow, int dcol) {
        int pass_count = 0;
        int mapObject;

        if (confused != 0) {
            return true;
        }
        if (blind > 0) {
            return false;
        }
        int iEnd = (row < (level.numRow - 2)) ? 1 : 0;
        int jEnd = (col < (level.numCol - 1)) ? 1 : 0;

        for (int i = row > MIN_ROW ? -1 : 0; i <= iEnd; i++) {
            for (int j = col > 0 ? -1 : 0; j <= jEnd; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                if (row + i == drow && col + j == dcol) {
                    continue;
                }
                int rowCheck = row + i;
                int colCheck = col + j;
                mapObject = level.map[rowCheck][colCheck];
                if (0 != (mapObject & HIDDEN)) {
                    continue;
                }

                /*
                 * If the rogue used to be right, up, left, down, or right of
                 * r,c, and now isn't, then don't stop
                 */
                if (0 != (mapObject & (MONSTER | TOY | STAIRS))) {
                    if ((rowCheck == drow || colCheck == dcol) && !(rowCheck == row || colCheck == col)) {
                        continue;
                    }
                    
                    return true;
                }
                if (0 != (mapObject & TRAP)) {
                    if ((rowCheck == drow || colCheck == dcol) && !(rowCheck == row || colCheck == col)) {
                        continue;
                    }
                    
                    return true;
                }
                if ((i - j == 1 || i - j == -1) && 0 != (mapObject & TUNNEL)) {
                    if (++pass_count > 1) {
                        return true;
                    }
                }
                if (0 != (mapObject & DOOR) && (i == 0 || j == 0)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    protected Trap trapPlayer() { // Call with the trap list
        level.map[row][col] &= ~HIDDEN;
        if (rogue.rand.percent(exp + ringExp)) {
            tell("the trap failed", true);
            return null;
        }
        Trap trap = level.levelTraps.itemAt(row, col);
        if (trap == null) {
            return null;
        }
        switch (trap.kind) {
            case Trap.BEAR_TRAP:
                tell(trap.trapMessage(this), true);
                bearTrap = rogue.rand.get(4, 7);
                trap = null;
                break;
            case Trap.TRAP_DOOR:
                trapDoor = true;
                newLevelMessage = trap.trapMessage(this);
                break;
            case Trap.TELE_TRAP:
                view.mark(row, col);
                tele();
                break;
            case Trap.DART_TRAP:
                tell(trap.trapMessage(this), true);
                hpCurrent -= Id.getDamage("1d6", rogue.rand);
                if (hpCurrent <= 0) {
                    hpCurrent = 0;
                }
                if (!sustainStrength && rogue.rand.percent(40) && strCurrent >= 3) {
                    strCurrent--;
                }
                printStat();
                if (hpCurrent <= 0) {
                    killedBy(null, Monster.POISON_DART);
                }
                break;
            case Trap.SLEEPING_GAS_TRAP:
                tell(trap.trapMessage(this), true);
                takeANap();
                break;
            case Trap.RUST_TRAP:
                tell(trap.trapMessage(this), true);
                rust(null);
                break;
        }
        return trap;
    }

    protected void takeANap() {
        int i = rogue.rand.get(2, 5);
        rogue.mdSleep(1000);
        while (--i >= 0) {
            level.moveMonsters(this);
        }
        rogue.mdSleep(1000);
        tell("you can move again");
    }

    /*
     * Level: 99 Gold: 999999 Hp: 999(999) Str: 99(99) Arm: 99 Exp: 21/10000000
     * Hungry 0 5 1 5 2 5 3 5 4 5 5 5 6 5 7 5
     */
    private String statString() {
        if (hpMax > MAX_HP) {
            hpCurrent -= hpMax - MAX_HP;
            hpMax = MAX_HP;
        }
        if (strMax > MAX_STRENGTH) {
            strCurrent -= strMax - MAX_STRENGTH;
            strMax = MAX_STRENGTH;
        }
        int armorclass = 0;
        if (armor != null) {
            if (armor.dEnchant > MAX_ARMOR) {
                armor.dEnchant = MAX_ARMOR;
            }
            armorclass = armor.getArmorClass();
        }
        if (expPoints > MAX_EXP) {
            expPoints = MAX_EXP;
        }
        if (exp > MAX_EXP_LEVEL) {
            exp = MAX_EXP_LEVEL;
        }
        
        return "Level: " + String.format("%2d", level.currentLevel) + " Gold: " + String.format("%6d", gold) + " Hp: " + String.format("%3d", hpCurrent) 
                + '(' + String.format("%3d", hpMax) + ") Str: " + String.format("%2d", strCurrent) + '(' + String.format("%2d", strMax, 2)
                + ") Arm: " + String.format("%2d", armorclass, 2) + " Exp: " + String.format("%2d", exp, 2) + '/' + String.format("%8d", expPoints, 8) + " " + hungerStr;
    }

    protected void printStat() {
        view.addch(level.numRow - 1, 0, statString());
        /// view.refresh();
    }

    private void drop() {
        Toy obj;
        int ch;

        if (0 != (level.map[row][col] & (TOY | STAIRS | TRAP))) {
            tell("there's already something there");
            
            return;
        }
        if (null == pack) {
            tell("you have nothing to drop");
            
            return;
        }
        ch = packLetter("drop what?", Id.ALL_TOYS);
        if (ch == '\033') {
            return;
        }
        obj = (Toy) pack.getLetterToy(ch);
        if (obj == null) {
            tell("no such item.");
            
            return;
        }
        if (obj.kind == Id.SCARE_MONSTER) {
            obj.pickedUp = true;
        }
        obj.drop();
    }

//    private int is_pack_letter(int c) {
//        switch (c) {
//            case '?':
//                return Id.SCROLL;
//            case '!':
//                return Id.POTION;
//            case ':':
//                return Id.FOOD;
//            case ')':
//                return Id.WEAPON;
//            case ']':
//                return Id.ARMOR;
//            case '/':
//                return Id.WAND;
//            case '=':
//                return Id.RING;
//            case ',':
//                return Id.AMULET;
//            default:
//                break;
//        }
//        return 0;
//    }

    private int packLetter(String prompt, int mask) {
        int ch;

        if (!pack.maskPack(mask)) {
            tell("nothing appropriate");

            return '\033';
        }
        tell(prompt);
        ch = rogue.rgetchar();
        while ((ch < 'a' || ch > 'z') && ch != '\033') {
            int m = mask;
            if (ch == '*' || m == 0) {
                m = Id.ALL_TOYS;
            }
            /// view.msg.check_message();
            ch = pack.inventory(m, view.msg, true);
            // System.out.println("In pack_letter " + ch);
        }
        view.msg.checkMessage();

        return ch;
    }

    private void takeOff() {
        if (armor != null) {
            if (armor.isCursed) {
                tell(Toy.curseMessage);
            } else {
                level.moveAquatars(this);
                Toy obj = armor;
                unwear();
                tell("was wearing " + obj.getDesc());
                printStat();
                regMove();
            }
        } else {
            tell("not wearing any");
        }
    }

    private void wear() {
        if (armor != null) {
            tell("your already wearing some");
            
            return;
        }
        int ch = packLetter("wear what?", Id.ARMOR);
        if (ch == '\033') {
            return;
        }
        Toy obj = (Toy) pack.getLetterToy(ch);
        if (null == obj) {
            tell("no such item.");
            
            return;
        }
        if (0 == (obj.kind & Id.ARMOR)) {
            tell("you can't wear that");
            
            return;
        }
        obj.identified = true;
        tell("wearing " + obj.getDesc());
        doWear(obj);
        printStat();
        regMove();
    }

    private void wield() {
        if (weapon != null && weapon.isCursed) {
            tell(Toy.curseMessage);
            
            return;
        }
        int ch = packLetter("wield what?", Id.WEAPON);
        if (ch == '\033') {
            return;
        }
        Toy obj = (Toy) pack.getLetterToy(ch);
        if (obj == null) {
            if (ch == '-' && weapon != null) {
                unwield();
            } else {
                tell("No such item.");
            }
            
            return;
        }
        if (0 != (obj.kind & (Id.ARMOR | Id.RING))) {
            tell("you can't wield " + (0 != (obj.kind & Id.ARMOR) ? "armor" : "rings"));
            
            return;
        }
        if (0 != (obj.inUseFlags & Id.BEING_WIELDED)) {
            tell("in use");
        } else {
            unwield();
            tell("wielding " + obj.getDesc());
            doWield(obj);
            regMove();
        }
    }

    /**
     * @param mask
     * @param prompt
     * @param fail
     * @return The Toy that was found
     */
    public Toy find(int mask, String prompt, String fail) {
        int ch = packLetter(prompt, mask);
        if (ch == '\033') {
            return null;
        }
        view.msg.checkMessage();
        Toy toy = (Toy) pack.getLetterToy(ch);
        if (toy == null) {
            tell("no such item.");
            return null;
        }
        if (0 == (toy.kind & mask)) {
            tell(fail);
            return null;
        }
        
        return toy;
    }

    private void callIt() {
        Toy obj = find(Id.SCROLL | Id.POTION | Id.WAND | Id.RING, "call what?", "surely you already know what that's called");
        if (obj == null) {
            return;
        }
        Id[] idTable = Id.getIdTable(obj);
        String buf = view.msg.getInputLine("call it:", "", idTable[obj.kind & 255].title, true, true);
        if (buf != null) {
            idTable[obj.kind & 255].idStatus = Id.CALLED;
            idTable[obj.kind & 255].title = buf;
        }
    }

    private void kickIntoPack() {
        if (0 == (level.map[row][col] & TOY)) {
            tell("nothing here");
        } else {
            Toy obj = pickUp();
            if (obj != null && obj.itemCharacter != 1) { // Not a dusted scroll
                regMove();
            }
        }
    }

    private void monsterForWizard() {
        tell("type of monster? ");
        int ch = rogue.rgetchar();
        view.msg.checkMessage();
        if (ch == '\033' || ch < 'A' || ch > 'Z') {
            return;
        }
        Monster m = new Monster(level, ch - 'A');
        int r = row, c = col - 2;
        if (0 != (level.map[r][c] & (Level.FLOOR | Level.TUNNEL))) {
            m.putMonsterAt(row, col - 2);
        } else {
            tell("cannot put monster there!");
        }
    }

    private void cToyForWizard() {
        if (pack.size() >= MAX_PACK_COUNT) {
            tell("pack full");
            
            return;
        }
        tell("type of object? ");
        int ch = rogue.rgetchar();
        view.msg.checkMessage();
        if (ch == '\033') {
            return;
        }
        Toy obj = level.wizardToy(this, ch);
        if (obj != null) {
            tell("Wizard got " + obj.getDesc());
            obj.addToPack(this);
        } else {
            tell("Wizard failed");
        }
    }

    private Toy pickUp() {
        Toy toy = level.levelToys.itemAt(row, col);
        if (toy == null) {
            tell("pick_up(): inconsistent", true);
            
            return null;
        }
        if (levitate > 0) {
            tell("you're floating in the air!");
            
            return null;
        }
        if (pack.size() >= MAX_PACK_COUNT && toy.kind != Id.GOLD) {
            tell("pack too full", true);
            
            return null;
        }
        level.map[row][col] &= ~TOY;

        // Pick up from a tunnel or door shows the substrate
        if (0 != (level.map[row][col] & TUNNEL)) {
            seen[row][col] |= wallcode('#');
        }
        if (0 != (level.map[row][col] & DOOR)) {
            seen[row][col] |= wallcode('+');
        }
        level.levelToys.remove(toy);

        if (toy.kind == Id.SCARE_MONSTER && toy.pickedUp) {
            tell("the scroll turns to dust as you pick it up");
            if (Id.idScrolls[Id.SCARE_MONSTER & 255].idStatus == Id.UNIDENTIFIED) {
                Id.idScrolls[Id.SCARE_MONSTER & 255].idStatus = Id.IDENTIFIED;
            }
            toy.itemCharacter = 1; // Flag the dusted scroll

            return toy;
        }
        if (toy.kind == Id.GOLD) {
            gold += toy.quantity;
            tell(toy.getDesc(), true);
            printStat();
        } else {
            toy = toy.addToPack(this);
            if (toy != null) {
                toy.pickedUp = true;
                tell(toy.getDesc() + " (" + ((char) toy.itemCharacter) + ")", true);
            }
        }
        return toy;
    }

    /**
     * 
     */
    public static final int[] LEVEL_POINTS = { 10, 20, 40, 80, 160, 320, 640, 1300, 2600, 5200, 10000, 20000, 40000, 80000, 160000, 320000, 1000000, 3333333, 6666666, MAX_EXP, 99900000 };

    private int hpRaise() {
        return wizard ? 10 : rogue.rand.get(3, 10);
    }

    private static int getExpLevel(int e) {
        int i;
        
        for (i = 0; i < LEVEL_POINTS.length; i++) {
            if (LEVEL_POINTS[i] > e) {
                break;
            }
        }
        
        return i + 1;
    }

    /**
     * @param points
     * @param promotion
     */
    public void add_exp(int points, boolean promotion) {
        expPoints += points;
        if (expPoints >= LEVEL_POINTS[exp - 1]) {
            int new_exp = getExpLevel(expPoints);
            if (expPoints > MAX_EXP) {
                expPoints = MAX_EXP + 1;
            }
            for (int i = exp + 1; i <= new_exp; i++) {
                tell("welcome to level " + i);
                if (promotion) {
                    int hp = hpRaise();
                    hpCurrent += hp;
                    hpMax += hp;
                }
                exp = i;
                printStat();
            }
        } else {
            printStat();
        }
    }

    private void eat() {
        Toy obj = find(Id.FOOD, "eat what?", "you can't eat that");
        if (obj != null) {
            obj.eatenby();
            regMove();
        }
    }

    private void quaff() {
        Potion obj = (Potion) find(Id.POTION, "quaff what?", "you can't drink that");
        if (obj != null) {
            obj.quaffby();
            regMove();
        }
    }

    private void readScroll() {
        Scroll obj = (Scroll) find(Id.SCROLL, "read what?", "you can't read that");
        if (obj != null) {
            obj.readby();
            if (obj.kind != Id.SLEEP) {
                regMove();
            }
        }
    }

    private void putOnRing() {
        if (leftRing != null && rightRing != null) {
            tell("wearing two rings already");
            
            return;
        }
        Toy obj = (Toy) find(Id.RING, "put on what?", "that's not a ring");
        if (obj == leftRing || obj == rightRing) {
            tell("that ring is already being worn");

            return;
        }
        if (obj != null) {
            int ch = 'r';
            if (leftRing == null) {
                ch = 'l';
                if (rightRing == null) {
                    ch = view.msg.leftOrRight();
                    if (ch == 0) {
                        view.msg.checkMessage();
                        return;
                    }
                }
            }
            if (ch == 'l') {
                obj.inUseFlags |= Id.ON_LEFT_HAND;
                leftRing = obj;
            } else {
                obj.inUseFlags |= Id.ON_RIGHT_HAND;
                rightRing = obj;
            }
            ringStats(true);
            view.msg.checkMessage();
            tell(obj.getDesc());
            regMove();
        }
    }

    private void removeRing() {
        Toy obj = rightRing;
        if (leftRing != null && rightRing != null) {
            int ch = view.msg.leftOrRight();
            if (ch == 0) {
                view.msg.checkMessage();
                return;
            }
            if (ch == 'l') {
                obj = leftRing;
            }
        } else if (leftRing != null) {
            obj = leftRing;
        } else if (rightRing == null) {
            tell("there's no ring on that hand");
            return;
        }
        if (obj.isCursed) {
            tell(Toy.curseMessage);
        } else {
            obj.unPutOn();
            tell("removed " + obj.getDesc());
        }
    }

    protected void tele() {
        level.putPlayer(this);
        vizset();
        moveSeen();
        beingHeld = false;
        bearTrap = 0;
    }

    protected void ringStats(boolean pr) {
        stealthy = 0;
        rRings = 0;
        eRings = 0;
        ringExp = 0;
        rTeleport = false;
        sustainStrength = true;
        addStrength = 0;
        regeneration = 0;
        rSeeInvisible = false;
        maintainArmor = false;
        autoSearch = 0;

        for (int i = 0; i < 2; i++) {
            Toy ring = null;
            if (i == 0 && leftRing != null) {
                ring = leftRing;
            } else if (i == 1 && rightRing != null) {
                ring = rightRing;
            } else {
                continue;
            }
            rRings++;
            eRings++;
            switch (ring.kind) {
                case Id.STEALTH:
                    stealthy++;
                    break;
                case Id.R_TELEPORT:
                    rTeleport = true;
                    break;
                case Id.REGENERATION:
                    regeneration++;
                    break;
                case Id.SLOW_DIGEST:
                    eRings -= 2;
                    break;
                case Id.ADD_STRENGTH:
                    addStrength += ring.klass;
                    break;
                case Id.SUSTAIN_STRENGTH:
                    sustainStrength = true;
                    break;
                case Id.DEXTERITY:
                    ringExp += ring.klass;
                    break;
                case Id.ADORNMENT:
                    break;
                case Id.R_SEE_INVISIBLE:
                    rSeeInvisible = true;
                    break;
                case Id.MAINTAIN_ARMOR:
                    maintainArmor = true;
                    break;
                case Id.SEARCHING:
                    autoSearch += 2;
                    break;
            }
        }
        if (pr) {
            printStat();
            view.markall(); // relight
        }
    }

    private static final int[] HEALTAB = { 2, 20, 18, 17, 14, 13, 10, 9, 8, 7, 4, 3, 2 };
    private int cHeal = 0;
    private boolean bHealAlt = false;

    private void heal() {
        if (hpCurrent == hpMax) {
            cHeal = 0;
            return;
        }
        int n = exp < HEALTAB.length ? HEALTAB[exp] : 2;
        if (++cHeal >= n) {
            hpCurrent++;
            if (bHealAlt = !bHealAlt) {
                hpCurrent++;
            }
            cHeal = 0;
            hpCurrent += regeneration;
            if (hpCurrent > hpMax) {
                hpCurrent = hpMax;
            }
            printStat();
        }
    }

    private void zapp() {
        int d = view.msg.keyboardDirection();
        if (d < 0) {
            return;
        }
        Toy wand = find(Id.WAND, "zap with what?", "you can't zap with that");
        if (wand == null) {
            return;
        }
        if (wand.klass <= 0) {
            tell("nothing happens");
        } else {
            wand.klass--;
            if ((wand.kind == Id.COLD) || (wand.kind == Id.FIRE)) {
                level.bounce(wand, d, row, col, 0);
                view.markall(); // relight
            } else {
                Monster monster = level.getZappedMonster(d, row, col);
                if (monster != null) {
                    if (wand.kind == Id.DRAIN_LIFE) {
                        level.wandDrainLife(this, monster);
                    } else if (monster != null) {
                        monster.wakeUp();
                        monster.sConMon(this);
                        monster.zap_monster(this, wand.kind);
                        view.markall(); // relight
                    }
                }
            }
        }
        regMove();
    }

    private void idCom() {
        view.msg.checkMessage();
        tell("Character you want help for(* for all):");
        int ch = rogue.mdGetchar();
        view.msg.checkMessage();
        Identifychar.cmdsList((char) ch, view.msg);
        view.markall(); // relight
    }

    /**
     * @return true of the pack has an amulet
     */
    public boolean hasAmulet() {
        return pack.maskPack(Id.AMULET);
    }

    /**
     * 
     */
    public void playerInit() {
        pack = new ItemList<Item>(MAX_PACK_COUNT);
        level.getFood(true).addToPack(this);
        level.getFood(true).addToPack(this);
        level.getFood(true).addToPack(this);

        Toy obj = level.getRandomArmor();
        obj.kind = Id.RINGMAIL;
        obj.klass = (Id.RINGMAIL & 255) + 2;
        obj.isProtected = false;
        obj.dEnchant = 1;
        obj.identified = true;
        obj.addToPack(this);
        doWear(obj);

        obj = level.getRandomWeapon(Id.MACE);
        obj.hitEnchant = obj.dEnchant = 1;
        obj.identified = true;
        obj.addToPack(this);
        obj.isCursed = false;
        doWield(obj);

        obj = level.getRandomWeapon(Id.BOW);
        obj.damage = "1d2";
        obj.hitEnchant = 1;
        obj.dEnchant = 0;
        obj.identified = true;
        obj.isCursed = false;
        obj.addToPack(this);

        obj = level.getRandomWeapon(Id.ARROW);
        obj.quantity = rogue.rand.get(25, 35);
        obj.hitEnchant = 0;
        obj.dEnchant = 0;
        obj.identified = true;
        obj.isCursed = false;
        obj.addToPack(this);

        for (int i = 1; i < 10; i++) {
            obj = level.getRandomScroll();
            obj.hitEnchant = 0;
            obj.dEnchant = 0;
            obj.identified = false;
            obj.isCursed = false;
            obj.addToPack(this);
        }
    }

    private void idTrap() {
        view.msg.checkMessage();
        tell("direction? ");
        int d = Id.isDirection(rogue.rgetchar());
        view.msg.checkMessage();
        if (d < 0) {
            return;
        }
        Rowcol pt = level.getDirRowCol(d, row, col, false);
        int r = pt.row;
        int c = pt.col;
        Trap trap;
        if (0 != (level.map[r][c] & TRAP) && 0 == (level.map[r][c] & HIDDEN) && (trap = level.levelTraps.itemAt(r, c)) != null) {
            tell(Trap.name[trap.kind]);
        } else {
            tell("no trap there");
        }
    }

    private void throwMissile() {
        int dir = view.msg.keyboardDirection();
        if (dir < 0) {
            return;
        }
        int wch = packLetter("throw what?", Id.WEAPON);
        if (wch == '\033') {
            return;
        }
        view.msg.checkMessage();
        Toy missile = (Toy) pack.getLetterToy(wch);
        if (missile == null) {
            tell("no such item.");
            
            return;
        }
        if (0 != (missile.inUseFlags & Id.BEING_USED) && missile.isCursed) {
            tell(Toy.curseMessage);
            
            return;
        }
        missile.owner = this;
        missile.thrownby(dir);
        regMove();
    }

    /**
     * @param checkRow
     * @param checkCol
     * @return true if the Rogue is within 1 of the provide location
     */
    public boolean rogueIsAround(int checkRow, int checkCol) {
        checkRow -= row;
        checkCol -= col;

        return checkRow >= -1 && checkRow <= 1 && checkCol >= -1 && checkCol <= 1;
    }

    /**
     * @param monster
     * @param forceHit
     */
    public void rogueHit(Monster monster, boolean forceHit) {
        if (monster.check_imitator()) {
            if (blind == 0) {
                view.msg.checkMessage();
                tell("wait, that's a " + monster.name() + '!');
            }

            return;
        }
        int hitChance = 100;
        if (!forceHit) {
            hitChance = getHitChance(weapon);
        }
        if (wizard) {
            hitChance *= 2;
        }
        if (!rogue.rand.percent(hitChance)) {
            if (null == ihate) {
                hitMessage += who("miss", "misses") + " ";
            }
        } else {
            int dmg = getWeaponDamage(weapon);
            if (wizard) {
                dmg *= 3;
            }
            if (conMon) {
                monster.sConMon(this);
            }
            if (monster.damage(this, dmg, 0)) { /* still alive? */
                if (null == ihate) {
                    hitMessage += who("hit") + " ";
                }
            }
        }
        monster.checkGoldSeeker();
        monster.wakeUp();
    }

    protected boolean damage(Persona monster, int d, int other) {
        if (d >= hpCurrent) {
            hpCurrent = 0;
            printStat();
            killedBy(monster, other);
            
            return true;
        }
        if (d > 0) {
            rogue.flashadd(row, col, U_RED);
            hpCurrent -= d;
            printStat();
            if (hpCurrent <= hpMax / 8) {
                monster.gloat(this);
            }
        }
        
        return false;
    }

    /**
     * @param monster
     */
    public void rust(Monster monster) {
        if (null == armor || armor.getArmorClass() <= 1 || armor.kind == Id.LEATHER) {
            return;
        }
        if (armor.isProtected || maintainArmor) {
            if (monster != null && 0 == (monster.mFlags & Monster.RUST_VANISHED)) {
                tell("the rust vanishes instantly");
                monster.mFlags |= Monster.RUST_VANISHED;
            }
        } else {
            armor.dEnchant--;
            tell("your armor weakens");
            printStat();
        }
    }

    /**
     * @param monster
     */
    public void freeze(Monster monster) {
        int freezePercent = 99;
        int i, n;

        if (rogue.rand.percent(12)) {
            return;
        }
        freezePercent -= strCurrent + strCurrent / 2;
        freezePercent -= (exp + ringExp) * 4;
        if (armor != null) {
            freezePercent -= armor.getArmorClass() * 5;
        }
        freezePercent -= hpMax / 3;

        if (freezePercent > 10) {
            monster.mFlags |= Monster.FREEZING_ROGUE;
            tell("you are frozen", true);

            n = rogue.rand.get(4, 8);
            for (i = 0; i < n; i++) {
                level.moveMonsters(this);
            }
            if (rogue.rand.percent(freezePercent)) {
                for (i = 0; i < 50; i++) {
                    level.moveMonsters(this);
                }
                killedBy(null, Monster.HYPOTHERMIA);
            } else {
                tell("you_can_move_again", true);
            }
            monster.mFlags &= ~Monster.FREEZING_ROGUE;
        }
    }

    /**
     * @param monster
     */
    public void sting(Monster monster) {
        int stingChance = 35 + 36;
        if (strCurrent <= 3 || sustainStrength) {
            return;
        }
        if (armor != null) {
            stingChance = 35 + 6 * (6 - armor.getArmorClass());
        }

        if (exp + ringExp > 8) {
            stingChance -= 6 * (exp + ringExp - 8);
        }
        if (rogue.rand.percent(stingChance)) {
            tell("the " + monster.name() + "'s bite has weakened you");
            strCurrent--;
            printStat();
        }
    }

    /**
     * 
     */
    public void dropLevel() {
        if (rogue.rand.percent(80) || exp <= 5) {
            return;
        }
        expPoints = LEVEL_POINTS[exp - 2] - rogue.rand.get(9, 29);
        exp -= 2;
        int hp = hpRaise();
        hpCurrent -= hp;
        if (hpCurrent <= 0) {
            hpCurrent = 1;
        }
        hpMax -= hp;
        if (hpMax <= 0) {
            hpMax = 1;
        }
        add_exp(1, false);
    }

    /**
     * 
     */
    public void drainLife() {
        if (rogue.rand.percent(60) || hpMax <= 30 || hpCurrent < 10) {
            return;
        }
        int n = rogue.rand.get(1, 3); /* 1 Hp, 2 Str, 3 both */

        if (n != 2 || !sustainStrength) {
            tell("you feel weaker");
        }
        if (n != 2) {
            hpMax--;
            hpCurrent--;
        }
        if (n != 1) {
            if (strCurrent > 3 && !sustainStrength) {
                strCurrent--;
                if (rogue.rand.coin()) {
                    strMax--;
                }
            }
        }
        printStat();
    }

    private void win() {
        Date d = new Date();
        rogue.starttime = d.getTime() - rogue.starttime;
        tell("YOU WON!");
        col = -1;
        trapDoor = true;
        gameOver = true;
        view.empty();
        view.msg.banner(1, 6, option.nickName);
        view.addch(10, 11, "@   @  @@@   @   @      @  @  @   @@@   @   @   @");
        view.addch(11, 11, " @ @  @   @  @   @      @  @  @  @   @  @@  @   @");
        view.addch(12, 11, "  @   @   @  @   @      @  @  @  @   @  @ @ @   @");
        view.addch(13, 11, "  @   @   @  @   @      @  @  @  @   @  @  @@");
        view.addch(14, 11, "  @    @@@    @@@        @@ @@    @@@   @   @   @");
        view.addch(17, 11, "Congratulations  you have  been admitted  to  the");
        view.addch(18, 11, "Fighters' Guild.   You return home,  sell all your");
        view.addch(19, 11, "treasures at great profit and retire into comfort.");
        view.addch(21, 16, "You have " + gold + " in gold");
        view.addch(23, 11, "Press SPACE to see the hall of fame");
        view.refresh();
        rogue.waitForAck();
    }

    private String obituaryString(Persona monster, int other) {
        String obit = "";
        if (other != 0) {
            switch (other) {
                case Monster.HYPOTHERMIA:
                    obit = "Died of hypothermia";
                    break;
                case Monster.STARVATION:
                    obit = "Died of starvation";
                    break;
                case Monster.POISON_DART:
                    obit = "Killed by a dart";
                    break;
                case Monster.QUIT:
                    obit = "Quit the game";
                    break;
                case Monster.KFIRE:
                    obit = "Killed by fire";
                    break;
            }
        } else if (monster != null) {
            /* Took out the vowel lookup */
            // char i= monster.name().charAt(0);
            // if(i=='a'||i=='e'||i=='i'||i=='o'||i=='u')
            if (Id.isVowel((int) monster.name().charAt(0))) {
                obit = "Killed by an " + monster.name();
            } else {
                obit = "Killed by a " + monster.name();
            }
        }
        return obit;
    }

    private void killedBy(Persona monster, int other) {
        Date d = new Date();
        rogue.starttime = d.getTime() - rogue.starttime;
        if (other != Monster.QUIT) {
            gold = ((gold * 9) / 10);
        }
        String obit = obituaryString(monster, other);
        gameOverMessage = obit;

        String s = obit + " with " + gold + " gold";
        col = -1;
        gameOver = true;
        trapDoor = true;
        if (!option.noSkull) {
            view.empty();
            view.addch(4, 32, "__---------__");
            view.addch(5, 30, "_~             ~_");
            view.addch(6, 29, "/                 \\");
            view.addch(7, 28, "~                   ~");
            view.addch(8, 27, "/                     \\");
            view.addch(9, 27, "|    XXXX     XXXX    |");
            view.addch(10, 27, "|    XXXX     XXXX    |");
            view.addch(11, 27, "|    XXX       XXX    |");
            view.addch(12, 28, "\\         @         /");
            view.addch(13, 29, "--\\     @@@     /--");
            view.addch(14, 30, "| |    @@@    | |");
            view.addch(15, 30, "| |           | |");
            view.addch(16, 30, "| vvVvvvvvvvVvv |");
            view.addch(17, 30, "|  ^^^^^^^^^^^  |");
            view.addch(18, 31, "\\_           _/");
            view.addch(19, 33, "~---------~");
            view.addch(21, 8, option.nickName);
            view.addch(22, 8, s);
            view.addch(23, 8, "Press SPACE to see the graveyard");
        } else {
            tell(s);
            tell("Press SPACE to see the graveyard");
        }
        view.refresh();
        rogue.waitForAck();
    }

    /**
     * 
     */
    public void initSeen() {
        // terrible, terrible hack
        // I couldn't figure out another way to prevent the level
        // for being initialized as "unseen" when it's loaded from
        // a saved file. This just avoids the first 2 times this
        // method is run after a game is loaded from a save - UGH!
        if (justLoaded > 0) {
            justLoaded--;

            return;
        }
        seen = level.initSeen();
        vizset();
        moveSeen();
    }

    /**
     * @param ch
     * @return The code for the wall character provided
     */
    public int wallcode(char ch) {
        if (ch == '|') {
            return 0x10;
        }
        if (ch == '-') {
            return 0x20;
        }
        if (ch == '+') {
            return 0x30;
        }
        if (ch == '#') {
            return 0x40;
        }
        if (ch == '%') {
            return 0x50;
        }
        if (ch == '^') {
            return 0x60;
        }
        
        return 0;
    }

    private void moveSeen() {
        for (int r = 0; r < level.numRow; r++) {
            for (int c = 0; c < level.numCol; c++) {
                int w = seen[r][c];
                int v = w & 15;
                w &= 0xf0;
                if (v >= 4) {
                    if (v < 12) {
                        if (v < 8) {
                            char ch = level.getChar(r, c);
                            w = wallcode(ch);
                        }
                        view.mark(r, c);
                    } else {
                        v -= 8;
                    }
                    if (v < 8) {
                        v += 4;
                    } else {
                        v -= 8;
                    }
                    seen[r][c] = (char) (v + w);
                }
            }
        }
    }

    private void vizset() {
        if (0 != (level.map[row][col] & TUNNEL)) {
            for (int k = 0; k < 8; k++) {
                int r = row + Id.X_TABLE[k];
                int c = col + Id.Y_TABLE[k];
                int mask = level.map[r][c];
                if (0 != (mask & (TUNNEL | DOOR))) {
                    if (0 == (mask & HIDDEN)) {
                        seen[r][c] |= 4;
                    }
                }
            }
        } else {
            for (int r = 0; r < level.numRow; r++) {
                for (int c = 0; c < level.numCol; c++) {
                    if (seen[r][c] != 0) {
                        if (level.sees(r, c, row, col)) {
                            seen[r][c] |= 4;
                        }
                    }
                }
            }
        }
        seen[row][col] |= 4;
    }

    public boolean canSee(int r, int c) {
        if (blind > 0) {
            return false;
        }

        return 0 != (seen[r][c] & 8);
    }

    public String name() {
        return option.nickName;
    }

    private void saveGame() {
        System.out.println("Saving game.");
        rogue.pullIds();
        rogue.parentFrame.removeKeyListener(rogue);
        try {
            File saveFile = new File(System.getProperty("user.home") + "/.rogue/rogue.ser");
            if (!saveFile.getParentFile().exists()) {
                saveFile.getParentFile().mkdirs();
            }
            FileOutputStream fileOut = new FileOutputStream(saveFile);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(rogue);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in rogue.ser");
        } catch (IOException e) {
            e.printStackTrace();
        }
        gameOver = true;
        savedGame = true;
    }
}
