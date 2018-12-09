package rogue;

import java.io.Serializable;

/**
 *
 */
public class Rowcol implements Serializable {
    private static final long serialVersionUID = -8950780811400161169L;

    int row;
    int col;

    /**
     * @param row
     * @param col
     */
    public Rowcol(int row, int col) {
        this.row = row;
        this.col = col;
    }

    protected Rowcol() {
        this(0, 0);
    }

    @Override
    public String toString() {
        return "[" + Integer.toString(row) + ' ' + Integer.toString(col) + "]";
    }
}
