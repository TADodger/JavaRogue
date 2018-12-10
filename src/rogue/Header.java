package rogue;

/**
 *
 */
public interface Header {
    /** */
    public static final int MIN_ROW = 1;
    /** */
    public static final int AMULET_LEVEL = 26;
    /** */
    public static final int LAST_MAP = 99;

    // Values for the level map:
    /** */
    public static final int TOY = 01;
    /** */
    public static final int MONSTER = 02;
    /** */
    public static final int STAIRS = 04;
    /** */
    public static final int HORWALL = 010;
    /** */
    public static final int VERTWALL = 020;
    /** */
    public static final int DOOR = 040;
    /** */
    public static final int FLOOR = 0100;
    /** */
    public static final int TUNNEL = 0200;
    /** */
    public static final int TRAP = 0400;
    /** */
    /** */
    public static final int HIDDEN = 01000;
    /** */
    public static final int MAN = 02000; // The rogue is here
    /** */
    public static final int HOLDER = 04000; // May contain trap/toy/monster
    /** */
    public static final int DARK = 010000; // Dark place

    /** */
    public static final int DROPHERE = (DOOR | FLOOR | TUNNEL | MAN | HOLDER | MONSTER);
    /** */
    public static final int SOMETHING = 0777;

    /** */
    public static final int U_NORMAL = 0x000; // Light gray
    /** */
    public static final int U_WEAK = 0x100; // Dark gray
    /** */
    public static final int U_BLACK = 0x200; // Black is invisible
    /** */
    public static final int U_BRITE = 0x300; // White
    /** */
    public static final int U_RED = 0x400;
    /** */
    /** */
    public static final int U_ROGUE = 0x500;
    /** */
    public static final int U_DARK_RED = 0x600;
    /** */
    public static final int U_GREEN = 0x700;
}
