package rogue;

import java.io.Serializable;

/**
 *
 */
public class Rowcol implements Serializable {
    private static final long serialVersionUID = -8950780811400161169L;

    int row;
    int col;

    Rowcol(int r, int c) {
        row = r;
        col = c;
    }

    Rowcol() {
        this(0, 0);
    }

    public String toString() {
        return '[' + Integer.toString(row) + ' ' + Integer.toString(col) + ']';
    }
}
