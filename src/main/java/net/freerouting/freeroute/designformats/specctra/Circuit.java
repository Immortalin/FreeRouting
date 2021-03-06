/*
 *  Copyright (C) 2014  Alfons Wirtz  
 *   website www.freerouting.net
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License at <http://www.gnu.org/licenses/> 
 *   for more details.
 *
 * Circuit.java
 *
 * Created on 30. Mai 2005, 06:30
 *
 */
package net.freerouting.freeroute.designformats.specctra;

import static net.freerouting.freeroute.designformats.specctra.DsnFile.read_string_list_scope;
import static net.freerouting.freeroute.designformats.specctra.ScopeKeyword.skip_scope;
import static net.freerouting.freeroute.designformats.specctra.Structure.read_via_padstacks;

/**
 *
 * @author Alfons Wirtz
 */
class Circuit {

    /**
     * Currently only the length matching rule is read from a circuit scope. If
     * the scope does not contain a length matching rule, nulll is returned.
     */
    static ReadScopeResult read_circuit_scope(Scanner p_scanner)
            throws DsnFileException, ReadScopeException {

        Object next_token = null;
        double min_trace_length = 0;
        double max_trace_length = 0;
        java.util.Collection<String> use_via = new java.util.LinkedList<>();
        java.util.Collection<String> use_layer = new java.util.LinkedList<>();
        for (;;) {
            Object prev_token = next_token;
            try {
                next_token = p_scanner.next_token();
            } catch (java.io.IOException e) {
                throw new ReadScopeException("Circuit.read_scope: IO error scanning file", e);
            }
            if (next_token == null) {
                throw new ReadScopeException("Circuit.read_scope: unexpected end of file");
            }
            if (next_token == Keyword.CLOSED_BRACKET) {
                // end of scope
                break;
            }
            if (prev_token == Keyword.OPEN_BRACKET) {
                if (next_token == Keyword.LENGTH) {
                    LengthMatchingRule length_rule = LengthMatchingRule.read_length_scope(p_scanner);
                    min_trace_length = length_rule.min_length;
                    max_trace_length = length_rule.max_length;
                } else if (next_token == Keyword.USE_VIA) {
                    use_via.addAll(read_via_padstacks(p_scanner));
                } else if (next_token == Keyword.USE_LAYER) {
                    use_layer.addAll(read_string_list_scope(p_scanner));
                } else {
                    skip_scope(p_scanner);
                }
            }
        }
        return new ReadScopeResult(max_trace_length, min_trace_length, use_via, use_layer);
    }

    private Circuit() {
        // not called
    }

    /**
     * A max_length of -1 indicates, tha no maximum length is defined.
     */
    static class ReadScopeResult {

        final double max_length;
        final double min_length;
        final java.util.Collection<String> use_via;
        final java.util.Collection<String> use_layer;

        private ReadScopeResult(double p_max_length, double p_min_length, java.util.Collection<String> p_use_via, java.util.Collection<String> p_use_layer) {
            max_length = p_max_length;
            min_length = p_min_length;
            use_via = p_use_via;
            use_layer = p_use_layer;
        }
    }

    /**
     * A max_length of -1 indicates, tha no maximum length is defined.
     */
    private static class LengthMatchingRule {

        static LengthMatchingRule read_length_scope(Scanner p_scanner) throws ReadScopeException {
            double[] length_arr = new double[2];
            Object next_token = null;
            for (int i = 0; i < 2; ++i) {
                try {
                    next_token = p_scanner.next_token();
                } catch (java.io.IOException e) {
                    throw new ReadScopeException("Circuit.read_length_scope: IO error scanning file");
                }
                if (next_token instanceof Double) {
                    length_arr[i] = (double) next_token;
                } else if (next_token instanceof Integer) {
                    length_arr[i] = ((Number) next_token).doubleValue();
                } else {
                    throw new ReadScopeException("Circuit.read_length_scope: number expected");
                }
            }
            LengthMatchingRule result = new LengthMatchingRule(length_arr[0], length_arr[1]);
            for (;;) {
                Object prev_token = next_token;
                try {
                    next_token = p_scanner.next_token();
                } catch (java.io.IOException e) {
                    throw new ReadScopeException("Circuit.read_length_scope: IO error scanning file", e);
                }
                if (next_token == null) {
                    throw new ReadScopeException("Circuit.read_length_scope: unexpected end of file");
                }
                if (next_token == Keyword.CLOSED_BRACKET) {
                    // end of scope
                    break;
                }
                if (prev_token == Keyword.OPEN_BRACKET) {
                    skip_scope(p_scanner);
                }
            }
            return result;
        }

        final double max_length;
        final double min_length;

        LengthMatchingRule(double p_max_length, double p_min_length) {
            max_length = p_max_length;
            min_length = p_min_length;
        }
    }
}
