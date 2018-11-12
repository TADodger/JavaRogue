package rogue;

import java.io.Serializable;

class Scroll extends Toy implements Serializable {
    private static final long serialVersionUID = 3154221148022434821L;

    Scroll(Level level) {
        super(level, Id.SCROLL);
    }

    void readby() {
        switch (kind) {
            case Id.SCARE_MONSTER:
                owner.tell("you hear a maniacal laughter in the distance");
                break;
            case Id.HOLD_MONSTER:
                hold_monster();
                break;
            case Id.ENCH_WEAPON:
                if (owner.weapon != null) {
                    if (0 != (owner.weapon.kind & Id.WEAPON)) {
                        String plural = owner.weapon.quantity <= 1 ? "s " : " ";
                        owner.tell("your " + owner.weapon.name() + "glow" + plural + owner.get_ench_color());
                        if (level.self.rand.coin()) {
                            owner.weapon.hit_enchant++;
                        } else {
                            owner.weapon.d_enchant++;
                        }
                    }
                    owner.weapon.is_cursed = false;
                } else {
                    owner.tell("your hands tingle");
                }
                break;
            case Id.ENCH_ARMOR:
                if (owner.armor != null) {
                    owner.tell("your armor glows " + owner.get_ench_color() + "for a moment");
                    owner.armor.d_enchant++;
                    owner.armor.is_cursed = false;
                    owner.print_stat();
                } else {
                    owner.tell("your skin crawls");
                }
                break;
            case Id.IDENTIFY:
                if (owner instanceof Man) {
                    owner.tell("this is a scroll of identify");
                    identified = true;
                    Id.idScrolls[kind & 255].idStatus = Id.IDENTIFIED;
                    Toy t = ((Man) owner).find(Id.ALL_TOYS, "what would you like to identify?", "");
                    if (t != null) {
                        t.identified = true;
                        Id.identify(t.kind);
                        owner.tell(t.get_desc());
                    }
                }
                break;
            case Id.TELEPORT:
                owner.tele();
                break;
            case Id.SLEEP:
                owner.describe(owner.who("fall") + "asleep", false);
                owner.take_a_nap();
                break;
            case Id.PROTECT_ARMOR:
                if (owner.armor != null) {
                    owner.tell("your armor is covered by a shimmering gold shield");
                    owner.armor.is_protected = true;
                    owner.armor.is_cursed = false;
                } else {
                    owner.tell("your acne seems to have disappeared");
                }
                break;
            case Id.REMOVE_CURSE:
                if (owner instanceof Man) {
                    owner.tell(owner.halluc == 0 ? "you feel as though someone is watching over you" : "you feel in touch with the universal oneness");
                    ((Man) owner).pack.uncurse_all();
                }
                break;
            case Id.CREATE_MONSTER:
                if (!create_monster(owner)) {
                    owner.tell("you hear a faint cry of anguish in the distance");
                }
                break;
            case Id.AGGRAVATE_MONSTER:
                owner.tell("you hear a high pitched humming noise");
                aggravate(owner);
                break;
            case Id.MAGIC_MAPPING:
                owner.tell("this scroll seems to have a map on it");
                if (owner instanceof Man) {
                    level.draw_magic_map((Man) owner);
                }
                break;
            case Id.CON_MON:
                owner.con_mon = true;
                owner.tell("your hands glow " + owner.get_ench_color() + "for a moment");
                break;
        }
        Id.identifyUncalled(kind);
        vanish();
    }

    boolean create_monster(Persona man) {
        int perm[] = level.self.rand.permute(9);
        for (int i = 0; i < 9; i++) {
            int c = Id.xtab[perm[i]] + man.col;
            int r = Id.ytab[perm[i]] + man.row;
            if ((r == man.row && c == man.col) || r < MIN_ROW || r > level.nrow - 2 || c < 0 || c > level.ncol - 1) {
                continue;
            }
            if (0 == (level.map[r][c] & MONSTER) && 0 != (level.map[r][c] & (FLOOR | TUNNEL | STAIRS | DOOR))) {
                Monster monster = level.gr_monster();
                monster.put_m_at(r, c);
                level.self.vset(r, c);
                if (0 != (monster.m_flags & (Monster.WANDERS | Monster.WAKENS))) {
                    monster.wake_up();
                }
                return true;
            }
        }

        return false;
    }

    void aggravate(Persona man) {
        for (Item item : level.level_monsters) {
            Monster monster = (Monster) item;
            monster.wake_up();
            monster.m_flags &= ~Monster.IMITATES;
            level.self.vset(monster.row, monster.col);
        }
    }

    void hold_monster() {
        int mcount = 0;
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                int r = owner.row + i;
                int c = owner.col + j;
                if (r < MIN_ROW || r > level.nrow - 2 || c < 0 || c > level.ncol - 1) {
                    continue;
                }
                if (0 != (level.map[r][c] & MONSTER)) {
                    Monster monster = (Monster) level.level_monsters.item_at(r, c);
                    if (monster != null && monster != owner) {
                        monster.m_flags |= Monster.ASLEEP;
                        monster.m_flags &= ~Monster.WAKENS;
                        mcount++;
                    }
                }
            }
        }
        String s = "the monsters around " + owner.who() + " freeze";
        if (mcount == 0) {
            s = owner.who("feel") + "a strange sense of loss";
        } else if (mcount == 1) {
            s = "the monster freezes";
        }
        owner.describe(s, false);
    }
}
