package rogue;

import java.io.Serializable;

/**
 *
 */
public class Door extends Item implements Serializable {
    private static final long serialVersionUID = -4425297158505745213L;

    // This is actually a directional passage (used by the monster's brain)
    // A monster entering at row,col wants to go to oth
    // passageto is null when it does not connect to another room
    // Monsters prefer passages that do connect to other rooms
    Rowcol other;
    Door passageto = null;

    Door(Level level, int row, int col, int otherRow, int otherCol) {
        super(level, row, col);
        itemCharacter = '+';
        placeAt(row, col, DOOR);
        other = new Rowcol(otherRow, otherCol);
    }

    Rowcol porch() {
        return level.porch(row, col);
    }

    Rowcol foyer() {
        return level.foyer(row, col);
    }

    Room otherRoom() {
        if (0 == (level.map[row][col] & Level.DOOR)) {
            return null;
        }
        
        return level.roomAt(row, col);
    }

    void connect(Door doorToOther) {
        passageto = doorToOther;
        doorToOther.passageto = this;
    }

    public String toString() {
        return super.toString() + other + passageto == null ? " to void" : " to room";
    }
}
