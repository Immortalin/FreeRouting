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
 * RatsNest.java
 *
 * Created on 18. Maerz 2004, 07:30
 */
package net.freerouting.freeroute.interactive;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import net.freerouting.freeroute.board.BasicBoard;
import net.freerouting.freeroute.board.Connectable;
import net.freerouting.freeroute.board.Item;
import net.freerouting.freeroute.boardgraphics.GraphicsContext;
import net.freerouting.freeroute.datastructures.UndoableObjects;
import net.freerouting.freeroute.geometry.planar.FloatPoint;
import net.freerouting.freeroute.rules.Net;

/**
 * Creates all Incompletes (Ratsnest) to display them on the screen
 *
 * @author Alfons Wirtz
 */
public class RatsNest {

    private final NetIncompletes[] net_incompletes;
    private final boolean[] is_filtered;
    public boolean hidden = false;

    /**
     * Creates a new instance of RatsNest
     */
    RatsNest(BasicBoard p_board) {
        int max_net_no = p_board.rules.nets.max_net_no();
        // Create the net item lists at once for performance reasons.
        ArrayList<Collection<Item>> net_item_lists = new ArrayList<>(max_net_no);
        for (int i = 0; i < max_net_no; ++i) {
            net_item_lists.add(new LinkedList<>());
        }
        Iterator<UndoableObjects.UndoableObjectNode> it = p_board.item_list.start_read_object();
        for (;;) {
            Item curr_item = (Item) p_board.item_list.read_object(it);
            if (curr_item == null) {
                break;
            }
            if (curr_item instanceof Connectable) {
                for (int i = 0; i < curr_item.net_count(); ++i) {
                    net_item_lists.get(curr_item.get_net_no(i) - 1).add(curr_item);
                }
            }
        }
        this.net_incompletes = new NetIncompletes[max_net_no];
        this.is_filtered = new boolean[max_net_no];
        for (int i = 0; i < net_incompletes.length; ++i) {
            net_incompletes[i] = new NetIncompletes(i + 1, net_item_lists.get(i), p_board);
            is_filtered[i] = false;
        }
    }

    /**
     * Recalculates the incomplete connections for the input net
     */
    public void recalculate(int p_net_no, BasicBoard p_board) {
        if (p_net_no >= 1 && p_net_no <= net_incompletes.length) {
            Collection<Item> item_list = p_board.get_connectable_items(p_net_no);
            net_incompletes[p_net_no - 1] = new NetIncompletes(p_net_no, item_list, p_board);
        }
    }

    /**
     * Recalculates the incomplete connections for the input net with the input
     * item list.
     */
    public void recalculate(int p_net_no, Collection<Item> p_item_list, BasicBoard p_board) {
        if (p_net_no >= 1 && p_net_no <= net_incompletes.length) {
            // copy p_item_list, because it will be changed inside the constructor of NetIncompletes
            Collection<Item> item_list = new LinkedList<>(p_item_list);
            net_incompletes[p_net_no - 1] = new NetIncompletes(p_net_no, item_list, p_board);
        }
    }

    public int incomplete_count() {
        int result = 0;
        for (int i = 0; i < net_incompletes.length; ++i) {
            result += net_incompletes[i].count();
        }
        return result;
    }

    public int length_violation_count() {
        int result = 0;
        for (int i = 0; i < net_incompletes.length; ++i) {
            if (net_incompletes[i].get_length_violation() != 0) {
                ++result;
            }
        }
        return result;
    }

    /**
     * Returns the length of the violation of the length restriction of the net
     * with number p_net_no, {@literal >} 0, if the cumulative trace length is
     * to big, {@literal <} 0, if the trace length is to smalll, 0, if the thace
     * length is ok or the net has no length restrictions
     */
    public double get_length_violation(int p_net_no) {
        if (p_net_no <= 0 || p_net_no > net_incompletes.length) {
            return 0;
        }
        return net_incompletes[p_net_no - 1].get_length_violation();
    }

    /**
     * Returns all airlines of the ratsnest.
     */
    public AirLine[] get_airlines() {
        AirLine[] result = new AirLine[incomplete_count()];
        int curr_index = 0;
        for (int i = 0; i < net_incompletes.length; ++i) {
            Collection<AirLine> curr_list = net_incompletes[i].incompletes;
            for (AirLine curr_line : curr_list) {
                result[curr_index] = curr_line;
                ++curr_index;
            }
        }
        return result;
    }

    public void hide() {
        hidden = true;
    }

    public void show() {
        hidden = false;
    }

    /**
     * Recalculate the length matching violations. Return false, if the length
     * violations have not changed.
     */
    public boolean recalculate_length_violations() {
        boolean result = false;
        for (int i = 0; i < net_incompletes.length; ++i) {
            if (net_incompletes[i].calc_length_violation()) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Used for example to hide the incompletes during interactive routiing.
     */
    public boolean is_hidden() {
        return hidden;
    }

    /**
     * Sets the visibility filter for the incompletes of the input net.
     */
    public void set_filter(int p_net_no, boolean p_value) {
        if (p_net_no < 1 || p_net_no > is_filtered.length) {
            return;
        }
        is_filtered[p_net_no - 1] = p_value;
    }

    public void draw(Graphics p_graphics, GraphicsContext p_graphics_context) {
        boolean draw_length_violations_only = this.hidden;

        for (int i = 0; i < net_incompletes.length; ++i) {
            if (!is_filtered[i]) {
                net_incompletes[i].draw(p_graphics, p_graphics_context, draw_length_violations_only);
            }
        }

    }

    public static class AirLine implements Comparable<AirLine>, net.freerouting.freeroute.board.ObjectInfoPanel.Printable {

        public final Net net;
        public final Item from_item;
        public final FloatPoint from_corner;
        public final Item to_item;
        public final FloatPoint to_corner;

        AirLine(Net p_net, Item p_from_item, FloatPoint p_from_corner, Item p_to_item,
                FloatPoint p_to_corner) {
            net = p_net;
            from_item = p_from_item;
            from_corner = p_from_corner;
            to_item = p_to_item;
            to_corner = p_to_corner;
        }

        @Override
        public int compareTo(AirLine p_other) {
            return this.net.name.compareTo(p_other.net.name);
        }

        @Override
        public String toString() {
            String result = this.net.name + ": " + item_info(from_item) + " - " + item_info(to_item);
            return result;
        }

        private String item_info(Item p_item) {
            java.util.ResourceBundle resources
                    = java.util.ResourceBundle.getBundle("net.freerouting.freeroute.interactive.resources.RatsNest", Locale.getDefault());
            String result;
            if (p_item instanceof net.freerouting.freeroute.board.Pin) {
                net.freerouting.freeroute.board.Pin curr_pin = (net.freerouting.freeroute.board.Pin) p_item;
                result = curr_pin.component_name() + ", " + curr_pin.name();
            } else if (p_item instanceof net.freerouting.freeroute.board.Via) {
                result = resources.getString("via");
            } else if (p_item instanceof net.freerouting.freeroute.board.Trace) {
                result = resources.getString("trace");
            } else if (p_item instanceof net.freerouting.freeroute.board.ConductionArea) {
                result = resources.getString("conduction_area");
            } else {
                result = resources.getString("unknown");
            }
            return result;
        }

        @Override
        public void print_info(net.freerouting.freeroute.board.ObjectInfoPanel p_window) {
            java.util.ResourceBundle resources
                    = java.util.ResourceBundle.getBundle("net.freerouting.freeroute.interactive.resources.RatsNest", Locale.getDefault());
            p_window.append_bold(resources.getString("incomplete"));
            p_window.append(" " + resources.getString("net") + " ");
            p_window.append(net.name);
            p_window.append(" " + resources.getString("from") + " ", "Incomplete Start Item", from_item);
            p_window.append(from_corner);
            p_window.append(" " + resources.getString("to") + " ", "Incomplete End Item", to_item);
            p_window.append(to_corner);
            p_window.newline();
        }
    }
}
