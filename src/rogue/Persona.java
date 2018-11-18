package rogue;

import java.io.Serializable;

class Persona extends Item implements Serializable {
    private static final long serialVersionUID = -968406821890590933L;

    Rogue self;
    Montype mt;

    // These all count moves until the condition stops
    int ringExp = 0;
    boolean wizard = false;

    Toy armor;
    Toy weapon;
    Toy leftRing;
    Toy rightRing;

    int blind = 0;
    int confused = 0;
    int halluc = 0;
    int levitate = 0;
    int bearTrap = 0;
    int hasteSelf = 0;
    int hpCurrent;
    int hpMax;
    int extra_hp = 0;
    String hitMessage = "";

    Persona ihate;
    int stealthy = 0;
    boolean conMon = false; // confuse monsters
    int rRings = 0;
    int eRings = 0;
    int strCurrent; // Current strength
    int strMax; // Max strength
    int addStrength = 0;
    int gold = 0;
    int exp; // Experience level
    boolean beingHeld = false;

    int mFlags; /* monster flags */

    Persona(Rogue self) {
        super();
        this.self = self;
    }

    Persona(Level level) {
        super(level, 0, 0);
        if (level != null) {
            this.self = level.self;
        }
    }

    void tell(String s, boolean b) {
        /* Describe my internal state (only if this is me) */
        // if(this instanceof Man)
        // ((Man)this).view.msg.message(s, b);
        self.tell(this, s, b);
    }

    void tell(String s) {
        tell(s, false);
    }

    boolean describe(String s, boolean b) {
        /* Describe a visible event about this guy */
        return self.describe(this, s, b);
    }

    String name() {
        return mt.mName;
    }

    boolean regMove() {
        if (hasteSelf > 0 && 0 == (--hasteSelf)) {
            tell(who("feel") + " yourself slowing down");
        }

        if (confused > 0 && 0 == (--confused)) {
            unconfuse();
        }

        if (halluc > 0 && 0 == (--halluc)) {
            unhallucinate();
        }

        if (bearTrap > 0) {
            bearTrap--;
        }

        if (levitate > 0 && 0 >= (--levitate)) {
            describe(who("float") + "gently to the ground", true);
        }
        if (blind > 0 && 0 == (--blind)) {
            unblind();
        }
        
        return false; /* not fainted */
    }

    void healPotional(boolean extra) {
        if (confused > 0 && extra) {
            unconfuse();
        } else if (confused > 0) {
            confused = (confused / 2) + 1;
        }

        if (halluc > 0 && extra) {
            unhallucinate();
        } else if (halluc > 0) {
            halluc = (halluc / 2) + 1;
        }

        if (blind > 0) {
            unblind();
        }
    }

    void goBlind() {
        if (blind == 0) {
            tell("a cloak of darkness falls around " + who());
        }
        blind += self.rand.get(500, 800);
        if (this instanceof Man) {
            ((Man) this).view.markall();
        }
    }

    void unblind() {
        blind = 0;
        tell("the veil of darkness lifts", true);
        if (this instanceof Man) {
            ((Man) this).view.markall(); // relight
        }
    }

    int movConfused() {
        String s = "jklhyubn";
        
        return s.charAt(self.rand.get(7));
    }

    void cnfs(int amt) {
        confused += amt;
    }

    String who() {
        return "@<" + name() + ">";
    }

    String who(String verb, String itverb) {
        return "@>" + name() + "+" + verb + "+" + itverb + "< ";
    }

    String who(String verb) {
        return who(verb, verb + 's');
    }

    void unconfuse() {
        if (confused > 0) {
            tell(who("feel") + " less " + (halluc > 0 ? "trippy" : "confused") + " now");
        }
        confused = 0;
        mFlags &= ~Monster.CONFUSED;
    }

    void unhallucinate() {
        halluc = 0;
        if (this instanceof Man) {
            ((Man) this).view.markall();
            tell("everything looks SO boring now", true);
        }
    }

    void takeANap() {
        mFlags |= Monster.ASLEEP;
    }

    Trap trapPlayer() {
        /* Traps a monster (man traps overrides this) */
        Trap t = (Trap) level.levelTraps.itemAt(row, col);
        if (t != null) {
            switch (t.kind) {
                case Trap.BEAR_TRAP:
                    if (describe(t.trapMessage(this), true)) {
                        level.map[row][col] &= ~HIDDEN;
                    }
                    bearTrap = self.rand.get(4, 7);
                    t = null;
                    break;
                case Trap.TRAP_DOOR:
                    die(); /* Just kill it! */
                    if (describe("the " + name() + " disappears!", false)) {
                        level.map[row][col] &= ~HIDDEN;
                    }
                    break;
                case Trap.TELE_TRAP:
                    if (describe("the " + name() + " disappears!", false)) {
                        level.map[row][col] &= ~HIDDEN;
                    }
                    tele();
                    break;
                case Trap.SLEEPING_GAS_TRAP:
                    if (describe(t.trapMessage(this), true)) {
                        level.map[row][col] &= ~HIDDEN;
                    }
                    takeANap();
                    break;
                case Trap.DART_TRAP:
                    String s = t.trapMessage(this);
                    if (damage(null, Id.getDamage("1d6", self.rand), 0)) {
                        s += ", and killed it.";
                    }
                    if (describe(s, true)) {
                        level.map[row][col] &= ~HIDDEN;
                    }
                    break;
            }
        }
        
        return t;
    }

    boolean mConfuse(Persona monster) {
        if (!canSee(monster.row, monster.col)) {
            return false;
        }
        if (self.rand.percent(45)) {
            monster.mFlags &= ~Monster.CONFUSES; /*
                                                   * will not confuse the rogue
                                                   */
            return false;
        }
        if (self.rand.percent(55)) {
            monster.mFlags &= ~Monster.CONFUSES;
            tell("the gaze of the " + monster.name() + " has confused " + who());
            cnfs(self.rand.get(12, 22));
            
            return true;
        }
        
        return false;
    }

    void doWear(Toy obj) {
        armor = obj;
        obj.inUseFlags |= Id.BEING_WORN;
        obj.identified = true;
    }

    void unwear() {
        if (armor != null) {
            armor.inUseFlags &= ~Id.BEING_WORN;
        }
        armor = null;
    }

    void doWield(Toy obj) {
        weapon = obj;
        obj.inUseFlags |= Id.BEING_WIELDED;
    }

    void unwield() {
        if (weapon != null) {
            weapon.inUseFlags &= ~Id.BEING_WIELDED;
        }
        weapon = null;
    }

    void printStat() {
        /* override for real man */
    }

    void ringStats(boolean huh) {
        /* monsters immune to rings */
    }

    int getHitChance(Toy t) {
        int hitChance = 40;
        hitChance += 3 * (t == null ? 1 : t.toHit());
        hitChance += 2 * (exp + ringExp) - rRings;
        
        return hitChance;
    }

    int getWeaponDamage(Toy t) {
        int damage = t == null ? -1 : t.getWeaponWDamage();

        damage += damageForStrength();
        damage += (exp + ringExp - rRings + 1) / 2;
        
        return damage;
    }

    int damageForStrength() {
        int strength = strCurrent + addStrength;

        if (strength <= 6) {
            return strength - 5;
        }
        if (strength <= 14) {
            return 1;
        }
        if (strength <= 17) {
            return 3;
        }
        if (strength <= 18) {
            return 4;
        }
        if (strength <= 20) {
            return 5;
        }
        if (strength <= 21) {
            return 6;
        }
        if (strength <= 30) {
            return 7;
        }

        return 8;
    }

    boolean canSee(int r, int c) {
        return true;
    }

    String getEnchColor() {
        if (halluc > 0) {
            return Id.idPotions[self.rand.get(Id.idPotions.length - 1)].title;
        }
        if (conMon) {
            return "red ";
        }
        
        return "blue ";
    }

    void tele() {
        /* overridden for men and monsters */
    }

    boolean damage(Persona hurter, int dmg, int other) {
        /* Force an error (must be overridden) */
        throw new RuntimeException("This method must be overridden. Cannot be called from Persona.");
    }

    void die() {
        if (ihate != null) {
            if (0 != (mFlags & Monster.HOLDS)) {
                ihate.beingHeld = false;
            }
            if (ihate.ihate == this) {
                ihate.ihate = null;
            }
        }
        level.map[row][col] &= ~(MONSTER | MAN);
        level.levelMonsters.remove(this);
        self.mark(row, col);
    }

    void gloat(Persona victim) {
        String s = "Oof!";
        switch (self.rand.get(4)) {
            case 0:
                s = "\"You don't look so good, " + victim.name() + "\", " + who("say") + '.';
                break;
            case 1:
                s = who("say") + "\"Your future is all in the past, " + victim.name() + ".\"";
                break;
            case 2:
                s = "\"I foresee your swift demise, " + victim.name() + "!\" , " + who("opine") + '.';
                break;
            case 3:
                s = who("command") + "\"Go now to your eternal rest, " + victim.name() + ".\"";
                break;
            case 4:
                s = "\"Death, where is thy sting? Right here, " + victim.name() + "!\", " + who("exclaim") + '.';
                break;
        }
        victim.tell(s);
    }
}
