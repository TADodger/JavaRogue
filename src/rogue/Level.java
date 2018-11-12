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
    ItemList level_men;
    ItemList level_toys;
    ItemList level_monsters;
    ItemList level_traps;
    ItemList level_doors;
    int map[][];
    int ncol; // Width
    int nrow; // Height
    // static int cur_level; // Level number
    // static int max_level;
    int cur_level;
    int max_level;
    int my_level;
    int foods = 0; // Higher levels get more food

    Level(int nrow, int ncol, Rogue self) {
        this.self = self;
        this.nrow = nrow;
        this.ncol = ncol;
        this.cur_level = self.cur_level;
        this.max_level = self.max_level;
        init();
    }

    void init() {
        map = new int[nrow][ncol];

        for (int r = 0; r < nrow; r++) {
            for (int c = 0; c < ncol; c++) {
                map[r][c] = 0;
            }
        }

        level_monsters = new ItemList(6); // Start with a few monsters
        level_doors = new ItemList(8); // and a few doors too
        level_men = new ItemList();
        level_toys = new ItemList(6);
        level_traps = new ItemList(3);
    }

    void mark(int r, int c) {
        int i = level_men.size();
        while (--i >= 0) {
            ((Man) level_men.get(i)).view.mark(r, c);
        }
    }

    char get_char(int row, int col) {
        if (row < 0 || row >= nrow || col < 0 || col >= ncol)
            return (char) 0;
        int mask = map[row][col];

        if (0 != (mask & TOY)) {
            Toy toy = (Toy) level_toys.item_at(row, col);
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

    boolean is_passable(int row, int col) {
        if (row < MIN_ROW || row > (nrow - 2) || col < 0 || col > (ncol - 1)) {
            return false;
        }
        if (0 != (map[row][col] & HIDDEN)) {
            return 0 != (map[row][col] & TRAP);
        }
        
        return 0 != (map[row][col] & (FLOOR | TUNNEL | DOOR | STAIRS | TRAP));
    }

    boolean can_turn(int row, int col) {
        return 0 != (TUNNEL & map[row][col]) && is_passable(row, col);
    }

    boolean can_move(int row1, int col1, int row2, int col2) {
        if (!is_passable(row2, col2)) {
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
    Rowcol gr_row_col(int mask, Item itm) {
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

    void plant_gold(int row, int col, int gold) {
        Toy obj = new Toy(this, Id.GOLD);
        obj.quantity = gold;
        obj.place_at(row, col, TOY);
    }

    Scroll gr_scroll() {
        Scroll t = new Scroll(this);
        t.kind = Id.grWhichScroll(self.rand);
        return t;
    }

    Potion gr_potion() {
        Potion t = new Potion(this);
        t.kind = Id.grWhichPotion(self.rand);
        return t;
    }

    Toy gr_weapon(int assign_wk) {
        if (assign_wk < 0)
            assign_wk = self.rand.get(Id.idWeapons.length - 1);
        return new Toy(this, Id.WEAPON | assign_wk);
    }

    Toy gr_armor() {
        return new Toy(this, Id.ARMOR + self.rand.get(Id.idArmors.length - 1));
    }

    Toy gr_wand() {
        return new Toy(this, Id.WAND + self.rand.get(Id.idWands.length - 1));
    }

    Toy gr_ring(int assign_wk) {
        if (assign_wk < 0)
            assign_wk = self.rand.get(Id.idRings.length - 1);
        return new Toy(this, Id.RING + assign_wk);
    }

    Toy get_food(boolean force_ration) {
        return new Toy(this, force_ration || self.rand.percent(80) ? Id.RATION : Id.FRUIT);
    }

    Toy gr_toy() {
        int k;
        if (foods < cur_level / 3) {
            k = Id.FOOD;
            foods++;
        } else
            k = Id.grSpecies(self.rand);
        switch (k) {
            case Id.SCROLL:
                return (Toy) gr_scroll();
            case Id.POTION:
                return (Toy) gr_potion();
            case Id.WEAPON:
                return (Toy) gr_weapon(-1);
            case Id.ARMOR:
                return (Toy) gr_armor();
            case Id.WAND:
                return (Toy) gr_wand();
            case Id.FOOD:
                return (Toy) get_food(false);
            case Id.RING:
                return (Toy) gr_ring(-1);
        }
        return null;
    }

    Toy wiztoy(Man man, int ch) {
        Toy t = null;
        int max = 0;
        switch (ch) {
            case '!':
                t = (Toy) gr_potion();
                max = Id.idPotions.length;
                break;
            case '?':
                t = (Toy) gr_scroll();
                max = Id.idScrolls.length;
                break;
            case ',':
                t = new Toy(this, Id.AMULET);
                break;
            case ':':
                t = (Toy) get_food(false);
                break;
            case ')':
                max = Id.idWeapons.length;
                break;
            case ']':
                t = (Toy) gr_armor();
                max = Id.idArmors.length;
                break;
            case '/':
                t = (Toy) gr_wand();
                max = Id.idWands.length;
                break;
            case '=':
                max = Id.idRings.length;
                break;
        }
        --max;
        if (ch == ',' || ch == ':')
            return t;
        if (max < 0)
            return null;
        String buf = man.view.msg.get_input_line("which kind of " + ((char) ch) + " (0 to " + max + ")?", "", "", false, true);
        man.view.msg.checkMessage();
        if (buf != null) {
            int wk = -1;
            try {
                wk = Integer.parseInt(buf);
            } catch (NumberFormatException e) {
            }
            if (wk >= 0 && wk <= max) {
                if (ch == '=')
                    t = gr_ring(wk);
                else if (ch == ')')
                    t = gr_weapon(wk);
                else if (t != null)
                    t.kind = (t.kind & Id.ALL_TOYS) + wk;
                return t;
            }
        }
        return null;
    }

    void put_toys() {
        if (cur_level < max_level)
            return;
        int n = self.rand.get(2, 4);
        if (self.rand.coin())
            n++;
        while (self.rand.percent(33))
            n++;
        for (int i = 0; i < n; i++) {
            Rowcol pt = gr_row_col(FLOOR | TUNNEL, null);
            if (pt != null) {
                Toy t = gr_toy();
                t.place_at(pt.row, pt.col, TOY);
            }
        }
    }

    Rowcol get_dir_rc(int dir, int row, int col, boolean allow_off_screen) {
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
        if (allow_off_screen || (row > MIN_ROW && row < nrow - 2 && col > 0 && col < ncol - 1))
            return new Rowcol(row, col);
        return null;
    }

    void put_player(Man man) {
        /* try not to put where he can see his current position */
        int misses = 2;
        Rowcol pt;
        do {
            pt = gr_row_col(FLOOR | TUNNEL | TOY | STAIRS, man);
            if (pt == null)
                return;
        } while (--misses >= 0 && sees(pt.row, pt.col, man.row, man.col));
        man.place_at(pt.row, pt.col, MAN);
        wake_room(man, true, pt.row, pt.col);
        if (man.new_level_message != null) {
            self.tell(man, man.new_level_message, false);
            man.new_level_message = null;
        }
    }

    void draw_magic_map(Man man) {
        for (int i = 0; i < nrow; i++)
            for (int j = 0; j < ncol; j++) {
                int s = map[i][j];
                if (0 != (s & (HORWALL | VERTWALL | DOOR | TUNNEL | TRAP | STAIRS))) {
                    if (0 == (s & MONSTER)) {
                        man.seen[i][j] = (char) ((man.seen[i][j] & 15) | man.wallcode(get_char(i, j)));
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

    Monster get_zapped_monster(int dir, int row, int col) {
        for (;;) {
            int ocol = col;
            int orow = row;
            Rowcol pt = get_dir_rc(dir, row, col, false);
            row = pt.row;
            col = pt.col;
            if ((row == orow && col == ocol) || 0 != (map[row][col] & (HORWALL | VERTWALL)) || 0 == (map[row][col] & SOMETHING))
                break;
            if (0 != (map[row][col] & MONSTER)) {
                Monster monster = (Monster) (level_monsters.item_at(row, col));
                if (!imitating(row, col))
                    return monster;
            }
        }
        return null;
    }

    void wdrain_life(Man man, Monster monster) {
        int hp = man.hp_current / 3;
        man.hp_current = (man.hp_current + 1) / 2;

        for (Item item : level_monsters) {
            Monster lmon = (Monster) item;
            if (sees(lmon.row, lmon.col, man.row, man.col)) {
                lmon.wake_up();
                lmon.damage(man, hp, 0);
                monster = null;
            }
        }
        if (monster != null) {
            monster.wake_up();
            monster.damage(man, hp, 0);
        }
        man.print_stat();
        man.view.markall(); // relight
    }

    static int btime;

    void bounce(Toy obj, int dir, int row, int col, int r) {
        boolean fiery = (obj.kind == Id.FIRE);
        int orow;
        int ocol;
        int i;
        int new_dir = -1;
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
        self.md_sleep(100);
        orow = row;
        ocol = col;
        Rowcol pt;
        do {
            self.flashadd(orow, ocol, uBrite);
            pt = get_dir_rc(dir, orow, ocol, true);
            orow = pt.row;
            ocol = pt.col;
        } while (!(ocol <= 0 || ocol >= ncol - 1 || 0 == (map[orow][ocol] & SOMETHING) || 0 != (map[orow][ocol] & MONSTER) || 0 != (map[orow][ocol] & (HORWALL | VERTWALL))
                || (orow == owner.row && ocol == owner.col)));
        self.xflash();
        do {
            orow = row;
            ocol = col;
            self.vset(row, col);
            pt = get_dir_rc(dir, row, col, true);
            row = pt.row;
            col = pt.col;
        } while (!(col <= 0 || col >= ncol - 1 || 0 == (map[row][col] & SOMETHING) || 0 != (map[row][col] & MONSTER) || 0 != (map[row][col] & (HORWALL | VERTWALL))
                || (row == owner.row && col == owner.col)));

        if (0 != (map[row][col] & MONSTER)) {
            Monster monster = (Monster) level_monsters.item_at(row, col);
            if (monster != null) {
                doend = monster.zapt(obj);
            }
        } else {
            for (View view : self.view_list) {
                Man man = view.man;
                if (row == man.row && col == man.col) {
                    int ac = man.armor == null ? 0 : man.armor.get_armor_class();
                    if (self.rand.percent(10 + (3 * ac))) {
                        self.describe(man, "the " + s + " misses", false);
                    } else {
                        doend = false;
                        damage = self.rand.get(3, (3 * man.exp));
                        if (fiery) {
                            damage = (damage * 3) / 2;
                            if (man.armor != null)
                                damage -= man.armor.get_armor_class();
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
                pt = get_dir_rc(dir, nr, nc, true);
                nr = pt.row;
                nc = pt.col;
                if ((nc >= 0 && nc <= ncol - 1) && 0 != (map[nr][nc] & SOMETHING) && 0 == (map[nr][nc] & (VERTWALL | HORWALL))) {
                    new_dir = dir;
                    break;
                }
            }
            if (new_dir != -1) {
                bounce(obj, new_dir, orow, ocol, r);
            }
        }
    }

    void put_monsters() {
        int n = self.rand.get(4, 6);

        for (int i = 0; i < n; i++) {
            Rowcol pt = gr_row_col(FLOOR | TUNNEL | STAIRS | TOY, null);
            if (pt == null) {
                continue;
            }
            Monster monster = gr_monster();
            if (0 != (monster.m_flags & Monster.WANDERS) && self.rand.coin()) {
                monster.wake_up();
            }
            monster.put_m_at(pt.row, pt.col);
            // System.out.println("Monster " + monster);
        }
    }

    Monster gr_monster() {
        int mn;

        for (;;) {
            mn = self.rand.get(Monster.MONSTERS - 1);
            if ((cur_level >= Monster.MON_TAB[mn].first_level) && (cur_level <= Monster.MON_TAB[mn].last_level)) {
                break;
            }
        }
        Monster monster = new Monster(this, mn);
        if (0 != (monster.m_flags & Monster.IMITATES)) {
            monster.disguise = Id.grObjectCharacter(self.rand);
        }
        if (cur_level > AMULET_LEVEL + 2) {
            monster.m_flags |= Monster.HASTED;
        }
        monster.trow = -1;
        
        return monster;
    }

    int gmc_row_col(Man man, int row, int col) {
        Monster monster = (Monster) level_monsters.item_at(row, col);
        
        return monster != null ? monster.gmc(man) : '&';
    }

    void mv_mons(Man man) { // Move all the monsters
        if (0 != (man.haste_self % 2)) {
            return;
        }
        for (Iterator<Item> it = level_monsters.iterator();it.hasNext() && !Man.game_over;) {
            Monster monster = (Monster) it.next();
            monster.dstrow = man.row;
            monster.dstcol = man.col;
            if (0 != (monster.m_flags & Monster.HASTED)) {
                monster.mv_monster();
                if (!level_monsters.contains(monster))
                    continue;
            } else if (0 != (monster.m_flags & Monster.SLOWED)) {
                monster.slowed_toggle = !monster.slowed_toggle;
                if (monster.slowed_toggle)
                    continue;
            }
            if (0 != (monster.m_flags & Monster.CONFUSED) && monster.move_confused()) {
                continue;
            }
            boolean flew = false;
            if (0 != (monster.m_flags & Monster.FLIES) && 0 == (monster.m_flags & Monster.NAPPING) && (monster.ihate == null || !monster.mon_can_go(monster.ihate.row, monster.ihate.col))) {
                flew = true;
                monster.mv_monster();
                if (!level_monsters.contains(monster)) {
                    continue;
                }
            }
            if (!(flew && (monster.ihate == null || monster.mon_can_go(monster.ihate.row, monster.ihate.col)))) {
                monster.mv_monster();
            }
        }
    }

    void wanderer() {
        for (int i = 0; i < 15; i++) {
            Monster monster = gr_monster();
            if (0 != (monster.m_flags & (Monster.WAKENS | Monster.WANDERS))) {
                monster.wake_up();
                for (i = 0; i < 25; i++) {
                    Rowcol pt = gr_row_col(FLOOR | TUNNEL | STAIRS | TOY, null);
                    if (pt != null) {
                        int j = level_men.size();
                        while (--j >= 0) {
                            Man m = (Man) level_men.get(j);
                            if (m.can_see(pt.row, pt.col)) {
                                break;
                            }
                        }
                        if (j < 0) {
                            monster.put_m_at(pt.row, pt.col);
                            
                            return;
                        }
                    }
                }
                
                return;
            }
        }
    }

    void mv_aquatars(Persona man) {
        /* aquatars get to hit early if man removes his armor */
        for (Item item : level_monsters) {
            Monster monster = (Monster) item;
            if ((monster.ichar == 'A') && monster.mon_can_go(man.row, man.col)) {
                monster.ihate = man;
                monster.mv_monster();
                monster.m_flags |= Monster.ALREADY_MOVED;
            }
        }
    }

    boolean imitating(int r, int c) {
        if (0 != (map[r][c] & MONSTER)) {
            Monster monster = (Monster) level_monsters.item_at(r, c);
            return monster != null && 0 != (monster.m_flags & Monster.IMITATES);
        }
        
        return false;
    }

    boolean show_monsters(Man man) {
        boolean found = false;
        man.detect_monster = true;
        if (man.blind > 0) {
            return false;
        }
        for (Item item : level_monsters) {
            Monster monster = (Monster) item;
            man.view.addch(monster.row, monster.col, monster.ichar);
            if (0 != (monster.m_flags & Monster.IMITATES)) {
                monster.m_flags &= ~Monster.IMITATES;
                monster.m_flags |= Monster.WAKENS;
            }
            found = true;
        }
        return found;
    }

    void show_toys(Man man) {
        if (man.blind > 0) {
            return;
        }
        for (Item item : level_toys) {
            Toy t = (Toy) item;
            man.view.addch(t.row, t.col, (char) t.ichar);
        }
    }

    void show_traps(Man man) {
        for (Item t : level_traps) {
            man.view.addch(t.row, t.col, '^');
        }
    }

    boolean seek_gold(Monster monster) {
        for (Item item : level_toys) {
            Toy gold = (Toy) item;
            if (gold.kind != Id.GOLD || 0 != (map[gold.row][gold.col] & MONSTER) || !sees(monster.row, monster.col, gold.row, gold.col)) {
                continue;
            }
            monster.m_flags |= Monster.CAN_FLIT;
            if (monster.mon_can_go(gold.row, gold.col)) {
                monster.m_flags &= ~Monster.CAN_FLIT;
                monster.move_mon_to(gold.row, gold.col);
                monster.m_flags |= Monster.ASLEEP;
                monster.m_flags &= ~(Monster.WAKENS | Monster.SEEKS_GOLD);
            } else {
                monster.m_flags &= ~Monster.SEEKS_GOLD;
                monster.m_flags |= Monster.CAN_FLIT;
                monster.mv_to(gold.row, gold.col);
                monster.m_flags &= ~Monster.CAN_FLIT;
                monster.m_flags |= Monster.SEEKS_GOLD;
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

    void wake_room(Man man, boolean entering, int row, int col) {
        ItemList v = new ItemList(4);

        // List the monsters in the room that can be seen and are asleep
        for (Item item : level_monsters) {
            Monster monster = (Monster) item;
            if (0 != (monster.m_flags & Monster.ASLEEP) && sees(row, col, monster.row, monster.col))
                v.add(monster);
        }
        // It's a party if there are more than 4 sleepy monsters
        int wake_percent = v.size() > 4 ? Monster.PARTY_WAKE_PERCENT : Monster.WAKE_PERCENT;
        if (man.stealthy > 0) {
            wake_percent /= (Monster.STEALTH_FACTOR + man.stealthy);
        }

        for (Item item : v) {
            Monster monster = (Monster) item;
            if (entering)
                monster.trow = -1;
            else {
                monster.trow = row;
                monster.tcol = col;
            }
            if (0 != (monster.m_flags & Monster.WAKENS) && self.rand.percent(wake_percent)) {
                monster.wake_up();
            }
        }
    }

    boolean same_row(Room rfr, Room rto) {
        return false;
    }

    boolean same_col(Room rfr, Room rto) {
        return false;
    }

    Room room_at(int row, int col) {
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

    char[][] init_seen() {
        System.out.println("Level.init_seen");
        char[][] see = new char[nrow][ncol];
        for (int r = 0; r < nrow; r++) {
            for (int c = 0; c < ncol; c++) {
                if (0 == (map[r][c] & (STAIRS | FLOOR))) {
                    char s = 0; // Opaque cell
                    for (int k = 0; k < 8; k++) {
                        int r1 = r + Id.xtab[k];
                        int c1 = c + Id.ytab[k];
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

    boolean try_to_cough(int r, int c, Toy obj) {
        if (r < MIN_ROW || r > nrow - 2 || c < 0 || c > ncol - 1) {
            return false;
        }
        if (0 == (map[r][c] & (TOY | STAIRS | TRAP)) && 0 != (map[r][c] & (TUNNEL | FLOOR | DOOR))) {
            obj.place_at(r, c, TOY);
            if (0 == (map[r][c] & (MONSTER | MAN))) {
                self.mark(r, c);
            }
            
            return true;
        }
        
        return false;
    }
}
