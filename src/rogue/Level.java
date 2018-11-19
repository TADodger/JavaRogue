package rogue;

import java.io.Serializable;
import java.util.Iterator;

/**
 *
 */
public class Level implements Header, Serializable {
    private static final long serialVersionUID = -8320125298348249127L;

    /* ItemVector level_men; */
    Rogue self;
    Item dummy;
    ItemList levelMen;
    ItemList levelToys;
    ItemList levelMonsters;
    ItemList levelTraps;
    ItemList levelDoors;
    int map[][];
    int ncol; // Width
    int nrow; // Height
    // static int cur_level; // Level number
    // static int max_level;
    int currentLevel;
    int maxLevel;
    int myLevel;
    int foods = 0; // Higher levels get more food

    Level(int nrow, int ncol, Rogue self) {
        this.self = self;
        this.nrow = nrow;
        this.ncol = ncol;
        this.currentLevel = self.currentLevel;
        this.maxLevel = self.maxLevel;
        init();
    }

    void init() {
        map = new int[nrow][ncol];

        for (int r = 0; r < nrow; r++) {
            for (int c = 0; c < ncol; c++) {
                map[r][c] = 0;
            }
        }

        levelMonsters = new ItemList(6); // Start with a few monsters
        levelDoors = new ItemList(8); // and a few doors too
        levelMen = new ItemList();
        levelToys = new ItemList(6);
        levelTraps = new ItemList(3);
    }

    void mark(int r, int c) {
        int i = levelMen.size();
        while (--i >= 0) {
            ((Man) levelMen.get(i)).view.mark(r, c);
        }
    }

    char getChar(int row, int col) {
        if (row < 0 || row >= nrow || col < 0 || col >= ncol) {
            return (char) 0;
        }
        int mask = map[row][col];

        if (0 != (mask & TOY)) {
            Toy toy = (Toy) levelToys.itemAt(row, col);
            return toy == null ? ';' : (char) Id.getMaskCharacter(toy.kind);
        }
        /* Not allowing hidden stairs */
        if (0 != (mask & STAIRS))
            return '%';
        if (0 != (mask & (TUNNEL | STAIRS | HORWALL | VERTWALL | FLOOR | DOOR))) {
            if (0 == (mask & HIDDEN))
                if (0 != (mask & TUNNEL))
                    return '#';

            if (0 != (mask & HORWALL))
                return '-';
            if (0 != (mask & VERTWALL))
                return '|';
            if (0 != (mask & FLOOR))
                return (mask & (HIDDEN | TRAP)) == TRAP ? '^' : '.';
            if (0 != (mask & DOOR)) {
                if (0 == (mask & HIDDEN))
                    return '+';

                /* Hidden door: */
                if (0 != (mask & TUNNEL)) {
                    // return ' ';FOOP
                    return '$';
                }
                if (col <= 0 || col >= ncol - 1)
                    return '|';
                if (0 != (map[row][col - 1] & HORWALL) || 0 != (map[row][col + 1] & HORWALL))
                    return '-';
                return '|';
            }
        }
        if (0 != (mask & TUNNEL))
            return '$';// FOOP
        return ' ';
    }

    boolean isPassable(int row, int col) {
        if (row < MIN_ROW || row > (nrow - 2) || col < 0 || col > (ncol - 1)) {
            return false;
        }
        if (0 != (map[row][col] & HIDDEN)) {
            return 0 != (map[row][col] & TRAP);
        }
        
        return 0 != (map[row][col] & (FLOOR | TUNNEL | DOOR | STAIRS | TRAP));
    }

    boolean canTurn(int row, int col) {
        return 0 != (TUNNEL & map[row][col]) && isPassable(row, col);
    }

    boolean canMove(int row1, int col1, int row2, int col2) {
        if (!isPassable(row2, col2)) {
            return false;
        }
        if (row1 != row2 && col1 != col2) {
            if (0 != ((map[row1][col1] | map[row2][col2]) & DOOR)) {
                return false;
            }
            if (0 == map[row1][col2] || 0 == map[row2][col1]) {
                return false;
            }
        }
        return true;
    }

    // Get a point in a random room--avoid itm
    Rowcol grRowCol(int mask, Item itm) {
        int r = 0, c = 0;
        mask |= HOLDER | DARK;
        int ntry = 2400;
        do {
            if (--ntry > 0) {
                r = self.rand.get(MIN_ROW, nrow - 2);
                c = self.rand.get(ncol - 1);
            } else if (ntry == 0) {
                r = nrow - 2;
                c = ncol - 1;
            } else if (--c < 0) {
                c = ncol - 1;
                if (--r < 0) {
                    return null;
                }
            }
        } while (0 == (map[r][c] & mask) || 0 != (map[r][c] & (~mask)) || 0 == (map[r][c] & HOLDER) || (itm != null && r == itm.row && c == itm.col));
        
        return new Rowcol(r, c);
    }

    void plantGold(int row, int col, int gold) {
        Toy obj = new Toy(this, Id.GOLD);
        obj.quantity = gold;
        obj.placeAt(row, col, TOY);
    }

    Scroll grScroll() {
        Scroll t = new Scroll(this);
        t.kind = Id.getRandomWhichScroll(self.rand);
        
        return t;
    }

    Potion grPotion() {
        Potion t = new Potion(this);
        t.kind = Id.getRandomWhichPotion(self.rand);
        
        return t;
    }

    Toy grWeapon(int assignWk) {
        if (assignWk < 0) {
            assignWk = self.rand.get(Id.idWeapons.length - 1);
        }
        
        return new Toy(this, Id.WEAPON | assignWk);
    }

    Toy grArmor() {
        return new Toy(this, Id.ARMOR + self.rand.get(Id.idArmors.length - 1));
    }

    Toy grWand() {
        return new Toy(this, Id.WAND + self.rand.get(Id.idWands.length - 1));
    }

    Toy grWing(int assignWk) {
        if (assignWk < 0) {
            assignWk = self.rand.get(Id.idRings.length - 1);
        }
        
        return new Toy(this, Id.RING + assignWk);
    }

    Toy getFood(boolean forceRation) {
        return new Toy(this, forceRation || self.rand.percent(80) ? Id.RATION : Id.FRUIT);
    }

    Toy grToy() {
        int k;
        if (foods < currentLevel / 3) {
            k = Id.FOOD;
            foods++;
        } else
            k = Id.getRandomSpecies(self.rand);
        switch (k) {
            case Id.SCROLL:
                return (Toy) grScroll();
            case Id.POTION:
                return (Toy) grPotion();
            case Id.WEAPON:
                return (Toy) grWeapon(-1);
            case Id.ARMOR:
                return (Toy) grArmor();
            case Id.WAND:
                return (Toy) grWand();
            case Id.FOOD:
                return (Toy) getFood(false);
            case Id.RING:
                return (Toy) grWing(-1);
        }
        return null;
    }

    Toy wiztoy(Man man, int ch) {
        Toy t = null;
        int max = 0;
        switch (ch) {
            case '!':
                t = (Toy) grPotion();
                max = Id.idPotions.length;
                break;
            case '?':
                t = (Toy) grScroll();
                max = Id.idScrolls.length;
                break;
            case ',':
                t = new Toy(this, Id.AMULET);
                break;
            case ':':
                t = (Toy) getFood(false);
                break;
            case ')':
                max = Id.idWeapons.length;
                break;
            case ']':
                t = (Toy) grArmor();
                max = Id.idArmors.length;
                break;
            case '/':
                t = (Toy) grWand();
                max = Id.idWands.length;
                break;
            case '=':
                max = Id.idRings.length;
                break;
        }
        --max;
        if (ch == ',' || ch == ':') {
            return t;
        }
        if (max < 0) {
            return null;
        }
        String buf = man.view.msg.getInputLine("which kind of " + ((char) ch) + " (0 to " + max + ")?", "", "", false, true);
        man.view.msg.checkMessage();
        if (buf != null) {
            int wk = -1;
            try {
                wk = Integer.parseInt(buf);
            } catch (NumberFormatException e) {
            }
            if (wk >= 0 && wk <= max) {
                if (ch == '=') {
                    t = grWing(wk);
                } else if (ch == ')') {
                    t = grWeapon(wk);
                } else if (t != null) {
                    t.kind = (t.kind & Id.ALL_TOYS) + wk;
                }
                
                return t;
            }
        }
        
        return null;
    }

    void putToys() {
        if (currentLevel < maxLevel) {
            return;
        }
        int n = self.rand.get(2, 4);
        if (self.rand.coin()) {
            n++;
        }
        while (self.rand.percent(33)) {
            n++;
        }
        for (int i = 0; i < n; i++) {
            Rowcol point = grRowCol(FLOOR | TUNNEL, null);
            if (point != null) {
                Toy t = grToy();
                t.placeAt(point.row, point.col, TOY);
            }
        }
    }

    Rowcol getDirRc(int dir, int row, int col, boolean allowOffScreen) {
        switch (dir) {
            case Id.UPLEFT:
                --row;
            case Id.LEFT:
                --col;
                break;
            case Id.DOWNLEFT:
                --col;
            case Id.DOWN:
                ++row;
                break;
            case Id.UPRIGHT:
                ++col;
            case Id.UPWARD:
                --row;
                break;
            case Id.DOWNRIGHT:
                ++row;
            case Id.RIGHT:
                ++col;
                break;
        }
        if (allowOffScreen || (row > MIN_ROW && row < nrow - 2 && col > 0 && col < ncol - 1)) {
            return new Rowcol(row, col);
        }
        
        return null;
    }

    void putPlayer(Man man) {
        /* try not to put where he can see his current position */
        int misses = 2;
        Rowcol pt;
        do {
            pt = grRowCol(FLOOR | TUNNEL | TOY | STAIRS, man);
            if (pt == null) {
                return;
            }
        } while (--misses >= 0 && sees(pt.row, pt.col, man.row, man.col));
        man.placeAt(pt.row, pt.col, MAN);
        wakeRoom(man, true, pt.row, pt.col);
        if (man.newLevelMessage != null) {
            self.tell(man, man.newLevelMessage, false);
            man.newLevelMessage = null;
        }
    }

    void drawMagicMap(Man man) {
        for (int i = 0; i < nrow; i++)
            for (int j = 0; j < ncol; j++) {
                int s = map[i][j];
                if (0 != (s & (HORWALL | VERTWALL | DOOR | TUNNEL | TRAP | STAIRS))) {
                    if (0 == (s & MONSTER)) {
                        man.seen[i][j] = (char) ((man.seen[i][j] & 15) | man.wallcode(getChar(i, j)));
                        man.view.mark(i, j);
                    }
                }
            }
    }

    void unhide() {
        for (int i = 0; i < nrow; i++)
            for (int j = 0; j < ncol; j++)
                if (0 != (map[i][j] & HIDDEN)) {
                    map[i][j] &= ~HIDDEN;
                    mark(i, j);
                }
    }

    Monster getZappedMonster(int dir, int row, int col) {
        for (;;) {
            int ocol = col;
            int orow = row;
            Rowcol pt = getDirRc(dir, row, col, false);
            row = pt.row;
            col = pt.col;
            if ((row == orow && col == ocol) || 0 != (map[row][col] & (HORWALL | VERTWALL)) || 0 == (map[row][col] & SOMETHING)) {
                break;
            }
            if (0 != (map[row][col] & MONSTER)) {
                Monster monster = (Monster) (levelMonsters.itemAt(row, col));
                if (!imitating(row, col))
                    return monster;
            }
        }
        
        return null;
    }

    void wdrainLife(Man man, Monster monster) {
        int hp = man.hpCurrent / 3;
        man.hpCurrent = (man.hpCurrent + 1) / 2;

        for (Item item : levelMonsters) {
            Monster lmon = (Monster) item;
            if (sees(lmon.row, lmon.col, man.row, man.col)) {
                lmon.wakeUp();
                lmon.damage(man, hp, 0);
                monster = null;
            }
        }
        if (monster != null) {
            monster.wakeUp();
            monster.damage(man, hp, 0);
        }
        man.printStat();
        man.view.markall(); // relight
    }

    static int btime;

    void bounce(Toy obj, int dir, int row, int col, int r) {
        boolean fiery = (obj.kind == Id.FIRE);
        int orow;
        int ocol;
        int i;
        int newDirection = -1;
        int damage;
        boolean doend = true;
        Persona owner = obj.owner;

        if (++r == 1) {
            btime = self.rand.get(3, 6);
        } else if (r > btime) {
            return;
        }
        String s = fiery ? "fire" : "ice";
        // if(r > 1)
        // self.view.msg.message("the "+s+" bounces", true);
        self.mdSleep(100);
        orow = row;
        ocol = col;
        Rowcol pt;
        do {
            self.flashadd(orow, ocol, U_BRITE);
            pt = getDirRc(dir, orow, ocol, true);
            orow = pt.row;
            ocol = pt.col;
        } while (!(ocol <= 0 || ocol >= ncol - 1 || 0 == (map[orow][ocol] & SOMETHING) || 0 != (map[orow][ocol] & MONSTER) || 0 != (map[orow][ocol] & (HORWALL | VERTWALL))
                || (orow == owner.row && ocol == owner.col)));
        self.xflash();
        do {
            orow = row;
            ocol = col;
            self.vset(row, col);
            pt = getDirRc(dir, row, col, true);
            row = pt.row;
            col = pt.col;
        } while (!(col <= 0 || col >= ncol - 1 || 0 == (map[row][col] & SOMETHING) || 0 != (map[row][col] & MONSTER) || 0 != (map[row][col] & (HORWALL | VERTWALL))
                || (row == owner.row && col == owner.col)));

        if (0 != (map[row][col] & MONSTER)) {
            Monster monster = (Monster) levelMonsters.itemAt(row, col);
            if (monster != null) {
                doend = monster.zapt(obj);
            }
        } else {
            for (View view : self.viewList) {
                Man man = view.man;
                if (row == man.row && col == man.col) {
                    int ac = man.armor == null ? 0 : man.armor.getArmorClass();
                    if (self.rand.percent(10 + (3 * ac))) {
                        self.describe(man, "the " + s + " misses", false);
                    } else {
                        doend = false;
                        damage = self.rand.get(3, (3 * man.exp));
                        if (fiery) {
                            damage = (damage * 3) / 2;
                            if (man.armor != null)
                                damage -= man.armor.getArmorClass();
                        }
                        man.damage(null, damage, fiery ? Monster.KFIRE : Monster.HYPOTHERMIA);
                        self.describe(man, "the " + s + " hits", false);
                    }
                }
            }
        }
        if (doend) {
            int nr;
            int nc;
            for (i = 0; i < 10; i++) {
                dir = self.rand.get(Id.DIRS - 1);
                nr = orow;
                nc = ocol;
                pt = getDirRc(dir, nr, nc, true);
                nr = pt.row;
                nc = pt.col;
                if ((nc >= 0 && nc <= ncol - 1) && 0 != (map[nr][nc] & SOMETHING) && 0 == (map[nr][nc] & (VERTWALL | HORWALL))) {
                    newDirection = dir;
                    break;
                }
            }
            if (newDirection != -1) {
                bounce(obj, newDirection, orow, ocol, r);
            }
        }
    }

    void putMonsters() {
        int n = self.rand.get(4, 6);

        for (int i = 0; i < n; i++) {
            Rowcol pt = grRowCol(FLOOR | TUNNEL | STAIRS | TOY, null);
            if (pt == null) {
                continue;
            }
            Monster monster = grMonster();
            if (0 != (monster.mFlags & Monster.WANDERS) && self.rand.coin()) {
                monster.wakeUp();
            }
            monster.putMonsterAt(pt.row, pt.col);
            // System.out.println("Monster " + monster);
        }
    }

    Monster grMonster() {
        int mn;

        for (;;) {
            mn = self.rand.get(Monster.MONSTERS - 1);
            if ((currentLevel >= Monster.MONSTER_TABLE[mn].firstLevel) && (currentLevel <= Monster.MONSTER_TABLE[mn].lastLevel)) {
                break;
            }
        }
        Monster monster = new Monster(this, mn);
        if (0 != (monster.mFlags & Monster.IMITATES)) {
            monster.disguise = Id.getRandomObjectCharacter(self.rand);
        }
        if (currentLevel > AMULET_LEVEL + 2) {
            monster.mFlags |= Monster.HASTED;
        }
        monster.trow = -1;
        
        return monster;
    }

    int gmcRowCol(Man man, int row, int col) {
        Monster monster = (Monster) levelMonsters.itemAt(row, col);
        
        return monster != null ? monster.gmc(man) : '&';
    }

    void moveMonsters(Man man) { // Move all the monsters
        if (0 != (man.hasteSelf % 2)) {
            return;
        }
        for (Iterator<Item> it = levelMonsters.iterator();it.hasNext() && !Man.gameOver;) {
            Monster monster = (Monster) it.next();
            monster.dstrow = man.row;
            monster.dstcol = man.col;
            if (0 != (monster.mFlags & Monster.HASTED)) {
                monster.moveMonster();
                if (!levelMonsters.contains(monster)) {
                    continue;
                }
            } else if (0 != (monster.mFlags & Monster.SLOWED)) {
                monster.slowedToggle = !monster.slowedToggle;
                if (monster.slowedToggle) {
                    continue;
                }
            }
            if (0 != (monster.mFlags & Monster.CONFUSED) && monster.moveConfused()) {
                continue;
            }
            boolean flew = false;
            if (0 != (monster.mFlags & Monster.FLIES) && 0 == (monster.mFlags & Monster.NAPPING) && (monster.ihate == null || !monster.monCanGo(monster.ihate.row, monster.ihate.col))) {
                flew = true;
                monster.moveMonster();
                if (!levelMonsters.contains(monster)) {
                    continue;
                }
            }
            if (!(flew && (monster.ihate == null || monster.monCanGo(monster.ihate.row, monster.ihate.col)))) {
                monster.moveMonster();
            }
        }
    }

    void wanderer() {
        for (int i = 0; i < 15; i++) {
            Monster monster = grMonster();
            if (0 != (monster.mFlags & (Monster.WAKENS | Monster.WANDERS))) {
                monster.wakeUp();
                for (i = 0; i < 25; i++) {
                    Rowcol pt = grRowCol(FLOOR | TUNNEL | STAIRS | TOY, null);
                    if (pt != null) {
                        int j = levelMen.size();
                        while (--j >= 0) {
                            Man m = (Man) levelMen.get(j);
                            if (m.canSee(pt.row, pt.col)) {
                                break;
                            }
                        }
                        if (j < 0) {
                            monster.putMonsterAt(pt.row, pt.col);
                            
                            return;
                        }
                    }
                }
                
                return;
            }
        }
    }

    void moveAquatars(Persona man) {
        /* aquatars get to hit early if man removes his armor */
        for (Item item : levelMonsters) {
            Monster monster = (Monster) item;
            if ((monster.ichar == 'A') && monster.monCanGo(man.row, man.col)) {
                monster.ihate = man;
                monster.moveMonster();
                monster.mFlags |= Monster.ALREADY_MOVED;
            }
        }
    }

    boolean imitating(int r, int c) {
        if (0 != (map[r][c] & MONSTER)) {
            Monster monster = (Monster) levelMonsters.itemAt(r, c);
            return monster != null && 0 != (monster.mFlags & Monster.IMITATES);
        }
        
        return false;
    }

    boolean showMonsters(Man man) {
        boolean found = false;
        man.detectMonster = true;
        if (man.blind > 0) {
            return false;
        }
        for (Item item : levelMonsters) {
            Monster monster = (Monster) item;
            man.view.addch(monster.row, monster.col, monster.ichar);
            if (0 != (monster.mFlags & Monster.IMITATES)) {
                monster.mFlags &= ~Monster.IMITATES;
                monster.mFlags |= Monster.WAKENS;
            }
            found = true;
        }
        return found;
    }

    void showToys(Man man) {
        if (man.blind > 0) {
            return;
        }
        for (Item item : levelToys) {
            Toy t = (Toy) item;
            man.view.addch(t.row, t.col, (char) t.ichar);
        }
    }

    void showTraps(Man man) {
        for (Item t : levelTraps) {
            man.view.addch(t.row, t.col, '^');
        }
    }

    boolean seekGold(Monster monster) {
        for (Item item : levelToys) {
            Toy gold = (Toy) item;
            if (gold.kind != Id.GOLD || 0 != (map[gold.row][gold.col] & MONSTER) || !sees(monster.row, monster.col, gold.row, gold.col)) {
                continue;
            }
            monster.mFlags |= Monster.CAN_FLIT;
            if (monster.monCanGo(gold.row, gold.col)) {
                monster.mFlags &= ~Monster.CAN_FLIT;
                monster.moveMonTo(gold.row, gold.col);
                monster.mFlags |= Monster.ASLEEP;
                monster.mFlags &= ~(Monster.WAKENS | Monster.SEEKS_GOLD);
            } else {
                monster.mFlags &= ~Monster.SEEKS_GOLD;
                monster.mFlags |= Monster.CAN_FLIT;
                monster.moveTo(gold.row, gold.col);
                monster.mFlags &= ~Monster.CAN_FLIT;
                monster.mFlags |= Monster.SEEKS_GOLD;
            }

            return true;
        }

        return false;
    }

    boolean sees(int r, int c, int r1, int c1) { // Is r,c visible from r1,c1?
        int ri;
        int ci;
        int dr;
        int dc;

        if (r1 > r) {
            dr = r1 - r;
            ri = 1;
        } else {
            dr = r - r1;
            ri = -1;
            if (dr == 0)
                ri = 0;
        }
        if (c1 > c) {
            dc = c1 - c;
            ci = 1;
        } else {
            dc = c - c1;
            ci = -1;
            if (dc == 0) {
                ci = 0;
            }
        }
        // Tunnel case
        if (dr <= 1 && dc <= 1 && 0 != (map[r1][c1] & TUNNEL)) {
            return 0 != (map[r][c] & (TUNNEL | DOOR | FLOOR));
        }
        if (dr > dc) {
            // If the first point (typically the non-critter point)
            // is not floor, then offset it a little so walls and
            // doors are more visible
            //
            // Note that r,c and r1,c1 are not checked for floorishness--
            // only the interior points of the line joining them are
            // so checked
            int sum = dr >> 1;
            if (0 == (map[r][c] & FLOOR)) {
                c += ci;
                sum -= dr;
            }
            do {
                r += ri;
                sum += dc;
                if (sum >= dr) {
                    c += ci;
                    sum -= dr;
                }
                if (r == r1 && c == c1) {
                    return true;
                }
            } while (0 != (map[r][c] & FLOOR));
        } else if (dc > 0) {
            int sum = dc >> 1;
            if (0 == (map[r][c] & FLOOR)) {
                r += ri;
                sum -= dc;
            }
            do {
                c += ci;
                sum += dr;
                if (sum >= dc) {
                    r += ri;
                    sum -= dc;
                }
                if (r == r1 && c == c1) {
                    return true;
                }
            } while (0 != (map[r][c] & FLOOR));
        }

        return false;
    }

    // Places near a door...
    Rowcol porch(int r, int c) {
        if (0 != (TUNNEL & map[r + 1][c])) {
            return new Rowcol(r + 1, c);
        }
        if (0 != (TUNNEL & map[r - 1][c])) {
            return new Rowcol(r - 1, c);
        }
        if (0 != (TUNNEL & map[r][c + 1])) {
            return new Rowcol(r, c + 1);
        }
        if (0 != (TUNNEL & map[r][c - 1])) {
            return new Rowcol(r, c - 1);
        }
        
        return null;
    }

    Rowcol foyer(int r, int c) {
        if (0 != (HOLDER & map[r + 1][c])) {
            return new Rowcol(r + 1, c);
        }
        if (0 != (HOLDER & map[r - 1][c])) {
            return new Rowcol(r - 1, c);
        }
        if (0 != (HOLDER & map[r][c + 1])) {
            return new Rowcol(r, c + 1);
        }
        if (0 != (HOLDER & map[r][c - 1])) {
            return new Rowcol(r, c - 1);
        }
        
        return null;
    }

    void wakeRoom(Man man, boolean entering, int row, int col) {
        ItemList itemList = new ItemList(4);

        // List the monsters in the room that can be seen and are asleep
        for (Item item : levelMonsters) {
            Monster monster = (Monster) item;
            if (0 != (monster.mFlags & Monster.ASLEEP) && sees(row, col, monster.row, monster.col)) {
                itemList.add(monster);
            }
        }
        // It's a party if there are more than 4 sleepy monsters
        int wakePercent = itemList.size() > 4 ? Monster.PARTY_WAKE_PERCENT : Monster.WAKE_PERCENT;
        if (man.stealthy > 0) {
            wakePercent /= (Monster.STEALTH_FACTOR + man.stealthy);
        }

        for (Item item : itemList) {
            Monster monster = (Monster) item;
            if (entering) {
                monster.trow = -1;
            } else {
                monster.trow = row;
                monster.tcol = col;
            }
            if (0 != (monster.mFlags & Monster.WAKENS) && self.rand.percent(wakePercent)) {
                monster.wakeUp();
            }
        }
    }

    boolean sameRow(Room rfr, Room rto) {
        return false;
    }

    boolean sameCol(Room rfr, Room rto) {
        return false;
    }

    Room roomAt(int row, int col) {
        return null;
    }

    Room[] nabes(Room r) {
        Room[] ra = new Room[1];
        ra[0] = null;

        return ra;
    }

    String maps(int r, int c) {
        return new Rowcol(r, c) + Integer.toString(map[r][c], 16) + ' ';
    }

    char[][] initSeen() {
        System.out.println("Level.initSeen");
        char[][] see = new char[nrow][ncol];
        for (int r = 0; r < nrow; r++) {
            for (int c = 0; c < ncol; c++) {
                if (0 == (map[r][c] & (STAIRS | FLOOR))) {
                    char s = 0; // Opaque cell
                    for (int k = 0; k < 8; k++) {
                        int r1 = r + Id.X_TABLE[k];
                        int c1 = c + Id.Y_TABLE[k];
                        if (r1 >= 0 && r1 < nrow && c1 >= 0 && c1 < ncol) {
                            if (0 != (map[r1][c1] & (FLOOR | DOOR))) {
                                s = 2; // Cell neighbors transparent cell
                                break;
                            }
                        }
                    }
                    see[r][c] = s;
                } else {
                    see[r][c] = 1;
                }
            }
        }

        return see;
    }

    boolean tryToCough(int r, int c, Toy obj) {
        if (r < MIN_ROW || r > nrow - 2 || c < 0 || c > ncol - 1) {
            return false;
        }
        if (0 == (map[r][c] & (TOY | STAIRS | TRAP)) && 0 != (map[r][c] & (TUNNEL | FLOOR | DOOR))) {
            obj.placeAt(r, c, TOY);
            if (0 == (map[r][c] & (MONSTER | MAN))) {
                self.mark(r, c);
            }
            
            return true;
        }
        
        return false;
    }
}
