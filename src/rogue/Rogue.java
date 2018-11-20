package rogue;

import java.awt.BorderLayout;
/* Rogue.java -- Rogue game for java */
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 */
public class Rogue extends JPanel implements Runnable, Header, Serializable, KeyListener {
    private static final long serialVersionUID = 1484273858992751880L;
    private static final int[] MAP_CODES = {1002, 1003, 1001, 1000, 1006, 1004, 1007, 1005};

    private boolean running;
    int pointsize = 24;
    
    JFrame parentFrame;
    transient Thread gamer;
    Level level;
    Room endroom;
    int currentLevel;
    int maxLevel;
    List<View> viewList = new ArrayList<>();
    List<int[]> flashers = new ArrayList<>();
    Randomx rand;
    String keybuf = "";
    long starttime;

    Id[] idPotions = null;
    Id[] idScrolls = null;
    Id[] idWeapons = null;
    Id[] idArmors = null;
    Id[] idWands = null;
    Id[] idRings = null;

    String scorepagename;
    boolean interrupted;

    /**
     * @param args
     */
    public static void main(String[] args) {
        // Rogue r = new Rogue ();
        Rogue r = loadGame();
        if (r.parentFrame == null) {
            r.parentFrame = new JFrame("Java Rogue");
            // r.f.setSize(800,520);
            r.setSize(new Dimension(1200, 750));

            r.parentFrame.setLayout(new BorderLayout());

            r.parentFrame.add(r, BorderLayout.CENTER);
        }
        r.parentFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                r.exit();
            }
        });
//        ApplicationPreferences.loadPrefs(r);
//        r.parentFrame.invalidate();
//        r.parentFrame.validate();
//        r.parentFrame.repaint();
//        r.parentFrame.pack();
//        r.parentFrame.setVisible(true);
//        r.parentFrame.validate();

    }

    /*
     * public void windowClosing(WindowEvent e) { dispose(); System.exit(0); }
     * public void windowOpened(WindowEvent e) { } public void
     * windowIconified(WindowEvent e) { } public void windowClosed(WindowEvent
     * e) { dispose(); System.exit(0); } public void
     * windowDeiconified(WindowEvent e) { } public void
     * windowActivated(WindowEvent e) { } public void
     * windowDeactivated(WindowEvent e) { }
     */
    /**
     * 
     */
    public Rogue() {
        setLayout(new BorderLayout());
        setOpaque(false);
        rand = new Randomx((int) System.currentTimeMillis());
        try {
            // int i= Integer.parseInt(getParameter("srand"));
            // rand= new Randomx(i);
            rand = new Randomx();
        } catch (NumberFormatException e) {
        }
        // scorepagename= getParameter("score");
        setBackground(Color.black);
        new Monster(); // Force static definitions
        start();
    }

    /**
     * Startup the game thread
     */
    public void start() {
        running = true;
        gamer = new Thread(this, "Rogue Thread");
        gamer.start();
        repaint(30);
    }

    /**
     * Stop the game thread
     */
    public void stop() {
        running = false;
        gamer = null;
    }

    boolean newlevel = true;

    void exit() {
        ApplicationPreferences.savePrefs(this);
        System.exit(0);
    }
    
    void outputScores() {
        View view = (View) viewList.get(0);
        view.empty();
        view.addch(4, 32, "__--HIGH SCORES---__");

        File scoreFile = new File(System.getProperty("user.home") + "/.rogue/jrogue.scr");
        if (!scoreFile.exists()) {
            scoreFile.getParentFile().mkdirs();
        }
        String[] topTen = new String[10];
        try {
            Scanner inStream = new Scanner(scoreFile);
            for (int i = 0; inStream.hasNextLine() && i < 10; i++) {
                topTen[i] = inStream.nextLine();
            }
            inStream.close();
        } catch (FileNotFoundException e) {
            System.out.println("No score file found.");
        }
        try {
            if (!scoreFile.exists()) {
                scoreFile.getParentFile().mkdirs();
            }
            PrintStream outStream = new PrintStream(scoreFile);

            Man man = view.man;
            boolean currentScoreOutputted = false;
            for (int i = 0; i < topTen.length; i++) {
                if (!currentScoreOutputted && topTen[i] == null) {
                    outStream.println(i + 1 + " " + man.gold + " " + man.name() + " " + Man.gameOverMessage);
                    view.addch(5 + i, 30, i + 1 + " " + man.gold + " " + man.name() + " " + Man.gameOverMessage);
                    currentScoreOutputted = true;
                } else if (!currentScoreOutputted && topTen[i] != null) {
                    System.out.println(topTen[i]);
                    String[] score = topTen[i].split("\\s+");
                    for (int j = 4; j < score.length; j++) { // terrible hack,
                                                             // fix
                        score[3] += " " + score[j];
                    }
                    if (!currentScoreOutputted && man.gold > Integer.parseInt(score[1])) {
                        outStream.println(i + 1 + " " + man.gold + " " + man.name() + " " + Man.gameOverMessage);
                        view.addch(5 + i, 30, i + 1 + " " + man.gold + " " + man.name() + " " + Man.gameOverMessage);
                        outStream.println(i + 2 + " " + score[1] + " " + score[2] + " " + score[3]);
                        view.addch(6 + i, 30, i + 2 + " " + score[1] + " " + score[2] + " " + score[3]);
                        currentScoreOutputted = true;
                    } else if (score.length >= 3) {
                        outStream.println(i + 1 + " " + score[1] + " " + score[2] + " " + score[3]);
                        view.addch(5 + i, 30, i + 1 + " " + score[1] + " " + score[2] + " " + score[3]);
                    }
                } else if (topTen[i] != null) {
                    String[] score = topTen[i].split("\\s+");
                    for (int j = 4; j < score.length; j++) { // terrible hack,
                                                             // fix
                        score[3] += " " + score[j];
                    }
                    outStream.println(i + 2 + " " + score[1] + " " + score[2] + " " + score[3]);
                    view.addch(6 + i, 30, i + 2 + " " + score[1] + " " + score[2] + " " + score[3]);
                }
            }
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        view.addch(19, 33, "~---------~");
        view.addch(23, 8, "Press SPACE to exit");
        view.refresh();
        waitForAck();
    }

    void endGame() {
        if (!Man.savedGame) {
            outputScores();
        }
        exit();
    }

    void beginGame() {
        System.out.println("Beginning new game");
        View view = null;
        // Id.list_items();
        if (viewList.size() == 0) {
            view = new View(this, pointsize, 25, 80);
            add(view, BorderLayout.CENTER);
            viewList.add(view);
            Man man = new Man(this, view);
            view.man = man;
        } else {
            view = viewList.get(0);
            Man man = view.man;
            Option oldop = man != null ? man.option : null;
            view.empty();
            view.man = man = new Man(this, view);
            man.option = oldop;
        }
        view.requestFocus();
        view.addKeyListener(this);
        Id.mixColors(rand);
        Id.makeScrollTitles(rand);
        // Level.cur_level= 0;
        currentLevel = 0;
        // Level.max_level= 0;
        maxLevel = 0;
        Date d = new Date();
        starttime = d.getTime();
    }

    public void run() {
        System.out.println("running");
        Man man;
        gamer.setPriority(Thread.MIN_PRIORITY);
        while (parentFrame == null) {
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
        ApplicationPreferences.loadPrefs(this);
        if (viewList.size() == 0) {
            beginGame();
        } else {
            viewList.get(0).pointsize = pointsize;
            viewList.get(0).getPreferredSize();
            viewList.get(0).repaint();
        }
        parentFrame.invalidate();
        parentFrame.validate();
        parentFrame.repaint();
        parentFrame.pack();
        parentFrame.setVisible(true);
        parentFrame.validate();
        while (running) {
            System.out.println("in main game loop");
            if (newlevel) {
                System.out.println("creating new level");
                interrupted = false;
                level = new NineRoom(25, 80, this);
                level.putMonsters();
                for (View v : viewList) {
                    man = v.man;
                    man.level = level;
                    if (man.pack == null) {
                        man.playerInit();
                    }
                    if (!man.hasAmulet() && (level.currentLevel >= Level.AMULET_LEVEL)) {
                        Rowcol pt = level.getRandomRowCol(Level.FLOOR | Level.TUNNEL, null);
                        if (pt != null) {
                            Toy amulet = new Toy(level, Id.AMULET);
                            amulet.placeAt(pt.row, pt.col, TOY);
                        }
                    }
                    v.level = level;
                    v.empty();
                    v.man.pack.relevel(level);
                    level.putPlayer(v.man);
                }
                newlevel = false;
                parentFrame.revalidate();
            }
            for (View v : viewList) {
                v.man.initSeen();
                v.man.printStat();
                v.refresh();
            }
            repaint();
            //// view.refresh();
            man = (Man) ((View) viewList.get(0)).man;
            man.play_level();
            if (Man.gameOver) {
                mdSlurp();
                endGame();
            }
            newlevel = true;
        }
        exit();
    }

    public boolean mouseDown(Event evt, int x, int y) {
        return true;
    }

    public boolean mouseUp(Event evt, int x, int y) {
        return true;
    }

    synchronized public boolean keyDown(Event evt, int key) {
        if (key == '\033') {
            interrupted = true;
        }
        if (!gamer.isAlive()) {
            if (key == ' ') {
                start();
            }
        } else {
            keybuf = keybuf + ((char) key);
        }
        notify();
        
        return true;
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        int key = e.getKeyChar();
        int code = e.getKeyCode();
        
        if (code != KeyEvent.VK_SHIFT && code != KeyEvent.VK_CONTROL) {
            if (KeyEvent.VK_PAGE_UP <= code && code <= KeyEvent.VK_DOWN) {
                key = MAP_CODES[code - KeyEvent.VK_PAGE_UP];
            }
            if (key == KeyEvent.VK_ESCAPE) {
                interrupted = true;
            }
            if (!gamer.isAlive()) {
                if (key == KeyEvent.VK_SPACE) {
                    start();
                }
            } else {
                if ((e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK) {
                    if (code >= KeyEvent.VK_A && code <= KeyEvent.VK_Z) {
                        key = '\000' + (char) (code - KeyEvent.VK_A);
                    } else if (code == KeyEvent.VK_LEFT) {
                        key = '\010';
                    } else if (code == KeyEvent.VK_RIGHT) {
                        key = '\014';
                    } else if (code == KeyEvent.VK_UP) {
                        key = '\013';
                    } else if (code == KeyEvent.VK_DOWN) {
                        key = '\012';
                    }
                }
                keybuf = keybuf + ((char) key);
            }
        }
        notify();
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
    
    synchronized void mdSleep(int mseconds) {
        if (mseconds > 0) {
            try { Thread.sleep(mseconds); } catch (InterruptedException e) {}
        }
        keybuf = "";
    }

    synchronized void mdSlurp() {
        keybuf = "";
    }

    synchronized int mdGetchar() {
        while (keybuf == null || keybuf.length() == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                interrupted = true;
         
                return '\033';
            }
        }
        int key = (int) keybuf.charAt(0);
        keybuf = keybuf.substring(1);
        
        return key;
    }

    int rgetchar() {
        return mdGetchar();
    }

    void waitForAck() {
        int c;
        do {
            c = rgetchar();
        } while (c != ' ' && c != '\033');
    }

    void flashadd(int row, int col, int color) {
        int ia[] = new int[3];
        ia[0] = row;
        ia[1] = col;
        ia[2] = color;
        flashers.add(ia);
    }

    void xflash() {
        if (flashers.size() > 0) {
            boolean bseen = false;
            List<Character> chsave = new ArrayList<>(flashers.size());

            for (View v : viewList) {
                boolean vseen = false;
                for (int[] ia : flashers) {
                    if (v.inSight(ia[0], ia[1])) {
                        int ch = v.terminal[ia[0]][ia[1]];
                        chsave.add(new Character((char) ch));
                        ch &= 255;
                        if (ch == '.') {
                            ch = '*';
                        }
                        ch = (ch & 255) | ia[2];
                        v.addch(ia[0], ia[1], (char) ch);
                        vseen = true;
                    }
                }
                if (vseen) {
                    v.refresh();
                    bseen = true;
                    mdSleep(120);
                }
            }
            if (bseen)
                for (View v : viewList) {
                    for (int i=0;i<flashers.size();i++) {
                        int ia[] = flashers.get(i);
                        if (v.inSight(ia[0], ia[1])) {
                            v.addch(ia[0], ia[1], chsave.get(i).charValue());
                        }
                        v.mark(ia[0], ia[1]);
                    }
                }
            flashers = new ArrayList<>();
        }
    }

    void vflash(int r, int c, char ch) {
        boolean bseen = false;

        for (View v : viewList) {
            if (v.inSight(r, c)) {
                bseen = true;
                v.addch(r, c, ch);
            }
        }
        if (bseen) {
            refresh();
            mdSleep(50);
            ch = level.getChar(r, c);
            for (View v : viewList) {
                v.addch(r, c, ch);
            }
        }
    }

    void tell(Persona p, String s, boolean bintr) {
        for (View v : viewList) {
            if (v.man == p) {
                String ss = whoify(p, s);
                v.msg.message(ss, bintr);
            }
        }
        xflash();
    }

    boolean describe(Rowcol rc, String s, boolean bintr) {
        for (View v : viewList) {
            if (v.inSight(rc.row, rc.col)) {
                String ss = whoify(v.man, s);
                v.msg.message(ss, bintr);
                return true;
            }
        }
        xflash();
        return false;
    }

    void checkMessage(Persona p) {
        for (View v : viewList) {
            if (v.man == p)
                v.msg.checkMessage();
        }
    }

    void refresh() {
        for (View v : viewList) {
            v.refresh();
        }
    }

    void vset(int r, int c) {
        for (View v : viewList) {
            char ch = v.charat(r, c);
            v.addch(r, c, ch);
        }
    }

    void mark(int r, int c) {
        for (View v : viewList) {
            v.mark(r, c);
        }
    }

    void markall() {
        for (View v : viewList) {
            v.markall();
        }
    }

    String whoify(Persona p, String src) {
        String dst = "";
        int i = 0;
        int j;
        try {
            while ((j = src.indexOf('@', i)) >= 0) {
                dst += src.substring(i, j);
                boolean hasverb = src.charAt(++j) == '>';
                i = j + 1;
                j = src.indexOf(hasverb ? '+' : '>', i);
                boolean byou = false;
                String name = src.substring(i, j);
                if (name.equals(p.name())) {
                    dst += "you";
                    byou = true;
                } else {
                    dst += "the " + name;
                }
                if (hasverb) {
                    i = j + 1;
                    dst += " ";
                    j = src.indexOf('+', i);
                    if (byou) {
                        dst += src.substring(i, j);
                        i = src.indexOf('<', j);
                    } else {
                        i = src.indexOf('<', j);
                        dst += src.substring(j + 1, i);
                    }
                } else {
                    i = j;
                }
                ++i;
            }
        } catch (Exception e) {
            System.out.println("whoify error on " + p.name());
            System.out.println(src + "\n" + dst);
        }
        dst += src.substring(i);
        
        return dst;
    }

    void pullIds() {
        idPotions = Id.idPotions;
        idScrolls = Id.idScrolls;
        idWeapons = Id.idWeapons;
        idArmors = Id.idArmors;
        idWands = Id.idWands;
        idRings = Id.idRings;
    }

    void pushIds() {
        Id.idPotions = idPotions;
        Id.idScrolls = idScrolls;
        Id.idWeapons = idWeapons;
        Id.idArmors = idArmors;
        Id.idWands = idWands;
        Id.idRings = idRings;
    }

    static Rogue loadGame() {
        Rogue r = null;
        try {

            // Reading the object from a file
            File file = new File(System.getProperty("user.home") + "/.rogue/rogue.ser");
            FileInputStream fileStream = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileStream);

            // Method for deserialization of object
            try {
                r = (Rogue) in.readObject();
                // Id.mix_colors(new Randomx(r.potionSeed));
                // Id.make_scroll_titles(new Randomx(r.scrollSeed));
                r.pushIds();
            } catch (InvalidClassException e) {
                // This is thrown when the code is updated and invalidates
                // saved games from previous versions.
                System.err.println("Saved game is from a previous versions.  Starting new game.");
                r = new Rogue();
            }
            in.close();
            fileStream.close();
            file.delete();

            System.out.println("Object has been deserialized ");
            Man.justLoaded = 2;
            r.start();
        } catch (Exception e) {
            System.err.println("Couldn't load saved game.  Starting new game.");
            r = new Rogue();
        }

        return r;
    }
}
