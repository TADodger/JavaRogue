package rogue;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @param <T> Any Item or sub type
 */
public class ItemList<T extends Item> extends ArrayList<T> implements Serializable {
    private static final long serialVersionUID = 529121319079215441L;

    /**
     * 
     */
    public ItemList() {
        super();
    }

    /**
     * @param n
     */
    public ItemList(int n) {
        super(n);
    }

    /**
     * Move all items the the specified Level
     * 
     * @param level
     */
    public void relevel(Level level) {
        for (Item item : this) {
            item.level = level;
        }
    }

    /**
     * @param row
     * @param col
     * @return The Item at the specified location
     */
    @SuppressWarnings("unchecked")
    public T itemAt(int row, int col) {
        int i = size();
        while (--i >= 0) {
            Item p = get(i);
            if (p.row == row && p.col == col) {
                return (T) p;
            }
        }
        
        return null;
    }

    /**
     * @param ch
     * @return The item that matches the letter slot in the Rogue's pack
     */
    @SuppressWarnings("unchecked")
    public T getLetterToy(int ch) { // Call on the rogue's pack
        int i = size();
        while (--i >= 0) {
            Item item = (Item) get(i);
            if (item.itemCharacter == ch) {
                return (T) item;
            }
        }
        
        return null;
    }

    /**
     * @param mask
     * @param msg
     * @param ask
     * @return ????
     * TODO figure out return 
     */
    public int inventory(int mask, Message msg, boolean ask) {
        int i = size();
        String[] descs = {"--"};

        if (i == 0) {
            msg.message("your pack is empty");
            return '\033';
        }
        int n = 0;
        for (T item : this) {
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
                    int k = obj.itemCharacter >= 'a' && obj.itemCharacter <= 'z' ? obj.itemCharacter : n;
                    descs[n++] = singleInv(k);
                }
            }
        }
        if (n == 0) {
            descs = new String[1];
            descs[0] = "--nothing appropriate--";
        }

        return msg.rightlist(descs, ask);
    }

    /**
     * @param ch
     * @return Item character and description
     */
    public String singleInv(int ch) {
        if (ch < 'a') {
            ch += 'a';
        }
        Toy obj = null;
        for (T item : this) {
            obj = (Toy) item;
            if (obj.itemCharacter == ch) {
                break;
            }
        }
        if (obj == null) {
            return "";
        }
        String sep = ") ";
        if (0 != (obj.kind & Id.ARMOR) && obj.isProtected) {
            sep = "} ";
        }
        
        return " " + (char) obj.itemCharacter + sep + obj.getDesc();
    }

    /**
     * @param mask
     * @return true if this list contains an item with the matching type
     */
    public boolean maskPack(int mask) {
        int i = size();
        while (--i >= 0) {
            Toy t = (Toy) get(i);
            if (0 != (t.kind & mask)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return The next available letter slot in the Rogue's pack
     */
    public char nextAvailItemChar() {
        int i;
        boolean ichars[] = new boolean[26];

        for (i = 0; i < 26; i++)
            ichars[i] = false;
        i = size();
        while (--i >= 0) {
            Toy obj = (Toy) get(i);
            int k = obj.itemCharacter - 'a';
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

    /**
     * Remove curse from all items in this list.
     */
    public void uncurseAll() {
        for (Item item : this) { 
            ((Toy) item).isCursed = false;
        }
    }
}
