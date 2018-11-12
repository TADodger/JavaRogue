package rogue;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 */
public class ItemList extends ArrayList<Item> implements Serializable {
    private static final long serialVersionUID = 529121319079215441L;

    ItemList() {
        super();
    }

    ItemList(int n) {
        super(n);
    }

    void relevel(Level level) {
        for (Item item : this) {
            item.level = level;
        }
    }

    Item item_at(int row, int col) {
        int i = size();
        while (--i >= 0) {
            Item p = get(i);
            if (p.row == row && p.col == col) {
                return p;
            }
        }
        
        return null;
    }

    Item get_letter_toy(int ch) { // Call on the rogue's pack
        int i = size();
        while (--i >= 0) {
            Item p = get(i);
            if (p.ichar == ch) {
                return p;
            }
        }
        
        return null;
    }

    int inventory(int mask, Message msg, boolean ask) {
        int i = size();
        String[] descs = {"--"};

        if (i == 0) {
            msg.message("your pack is empty");
            return '\033';
        }
        int n = 0;
        for (Item item : this) {
            Toy obj = (Toy) item;
            if (0 != (obj.kind & mask)) {
                ++n;
            }
        }
        if (n > 0) {
            descs = new String[n];
            n = 0;
            for (Item item : this) {
                Toy obj = (Toy) item;
                if (0 != (obj.kind & mask)) {
                    int k = obj.ichar >= 'a' && obj.ichar <= 'z' ? obj.ichar : n;
                    descs[n++] = single_inv(k);
                }
            }
        }
        if (n == 0) {
            descs = new String[1];
            descs[0] = "--nothing appropriate--";
        }
        return msg.rightlist(descs, ask);
    }

    String single_inv(int ch) {
        if (ch < 'a')
            ch += 'a';
        Toy obj = null;
        for (Item item : this) {
            obj = (Toy) item;
            if (obj.ichar == ch) {
                break;
            }
        }
        if (obj == null) {
            return "";
        }
        String sep = ") ";
        if (0 != (obj.kind & Id.ARMOR) && obj.is_protected) {
            sep = "} ";
        }
        
        return " " + (char) obj.ichar + sep + obj.get_desc();
    }

    boolean mask_pack(int mask) {
        int i = size();
        while (--i >= 0) {
            Toy t = (Toy) get(i);
            if (0 != (t.kind & mask)) {
                return true;
            }
        }
        return false;
    }

    char next_avail_ichar() {
        int i;
        boolean ichars[] = new boolean[26];

        for (i = 0; i < 26; i++)
            ichars[i] = false;
        i = size();
        while (--i >= 0) {
            Toy obj = (Toy) get(i);
            int k = obj.ichar - 'a';
            if (k >= 0 && k < 26) {
                ichars[k] = true;
            }
        }
        for (i = 0; i < 26; i++) {
            if (!ichars[i]) {
                return (char) (i + 'a');
            }
        }
        
        return '?';
    }

    void uncurse_all() {
        for (Item item : this) { 
            ((Toy) item).is_cursed = false;
        }
    }
}
