// CRFClassifier -- a probabilistic (CRF) sequence model, mainly used for NER.
// Copyright (c) 2002-2008 The Board of Trustees of
// The Leland Stanford Junior University. All Rights Reserved.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
// For more information, bug reports, fixes, contact:
//    Christopher Manning
//    Dept of Computer Science, Gates 1A
//    Stanford CA 94305-9010
//    USA
//    Support/Questions: java-nlp-user@lists.stanford.edu
//    Licensing: java-nlp-support@lists.stanford.edu

package edu.stanford.nlp.ie.crf;

import edu.stanford.nlp.math.ArrayMath;
import edu.stanford.nlp.optimization.Evaluator;
import edu.stanford.nlp.optimization.FloatFunction;
import edu.stanford.nlp.optimization.QNMinimizer;
import edu.stanford.nlp.optimization.ResultStoringFloatMonitor;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.ConvertByteArray;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

/**
 * Subclass of CRFClassifier that performs dropout feature-noisying training
 *
 * @author Mengqiu Wang
 */
public class CRFClassifierFloat<IN extends CoreMap> extends CRFClassifier<IN> {

    protected CRFClassifierFloat() {
        super(new SeqClassifierFlags());
    }

    public CRFClassifierFloat(Properties props) {
        super(props);
    }

    public CRFClassifierFloat(SeqClassifierFlags flags) {
        super(flags);
    }

    @Override
    protected double[] trainWeights(int[][][][] data, int[][] labels, Evaluator[] evaluators, int pruneFeatureItr, double[][][][] featureVals) {
        CRFLogConditionalObjectiveFloatFunction func = new CRFLogConditionalObjectiveFloatFunction(data, labels,
                featureIndex, windowSize, classIndex, labelIndices, map, flags.backgroundSymbol, flags.sigma);
        cliquePotentialFunctionHelper = func;

        QNMinimizer minimizer;
        if (flags.interimOutputFreq != 0) {
            FloatFunction monitor = new ResultStoringFloatMonitor(flags.interimOutputFreq, flags.serializeTo);
            minimizer = new QNMinimizer(monitor);
        } else {
            minimizer = new QNMinimizer();
        }

        if (pruneFeatureItr == 0) {
            minimizer.setM(flags.QNsize);
        } else {
            minimizer.setM(flags.QNsize2);
        }

        float[] initialWeights;
        if (flags.initialWeights == null) {
            initialWeights = func.initial();
        } else {
            try {
                System.err.println("Reading initial weights from file " + flags.initialWeights);
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(
                        flags.initialWeights))));
                initialWeights = ConvertByteArray.readFloatArr(dis);
            } catch (IOException e) {
                throw new RuntimeException("Could not read from float initial weight file " + flags.initialWeights);
            }
        }
        System.err.println("numWeights: " + initialWeights.length);
        float[] weights = minimizer.minimize(func, (float) flags.tolerance, initialWeights);
        return ArrayMath.floatArrayToDoubleArray(weights);
    }
} // end class CRFClassifierFloat
