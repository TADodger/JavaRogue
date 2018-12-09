package rogue;

import java.io.Serializable;

/**
 *
 */
public abstract class Persona extends Item implements Serializable {
    private static final long serialVersionUID = -968406821890590933L;

    /** */
    public Rogue self;
    /** */
    public Montype montype;

    // These all count moves until the condition stops
    /** */
    public int ringExp = 0;
    /** */
    public boolean wizard = false;

    /** */
    public Toy armor;
    /** */
    public Toy weapon;
    /** */
    public Toy leftRing;
    /** */
    public Toy rightRing;

    /** */
    public int blind = 0;
    /** */
    public int confused = 0;
    /** */
    public int halluc = 0;
    /** */
    public int levitate = 0;
    /** */
    public int bearTrap = 0;
    /** */
    public int hasteSelf = 0;
    /** */
    public int hpCurrent;
    /** */
    public int hpMax;
    /** */
    public int extra_hp = 0;
    /** */
    public String hitMessage = "";

    /** */
    public Persona ihate;
    /** */
    public int stealthy = 0;
    /** */
    public boolean conMon = false; // confuse monsters
    /** */
    public int rRings = 0;
    /** */
    public int eRings = 0;
    /** */
    public int strCurrent; // Current strength
    /** */
    public int strMax; // Max strength
    /** */
    public int addStrength = 0;
    /** */
    public int gold = 0;
    /** */
    public int exp; // Experience level
    /** */
    public boolean beingHeld = false;

    /** */
    public int mFlags; /* monster flags */

    /**
     * @param self
     */
    public Persona(Rogue self) {
        super();
        this.self = self;
    }

    /**
     * @param level
     */
    public Persona(Level level) {
        super(level, 0, 0);
        if (level != null) {
            this.self = level.rogue;
        }
    }

    /**
     * @param s
     * @param b
     */
    public void tell(String s, boolean b) {
        /* Describe my internal state (only if this is me) */
        // if(this instanceof Man)
        // ((Man)this).view.msg.message(s, b);
        self.tell(this, s, b);
    }

    /**
     * @param s
     */
    public void tell(String s) {
        tell(s, false);
    }

    /**
     * @param s
     * @param b
     * @return ???
     */
    public boolean describe(String s, boolean b) {
        /* Describe a visible event about this guy */
        return self.describe(this, s, b);
    }

    /**
     * @return the type name
     */
    public String name() {
        return montype.mName;
    }

    protected boolean regMove() {
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

    /**
     * @param extra
     */
    public void healPotional(boolean extra) {
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

    /**
     *  
     */
    public void goBlind() {
        if (blind == 0) {
            tell("a cloak of darkness falls around " + who());
        }
        blind += self.rand.get(500, 800);
        if (this instanceof Man) {
            ((Man) this).view.markall();
        }
    }

    /** 
     * 
     */
    public void unblind() {
        blind = 0;
        tell("the veil of darkness lifts", true);
        if (this instanceof Man) {
            ((Man) this).view.markall(); // relight
        }
    }

    /**
     * @return random direction character
     */
    public int movConfused() {
        String s = "jklhyubn";
        
        return s.charAt(self.rand.get(7));
    }

    /**
     * @param amt
     */
    public void cnfs(int amt) {
        confused += amt;
    }

    /**
     * @return the name
     */
    public String who() {
        return "@<" + name() + ">";
    }

    /**
     * @param verb
     * @param itverb
     * @return the name
     */
    public String who(String verb, String itverb) {
        return "@>" + name() + "+" + verb + "+" + itverb + "< ";
    }

    /**
     * @param verb
     * @return the name
     */
    public String who(String verb) {
        return who(verb, verb + 's');
    }

    private void unconfuse() {
        if (confused > 0) {
            tell(who("feel") + " less " + (halluc > 0 ? "trippy" : "confused") + " now");
        }
        confused = 0;
        mFlags &= ~Monster.CONFUSED;
    }

    /** 
     * 
     */
    public void unhallucinate() {
        halluc = 0;
        if (this instanceof Man) {
            ((Man) this).view.markall();
            tell("everything looks SO boring now", true);
        }
    }

    protected void takeANap() {
        mFlags |= Monster.ASLEEP;
    }

    protected Trap trapPlayer() {
        /* Traps a monster (man traps overrides this) */
        Trap trap = level.levelTraps.itemAt(row, col);
        if (trap != null) {
            switch (trap.kind) {
                case Trap.BEAR_TRAP:
                    if (describe(trap.trapMessage(this), true)) {
                        level.map[row][col] &= ~HIDDEN;
                    }
                    bearTrap = self.rand.get(4, 7);
                    trap = null;
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
                    if (describe(trap.trapMessage(this), true)) {
                        level.map[row][col] &= ~HIDDEN;
                    }
                    takeANap();
                    break;
                case Trap.DART_TRAP:
                    String s = trap.trapMessage(this);
                    if (damage(null, Id.getDamage("1d6", self.rand), 0)) {
                        s += ", and killed it.";
                    }
                    if (describe(s, true)) {
                        level.map[row][col] &= ~HIDDEN;
                    }
                    break;
            }
        }
        
        return trap;
    }

    /**
     * @param monster
     * @return true if the monster is confused
     */
    public boolean mConfuse(Persona monster) {
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

    /**
     * @param obj
     */
    public void doWear(Toy obj) {
        armor = obj;
        obj.inUseFlags |= Id.BEING_WORN;
        obj.identified = true;
    }

    /** 
     * 
     */
    public void unwear() {
        if (armor != null) {
            armor.inUseFlags &= ~Id.BEING_WORN;
        }
        armor = null;
    }

    /**
     * @param obj
     */
    public void doWield(Toy obj) {
        weapon = obj;
        obj.inUseFlags |= Id.BEING_WIELDED;
    }

    /** 
     * 
     */
    public void unwield() {
        if (weapon != null) {
            weapon.inUseFlags &= ~Id.BEING_WIELDED;
        }
        weapon = null;
    }

    protected abstract void printStat();

    protected abstract void ringStats(boolean huh);

    /**
     * @param t
     * @return chance to hit
     */
    public int getHitChance(Toy t) {
        int hitChance = 40;
        hitChance += 3 * (t == null ? 1 : t.toHit());
        hitChance += 2 * (exp + ringExp) - rRings;
        
        return hitChance;
    }

    /**
     * @param toy
     * @return the damage from the given weapon
     */
    public int getWeaponDamage(Toy toy) {
        int damage = toy == null ? -1 : toy.getWeaponWDamage();

        damage += damageForStrength();
        damage += (exp + ringExp - rRings + 1) / 2;
        
        return damage;
    }

    private int damageForStrength() {
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

    private boolean canSee(int r, int c) {
        return true;
    }

    /**
     * @return ???
     */
    public String getEnchColor() {
        if (halluc > 0) {
            return Id.idPotions[self.rand.get(Id.idPotions.length - 1)].title;
        }
        if (conMon) {
            return "red ";
        }
        
        return "blue ";
    }

    protected abstract void tele();

    protected abstract boolean damage(Persona hurter, int dmg, int other);

    /** 
     * 
     */
    public void die() {
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

    /**
     * @param victim
     */
    public void gloat(Persona victim) {
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
