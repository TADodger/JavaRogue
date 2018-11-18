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

    String mName;
    int mFlags;
    String mDamage;

    char ichar;
    int expPoints;
    int firstLevel;
    int lastLevel;
    int mHitChance;
    int stationaryDamage;
    int dropPercent;
    int hpCurrent;

    Montype(String mName, int mFlags, String mDamage, int hpCurrent, char ichar, int expPoints, int firstLevel, int lastLevel, int mHitChance, int stationaryDamage, int dropPercent) {
        this.mName = mName;
        this.mFlags = mFlags;
        this.mDamage = mDamage;
        this.hpCurrent = hpCurrent;
        this.ichar = ichar;
        this.expPoints = expPoints;
        this.firstLevel = firstLevel;
        this.lastLevel = lastLevel;
        this.mHitChance = mHitChance;
        this.stationaryDamage = stationaryDamage;
        this.dropPercent = dropPercent;
    }
}
