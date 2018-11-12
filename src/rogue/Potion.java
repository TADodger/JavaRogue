package rogue;

import java.io.Serializable;

class Potion extends Toy implements Serializable {
    private static final long serialVersionUID = 2698734724537852689L;

    static final String strange_feeling = "strange feeling";

    Potion(Level level) {
        super(level, Id.POTION);
    }

    void quaffby() {
        switch (kind) {
            case Id.INCREASE_STRENGTH:
                owner.tell("you feel stronger now, what bulging muscles!");
                owner.str_current++;
                if (owner.str_current > owner.str_max) {
                    owner.str_max = owner.str_current;
                }
                break;
            case Id.RESTORE_STRENGTH:
                owner.str_current = owner.str_max;
                owner.tell("this tastes great, you feel warm all over");
                break;
            case Id.HEALING:
                owner.tell("you begin to feel better");
                potion_heal(false);
                break;
            case Id.EXTRA_HEALING:
                owner.tell("you begin to feel much better");
                potion_heal(true);
                break;
            case Id.POISON:
                if ((owner instanceof Man) && ((Man) owner).sustain_strength) {
                    break;
                }
                owner.str_current -= level.self.rand.get(1, 3);
                if (owner.str_current < 1) {
                    owner.str_current = 1;
                }
                owner.tell("you feel very sick now");
                if (owner.halluc > 0) {
                    owner.unhallucinate();
                }
                break;
            case Id.RAISE_LEVEL:
                if (owner instanceof Man) {
                    ((Man) owner).exp_points = Man.level_points[((Man) owner).exp - 1];
                    owner.tell("you suddenly feel much more skillful");
                    ((Man) owner).add_exp(1, true);
                }
                break;
            case Id.BLINDNESS:
                owner.go_blind();
                break;
            case Id.HALLUCINATION:
                owner.tell("oh wow, everything seems so cosmic");
                owner.halluc += level.self.rand.get(500, 800);
                break;
            case Id.DETECT_MONSTER:
                if ((owner instanceof Man) && !level.show_monsters((Man) owner)) {
                    owner.tell(strange_feeling);
                }
                break;
            case Id.DETECT_TOYS:
                if (level.level_toys.size() > 0 && (owner instanceof Man)) {
                    if (owner.blind != 0)
                        level.show_toys((Man) owner);
                } else
                    owner.tell(strange_feeling);
                break;
            case Id.CONFUSION:
                owner.tell(owner.halluc > 0 ? "what a trippy feeling" : "you feel confused");
                owner.cnfs(level.self.rand.get(12, 22));
                break;
            case Id.LEVITATION:
                owner.describe(owner.who("start") + "to float in the air", false);
                owner.levitate += level.self.rand.get(15, 30);
                owner.being_held = false;
                owner.bear_trap = 0;
                break;
            case Id.HASTE_SELF:
                owner.tell("you feel yourself moving much faster");
                owner.haste_self += level.self.rand.get(11, 21);
                owner.haste_self += 1 - (owner.haste_self & 1);
                break;
            case Id.SEE_INVISIBLE:
                if (owner instanceof Man) {
                    owner.tell("hmm, this potion tastes like " + ((Man) owner).option.fruit + " juice");
                    if (owner.blind > 0) {
                        owner.unblind();
                    }
                    ((Man) owner).see_invisible = true;
                    ((Man) owner).view.markall(); // relight
                }
                break;
        }
        owner.print_stat();
        Id.identifyUncalled(kind);
        vanish();
    }

    void potion_heal(boolean extra) {
        double ratio;
        int add;

        owner.hp_current += owner.exp;

        ratio = ((double) owner.hp_current) / owner.hp_max;

        if (ratio >= 1.00) {
            owner.hp_max += (extra ? 2 : 1);
            owner.extra_hp += (extra ? 2 : 1);
            owner.hp_current = owner.hp_max;
        } else if (ratio >= 0.90) {
            owner.hp_max += (extra ? 1 : 0);
            owner.extra_hp += (extra ? 1 : 0);
            owner.hp_current = owner.hp_max;
        } else {
            if (ratio < 0.33) {
                ratio = 0.33;
            }
            if (extra) {
                ratio += ratio;
            }
            add = (int) (ratio * ((double) owner.hp_max - owner.hp_current));
            owner.hp_current += add;
            if (owner.hp_current > owner.hp_max) {
                owner.hp_current = owner.hp_max;
            }
        }
        owner.heal_potional(extra);
    }
}
