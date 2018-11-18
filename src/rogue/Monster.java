package rogue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Monster extends Persona implements Serializable {
    private static final long serialVersionUID = -2717942972899399217L;

    int expPoints;
    int mHitChance;
    int stationaryDamage;
    int dropPercent;
    boolean slowedToggle;
    int napLength;
    int disguise;
    int trow;
    int tcol; /* Target */
    int oRow;
    int oCol;
    int stuck; /* stuck is how many times stuck at o_row, o_col */
    int dstrow;
    int dstcol; /* Default target (where the rogue is) */

    /* m_flags values */
    static final int HASTED = 01;
    static final int SLOWED = 02;
    static final int INVISIBLE = 04;
    static final int ASLEEP = 010;
    static final int WAKENS = 020;
    static final int WANDERS = 040;
    static final int FLIES = 0100;
    static final int FLITS = 0200;
    static final int CAN_FLIT = 0400; /* can, but usually doesn't, flit */
    static final int CONFUSED = 01000;
    static final int RUSTS = 02000;
    static final int HOLDS = 04000;
    static final int FREEZES = 010000;
    static final int STEALS_GOLD = 020000;
    static final int STEALS_ITEM = 040000;
    static final int STINGS = 0100000;
    static final int DRAINS_LIFE = 0200000;
    static final int DROPS_LEVEL = 0400000;
    static final int SEEKS_GOLD = 01000000;
    static final int FREEZING_ROGUE = 02000000;
    static final int RUST_VANISHED = 04000000;
    static final int CONFUSES = 010000000;
    static final int IMITATES = 020000000;
    static final int FLAMES = 040000000;
    static final int STATIONARY = 0100000000; /* damage will be 1,2,3,... */
    static final int NAPPING = 0200000000; /* can't wake up for a while */
    static final int ALREADY_MOVED = 0400000000;
    static final int SPECIAL_HIT = (RUSTS | HOLDS | FREEZES | STEALS_GOLD | STEALS_ITEM | STINGS | DRAINS_LIFE | DROPS_LEVEL);

    static final int WAKE_PERCENT = 45;
    static final int FLIT_PERCENT = 40;
    static final int PARTY_WAKE_PERCENT = 75;
    static final int STEALTH_FACTOR = 3;

    static final int HYPOTHERMIA = 1;
    static final int STARVATION = 2;
    static final int POISON_DART = 3;
    static final int QUIT = 4;
    static final int WIN = 5;
    static final int KFIRE = 6;
    static final int MONSTERS = 26 + 1;

    static final Montype[] MONSTER_TABLE = new Montype[MONSTERS];
    static {
        MONSTER_TABLE[0] = new Montype("aquator", (ASLEEP | WAKENS | WANDERS | RUSTS), "0d0", 25, 'A', 20, 9, 18, 100, 0, 0);
        MONSTER_TABLE[1] = new Montype("bat", (ASLEEP | WANDERS | FLITS | FLIES), "1d3", 10, 'B', 2, 1, 8, 60, 0, 0);
        MONSTER_TABLE[2] = new Montype("centaur", (ASLEEP | WANDERS), "3d3/2d5", 32, 'C', 15, 7, 16, 85, 0, 10);
        MONSTER_TABLE[3] = new Montype("dragon", (ASLEEP | WAKENS | FLAMES), "4d6/4d9", 145, 'D', 5000, 21, 126, 100, 0, 90);
        MONSTER_TABLE[4] = new Montype("emu", (ASLEEP | WAKENS), "1d3", 11, 'E', 2, 1, 7, 65, 0, 0);
        MONSTER_TABLE[5] = new Montype("venus fly-trap", (HOLDS | STATIONARY), "5d5", 73, 'F', 91, 12, 126, 80, 0, 0);
        MONSTER_TABLE[6] = new Montype("griffin", (ASLEEP | WAKENS | WANDERS | FLIES), "5d5/5d5", 115, 'G', 2000, 20, 126, 85, 0, 10);
        MONSTER_TABLE[7] = new Montype("hobgoblin", (ASLEEP | WAKENS | WANDERS), "1d3/1d2", 15, 'H', 3, 1, 10, 67, 0, 0);
        MONSTER_TABLE[8] = new Montype("ice monster", (ASLEEP | FREEZES), "0d0", 15, 'I', 5, 2, 11, 68, 0, 0);
        MONSTER_TABLE[9] = new Montype("jabberwock", (ASLEEP | WANDERS), "3d10/4d5", 132, 'J', 3000, 21, 126, 100, 0, 0);
        MONSTER_TABLE[10] = new Montype("kestrel", (ASLEEP | WAKENS | WANDERS | FLIES), "1d4", 10, 'K', 2, 1, 6, 60, 0, 0);
        MONSTER_TABLE[11] = new Montype("leprechaun", (ASLEEP | STEALS_GOLD), "0d0", 25, 'L', 21, 6, 16, 75, 0, 0);
        MONSTER_TABLE[12] = new Montype("medusa", (ASLEEP | WAKENS | WANDERS | CONFUSES), "4d4/3d7", 97, 'M', 250, 18, 126, 85, 0, 25);
        MONSTER_TABLE[13] = new Montype("nymph", (ASLEEP | STEALS_ITEM), "0d0", 25, 'N', 39, 10, 19, 75, 0, 100);
        MONSTER_TABLE[14] = new Montype("orc", (ASLEEP | WANDERS | WAKENS | SEEKS_GOLD), "1d6", 25, 'O', 5, 4, 13, 70, 0, 10);
        MONSTER_TABLE[15] = new Montype("phantom", (ASLEEP | INVISIBLE | WANDERS | FLITS), "5d4", 76, 'P', 120, 15, 24, 80, 0, 50);
        MONSTER_TABLE[16] = new Montype("quagga", (ASLEEP | WAKENS | WANDERS), "3d5", 30, 'Q', 20, 8, 17, 78, 0, 20);
        MONSTER_TABLE[17] = new Montype("rattlesnake", (ASLEEP | WAKENS | WANDERS | STINGS), "2d5", 19, 'R', 10, 3, 12, 70, 0, 0);
        MONSTER_TABLE[18] = new Montype("snake", (ASLEEP | WAKENS | WANDERS), "1d3", 8, 'S', 2, 1, 9, 50, 0, 0);
        MONSTER_TABLE[19] = new Montype("troll", (ASLEEP | WAKENS | WANDERS), "4d6/1d4", 75, 'T', 125, 13, 22, 75, 0, 33);
        MONSTER_TABLE[20] = new Montype("black unicorn", (ASLEEP | WAKENS | WANDERS), "4d10", 90, 'U', 200, 17, 26, 85, 0, 33);
        MONSTER_TABLE[21] = new Montype("vampire", (ASLEEP | WAKENS | WANDERS | DRAINS_LIFE), "1d14/1d4", 55, 'V', 350, 19, 126, 85, 0, 18);
        MONSTER_TABLE[22] = new Montype("wraith", (ASLEEP | WANDERS | DROPS_LEVEL), "2d8", 45, 'W', 55, 14, 23, 75, 0, 0);
        MONSTER_TABLE[23] = new Montype("xeroc", (ASLEEP | IMITATES), "4d6", 42, 'X', 110, 16, 25, 75, 0, 0);
        MONSTER_TABLE[24] = new Montype("yeti", (ASLEEP | WANDERS), "3d6", 35, 'Y', 50, 11, 20, 80, 0, 20);
        MONSTER_TABLE[25] = new Montype("zombie", (ASLEEP | WAKENS | WANDERS), "1d7", 21, 'Z', 8, 5, 14, 69, 0, 0);
        MONSTER_TABLE[26] = new Montype("rogue", 0, "1d7", 12, '@', 1, -1, -1, 90, 0, 0);
    }

    Monster() { // Undefined monster
        super((Level) null);
    }

    Monster(Level level, int k) { // The kth monster
        super(level);
        mt = MONSTER_TABLE[k];
        mFlags = mt.mFlags;
        hpMax = hpCurrent = mt.hpCurrent;
        ichar = mt.ichar;
        expPoints = mt.expPoints;
        mHitChance = mt.mHitChance;
        stationaryDamage = mt.stationaryDamage;
        dropPercent = mt.dropPercent;
        setHated();
    }

    void zap_monster(Man man, int kind) {
        switch (kind) {
            case Id.SLOW_MONSTER:
                if (0 != (mFlags & HASTED)) {
                    mFlags &= (~HASTED);
                } else {
                    slowedToggle = false;
                    mFlags |= SLOWED;
                }
                break;
            case Id.HASTE_MONSTER:
                if (0 != (mFlags & SLOWED)) {
                    mFlags &= (~SLOWED);
                } else {
                    mFlags |= HASTED;
                }
                break;
            case Id.TELE_AWAY:
                if (0 != (mFlags & HOLDS)) {
                    man.beingHeld = false;
                }
                tele();
                break;
            case Id.INVISIBILITY:
                mFlags |= INVISIBLE;
                break;
            case Id.POLYMORPH:
                if (0 != (mFlags & HOLDS)) {
                    man.beingHeld = false;
                }
                Monster mnew = level.grMonster();
                if (mnew != null) {
                    die();
                    mnew.putMonsterAt(row, col);
                    if (0 != (mnew.mFlags & IMITATES)) {
                        mnew.wakeUp();
                    }
                }
                break;
            case Id.MAGIC_MISSILE:
                man.rogueHit(this, true);
                break;
            case Id.CANCELLATION:
                if (0 != (mFlags & HOLDS)) {
                    man.beingHeld = false;
                }
                if (0 != (mFlags & STEALS_ITEM)) {
                    dropPercent = 0;
                }
                mFlags &= (~(FLIES | FLITS | SPECIAL_HIT | INVISIBLE | FLAMES | IMITATES | CONFUSES | SEEKS_GOLD | HOLDS));
                break;
            case Id.DO_NOTHING:
                man.tell("nothing happens");
                break;
        }
    }

    boolean zapt(Toy obj) {
        String s = obj.kind == Id.FIRE ? "fire" : "ice";
        boolean doend = true;
        int dmg = 0;
        wakeUp();
        if (self.rand.percent(33)) {
            tell("the " + s + " misses " + who());
        } else {
            doend = false;
        }
        if (obj.kind == Id.FIRE) {
            if (0 == (mFlags & RUSTS)) {
                if (0 != (mFlags & FREEZES)) {
                    dmg = hpCurrent;
                } else if (0 != (mFlags & FLAMES)) {
                    dmg = (hpCurrent / 10) + 1;
                } else {
                    dmg = self.rand.get((obj.owner.hpCurrent / 3), obj.owner.hpMax);
                }
            } else {
                dmg = (hpCurrent / 2) + 1;
            }
            tell("the " + s + " hits " + who());
            damage(obj.owner, dmg, 0);
        } else {
            dmg = -1;
            if (0 == (mFlags & FREEZES)) {
                if (self.rand.percent(33)) {
                    tell("the " + name() + " is frozen");
                    mFlags |= (ASLEEP | NAPPING);
                    napLength = self.rand.get(3, 6);
                } else {
                    dmg = obj.owner.hpCurrent / 4;
                }
            } else {
                dmg = -2;
            }
            if (dmg != -1) {
                tell("the " + s + " hits " + who());
                damage(obj.owner, dmg, 0);
            }
        }
        
        return doend;
    }

    boolean throw_at_monster(Toy missile, Persona man) {
        int hitChance = man.getHitChance(missile);
        int dmg = man.getWeaponDamage(missile);
        if (missile.kind == Id.ARROW && man.weapon != null && man.weapon.kind == Id.BOW) {
            dmg += man.getWeaponDamage(man.weapon);
            dmg = dmg * 2 / 3;
            hitChance += hitChance / 3;
        } else if (0 != (missile.inUseFlags & Id.BEING_WIELDED) && (missile.kind == Id.DAGGER || missile.kind == Id.SHURIKEN || missile.kind == Id.DART)) {
            dmg = dmg * 3 / 2;
            hitChance += hitChance / 3;
        }
        int t = missile.quantity;
        missile.quantity = 1;
        man.hitMessage += "the " + missile.name();
        missile.quantity = t;

        if (!self.rand.percent(hitChance)) {
            man.hitMessage += "misses  ";
            return false;
        }
        sConMon(man);
        man.hitMessage += "hit  ";
        damage(man, dmg, 0);
        
        return true;
    }

    void putMonsterAt(int r, int c) {
        placeAt(r, c, MONSTER);
        aimMonster();
    }

    boolean mtry(int row, int col) {
        if (!monCanGo(row, col)) {
            return false;
        }
        moveMonTo(row, col);
        
        return true;
    }

    void aimMonster() {
        List<Door> v = new ArrayList<>(4);
        for (Item item : level.levelDoors) {
            Door dr = (Door) item;
            if (dr.passageto != null && monSees(dr.row, dr.col)) {
                v.add(dr);
            }
        }
        if (v.size() > 0) {
            Door dr = v.get(self.rand.get(v.size() - 1));
            trow = dr.row;
            tcol = dr.col;
        }
    }

    boolean moveConfused() {
        if (0 == (mFlags & ASLEEP)) {
            if (--confused <= 0) {
                mFlags &= ~CONFUSED;
            }
            if (0 != (mFlags & STATIONARY)) {
                return self.rand.coin();
            }
            if (self.rand.percent(15)) {
                return true;
            }
            int perm[] = self.rand.permute(9);
            for (int i = 0; i < 9; i++) {
                int c = Id.xtab[perm[i]] + col;
                int r = Id.ytab[perm[i]] + row;
                if (0 != (level.map[r][c] & MAN)) {
                    return false;
                }
                if (mtry(r, c)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    boolean flit() {
        if (!self.rand.percent(FLIT_PERCENT + (0 != (mFlags & FLIES) ? 20 : 0))) {
            return false;
        }
        if (self.rand.percent(10)) {
            return true;
        }
        int perm[] = self.rand.permute(9);
        for (int i = 0; i < 9; i++) {
            int c = Id.xtab[perm[i]] + col;
            int r = Id.ytab[perm[i]] + row;

            if (0 == (level.map[r][c] & MAN)) {
                if (mtry(r, c)) {
                    return true;
                }
            }
        }
        
        return true;
    }

    int gmc(Man man) {
        if ((!(man.detectMonster || man.seeInvisible || man.rSeeInvisible) && 0 != (mFlags & INVISIBLE)) || man.blind > 0) {
            return level.getChar(row, col);
        }
        if (man.halluc > 0) {
            return self.rand.get('A', 'Z');
        }
        if (0 != (mFlags & IMITATES)) {
            return disguise | color();
        }
        
        return ichar | color();
    }

    boolean rogueIsAround() {
        for (int r = -1; r <= 1; r++) {
            for (int c = -1; c <= 1; c++) {
                if (0 != (level.map[row + r][col + c] & MAN)) {
                    setHated();
                    if (ihate == null) {
                        ihate = (Man) level.levelMen.itemAt(row + r, col + c);
                    }
        
                    return true;
                }
            }
        }
        
        return false;
    }

    void moveMonster() {
        int rwas = row;
        int cwas = col;

        if (ihate == null) {
            setHated();
        }
        if (ihate != null) {
            moveTo(ihate.row, ihate.col);
        } else {
            moveTo(trow, tcol);
        }
        regMove();
        if (row != rwas || col != cwas) {
            /* It really did move */
            if (0 != (level.map[row][col] & TRAP)) {
                trapPlayer();
            }
        }
    }

    void moveTo(int r, int c) {
        int i;
        if (0 != (mFlags & ASLEEP)) {
            if (0 != (mFlags & NAPPING)) {
                if (--napLength <= 0) {
                    mFlags &= ~(NAPPING | ASLEEP);
                }
                
                return;
            }
            if (0 != (mFlags & WAKENS) && rogueIsAround() && ihate != null && self.rand.percent(ihate.stealthy > 0 ? WAKE_PERCENT / (STEALTH_FACTOR + ihate.stealthy) : WAKE_PERCENT)) {
                wakeUp();
            }
            
            return;
        } else if (0 != (mFlags & ALREADY_MOVED)) {
            mFlags &= ~ALREADY_MOVED;
            
            return;
        }
        if (0 != (mFlags & FLITS) && flit()) {
            return;
        }
        // This is the general stick-close behavior:
        if (0 != (mFlags & STATIONARY) && ihate != null && !monCanGo(ihate.row, ihate.col)) {
            return;
        }
        if (0 != (mFlags & FREEZING_ROGUE)) {
            return;
        }
        if (0 != (mFlags & CONFUSES) && ihate != null && ihate.mConfuse(this)) {
            return;
        }
        if (ihate != null && monCanGo(ihate.row, ihate.col)) {
            monHit(ihate);
            
            return;
        }
        if (0 != (mFlags & FLAMES) && ihate != null && flame_broil(ihate)) {
            return;
        }
        if (0 != (mFlags & SEEKS_GOLD) && level.seekGold(this)) {
            return;
        }
        if (trow == row && tcol == col) {
            trow = -1;
        } else if (trow != -1) {
            r = trow;
            c = tcol;
        }
        if (row > r) {
            r = row - 1;
        } else if (row < r) {
            r = row + 1;
        }
        if (0 != (level.map[r][col] & DOOR) && mtry(r, col)) {
            return;
        }
        if (col > c) {
            c = col - 1;
        } else if (col < c) {
            c = col + 1;
        }
        if (0 != (level.map[row][c] & DOOR) && mtry(row, c)) {
            return;
        }
        if (mtry(r, c)) {
            return;
        }
        int perm[] = self.rand.permute(6);
        for (i = 0; i < 6; i++) {
            switch (perm[i]) {
                case 0:
                    if (mtry(r, col - 1)) {
                        i = 6;
                    }
                    break;
                case 1:
                    if (mtry(r, col)) {
                        i = 6;
                    }
                    break;
                case 2:
                    if (mtry(r, col + 1)) {
                        i = 6;
                    }
                    break;
                case 3:
                    if (mtry(row - 1, c)) {
                        i = 6;
                    }
                    break;
                case 4:
                    if (mtry(row, c)) {
                        i = 6;
                    }
                    break;
                case 5:
                    if (mtry(row + 1, c)) {
                        i = 6;
                    }
                    break;
            }
        }
        if (row == oRow && col == oCol) {
            if (++stuck > 4) {
                if (trow == -1 && (ihate == null || !monSees(ihate.row, ihate.col))) {
                    setHated();
                    trow = self.rand.get(1, level.nrow - 2);
                    tcol = self.rand.get(level.ncol - 1);
                } else {
                    trow = -1;
                    stuck = 0;
                }
            }
        } else {
            oRow = row;
            oCol = col;
            stuck = 0;
        }
    }

    void setHated() {
        Persona hated = ihate;
        int dmin = 10000;
        int j = level.levelMen.size();
        while (--j >= 0) {
            Man m = (Man) level.levelMen.get(j);
            if (monSees(m.row, m.col)) {
                int dr = m.row - row;
                int dc = m.col - col;
                int d = dr * dr + dc * dc;
                if (d < dmin) {
                    dmin = d;
                    hated = m;
                }
            }
        }
        if (hated != ihate) {
            if (ihate != null && 0 != (mFlags & HOLDS)) {
                ihate.beingHeld = false;
            }
            ihate = hated;
        }
    }

    void moveMonTo(int r, int c) {
        boolean inroom = 0 != (level.map[row][col] & HOLDER);
        placeAt(r, c, MONSTER);
        if (0 != (level.map[r][c] & DOOR)) {
            trow = -1;
            setHated();
            if (ihate == null) {
                drCourse(!inroom);
            }
        }
    }

    boolean monCanGo(int r, int c) {
        int dr = row - r; /* check if move distance > 1 */
        if (dr >= 2 || dr <= -2) {
            return false;
        }
        int dc = c - col;
        if (dc >= 2 || dc <= -2) {
            return false;
        }
        if (0 == level.map[row][c] || 0 == level.map[r][col]) {
            return false;
        }
        if (!level.isPassable(r, c) || 0 != (level.map[r][c] & MONSTER)) {
            return false;
        }
        if (row != r && col != c && 0 != ((level.map[r][c] | level.map[row][col]) & DOOR)) {
            return false;
        }
        if (0 == (mFlags & (FLITS | CONFUSED | CAN_FLIT)) && trow == -1) {
            if (row < dstrow && r < row) {
                return false;
            }
            if (row > dstrow && r > row) {
                return false;
            }
            if (col < dstcol && c < col) {
                return false;
            }
            if (col > dstcol && c > col) {
                return false;
            }
        }
        if (0 != (level.map[r][c] & TOY)) {
            Toy obj = (Toy) level.levelToys.itemAt(r, c);
            if (obj.kind == Id.SCARE_MONSTER) {
                return false;
            }
        }
        
        return true;
    }

    void wakeUp() {
        if (0 == (mFlags & NAPPING)) {
            mFlags &= ~(ASLEEP | IMITATES | WAKENS);
        }
    }

    boolean monSees(int r, int c) {
        if (level.sees(row, col, r, c)) {
            return true;
        }
        int rdif = r - row;
        int cdif = c - col;

        return rdif >= -1 && rdif <= 1 && cdif >= -1 && cdif <= 1;
    }

    void drCourse(boolean entering) {
        Door my = (Door) level.levelDoors.itemAt(row, col);
        if (entering) {
            Rowcol foyer = level.foyer(row, col);
            Door dtab[] = new Door[level.levelDoors.size()];
            dtab = level.levelDoors.toArray(dtab);
            self.rand.permute(dtab);
            for (int i = 0; i < dtab.length; i++) {
                Door dr = dtab[i];
                Door d = dr.passageto;
                if (dr == my) {
                    // My door is a last resort (go back)
                    if (trow == -1 && d != null) {
                        trow = d.row;
                        tcol = d.col;
                    }
                } else if (foyer != null && level.sees(dr.row, dr.col, foyer.row, foyer.col)) {
                    trow = dr.row;
                    tcol = dr.col;
                    // Passage to another door is preferred
                    if (d != null) {
                        break;
                    }
                }
            }
        } else if (my != null && my.passageto != null) {
            trow = my.passageto.row;
            tcol = my.passageto.col;
        }
    }

    void sConMon(Persona man) {
        if (man.conMon) {
            mFlags |= CONFUSED;
            cnfs(self.rand.get(12, 22));
            tell(who("appear") + " confused");
            man.conMon = false;
        }
    }

    void monHit(Persona man) {
        double minus = 0;

        if (man.ihate != null && this != man.ihate) {
            man.ihate = null;
        }
        ihate = man;
        trow = -1;
        int hitChance = 100;
        if (level.currentLevel < AMULET_LEVEL * 2) {
            hitChance = mHitChance;
            hitChance -= 2 * man.exp + 2 * man.ringExp - man.rRings;
        }
        if (man.wizard) {
            hitChance /= 2;
        }
        if (null == man.ihate) {
            level.self.interrupted = true;
        }
        if (!self.rand.percent(hitChance)) {
            if (null == man.ihate) {
                man.hitMessage += who("miss", "misses");
                man.tell(man.hitMessage, true);
                man.hitMessage = "";
            }
         
            return;
        }
        if (null == man.ihate) {
            man.hitMessage += who("hit");
            man.tell(man.hitMessage, true);
            man.hitMessage = "";
        }
        int dmg;
        if (0 == (mFlags & STATIONARY)) {
            dmg = Id.getDamage(mt.mDamage, self.rand);
            if (level.currentLevel >= AMULET_LEVEL * 2) {
                minus = (double) ((AMULET_LEVEL * 2) - level.currentLevel);
            } else {
                if (man.armor != null) {
                    minus = (double) man.armor.getArmorClass() * 3.00;
                }
                minus = minus / 100.00 * dmg;
            }
            dmg -= (int) minus;
        } else {
            dmg = stationaryDamage++;
        }
        if (man.wizard) {
            dmg /= 3;
        }
        if (dmg > 0) {
            man.damage((Persona) this, dmg, 0);
        }
        if (0 != (mFlags & SPECIAL_HIT)) {
            special_hit(man);
        }
    }

    boolean damage(Persona man, int dmg, int other) {
        if (dmg > 0 && man != this && man != null)
            ihate = man;
        hpCurrent -= dmg;
        if (hpCurrent > 0) {
            if (dmg > 0) {
                self.flashadd(row, col, U_RED);
            }
            
            return true;
        }
        die(man);
        
        return false;
    }

    void die(Persona man) {
        if (man != null) {
            if (this == man.ihate) {
                man.ihate = null;
            }
            coughUp();
            man.tell(man.who() + " defeated " + who());
            man.hitMessage = "";
            if (man instanceof Man) {
                ((Man) man).add_exp(expPoints, true);
            }
            if (0 != (mFlags & HOLDS)) {
                man.beingHeld = false;
            }
        }
        super.die();
    }

    void stealGold(Persona man) {
        int amount;
        if ((man.gold <= 0) || self.rand.percent(10)) {
            return;
        }
        amount = self.rand.get(level.currentLevel * 10, level.currentLevel * 30);

        if (amount > man.gold) {
            amount = man.gold;
        }
        man.gold -= amount;
        gold += self.rand.get(amount / 4, amount);
        man.tell("your purse feels lighter");
        man.printStat();
        tele();
    }

    void stealItem(Persona man) {
        int t = 1;
        int nn = 0;
        Toy obj = null;

        if (self.rand.percent(15) || !(man instanceof Man)) {
            return;
        }

        for (Item item : ((Man) man).pack) {
            Toy o = (Toy) item;
            if (0 == (o.inUseFlags & Id.BEING_USED)) {
                if (0 == self.rand.get(nn)) {
                    obj = o;
                }
                ++nn;
            }
        }
        if (obj != null) {
            if (0 == (obj.kind & Id.WEAPON)) {
                t = obj.quantity;
                obj.quantity = 1;
            }
            man.tell("she stole " + obj.getDesc());
            obj.quantity = t;

            obj.vanish();
        }
        die(); /* Kill the monster to disappear it */
    }

    void coughUp() {
        Toy obj;
        if (level.currentLevel < level.maxLevel) {
            return;
        }
        if (0 != (mFlags & STEALS_GOLD)) {
            obj = new Toy(level, Id.GOLD);
            obj.quantity = self.rand.get(level.currentLevel * 15, level.currentLevel * 30);
        } else {
            if (!self.rand.percent(dropPercent))
                return;
            obj = level.grToy();
        }
        for (int n = 0; n <= 5; n++) {
            for (int i = -n; i <= n; i++) {
                if (level.tryToCough(row + n, col + i, obj)) {
                    return;
                }
                if (level.tryToCough(row - n, col + i, obj)) {
                    return;
                }
            }
            for (int i = -n; i <= n; i++) {
                if (level.tryToCough(row + i, col - n, obj)) {
                    return;
                }
                if (level.tryToCough(row + i, col + n, obj)) {
                    return;
                }
            }
        }
    }

    void checkGoldSeeker() {
        mFlags &= ~SEEKS_GOLD;
    }

    boolean check_imitator() {
        if (0 != (mFlags & IMITATES)) {
            wakeUp();
            level.mark(row, col);

            return true;
        }

        return false;
    }

    boolean flame_broil(Persona man) {
        if (!monSees(man.row, man.col) || self.rand.coin()) {
            return false;
        }
        int r = man.row - row;
        int c = man.col - col;
        if (r < 0) {
            r = -r;
        }
        if (c < 0) {
            c = -c;
        }
        if ((r != 0 && c != 0 && r != c) || r > 7 || c > 7) {
            return false;
        }

        int dir = Id.getDirection(row, col, man.row, man.col);
        Toy wand = level.grWand();
        wand.kind = Id.FIRE;
        wand.owner = this;
        level.bounce(wand, dir, row, col, 0);
        self.markall(); // relight
        
        return true;
    }

    void special_hit(Persona p) {
        if (!(p instanceof Man)) {
            return;
        }
        Man man = (Man) p;

        if (0 != (mFlags & CONFUSED) && self.rand.percent(66)) {
            return;
        }
        if (0 != (mFlags & RUSTS)) {
            man.rust(this);
        }
        if (0 != (mFlags & HOLDS) && man.levitate == 0) {
            man.beingHeld = true;
        }
        if (0 != (mFlags & FREEZES)) {
            man.freeze(this);
        }
        if (0 != (mFlags & STINGS)) {
            man.sting(this);
        }
        if (0 != (mFlags & DRAINS_LIFE)) {
            man.drainLife();
        }
        if (0 != (mFlags & DROPS_LEVEL)) {
            man.dropLevel();
        }
        if (0 != (mFlags & STEALS_GOLD)) {
            stealGold(man);
        } else if (0 != (mFlags & STEALS_ITEM)) {
            stealItem(man);
        }
    }

    static String mflagname[] = { 
            "HASTED", "SLOWED", "INVISIBLE", "ASLEEP", "WAKENS", "WANDERS", "FLIES", "FLITS", "CAN_FLIT", "CONFUSED", "RUSTS", "HOLDS", "FREEZES", "STEALS_GOLD", "STEALS_ITEM",
            "STINGS", "DRAINS_LIFE", "DROPS_LEVEL", "SEEKS_GOLD", "FREEZING_ROGUE", "RUST_VANISHED", "CONFUSES", "IMITATES", "FLAMES", "STATIONARY", "NAPPING" 
    };

    public String toString() {
        String s = "";
        for (int k = 0; k < mflagname.length; k++) {
            if (0 != (mFlags & (1 << k))) {
                s = s + ' ' + mflagname[k];
            }
        }
        
        return name() + super.toString() + "to " + (new Rowcol(trow, tcol)).toString() + s;
    }

    void tele() {
        Rowcol pt = level.grRowCol(FLOOR | TUNNEL | STAIRS | TOY, this);
        if (pt != null) {
            putMonsterAt(pt.row, pt.col);
        }
        beingHeld = false;
        if (0 != (mFlags & HOLDS) && ihate != null) {
            ihate.beingHeld = false;
        }
        bearTrap = 0;
    }

    int color() {
        // if(0 != (m_flags & (ASLEEP | NAPPING)))
        // return uGreen;
        // if(2*hp_current <= hp_max)
        // return uWeak;
        return U_NORMAL;
    }
}
