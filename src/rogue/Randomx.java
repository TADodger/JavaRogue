package rogue;

import java.io.Serializable;
import java.util.Random;

/**
 *
 */
public class Randomx extends Random implements Serializable {
    private static final long serialVersionUID = 5556093662462787527L;

    private static final int STRIP_NEGATIVE_MASK = 0x7fffffff; /* Mask for random number generator */

    /**
     * @param l
     */
    public Randomx(long l) {
        super(l);
    }

    /**
     * 
     */
    public Randomx() {
        super();
    }

    /**
     * @param number
     * @return true if random percent is less than given number
     */
    public boolean percent(int number) {
        return (this.nextInt() & STRIP_NEGATIVE_MASK) % 100 < number;
    }

    /**
     * @return true/false coin flip
     */
    public boolean coin() {
        return 0 != (nextInt() & 1);
    }

    /**
     * @param n0
     * @param n1
     * @return ???
     */
    public int get(int n0, int n1) {
        return n0 >= n1 ? n0 : n0 + (nextInt() & STRIP_NEGATIVE_MASK) % (1 + n1 - n0);
    }

    /**
     * @param number
     * @return random number between 0 and the given number
     */
    public int get(int number) {
        return (this.nextInt() & STRIP_NEGATIVE_MASK) % (1 + number);
    }

    //shuffling algorithm
    /**
     * @param objects
     * @return the given objects shuffled
     */
    public Object[] permute(Object[] objects) {
        int j = objects.length;
        while (--j > 0) {
            int i = get(j); // was j-1?
            Object t = objects[j];
            objects[j] = objects[i];
            objects[i] = t;
        }
    
        return objects;
    }

    //shuffling algorithm
    /**
     * @param baseNumbers
     * @return the given numbers shuffled
     */
    public int[] permute(int[] baseNumbers) {
        int n = baseNumbers.length;
        Integer[] numbers = new Integer[n];
        int j;
        for (j = 0; j < n; j++) {
            numbers[j] = new Integer(baseNumbers[j]);
        }
        permute(numbers);
        for (j = 0; j < n; j++) {
            baseNumbers[j] = numbers[j].intValue();
        }
        
        return baseNumbers;
    }

    //shuffling algorithm
    /**
     * @param n
     * @return the given numbers shuffled
     */
    public int[] permute(int n) {
        int[] baseNumbers = new int[n];
        for (int j = 0; j < n; j++) {
            baseNumbers[j] = j;
        }

        return permute(baseNumbers);
    }
}
