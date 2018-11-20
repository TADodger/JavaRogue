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
                holdMonster();
                break;
            case Id.ENCH_WEAPON:
                if (owner.weapon != null) {
                    if (0 != (owner.weapon.kind & Id.WEAPON)) {
                        String plural = owner.weapon.quantity <= 1 ? "s " : " ";
                        owner.tell("your " + owner.weapon.name() + "glow" + plural + owner.getEnchColor());
                        if (level.rogue.rand.coin()) {
                            owner.weapon.hitEnchant++;
                        } else {
                            owner.weapon.dEnchant++;
                        }
                    }
                    owner.weapon.isCursed = false;
                } else {
                    owner.tell("your hands tingle");
                }
                break;
            case Id.ENCH_ARMOR:
                if (owner.armor != null) {
                    owner.tell("your armor glows " + owner.getEnchColor() + "for a moment");
                    owner.armor.dEnchant++;
                    owner.armor.isCursed = false;
                    owner.printStat();
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
                        owner.tell(t.getDesc());
                    }
                }
                break;
            case Id.TELEPORT:
                owner.tele();
                break;
            case Id.SLEEP:
                owner.describe(owner.who("fall") + "asleep", false);
                owner.takeANap();
                break;
            case Id.PROTECT_ARMOR:
                if (owner.armor != null) {
                    owner.tell("your armor is covered by a shimmering gold shield");
                    owner.armor.isProtected = true;
                    owner.armor.isCursed = false;
                } else {
                    owner.tell("your acne seems to have disappeared");
                }
                break;
            case Id.REMOVE_CURSE:
                if (owner instanceof Man) {
                    owner.tell(owner.halluc == 0 ? "you feel as though someone is watching over you" : "you feel in touch with the universal oneness");
                    ((Man) owner).pack.uncurseAll();
                }
                break;
            case Id.CREATE_MONSTER:
                if (!createMonster(owner)) {
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
                    level.drawMagicMap((Man) owner);
                }
                break;
            case Id.CON_MON:
                owner.conMon = true;
                owner.tell("your hands glow " + owner.getEnchColor() + "for a moment");
                break;
        }
        Id.identifyUncalled(kind);
        vanish();
    }

    boolean createMonster(Persona man) {
        int perm[] = level.rogue.rand.permute(9);
        for (int i = 0; i < 9; i++) {
            int c = Id.X_TABLE[perm[i]] + man.col;
            int r = Id.Y_TABLE[perm[i]] + man.row;
            if ((r == man.row && c == man.col) || r < MIN_ROW || r > level.numRow - 2 || c < 0 || c > level.numCol - 1) {
                continue;
            }
            if (0 == (level.map[r][c] & MONSTER) && 0 != (level.map[r][c] & (FLOOR | TUNNEL | STAIRS | DOOR))) {
                Monster monster = level.getRandomMonster();
                monster.putMonsterAt(r, c);
                level.rogue.vset(r, c);
                if (0 != (monster.mFlags & (Monster.WANDERS | Monster.WAKENS))) {
                    monster.wakeUp();
                }
                return true;
            }
        }

        return false;
    }

    void aggravate(Persona man) {
        for (Monster monster : level.levelMonsters) {
            monster.wakeUp();
            monster.mFlags &= ~Monster.IMITATES;
            level.rogue.vset(monster.row, monster.col);
        }
    }

    void holdMonster() {
        int mcount = 0;
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                int r = owner.row + i;
                int c = owner.col + j;
                if (r < MIN_ROW || r > level.numRow - 2 || c < 0 || c > level.numCol - 1) {
                    continue;
                }
                if (0 != (level.map[r][c] & MONSTER)) {
                    Monster monster = level.levelMonsters.itemAt(r, c);
                    if (monster != null && monster != owner) {
                        monster.mFlags |= Monster.ASLEEP;
                        monster.mFlags &= ~Monster.WAKENS;
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
