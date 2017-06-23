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
 * WindowLengthViolations.java
 *
 * Created on 1. Juni 2005, 06:52
 *
 */
package net.freerouting.freeroute;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import net.freerouting.freeroute.interactive.RatsNest;
import net.freerouting.freeroute.rules.Net;
import net.freerouting.freeroute.rules.NetClass;
import net.freerouting.freeroute.rules.Nets;

/**
 *
 * @author Alfons Wirtz
 */
@SuppressWarnings("serial")
public final class WindowLengthViolations extends WindowObjectListWithFilter {

    private final java.util.ResourceBundle resources;

    /**
     * Creates a new instance of WindowLengthViolations
     */
    WindowLengthViolations(BoardFrame p_board_frame) {
        super(p_board_frame);
        this.resources = java.util.ResourceBundle.getBundle("net.freerouting.freeroute.resources.WindowLengthViolations", Locale.getDefault());
        this.setTitle(resources.getString("title"));
        this.list_empty_message.setText(resources.getString("list_empty"));
        p_board_frame.set_context_sensitive_help(this, "WindowObjectList_LengthViolations");
    }

    @Override
    void fill_list() {
        RatsNest ratsnest = this.board_frame.board_panel.board_handling.get_ratsnest();
        Nets net_list = this.board_frame.board_panel.board_handling.get_routing_board().rules.nets;
        java.util.SortedSet<LengthViolation> length_violations = new java.util.TreeSet<>();
        for (int net_index = 1; net_index <= net_list.max_net_no(); ++net_index) {
            double curr_violation_length = ratsnest.get_length_violation(net_index);
            if (curr_violation_length != 0) {
                LengthViolation curr_length_violation = new LengthViolation(net_list.get(net_index), curr_violation_length);
                length_violations.add(curr_length_violation);
            }
        }

        for (LengthViolation curr_violation : length_violations) {
            this.add_to_list(curr_violation);
        }
        this.list.setVisibleRowCount(Math.min(length_violations.size(), DEFAULT_TABLE_SIZE));
    }

    @Override
    void select_instances() {
        List<?> selected_violations = list.getSelectedValuesList();
        if (selected_violations.isEmpty()) {
            return;
        }
        java.util.Set<net.freerouting.freeroute.board.Item> selected_items = new java.util.TreeSet<>();
        for (Iterator<?> it = selected_violations.iterator(); it.hasNext();) {
            LengthViolation curr_violation = (LengthViolation) it.next();
            selected_items.addAll(curr_violation.net.get_items());
        }
        net.freerouting.freeroute.interactive.BoardHandling board_handling = board_frame.board_panel.board_handling;
        board_handling.select_items(selected_items);
        board_handling.zoom_selection();
    }

    private class LengthViolation implements Comparable<LengthViolation> {

        final Net net;
        final double violation_length;

        LengthViolation(Net p_net, double p_violation_length) {
            net = p_net;
            violation_length = p_violation_length;
        }

        @Override
        public int compareTo(LengthViolation p_other) {
            return this.net.name.compareToIgnoreCase(p_other.net.name);
        }

        @Override
        public String toString() {
            net.freerouting.freeroute.board.CoordinateTransform coordinate_transform = board_frame.board_panel.board_handling.coordinate_transform;
            NetClass net_class = this.net.get_class();
            Float allowed_length;
            String allowed_string;
            if (violation_length > 0) {
                allowed_length = (float) coordinate_transform.board_to_user(net_class.get_maximum_trace_length());
                allowed_string = " " + resources.getString("maximum_allowed") + " ";
            } else {
                allowed_length = (float) coordinate_transform.board_to_user(net_class.get_minimum_trace_length());
                allowed_string = " " + resources.getString("minimum_allowed") + " ";
            }
            Float length = (float) coordinate_transform.board_to_user(this.net.get_trace_length());
            String result = resources.getString("net") + " " + this.net.name + resources.getString("trace_length")
                    + " " + length.toString() + allowed_string + allowed_length;
            return result;
        }

    }
}
