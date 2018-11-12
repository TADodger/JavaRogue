package rogue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class NineRoom extends Level implements Serializable {
    private static final long serialVersionUID = 3336671744682766539L;

    List<Room> room = null;
    Room party_room = null;

    final Room PASSAGE = new Room();

    static final int GOLD_PERCENT = 46;
    static final int MAXROOMS = 9;
    static final int BIG_ROOM = 10;

    NineRoom(int nrow, int ncol, Rogue self) {
        super(nrow, ncol, self);
        PASSAGE.rn = -1;
        int i;
        int j;
        boolean bfinished = false;

        if (cur_level < LAST_map) {
            self.cur_level++;
            cur_level = self.cur_level;
        }
        if (cur_level > max_level) {
            self.max_level = cur_level;
            max_level = self.max_level;
        }

        while (!bfinished) {
            try {
                int must_1 = 0, must_2 = 0, must_3 = 0;
                Room r;

                if (cur_level > 1 && self.rand.percent(8)) {
                    party_room = PASSAGE; // Flag set up party room
                }

                boolean big_room = ((party_room != null) && self.rand.percent(1));
                if (big_room) {
                    room = new ArrayList<>(1);

                    int top_row = self.rand.get(MIN_ROW, MIN_ROW + 5);
                    int height = self.rand.get(nrow - 7, nrow - 2) - top_row;
                    int left_col = self.rand.get(1, 10);
                    ;
                    int width = self.rand.get(ncol - 11, ncol - 1) - left_col;
                    room.add(new Room(BIG_ROOM, left_col, top_row, width, height, this, false));
                } else {
                    room = new ArrayList<>(MAXROOMS);

                    // Required rooms (need one full row or one full column)
                    switch (self.rand.get(5)) {
                        case 0:
                            must_1 = 0;
                            must_2 = 1;
                            must_3 = 2;
                            break;
                        case 1:
                            must_1 = 3;
                            must_2 = 4;
                            must_3 = 5;
                            break;
                        case 2:
                            must_1 = 6;
                            must_2 = 7;
                            must_3 = 8;
                            break;
                        case 3:
                            must_1 = 0;
                            must_2 = 3;
                            must_3 = 6;
                            break;
                        case 4:
                            must_1 = 1;
                            must_2 = 4;
                            must_3 = 7;
                            break;
                        case 5:
                            must_1 = 2;
                            must_2 = 5;
                            must_3 = 8;
                            break;
                    }
                    for (i = 0; i < MAXROOMS; i++) {
                        int top_row = MIN_ROW + (i / 3) * (nrow / 3);
                        int left_col = (i % 3) * (ncol / 3) + 1;
                        int height = self.rand.get(4, (nrow / 3) - 1);
                        int width = self.rand.get(7, (ncol / 3) - 4);

                        top_row += self.rand.get((nrow / 3) - height - 1);
                        left_col += self.rand.get((ncol / 3) - width - 1);
                        boolean isnothing = i != must_1 && i != must_2 && i != must_3 && self.rand.percent(40);
                        room.add(new Room(i, left_col, top_row, width, height, this, isnothing));
                    }
                    // int nx= 0; for(j= 0; j<MAXROOMS;
                    // j++)if(((Room)room.elementAt(j)).is_room==Room.R_NOTHING)++nx;
                    // System.out.println("Missing " + nx);
                    add_mazes();
                    int perm[] = self.rand.permute(MAXROOMS);

                    for (j = 0; j < MAXROOMS; j++) {
                        r = (Room) room.get(perm[j]);
                        connect(r, 1);
                        connect(r, 3);
                        if (r.rn + 2 < room.size() && room.get(r.rn + 1).is_room == Room.R_NOTHING) {
                            if (connect(r, 2)) {
                                Room ra = room.get(r.rn + 1);
                                ra.is_room = Room.R_CROSS;
                            }
                        }
                        if (r.rn + 6 < room.size() && room.get(r.rn + 3).is_room == Room.R_NOTHING) {
                            if (connect(r, 6)) {
                                Room ra = room.get(r.rn + 3);
                                ra.is_room = Room.R_CROSS;
                            }
                        }
                        if (is_all_connected()) {
                            break;
                        }
                    }
                    fill_out_level();
                }
                // put_stairs here, but not in a maze room
                Rowcol p = gr_row_col(FLOOR, null);
                map[p.row][p.col] |= STAIRS | FLOOR;

                add_traps();
                put_toys();

                // Add gold (one lump per room)
                if (level_toys != null) {
                    for (i = 0; i < room.size(); i++) {
                        room.get(i).put_gold(GOLD_PERCENT);
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
                System.out.println("Something failed--redo level " + cur_level);
                // e.printStackTrace();
                super.init();
            }
        }
    }

    private void add_mazes() {
        if (cur_level > 1) {
            int maze_percent = (cur_level * 5) / 4;
            if (cur_level > 15) {
                maze_percent += cur_level;
            }
            int i = room.size();
            while (--i >= 0) {
                Room rj = room.get(i);
                if (rj.is_room == Room.R_NOTHING) {
                    if (self.rand.percent(maze_percent)) {
                        rj.is_room = Room.R_MAZE;
                        if (rj.left_col < 2) {
                            rj.left_col = 2;
                        }
                        int rv = self.rand.get(rj.top_row + 1, rj.bottom_row - 1);
                        int cv = self.rand.get(rj.left_col + 1, rj.right_col - 1);
                        rj.make_maze(rv, cv);
                        rj.hide_boxed_passage(rj.top_row, rj.left_col, rj.bottom_row, rj.right_col, self.rand.get(2));
                    }
                }
            }
        }
    }

    boolean is_all_connected() {
        int i = room.size();
        while (--i >= 0) {
            Room r = room.get(i);
            r.is_room &= ~Room.R_VISIT;
        }
        i = room.size();
        while (--i >= 0) {
            Room r = room.get(i);
            if (0 != (r.is_room & (Room.R_ROOM | Room.R_MAZE))) {
                // Find a real room
                r.visit_rooms();
                // Check that all real rooms have been visited
                while (--i >= 0) {
                    r = room.get(i);
                    if (0 != (r.is_room & (Room.R_ROOM | Room.R_MAZE)) && 0 == (r.is_room & Room.R_VISIT)) {
                        return false;
                    }
                }
                break;
            }
        }

        return true;
    }

    void fill_out_level() {
        int i = room.size();
        int perm[] = self.rand.permute(i);

        self.endroom = null;
        while (--i >= 0) {
            Room r = room.get(perm[i]);
            if (r != null && (0 != (r.is_room & Room.R_NOTHING) || (0 != (r.is_room & Room.R_CROSS) && self.rand.coin()))) {
                r.fill_it(true);
            }
        }
        if (self.endroom != null)
            self.endroom.fill_it(false);
    }

    Room gr_room() {
        int perm[] = self.rand.permute(room.size());
        for (int i = 0; i < room.size(); i++) {
            Room rm = room.get(perm[i]);
            if (0 != (rm.is_room & (Room.R_ROOM | Room.R_MAZE))) {
                return rm;
            }
        }
        
        return null;
    }

    void make_party() {
        party_room = gr_room();
        if (party_room != null) {
            int n = self.rand.percent(99) ? party_room.party_toys() : 11;
            if (self.rand.percent(99)) {
                party_room.party_monsters(n);
            }
        }
    }

    void put_toys() {
        if (cur_level < max_level) {
            return;
        }
        if (party_room != null) {
            make_party();
        } else {
            super.put_toys();
        }
    }

    void add_traps() {
        int i, n = 0, tries = 0;
        int row = 0, col = 0;

        if (cur_level > 2 && cur_level <= 7) {
            n = self.rand.get(2);
        } else if (cur_level <= 11) {
            n = self.rand.get(1, 2);
        } else if (cur_level <= 16) {
            n = self.rand.get(2, 3);
        } else if (cur_level <= 21) {
            n = self.rand.get(2, 4);
        } else if (cur_level <= AMULET_LEVEL + 2) {
            n = self.rand.get(3, 5);
        } else {
            n = self.rand.get(5, 10); // Maximum number of traps
        }
        for (i = 0; i < n; i++) {
            if (i == 0 && party_room != null) {
                do {
                    row = self.rand.get(party_room.top_row + 1, party_room.bottom_row - 1);
                    col = self.rand.get(party_room.left_col + 1, party_room.right_col - 1);
                    tries++;
                } while (0 != (map[row][col] & (TOY | STAIRS | TRAP | TUNNEL)) || (0 == (map[row][col] & SOMETHING) && tries < 15));
            }
            if (tries == 0 || tries >= 15) {
                Rowcol pt = gr_row_col(FLOOR | MONSTER, null);
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

    boolean connect(Room rfr, int n) {
        Rowcol p1 = null, p2 = null;
        int dir = 0;

        if (rfr.rn + n >= room.size()) {
            return false;
        }
        Room rto = room.get(rfr.rn + n);
        if (0 == (rfr.is_room & (Room.R_ROOM | Room.R_MAZE)) || 0 == (rto.is_room & (Room.R_ROOM | Room.R_MAZE))) {
            return false;
        }

        if (same_row(rfr, rto)) {
            if (rfr.left_col > rto.right_col) {
                p1 = rfr.put_door(Id.LEFT);
                p2 = rto.put_door(Id.RIGHT);
                dir = Id.LEFT;
            } else if (rto.left_col > rfr.right_col) {
                p1 = rfr.put_door(Id.RIGHT);
                p2 = rto.put_door(Id.LEFT);
                dir = Id.RIGHT;
            }
        } else if (same_col(rfr, rto)) {
            if (rfr.top_row > rto.bottom_row) {
                p1 = rfr.put_door(Id.UPWARD);
                p2 = rto.put_door(Id.DOWN);
                dir = Id.UPWARD;
            } else if (rto.top_row > rfr.bottom_row) {
                p1 = rfr.put_door(Id.DOWN);
                p2 = rto.put_door(Id.UPWARD);
                dir = Id.DOWN;
            }
        } else
            return false;
        do {
            rfr.draw_simple_passage(p1.row, p1.col, p2.row, p2.col, dir);
        } while (self.rand.percent(4));
        rfr.doors[dir / 2] = new Door(this, p1.row, p1.col, p2.row, p2.col);
        rto.doors[((dir + 4) % Id.DIRS) / 2] = new Door(this, p2.row, p2.col, p1.row, p1.col);
        rfr.doors[dir / 2].connect(rto.doors[((dir + 4) % Id.DIRS) / 2]);
        
        return true;
    }

    boolean same_row(Room rfr, Room rto) {
        return rfr.rn / 3 == rto.rn / 3;
    }

    boolean same_col(Room rfr, Room rto) {
        return rfr.rn % 3 == rto.rn % 3;
    }

    private Room nth_room(int n) {
        return n >= 0 && n < room.size() ? (Room) room.get(n) : null;
    }

    Room nabes(Room r)[] {
        Room ra[] = new Room[4];
        ra[0] = nth_room(r.rn - 3);
        ra[1] = nth_room(r.rn + 1);
        ra[2] = nth_room(r.rn + 3);
        ra[3] = nth_room(r.rn - 1);
        return ra;
    }

    Room room_at(int row, int col) {
        for (Room r : room) {
            if (r.in_room(row, col))
                return r;
        }
        return null;
    }
}
