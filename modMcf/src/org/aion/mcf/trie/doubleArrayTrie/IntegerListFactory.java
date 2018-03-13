package org.aion.mcf.trie.doubleArrayTrie;

public interface IntegerListFactory {
    /**
     * Creates and returns an <tt>IntegerList</tt> with the implementation and
     * options this factory was created to hold.
     *
     * @return An <tt>IntegerList</tt> configured as per this factory object
     */
    public IntegerList getNewIntegerList();
}
