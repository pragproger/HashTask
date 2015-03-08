package org.abazilev.task;

import java.util.*;


/**
 * @author  abazilev
 *
 * Class is not thread safe!!!!!
 *
 * Null keys are not possible! Null values are possible.
 *
 */
public final class OpenAddressHashMap  {

    private static class Element {
        private Long value;
        private final Integer key;

        private Element next;
        private Element prev;

        private Element(Integer key, Long value, Element next, Element prev) {
            this.key = key;
            this.value = value;
            this.next = next;
            this.prev = prev;
        }

        private Long getValue() {
            return value;
        }

        private Integer getKey() {
            return key;
        }

        private Element getNext() {
            return next;
        }

        private Element getPrev() {
            return prev;
        }

        private void setNext(Element next) {
            this.next = next;
        }

        private void setValue(Long value) {
            this.value = value;
        }

        private void setPrev(Element prev) {
            this.prev = prev;
        }
    }

    /**
     * Stupid Java doesn't allow to just create a tuple.
     */
    private static class ElementPlusPosition {
        final Element element;
        final int position;

        private ElementPlusPosition(Element element, int position) {
            this.element = element;
            this.position = position;

            //contract of our tuple
            assert((element == null && position == -1) || (element != null && position >= 0));
        }
    }

    private final int DEFAULT_SIZE = 10;
    private final int DEFAULT_SIZE_MULTIPLIER = 2;

    private Element [] elements;
    private int size;
    private Element root;

    //assertions will be used to track invariants

    public OpenAddressHashMap() {
        elements = new Element [DEFAULT_SIZE];
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

    /**
     *
     * @param key is not null!
     * @return  true if key is contained as key of map or false in another case
     */
    public boolean containsKey(Integer key) {
        return getElementByKey(key) != null;
    }

    private ElementPlusPosition getElementByKey(Integer key) {
        for(int index = getIndex(key); index < elements.length &&
                cursorInCollisionedBuckets(key, index); index++) {
            if (elements[index].getKey().equals(key)) {
                return new ElementPlusPosition(elements[index], index);
            }
        }

        return null;
    }

    private boolean cursorInCollisionedBuckets(Integer key, int index) {
        return elements[index] != null &&
                getNormalizedHashcode(elements[index].getKey().hashCode())
                        == getNormalizedHashcode(key.hashCode());
    }

    /**
     *
     * @param value any
     * @return true if some entry has the specified value
     */
    public boolean containsValue(Object value) {
        //we should iterate across our internal list
        for(Element pointer = root;pointer != null; pointer = pointer.getNext()) {
            if((pointer.getValue() == null && value == null) ||
                    (value != null && value.equals(pointer.getValue())) ) {
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
    public Long get(Integer key) {
        ElementPlusPosition elementPlusPosition = getElementByKey(key);

        return (elementPlusPosition != null) ? elementPlusPosition.element.getValue() : null;
    }

    /**
     *
     * @param key cannot be null!
     * @param value
     * @return
     */
    public Long put(Integer key, Long value) {
        final Long result = doPut(key, value);

        checkInvariant();
        return result;
    }

    private Long doPut(Integer key, Long value) {
        Element old = null;
        final Long result;

        int i = getIndex(key);

        for(; (i < elements.length) && ((old == null) &&
            cursorInCollisionedBuckets(key, i)) ;i++) {
            if(elements[i].getKey().equals(key)) {
                old = elements[i];
            }
        }

        if (old != null) {
            result = old.getValue();
            old.setValue(value);
        }else if(i < elements.length && elements[i] == null) {
            //simple case - cell is free for new element, we can just insert new element
            insertAbsentElementToFreeCell(key, value, i);
            //no existing element before, so return null
            result = null;
        }else {
            recreate();
            result = doPut(key, value);
        }
        return result;
    }

    private void checkInvariant() {
       assert ((elements != null) && ((size > 0 && root != null) ||
               (size == 0 && root == null)) && (size >= 0));
    }

    private void insertToFreeCell(Integer key, Long value, int newIndex, Element previous) {
        //we insert element to both array and internal linked list
        Element newElement = new Element(key, value,
                previous.getNext(), previous);
        elements[newIndex] = newElement; //our new added el
        previous.setNext(newElement);
    }

    private void insertAbsentElementToFreeCell(Integer key, Long value, int index) {
        Element previous = getPreviousListedElement(index);

        if(previous != null) {
            //insert new node into linked list
            insertToFreeCell(key, value, index, previous);
        }else {
            //the inserted element is first
            root = new Element(key, value, null, null);
            elements[index] = root;
        }

        size++;
    }

    private Element getPreviousListedElement(int index) {
        for(int k = index; k >= 0; k--){
            if(elements[k] != null) {
                return elements[k];
            }

        }
        return null;
    }

    private int getIndex(Integer key) {
        return getNormalizedHashcode(key) % elements.length;
    }

    private int getNormalizedHashcode(Integer key) {
        //we use 2 multiplier here because we need to track the situation
        //when key -2 is inserted after key 1 and 2 - we prevent infinite recursion
        return Math.abs(key.hashCode() * 2);
    }

    /**
     *
     * @param key is not null!
     * @return old value if was present or null
     */
    public Long remove(Integer key) {
        //we need to find required element
        ElementPlusPosition elementPlusPosition = getElementByKey(key);
        Element existed = (elementPlusPosition != null) ? elementPlusPosition.element : null;

        if(existed != null) {
            //just as the contract of el plus position, indexFound be correct in this case
            elements[elementPlusPosition.position] = null;

            if(existed.getPrev() != null) {
                existed.getPrev().setNext(existed.getNext());
            }

            if(existed.getNext() != null) {
                existed.getNext().setPrev(existed.getPrev());
            }

            size--;
        }

        checkInvariant();
        return (existed != null) ? existed.getValue() : null;
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

    private void recreate() {
        int futureSize = elements.length * DEFAULT_SIZE_MULTIPLIER;
        elements = new Element[futureSize];
        size = 0;

        Element current = root;
        root = null;

        for(;current != null; current = current.getNext()){
            doPut(current.getKey(), current.getValue());
        }
    }


    /**
     * Removes all entries from map.
     */
    public void clear() {
        size = 0;
        elements = new Element[elements.length];
        root = null;

        checkInvariant();
    }
}
