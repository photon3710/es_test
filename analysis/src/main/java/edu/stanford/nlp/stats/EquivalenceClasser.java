package edu.stanford.nlp.stats;

/**
 * A strategy-type interface for specifying a function from {@link Object}s
 * to their equivalence classes.
 *
 * @param <IN>  The type being classified
 * @param <OUT> The class types
 * @author Roger Levy
 * @author Sarah Spikes (sdspikes@cs.stanford.edu) (Re-templatization)
 * @see EquivalenceClassEval
 */

public interface EquivalenceClasser<IN, OUT> {
    public OUT equivalenceClass(IN o);
}
