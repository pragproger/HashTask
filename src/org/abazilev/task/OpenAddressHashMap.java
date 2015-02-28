package org.abazilev.task;

import java.util.*;


/**
 * @author  abazilev
 *
 * Class is not thread safe!!!!!
 *
 * Null keys are not possible! Null values are possible.
 *
 * @param <K>
 * @param <V>
 */
public final class OpenAddressHashMap<K, V> implements Map<K, V> {

    private static class Element<K, V> {
        private V value;
        private final K key;

        private Element<K, V> next;
        private Element<K, V> prev;

        private Element(K key, V value, Element<K, V> next, Element<K, V> prev) {
            this.key = key;
            this.value = value;
            this.next = next;
            this.prev = prev;
        }

        private V getValue() {
            return value;
        }

        private K getKey() {
            return key;
        }

        private Element<K, V> getNext() {
            return next;
        }

        private Element<K, V> getPrev() {
            return prev;
        }

        private void setNext(Element<K, V> next) {
            this.next = next;
        }

        private void setValue(V value) {
            this.value = value;
        }

        private void setPrev(Element<K, V> prev) {
            this.prev = prev;
        }
    }

    private final int DEFAULT_SIZE = 100;
    private final int DEFAULT_SIZE_MULTIPLIER = 2;

    private Element<K, V> [] elements;
    private int size;
    private Element<K, V> root;
    //(size > 0 && root != null) - is a part of invariant, howevevr invariant
    //is much bigger
    //assertions will be used to track invariants

    private final int initialSize;
    private final int sizeMultiplier;

    public OpenAddressHashMap() {
        initialSize = DEFAULT_SIZE;
        sizeMultiplier = DEFAULT_SIZE_MULTIPLIER;

        elements = new Element [initialSize];
        checkInvariant();
    }

    public OpenAddressHashMap(int initialSize, int sizeMultiplier) {
        if(initialSize <= 0) {
            throw new IllegalArgumentException("Size has to be greater than 0!");
        }
        if(sizeMultiplier <= 1) {
            throw new IllegalArgumentException("Multiplier has to be greater than 1!");
        }

        this.initialSize = initialSize;
        this.sizeMultiplier = sizeMultiplier;

        elements = new Element [initialSize];
        checkInvariant();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if(key == null) {
            throw new NullPointerException("Key cannot be null!");
        }

        Element<K, V> element = elements[getIndex((K) key)];
        return element != null;
    }

    @Override
    public boolean containsValue(Object value) {
        //we should iterate across our internal list
        Element<K,V> pointer = root;

        for(;pointer != null;pointer = pointer.getNext()) {
            if((pointer.getValue() == null && value == null) ||
                    (value != null && value.equals(pointer.getValue())) ) {
                return true;
            }
        }

        return false;
    }

    @Override
    public V get(Object key) {
        if(key == null) {
            throw new NullPointerException("key cannot be null!");
        }

        int startIndex = getIndex((K) key);
        Element<K,V> current = elements[startIndex];

        if(current != null) {
            if(current.getKey().equals(key)) {
                return current.getValue();
            }else {
                for(int index = startIndex;index < elements.length;index++) {
                   if(elements[index] != null && elements[index].getKey().equals(key)) {
                       return elements[index].getValue();
                   }
                }
            }
        }

        return null;
    }

    @Override
    public V put(K key, V value) {
        if(key == null) {
            throw new NullPointerException("Key cannot be null!");
        }

        int index = getIndex(key);
        if(elements[index] == null) {
            //simple case - cell is free for new element, we can just insert new element
            insertAbsentElementToFreeCell(key, value, index);
            //no existing element before, so return null
            checkInvariant();
            return null;
        }else {
            //elements exist
            Element<K,V> existed = elements[index];

            if(existed.getKey().equals(key)) {
                //just change the value
                V oldValue = existed.getValue();
                existed.setValue(value);

                checkInvariant();
                return oldValue;
            } else {
                //we have hash collision - so just use the open addressing to find the free bucket
                int newIndex = -1;
                V oldValue = null;

                //TODO review this cycle
                for(int k = index;k < elements.length && newIndex < 0;k++) {
                   if(elements[k] == null) {
                       newIndex = k;
                   }else {
                       if(elements[k].getKey().equals(key)) {
                           oldValue = elements[k].getValue();
                       }
                   }
                }

                if(newIndex > 0) {
                    //we have found free cell (or free bucket)
                    //we know that previous will be not null by the contract of hash map internal
                    //implementation
                    insertToFreeCell(key, value, newIndex, elements[newIndex - 1]);

                    size++;

                    checkInvariant();
                    return oldValue;
                } else {
                    //there are no free space
                    recreate();
                    return put(key, value);
                }
            }
        }
    }

    private void checkInvariant() {
       assert ((elements != null) && ((size > 0 && root != null) ||
               (size == 0 && root == null)) && (size >= 0));
    }

    private void insertToFreeCell(K key, V value, int newIndex, Element<K, V> previous) {
        Element<K,V> tempNext = previous.getNext();
        Element<K,V> inserted = new Element<>(key, value, tempNext, previous);

        elements[newIndex] = inserted;
        previous.setNext(inserted);
    }

    private void insertAbsentElementToFreeCell(K key, V value, int index) {
        Element<K, V> previous = getPreviousListedElement(index);

        if(previous != null) {
            //insert new node into linked list
            insertToFreeCell(key, value, index, previous);
        }else {
            //the inserted element is first
            Element<K,V> inserted = new Element<>(key, value, null, null);
            elements[index] = inserted;

            root = inserted;
        }

        size++;
    }

    private Element<K, V> getPreviousListedElement(int index) {
        Element<K,V> previous = null;

        for(int k = index;k >= 0 && previous == null;k--){
            if(elements[k] != null) {
                previous = elements[k];
            }

        }
        return previous;
    }

    private int getIndex(K key) {
        return key.hashCode() % elements.length;
    }

    @Override
    public V remove(Object key) {
        if(key == null) {
            throw new NullPointerException("Key cannot be null!");
        }

        Element<K,V> existed = null;
        int indexFound = -1;

        //we need to find required element
        for(int index = getIndex((K) key); (index < elements.length) &&
                (elements[index] != null && elements[index].getKey().hashCode() == key.hashCode())
                && (existed == null); index++) {
            if(elements[index].getKey().equals(key)) {
                existed = elements[index];
                indexFound = index;
            }
        }

        if(existed != null) {
            elements[indexFound] = null;

            if(existed.getPrev() != null) {
                existed.getPrev().setNext(existed.getNext());
            }

            if(existed.getNext() != null) {
                existed.getNext().setPrev(existed.getPrev());
            }

            size--;

            checkInvariant();
            return existed.getValue();
        }

        checkInvariant();
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if(m == null) {
            throw new NullPointerException("Map m cannot be null!");
        }

        for(Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }

        checkInvariant();
    }

    private void recreate() {
        int futureSize = elements.length * sizeMultiplier;
        elements = new Element[futureSize];
        size = 0;

        Element<K,V> current = root;
        root = null;

        for(;current != null; current = current.getNext()){
            put(current.getKey(), current.getValue());
        }

        checkInvariant();
    }

    @Override
    public void clear() {
        size = 0;
        elements = new Element[elements.length];
        root = null;

        checkInvariant();
    }

    @Override
    public Set<K> keySet() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<V> values() {

        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
