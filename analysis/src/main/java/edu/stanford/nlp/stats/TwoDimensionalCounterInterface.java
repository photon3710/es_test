package edu.stanford.nlp.stats;

import java.util.Set;

/**
 * Interface representing a mapping between pairs of typed objects and double
 * values.
 *
 * @author Angel Chang
 */
public interface TwoDimensionalCounterInterface<K1, K2> {

    /**
     * @return total number of entries (key pairs)
     */
    public int size();

    public boolean containsKey(K1 o1, K2 o2);

    /**
     */
    public void incrementCount(K1 o1, K2 o2);

    /**
     */
    public void incrementCount(K1 o1, K2 o2, double count);

    /**
     */
    public void setCount(K1 o1, K2 o2, double count);

    public double remove(K1 o1, K2 o2);

    /**
     */
    public double getCount(K1 o1, K2 o2);

    public double totalCount();

    /**
     */
    public double totalCount(K1 k1);

    public Set<K1> firstKeySet();


    public boolean isEmpty();

    public void remove(K1 key);


    /** Counter based operations */

    /**
     * @return the inner Counter associated with key o
     */
    public Counter<K2> getCounter(K1 o);
}
