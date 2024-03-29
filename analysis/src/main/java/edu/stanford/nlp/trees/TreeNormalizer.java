package edu.stanford.nlp.trees;

import java.io.Serializable;

/**
 * A class for tree normalization.  The default one does no normalization.
 * Other tree normalizers will change various node labels, or perhaps the
 * whole tree geometry (by doing such things as deleting functional tags or
 * empty elements).  Another operation that a <code>TreeNormalizer</code>
 * may wish to perform is interning the <code>String</code>s passed to
 * it.  Can be reused as a Singleton.  Designed to be extended.
 * <p>
 * The <code>TreeNormalizer</code> methods are in two groups.
 * The contract for this class is that first normalizeTerminal or
 * normalizeNonterminal will be called on each <code>String</code> that will
 * be put into a <code>Tree</code>, when they are read from files or
 * otherwise created.  Then <code>normalizeWholeTree</code> will
 * be called on the <code>Tree</code>.  It normally walks the
 * <code>Tree</code> making whatever modifications it wishes to. A
 * <code>TreeNormalizer</code> need not make a deep copy of a
 * <code>Tree</code>.  It is assumed to be able to work destructively,
 * because afterwards we will only use the normalized <code>Tree</code>.
 * <p>
 * <i>Implementation note:</i> This is a very old legacy class used in conjunction
 * with PennTreeReader.  It seems now that it would be better to move the
 * String normalization into the tokenizer, and then we are just left with a
 * (possibly destructive) TreeTransformer.
 *
 * @author Christopher Manning
 */
public class TreeNormalizer implements Serializable {

    public TreeNormalizer() {
    }

    /**
     * Normalizes a leaf contents (and maybe intern it).
     *
     * @param leaf The String that decorates the leaf
     * @return The normalized form of this leaf String
     */
    public String normalizeTerminal(String leaf) {
        return leaf;
    }

    /**
     * Normalizes a nonterminal contents (and maybe intern it).
     *
     * @param category The String that decorates this nonterminal node
     * @return The normalized form of this nonterminal String
     */
    public String normalizeNonterminal(String category) {
        return category;
    }

    /**
     * Normalize a whole tree -- this method assumes that the argument
     * that it is passed is the root of a complete <code>Tree</code>.
     * It is normally implemented as a Tree-walking routine. <p>
     * This method may return <code>null</code>. This is interpreted to
     * mean that this is a tree that should not be included in further
     * processing.  PennTreeReader recognizes this return value, and
     * asks for another Tree from the input Reader.
     *
     * @param tree The tree to be normalized
     * @param tf   the TreeFactory to create new nodes (if needed)
     * @return Tree the normalized tree
     */
    public Tree normalizeWholeTree(Tree tree, TreeFactory tf) {
        return tree;
    }

    private static final long serialVersionUID = 1540681875853883387L;

}
