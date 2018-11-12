package rogue;

/* Rogue.java -- Rogue game for java */
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
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

/**
 *
 */
public class Rogue extends Panel implements Runnable, Header, Serializable {
    private static final long serialVersionUID = 1484273858992751880L;

    private boolean running;
    
    Frame f;
    transient Thread gamer;
    Level level;
    Room endroom;
    int cur_level;
    int max_level;
    List<View> view_list = new ArrayList<>();
    List<int[]> flashers = new ArrayList<>();
    Randomx rand;
    String keybuf = "";
    long starttime;

    Id[] id_potions = null;
    Id[] id_scrolls = null;
    Id[] id_weapons = null;
    Id[] id_armors = null;
    Id[] id_wands = null;
    Id[] id_rings = null;

    int pointsize = 12;
    String scorepagename;
    boolean interrupted;

    /**
     * @param args
     */
    public static void main(String[] args) {
        // Rogue r = new Rogue ();
        Rogue r = loadGame();
        if (r.f == null) {
            r.f = new Frame();
            // r.f.setSize(800,520);
            r.setPreferredSize(new Dimension(1200, 750));

            r.f.setLayout(new FlowLayout());

            r.f.add(r);
            r.f.pack();
        }
        r.f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
        r.f.invalidate();
        r.f.validate();
        r.f.repaint();
        r.f.pack();
        r.f.setVisible(true);
        r.f.validate();

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
        try {
            // pointsize= Integer.parseInt(getParameter("pointsize"));
            pointsize = 16;
        } catch (NumberFormatException e) {
            pointsize = 12;
        }
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

    void outputScores() {
        View view = (View) view_list.get(0);
        view.empty();
        view.addch(4, 32, "__--HIGH SCORES---__");

        File scoreFile = new File("jrogue.scr");
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
            PrintStream outStream = new PrintStream(scoreFile);

            Man man = view.man;
            boolean currentScoreOutputted = false;
            for (int i = 0; i < topTen.length; i++) {
                if (!currentScoreOutputted && topTen[i] == null) {
                    outStream.println(i + 1 + " " + man.gold + " " + man.name() + " " + Man.game_over_String);
                    view.addch(5 + i, 30, i + 1 + " " + man.gold + " " + man.name() + " " + Man.game_over_String);
                    currentScoreOutputted = true;
                } else if (!currentScoreOutputted && topTen[i] != null) {
                    System.out.println(topTen[i]);
                    String[] score = topTen[i].split("\\s+");
                    for (int j = 4; j < score.length; j++) { // terrible hack,
                                                             // fix
                        score[3] += " " + score[j];
                    }
                    if (!currentScoreOutputted && man.gold > Integer.parseInt(score[1])) {
                        outStream.println(i + 1 + " " + man.gold + " " + man.name() + " " + Man.game_over_String);
                        view.addch(5 + i, 30, i + 1 + " " + man.gold + " " + man.name() + " " + Man.game_over_String);
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
        wait_for_ack();
    }

    void end_game() {
        if (!Man.saved_game)
            outputScores();
        System.exit(0);
    }

    void begin_game() {
        System.out.println("Beginning new game");
        View view = null;
        // Id.list_items();
        if (view_list.size() == 0) {
            add(view = new View(this, pointsize, 25, 80));
            view_list.add(view);
            Man man = new Man(this, view);
            view.man = man;
        } else {
            view = view_list.get(0);
            Man man = view.man;
            Option oldop = man != null ? man.option : null;
            view.empty();
            view.man = man = new Man(this, view);
            man.option = oldop;
        }
        System.gc();
        view.requestFocus();
        Id.mixColors(rand);
        Id.makeScrollTitles(rand);
        // Level.cur_level= 0;
        cur_level = 0;
        // Level.max_level= 0;
        max_level = 0;
        Date d = new Date();
        starttime = d.getTime();
    }

    public void run() {
        System.out.println("running");
        Man man;
        gamer.setPriority(Thread.MIN_PRIORITY);
        while (running) {
            System.out.println("in main game loop");
            if (newlevel) {
                System.out.println("creating new level");
                if (view_list.size() == 0) {
                    begin_game();
                }
                interrupted = false;
                level = new NineRoom(25, 80, this);
                level.put_monsters();
                for (View v : view_list) {
                    man = v.man;
                    man.level = level;
                    if (man.pack == null) {
                        man.player_init();
                    }
                    if (!man.has_amulet() && (level.cur_level >= Level.AMULET_LEVEL)) {
                        Rowcol pt = level.gr_row_col(Level.FLOOR | Level.TUNNEL, null);
                        if (pt != null) {
                            Toy amulet = new Toy(level, Id.AMULET);
                            amulet.place_at(pt.row, pt.col, TOY);
                        }
                    }
                    v.level = level;
                    v.empty();
                    v.man.pack.relevel(level);
                    level.put_player(v.man);
                }
                newlevel = false;
                f.revalidate();
            }
            for (View v : view_list) {
                v.man.init_seen();
                v.man.print_stat();
                v.refresh();
            }
            repaint();
            //// view.refresh();
            man = (Man) ((View) view_list.get(0)).man;
            man.play_level();
            if (Man.game_over) {
                md_slurp();
                end_game();
            }
            newlevel = true;
        }
    }

    public void paint(Graphics g) {
        int y = 0;
        for (View v : view_list) {
            Dimension s = getSize();
            Dimension d = v.getSize();
            v.setLocation((s.width - d.width) / 2, y + v.ch);
            v.repaint();
            y += d.height + 2 * v.ch;
        }
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

    synchronized void md_sleep(int mseconds) {
        if (mseconds > 0) {
            try { Thread.sleep(mseconds); } catch (InterruptedException e) {}
        }
        keybuf = "";
    }

    synchronized void md_slurp() {
        keybuf = "";
    }

    synchronized int md_getchar() {
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
        return md_getchar();
    }

    void wait_for_ack() {
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

            for (View v : view_list) {
                boolean vseen = false;
                for (int[] ia : flashers) {
                    if (v.in_sight(ia[0], ia[1])) {
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
                    md_sleep(120);
                }
            }
            if (bseen)
                for (View v : view_list) {
                    for (int i=0;i<flashers.size();i++) {
                        int ia[] = flashers.get(i);
                        if (v.in_sight(ia[0], ia[1])) {
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

        for (View v : view_list) {
            if (v.in_sight(r, c)) {
                bseen = true;
                v.addch(r, c, ch);
            }
        }
        if (bseen) {
            refresh();
            md_sleep(50);
            ch = level.get_char(r, c);
            for (View v : view_list) {
                v.addch(r, c, ch);
            }
        }
    }

    void tell(Persona p, String s, boolean bintr) {
        for (View v : view_list) {
            if (v.man == p) {
                String ss = whoify(p, s);
                v.msg.message(ss, bintr);
            }
        }
        xflash();
    }

    boolean describe(Rowcol rc, String s, boolean bintr) {
        for (View v : view_list) {
            if (v.in_sight(rc.row, rc.col)) {
                String ss = whoify(v.man, s);
                v.msg.message(ss, bintr);
                return true;
            }
        }
        xflash();
        return false;
    }

    void check_message(Persona p) {
        for (View v : view_list) {
            if (v.man == p)
                v.msg.checkMessage();
        }
    }

    void refresh() {
        for (View v : view_list) {
            v.refresh();
        }
    }

    void vset(int r, int c) {
        for (View v : view_list) {
            char ch = v.charat(r, c);
            v.addch(r, c, ch);
        }
    }

    void mark(int r, int c) {
        for (View v : view_list) {
            v.mark(r, c);
        }
    }

    void markall() {
        for (View v : view_list) {
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
        id_potions = Id.idPotions;
        id_scrolls = Id.idScrolls;
        id_weapons = Id.idWeapons;
        id_armors = Id.idArmors;
        id_wands = Id.idWands;
        id_rings = Id.idRings;
    }

    void pushIds() {
        Id.idPotions = id_potions;
        Id.idScrolls = id_scrolls;
        Id.idWeapons = id_weapons;
        Id.idArmors = id_armors;
        Id.idWands = id_wands;
        Id.idRings = id_rings;
    }

    static Rogue loadGame() {
        Rogue r = null;
        try {

            // Reading the object from a file
            File file = new File("rogue.ser");
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
