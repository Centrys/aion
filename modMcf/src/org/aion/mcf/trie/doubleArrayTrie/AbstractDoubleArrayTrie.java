package org.aion.mcf.trie.doubleArrayTrie;

import org.aion.base.util.ByteArrayWrapper;
import org.aion.crypto.HashUtil;
import org.aion.mcf.trie.Node;
import org.aion.rlp.RLP;
import org.aion.rlp.Value;

import java.util.*;

import static org.aion.base.util.ByteArrayWrapper.wrap;
import static org.aion.crypto.HashUtil.EMPTY_TRIE_HASH;
import static org.aion.rlp.CompactEncoder.binToNibbles;
import static org.aion.rlp.CompactEncoder.packNibbles;
import static org.aion.rlp.Utils.hexEncode;

public abstract class AbstractDoubleArrayTrie {

    // The leaf base value
    protected static final int LEAF_BASE_VALUE = -2;
    // The root check value, normally unnecessary
    protected static final int ROOT_CHECK_VALUE = -3;
    // The unoccupied spot value
    protected static final int EMPTY_VALUE = -1;
    // The initial offset.
    protected static final int INITIAL_ROOT_BASE = 1;
    // The alphabet length
    protected final int alphabetLength;
    //storing the hashmap of the values of the leaf nodes
    private Map<Integer, byte[]> cache = new HashMap<>();
    private Map<Integer, Object> hashCache = new HashMap<>();

    /**
     * Constructs a DoubleArrayTrie for the given alphabet length.
     *
     * @param alphabetLength The size of the set of values that
     * 				are to be stored.
     */
    protected AbstractDoubleArrayTrie(int alphabetLength) {
        this.alphabetLength = alphabetLength;
    }

    public boolean addToTrie(String key, String value) {
        return addToTrie(key.getBytes(), value.getBytes());
    }

    /**
     * Adds this string to the trie.
     *
     */
    public boolean addToTrie(byte[] key, byte[] value) {
        boolean changed = false;
        // Start from the root
        int state = 0;		// The current DFA state ordinal
        int transition = 0;	// The candidate for the transition end state
        int i = 0;			// The input string index
        int c = 0;			// The current input string character
        // For every input character
        byte[] keyNibbles = binToNibbles(key);
        while (i < keyNibbles.length) {
            assert state >= 0;
            assert getBase(state) >= 0;
            c = keyNibbles[i];
            // Calculate next hop. It is the base contents of the current state
            // plus the input character.
            transition = getBase(state) + c;
            assert transition > 0;
            ensureReachableIndex(transition);
            /*
             * If the next hop index is empty
             * (-1), then simply add a new state of the DFA in that spot, with
             * owner state the current state and next hop address the next available
             * space.
             */
            if (getCheck(transition) == EMPTY_VALUE) {
                setCheck(transition, state);
                if (i == keyNibbles.length - 1) { 				// The string is done
                    setBase(transition, LEAF_BASE_VALUE); 	// So this is a leaf

                    //storing the value in the cache for further referencing
                    Object[] newNode = new Object[] { packNibbles(keyNibbles), value };
                    cache.put(transition, value);
                    hashCache.put(transition, getHash(newNode));
                    //backtracking hashing values to the root node
                    propagate(transition);

                    changed = true;
                }
                else {
                    setBase(transition, nextAvailableHop(keyNibbles[i + 1])); // Add a state
                    changed = true;
                }
            }
            else if (getCheck(transition) != state) { // We have been through here before
                /*
                 *
                 * The place we must add a new children state is already
                 * occupied. Move this state's base to a new location.
                 */
                resolveConflict(state, c);
                changed = true;
                // We must redo this character
                continue;
            }
            else if (getBase(transition) == LEAF_BASE_VALUE) {
                //update the leaf if it exists

                Object[] newNode = new Object[] { packNibbles(keyNibbles), value };
                cache.put(transition, value);

                hashCache.put(transition, getHash(newNode));
                propagate(transition);

                changed = true;
            }

            /*
             * There is another case that is the default and always executed
             * by the if above. That is simply transition through the DFA
             * and advance the string index. This is done after we notify
             * for the transition event.
             */
            //TODO @Robert this does nothing as of now
            //updateInsert(state, i-1, keyNibbles);
            state = transition;
            i++;
        }
        return changed;
    }

    private static Object[] emptyStringSlice(int l) {
        Object[] slice = new Object[l];
        for (int i = 0; i < l; i++) {
            slice[i] = "";
        }
        return slice;
    }

    public Object getRoot(){
        //System.out.println(hashCache.get(0));
        return hashCache.get(0);
    }

    public byte[] getRootHash() {
        if (getRoot() == null || (getRoot() instanceof byte[] && ((byte[]) getRoot()).length == 0) || (getRoot() instanceof String && ""
                .equals(getRoot()))) {
            return EMPTY_TRIE_HASH;
        } else if (getRoot() instanceof byte[]) {
            return (byte[]) this.getRoot();
        } else {
            Value rootValue = new Value(this.getRoot());
            return HashUtil.h256(rootValue.encode());
        }
    }

    private void propagate(int transition) {
        //propagate the hash upwards in the trie up to the corresponding root node
        int currentTransition = transition;
        do {
            //get the parent of this node
            int parentState   = getCheck(currentTransition);
            Object[] childHashes = emptyStringSlice(17);

            int numberOfChilds = 0;
            int lastChildIndex = -1;
            //get all he children from this node
            for (int c = 0; c < alphabetLength; c++) {
                int tempNext = getBase(parentState) + c;
                if (tempNext < getSize() && getCheck(tempNext) == parentState) {
                    if (hashCache.get(tempNext) != null) {
                        childHashes[c] = (hashCache.get(tempNext));
                        lastChildIndex = c;
                        numberOfChilds++;
                    } else {
                        System.out.println("The child has no hash");
                    }
                }
            }

            // we have all the hashes of it's children
            // compute the parent hash
            if(numberOfChilds > 1) {
                hashCache.put(parentState, getHash(childHashes));
            } else {
                hashCache.put(parentState, childHashes[lastChildIndex]);
            }

            currentTransition = parentState;
        } while(currentTransition != 0);
    }

    private Object getHash(Object o){
        Value value = new Value(o);
        byte[] enc = value.encode();
        if (enc.length >= 32) {
            return HashUtil.h256(value.encode());
        }
        return value;
    }



    /**
     * This method is the most complex part of the algorithm.
     * First of all, keep in mind that the children of a state
     * are stored in ordered locations. That means that there is the possibility
     * that although a new child for state s must be added, the position
     * has already been taken. This is the conflict that is resolved here.
     * There are two ways. One is to move the obstructing state to a new
     * location and the other is to move the obstructed state. Here the
     * latter is chosen. This also ensures that the root node is never moved.
     * @param s The state to move
     * @param newValue The value that causes the conflict.
     */
    protected void resolveConflict(int s, int newValue) {

        // The set of children values
        TreeSet<Integer> values = new TreeSet<Integer>();

        // Add the value-to-add
        values.add(new Integer(newValue));

        // Find all existing children and add them too.
        for (int c = 0; c < alphabetLength; c++) {
            int tempNext = getBase(s) + c;
            if (tempNext < getSize() && getCheck(tempNext) == s)
                values.add(new Integer(c));
        }

        // Find a place to move them.
        int newLocation = nextAvailableMove(values);

        // newValue is not yet a child of s, so we should not check for it.
        values.remove(new Integer(newValue));

        /*
         * This is where the job is done. For each child of s,
         */
        for (Integer value : values) {
            int c = value.intValue();		// The child state to move
            int tempNext = getBase(s) + c;	//
            assert tempNext < getSize();
            assert getCheck(tempNext) == s;
            /*
             * base(s)+c state is child of s.
             * Mark new position as owned by s.
             */
            assert getCheck(newLocation + c) == EMPTY_VALUE;
            setCheck(newLocation + c, s);

            /*
             * Copy pointers to children for this child of s.
             * Note that even if this child is a leaf, this is needed.
             */
            assert getBase(newLocation + c) == EMPTY_VALUE;
            setBase(newLocation + c, getBase(getBase(s) + c));

            //TODO @Robert test this separately
            if(getBase(getBase(s) + c) == LEAF_BASE_VALUE ) {
                byte[] oldValue = cache.get(s);
                cache.remove(getBase(s) + c);
                cache.put(getBase(newLocation + c), oldValue);
            }

            if(hashCache.get(getBase(s) + c) != null) {
                Object tmp = hashCache.get(getBase(s) + c);
                hashCache.remove(getBase(s) + c);
                hashCache.put(newLocation + c, tmp);
            }

            updateChildMove(s, c, newLocation);
            /*
             * Here the child c is moved, but not *its* children. They must be
             * updated so that their check values point to the new position of their
             * parent (i.e. c)
             */
            if (getBase(getBase(s) + c) != LEAF_BASE_VALUE) {
                // First, iterate over all possible children of c
                for (int d = 0; d < alphabetLength; d++) {
                    /*
                     *  Get the child. This could well be beyond the store size
                     *  since we don't know how many children c has.
                     */
                    int tempNextChild = getBase(getBase(s) + c) + d;
                    /*
                     * Here we could also check if tempNext > 0, since
                     * negative values end the universe. However, since the
                     * implementation of nextAvailableHop never returns
                     * negative values, this should never happen. Presto, a
                     * nice way of catching bugs.
                     */
                    if (tempNextChild < getSize() && getCheck(tempNextChild) == getBase(s) + c) {
                        // Update its check value, so that it shows to the new position of this child of s.
                        setCheck(getBase(getBase(s) + c) + d, newLocation + c);
                    }
                    else if (tempNextChild >= getSize()) {
                        /*
                         *  Minor optimization here. If the above if fails then tempNextChild > check.size()
                         *  or the tempNextChild position is already owned by some other state. Remember
                         *  that children states are stored in increasing order (though not necessarily
                         *  right next to each other, since other states can be between the gaps they leave).
                         *  That means that failure of the second part of the conjuction of the if above
                         *  does not mean failure, since the next child can exist. Failure of the first conjuct
                         *  however means we are done, since all the rest of the children will only be further
                         *  down the store and therefore beyond its end also. Nothing left to do but break
                         */
                        break;
                    }
                }
                // Finally, free the position held by this child of s
                setBase(getBase(s) + c, EMPTY_VALUE);
                setCheck(getBase(s) + c, EMPTY_VALUE);
            }
        }
        // Here, all children and grandchildren (if existent) of s have been
        // moved or updated. That which remains is for the state s to show
        // to its new children
        setBase(s, newLocation);
        updateStateMove(s, newLocation);
    }

    public byte[] get(byte[] prefix) {
        return runPrefix(prefix);
    }

    /**
     * This method, at its core, walks a path on the trie. Given a string, it
     * decides whether it is contained as a prefix of other strings, if it is
     * contained as a standalone string or if it is not present. Particularly:
     * <li>If the string is contained but has not been inserted</li>
     *
     * @return The result of the search
     */
    protected byte[] runPrefix(byte[] key) {
        int state		= 0; // The current DFA state ordinal
        int transition	= 0; // The candidate for the transition end state
        int i			= 0; // The input string index
        int current		= 0; // The current input character
        SearchState result = new SearchState();  // The search result
        byte[] keyNibbles = binToNibbles(key);
        result.prefix = keyNibbles;
        result.result = SearchResult.PURE_PREFIX; // The default value
        // For every input character
        while (i < keyNibbles.length) {
            current = keyNibbles[i];
            assert current >= 0;
            assert current < alphabetLength;
            transition = getBase(state) + current;	// Get next candidate state
            if (transition < getSize() && getCheck(transition) == state) {	// If it is valid...
                if (getBase(transition) == LEAF_BASE_VALUE) {
                    // We reached a leaf. There are two possibilities:
                    if (i == keyNibbles.length - 1) {
                        // The string has been exhausted. Return perfect match
                        result.result = SearchResult.PERFECT_MATCH;
                        return cache.get(transition);
                    } else {
                        // The string still has more to go. Return not found.
                        result.result = SearchResult.NOT_FOUND;
                        break;
                    }
                }
                state = transition; //  ...switch and continue
            }
            else {
                // The candidate does not belong to the current state. Not found.
                result.result = SearchResult.NOT_FOUND;
                break;
            }
            //TODO @Robert this does nothing
            //updateSearch(state, i, keyNibbles);
            i++;
        }
        //TODO @Robert this does nothing
        //updateSearch(state, i, keyNibbles);
        result.finishedAtState = state;
        result.index = i;
        //return result;
        return null;
    }

    public int getAlphabetSize() {
        return alphabetLength;
    }

    // Event management methods. Delegated to subclasses.

    /**
     * For every state transition during an insertion, this method is called to
     * inform implementations of the fact and do their housekeeping.
     *
     * @param state The index in the base array the transition is at
     * @param stringIndex The index of the inserted string for which the event occurred
     * @param insertString The inserted string
     */
    protected abstract void updateInsert(int state, int stringIndex, IntegerList insertString);

    /**
     * For every state transition during a search, this method is called to
     * inform implementations of the fact and do their housekeeping.
     *
     * @param state The index in the base array the transition is at
     * @param stringIndex The index of the search string for which the event occurred
     * @param searchString The search string
     */
    protected abstract void updateSearch(int state, int stringIndex, IntegerList searchString);

    /**
     * After a state conflict, each children of the parent state is moved to a
     * new location. For each such event, this method is called with all
     * necessary information. This method is called AFTER the move of the child
     * and before the move of the parent and provides the array index of the parent
     * state, the character that is the child and the new parent state base value.
     *
     * @param parentIndex The index of the parent state
     * @param forCharacter The character leading to this child from the parent.
     * @param newParentBase The new parent base value
     */
    protected abstract void updateChildMove(int parentIndex, int forCharacter, int newParentBase);

    /**
     * After a state conflict and after the moved state's children have been themselves moved,
     * the base of the state must change. This method is called AFTER this change happens and
     * after all children have been moved
     *
     * @param stateIndex The index of the state whose base is changed
     * @param newBase The new base value for the state
     */
    protected abstract void updateStateMove(int stateIndex, int newBase);

    // Storage management methods. Delegated to subclasses.

    /**
     * Returns the value of the base array at <tt>position</tt>.
     *
     * @param position The index in the base array
     * @return The value at <tt>position</tt>
     */
    protected abstract int getBase(int position);

    /**
     * Returns the value of the check array at <tt>position</tt>.
     *
     * @param position The index in the check array
     * @return The value at <tt>position</tt>
     */
    protected abstract int getCheck(int position);

    /**
     * Sets the value of the base array at <tt>position</tt> to
     * value <tt>value</tt>.
     *
     * @param position The index in the base array whose value is to be set
     * @param value The value to set
     */
    protected abstract void setBase(int position, int value);

    /**
     * Sets the value of the check array at <tt>position</tt> to
     * value <tt>value</tt>.
     *
     * @param position The index in the check array whose value is to be set
     * @param value The value to set
     */
    protected abstract void setCheck(int position, int value);

    /**
     * Returns the size of the backing store. Indexes above this
     * value are assumed not to exist.
     *
     * @return The size of the backing store. Equal to both the
     * 		size of the base array and of the check array
     */
    protected abstract int getSize();

    /**
     * Finds a suitable location for inserting a new state transition.
     * In essence, it returns the lesser free position that is at least
     * equal to the argument.
     *
     * @param forValue An ordinal of the trie content type.
     * @return An index of the store that can support the argument.
     */
    protected abstract int nextAvailableHop(int forValue);

    /**
     * Does the same thing as nextAvailableHop, that is finds where in the store
     * there is room for insertion, but instead for one transition, it does that for
     * a subgraph of the trie.
     *
     * @param values The children of a state.
     * @return Where the state must be moved to accommodate it's children.
     */
    protected abstract int nextAvailableMove(SortedSet<Integer> values);

    /**
     * Ensures that the size of the backing store is enough to
     * have an addressable <tt>index</tt>. This means that
     * all array accesses up to and including <tt>index</tt> must
     * be successful. The getSize() call must return
     * the greatest value that has been passed as an argument
     * to this method during a run.
     *
     * @param index The index to be reachable
     */
    protected abstract void ensureReachableIndex(int index);

    /**
     * Utility class to represent the necessary state after the end
     * of a search. The walking algorithm besides deciding on the
     * search result outcome is also useful to find the last valid
     * index of an input string. This class represents just that.
     */
    protected static class SearchState {
        /**
         * The searched for string
         */
        protected byte[] prefix;

        /**
         * The index within the prefix string that the search ended.
         * If it was exhausted without reaching a leaf node it is
         * equal to prefix.size()
         */
        protected int index;

        /**
         * The index in the base array of the state at which the
         * walking algorithm concluded.
         */
        protected int finishedAtState;

        /**
         * The result of the search. It is also reproducible by
         * the other fields of this class.
         */
        protected SearchResult result;
    }
}
