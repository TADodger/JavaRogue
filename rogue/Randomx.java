package rogue;

import java.io.Serializable;
import java.util.Random;

class Randomx extends Random implements Serializable {
    private static final long serialVersionUID = 5556093662462787527L;

    static final int STRIP_NEGATIVE_MASK = 0x7fffffff; /* Mask for random number generator */

    Randomx(long l) {
        super(l);
    }

    Randomx() {
        super();
    }

    boolean percent(int number) {
        return (this.nextInt() & STRIP_NEGATIVE_MASK) % 100 < number;
    }

    boolean coin() {
        return 0 != (nextInt() & 1);
    }

    int get(int n0, int n1) {
        return n0 >= n1 ? n0 : n0 + (this.nextInt() & STRIP_NEGATIVE_MASK) % (1 + n1 - n0);
    }

    int get(int number) {
        return (this.nextInt() & STRIP_NEGATIVE_MASK) % (1 + number);
    }

    //shuffling algorithm
    Object[] permute(Object[] objects) {
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
    int[] permute(int[] baseNumbers) {
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
    int[] permute(int n) {
        int[] baseNumbers = new int[n];
        for (int j = 0; j < n; j++) {
            baseNumbers[j] = j;
        }

        return permute(baseNumbers);
    }
}
