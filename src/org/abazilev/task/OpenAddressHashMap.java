package org.abazilev.task;

import java.util.*;


/**
 * @author  abazilev
 *
 * Class is not thread safe!!!!!
 *
 * Null keys are not possible!
 *
 * @param <K>
 * @param <V>
 */
public class OpenAddressHashMap<K, V> implements Map<K, V> {

    private static class Element<V> {
        private final V value;

        private Element(V value) {
            this.value = value;
        }

        private V getValue() {
            return value;
        }
    }

    private final int DEFAULT_SIZE = 100;

    private Element<V> [] elements;
    private int size;

    public OpenAddressHashMap() {
        elements = new Element [DEFAULT_SIZE];
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
        Element<V> element = elements[key.hashCode() % elements.length];
        return element != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public V get(Object key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public V put(K key, V value) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public V remove(Object key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void clear() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<K> keySet() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<V> values() {
        List<V> values = new LinkedList<>();

        for(Element<V> element : elements) {
            if(element != null)  {
                values.add(element.getValue());
            }
        }
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
