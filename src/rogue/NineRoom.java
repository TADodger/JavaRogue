package rogue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class NineRoom extends Level implements Serializable {
    private static final long serialVersionUID = 3336671744682766539L;

    private List<Room> room = null;
    private Room partyRoom = null;

    private final Room PASSAGE = new Room();

    private static final int GOLD_PERCENT = 46;
    private static final int MAXROOMS = 9;
    private static final int BIG_ROOM = 10;

    /**
     * @param nrow
     * @param ncol
     * @param self
     */
    public NineRoom(int nrow, int ncol, Rogue self) {
        super(nrow, ncol, self);
        PASSAGE.roomSize = -1;
        int i;
        int j;
        boolean bfinished = false;

        if (currentLevel < LAST_MAP) {
            self.currentLevel++;
            currentLevel = self.currentLevel;
        }
        if (currentLevel > maxLevel) {
            self.maxLevel = currentLevel;
            maxLevel = self.maxLevel;
        }

        while (!bfinished) {
            try {
                int must1 = 0;
                int must2 = 0;
                int must3 = 0;
                Room r;

                if (currentLevel > 1 && self.rand.percent(8)) {
                    partyRoom = PASSAGE; // Flag set up party room
                }

                boolean bigRoom = ((partyRoom != null) && self.rand.percent(1));
                if (bigRoom) {
                    room = new ArrayList<>(1);

                    int topRow = self.rand.get(MIN_ROW, MIN_ROW + 5);
                    int height = self.rand.get(nrow - 7, nrow - 2) - topRow;
                    int leftCol = self.rand.get(1, 10);
                    ;
                    int width = self.rand.get(ncol - 11, ncol - 1) - leftCol;
                    room.add(new Room(BIG_ROOM, leftCol, topRow, width, height, this, false));
                } else {
                    room = new ArrayList<>(MAXROOMS);

                    // Required rooms (need one full row or one full column)
                    switch (self.rand.get(5)) {
                        case 0:
                            must1 = 0;
                            must2 = 1;
                            must3 = 2;
                            break;
                        case 1:
                            must1 = 3;
                            must2 = 4;
                            must3 = 5;
                            break;
                        case 2:
                            must1 = 6;
                            must2 = 7;
                            must3 = 8;
                            break;
                        case 3:
                            must1 = 0;
                            must2 = 3;
                            must3 = 6;
                            break;
                        case 4:
                            must1 = 1;
                            must2 = 4;
                            must3 = 7;
                            break;
                        case 5:
                            must1 = 2;
                            must2 = 5;
                            must3 = 8;
                            break;
                    }
                    for (i = 0; i < MAXROOMS; i++) {
                        int topRow = MIN_ROW + (i / 3) * (nrow / 3);
                        int leftCol = (i % 3) * (ncol / 3) + 1;
                        int height = self.rand.get(4, (nrow / 3) - 1);
                        int width = self.rand.get(7, (ncol / 3) - 4);

                        topRow += self.rand.get((nrow / 3) - height - 1);
                        leftCol += self.rand.get((ncol / 3) - width - 1);
                        boolean isnothing = i != must1 && i != must2 && i != must3 && self.rand.percent(40);
                        room.add(new Room(i, leftCol, topRow, width, height, this, isnothing));
                    }
                    // int nx= 0; for(j= 0; j<MAXROOMS;
                    // j++)if(((Room)room.elementAt(j)).is_room==Room.R_NOTHING)++nx;
                    // System.out.println("Missing " + nx);
                    addMazes();
                    int perm[] = self.rand.permute(MAXROOMS);

                    for (j = 0; j < MAXROOMS; j++) {
                        r = (Room) room.get(perm[j]);
                        connect(r, 1);
                        connect(r, 3);
                        if (r.roomSize + 2 < room.size() && room.get(r.roomSize + 1).isRoom == Room.R_NOTHING) {
                            if (connect(r, 2)) {
                                Room ra = room.get(r.roomSize + 1);
                                ra.isRoom = Room.R_CROSS;
                            }
                        }
                        if (r.roomSize + 6 < room.size() && room.get(r.roomSize + 3).isRoom == Room.R_NOTHING) {
                            if (connect(r, 6)) {
                                Room ra = room.get(r.roomSize + 3);
                                ra.isRoom = Room.R_CROSS;
                            }
                        }
                        if (isAllConnected()) {
                            break;
                        }
                    }
                    fillOutLevel();
                }
                // put_stairs here, but not in a maze room
                Rowcol p = getRandomRowCol(FLOOR, null);
                map[p.row][p.col] |= STAIRS | FLOOR;

                addTraps();
                putToys();

                // Add gold (one lump per room)
                if (levelToys != null) {
                    for (i = 0; i < room.size(); i++) {
                        room.get(i).putGold(GOLD_PERCENT);
                    }
                }

                /*
                 * for(j= 0; j<level_doors.size(); j++){ Door d=
                 * (Door)level_doors.elementAt(j); Room r1= room_at(d.row,
                 * d.col); Room r2= room_at(d.oth.row, d.oth.col);
                 * System.out.print(" " + get_char(d.row,d.col) +
                 * get_char(d.oth.row,d.oth.col));
                 * if(r1!=null)System.out.print(" From" + r1);
                 * if(r2!=null)System.out.print(" To" + r2);
                 * System.out.println(""); }
                 */
                bfinished = true;
            } catch (Exception e) {
                System.out.println("Something failed--redo level " + currentLevel);
                // e.printStackTrace();
                super.init();
            }
        }
    }

    private void addMazes() {
        if (currentLevel > 1) {
            int maze_percent = (currentLevel * 5) / 4;
            if (currentLevel > 15) {
                maze_percent += currentLevel;
            }
            int i = room.size();
            while (--i >= 0) {
                Room rj = room.get(i);
                if (rj.isRoom == Room.R_NOTHING) {
                    if (rogue.rand.percent(maze_percent)) {
                        rj.isRoom = Room.R_MAZE;
                        if (rj.leftCol < 2) {
                            rj.leftCol = 2;
                        }
                        int rv = rogue.rand.get(rj.topRow + 1, rj.bottomRow - 1);
                        int cv = rogue.rand.get(rj.leftCol + 1, rj.rightCol - 1);
                        rj.makeMaze(rv, cv);
                        rj.hideBoxedPassage(rj.topRow, rj.leftCol, rj.bottomRow, rj.rightCol, rogue.rand.get(2));
                    }
                }
            }
        }
    }

    private boolean isAllConnected() {
        int i = room.size();
        while (--i >= 0) {
            Room r = room.get(i);
            r.isRoom &= ~Room.R_VISIT;
        }
        i = room.size();
        while (--i >= 0) {
            Room r = room.get(i);
            if (0 != (r.isRoom & (Room.R_ROOM | Room.R_MAZE))) {
                // Find a real room
                r.visitRooms();
                // Check that all real rooms have been visited
                while (--i >= 0) {
                    r = room.get(i);
                    if (0 != (r.isRoom & (Room.R_ROOM | Room.R_MAZE)) && 0 == (r.isRoom & Room.R_VISIT)) {
                        return false;
                    }
                }
                break;
            }
        }

        return true;
    }

    private void fillOutLevel() {
        int i = room.size();
        int perm[] = rogue.rand.permute(i);

        rogue.endroom = null;
        while (--i >= 0) {
            Room r = room.get(perm[i]);
            if (r != null && (0 != (r.isRoom & Room.R_NOTHING) || (0 != (r.isRoom & Room.R_CROSS) && rogue.rand.coin()))) {
                r.fillIt(true);
            }
        }
        if (rogue.endroom != null)
            rogue.endroom.fillIt(false);
    }

    private Room grRoom() {
        int perm[] = rogue.rand.permute(room.size());
        for (int i = 0; i < room.size(); i++) {
            Room rm = room.get(perm[i]);
            if (0 != (rm.isRoom & (Room.R_ROOM | Room.R_MAZE))) {
                return rm;
            }
        }
        
        return null;
    }

    private void makeParty() {
        partyRoom = grRoom();
        if (partyRoom != null) {
            int n = rogue.rand.percent(99) ? partyRoom.partyToys() : 11;
            if (rogue.rand.percent(99)) {
                partyRoom.partyMonsters(n);
            }
        }
    }

    protected void putToys() {
        if (currentLevel < maxLevel) {
            return;
        }
        if (partyRoom != null) {
            makeParty();
        } else {
            super.putToys();
        }
    }

    private void addTraps() {
        int i, n = 0, tries = 0;
        int row = 0, col = 0;

        if (currentLevel > 2 && currentLevel <= 7) {
            n = rogue.rand.get(2);
        } else if (currentLevel <= 11) {
            n = rogue.rand.get(1, 2);
        } else if (currentLevel <= 16) {
            n = rogue.rand.get(2, 3);
        } else if (currentLevel <= 21) {
            n = rogue.rand.get(2, 4);
        } else if (currentLevel <= AMULET_LEVEL + 2) {
            n = rogue.rand.get(3, 5);
        } else {
            n = rogue.rand.get(5, 10); // Maximum number of traps
        }
        for (i = 0; i < n; i++) {
            if (i == 0 && partyRoom != null) {
                do {
                    row = rogue.rand.get(partyRoom.topRow + 1, partyRoom.bottomRow - 1);
                    col = rogue.rand.get(partyRoom.leftCol + 1, partyRoom.rightCol - 1);
                    tries++;
                } while (0 != (map[row][col] & (TOY | STAIRS | TRAP | TUNNEL)) || (0 == (map[row][col] & SOMETHING) && tries < 15));
            }
            if (tries == 0 || tries >= 15) {
                Rowcol pt = getRandomRowCol(FLOOR | MONSTER, null);
                if (pt != null) {
                    row = pt.row;
                    col = pt.col;
                }
            }
            map[row][col] |= HIDDEN;
            // System.out.println("Trap at " + row + " " + col + " (" +
            // trap.m_flags + ")");
        }
    }

    private boolean connect(Room rfr, int n) {
        Rowcol p1 = null, p2 = null;
        int dir = 0;

        if (rfr.roomSize + n >= room.size()) {
            return false;
        }
        Room rto = room.get(rfr.roomSize + n);
        if (0 == (rfr.isRoom & (Room.R_ROOM | Room.R_MAZE)) || 0 == (rto.isRoom & (Room.R_ROOM | Room.R_MAZE))) {
            return false;
        }

        if (sameRow(rfr, rto)) {
            if (rfr.leftCol > rto.rightCol) {
                p1 = rfr.putDoor(Id.LEFT);
                p2 = rto.putDoor(Id.RIGHT);
                dir = Id.LEFT;
            } else if (rto.leftCol > rfr.rightCol) {
                p1 = rfr.putDoor(Id.RIGHT);
                p2 = rto.putDoor(Id.LEFT);
                dir = Id.RIGHT;
            }
        } else if (sameCol(rfr, rto)) {
            if (rfr.topRow > rto.bottomRow) {
                p1 = rfr.putDoor(Id.UPWARD);
                p2 = rto.putDoor(Id.DOWN);
                dir = Id.UPWARD;
            } else if (rto.topRow > rfr.bottomRow) {
                p1 = rfr.putDoor(Id.DOWN);
                p2 = rto.putDoor(Id.UPWARD);
                dir = Id.DOWN;
            }
        } else {
            return false;
        }
        do {
            rfr.drawSimplePassage(p1.row, p1.col, p2.row, p2.col, dir);
        } while (rogue.rand.percent(4));
        rfr.doors[dir / 2] = new Door(this, p1.row, p1.col, p2.row, p2.col);
        rto.doors[((dir + 4) % Id.DIRS) / 2] = new Door(this, p2.row, p2.col, p1.row, p1.col);
        rfr.doors[dir / 2].connect(rto.doors[((dir + 4) % Id.DIRS) / 2]);
        
        return true;
    }

    @Override
    public boolean sameRow(Room rfr, Room rto) {
        return rfr.roomSize / 3 == rto.roomSize / 3;
    }

    @Override
    public boolean sameCol(Room rfr, Room rto) {
        return rfr.roomSize % 3 == rto.roomSize % 3;
    }

    private Room nthRoom(int n) {
        return n >= 0 && n < room.size() ? (Room) room.get(n) : null;
    }

    @Override
    public Room[] nabes(Room room) {
        Room[] rooms = new Room[4];
        rooms[0] = nthRoom(room.roomSize - 3);
        rooms[1] = nthRoom(room.roomSize + 1);
        rooms[2] = nthRoom(room.roomSize + 3);
        rooms[3] = nthRoom(room.roomSize - 1);

        return rooms;
    }

    public Room roomAt(int row, int col) {
        for (Room r : room) {
            if (r.inRoom(row, col)) {
                return r;
            }
        }

        return null;
    }
}
