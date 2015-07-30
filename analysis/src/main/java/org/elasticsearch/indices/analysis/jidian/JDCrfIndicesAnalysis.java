/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.indices.analysis.jidian;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.jidian.JDCrfChineseAnalyzer;
import org.apache.lucene.analysis.jidian.JDCrfChineseTokenizer;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.lucene.Lucene;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.AnalyzerScope;
import org.elasticsearch.index.analysis.PreBuiltAnalyzerProviderFactory;
import org.elasticsearch.index.analysis.PreBuiltTokenizerFactoryFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;

import java.io.Reader;

/**
 * Registers indices level analysis components so, if not explicitly configured, will be shared
 * among all indices.
 */
public class JDCrfIndicesAnalysis extends AbstractComponent {

    @Inject
    public JDCrfIndicesAnalysis(Settings settings, IndicesAnalysisService indicesAnalysisService) {
        super(settings);

        // Register jdcrfcn analyzer
        indicesAnalysisService.analyzerProviderFactories().put("jdcrfcn",
                new PreBuiltAnalyzerProviderFactory("jdcrfcn", AnalyzerScope.INDICES, new JDCrfChineseAnalyzer(Lucene.ANALYZER_VERSION)));


        // Register smartcn_sentence tokenizer
        indicesAnalysisService.tokenizerFactories().put("jdcrfcn_tokenizer",
                new PreBuiltTokenizerFactoryFactory(new TokenizerFactory() {
                    @Override
                    public String name() {
                        return "jdcrfcn_tokenizer";
                    }

                    @Override
                    public Tokenizer create(Reader reader) {
                        return new JDCrfChineseTokenizer(reader);
                    }
                })
        );
    }
}
