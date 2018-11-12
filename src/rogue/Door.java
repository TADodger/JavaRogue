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
    Rowcol oth;
    Door passageto = null;

    Door(Level level, int r, int c, int or, int oc) {
        super(level, r, c);
        ichar = '+';
        place_at(r, c, DOOR);
        oth = new Rowcol(or, oc);
    }

    Rowcol porch() {
        return level.porch(row, col);
    }

    Rowcol foyer() {
        return level.foyer(row, col);
    }

    Room other_room() {
        if (0 == (level.map[row][col] & Level.DOOR))
            return null;
        return level.room_at(row, col);
    }

    void connect(Door dto) {
        passageto = dto;
        dto.passageto = this;
    }

    public String toString() {
        return super.toString() + oth + passageto == null ? " to void" : " to room";
    }
}
