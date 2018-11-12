package rogue;

import java.io.Serializable;

//Hate error happens after zap hits emu

//Check drain life hitting monsters (in level.java)
//Does permute screw up on 0 or 1 elt?

/**
 *
 */
public class Montype implements Serializable {
    private static final long serialVersionUID = 3776256900815795238L;

    String m_name;
    int m_flags;
    String m_damage;

    char ichar;
    int exp_points;
    int first_level;
    int last_level;
    int m_hit_chance;
    int stationary_damage;
    int drop_percent;
    int hp_current;

    Montype(String m_name, int m_flags, String m_damage, int hp_current, char ichar, int exp_points, int first_level, int last_level, int m_hit_chance, int stationary_damage, int drop_percent) {
        this.m_name = m_name;
        this.m_flags = m_flags;
        this.m_damage = m_damage;
        this.hp_current = hp_current;
        this.ichar = ichar;
        this.exp_points = exp_points;
        this.first_level = first_level;
        this.last_level = last_level;
        this.m_hit_chance = m_hit_chance;
        this.stationary_damage = stationary_damage;
        this.drop_percent = drop_percent;
    }
}
