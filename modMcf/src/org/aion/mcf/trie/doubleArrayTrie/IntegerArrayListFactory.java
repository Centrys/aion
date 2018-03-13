package org.aion.mcf.trie.doubleArrayTrie;

public class IntegerArrayListFactory implements IntegerListFactory {

    private final int initialCapacity;
    private final int numerator;
    private final int denominator;
    private final int fixedInc;

    /**
     * Private, for use by static factory methods.
     */
    private IntegerArrayListFactory(int initialCapacity, int numerator, int denominator, int fixedInc) {
        this.initialCapacity = initialCapacity;
        this.numerator = numerator;
        this.denominator = denominator;
        this.fixedInc = fixedInc;
    }

    /**
     * Creates and returns an <tt>IntegerListFactory</tt> that manufactures <tt>IntegerArrayList</tt>s
     * with an initial capacity of <tt>initialCapacity</tt> and a growth factor of <p>
     * <tt>numerator/denominator + fixedInc</tt>
     *
     * @param initialCapacity The initialCapacity of the Array
     * @param numerator
     * @param denominator
     * @param fixedInc
     * @return
     */
    public static IntegerArrayListFactory newInstance(int initialCapacity, int numerator, int denominator, int fixedInc) {
        return new IntegerArrayListFactory(initialCapacity, numerator, denominator, fixedInc);
    }

    /**
     * Creates and returns an <tt>IntegerListFactory</tt> that manufactures <tt>IntegerArrayList</tt>s
     * with an initial capacity of 16 and a growth factor of 5/4 + 10.
     *
     * @return An IntegerArrayList with sensible defaults.
     */
    public static IntegerArrayListFactory newInstance() {
        return newInstance(16, 5, 4, 10);
    }


    public IntegerList getNewIntegerList() {
        return new IntegerArrayList(initialCapacity, numerator, denominator, fixedInc);
    }
}
