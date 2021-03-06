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
 * NetsWindow.java
 *
 * Created on 24. Maerz 2005, 07:41
 */
package net.freerouting.freeroute;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import net.freerouting.freeroute.rules.Net;
import net.freerouting.freeroute.rules.Nets;

/**
 *
 * @author Alfons Wirtz
 */
@SuppressWarnings("serial")
public final class WindowNets extends WindowObjectListWithFilter {

    private final java.util.ResourceBundle resources;

    /**
     * Creates a new instance of NetsWindow
     */
    private WindowNets(BoardFrame p_board_frame) {
        super(p_board_frame);
        this.resources = java.util.ResourceBundle.getBundle("net.freerouting.freeroute.resources.WindowNets", Locale.getDefault());
        this.setTitle(resources.getString("title"));

        javax.swing.JPanel curr_button_panel = new javax.swing.JPanel();
        this.south_panel.add(curr_button_panel, java.awt.BorderLayout.NORTH);

        final javax.swing.JButton assign_class_button = new javax.swing.JButton(resources.getString("assign_class"));
        curr_button_panel.add(assign_class_button);
        assign_class_button.setToolTipText(resources.getString("assign_class_tooltip"));
        assign_class_button.addActionListener(new AssignClassListener());

        final javax.swing.JButton filter_incompletes_button = new javax.swing.JButton(resources.getString("filter_incompletes"));
        curr_button_panel.add(filter_incompletes_button);
        filter_incompletes_button.setToolTipText(resources.getString("filter_incompletes_tooltip"));
        filter_incompletes_button.addActionListener(new FilterIncompletesListener());
    }

    static WindowNets getInstance(BoardFrame p_board_frame) {
        WindowNets window = new WindowNets(p_board_frame);
        p_board_frame.set_context_sensitive_help(window, "WindowObjectList_Nets");
        return window;
    }

    /**
     * Fills the list with the nets in the net list.
     */
    @Override
    void fill_list() {
        Nets nets = this.board_frame.board_panel.board_handling.get_routing_board().rules.nets;
        Net[] sorted_arr = new Net[nets.max_net_no()];
        for (int i = 0; i < sorted_arr.length; ++i) {
            sorted_arr[i] = nets.get(i + 1);
        }
        java.util.Arrays.parallelSort(sorted_arr);
        for (int i = 0; i < sorted_arr.length; ++i) {
            this.add_to_list(sorted_arr[i]);
        }
        this.list.setVisibleRowCount(Math.min(sorted_arr.length, DEFAULT_TABLE_SIZE));
    }

    @Override
    void select_instances() {
        List<?> selected_nets = list.getSelectedValuesList();
        if (selected_nets.isEmpty()) {
            return;
        }
        int[] selected_net_numbers = new int[selected_nets.size()];
        for (int i = 0; i < selected_nets.size(); ++i) {
            selected_net_numbers[i] = ((Net) selected_nets.get(i)).net_number;
        }
        net.freerouting.freeroute.board.RoutingBoard routing_board = board_frame.board_panel.board_handling.get_routing_board();
        java.util.Set<net.freerouting.freeroute.board.Item> selected_items = new java.util.TreeSet<>();
        java.util.Collection<net.freerouting.freeroute.board.Item> board_items = routing_board.get_items();
        for (net.freerouting.freeroute.board.Item curr_item : board_items) {
            boolean item_matches = false;
            for (int curr_net_no : selected_net_numbers) {
                if (curr_item.contains_net(curr_net_no)) {
                    item_matches = true;
                    break;
                }
            }
            if (item_matches) {
                selected_items.add(curr_item);
            }
        }
        board_frame.board_panel.board_handling.select_items(selected_items);
        board_frame.zoom_all();
        board_frame.board_panel.board_handling.zoom_selection();
    }

    private class AssignClassListener implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent p_evt) {
            List<?> selected_nets = list.getSelectedValuesList();
            if (selected_nets.isEmpty()) {
                return;
            }
            net.freerouting.freeroute.rules.NetClasses net_classes = board_frame.board_panel.board_handling.get_routing_board().rules.net_classes;
            net.freerouting.freeroute.rules.NetClass[] class_arr = new net.freerouting.freeroute.rules.NetClass[net_classes.count()];
            for (int i = 0; i < class_arr.length; ++i) {
                class_arr[i] = net_classes.get(i);
            }
            Object selected_value = javax.swing.JOptionPane.showInputDialog(null, resources.getString("message_1"),
                    resources.getString("message_2"), javax.swing.JOptionPane.INFORMATION_MESSAGE,
                    null, class_arr, class_arr[0]);
            if (!(selected_value instanceof net.freerouting.freeroute.rules.NetClass)) {
                return;
            }
            net.freerouting.freeroute.rules.NetClass selected_class = (net.freerouting.freeroute.rules.NetClass) selected_value;
            Iterator<?> it = selected_nets.iterator();
            while (it.hasNext()) {
                ((Net) it.next()).set_class(selected_class);
            }
            board_frame.refresh_windows();
        }
    }

    private class FilterIncompletesListener implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent p_evt) {
            List<?> selected_nets = list.getSelectedValuesList();
            if (selected_nets.isEmpty()) {
                return;
            }
            net.freerouting.freeroute.interactive.BoardHandling board_handling = board_frame.board_panel.board_handling;
            int max_net_no = board_handling.get_routing_board().rules.nets.max_net_no();
            for (int i = 1; i <= max_net_no; ++i) {
                board_handling.set_incompletes_filter(i, true);
            }
            for (Iterator<?> it = selected_nets.iterator(); it.hasNext();) {
                Net current_net = (Net) it.next();
                board_handling.set_incompletes_filter(current_net.net_number, false);
            }
            board_frame.board_panel.repaint();
        }
    }
}
