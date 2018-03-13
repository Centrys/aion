package org.aion.mcf.trie.doubleArrayTrie;

import java.util.*;

public class DATImpl extends AbstractDoubleArrayTrie {

    // The base array.
    private IntegerList base;
    // The check array.
    private IntegerList check;
    // The free positions, for quick access
    private TreeSet<Integer> freePositions;


    /**
     * Constructs a DoubleArrayTrie for the given alphabet length.
     * Uses a default IntegerArrayList for storage.
     *
     * @param alphabetLength The size of the set of values that
     * 				are to be stored.
     */
    public DATImpl(int alphabetLength) {
        this(alphabetLength, IntegerArrayListFactory.newInstance());
    }

    /**
     * Constructs a DoubleArrayTrie for the given alphabet length that
     * uses the provided IntegerListFactory for creating the storage.
     *
     * @param alphabetLength The size of the set of values that
     * 				are to be stored.
     * @param listFactory The IntegerListFactory to use for creating
     * 				the storage.
     */
    public DATImpl(int alphabetLength, IntegerListFactory listFactory) {
        super(alphabetLength);
        init(listFactory);
    }

    protected void init(IntegerListFactory listFactory) {
        base = listFactory.getNewIntegerList();
        check = listFactory.getNewIntegerList();
        // The original offset, everything non-root starts at base(1)
        base.add(INITIAL_ROOT_BASE);
        // The root check has no meaning, thus a special value is needed.
        check.add(ROOT_CHECK_VALUE);
        freePositions = new TreeSet<Integer>();
    }

    /**
     * Ensures that the index == <tt>limit</tt> is available from
     * the backing arrays. If it already available, this call is
     * almost zero overhead.
     * @param limit The least required accessible index.
     */
    @Override
    protected void ensureReachableIndex(int limit) {
        while (getSize() <= limit) {
            /*
             * In essence, we let all enlargement operations to the implementing
             * class of the backing store. Since this currently is a ArrayList,
             * simply adding values until we are done will work.
             */
            base.add(EMPTY_VALUE);
            check.add(EMPTY_VALUE);
            // All new positions are free by default.
            freePositions.add(base.size() - 1);
        }
    }

    @Override
    protected int nextAvailableHop(int forValue) {

        Integer value = new Integer(forValue);
        /*
         * First we make sure that there exists a free location that is
         * strictly greater than the value.
         */
        while (freePositions.higher(value) == null) {
            ensureReachableIndex(base.size() + 1); // This adds to the freePositions store
        }
        /*
         * From the termination condition of the loop above, the next line
         * CANNOT throw NullPointerException
         * Note that we return the position minus the value. That is because
         * the result is the ordinal of the new state which is translated
         * to a store index. Therefore, since we add the value to the base
         * to find the next state, here we must subtract.
         */
        int result = freePositions.higher(value).intValue() - forValue;
        // This assertion must pass thanks to the loop above
        assert result >= 0;
        return result;
    }

    @Override
    protected int nextAvailableMove(SortedSet<Integer> values) {
        // In the case of a single child, the problem is solved.
        if (values.size() == 1) {
            return nextAvailableHop(values.first());
        }

        int minValue = values.first();
        int maxValue = values.last();
        int neededPositions = maxValue - minValue + 1;

        int possible = findConsecutiveFree(neededPositions);
        if (possible - minValue >= 0) {
            return possible - minValue;
        }

        ensureReachableIndex(base.size() + neededPositions);
        return base.size() - neededPositions - minValue;
    }

    /**
     * Finds consecutive free positions in the trie.
     *
     * @param amount
     *            How many consecutive positions are needed.
     * @return The index of the first position in the group, or -1 if
     *         unsuccessful.
     */
    private int findConsecutiveFree(int amount) {

        assert amount >= 0;
        /*
         * Quick way out, that also ensures the invariants
         * of the main loop.
         */
        if (freePositions.isEmpty()) {
            return -1;
        }

        Iterator<Integer> it = freePositions.iterator();
        Integer from; 		// The location from where the positions begin
        Integer current;	// The next integer in the set
        Integer previous;	// The previously checked index
        int consecutive;	// How many consecutive positions have we seen so far

        from = it.next();	// Guaranteed to succeed, from the if at the start
        previous = from;	// The first previous is the first in the series
        consecutive = 1;	// 1, since from is a valid location
        while(consecutive < amount && it.hasNext()) {
            current = it.next();
            if (current - previous == 1) {
                previous = current;
                consecutive++;
            }
            else {
                from = current;
                previous = from;
                consecutive = 1;
            }
        }
        if (consecutive == amount) {
            return from;
        }
        else {
            return -1;
        }
    }

    @Override
    protected int getBase(int position) {
        return base.get(position);
    }

    @Override
    protected int getCheck(int position) {
        return check.get(position);
    }

    @Override
    protected void setBase(int position, int value) {
        base.set(position, value);
        if (value == EMPTY_VALUE) {
            freePositions.add(new Integer(position));
        }
        else {
            freePositions.remove(new Integer(position));
        }
    }

    @Override
    protected void setCheck(int position, int value) {
        check.set(position, value);
        if (value == EMPTY_VALUE) {
            freePositions.add(new Integer(position));
        }
        else {
            freePositions.remove(new Integer(position));
        }
    }

    @Override
    protected int getSize() {
        return base.size();
    }

    @Override
    protected void updateSearch(int state, int stringIndex, IntegerList searchString) {
        // No op
    }

    @Override
    protected void updateInsert(int state, int stringIndex, IntegerList insertString) {
        // No op
    }

    @Override
    protected void updateChildMove(int parentIndex, int forCharacter,
                                   int newParentBase) {
        assert getCheck(getBase(parentIndex) + forCharacter) == parentIndex;
    }

    @Override
    protected void updateStateMove(int stateIndex, int newBase) {
        // No op
    }
}
