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

package org.elasticsearch.plugin.analysis.jidian;

import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.JDCrfAnalysisBinderProcessor;
import org.elasticsearch.indices.analysis.jidian.JDCrfIndicesAnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;

import java.util.Collection;

/**
 *
 */
public class JDCrfAnalysisPlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "analysis-jidian";
    }

    @Override
    public String description() {
        return "Jidian analysis support";
    }

    @Override
    public Collection<Class<? extends Module>> modules() {
        return ImmutableList.<Class<? extends Module>>of(JDCrfIndicesAnalysisModule.class);
    }

    public void onModule(AnalysisModule module) {
        module.addProcessor(new JDCrfAnalysisBinderProcessor());
    }
}
