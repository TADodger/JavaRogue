package rogue;

import java.io.Serializable;

/**
 * Hate error happens after zap hits emu
 * 
 * Check drain life hitting monsters (in level.java)
 * Does permute screw up on 0 or 1 elt?
 */
public class Montype implements Serializable {
    private static final long serialVersionUID = 3776256900815795238L;

    /** */
    public String mName;
    /** */
    public int mFlags;
    /** */
    public String mDamage;

    /** */
    public char ichar;
    /** */
    public int expPoints;
    /** */
    public int firstLevel;
    /** */
    public int lastLevel;
    /** */
    public int mHitChance;
    /** */
    public int stationaryDamage;
    /** */
    public int dropPercent;
    /** */
    public int hpCurrent;

    /**
     * @param mName
     * @param mFlags
     * @param mDamage
     * @param hpCurrent
     * @param ichar
     * @param expPoints
     * @param firstLevel
     * @param lastLevel
     * @param mHitChance
     * @param stationaryDamage
     * @param dropPercent
     */
    public Montype(String mName, int mFlags, String mDamage, int hpCurrent, char ichar, int expPoints, int firstLevel, int lastLevel, int mHitChance, int stationaryDamage, int dropPercent) {
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
