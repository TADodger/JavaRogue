package rogue;

import java.io.Serializable;
import java.util.List;

/**
 *
 */
public class Item extends Rowcol implements Header, Serializable {
    private static final long serialVersionUID = 2276875920996375357L;

    char ichar; /* 'A' is for aquatar */
    Level level;

    Item() {
        level = null;
    }

    Item(Level level, int r, int c) {
        this.level = level;
        row = r;
        col = c;
        ichar = '?';
    }

    Item(Level level) {
        this(level, 0, 0);
    }

    void placeAt(int r, int c, int what) {
        List<Item> list = null;
        switch (what) {
            case TOY:
                list = level.levelToys;
                break;
            case MONSTER:
                list = level.levelMonsters;
                break;
            case MAN:
                list = level.levelMen;
                break;
            case TRAP:
                list = level.levelTraps;
                break;
            case DOOR:
                list = level.levelDoors;
                break;
        }
        if (list != null) {
            if (list.contains(this)) {
                level.mark(row, col);
                level.map[row][col] &= ~what;
            } else if (r >= 0) {
                list.add(this);
            }
        }
        row = r;
        col = c;
        if (r > 0) {
            level.mark(r, c);
            level.map[r][c] |= what;
        } else if (list != null) {
            list.remove(this);
        }
    }

    public String toString() {
        return super.toString() + Integer.toString(ichar >> 8) + ((char) (ichar & 255)) + " ";
    }
}
