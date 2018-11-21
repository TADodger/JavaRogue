package rogue;

import java.io.Serializable;
import java.util.Iterator;

/**
 *
 */
public abstract class Level implements Header, Serializable {
    private static final long serialVersionUID = -8320125298348249127L;

    /** A reference to the Rogue */
    public Rogue rogue;
    /** List of Man for this level */
    public ItemList<Man> levelMen;
    /** List of Toy for this level */
    public ItemList<Toy> levelToys;
    /** List of Monster for this level */
    public ItemList<Monster> levelMonsters;
    /** List of Trap for this level */
    public ItemList<Trap> levelTraps;
    /** List of Door for this level */
    public ItemList<Door> levelDoors;
    /** dungeon map of the Level */
    public int[][] map;
    /** width of the map */
    public int numCol;
    /** height of the map */
    public int numRow;
    /** the current level */
    public int currentLevel;
    /** highext level */
    public int maxLevel;

    private int foods = 0; // Higher levels get more food

    Level(int numRow, int numCol, Rogue rogue) {
        this.rogue = rogue;
        this.numRow = numRow;
        this.numCol = numCol;
        this.currentLevel = rogue.currentLevel;
        this.maxLevel = rogue.maxLevel;
        init();
    }

    protected void init() {
        map = new int[numRow][numCol];

        for (int r = 0; r < numRow; r++) {
            for (int c = 0; c < numCol; c++) {
                map[r][c] = 0;
            }
        }

        levelMonsters = new ItemList<>(6); // Start with a few monsters
        levelDoors = new ItemList<>(8); // and a few doors too
        levelMen = new ItemList<>();
        levelToys = new ItemList<>(6);
        levelTraps = new ItemList<>(3);
    }

    /**
     * @param row
     * @param col
     */
    public void mark(int row, int col) {
        int i = levelMen.size();
        while (--i >= 0) {
            levelMen.get(i).view.mark(row, col);
        }
    }

    /**
     * @param row
     * @param col
     * @return The display character at the specified location
     */
    public char getChar(int row, int col) {
        if (row < 0 || row >= numRow || col < 0 || col >= numCol) {
            return (char) 0;
        }
        int mask = map[row][col];

        if (0 != (mask & TOY)) {
            Toy toy = levelToys.itemAt(row, col);
            return toy == null ? ';' : (char) Id.getMaskCharacter(toy.kind);
        }
        /* Not allowing hidden stairs */
        if (0 != (mask & STAIRS)) {
            return '%';
        }
        if (0 != (mask & (TUNNEL | STAIRS | HORWALL | VERTWALL | FLOOR | DOOR))) {
            if (0 == (mask & HIDDEN)) {
                if (0 != (mask & TUNNEL)) {
                    return '#';
                }
            }

            if (0 != (mask & HORWALL)) {
                return '-';
            }
            if (0 != (mask & VERTWALL)) {
                return '|';
            }
            if (0 != (mask & FLOOR)) {
                return (mask & (HIDDEN | TRAP)) == TRAP ? '^' : '.';
            }
            if (0 != (mask & DOOR)) { 
                if (0 == (mask & HIDDEN)) {
                    return '+';
                }

                /* Hidden door: */
                if (0 != (mask & TUNNEL)) {
                    // return ' ';FOOP
                    return '$';
                }
                if (col <= 0 || col >= numCol - 1) {
                    return '|';
                }
                if (0 != (map[row][col - 1] & HORWALL) || 0 != (map[row][col + 1] & HORWALL)) {
                    return '-';
                }
                
                return '|';
            }
        }
        if (0 != (mask & TUNNEL)) {
            return '$';// FOOP
        }
        
        return ' ';
    }

    /**
     * @param row
     * @param col
     * @return If the specified location is passable
     */
    public boolean isPassable(int row, int col) {
        if (row < MIN_ROW || row > (numRow - 2) || col < 0 || col > (numCol - 1)) {
            return false;
        }
        if (0 != (map[row][col] & HIDDEN)) {
            return 0 != (map[row][col] & TRAP);
        }
        
        return 0 != (map[row][col] & (FLOOR | TUNNEL | DOOR | STAIRS | TRAP));
    }

    /**
     * @param row
     * @param col
     * @return true if the tunnel can turn?
     */
    public boolean canTurn(int row, int col) {
        return 0 != (TUNNEL & map[row][col]) && isPassable(row, col);
    }

    /**
     * @param srcRow
     * @param srcCol
     * @param destRow
     * @param destCol
     * @return true if a move can be made from the src to the dest location
     */
    public boolean canMove(int srcRow, int srcCol, int destRow, int destCol) {
        if (!isPassable(destRow, destCol)) {
            return false;
        }
        if (srcRow != destRow && srcCol != destCol) {
            if (0 != ((map[srcRow][srcCol] | map[destRow][destCol]) & DOOR)) {
                return false;
            }
            if (0 == map[srcRow][destCol] || 0 == map[destRow][srcCol]) {
                return false;
            }
        }

        return true;
    }

    /**
     * @param mask
     * @param item
     * @return a point in a random room--avoid item
     */
    public Rowcol getRandomRowCol(int mask, Item item) {
        int row = 0;
        int col = 0;
        mask |= HOLDER | DARK;
        int numTries = 2400;
        do {
            if (--numTries > 0) {
                row = rogue.rand.get(MIN_ROW, numRow - 2);
                col = rogue.rand.get(numCol - 1);
            } else if (numTries == 0) {
                row = numRow - 2;
                col = numCol - 1;
            } else if (--col < 0) {
                col = numCol - 1;
                if (--row < 0) {
                    return null;
                }
            }
        } while (0 == (map[row][col] & mask) || 0 != (map[row][col] & (~mask)) || 0 == (map[row][col] & HOLDER) || (item != null && row == item.row && col == item.col));
        
        return new Rowcol(row, col);
    }

    /**
     * Place gold at the specified location
     * 
     * @param row
     * @param col
     * @param gold
     */
    public void plantGold(int row, int col, int gold) {
        Toy toy = new Toy(this, Id.GOLD);
        toy.quantity = gold;
        toy.placeAt(row, col, TOY);
    }

    /**
     * @return A random type of scroll
     */
    public Scroll getRandomScroll() {
        Scroll scroll = new Scroll(this);
        scroll.kind = Id.getRandomWhichScroll(rogue.rand);
        
        return scroll;
    }

    /**
     * @return A random type of potion
     */
    public Potion getRandomPotion() {
        Potion potion = new Potion(this);
        potion.kind = Id.getRandomWhichPotion(rogue.rand);
        
        return potion;
    }

    /**
     * @param assignType 
     * @return A random type of weapon
     */
    public Toy getRandomWeapon(int assignType) {
        if (assignType < 0) {
            assignType = rogue.rand.get(Id.idWeapons.length - 1);
        }
        
        return new Toy(this, Id.WEAPON | assignType);
    }

    /**
     * @return A random type of armor
     */
    public Toy getRandomArmor() {
        return new Toy(this, Id.ARMOR + rogue.rand.get(Id.idArmors.length - 1));
    }

    /**
     * @return A random type of wand
     */
    public Toy getRandomWand() {
        return new Toy(this, Id.WAND + rogue.rand.get(Id.idWands.length - 1));
    }

    /**
     * @param assignType 
     * @return A random type of ring
     */
    public Toy getRandomRing(int assignType) {
        if (assignType < 0) {
            assignType = rogue.rand.get(Id.idRings.length - 1);
        }
        
        return new Toy(this, Id.RING + assignType);
    }

    /**
     * @param forceRation
     * @return Some food
     */
    public Toy getFood(boolean forceRation) {
        return new Toy(this, forceRation || rogue.rand.percent(80) ? Id.RATION : Id.FRUIT);
    }

    /**
     * @return A random type of Toy
     */
    public Toy getRandomToy() {
        int k;
        if (foods < currentLevel / 3) {
            k = Id.FOOD;
            foods++;
        } else
            k = Id.getRandomSpecies(rogue.rand);
        switch (k) {
            case Id.SCROLL:
                return (Toy) getRandomScroll();
            case Id.POTION:
                return (Toy) getRandomPotion();
            case Id.WEAPON:
                return (Toy) getRandomWeapon(-1);
            case Id.ARMOR:
                return (Toy) getRandomArmor();
            case Id.WAND:
                return (Toy) getRandomWand();
            case Id.FOOD:
                return (Toy) getFood(false);
            case Id.RING:
                return (Toy) getRandomRing(-1);
        }

        return null;
    }

    /**
     * @param man
     * @param ch
     * @return A Toy in wizard mode
     */
    public Toy wizardToy(Man man, int ch) {
        Toy toy = null;
        int max = 0;
        switch (ch) {
            case '!':
                toy = (Toy) getRandomPotion();
                max = Id.idPotions.length;
                break;
            case '?':
                toy = (Toy) getRandomScroll();
                max = Id.idScrolls.length;
                break;
            case ',':
                toy = new Toy(this, Id.AMULET);
                break;
            case ':':
                toy = (Toy) getFood(false);
                break;
            case ')':
                max = Id.idWeapons.length;
                break;
            case ']':
                toy = (Toy) getRandomArmor();
                max = Id.idArmors.length;
                break;
            case '/':
                toy = (Toy) getRandomWand();
                max = Id.idWands.length;
                break;
            case '=':
                max = Id.idRings.length;
                break;
        }
        --max;
        if (ch == ',' || ch == ':') {
            return toy;
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
                    toy = getRandomRing(wk);
                } else if (ch == ')') {
                    toy = getRandomWeapon(wk);
                } else if (toy != null) {
                    toy.kind = (toy.kind & Id.ALL_TOYS) + wk;
                }
                
                return toy;
            }
        }
        
        return null;
    }

    protected void putToys() {
        if (currentLevel < maxLevel) {
            return;
        }
        int numberOfToys = rogue.rand.get(2, 4);
        if (rogue.rand.coin()) {
            numberOfToys++;
        }
        while (rogue.rand.percent(33)) {
            numberOfToys++;
        }
        for (int i = 0; i < numberOfToys; i++) {
            Rowcol point = getRandomRowCol(FLOOR | TUNNEL, null);
            if (point != null) {
                Toy toy = getRandomToy();
                toy.placeAt(point.row, point.col, TOY);
            }
        }
    }

    /**
     * @param dir
     * @param row
     * @param col
     * @param allowOffScreen
     * @return The location in the direction given
     */
    public Rowcol getDirRowCol(int dir, int row, int col, boolean allowOffScreen) {
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
        if (allowOffScreen || (row > MIN_ROW && row < numRow - 2 && col > 0 && col < numCol - 1)) {
            return new Rowcol(row, col);
        }
        
        return null;
    }

    /**
     * Place the player on the level.
     * 
     * @param man
     */
    public void putPlayer(Man man) {
        /* try not to put where he can see his current position */
        int misses = 2;
        Rowcol pt;
        do {
            pt = getRandomRowCol(FLOOR | TUNNEL | TOY | STAIRS, man);
            if (pt == null) {
                return;
            }
        } while (--misses >= 0 && sees(pt.row, pt.col, man.row, man.col));
        man.placeAt(pt.row, pt.col, MAN);
        wakeRoom(man, true, pt.row, pt.col);
        if (man.newLevelMessage != null) {
            rogue.tell(man, man.newLevelMessage, false);
            man.newLevelMessage = null;
        }
    }

    /**
     * Draw the whole map
     * 
     * @param man
     */
    public void drawMagicMap(Man man) {
        for (int i = 0; i < numRow; i++)
            for (int j = 0; j < numCol; j++) {
                int s = map[i][j];
                if (0 != (s & (HORWALL | VERTWALL | DOOR | TUNNEL | TRAP | STAIRS))) {
                    if (0 == (s & MONSTER)) {
                        man.seen[i][j] = (char) ((man.seen[i][j] & 15) | man.wallcode(getChar(i, j)));
                        man.view.mark(i, j);
                    }
                }
            }
    }

    /**
     * Remove the hidden attribute for the whole map
     */
    public void unhide() {
        for (int i = 0; i < numRow; i++)
            for (int j = 0; j < numCol; j++)
                if (0 != (map[i][j] & HIDDEN)) {
                    map[i][j] &= ~HIDDEN;
                    mark(i, j);
                }
    }

    /**
     * @param dir
     * @param row
     * @param col
     * @return A Monster in the given direction. null if no Monster is found.
     */
    public Monster getZappedMonster(int dir, int row, int col) {
        for (;;) {
            int ocol = col;
            int orow = row;
            Rowcol pt = getDirRowCol(dir, row, col, false);
            row = pt.row;
            col = pt.col;
            if ((row == orow && col == ocol) || 0 != (map[row][col] & (HORWALL | VERTWALL)) || 0 == (map[row][col] & SOMETHING)) {
                break;
            }
            if (0 != (map[row][col] & MONSTER)) {
                Monster monster = levelMonsters.itemAt(row, col);
                if (!imitating(row, col))
                    return monster;
            }
        }
        
        return null;
    }

    /**
     * Drain life from a monster struck with a wand.
     * 
     * @param man
     * @param monster
     */
    public void wandDrainLife(Man man, Monster monster) {
        int hp = man.hpCurrent / 3;
        man.hpCurrent = (man.hpCurrent + 1) / 2;

        for (Monster levelMonster : levelMonsters) {
            if (sees(levelMonster.row, levelMonster.col, man.row, man.col)) {
                levelMonster.wakeUp();
                levelMonster.damage(man, hp, 0);
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

    /**
     * @param toy
     * @param dir
     * @param row
     * @param col
     * @param r
     */
    public void bounce(Toy toy, int dir, int row, int col, int r) {
        boolean fiery = (toy.kind == Id.FIRE);
        int orow;
        int ocol;
        int i;
        int newDirection = -1;
        int damage;
        boolean doend = true;
        Persona owner = toy.owner;

        if (++r == 1) {
            btime = rogue.rand.get(3, 6);
        } else if (r > btime) {
            return;
        }
        String s = fiery ? "fire" : "ice";
        // if(r > 1)
        // self.view.msg.message("the "+s+" bounces", true);
        rogue.mdSleep(100);
        orow = row;
        ocol = col;
        Rowcol pt;
        do {
            rogue.flashadd(orow, ocol, U_BRITE);
            pt = getDirRowCol(dir, orow, ocol, true);
            orow = pt.row;
            ocol = pt.col;
        } while (!(ocol <= 0 || ocol >= numCol - 1 || 0 == (map[orow][ocol] & SOMETHING) || 0 != (map[orow][ocol] & MONSTER) || 0 != (map[orow][ocol] & (HORWALL | VERTWALL))
                || (orow == owner.row && ocol == owner.col)));
        rogue.xflash();
        do {
            orow = row;
            ocol = col;
            rogue.vset(row, col);
            pt = getDirRowCol(dir, row, col, true);
            row = pt.row;
            col = pt.col;
        } while (!(col <= 0 || col >= numCol - 1 || 0 == (map[row][col] & SOMETHING) || 0 != (map[row][col] & MONSTER) || 0 != (map[row][col] & (HORWALL | VERTWALL))
                || (row == owner.row && col == owner.col)));

        if (0 != (map[row][col] & MONSTER)) {
            Monster monster = levelMonsters.itemAt(row, col);
            if (monster != null) {
                doend = monster.zapt(toy);
            }
        } else {
            for (View view : rogue.viewList) {
                Man man = view.man;
                if (row == man.row && col == man.col) {
                    int ac = man.armor == null ? 0 : man.armor.getArmorClass();
                    if (rogue.rand.percent(10 + (3 * ac))) {
                        rogue.describe(man, "the " + s + " misses", false);
                    } else {
                        doend = false;
                        damage = rogue.rand.get(3, (3 * man.exp));
                        if (fiery) {
                            damage = (damage * 3) / 2;
                            if (man.armor != null)
                                damage -= man.armor.getArmorClass();
                        }
                        man.damage(null, damage, fiery ? Monster.KFIRE : Monster.HYPOTHERMIA);
                        rogue.describe(man, "the " + s + " hits", false);
                    }
                }
            }
        }
        if (doend) {
            int nr;
            int nc;
            for (i = 0; i < 10; i++) {
                dir = rogue.rand.get(Id.DIRS - 1);
                nr = orow;
                nc = ocol;
                pt = getDirRowCol(dir, nr, nc, true);
                nr = pt.row;
                nc = pt.col;
                if ((nc >= 0 && nc <= numCol - 1) && 0 != (map[nr][nc] & SOMETHING) && 0 == (map[nr][nc] & (VERTWALL | HORWALL))) {
                    newDirection = dir;
                    break;
                }
            }
            if (newDirection != -1) {
                bounce(toy, newDirection, orow, ocol, r);
            }
        }
    }

    /**
     * Place random monsters in random places
     */
    public void putMonsters() {
        int n = rogue.rand.get(4, 6);

        for (int i = 0; i < n; i++) {
            Rowcol point = getRandomRowCol(FLOOR | TUNNEL | STAIRS | TOY, null);
            if (point == null) {
                continue;
            }
            Monster monster = getRandomMonster();
            if (0 != (monster.mFlags & Monster.WANDERS) && rogue.rand.coin()) {
                monster.wakeUp();
            }
            monster.putMonsterAt(point.row, point.col);
            // System.out.println("Monster " + monster);
        }
    }

    /**
     * @return generate a random Monster
     */
    public Monster getRandomMonster() {
        int monsterType;

        for (;;) {
            monsterType = rogue.rand.get(Monster.MONSTERS - 1);
            if ((currentLevel >= Monster.MONSTER_TABLE[monsterType].firstLevel) && (currentLevel <= Monster.MONSTER_TABLE[monsterType].lastLevel)) {
                break;
            }
        }
        Monster monster = new Monster(this, monsterType);
        if (0 != (monster.mFlags & Monster.IMITATES)) {
            monster.disguise = Id.getRandomObjectCharacter(rogue.rand);
        }
        if (currentLevel > AMULET_LEVEL + 2) {
            monster.mFlags |= Monster.HASTED;
        }
        monster.trow = -1;
        
        return monster;
    }

    /**
     * Move all monsters on the level
     * 
     * @param man
     */
    public void moveMonsters(Man man) { // Move all the monsters
        if (0 != (man.hasteSelf % 2)) {
            return;
        }
        for (Iterator<Monster> it = levelMonsters.iterator();it.hasNext() && !Man.gameOver;) {
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

    /**
     * Create a wandering monster
     */
    public void wanderer() {
        for (int i = 0; i < 15; i++) {
            Monster monster = getRandomMonster();
            if (0 != (monster.mFlags & (Monster.WAKENS | Monster.WANDERS))) {
                monster.wakeUp();
                for (i = 0; i < 25; i++) {
                    Rowcol point = getRandomRowCol(FLOOR | TUNNEL | STAIRS | TOY, null);
                    if (point != null) {
                        int j = levelMen.size();
                        while (--j >= 0) {
                            Man m = levelMen.get(j);
                            if (m.canSee(point.row, point.col)) {
                                break;
                            }
                        }
                        if (j < 0) {
                            monster.putMonsterAt(point.row, point.col);
                            
                            return;
                        }
                    }
                }
                
                return;
            }
        }
    }

    /**
     * Move Aquatars
     * 
     * @param man
     */
    public void moveAquatars(Persona man) {
        /* aquatars get to hit early if man removes his armor */
        for (Monster monster : levelMonsters) {
            if ((monster.itemCharacter == 'A') && monster.monCanGo(man.row, man.col)) {
                monster.ihate = man;
                monster.moveMonster();
                monster.mFlags |= Monster.ALREADY_MOVED;
            }
        }
    }

    /**
     * @param row
     * @param col
     * @return ????
     */
    public boolean imitating(int row, int col) {
        if (0 != (map[row][col] & MONSTER)) {
            Monster monster = levelMonsters.itemAt(row, col);
            
            return monster != null && 0 != (monster.mFlags & Monster.IMITATES);
        }
        
        return false;
    }

    /**
     * Add Monster to the map
     * 
     * @param man
     * @return true if one or more Monster are visible
     */
    public boolean showMonsters(Man man) {
        boolean found = false;
        man.detectMonster = true;
        if (man.blind > 0) {
            return false;
        }
        for (Monster monster : levelMonsters) {
            man.view.addch(monster.row, monster.col, monster.itemCharacter);
            if (0 != (monster.mFlags & Monster.IMITATES)) {
                monster.mFlags &= ~Monster.IMITATES;
                monster.mFlags |= Monster.WAKENS;
            }
            found = true;
        }

        return found;
    }

    /**
     * Add Toy to the map
     * 
     * @param man
     */
    public void showToys(Man man) {
        if (man.blind > 0) {
            return;
        }
        for (Toy toy : levelToys) {
            man.view.addch(toy.row, toy.col, toy.itemCharacter);
        }
    }

    /**
     * Show each trap on the map
     * 
     * @param man
     */
    public void showTraps(Man man) {
        for (Trap trap : levelTraps) {
            man.view.addch(trap.row, trap.col, '^');
        }
    }

    /**
     * @param monster
     * @return true if the Monster finds gold??
     */
    public boolean seekGold(Monster monster) {
        for (Toy toy : levelToys) {
            if (toy.kind != Id.GOLD || 0 != (map[toy.row][toy.col] & MONSTER) || !sees(monster.row, monster.col, toy.row, toy.col)) {
                continue;
            }
            monster.mFlags |= Monster.CAN_FLIT;
            if (monster.monCanGo(toy.row, toy.col)) {
                monster.mFlags &= ~Monster.CAN_FLIT;
                monster.moveMonTo(toy.row, toy.col);
                monster.mFlags |= Monster.ASLEEP;
                monster.mFlags &= ~(Monster.WAKENS | Monster.SEEKS_GOLD);
            } else {
                monster.mFlags &= ~Monster.SEEKS_GOLD;
                monster.mFlags |= Monster.CAN_FLIT;
                monster.moveTo(toy.row, toy.col);
                monster.mFlags &= ~Monster.CAN_FLIT;
                monster.mFlags |= Monster.SEEKS_GOLD;
            }

            return true;
        }

        return false;
    }

    /**
     * Is targetRow,targetCol visible from srcRow,srcCol
     * 
     * @param targetRow
     * @param targetCol
     * @param srcRow
     * @param srcCol
     * @return true if the target location is visible from the src location
     */
    public boolean sees(int targetRow, int targetCol, int srcRow, int srcCol) {
        int ri;
        int ci;
        int dr;
        int dc;

        if (srcRow > targetRow) {
            dr = srcRow - targetRow;
            ri = 1;
        } else {
            dr = targetRow - srcRow;
            ri = -1;
            if (dr == 0)
                ri = 0;
        }
        if (srcCol > targetCol) {
            dc = srcCol - targetCol;
            ci = 1;
        } else {
            dc = targetCol - srcCol;
            ci = -1;
            if (dc == 0) {
                ci = 0;
            }
        }
        // Tunnel case
        if (dr <= 1 && dc <= 1 && 0 != (map[srcRow][srcCol] & TUNNEL)) {
            return 0 != (map[targetRow][targetCol] & (TUNNEL | DOOR | FLOOR));
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
            if (0 == (map[targetRow][targetCol] & FLOOR)) {
                targetCol += ci;
                sum -= dr;
            }
            do {
                targetRow += ri;
                sum += dc;
                if (sum >= dr) {
                    targetCol += ci;
                    sum -= dr;
                }
                if (targetRow == srcRow && targetCol == srcCol) {
                    return true;
                }
            } while (0 != (map[targetRow][targetCol] & FLOOR));
        } else if (dc > 0) {
            int sum = dc >> 1;
            if (0 == (map[targetRow][targetCol] & FLOOR)) {
                targetRow += ri;
                sum -= dc;
            }
            do {
                targetCol += ci;
                sum += dr;
                if (sum >= dc) {
                    targetRow += ri;
                    sum -= dc;
                }
                if (targetRow == srcRow && targetCol == srcCol) {
                    return true;
                }
            } while (0 != (map[targetRow][targetCol] & FLOOR));
        }

        return false;
    }

    // Places near a door...
    /**
     * @param row
     * @param col
     * @return ???
     */
    public Rowcol porch(int row, int col) {
        if (0 != (TUNNEL & map[row + 1][col])) {
            return new Rowcol(row + 1, col);
        }
        if (0 != (TUNNEL & map[row - 1][col])) {
            return new Rowcol(row - 1, col);
        }
        if (0 != (TUNNEL & map[row][col + 1])) {
            return new Rowcol(row, col + 1);
        }
        if (0 != (TUNNEL & map[row][col - 1])) {
            return new Rowcol(row, col - 1);
        }
        
        return null;
    }

    /**
     * @param row
     * @param col
     * @return ???
     */
    public Rowcol foyer(int row, int col) {
        if (0 != (HOLDER & map[row + 1][col])) {
            return new Rowcol(row + 1, col);
        }
        if (0 != (HOLDER & map[row - 1][col])) {
            return new Rowcol(row - 1, col);
        }
        if (0 != (HOLDER & map[row][col + 1])) {
            return new Rowcol(row, col + 1);
        }
        if (0 != (HOLDER & map[row][col - 1])) {
            return new Rowcol(row, col - 1);
        }
        
        return null;
    }

    /**
     * Determine if Monster wake up when the Man enters the room
     * 
     * @param man
     * @param entering
     * @param row
     * @param col
     */
    public void wakeRoom(Man man, boolean entering, int row, int col) {
        ItemList<Monster> itemList = new ItemList<>(4);

        // List the monsters in the room that can be seen and are asleep
        for (Monster monster : levelMonsters) {
            if (0 != (monster.mFlags & Monster.ASLEEP) && sees(row, col, monster.row, monster.col)) {
                itemList.add(monster);
            }
        }
        // It's a party if there are more than 4 sleepy monsters
        int wakePercent = itemList.size() > 4 ? Monster.PARTY_WAKE_PERCENT : Monster.WAKE_PERCENT;
        if (man.stealthy > 0) {
            wakePercent /= (Monster.STEALTH_FACTOR + man.stealthy);
        }

        for (Monster monster : itemList) {
            if (entering) {
                monster.trow = -1;
            } else {
                monster.trow = row;
                monster.tcol = col;
            }
            if (0 != (monster.mFlags & Monster.WAKENS) && rogue.rand.percent(wakePercent)) {
                monster.wakeUp();
            }
        }
    }

    /**
     * @param roomFrom
     * @param roomTo
     * @return true if both Room have the same number of rows
     */
    public abstract boolean sameRow(Room roomFrom, Room roomTo);

    /**
     * @param roomFrom
     * @param roomTo
     * @return true if both Room have the same number of columns
     */
    public abstract boolean sameCol(Room roomFrom, Room roomTo);

    /**
     * @param row
     * @param col
     * @return true if there is a room at 
     */
    public abstract Room roomAt(int row, int col);

    /**
     * @param room
     * @return Array of Room ???
     */
    public abstract Room[] nabes(Room room);

    /**
     * @return map initialized with what has been seen
     */
    public char[][] initSeen() {
        System.out.println("Level.initSeen");
        char[][] see = new char[numRow][numCol];
        for (int row = 0; row < numRow; row++) {
            for (int col = 0; col < numCol; col++) {
                if (0 == (map[row][col] & (STAIRS | FLOOR))) {
                    char seeValue = 0; // Opaque cell
                    for (int k = 0; k < 8; k++) {
                        int targetRow = row + Id.X_TABLE[k];
                        int targetCol = col + Id.Y_TABLE[k];
                        if (targetRow >= 0 && targetRow < numRow && targetCol >= 0 && targetCol < numCol) {
                            if (0 != (map[targetRow][targetCol] & (FLOOR | DOOR))) {
                                seeValue = 2; // Cell neighbors transparent cell
                                break;
                            }
                        }
                    }
                    see[row][col] = seeValue;
                } else {
                    see[row][col] = 1;
                }
            }
        }

        return see;
    }

    /**
     * @param row
     * @param col
     * @param toy
     * @return ???
     */
    public boolean tryToCough(int row, int col, Toy toy) {
        if (row < MIN_ROW || row > numRow - 2 || col < 0 || col > numCol - 1) {
            return false;
        }
        if (0 == (map[row][col] & (TOY | STAIRS | TRAP)) && 0 != (map[row][col] & (TUNNEL | FLOOR | DOOR))) {
            toy.placeAt(row, col, TOY);
            if (0 == (map[row][col] & (MONSTER | MAN))) {
                rogue.mark(row, col);
            }
            
            return true;
        }
        
        return false;
    }
}
