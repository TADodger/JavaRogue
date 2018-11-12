package rogue;

import java.io.Serializable;

/**
 *
 */
public class Room implements Header, Serializable {
    private static final long serialVersionUID = 7381179944811621845L;

    Level level;
    Item any;
    // Room endroom;

    int is_room;
    int left_col, right_col, top_row, bottom_row;
    Door doors[] = new Door[4];

    int rn; /* Room location classification */

    static final int HIDE_PERCENT = 12;
    static final int R_NOTHING = 01;
    static final int R_ROOM = 02;
    static final int R_MAZE = 04;
    static final int R_DEADEND = 010;
    static final int R_CROSS = 020;
    static final int R_VISIT = 040;

    public String toString() {
        int wi = right_col - left_col;
        int hi = bottom_row - top_row;
        String s = new String("[" + rn + "  +" + left_col + "+" + top_row + " " + wi + "x" + hi);
        if (0 != (is_room & R_NOTHING)) {
            s = s.concat(" Nothing");
        }
        if (0 != (is_room & R_ROOM)) {
            s = s.concat(" Room");
        }
        if (0 != (is_room & R_MAZE)) {
            s = s.concat(" Maze");
        }
        if (0 != (is_room & R_DEADEND)) {
            s = s.concat(" Deadend");
        }
        if (0 != (is_room & R_CROSS)) {
            s = s.concat(" Cross");
        }
        if (0 != (is_room & R_VISIT)) {
            s = s.concat(" Visit");
        }
        
        return s + ']';
    }

    Room() { // A named place-holder
    }

    Room(int rn, int left_col, int top_row, int width, int height, Level level, boolean isnothing) {
        int dark_pct = (level.cur_level - 2) * 100 / (27 - 2);
        int floormask = FLOOR | HOLDER;
        if (dark_pct > 0 && level.self.rand.percent(dark_pct)) {
            floormask |= DARK;
        }
        this.rn = rn;
        this.level = level;
        this.is_room = R_NOTHING;
        for (int i = 0; i < 4; i++) {
            doors[i] = null;
        }
        this.left_col = left_col;
        this.top_row = top_row;
        bottom_row = top_row + height - 1;
        right_col = left_col + width - 1;
        if (!isnothing) {
            this.is_room = R_ROOM;

            for (int i = top_row + 1; i <= bottom_row - 1; i++) {
                for (int j = left_col + 1; j <= right_col - 1; j++) {
                    level.map[i][j] = floormask;
                }
            }

            for (int i = top_row; i <= bottom_row; i++) {
                level.map[i][left_col] = VERTWALL;
                level.map[i][right_col] = VERTWALL;
            }
            for (int j = left_col; j <= right_col; j++) {
                level.map[top_row][j] = HORWALL;
                level.map[bottom_row][j] = HORWALL;
            }
        }
    }

    void make_maze(int r, int c) {
        int dirs[] = new int[4];
        int i;

        dirs[0] = Id.UPWARD;
        dirs[1] = Id.DOWN;
        dirs[2] = Id.LEFT;
        dirs[3] = Id.RIGHT;

        level.map[r][c] = TUNNEL | HOLDER;

        if (level.self.rand.percent(20))
            level.self.rand.permute(dirs);
        for (i = 0; i < 4; i++) {
            switch (dirs[i]) {
                case Id.UPWARD:
                    if (r - 1 >= top_row && level.map[r - 1][c] != (TUNNEL | HOLDER) && level.map[r - 1][c - 1] != (TUNNEL | HOLDER) && level.map[r - 1][c + 1] != (TUNNEL | HOLDER)
                            && level.map[r - 2][c] != (TUNNEL | HOLDER)) {
                        make_maze(r - 1, c);
                    }
                    break;
                case Id.DOWN:
                    if (r + 1 <= bottom_row && level.map[r + 1][c] != (TUNNEL | HOLDER) && level.map[r + 1][c - 1] != (TUNNEL | HOLDER) && level.map[r + 1][c + 1] != (TUNNEL | HOLDER)
                            && level.map[r + 2][c] != (TUNNEL | HOLDER)) {
                        make_maze(r + 1, c);
                    }
                    break;
                case Id.LEFT:
                    if (c - 1 >= left_col && level.map[r][c - 1] != (TUNNEL | HOLDER) && level.map[r - 1][c - 1] != (TUNNEL | HOLDER) && level.map[r + 1][c - 1] != (TUNNEL | HOLDER)
                            && level.map[r][c - 2] != (TUNNEL | HOLDER)) {
                        make_maze(r, c - 1);
                    }
                    break;
                case Id.RIGHT:
                    if (c + 1 <= right_col && level.map[r][c + 1] != (TUNNEL | HOLDER) && level.map[r - 1][c + 1] != (TUNNEL | HOLDER) && level.map[r + 1][c + 1] != (TUNNEL | HOLDER)
                            && level.map[r][c + 2] != (TUNNEL | HOLDER)) {
                        make_maze(r, c + 1);
                    }
                    break;
            }
        }
    }

    void hide_boxed_passage(int row1, int col1, int row2, int col2, int n) {
        int i, j, t;
        int row, col, row_cut, col_cut;
        int h, w;

        if (level.cur_level > 2) {
            if (row1 > row2) {
                t = row1;
                row1 = row2;
                row2 = t;
            }
            if (col1 > col2) {
                t = col1;
                col1 = col2;
                col2 = t;
            }
            h = row2 - row1;
            w = col2 - col1;

            if ((w >= 5) || (h >= 5)) {
                row_cut = ((h >= 2) ? 1 : 0);
                col_cut = ((w >= 2) ? 1 : 0);

                for (i = 0; i < n; i++) {
                    for (j = 0; j < 10; j++) {
                        row = level.self.rand.get(row1 + row_cut, row2 - row_cut);
                        col = level.self.rand.get(col1 + col_cut, col2 - col_cut);
                        if (0 != (level.map[row][col] & TUNNEL)) {
                            level.map[row][col] |= Level.HIDDEN;
                            break;
                        }
                    }
                }
            }
        }
    }

    Rowcol put_door(int dir) {
        // Actually just returns where the new door will go in the room
        int wall_width = (is_room & R_MAZE) != 0 ? 0 : 1;
        int row = -1;
        int col = -1;

        switch (dir) {
            case Id.UPWARD:
                row = top_row;
                break;
            case Id.DOWN:
                row = bottom_row;
                break;
            case Id.RIGHT:
                col = right_col;
                break;
            case Id.LEFT:
                col = left_col;
                break;
        }
        if (row >= 0) {
            do {
                col = level.self.rand.get(left_col + wall_width, right_col - wall_width);
            } while (0 == (level.map[row][col] & (Level.HORWALL | TUNNEL)));
        } else {
            do {
                row = level.self.rand.get(top_row + wall_width, bottom_row - wall_width);
            } while (0 == (level.map[row][col] & (Level.VERTWALL | TUNNEL)));
        }
        if (0 != (is_room & R_ROOM)) {
            level.map[row][col] = Level.DOOR;
        }
        if ((level.cur_level > 2) && level.self.rand.percent(HIDE_PERCENT)) {
            level.map[row][col] |= Level.HIDDEN;
        }
        
        return new Rowcol(row, col);
    }

    boolean settunnel(int r, int c) {
        if (0 != (level.map[r][c] & (HORWALL | VERTWALL | FLOOR | HOLDER | STAIRS))) {
            if (0 != (level.map[r][c] & (HORWALL | VERTWALL))) {
                System.out.print("Added door ");
                level.map[r][c] = DOOR;
            }
            System.out.println(" " + r + " " + c + " (failed in)" + this);

            return false;
        }
        level.map[r][c] = TUNNEL;
        
        return true;
    }

    void draw_simple_passage(int row1, int col1, int row2, int col2, int dir) {
        int i, middle, t;
        if ((dir == Id.LEFT) || (dir == Id.RIGHT)) {
            if (col1 > col2) {
                t = row1;
                row1 = row2;
                row2 = t;
                t = col1;
                col1 = col2;
                col2 = t;
            }
            middle = level.self.rand.get(col1 + 1, col2 - 1);
            for (i = col1 + 1; i != middle; i++) {
                if (!settunnel(row1, i)) {
                    return;
                }
            }
            for (i = row1; i != row2; i += (row1 > row2) ? -1 : 1) {
                if (!settunnel(i, middle)) {
                    return;
                }
            }
            for (i = middle; i != col2; i++) {
                if (!settunnel(row2, i)) {
                    return;
                }
            }
        } else {
            if (row1 > row2) {
                t = row1;
                row1 = row2;
                row2 = t;
                t = col1;
                col1 = col2;
                col2 = t;
            }
            middle = level.self.rand.get(row1 + 1, row2 - 1);
            for (i = row1 + 1; i != middle; i++) {
                if (!settunnel(i, col1)) {
                    return;
                }
            }
            for (i = col1; i != col2; i += (col1 > col2) ? -1 : 1) {
                if (!settunnel(middle, i)) {
                    return;
                }
            }
            for (i = middle; i != row2; i++) {
                if (!settunnel(i, col2)) {
                    return;
                }
            }
        }
        if (level.self.rand.percent(HIDE_PERCENT)) {
            hide_boxed_passage(row1, col1, row2, col2, 1);
        }
    }

    boolean valid() {
        return is_room == R_ROOM;
    }

    void visit_rooms() {
        Room r = this;

        r.is_room |= R_VISIT;
        for (int i = 0; i < 4; i++) {
            if (doors[i] != null) {
                Room roth = doors[i].other_room();
                if (roth == null) {
                    continue;
                }
                if (0 == (roth.is_room & R_VISIT)) {
                    roth.visit_rooms();
                }
            }
        }
    }

    private void recursive_deadend(int srow, int scol) {
        int i;
        int drow, dcol, tunnel_dir = 0;
        Room[] nabes = level.nabes(this);

        level.self.rand.permute((Object[]) nabes);
        is_room = R_DEADEND;
        if (0 == (level.map[srow][scol] & (HORWALL | VERTWALL | FLOOR | HOLDER | STAIRS))) {
            level.map[srow][scol] = TUNNEL;
        } else {
            return;
        }
        for (i = 0; i < 4; i++) {
            Room r = nabes[i];
            if (r == null || 0 == (r.is_room & R_NOTHING)) {
                continue;
            }
            if (level.same_row(this, r)) {
                tunnel_dir = left_col < r.left_col ? Id.RIGHT : Id.LEFT;
            } else if (level.same_col(this, r)) {
                tunnel_dir = top_row < r.top_row ? Id.DOWN : Id.UPWARD;
            } else {
                continue;
            }
            drow = (r.top_row + r.bottom_row) / 2;
            dcol = (r.left_col + r.right_col) / 2;
            draw_simple_passage(srow, scol, drow, dcol, tunnel_dir);
            level.self.endroom = r;
            r.recursive_deadend(drow, dcol);
        }
    }

    private Rowcol mask_room(int mask) {
        for (int i = top_row; i <= bottom_row; i++) {
            for (int j = left_col; j <= right_col; j++) {
                if (0 != (level.map[i][j] & mask)) {
                    return new Rowcol(i, j);
                }
            }
        }
        
        return null;
    }

    void fill_it(boolean do_rec_de) {
        Rowcol ps = null;
        int i;
        int tunnel_dir;
        int door_dir;
        int rooms_found = 0;
        boolean did_this = false;
        Room[] nabes = level.nabes(this);

        level.self.rand.permute((Object[]) nabes);
        for (i = 0; i < 4; i++) {
            Room r = nabes[i];
            if (r == null || 0 == (r.is_room & (R_ROOM | R_MAZE))) {
                continue;
            }
            if (level.same_row(this, r)) {
                tunnel_dir = left_col < r.left_col ? Id.RIGHT : Id.LEFT;
            } else if (level.same_col(this, r)) {
                tunnel_dir = top_row < r.top_row ? Id.DOWN : Id.UPWARD;
            } else {
                continue;
            }
            door_dir = ((tunnel_dir + 4) % Id.DIRS);
            Door dr = doors[door_dir / 2];
            if (dr != null && dr.other_room() != null) {
                continue;
            }
            if (!do_rec_de || did_this || ps == null) {
                ps = mask_room(TUNNEL);
                if (ps == null) {
                    ps = new Rowcol((top_row + bottom_row) / 2, (left_col + right_col) / 2);
                }
            }
            Rowcol pd = r.put_door(door_dir);
            rooms_found++;
            draw_simple_passage(ps.row, ps.col, pd.row, pd.col, tunnel_dir);
            is_room = R_DEADEND;
            level.map[ps.row][ps.col] = TUNNEL;
            /*
             * if(i < 3 && !did_this){ did_this= true;
             * if(level.self.rand.coin()) continue; }
             */
            if (rooms_found < 2 && do_rec_de) {
                recursive_deadend(ps.row, ps.col);
            }
            break;
        }
    }

    boolean no_room_for_monster() {
        for (int i = top_row + 1; i < bottom_row; i++) {
            for (int j = left_col + 1; j < right_col; j++) {
                if (0 == (level.map[i][j] & MONSTER)) {
                    return false;
                }
            }
        }
        
        return true;
    }

    int party_toys() {
        int nf = 0;
        int area = (bottom_row - top_row - 1) * (right_col - left_col - 1);
        int n = level.self.rand.get(5, 10);
        if (n >= area) {
            n = area - 2;
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 250; j++) {
                int r = level.self.rand.get(top_row + 1, bottom_row - 1);
                int c = level.self.rand.get(left_col + 1, right_col - 1);
                if (0 != (level.map[r][c] & HOLDER)) {
                    Toy obj = level.gr_toy();
                    obj.place_at(r, c, TOY);
                    nf++;
                    break;
                }
            }
        }
        
        return nf;
    }

    void party_monsters(int n) {
        n += n;
        int d = level.cur_level % 3;
        for (int i = 0; i < Monster.MONSTERS; i++) {
            Monster.MON_TAB[i].first_level -= d;
        }
        for (int i = 0; i < n && !no_room_for_monster(); i++) {
            for (int j = 0; j < 250; j++) {
                int row = level.self.rand.get(top_row + 1, bottom_row - 1);
                int col = level.self.rand.get(left_col + 1, right_col - 1);
                if (0 == (level.map[row][col] & MONSTER) && 0 != (level.map[row][col] & HOLDER)) {
                    Monster monster = level.gr_monster();
                    if (0 == (monster.m_flags & Monster.IMITATES)) {
                        monster.m_flags |= Monster.WAKENS;
                    }
                    monster.put_m_at(row, col);
                    break;
                }
            }
        }
        for (int i = 0; i < Monster.MONSTERS; i++) {
            Monster.MON_TAB[i].first_level += d;
        }
    }

    void put_gold(int gold_pct) {
        if (0 == (is_room & (R_MAZE | R_ROOM))) {
            return;
        }
        boolean is_maze = 0 != (is_room & R_MAZE);
        if (is_maze || level.self.rand.percent(gold_pct)) {
            for (int j = 0; j < 50; j++) {
                int row = level.self.rand.get(top_row + 1, bottom_row - 1);
                int col = level.self.rand.get(left_col + 1, right_col - 1);
                if (0 != (level.map[row][col] & HOLDER)) {
                    int gold = (is_maze ? 24 : 16) * level.cur_level;
                    gold = level.self.rand.get(gold / 8, gold);
                    level.plant_gold(row, col, gold);
                    break;
                }
            }
        }
    }

    boolean in_room(int row, int col) {
        return left_col <= col && col <= right_col && top_row <= row && row <= bottom_row;
    }
}
