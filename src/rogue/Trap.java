package rogue;

import java.io.Serializable;

/**
 *
 */
public class Trap extends Item implements Serializable {
    private static final long serialVersionUID = -5822054677475787076L;

    int kind;
    static final int TRAP_DOOR = 0;
    static final int BEAR_TRAP = 1;
    static final int TELE_TRAP = 2;
    static final int DART_TRAP = 3;
    static final int SLEEPING_GAS_TRAP = 4;
    static final int RUST_TRAP = 5;
    static final int TRAPS = 6;

    static String msg[] = { "$ fell down a trap", "$are caught in a bear trap", "teleport", "a small dart just hit $ in the shoulder", "a strange white mist envelops $ and $fall asleep",
            "a gush of water hits $ on the head" };
    static String name[] = { "trap door", "bear trap", "teleport trap", "poison dart trap", "sleeping gas trap", "rust trap", };

    Trap(Level level, int r, int c, int kind) {
        super(level, r, c);
        this.kind = kind;
        place_at(r, c, TRAP);
    }

    String trap_message(Persona p) {
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
            System.out.println("trap_message(" + p.name() + ")\n\t" + src + '\n' + dst);
        }
        dst += src.substring(i);
        return dst;
    }
}
