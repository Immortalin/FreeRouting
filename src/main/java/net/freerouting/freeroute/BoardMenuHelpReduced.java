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
 * BoardMenuHelpReduced.java
 *
 * Created on 21. Oktober 2005, 09:06
 *
 */
package net.freerouting.freeroute;

import java.util.Locale;

/**
 *
 * @author Alfons Wirtz
 */
@SuppressWarnings("serial")
class BoardMenuHelpReduced extends BoardMenu {

    final java.util.ResourceBundle resources;

    /**
     * Creates a new instance of BoardMenuHelpReduced Separated from
     * BoardMenuHelp to avoid ClassNotFound exception when the library jh.jar is
     * not found, which is only used in the extended help menu.
     */
    BoardMenuHelpReduced(BoardFrame p_board_frame) {
        super(p_board_frame);
        this.resources = java.util.ResourceBundle.getBundle(
                BoardMenuHelpReduced.class.getPackageName() + ".resources.BoardMenuHelp",
                Locale.getDefault());
        initializeComponents();
    }

    private void initializeComponents() {
        this.setText(this.resources.getString("help"));
        if (!System.getProperty("os.name").equals("Mac OS X")) {
            javax.swing.JMenuItem about_window = new javax.swing.JMenuItem();
            about_window.setText(this.resources.getString("about"));
            about_window.addActionListener((java.awt.event.ActionEvent evt) -> {
                board_frame.savable_subwindows.get(SavableSubwindowKey.ABOUT).setVisible(true);
            });
            this.add(about_window);
        }
    }
}
