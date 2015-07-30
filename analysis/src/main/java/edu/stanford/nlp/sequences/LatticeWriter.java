package edu.stanford.nlp.sequences;

import edu.stanford.nlp.fsm.DFSA;
import edu.stanford.nlp.util.CoreMap;

import java.io.PrintWriter;
import java.util.List;

/**
 * This interface is used for writing
 * lattices out of {@link edu.stanford.nlp.ie.AbstractSequenceClassifier}s.
 *
 * @author Michel Galley
 */

public interface LatticeWriter<IN extends CoreMap, T, S> {

    /**
     * This method prints the output lattice (typically, Viterbi search graph) of
     * the classifier to a {@link PrintWriter}.
     */
    public void printLattice(DFSA<T, S> tagLattice, List<IN> doc, PrintWriter out);

}
