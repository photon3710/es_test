package edu.stanford.nlp.ling.tokensregex;

import edu.stanford.nlp.util.Interval;
import edu.stanford.nlp.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Basic results for a Sequence Match
 *
 * @author Angel Chang
 */
public class BasicSequenceMatchResult<T> implements SequenceMatchResult<T> {
    SequencePattern<T> pattern;     // Pattern we matched against
    List<? extends T> elements;      // Original sequence
    MatchedGroup[] matchedGroups;    // Groups that we matched
    Object[] matchedResults;         // Additional information about matches (per element)
    Function<List<? extends T>, String> nodesToStringConverter;
    SequencePattern.VarGroupBindings varGroupBindings;
    double score = 0.0;
    int order;

    public List<? extends T> elements() {
        return elements;
    }

    public SequencePattern<T> pattern() {
        return pattern;
    }

//  public static <T> BasicSequenceMatchResult<T> toBasicSequenceMatchResult(List<? extends T> elements) {
//    BasicSequenceMatchResult<T> matchResult = new BasicSequenceMatchResult<T>();
//    matchResult.elements = elements;
//    matchResult.matchedGroups = new MatchedGroup[0];
//    return matchResult;
//  }

    public BasicSequenceMatchResult<T> toBasicSequenceMatchResult() {
        return copy();
    }

    public BasicSequenceMatchResult<T> copy() {
        BasicSequenceMatchResult res = new BasicSequenceMatchResult<T>();
        res.pattern = pattern;
        res.elements = elements;
        res.matchedGroups = new MatchedGroup[matchedGroups.length];
        res.nodesToStringConverter = nodesToStringConverter;
        res.score = score;
        res.order = order;
        res.varGroupBindings = varGroupBindings;
        for (int i = 0; i < matchedGroups.length; i++) {
            if (matchedGroups[i] != null) {
                res.matchedGroups[i] = new MatchedGroup(matchedGroups[i]);
            }
        }
        if (matchedResults != null) {
            res.matchedResults = new Object[res.matchedResults.length];
            System.arraycopy(res.matchedResults, 0, matchedResults, 0, matchedResults.length);
        }
        return res;
    }

    public Interval<Integer> getInterval() {
        return TO_INTERVAL.apply(this);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public double score() {
        return score;
    }

    public int start() {
        return start(0);
    }

    public int start(int group) {
        if (group == GROUP_BEFORE_MATCH) {
            return 0;
        } else if (group == GROUP_AFTER_MATCH) {
            return matchedGroups[0].matchEnd;
        }
        if (matchedGroups[group] != null) {
            return matchedGroups[group].matchBegin;
        } else {
            return -1;
        }
    }

    public int start(String var) {
        int g = getFirstVarGroup(var);
        if (g >= 0) {
            return start(g);
        } else {
            return -1;
        }
    }

    public int end() {
        return end(0);
    }

    public int end(int group) {
        if (group == GROUP_BEFORE_MATCH) {
            return matchedGroups[0].matchBegin;
        } else if (group == GROUP_AFTER_MATCH) {
            return elements.size();
        }
        if (matchedGroups[group] != null) {
            return matchedGroups[group].matchEnd;
        } else {
            return -1;
        }
    }

    public int end(String var) {
        int g = getFirstVarGroup(var);
        if (g >= 0) {
            return end(g);
        } else {
            return -1;
        }
    }

    public String group() {
        return group(0);
    }

    public String group(int group) {
        List<? extends T> groupTokens = groupNodes(group);
        if (nodesToStringConverter == null) {
            return (groupTokens != null) ? StringUtils.join(groupTokens, " ") : null;
        } else {
            return nodesToStringConverter.apply(groupTokens);
        }
    }

    public String group(String var) {
        int g = getFirstVarGroup(var);
        if (g >= 0) {
            return group(g);
        } else {
            return null;
        }
    }

    public List<T> groupNodes() {
        return groupNodes(0);
    }

    public List<T> groupNodes(int group) {
        if (group == GROUP_BEFORE_MATCH || group == GROUP_AFTER_MATCH) {
            // return a new list so the resulting object is serializable
            return new ArrayList<T>(elements.subList(start(group), end(group)));
        }
        if (matchedGroups[group] != null) {
            // return a new list so the resulting object is serializable
            return new ArrayList<T>(elements.subList(matchedGroups[group].matchBegin, matchedGroups[group].matchEnd));
        } else {
            return null;
        }
    }

    public List<? extends T> groupNodes(String var) {
        int g = getFirstVarGroup(var);
        if (g >= 0) {
            return groupNodes(g);
        } else {
            return null;
        }
    }

    public Object groupValue() {
        return groupValue(0);
    }

    public Object groupValue(int group) {
        if (group == GROUP_BEFORE_MATCH || group == GROUP_AFTER_MATCH) {
            // return a new list so the resulting object is serializable
            return new ArrayList<T>(elements.subList(start(group), end(group)));
        }
        if (matchedGroups[group] != null) {
            return matchedGroups[group].value;
        } else {
            return null;
        }
    }

    public Object groupValue(String var) {
        int g = getFirstVarGroup(var);
        if (g >= 0) {
            return groupValue(g);
        } else {
            return null;
        }
    }

    public MatchedGroupInfo<T> groupInfo() {
        return groupInfo(0);
    }

    public MatchedGroupInfo<T> groupInfo(int group) {
        List<? extends T> nodes = groupNodes(group);
        if (nodes != null) {
            Object value = groupValue(group);
            String text = group(group);
            List<Object> matchedResults = groupMatchResults(group);
            return new MatchedGroupInfo<T>(text, nodes, matchedResults, value);
        } else {
            return null;
        }
    }

    public MatchedGroupInfo<T> groupInfo(String var) {
        int g = getFirstVarGroup(var);
        if (g >= 0) {
            return groupInfo(g);
        } else {
            return null;
        }
    }

    public int groupCount() {
        return matchedGroups.length - 1;
    }

    public List<Object> groupMatchResults() {
        return groupMatchResults(0);
    }

    public List<Object> groupMatchResults(int group) {
        if (matchedResults == null) return null;
        if (group == GROUP_BEFORE_MATCH || group == GROUP_AFTER_MATCH) {
            return Arrays.asList(Arrays.copyOfRange(matchedResults, start(group), end(group)));
        }
        if (matchedGroups[group] != null) {
            return Arrays.asList(Arrays.copyOfRange(matchedResults, matchedGroups[group].matchBegin, matchedGroups[group].matchEnd));
        } else {
            return null;
        }
    }

    public List<Object> groupMatchResults(String var) {
        int g = getFirstVarGroup(var);
        if (g >= 0) {
            return groupMatchResults(g);
        } else {
            return null;
        }
    }

    public Object nodeMatchResult(int index) {
        if (matchedResults != null) {
            return matchedResults[index];
        } else {
            return null;
        }
    }

    public Object groupMatchResult(int group, int index) {
        if (matchedResults != null) {
            int s = start(group);
            int e = end(group);
            if (s >= 0 && e > s) {
                int d = e - s;
                if (index >= 0 && index < d) {
                    return matchedResults[s + index];
                }
            }
        }
        return null;
    }

    public Object groupMatchResult(String var, int index) {
        int g = getFirstVarGroup(var);
        if (g >= 0) {
            return groupMatchResult(g, index);
        } else {
            return null;
        }
    }

    private int getFirstVarGroup(String v) {
        // Trim the variable...
        v = v.trim();
        for (int i = 0; i < varGroupBindings.varnames.length; i++) {
            String s = varGroupBindings.varnames[i];
            if (v.equals(s)) {
                if (matchedGroups[i] != null) {
                    return i;
                }
            }
        }
        return -1;
    }

    protected static class MatchedGroup {
        int matchBegin = -1;
        int matchEnd = -1;
        Object value = null;

        protected MatchedGroup(MatchedGroup mg) {
            this.matchBegin = mg.matchBegin;
            this.matchEnd = mg.matchEnd;
            this.value = mg.value;
        }

        protected MatchedGroup(int matchBegin, int matchEnd, Object value) {
            this.matchBegin = matchBegin;
            this.matchEnd = matchEnd;
            this.value = value;
        }

        public String toString() {
            return "(" + matchBegin + "," + matchEnd + ")";
        }
    }

}
