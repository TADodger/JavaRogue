package rogue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.EmptyStackException;
import java.util.Stack;

import javax.swing.JPanel;

/**
 *
 */
public class View extends JPanel implements Serializable, ComponentListener {
    private static final long serialVersionUID = -68447722052194881L;

    private Dimension size;
    int pointsize;
    private transient BufferedImage backgroundBuffer;
    private boolean initial;
    
    Rogue self;
    Message msg;
    Item dummy;
    Stack<Rowcol> marked = new Stack<>();
    Level level;
    char[][] terminal;
    char[][] buffer; // Low byte=ascii character, High byte=color index
    boolean line_dirty[];
    int nrow;
    int ncol;
    int characterWidth; /* Character width */
    int characterHeight; /* Character height */
    int characterAscent; /* Ascent */
    int lead; /* Font leading */
    Font ffixed;
    FontMetrics fm;
    Man man = null; /* From whose point of view this is */

    /**
     * @param self
     * @param pointsize
     * @param nrow
     * @param ncol
     */
    public View(Rogue self, int pointsize, int nrow, int ncol) {
        this.self = self;
        this.nrow = nrow;
        this.ncol = ncol;
        this.msg = new Message(this);
        terminal = new char[nrow][ncol];
        buffer = new char[nrow][ncol];
        line_dirty = new boolean[nrow];
        initial = true;

        this.pointsize = pointsize;
        for (int k = 0; k < nrow; k++) {
            line_dirty[k] = false;
            for (int c = 0; c < ncol; c++) {
                terminal[k][c] = ' ';
            }
        }
        getPreferredSize();
        requestFocus();
        addComponentListener(this);
    }

    public Dimension getPreferredSize() {
        calculateSize();

        return size;
    }

    @Override
    public void componentHidden(ComponentEvent e) {}

    @Override
    public void componentMoved(ComponentEvent e) {}

    @Override
    public void componentShown(ComponentEvent e) {}

    @Override
    public void componentResized(ComponentEvent e) {
//        System.out.println("RESIZED");
        calculateSize();
    }
    
    private void calculateSize() {
        ffixed = new Font("Courier", Font.PLAIN, pointsize);
        FontMetrics fm = getFontMetrics(ffixed);
        characterWidth = fm.charWidth('X');
        characterHeight = fm.getHeight();
        characterAscent = fm.getAscent();
        size = new Dimension(ncol * characterWidth, nrow * characterHeight);
        if (!initial) {
            Dimension currentSize = getSize();
            if (size.width < currentSize.width && size.height < currentSize.height) {
                do {
                    pointsize += 1;
                    ffixed = new Font("Courier", Font.PLAIN, pointsize);
                    fm = getFontMetrics(ffixed);
                    characterWidth = fm.charWidth('X');
                    characterHeight = fm.getHeight();
                    characterAscent = fm.getAscent();
                    size = new Dimension(ncol * characterWidth, nrow * characterHeight);
                } while (size.width < currentSize.width && size.height < currentSize.height);
                pointsize -= 1;
                ffixed = new Font("Courier", Font.PLAIN, pointsize);
                fm = getFontMetrics(ffixed);
                characterWidth = fm.charWidth('X');
                characterHeight = fm.getHeight();
                characterAscent = fm.getAscent();
                size = new Dimension(ncol * characterWidth, nrow * characterHeight);
            } else if (size.width > currentSize.width || size.height > currentSize.height) {
                do {
                    pointsize -= 1;
                    ffixed = new Font("Courier", Font.PLAIN, pointsize);
                    fm = getFontMetrics(ffixed);
                    characterWidth = fm.charWidth('X');
                    characterHeight = fm.getHeight();
                    characterAscent = fm.getAscent();
                    size = new Dimension(ncol * characterWidth, nrow * characterHeight);
                } while (size.width > currentSize.width || size.height > currentSize.height);
            }
        }
        initial = false;
        backgroundBuffer = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        self.setPreferredSize(size);
        repaint();
        self.parentFrame.pack();
    }

    boolean in_sight(int row, int col) {
        return man.can_see(row, col);
    }

    static Color cmap[] = new Color[8];
    static {
        cmap[0] = Color.lightGray;
        cmap[1] = Color.gray;
        cmap[2] = Color.black;
        cmap[3] = Color.white;
        cmap[4] = Color.red;
        cmap[5] = Color.yellow;
        cmap[6] = new Color(128, 0, 0); // Dark red
        cmap[7] = new Color(0, 160, 0); // Green
    }

    public void update(Graphics g) {
        paint(g);
    }
    
    private void inupdate(Graphics g) {
        if (g != null) {
            if (backgroundBuffer == null) {
                backgroundBuffer = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics g2 = backgroundBuffer.getGraphics();
            g2.setFont(ffixed);
            byte ba[] = new byte[ncol];
            for (int y = 0; y < nrow; y++) {
                if (line_dirty[y]) {
                    line_dirty[y] = false;
                    char[] ter = terminal[y];
                    char[] buf = buffer[y];
                    for (int x = 0; x < ncol; x++) {
                        ter[x] = buf[x];
                        ba[0] = (byte) (buf[x] & 127);
                        int st = buf[x] >> 8;
                        if (ba[0] == '_' || ba[0] == 0 || st == 2) {
                            ba[0] = ' ';
                        }
                        g2.setColor(Color.black);
                        g2.fillRect(x * characterWidth, y * characterHeight, characterWidth, characterHeight);
                        g2.setColor(cmap[st]);
                        g2.drawBytes(ba, 0, 1, x * characterWidth, y * characterHeight + characterAscent);
                    }
                }
            }
            g.drawImage(backgroundBuffer, 0, 0, this);
        }
    }

    void mark(int r, int c) {
        marked.push(new Rowcol(r, c));
    }

    void markall() {
        marked = new Stack<>();
        marked.push(new Rowcol(0, 0));
    }

    Rowcol getmark() {
        Rowcol pt;
        try {
            pt = (Rowcol) marked.pop();
        } catch (EmptyStackException e) {
            pt = null;
        }
        
        return pt;
    }
    
    protected void paintComponent(Graphics g) {
        synchronized (self.gamer) {
            for (int r = 0; r < nrow; r++) {
                line_dirty[r] = true;
                char ter[] = terminal[r];
                for (int c = 0; c < ncol; c++) {
                    ter[c] = 0;
                }
                inupdate(g);
            }
        }
    }

    void refresh() {
        Graphics g = getGraphics();
        // assert (g != null);
        inupdate(g);
        // g.dispose();
    }

    void addch(int row, int col, char ch) {
        if (row >= 0 && row < nrow && col >= 0 && col < ncol) {
            if (ch != buffer[row][col]) {
                buffer[row][col] = ch;
                line_dirty[row] = true;
            }
        }
    }

    void addch(int row, int col, String s) {
        if (row >= 0 && row < nrow) {
            int n = s.length();
            col += n;
            while (--n >= 0) {
                --col;
                if (col >= 0 && col < ncol) {
                    buffer[row][col] = s.charAt(n);
                }
            }
            line_dirty[row] = true;
        }
    }

    char charat(int row, int col) {
        return buffer[row][col];
    }

    void empty() {
        for (int r = 0; r < nrow; r++) {
            line_dirty[r] = true;
            for (int c = 0; c < ncol; c++) {
                terminal[r][c] = 0;
                buffer[r][c] = ' ';
            }
        }
        refresh();
    }
}

