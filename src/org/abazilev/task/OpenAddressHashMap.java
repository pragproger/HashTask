package org.abazilev.task;

import java.util.*;


/**
 * @author  abazilev
 *
 * Class is not thread safe!!!!!
 *
 * Null keys are not possible! Null values are possible. 0 means free cell.
 *
 */
public final class OpenAddressHashMap  {

    private final int DEFAULT_SIZE = 127;
    private final int SECOND_HASH_NUMBER = 7;

    private static class Entry {
        private final int key;
        private long value;

        private Entry(int key, long value) {
            this.value = value;
            this.key = key;
        }

        private int getKey() {
            return key;
        }

        private long getValue() {
            return value;
        }

        private void setValue(long value) {
            this.value = value;
        }
    }

    private Entry [] elements;
    private int size;

    //assertions will be used to track invariants

    public OpenAddressHashMap() {
        elements = new Entry [DEFAULT_SIZE];
        checkInvariant();
    }

    /**
     *
     * @return simply the quantity of entries in map
     */
    public int size() {
        return size;
    }

    /**
     *
     * @return true if map is empty, false in another case
     */
    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(int key) {
        return getElementByKey(key) != null;
    }

    private Entry getElementByKey(int key) {
        int index = getFreeOrSameKeyPosition(key);

        return (index != -1) ? elements[index] : null;
    }

    /**
     *
     * @param value any
     * @return true if some entry has the specified value
     */
    public boolean containsValue(long value) {
        //we should iterate across our internal list
        for(int i = 0;i < elements.length;i++) {
           if(elements[i] != null && elements[i].getValue() == value) {
               return true;
           }
        }

        return false;
    }

    /**
     *
     * @param key is not null!
     * @return
     */
    public long get(Integer key) {
        Entry entry = getElementByKey(key);

        return entry != null ? entry.getValue() : 0;
    }

    /**
     *
     * @param key
     * @param value can be 0 but in this case you will not be able to check the
     *              presence of value in hash table
     * @return
     */
    public long put(Integer key, Long value) {
        final Long result = doPut(key, value);

        checkInvariant();
        return result;
    }

    /**
     * This function checks the array to find the cell with specified key or first null (free)
     * cell. It returns -1 if no free cells or cell with specified key was found,
     * or index of cell in another case.
     *
     * @param key the key to find in array
     * @return  -1 or index
     */
    private int getFreeOrSameKeyPosition(int key) {
        int indexHashCode = Math.abs(key);

        for(int i = 0;i < elements.length;i++) {
            //use double hashing to prevent clustering
            int hashIndex = (indexHashCode + i*(SECOND_HASH_NUMBER -
                    (indexHashCode % SECOND_HASH_NUMBER)))
                    % elements.length;

            if(elements[hashIndex] == null || elements[hashIndex].getKey() == key) {
                return hashIndex;
            }
        }

        return -1;
    }

    private long doPut(int key, long value) {
        int index = getFreeOrSameKeyPosition(key);

        if(index == -1) {
            //no free space
            throw new IllegalStateException("No free space!");
        }

        if(elements[index] == null) {
            //simple free cell - add new element
            elements[index] = new Entry(key, value);
            size++;
            return 0;
        } else {
            long oldValue = elements[index].getValue();
            elements[index].setValue(value);
            return oldValue;
        }
    }

    /**
     * We use design by contract - at least sometimes.
     */
    private void checkInvariant() {
       assert (elements != null  && size >= 0);
    }

    /**
     * Removes the element with specified keys from table.
     *
     * @param key
     * @return old value if was present or null
     */
    public long remove(int key) {
        //we need to find required element
        int index = getFreeOrSameKeyPosition(key);

        final long oldValue;
        if(index == -1) {
            oldValue = 0;
        }else {
            oldValue = (elements[index] != null) ? elements[index].getValue() : 0;
            if(elements[index] != null) {
                size--;
            }
            elements[index] = null;
        }

        checkInvariant();
        return oldValue;
    }

    /**
     *
     * @param m strictly not null!
     */
    public void putAll(Map<? extends Integer, ? extends Long> m) {
        for(Map.Entry<? extends Integer, ? extends Long> entry : m.entrySet()) {
            this.doPut(entry.getKey(), entry.getValue());
        }

        checkInvariant();
    }


    /**
     * Removes all entries from map.
     */
    public void clear() {
        size = 0;
        elements = new Entry[DEFAULT_SIZE];

        checkInvariant();
    }
}
