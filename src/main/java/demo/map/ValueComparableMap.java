package demo.map;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.base.Functions;
import com.google.common.collect.Ordering;

public class ValueComparableMap<K extends Comparable<K>,V> extends TreeMap<K,V> {
    //A map for doing lookups on the keys for comparison so we don't get infinite loops
    private final Map<K, V> valueMap;

    public ValueComparableMap(final Ordering<? super V> partialValueOrdering) {
        this(partialValueOrdering, new HashMap<K,V>());
    }

    private ValueComparableMap(Ordering<? super V> partialValueOrdering,
            HashMap<K, V> valueMap) {
        super(partialValueOrdering //Apply the value ordering
                .onResultOf(Functions.forMap(valueMap)) //On the result of getting the value for the key from the map
                .compound(Ordering.natural())); //as well as ensuring that the keys don't get clobbered
        this.valueMap = valueMap;
    }

    public V put(K k, V v) {
        if (valueMap.containsKey(k)){
            //remove the key in the sorted set before adding the key again
            super.remove(k);
        }
        valueMap.put(k,v); //To get "real" unsorted values for the comparator
        return super.put(k, v); //Put it in value order
    }
    
    public void putAll(Map<? extends K, ? extends V> map) {
        for(java.util.Map.Entry<? extends K, ? extends V> item:map.entrySet()) {
        	this.put(item.getKey(), item.getValue());
        }
    }

}
