package org.apache.lucene.analysis.jidian;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.wordseg.ChineseStringUtils;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.SegmentingTokenizerBase;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;
import java.io.Reader;
import java.text.BreakIterator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

/**
 * Tokenizer for CJK or mixed Chinese-English text.
 * <p>
 * The analyzer uses probabilistic knowledge to find the optimal word segmentation for Simplified Chinese text.
 * The text is first broken into sentences, then each sentence is segmented into words.
 */
public class JDCrfChineseTokenizer extends SegmentingTokenizerBase {
    /**
     * used for breaking the text into sentences
     */
    private static final BreakIterator sentenceProto = BreakIterator.getSentenceInstance(Locale.ROOT);

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

    static CRFClassifier<CoreLabel> segmenter = null;

    static {
        Properties props = new Properties();
        props.setProperty("sighanCorporaDict", "data");
        // props.setProperty("NormalizationTable", "data/norm.simp.utf8");
        // props.setProperty("normTableEncoding", "UTF-8");
        // below is needed because CTBSegDocumentIteratorFactory accesses it
        props.setProperty("serDictionary", "data/dict-chris6.ser.gz");
        props.setProperty("inputEncoding", "UTF-8");
        props.setProperty("sighanPostProcessing", "false");

        segmenter = new CRFClassifier<CoreLabel>(props);
        segmenter.loadClassifierNoExceptions("data/ctb.gz", props);
    }

    //private Iterator<String> tokens;
    private Iterator<ChineseStringUtils.SegToken> tokens;
    int sentenceStart = -1;
    // private Iterator<Triple<String, Integer, Integer>> tokens;
    // private String sentence;

    /**
     * Creates a new HMMChineseTokenizer
     */
    public JDCrfChineseTokenizer(Reader input) {
        this(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY, input);
    }

    /**
     * Creates a new JDCrfChineseTokenizer, supplying the AttributeFactory
     */
    public JDCrfChineseTokenizer(AttributeFactory factory, Reader input) {
        super(factory, input, (BreakIterator) sentenceProto.clone());
    }

    @Override
    protected void setNextSentence(int sentenceStart, int sentenceEnd) {
        this.sentenceStart = sentenceStart;
        String sentence = new String(buffer, sentenceStart, sentenceEnd - sentenceStart);
        tokens = segmenter.segmentString(sentence).iterator();
    }

    @Override
    protected boolean incrementWord() {
        if (tokens == null || !tokens.hasNext()) {
            return false;
        } else {
            ChineseStringUtils.SegToken token = tokens.next();
            clearAttributes();
            termAtt.append(token.term, 0, token.term.length());
            int start = token.getCharOffsetStart() + sentenceStart;
            int end = token.getCharOffsetEnd() + sentenceStart;
            offsetAtt.setOffset(correctOffset(start), correctOffset(end));
            typeAtt.setType("word");
            return true;
        }
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        tokens = null;
    }
}
