package rogue;

import java.io.Serializable;
import java.util.List;

class Toy extends Item implements Serializable {
    private static final long serialVersionUID = -7095180483769968267L;

    // The stuff you can pick up
    String damage = "1d1";
    int quantity = 1;
    int killExp;
    boolean isProtected;
    boolean isCursed = false;
    int klass;
    boolean identified = false;
    int dEnchant;
    int quiver;
    int hitEnchant;
    int kind;
    boolean pickedUp = false; /* sleep from wand of sleep */
    int inUseFlags = 0;
    Persona owner; /* Who is carrying this item */

    static String curseMessage = "you can't, it appears to be cursed";

    Toy(Level level, int kind) {
        super(level, 0, 0);

        int percent;
        int blessing;
        int increment;

        this.kind = kind;
        this.itemCharacter = Id.getMaskCharacter(kind);
        switch (kind & Id.ALL_TOYS) {
            case Id.WEAPON:
                if (kind == Id.ARROW || kind == Id.DAGGER || kind == Id.SHURIKEN || kind == Id.DART) {
                    this.quantity = level.rogue.rand.get(3, 15);
                    this.quiver = level.rogue.rand.get(126);
                } else {
                    this.quantity = 1;
                }
                this.hitEnchant = this.dEnchant = 0;

                percent = level.rogue.rand.get(1, 96);
                blessing = level.rogue.rand.get(1, 3);
                increment = 1;

                if (percent > 16 && percent <= 32) {
                    increment = -1;
                    this.isCursed = true;
                }
                if (percent <= 32) {
                    for (int i = 0; i < blessing; i++) {
                        if (level.rogue.rand.coin()) {
                            this.hitEnchant += increment;
                        } else {
                            this.dEnchant += increment;
                        }
                    }
                }
                switch (kind) {
                    case Id.BOW:
                    case Id.DART:
                        this.damage = "1d1";
                        break;
                    case Id.ARROW:
                        this.damage = "1d2";
                        break;
                    case Id.DAGGER:
                        this.damage = "1d3";
                        break;
                    case Id.SHURIKEN:
                        this.damage = "1d4";
                        break;
                    case Id.MACE:
                        this.damage = "2d3";
                        break;
                    case Id.LONG_SWORD:
                        this.damage = "3d4";
                        break;
                    case Id.TWO_HANDED_SWORD:
                        this.damage = "4d5";
                        break;
                }
                break;
            case Id.ARMOR:
                klass = (255 & kind) + 2;
                if (kind == Id.PLATE || kind == Id.SPLINT) {
                    klass--;
                }
                isProtected = false;
                dEnchant = 0;

                percent = level.rogue.rand.get(1, 100);
                blessing = level.rogue.rand.get(1, 3);

                if (percent <= 16) {
                    isCursed = true;
                    dEnchant -= blessing;
                } else if (percent <= 33) {
                    dEnchant += blessing;
                }
                break;
            case Id.WAND:
                klass = level.rogue.rand.get(3, 7);
                break;
            case Id.RING:
                klass = 0;
                switch (kind) {
                    case Id.STEALTH:
                        break;
                    case Id.SLOW_DIGEST:
                        break;
                    case Id.REGENERATION:
                        break;
                    case Id.R_SEE_INVISIBLE:
                        break;
                    case Id.SUSTAIN_STRENGTH:
                        break;
                    case Id.MAINTAIN_ARMOR:
                        break;
                    case Id.SEARCHING:
                        break;
                    case Id.R_TELEPORT:
                        isCursed = true;
                        break;
                    case Id.ADD_STRENGTH:
                    case Id.DEXTERITY:
                        while ((klass = level.rogue.rand.get(4) - 2) == 0)
                            ;
                        isCursed = klass < 0;
                        break;
                    case Id.ADORNMENT:
                        isCursed = level.rogue.rand.coin();
                        break;
                }
                break;
            default:
                break;
        }
    }

    Toy(Toy t) { // New copy
        super(t.level, t.row, t.col);
        damage = new String(t.damage);
        killExp = t.killExp;
        isProtected = t.isProtected;
        isCursed = t.isCursed;
        klass = t.klass;
        identified = t.identified;
        dEnchant = t.dEnchant;
        quiver = t.quiver;
        hitEnchant = t.hitEnchant;
        kind = t.kind;
        inUseFlags = Id.NOT_USED;
        owner = t.owner;
        this.itemCharacter = Id.getMaskCharacter(kind);
    }

    Toy checkDuplicate(List<Item> pack) {
        if (0 == (kind & (Id.WEAPON | Id.FOOD | Id.SCROLL | Id.POTION))) {
            return null;
        }
        if (kind == Id.FRUIT) {
            return null;
        }
        int i = pack.size();
        while (--i >= 0) {
            Toy op = (Toy) pack.get(i);
            if (op.kind == kind) {
                if (0 == (kind & Id.WEAPON) || ((kind == Id.ARROW || kind == Id.DAGGER || kind == Id.DART || kind == Id.SHURIKEN) && quiver == op.quiver)) {
                    op.quantity += quantity;

                    return op;
                }
            }
        }

        return null;
    }

    Toy addToPack(Man man) {
        // Return the pack and the added toy
        Toy pdup = checkDuplicate(man.pack);
        if (null != pdup) {
            return pdup;
        }
        itemCharacter = man.pack.nextAvailItemChar();
        placeAt(-1, -1, TOY);
        int k = itemCharacter - 'a';
        if (k < man.pack.size()) {
            man.pack.add(k, this);
        } else {
            man.pack.add(this);
        }
        owner = man;
        
        return this;
    }

    int getArmorClass() {
        return klass + dEnchant;
    }

    String getDesc() {
        return Id.getDescription(this);
    }

    String name() {
        String retstring = "unknown ";
        switch (kind & Id.ALL_TOYS) {
            case Id.SCROLL:
                retstring = quantity > 1 ? "scrolls " : "scroll ";
                break;
            case Id.POTION:
                retstring = quantity > 1 ? "potions " : "potion ";
                break;
            case Id.FOOD:
                if (kind == Id.RATION || owner == null || !(owner instanceof Man)) {
                    retstring = "food ";
                } else {
                    retstring = ((Man) owner).option.fruit;
                }
                break;
            case Id.WAND:
                retstring = Id.IS_WOOD[kind & 255] ? "staff " : "wand ";
                break;
            case Id.WEAPON:
                switch (kind) {
                    case Id.DART:
                        retstring = quantity > 1 ? "darts " : "dart ";
                        break;
                    case Id.ARROW:
                        retstring = quantity > 1 ? "arrows " : "arrow ";
                        break;
                    case Id.DAGGER:
                        retstring = quantity > 1 ? "daggers " : "dagger ";
                        break;
                    case Id.SHURIKEN:
                        retstring = quantity > 1 ? "shurikens " : "shuriken ";
                        break;
                    default:
                        retstring = Id.idWeapons[kind & 255].title;
                }
                break;
            case Id.ARMOR:
                retstring = "armor ";
                break;
            case Id.RING:
                retstring = "ring ";
                break;
            case Id.AMULET:
                retstring = "amulet ";
                break;
        }
        return retstring;
    }

    void drop() {
        if (0 != (inUseFlags & Id.BEING_WIELDED)) {
            if (isCursed) {
                owner.tell(curseMessage);

                return;
            }
            owner.unwield();
        } else if (0 != (inUseFlags & Id.BEING_WORN)) {
            if (isCursed) {
                owner.tell(curseMessage);
                
                return;
            }
            level.moveAquatars(owner);
            owner.unwear();
            owner.printStat();
        } else if (0 != (inUseFlags & Id.ON_EITHER_HAND)) {
            if (isCursed) {
                owner.tell(curseMessage);
                
                return;
            }
            unPutOn();
        }
        Toy obj = this;
        if (quantity > 1 && 0 == (kind & Id.WEAPON)) {
            quantity--;
            obj = new Toy(obj);
        } else if (owner instanceof Man) {
            itemCharacter = Id.getMaskCharacter(kind);
            ((Man) owner).pack.remove(this);
        }
        obj.placeAt(owner.row, owner.col, TOY);
        owner.self.checkMessage(owner);
        owner.self.describe(owner, "dropped " + obj.getDesc(), false);
        owner.regMove();
        obj.owner = null;
    }

    void identify() {
        Id.identify(kind);
    }

    void unPutOn() {
        if (this == owner.leftRing) {
            inUseFlags &= ~Id.ON_LEFT_HAND;
            owner.leftRing = null;
        } else if (this == owner.rightRing) {
            owner.rightRing = null;
            inUseFlags &= ~Id.ON_RIGHT_HAND;
        } else {
            return;
        }
        owner.ringStats(true);
    }

    void vanish() {
        placeAt(-1, -1, TOY);
        if (quantity > 1) {
            --quantity;
        } else if (owner != null) {
            if (owner instanceof Man) {
                ((Man) owner).pack.remove(this);
            }
            if (0 != (inUseFlags & Id.BEING_WIELDED)) {
                owner.unwield();
            } else if (0 != (inUseFlags & Id.BEING_WORN)) {
                owner.unwear();
            } else if (0 != (inUseFlags & Id.ON_EITHER_HAND)) {
                unPutOn();
            }
            owner = null;
        }
    }

    void eatenby() {
        if (!(owner instanceof Man)) {
            return;
        }
        Man owner = (Man) this.owner;
        int moves = 0;
        if ((kind == Id.FRUIT) || level.rogue.rand.percent(60)) {
            moves = level.rogue.rand.get(950, 1150);
            if (kind == Id.RATION) {
                owner.tell("yum, that tasted good");
            } else {
                owner.tell("my, that was a yummy " + owner.option.fruit);
            }
        } else {
            moves = level.rogue.rand.get(750, 950);
            owner.tell("yuk, that food tasted awful");
            owner.add_exp(2, true);
        }
        owner.movesLeft /= 3;
        owner.movesLeft += moves;
        owner.hungerStr = "      ";
        owner.printStat();
        vanish();
    }

    void thrownby(int dir) {
        if (0 != (inUseFlags & Id.BEING_WIELDED) && quantity <= 1) {
            owner.unwield();
        } else if (0 != (inUseFlags & Id.BEING_WORN)) {
            level.moveAquatars(owner);
            owner.unwear();
            owner.printStat();
        } else if (0 != (inUseFlags & Id.ON_EITHER_HAND)) {
            unPutOn();
        }
        row = owner.row;
        col = owner.col;

        getThrownAtMonster(dir);
        Monster monster = (Monster) level.levelMonsters.itemAt(row, col);
        /* Only if the point is visible? */
        owner.self.mark(owner.row, owner.col);
        owner.self.refresh();

        owner.self.mark(row, col);
        if (monster != null) {
            monster.wakeUp();
            monster.checkGoldSeeker();

            if (!monster.throw_at_monster(this, owner)) {
                flopWeapon();
            }
        } else {
            flopWeapon();
        }
        vanish();
    }

    void getThrownAtMonster(int dir) {
        int orow = row, ocol = col;
        int ch = Id.getMaskCharacter(kind);

        for (int i = 0; i < 24; i++) {
            Rowcol pt = level.getDirRowCol(dir, row, col, false);
            row = pt.row;
            col = pt.col;
            if (col <= 0 || col >= level.numCol - 1 || 0 == (level.map[row][col] & SOMETHING) || (0 != (level.map[row][col] & (HORWALL | VERTWALL | HIDDEN)) && 0 == (level.map[row][col] & TRAP))) {
                break;
            }
            if (i != 0) {
                level.rogue.mark(orow, ocol);
            }

            if (0 == (level.map[row][col] & MONSTER)) {
                level.rogue.vflash(row, col, (char) ch);
            } else {
                level.rogue.refresh();
                level.rogue.mdSleep(50);
            }
            orow = row;
            ocol = col;
            if (0 != (level.map[row][col] & MONSTER)) {
                if (!level.imitating(row, col)) {
                    break;
                }
            }
            if (0 != (level.map[row][col] & TUNNEL)) {
                i += 2;
            }
        }
        placeAt(orow, ocol, TOY);
    }

    void flopWeapon() {
        int r = row, c = col;
        Rowcol pt = new Rowcol(r, c);
        int i;

        // Find a point near the destination where something can be dropped
        int perm[] = level.rogue.rand.permute(9);
        for (i = 0; i < 9; i++) {
            pt.col = Id.X_TABLE[perm[i]] + c;
            pt.row = Id.Y_TABLE[perm[i]] + r;
            if (pt.row <= level.numRow - 2 && pt.row > MIN_ROW && pt.col < level.numCol && pt.col >= 0 && 0 != level.map[pt.row][pt.col] && 0 == (level.map[pt.row][pt.col] & (~DROPHERE))) {
                r = pt.row;
                c = pt.col;
                Toy newMissile = new Toy(this);
                newMissile.placeAt(r, c, TOY);

                return;
            }
        }
        int t = quantity;
        quantity = 1;
        owner.tell("the " + name() + "vanishes as it hits the ground");
        quantity = t;
    }

    int getWeaponWDamage() { // Check for null (-1)
        if (0 == (kind & Id.WEAPON)) {
            return -1;
        }
        int dd[] = Id.parseDamage(damage);
        dd[0] += hitEnchant;
        dd[1] += dEnchant;
        String dnew = "" + dd[0] + 'd' + dd[1];
        
        return Id.getDamage(dnew, level.rogue.rand);
    }

    int toHit() {// Check for null (1)
        return Id.parseDamage(damage)[0] + hitEnchant;
    }

    public String toString() {
        return super.toString() + Integer.toString(kind, 16);
    }
}
