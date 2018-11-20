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

    int isRoom;
    int leftCol, rightCol, topRow, bottomRow;
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
        int wi = rightCol - leftCol;
        int hi = bottomRow - topRow;
        String s = new String("[" + rn + "  +" + leftCol + "+" + topRow + " " + wi + "x" + hi);
        if (0 != (isRoom & R_NOTHING)) {
            s = s.concat(" Nothing");
        }
        if (0 != (isRoom & R_ROOM)) {
            s = s.concat(" Room");
        }
        if (0 != (isRoom & R_MAZE)) {
            s = s.concat(" Maze");
        }
        if (0 != (isRoom & R_DEADEND)) {
            s = s.concat(" Deadend");
        }
        if (0 != (isRoom & R_CROSS)) {
            s = s.concat(" Cross");
        }
        if (0 != (isRoom & R_VISIT)) {
            s = s.concat(" Visit");
        }
        
        return s + ']';
    }

    Room() { // A named place-holder
    }

    Room(int rn, int leftCol, int topRow, int width, int height, Level level, boolean isnothing) {
        int darkPct = (level.currentLevel - 2) * 100 / (27 - 2);
        int floormask = FLOOR | HOLDER;
        if (darkPct > 0 && level.rogue.rand.percent(darkPct)) {
            floormask |= DARK;
        }
        this.rn = rn;
        this.level = level;
        this.isRoom = R_NOTHING;
        for (int i = 0; i < 4; i++) {
            doors[i] = null;
        }
        this.leftCol = leftCol;
        this.topRow = topRow;
        bottomRow = topRow + height - 1;
        rightCol = leftCol + width - 1;
        if (!isnothing) {
            this.isRoom = R_ROOM;

            for (int i = topRow + 1; i <= bottomRow - 1; i++) {
                for (int j = leftCol + 1; j <= rightCol - 1; j++) {
                    level.map[i][j] = floormask;
                }
            }

            for (int i = topRow; i <= bottomRow; i++) {
                level.map[i][leftCol] = VERTWALL;
                level.map[i][rightCol] = VERTWALL;
            }
            for (int j = leftCol; j <= rightCol; j++) {
                level.map[topRow][j] = HORWALL;
                level.map[bottomRow][j] = HORWALL;
            }
        }
    }

    void makeMaze(int r, int c) {
        int dirs[] = new int[4];
        int i;

        dirs[0] = Id.UPWARD;
        dirs[1] = Id.DOWN;
        dirs[2] = Id.LEFT;
        dirs[3] = Id.RIGHT;

        level.map[r][c] = TUNNEL | HOLDER;

        if (level.rogue.rand.percent(20))
            level.rogue.rand.permute(dirs);
        for (i = 0; i < 4; i++) {
            switch (dirs[i]) {
                case Id.UPWARD:
                    if (r - 1 >= topRow && level.map[r - 1][c] != (TUNNEL | HOLDER) && level.map[r - 1][c - 1] != (TUNNEL | HOLDER) && level.map[r - 1][c + 1] != (TUNNEL | HOLDER)
                            && level.map[r - 2][c] != (TUNNEL | HOLDER)) {
                        makeMaze(r - 1, c);
                    }
                    break;
                case Id.DOWN:
                    if (r + 1 <= bottomRow && level.map[r + 1][c] != (TUNNEL | HOLDER) && level.map[r + 1][c - 1] != (TUNNEL | HOLDER) && level.map[r + 1][c + 1] != (TUNNEL | HOLDER)
                            && level.map[r + 2][c] != (TUNNEL | HOLDER)) {
                        makeMaze(r + 1, c);
                    }
                    break;
                case Id.LEFT:
                    if (c - 1 >= leftCol && level.map[r][c - 1] != (TUNNEL | HOLDER) && level.map[r - 1][c - 1] != (TUNNEL | HOLDER) && level.map[r + 1][c - 1] != (TUNNEL | HOLDER)
                            && level.map[r][c - 2] != (TUNNEL | HOLDER)) {
                        makeMaze(r, c - 1);
                    }
                    break;
                case Id.RIGHT:
                    if (c + 1 <= rightCol && level.map[r][c + 1] != (TUNNEL | HOLDER) && level.map[r - 1][c + 1] != (TUNNEL | HOLDER) && level.map[r + 1][c + 1] != (TUNNEL | HOLDER)
                            && level.map[r][c + 2] != (TUNNEL | HOLDER)) {
                        makeMaze(r, c + 1);
                    }
                    break;
            }
        }
    }

    void hideBoxedPassage(int row1, int col1, int row2, int col2, int n) {
        int i, j, t;
        int row, col, row_cut, col_cut;
        int h, w;

        if (level.currentLevel > 2) {
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
                        row = level.rogue.rand.get(row1 + row_cut, row2 - row_cut);
                        col = level.rogue.rand.get(col1 + col_cut, col2 - col_cut);
                        if (0 != (level.map[row][col] & TUNNEL)) {
                            level.map[row][col] |= Level.HIDDEN;
                            break;
                        }
                    }
                }
            }
        }
    }

    Rowcol putDoor(int dir) {
        // Actually just returns where the new door will go in the room
        int wallWidth = (isRoom & R_MAZE) != 0 ? 0 : 1;
        int row = -1;
        int col = -1;

        switch (dir) {
            case Id.UPWARD:
                row = topRow;
                break;
            case Id.DOWN:
                row = bottomRow;
                break;
            case Id.RIGHT:
                col = rightCol;
                break;
            case Id.LEFT:
                col = leftCol;
                break;
        }
        if (row >= 0) {
            do {
                col = level.rogue.rand.get(leftCol + wallWidth, rightCol - wallWidth);
            } while (0 == (level.map[row][col] & (Level.HORWALL | TUNNEL)));
        } else {
            do {
                row = level.rogue.rand.get(topRow + wallWidth, bottomRow - wallWidth);
            } while (0 == (level.map[row][col] & (Level.VERTWALL | TUNNEL)));
        }
        if (0 != (isRoom & R_ROOM)) {
            level.map[row][col] = Level.DOOR;
        }
        if ((level.currentLevel > 2) && level.rogue.rand.percent(HIDE_PERCENT)) {
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

    void drawSimplePassage(int row1, int col1, int row2, int col2, int dir) {
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
            middle = level.rogue.rand.get(col1 + 1, col2 - 1);
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
            middle = level.rogue.rand.get(row1 + 1, row2 - 1);
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
        if (level.rogue.rand.percent(HIDE_PERCENT)) {
            hideBoxedPassage(row1, col1, row2, col2, 1);
        }
    }

    boolean valid() {
        return isRoom == R_ROOM;
    }

    void visitRooms() {
        Room r = this;

        r.isRoom |= R_VISIT;
        for (int i = 0; i < 4; i++) {
            if (doors[i] != null) {
                Room roth = doors[i].otherRoom();
                if (roth == null) {
                    continue;
                }
                if (0 == (roth.isRoom & R_VISIT)) {
                    roth.visitRooms();
                }
            }
        }
    }

    private void recursiveDeadend(int srow, int scol) {
        int i;
        int drow, dcol, tunnel_dir = 0;
        Room[] nabes = level.nabes(this);

        level.rogue.rand.permute((Object[]) nabes);
        isRoom = R_DEADEND;
        if (0 == (level.map[srow][scol] & (HORWALL | VERTWALL | FLOOR | HOLDER | STAIRS))) {
            level.map[srow][scol] = TUNNEL;
        } else {
            return;
        }
        for (i = 0; i < 4; i++) {
            Room r = nabes[i];
            if (r == null || 0 == (r.isRoom & R_NOTHING)) {
                continue;
            }
            if (level.sameRow(this, r)) {
                tunnel_dir = leftCol < r.leftCol ? Id.RIGHT : Id.LEFT;
            } else if (level.sameCol(this, r)) {
                tunnel_dir = topRow < r.topRow ? Id.DOWN : Id.UPWARD;
            } else {
                continue;
            }
            drow = (r.topRow + r.bottomRow) / 2;
            dcol = (r.leftCol + r.rightCol) / 2;
            drawSimplePassage(srow, scol, drow, dcol, tunnel_dir);
            level.rogue.endroom = r;
            r.recursiveDeadend(drow, dcol);
        }
    }

    private Rowcol maskRoom(int mask) {
        for (int i = topRow; i <= bottomRow; i++) {
            for (int j = leftCol; j <= rightCol; j++) {
                if (0 != (level.map[i][j] & mask)) {
                    return new Rowcol(i, j);
                }
            }
        }
        
        return null;
    }

    void fillIt(boolean doRecDe) {
        Rowcol ps = null;
        int i;
        int tunnelDir;
        int doorDir;
        int roomsFound = 0;
        boolean didThis = false;
        Room[] nabes = level.nabes(this);

        level.rogue.rand.permute((Object[]) nabes);
        for (i = 0; i < 4; i++) {
            Room r = nabes[i];
            if (r == null || 0 == (r.isRoom & (R_ROOM | R_MAZE))) {
                continue;
            }
            if (level.sameRow(this, r)) {
                tunnelDir = leftCol < r.leftCol ? Id.RIGHT : Id.LEFT;
            } else if (level.sameCol(this, r)) {
                tunnelDir = topRow < r.topRow ? Id.DOWN : Id.UPWARD;
            } else {
                continue;
            }
            doorDir = ((tunnelDir + 4) % Id.DIRS);
            Door dr = doors[doorDir / 2];
            if (dr != null && dr.otherRoom() != null) {
                continue;
            }
            if (!doRecDe || didThis || ps == null) {
                ps = maskRoom(TUNNEL);
                if (ps == null) {
                    ps = new Rowcol((topRow + bottomRow) / 2, (leftCol + rightCol) / 2);
                }
            }
            Rowcol pd = r.putDoor(doorDir);
            roomsFound++;
            drawSimplePassage(ps.row, ps.col, pd.row, pd.col, tunnelDir);
            isRoom = R_DEADEND;
            level.map[ps.row][ps.col] = TUNNEL;
            /*
             * if(i < 3 && !did_this){ did_this= true;
             * if(level.self.rand.coin()) continue; }
             */
            if (roomsFound < 2 && doRecDe) {
                recursiveDeadend(ps.row, ps.col);
            }
            break;
        }
    }

    boolean noRoomForMonster() {
        for (int i = topRow + 1; i < bottomRow; i++) {
            for (int j = leftCol + 1; j < rightCol; j++) {
                if (0 == (level.map[i][j] & MONSTER)) {
                    return false;
                }
            }
        }
        
        return true;
    }

    int partyToys() {
        int nf = 0;
        int area = (bottomRow - topRow - 1) * (rightCol - leftCol - 1);
        int n = level.rogue.rand.get(5, 10);
        if (n >= area) {
            n = area - 2;
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 250; j++) {
                int r = level.rogue.rand.get(topRow + 1, bottomRow - 1);
                int c = level.rogue.rand.get(leftCol + 1, rightCol - 1);
                if (0 != (level.map[r][c] & HOLDER)) {
                    Toy obj = level.getRandomToy();
                    obj.placeAt(r, c, TOY);
                    nf++;
                    break;
                }
            }
        }
        
        return nf;
    }

    void partyMonsters(int n) {
        n += n;
        int d = level.currentLevel % 3;
        for (int i = 0; i < Monster.MONSTERS; i++) {
            Monster.MONSTER_TABLE[i].firstLevel -= d;
        }
        for (int i = 0; i < n && !noRoomForMonster(); i++) {
            for (int j = 0; j < 250; j++) {
                int row = level.rogue.rand.get(topRow + 1, bottomRow - 1);
                int col = level.rogue.rand.get(leftCol + 1, rightCol - 1);
                if (0 == (level.map[row][col] & MONSTER) && 0 != (level.map[row][col] & HOLDER)) {
                    Monster monster = level.getRandomMonster();
                    if (0 == (monster.mFlags & Monster.IMITATES)) {
                        monster.mFlags |= Monster.WAKENS;
                    }
                    monster.putMonsterAt(row, col);
                    break;
                }
            }
        }
        for (int i = 0; i < Monster.MONSTERS; i++) {
            Monster.MONSTER_TABLE[i].firstLevel += d;
        }
    }

    void putGold(int goldPct) {
        if (0 == (isRoom & (R_MAZE | R_ROOM))) {
            return;
        }
        boolean isMaze = 0 != (isRoom & R_MAZE);
        if (isMaze || level.rogue.rand.percent(goldPct)) {
            for (int j = 0; j < 50; j++) {
                int row = level.rogue.rand.get(topRow + 1, bottomRow - 1);
                int col = level.rogue.rand.get(leftCol + 1, rightCol - 1);
                if (0 != (level.map[row][col] & HOLDER)) {
                    int gold = (isMaze ? 24 : 16) * level.currentLevel;
                    gold = level.rogue.rand.get(gold / 8, gold);
                    level.plantGold(row, col, gold);
                    break;
                }
            }
        }
    }

    boolean inRoom(int row, int col) {
        return leftCol <= col && col <= rightCol && topRow <= row && row <= bottomRow;
    }
}
