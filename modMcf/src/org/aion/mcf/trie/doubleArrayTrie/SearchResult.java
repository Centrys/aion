package org.aion.mcf.trie.doubleArrayTrie;

public enum SearchResult {
    /**
     * Represents a search for a string that
     * successfully ends at a leaf node.
     */
    PERFECT_MATCH,
    /**
     * Represents a search for a string that
     * successfully ends at a non-leaf node.
     */
    PURE_PREFIX,
    /**
     * Represents a search for a string that
     * is successful. Not strictly necessary
     * but useful as the disjunction of
     * PERFECT_MATCH and PURE_PREFIX.
     */
    PREFIX,
    /**
     * Represents a search that was unsuccessful.
     */
    NOT_FOUND
}
