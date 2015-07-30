package edu.stanford.nlp.process;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.trees.Tree;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * An interface for segmenting strings into words
 * (in unwordsegmented languages).
 *
 * @author Galen Andrew
 */
public interface WordSegmenter extends Serializable {

    void initializeTraining(double numTrees);

    void train(Collection<Tree> trees);

    void train(Tree trees);

    void train(List<TaggedWord> sentence);

    void finishTraining();

    void loadSegmenter(String filename);

    List<HasWord> segment(String s);
}
