package edu.stanford.nlp.trees;

import edu.stanford.nlp.process.AbstractTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

/**
 * Wrapper for TreeReaderFactory.  Any IOException in the readTree() method
 * of the TreeReader will result in a null
 * tree returned.
 *
 * @author Roger Levy (rog@stanford.edu)
 * @author javanlp
 */
public class TreeTokenizerFactory implements TokenizerFactory<Tree> {

    /**
     * Create a TreeTokenizerFactory from a TreeReaderFactory.
     */
    public TreeTokenizerFactory(TreeReaderFactory trf) {
        this.trf = trf;
    }

    private TreeReaderFactory trf;

    /**
     * Gets a tokenizer from a reader.
     */
    public Tokenizer<Tree> getTokenizer(final Reader r) {
        return new AbstractTokenizer<Tree>() {
            TreeReader tr = trf.newTreeReader(r);

            @Override
            public Tree getNext() {
                try {
                    return tr.readTree();
                } catch (IOException e) {
                    System.err.println("Error in reading tree.");
                    return null;
                }
            }
        };
    }

    public Tokenizer<Tree> getTokenizer(final Reader r, String extraOptions) {
        // Silently ignore extra options
        return getTokenizer(r);
    }

    /**
     * Same as getTokenizer().
     */
    public Iterator<Tree> getIterator(Reader r) {
        return null;
    }

    public void setOptions(String options) {
        //Silently ignore
    }
}
