package rogue;

import java.io.Serializable;

/**
 *
 */
public class Potion extends Toy implements Serializable {
    private static final long serialVersionUID = 2698734724537852689L;

    private static final String STRANGE_FEELING = "strange feeling";

    /**
     * @param level
     */
    public Potion(Level level) {
        super(level, Id.POTION);
    }

    /**
     * 
     */
    public void quaffby() {
        switch (kind) {
            case Id.INCREASE_STRENGTH:
                owner.tell("you feel stronger now, what bulging muscles!");
                owner.strCurrent++;
                if (owner.strCurrent > owner.strMax) {
                    owner.strMax = owner.strCurrent;
                }
                break;
            case Id.RESTORE_STRENGTH:
                owner.strCurrent = owner.strMax;
                owner.tell("this tastes great, you feel warm all over");
                break;
            case Id.HEALING:
                owner.tell("you begin to feel better");
                potionHeal(false);
                break;
            case Id.EXTRA_HEALING:
                owner.tell("you begin to feel much better");
                potionHeal(true);
                break;
            case Id.POISON:
                if ((owner instanceof Man) && ((Man) owner).sustainStrength) {
                    break;
                }
                owner.strCurrent -= level.rogue.rand.get(1, 3);
                if (owner.strCurrent < 1) {
                    owner.strCurrent = 1;
                }
                owner.tell("you feel very sick now");
                if (owner.halluc > 0) {
                    owner.unhallucinate();
                }
                break;
            case Id.RAISE_LEVEL:
                if (owner instanceof Man) {
                    ((Man) owner).expPoints = Man.LEVEL_POINTS[((Man) owner).exp - 1];
                    owner.tell("you suddenly feel much more skillful");
                    ((Man) owner).add_exp(1, true);
                }
                break;
            case Id.BLINDNESS:
                owner.goBlind();
                break;
            case Id.HALLUCINATION:
                owner.tell("oh wow, everything seems so cosmic");
                owner.halluc += level.rogue.rand.get(500, 800);
                break;
            case Id.DETECT_MONSTER:
                if ((owner instanceof Man) && !level.showMonsters((Man) owner)) {
                    owner.tell(STRANGE_FEELING);
                }
                break;
            case Id.DETECT_TOYS:
                if (level.levelToys.size() > 0 && (owner instanceof Man)) {
                    if (owner.blind != 0)
                        level.showToys((Man) owner);
                } else
                    owner.tell(STRANGE_FEELING);
                break;
            case Id.CONFUSION:
                owner.tell(owner.halluc > 0 ? "what a trippy feeling" : "you feel confused");
                owner.cnfs(level.rogue.rand.get(12, 22));
                break;
            case Id.LEVITATION:
                owner.describe(owner.who("start") + "to float in the air", false);
                owner.levitate += level.rogue.rand.get(15, 30);
                owner.beingHeld = false;
                owner.bearTrap = 0;
                break;
            case Id.HASTE_SELF:
                owner.tell("you feel yourself moving much faster");
                owner.hasteSelf += level.rogue.rand.get(11, 21);
                owner.hasteSelf += 1 - (owner.hasteSelf & 1);
                break;
            case Id.SEE_INVISIBLE:
                if (owner instanceof Man) {
                    owner.tell("hmm, this potion tastes like " + ((Man) owner).option.fruit + " juice");
                    if (owner.blind > 0) {
                        owner.unblind();
                    }
                    ((Man) owner).seeInvisible = true;
                    ((Man) owner).view.markall(); // relight
                }
                break;
        }
        owner.printStat();
        Id.identifyUncalled(kind);
        vanish();
    }

    private void potionHeal(boolean extra) {
        double ratio;
        int add;

        owner.hpCurrent += owner.exp;

        ratio = ((double) owner.hpCurrent) / owner.hpMax;

        if (ratio >= 1.00) {
            owner.hpMax += (extra ? 2 : 1);
            owner.extra_hp += (extra ? 2 : 1);
            owner.hpCurrent = owner.hpMax;
        } else if (ratio >= 0.90) {
            owner.hpMax += (extra ? 1 : 0);
            owner.extra_hp += (extra ? 1 : 0);
            owner.hpCurrent = owner.hpMax;
        } else {
            if (ratio < 0.33) {
                ratio = 0.33;
            }
            if (extra) {
                ratio += ratio;
            }
            add = (int) (ratio * ((double) owner.hpMax - owner.hpCurrent));
            owner.hpCurrent += add;
            if (owner.hpCurrent > owner.hpMax) {
                owner.hpCurrent = owner.hpMax;
            }
        }
        owner.healPotional(extra);
    }
}
