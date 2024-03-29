package edu.stanford.nlp.process;

import edu.stanford.nlp.util.CoreMap;

/**
 * To make tokens like CoreMap or CoreLabel. An alternative to LexedTokenFactory
 * since this one has option to make tokens differently, which would have been
 * an overhead for LexedTokenFactory
 *
 * @param <IN>
 * @author Sonal Gupta
 */
public interface CoreTokenFactory<IN extends CoreMap> {
    public IN makeToken();

    public IN makeToken(String[] keys, String[] values);

    public IN makeToken(IN tokenToBeCopied);

}
