package edu.stanford.nlp.trees.tregex.tsurgeon;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;

/**
 * @author Roger Levy (rog@nlp.stanford.edu)
 */
class CoindexNodes extends TsurgeonPattern {

    private static final String coindexationIntroductionString = "-";

    public CoindexNodes(TsurgeonPattern[] children) {
        super("coindex", children);
    }

    @Override
    public Tree evaluate(Tree t, TregexMatcher m) {
        int newIndex = root.coindexer.generateIndex();
        for (TsurgeonPattern child : children) {
            Tree node = child.evaluate(t, m);
            node.label().setValue(node.label().value() + coindexationIntroductionString + newIndex);
        }
        return t;
    }

}
