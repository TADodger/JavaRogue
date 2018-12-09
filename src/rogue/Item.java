package rogue;

import java.io.Serializable;
import java.util.List;

/**
 *
 */
public class Item extends Rowcol implements Header, Serializable {
    private static final long serialVersionUID = 2276875920996375357L;

    /** display character for this Item */
    public char itemCharacter; /* 'A' is for aquatar */
    /** Level this Item is found on */
    public Level level;

    /**
     * Create an empty item
     */
    public Item() {
        level = null;
    }

    /**
     * @param level
     * @param row
     * @param col
     */
    public Item(Level level, int row, int col) {
        super(row, col);
        this.level = level;
        itemCharacter = '?';
    }

    /**
     * @param level
     */
    public Item(Level level) {
        this(level, 0, 0);
    }

    /**
     * Place this Item at the specified location
     * 
     * @param destRow
     * @param destCol
     * @param what
     */
    @SuppressWarnings("unchecked")
    public void placeAt(int destRow, int destCol, int what) {
        //Due to the way the generic type system works this will only work with a raw type.
        @SuppressWarnings("rawtypes")
        List list = null;
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
            } else if (destRow >= 0) {
                list.add(this);
            }
        }
        row = destRow;
        col = destCol;
        if (destRow > 0) {
            level.mark(destRow, destCol);
            level.map[destRow][destCol] |= what;
        } else if (list != null) {
            list.remove(this);
        }
    }

    @Override
    public String toString() {
        return super.toString() + Integer.toString(itemCharacter >> 8) + ((char) (itemCharacter & 255)) + " ";
    }
}
