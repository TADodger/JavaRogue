package rogue;

import java.util.StringTokenizer;
import java.awt.Event;
import java.io.Serializable;

/**
 *
 */
public class Id implements Serializable {
    private static final long serialVersionUID = -1091380658537367767L;

    @SuppressWarnings("unused")
    private int value; //possibly not used
    private String real;
    
    /** Name of object */
    public String title;
    /** whether it's been identified or not */
    public int idStatus;

    /** X offset used when creating objects */
    public static final int X_TABLE[] = { 1, 1, 0, -1, -1, -1, 0, 1, 0 };
    /** Y offset used when creating objects */
    public static final int Y_TABLE[] = { 0, 1, 1, 1, 0, -1, -1, -1, 0 };

    private static final String POTIONS_LIST[] = { 
            "100", "blue ", "of increase strength ", 
            "250", "red ", "of restore strength ", 
            "100", "green ", "of healing ", 
            "200", "grey ", "of extra healing ", 
            "10", "brown ", "of poison ", 
            "300", "clear ", "of raise level ", 
            "10", "pink ", "of blindness ", 
            "25", "white ", "of hallucination ", 
            "100", "purple ", "of detect monster ", 
            "100", "black ", "of detect things ", 
            "10", "yellow ", "of confusion ", 
            "80", "plaid ", "of levitation ", 
            "150", "burgundy ", "of haste self ", 
            "145", "beige ", "of see invisible " 
            };
    /** Identity of potions */
    public static Id idPotions[] = getIdList(POTIONS_LIST, 0);

    /**
     * Mix the potion colors around
     * 
     * @param rand
     */
    public static void mixColors(Randomx rand) {
        for (int i = idPotions.length; --i > 1;) {
            int otherIndex = rand.get(i - 1);
            String title = idPotions[i].title;
            idPotions[i].title = idPotions[otherIndex].title;
            idPotions[otherIndex].title = title;
        }
    }

    private static final String SCROLLS_LIST[] = { 
            "505", "", "of protect armor ", 
            "200", "", "of hold monster ", 
            "235", "", "of enchant weapon ", 
            "235", "", "of enchant armor ", 
            "175", "", "of identify ",
            "190", "", "of teleportation ", 
            "25", "", "of sleep ", 
            "610", "", "of scare monster ", 
            "210", "", "of remove curse ", 
            "80", "", "of create monster ", 
            "25", "", "of aggravate monster ",
            "180", "", "of magic mapping ", 
            "90", "", "of confuse monster " 
            };

    private static final String SYLLABLES[] = { 
            "blech ", "foo ", "barf ", "rech ", "bar ", "blech ", "quo ", "bloto ", 
            "oh ", "caca ", "blorp ", "erp ", "festr ", "rot ", "slie ", "snorf ", 
            "iky ", "yuky ", "ooze ", "ah ", "bahl ", "zep ", "druhl ", "flem ", 
            "behil ", "arek ", "mep ", "zihr ", "grit ", "kona ", "kini ", "ichi ", 
            "tims ", "ogr ", "oo ", "ighr ", "coph ", "swerr ", "mihln ", "poxi "
            };
    /** Identity of scrolls */
    public static Id idScrolls[] = getIdList(SCROLLS_LIST, 0);

    private static final String WEAPONS_LIST[] = { 
            "150", "short bow ", "", 
            "8", "darts ", "", 
            "15", "arrows ", "", 
            "27", "daggers ", "", 
            "35", "shurikens ", "", 
            "360", "mace ", "", 
            "470", "long sword ", "",
            "580", "two-handed sword ", "" 
            };
    /** Identity of weapons */
    public static Id idWeapons[] = getIdList(WEAPONS_LIST, 0);

    private static final String ARMORS_LIST[] = { 
            "300", "leather armor ", "", 
            "300", "ring mail ", "", 
            "400", "scale mail ", "", 
            "500", "chain mail ", "", 
            "600", "banded mail ", "", 
            "600", "splint mail ", "",
            "700", "plate mail ", "" 
            };
    /** Identity of armor */
    public static Id idArmors[] = getIdList(ARMORS_LIST, 0);

    private static final String WANDS_LIST[] = { 
            "25", "", "of teleport away ", 
            "50", "", "of slow monster ", 
            "8", "", "of invisibility ", 
            "55", "", "of polymorph ", 
            "2", "", "of haste monster ", 
            "20", "", "of magic missile ", 
            "20", "", "of cancellation ", 
            "0", "", "of do nothing ", 
            "35", "", "of drain life ", 
            "20", "", "of cold ", 
            "20", "", "of fire " 
            };
    /** Identity of wands */
    public static Id idWands[] = getIdList(WANDS_LIST, 0);

    private static final String RINGS_LIST[] = { 
            "250", "", "of stealth ", 
            "100", "", "of teleportation ", 
            "255", "", "of regeneration ", 
            "295", "", "of slow digestion ", 
            "200", "", "of add strength ", 
            "250", "", "of sustain strength ", 
            "250", "", "of dexterity ", 
            "25", "", "of adornment ", 
            "300", "", "of see invisible ", 
            "290", "", "of maintain armor ", 
            "270", "", "of searching " 
            };
    /** Identity of rings */
    public static Id idRings[] = getIdList(RINGS_LIST, 0);

    private static final String WAND_MATERIALS[] = { 
            "steel ", "bronze ", "gold ", "silver ", "copper ", 
            "nickel ", "cobalt ", "tin ", "iron ", "magnesium ", 
            "chrome ", "carbon ", "platinum ", "silicon ", "titanium ",

            "teak ", "oak ", "cherry ", "birch ", "pine ", 
            "cedar ", "redwood ", "balsa ", "ivory ", "walnut ", 
            "maple ", "mahogany ", "elm ", "palm ", "wooden " 
            };
    /** Whether the wand at each index is made of wood or not */
    public static final boolean IS_WOOD[] = new boolean[WAND_MATERIALS.length];
    static {
        boolean wood = false;
        for (int k = 0; k < IS_WOOD.length; k++) {
            if (WAND_MATERIALS[k].compareTo("teak") == 0) {
                wood = true;
            }
            IS_WOOD[k] = wood;
        }
    }
    private static final String GEMS[] = { 
            "diamond ", "stibotantalite ", "lapi-lazuli ", "ruby ", "emerald ", "sapphire ", "amethyst ", 
            "quartz ", "tiger-eye ", "opal ", "agate ", "turquoise ", "pearl ", "garnet " 
            };

    /**
     * Make wand titles out of random syllables
     * 
     * @param rand
     */
    public static void makeScrollTitles(Randomx rand) {
        // Also name the wands and rings
        for (int i = 0; i < idScrolls.length; i++) {
            int syllableCount = rand.get(2, 5);
            String title = "'";
            for (int j = 0; j < syllableCount; j++) {
                int syllableNumber = rand.get(1, SYLLABLES.length - 1);
                title = title.concat(SYLLABLES[syllableNumber]);
            }
            idScrolls[i].title = title.concat("' ");
        }
        int[] permutaion = rand.permute(WAND_MATERIALS.length);
        for (int i = 0; i < idWands.length; i++) {
            idWands[i].title = WAND_MATERIALS[permutaion[i]];
        }

        permutaion = rand.permute(GEMS.length);
        for (int i = 0; i < idRings.length; i++) {
            idRings[i].title = GEMS[permutaion[i]];
        }
    }

    /** Constant */ public static final int ARMOR = 0x00100;
    /** Constant */ public static final int WEAPON = 0x00200;
    /** Constant */ public static final int SCROLL = 0x00400;
    /** Constant */ public static final int POTION = 0x00800;
    /** Constant */ public static final int GOLD = 0x01000;
    /** Constant */ public static final int FOOD = 0x02000;
    /** Constant */ public static final int WAND = 0x04000;
    /** Constant */ public static final int RING = 0x08000;
    /** Constant */ public static final int AMULET = 0x10000;
    /** Constant */ public static final int ALL_TOYS = 0x1ff00;

    /** Constant */ public static final int LEATHER = 0 + ARMOR;
    /** Constant */ public static final int RINGMAIL = 1 + ARMOR;
    /** Constant */ public static final int SCALE = 2 + ARMOR;
    /** Constant */ public static final int CHAIN = 3 + ARMOR;
    /** Constant */ public static final int BANDED = 4 + ARMOR;
    /** Constant */ public static final int SPLINT = 5 + ARMOR;
    /** Constant */ public static final int PLATE = 6 + ARMOR;
    /** Constant */ public static final int ARMORS = 7 + ARMOR;

    /** Constant */ public static final int BOW = 0 + WEAPON;
    /** Constant */ public static final int DART = 1 + WEAPON;
    /** Constant */ public static final int ARROW = 2 + WEAPON;
    /** Constant */ public static final int DAGGER = 3 + WEAPON;
    /** Constant */ public static final int SHURIKEN = 4 + WEAPON;
    /** Constant */ public static final int MACE = 5 + WEAPON;
    /** Constant */ public static final int LONG_SWORD = 6 + WEAPON;
    /** Constant */ public static final int TWO_HANDED_SWORD = 7 + WEAPON;
    /** Constant */ public static final int WEAPONS = 8;

    /** Constant */ public static final int PROTECT_ARMOR = 0 + SCROLL;
    /** Constant */ public static final int HOLD_MONSTER = 1 + SCROLL;
    /** Constant */ public static final int ENCH_WEAPON = 2 + SCROLL;
    /** Constant */ public static final int ENCH_ARMOR = 3 + SCROLL;
    /** Constant */ public static final int IDENTIFY = 4 + SCROLL;
    /** Constant */ public static final int TELEPORT = 5 + SCROLL;
    /** Constant */ public static final int SLEEP = 6 + SCROLL;
    /** Constant */ public static final int SCARE_MONSTER = 7 + SCROLL;
    /** Constant */ public static final int REMOVE_CURSE = 8 + SCROLL;
    /** Constant */ public static final int CREATE_MONSTER = 9 + SCROLL;
    /** Constant */ public static final int AGGRAVATE_MONSTER = 10 + SCROLL;
    /** Constant */ public static final int MAGIC_MAPPING = 11 + SCROLL;
    /** Constant */ public static final int CON_MON = 12 + SCROLL;
    /** Constant */ public static final int SCROLS = 13 + SCROLL;

    /** Constant */ public static final int INCREASE_STRENGTH = 0 + POTION;
    /** Constant */ public static final int RESTORE_STRENGTH = 1 + POTION;
    /** Constant */ public static final int HEALING = 2 + POTION;
    /** Constant */ public static final int EXTRA_HEALING = 3 + POTION;
    /** Constant */ public static final int POISON = 4 + POTION;
    /** Constant */ public static final int RAISE_LEVEL = 5 + POTION;
    /** Constant */ public static final int BLINDNESS = 6 + POTION;
    /** Constant */ public static final int HALLUCINATION = 7 + POTION;
    /** Constant */ public static final int DETECT_MONSTER = 8 + POTION;
    /** Constant */ public static final int DETECT_TOYS = 9 + POTION;
    /** Constant */ public static final int CONFUSION = 10 + POTION;
    /** Constant */ public static final int LEVITATION = 11 + POTION;
    /** Constant */ public static final int HASTE_SELF = 12 + POTION;
    /** Constant */ public static final int SEE_INVISIBLE = 13 + POTION;
    /** Constant */ public static final int POTIONS = 14 + POTION;

    /** Constant */ public static final int TELE_AWAY = 0 + WAND;
    /** Constant */ public static final int SLOW_MONSTER = 1 + WAND;
    /** Constant */ public static final int INVISIBILITY = 2 + WAND;
    /** Constant */ public static final int POLYMORPH = 3 + WAND;
    /** Constant */ public static final int HASTE_MONSTER = 4 + WAND;
    /** Constant */ public static final int MAGIC_MISSILE = 5 + WAND;
    /** Constant */ public static final int CANCELLATION = 6 + WAND;
    /** Constant */ public static final int DO_NOTHING = 7 + WAND;
    /** Constant */ public static final int DRAIN_LIFE = 8 + WAND;
    /** Constant */ public static final int COLD = 9 + WAND;
    /** Constant */ public static final int FIRE = 10 + WAND;
    /** Constant */ public static final int WANDS = 11 + WAND;

    /** Constant */ public static final int STEALTH = 0 + RING;
    /** Constant */ public static final int R_TELEPORT = 1 + RING;
    /** Constant */ public static final int REGENERATION = 2 + RING;
    /** Constant */ public static final int SLOW_DIGEST = 3 + RING;
    /** Constant */ public static final int ADD_STRENGTH = 4 + RING;
    /** Constant */ public static final int SUSTAIN_STRENGTH = 5 + RING;
    /** Constant */ public static final int DEXTERITY = 6 + RING;
    /** Constant */ public static final int ADORNMENT = 7 + RING;
    /** Constant */ public static final int R_SEE_INVISIBLE = 8 + RING;
    /** Constant */ public static final int MAINTAIN_ARMOR = 9 + RING;
    /** Constant */ public static final int SEARCHING = 10 + RING;
    /** Constant */ public static final int RINGS = 11 + RING;

    /** Constant */ public static final int RATION = 0 + FOOD;
    /** Constant */ public static final int FRUIT = 1 + FOOD;

    /** Constant */ public static final int NOT_USED = 0;
    /** Constant */ public static final int BEING_WIELDED = 01;
    /** Constant */ public static final int BEING_WORN = 02;
    /** Constant */ public static final int ON_LEFT_HAND = 04;
    /** Constant */ public static final int ON_RIGHT_HAND = 010;
    /** Constant */ public static final int ON_EITHER_HAND = 014;
    /** Constant */ public static final int BEING_USED = 017;

    /** Constant */ public static final int UNIDENTIFIED = 0;
    /** Constant */ public static final int IDENTIFIED = 1;
    /** Constant */ public static final int CALLED = 2;

    /** Constant */ public static final int UPWARD = 0;
    /** Constant */ public static final int UPRIGHT = 1;
    /** Constant */ public static final int RIGHT = 2;
    /** Constant */ public static final int DOWNRIGHT = 3;
    /** Constant */ public static final int DOWN = 4;
    /** Constant */ public static final int DOWNLEFT = 5;
    /** Constant */ public static final int LEFT = 6;
    /** Constant */ public static final int UPLEFT = 7;
    /** Constant */ public static final int DIRS = 8;

    /**
     * @param character
     * @return true if the provided character is for moving in a direction.
     */
    public static int isDirection(int character) {
        switch (character) {
            case Event.LEFT:
            case 'h':
                return LEFT;
            case Event.DOWN:
            case 'j':
                return DOWN;
            case Event.UP:
            case 'k':
                return UPWARD;
            case Event.RIGHT:
            case 'l':
                return RIGHT;
            case Event.END:
            case 'b':
                return DOWNLEFT;
            case Event.HOME:
            case 'y':
                return UPLEFT;
            case Event.PGUP:
            case 'u':
                return UPRIGHT;
            case Event.PGDN:
            case 'n':
                return DOWNRIGHT;
            case '\033':
                return -1;
        }
        
        return -2;
    }

    /**
     * @param startRow
     * @param startCol
     * @param destinationRow
     * @param destinationCol
     * @return The direction constant for the direction between the start and destination location 
     */
    public static int getDirection(int startRow, int startCol, int destinationRow, int destinationCol) {
        if (startRow > destinationRow) {
            return startCol > destinationCol ? UPLEFT : (startCol < destinationCol ? UPRIGHT : UPWARD);
        }
        if (startRow < destinationRow) {
            return startCol > destinationCol ? DOWNLEFT : (startCol < destinationCol ? DOWNRIGHT : DOWN);
        }
        
        return startCol < destinationCol ? RIGHT : LEFT;
    }

    private static Id[] getIdList(String[] list, int status) {
        int itemCount = list.length / 3;
        int i = 0;
        Id[] ids = new Id[itemCount];
        for (int j = 0; j < itemCount; j++) {
            ids[j] = new Id();
            ids[j].value = Integer.parseInt(list[i++]);
            ids[j].title = list[i++];
            ids[j].real = list[i++];
            ids[j].idStatus = status;
        }
        
        return ids;
    }

    /*
     * static void list_items(){ id_potions= idlist(potionslist, 0); id_scrolls=
     * idlist(scrollslist, 0); id_weapons= idlist(weaponslist, 0); id_armors=
     * idlist(armorslist, 0); id_wands= idlist(wandslist, 0); id_rings=
     * idlist(ringslist, 0); }
     */
    /**
     * @param ch
     * @return true if the given character is a vowel
     */
    public static boolean isVowel(int ch) {
        if (ch < 'a') {
            ch += 32;
        }
        
        return ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u';
    }

    /**
     * @param mask
     * @return The display character based on the Toy type
     */
    public static char getMaskCharacter(int mask) {
        switch (mask & ALL_TOYS) {
            case SCROLL:
                return '?';
            case POTION:
                return '!';
            case GOLD:
                return '*';
            case FOOD:
                return ':';
            case WAND:
                return '/';
            case ARMOR:
                return ']';
            case WEAPON:
                return ')';
            case RING:
                return '=';
            case AMULET:
                return ',';
            default:
                return '~'; /* unknown, something is wrong */
        }
    }

    private static Id[] getIdTable(int kind) {
        switch (kind & ALL_TOYS) {
            case SCROLL:
                return idScrolls;
            case POTION:
                return idPotions;
            case WAND:
                return idWands;
            case RING:
                return idRings;
            case WEAPON:
                return idWeapons;
            case ARMOR:
                return idArmors;
        }
        
        return null;
    }

    /**
     * @param obj
     * @return The table to be used for the type of Toy
     */
    public static Id[] getIdTable(Toy obj) {
        return getIdTable(obj.kind);
    }

    /**
     * @param toy
     * @return The description for the provided Toy
     */
    public static String getDescription(Toy toy) {
        String itemName;
        String description = "";
        int itstatus = 99;
        int species = toy.kind & ALL_TOYS;
        int what = toy.kind & 255;

        if (species == AMULET) {
            return "the amulet of Yendor ";
        }

        if (species == GOLD) {
            return "" + toy.quantity + " pieces of gold";
        }

        itemName = toy.name();

        if (species != ARMOR) {
            description = toy.quantity == 1 ? "a " : "" + toy.quantity + " ";
        }

        if (species == FOOD) {
            if (toy.kind == RATION) {
                description = toy.quantity > 1 ? "" + toy.quantity + " rations of " : "some ";
            } else {
                description = "a ";
            }
            description = description + itemName;
            itstatus = 98; /* Flag just name it */
        }
        Id[] idTable = getIdTable(toy);

        if (0 != (species & (WEAPON | ARMOR | WAND | RING))) {
            itstatus = UNIDENTIFIED;
        }
        if (itstatus == 99) {
            itstatus = idTable[what].idStatus;
        }
        while (itstatus != 98) {
            switch (itstatus) {
                case UNIDENTIFIED:
                    switch (species) {
                        case SCROLL:
                            description = description + itemName + "entitled: " + idTable[what].title;
                            break;
                        case POTION:
                            description = description + idTable[what].title + itemName;
                            break;
                        case WAND:
                        case RING:
                            if (toy.identified || idTable[what].idStatus == IDENTIFIED) {
                                itstatus = IDENTIFIED;
                            } else if (idTable[what].idStatus == CALLED) {
                                itstatus = CALLED;
                            } else { 
                                description = description + idTable[what].title + itemName;
                            }
                            break;
                        case ARMOR:
                            if (toy.identified) {
                                itstatus = IDENTIFIED;
                            } else {
                                description = description + idTable[what].title;
                            }
                            break;
                        case WEAPON:
                            if (toy.identified) {
                                itstatus = IDENTIFIED;
                            } else {
                                description = description + toy.name();
                            }
                            break;
                    }
                    if (itstatus == UNIDENTIFIED) {
                        itstatus = 98;
                    }
                    break;
                case CALLED:
                    switch (species) {
                        case SCROLL:
                        case POTION:
                        case WAND:
                        case RING:
                            description = description + itemName + "called " + idTable[what].title;
                            break;
                    }
                    itstatus = 98;
                    break;
                case IDENTIFIED:
                    switch (species) {
                        case SCROLL:
                        case POTION:
                            description = description + itemName + idTable[what].real;
                            break;
                        case RING:
                            if (toy.identified) {
                                if (toy.kind == DEXTERITY || toy.kind == ADD_STRENGTH) {
                                    if (toy.klass > 0) {
                                        description = description + '+';
                                    }
                                    description = description + toy.klass;
                                }
                            }
                            description = description + itemName + idTable[what].real;
                            break;
                        case WAND:
                            description = description + itemName + idTable[what].real;
                            if (toy.identified) {
                                description = description + '[' + toy.klass + ']';
                            }
                            break;
                        case ARMOR:
                            if (toy.dEnchant >= 0) {
                                description = description + '+';
                            }
                            description = description + toy.dEnchant + " " + idTable[what].title + '[' + toy.getArmorClass() + ']';
                            break;
                        case WEAPON:
                            if (toy.hitEnchant >= 0) {
                                description = description + '+';
                            }
                            description = description + toy.hitEnchant + ',';
                            if (toy.dEnchant >= 0) {
                                description = description + '+';
                            }
                            description = description + toy.dEnchant + " " + toy.name();
                            break;
                    }
                    itstatus = 98;
                    break;
            }
        }
        if (description.length() > 3) {
            if (description.charAt(0) == 'a' && description.charAt(1) == ' ') {
                if (isVowel(description.charAt(2))) {
                    String title = description.substring(1);
                    description = "an" + title;
                }
            }
        }
        if (0 != (toy.inUseFlags & BEING_WIELDED)) {
            description = description + " in hand";
        } else if (0 != (toy.inUseFlags & BEING_WORN)) {
            description = description + " being worn";
        } else if (0 != (toy.inUseFlags & ON_LEFT_HAND)) {
            description = description + " on left hand";
        } else if (0 != (toy.inUseFlags & ON_RIGHT_HAND)) {
            description = description + " on right hand";
        }
        
        return description;
    }

    /**
     * @param rand
     * @return A random species of Toy
     */
    public static int getRandomSpecies(Randomx rand) {
        int species = RING;
        int percent = rand.get(1, 91);

        if (percent <= 30) {
            species = SCROLL;
        } else if (percent <= 60) {
            species = POTION;
        } else if (percent <= 64) {
            species = WAND;
        } else if (percent <= 74) {
            species = WEAPON;
        } else if (percent <= 83) {
            species = ARMOR;
        } else if (percent <= 88) {
            species = FOOD;
        }
        
        return species;
    }

    /**
     * @param rand
     * @return A random scroll
     */
    public static int getRandomWhichScroll(Randomx rand) {
        int percent = rand.get(91);
        int scroll = 0;

        if (percent <= 5) {
            scroll = PROTECT_ARMOR;
        } else if (percent <= 10) {
            scroll = HOLD_MONSTER;
        } else if (percent <= 20) {
            scroll = CREATE_MONSTER;
        } else if (percent <= 35) {
            scroll = IDENTIFY;
        } else if (percent <= 43) {
            scroll = TELEPORT;
        } else if (percent <= 50) {
            scroll = SLEEP;
        } else if (percent <= 55) {
            scroll = SCARE_MONSTER;
        } else if (percent <= 64) {
            scroll = REMOVE_CURSE;
        } else if (percent <= 69) {
            scroll = ENCH_ARMOR;
        } else if (percent <= 74) {
            scroll = ENCH_WEAPON;
        } else if (percent <= 80) {
            scroll = AGGRAVATE_MONSTER;
        } else if (percent <= 86) {
            scroll = CON_MON;
        } else {
            scroll = MAGIC_MAPPING;
        }
        
        return scroll;
    }

    /**
     * @param rand
     * @return A random potion
     */
    public static int getRandomWhichPotion(Randomx rand) {
        int percent = rand.get(118);
        int potion = 0;
        
        if (percent <= 5) {
            potion = RAISE_LEVEL;
        } else if (percent <= 15) {
            potion = DETECT_TOYS;
        } else if (percent <= 25) {
            potion = DETECT_MONSTER;
        } else if (percent <= 35) {
            potion = INCREASE_STRENGTH;
        } else if (percent <= 45) {
            potion = RESTORE_STRENGTH;
        } else if (percent <= 55) {
            potion = HEALING;
        } else if (percent <= 65) {
            potion = EXTRA_HEALING;
        } else if (percent <= 75) {
            potion = BLINDNESS;
        } else if (percent <= 85) {
            potion = HALLUCINATION;
        } else if (percent <= 95) {
            potion = CONFUSION;
        } else if (percent <= 105) {
            potion = POISON;
        } else if (percent <= 110) {
            potion = LEVITATION;
        } else if (percent <= 114) {
            potion = HASTE_SELF;
        } else {
            potion = SEE_INVISIBLE;
        }
        
        return potion;
    }

    /**
     * Set the status of the provided item to identified
     * 
     * @param kind
     */
    public static void identify(int kind) {
        Id[] idTable = getIdTable(kind);
        if (idTable != null) {
            idTable[kind & 255].idStatus = IDENTIFIED;
        }
    }

    /**
     * @param kind
     */
    public static void identifyUncalled(int kind) {
        Id[] idTable = getIdTable(kind);
        if (idTable != null) {
            if (idTable[kind & 255].idStatus != CALLED) {
                idTable[kind & 255].idStatus = IDENTIFIED;
            }
        }
    }

    /**
     * 
     */
    public static void wizardIdentify() {
        for (int species = ARMOR; species <= ALL_TOYS; species *= 2) {
            Id[] idTable = getIdTable(species);
            if (idTable != null) {
                for (int i = 0; i < idTable.length; i++) {
                    identify(species + i);
                }
            }
        }
    }

    private final static String TOY_CHARS = "%!?]=/):*";

    /**
     * @param rand
     * @return Random character for a Toy
     */
    public static char getRandomObjectCharacter(Randomx rand) {
        return TOY_CHARS.charAt(rand.get(TOY_CHARS.length() - 1));
    }

    /**
     * Set an identity string for the Man.
     * 
     * @param man
     */
    public static void idType(Man man) {
        String id = "unknown character";
        int ch;
        man.tell("what do you want identified?");
        ch = man.self.rgetchar();
        if ((ch >= 'A') && (ch <= 'Z')) {
            id = Monster.MONSTER_TABLE[ch - 'A'].mName;
        } else if (ch < 32) {
            ch = '?';
        } else {
            switch (ch) {
                case '$':
                    id = "unidentified monster";
                    break;
                case '@':
                    id = "human";
                    break;
                case '%':
                    id = "staircase";
                    break;
                case '^':
                    id = "trap";
                    break;
                case '+':
                    id = "door";
                    break;
                case '-':
                case '|':
                    id = "wall of a room";
                    break;
                case '.':
                    id = "floor";
                    break;
                case '#':
                    id = "passage";
                    break;
                case ' ':
                    id = "solid rock";
                    break;
                case '=':
                    id = "ring";
                    break;
                case '?':
                    id = "scroll";
                    break;
                case '!':
                    id = "potion";
                    break;
                case '/':
                    id = "wand or staff";
                    break;
                case ')':
                    id = "weapon";
                    break;
                case ']':
                    id = "armor";
                    break;
                case '*':
                    id = "gold";
                    break;
                case ':':
                    id = "food";
                    break;
                case ',':
                    id = "the Amulet of Yendor";
                    break;
            }
        }
        man.view.msg.checkMessage();
        man.tell((char) ch + " : " + id);
    }

    /**
     * @param s
     * @return An array with the first index being the number of rolls and the second index being the size of the die.
     */
    public static int[] parseDamage(String s) {
        int[] answer = new int[2];
        int damage = s.indexOf('d');
        if (damage > 0) {
            answer[0] = Integer.parseInt(s.substring(0, damage));
            answer[1] = Integer.parseInt(s.substring(damage + 1));
        } else {
            answer[0] = Integer.parseInt(s);
            answer[1] = 0;
        }
        
        return answer;
    }

    /**
     * @param damageString
     * @param rand
     * @return The amount of damage done
     */
    public static int getDamage(String damageString, Randomx rand) {
        int total = 0;
        StringTokenizer st = new StringTokenizer(damageString, "/d", false);
        while (st.hasMoreTokens()) {
            int numberOfRolls = Integer.parseInt(st.nextToken());
            int diceType = Integer.parseInt(st.nextToken());
            for (int i = 0; i < numberOfRolls; i++) {
                total += rand != null ? rand.get(1, diceType) : diceType;
            }
        }
        
        return total;
    }
}
