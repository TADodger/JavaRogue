package rogue;

import java.io.Serializable;

/**
 *
 */
public class Trap extends Item implements Serializable {
    private static final long serialVersionUID = -5822054677475787076L;

    /** */
    public int kind;
    /** */
    public static final int TRAP_DOOR = 0;
    /** */
    public static final int BEAR_TRAP = 1;
    /** */
    public static final int TELE_TRAP = 2;
    /** */
    public static final int DART_TRAP = 3;
    /** */
    public static final int SLEEPING_GAS_TRAP = 4;
    /** */
    public static final int RUST_TRAP = 5;
    /** */
    public static final int TRAPS = 6;

    private static String msg[] = { "$ fell down a trap", "$are caught in a bear trap", "teleport", "a small dart just hit $ in the shoulder", "a strange white mist envelops $ and $fall asleep",
            "a gush of water hits $ on the head" };
    /**
     * 
     */
    public static String name[] = { "trap door", "bear trap", "teleport trap", "poison dart trap", "sleeping gas trap", "rust trap", };

    /**
     * @param level
     * @param r
     * @param c
     * @param kind
     */
    public Trap(Level level, int r, int c, int kind) {
        super(level, r, c);
        this.kind = kind;
        placeAt(r, c, TRAP);
    }

    /**
     * @param p
     * @return The message for this trap
     */
    public String trapMessage(Persona p) {
        String src = msg[kind];
        String dst = "";
        int i = 0;
        int j;
        try {
            while ((j = src.indexOf('$', i)) >= 0) {
                dst += src.substring(i, j);
                i = j + 1;
                if (src.charAt(i) != ' ') {
                    dst += "@>" + p.name() + '+';
                    if (src.charAt(i) == 'a') {
                        dst += "are+is<";
                        i += 3;
                    } else {
                        j = src.indexOf(' ', j);
                        dst += src.substring(i, j) + '+' + src.substring(i, j) + '<';
                        i = j;
                    }
                } else
                    dst += "@<" + p.name() + ">";
            }
        } catch (Exception e) {
            System.out.println("trapMessage(" + p.name() + ")\n\t" + src + '\n' + dst);
        }
        dst += src.substring(i);
        return dst;
    }
}
