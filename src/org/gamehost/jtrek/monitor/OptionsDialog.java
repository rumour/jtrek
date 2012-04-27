/**
 *  Copyright (C) 2003-2012  Joe Hopkinson, Jay Ashworth
 *  
 *  JavaTrek is based on Chuck L. Peterson's MTrek.
 *
 *  This file is part of JavaTrek.
 *
 *  JavaTrek is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  JavaTrek is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with JavaTrek; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.gamehost.jtrek.monitor;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: Jay
 * Date: Mar 9, 2004
 * Time: 3:18:58 PM
 */
public class OptionsDialog extends JDialog {
	private static final long serialVersionUID = -8170448039012790669L;
    JLabel addressLabel = new JLabel();
    protected JTextField addressField = new JTextField();
    JLabel portLabel = new JLabel();
    protected JTextField portField = new JTextField();
    JLabel playerLabel = new JLabel();
    protected JTextField playerField = new JTextField();
    JLabel passwordLabel = new JLabel();
    protected JPasswordField passwordField = new JPasswordField();
    JButton okButton = new JButton();

    public OptionsDialog(JFrame parent) {
        super(parent);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        addressLabel.setText("Address");
        addressField.setText("mtrek.game-host.org");
        portLabel.setText("Port");
        portField.setText("1710");
        playerLabel.setText("Player Account");
        passwordLabel.setText("Password");
        okButton.setText("OK");
        playerField.setText("");
        passwordField.setText("");
        
        JPanel panel = new JPanel(new MigLayout("ins 0, wrap 2", "[right][fill,grow]"));
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        panel.setMinimumSize(new Dimension(270, 101));

        panel.add(addressLabel);
        panel.add(addressField);
        panel.add(portLabel);
        panel.add(portField);
        panel.add(playerLabel);
        panel.add(playerField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(okButton, "skip 1");

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                saveOptions();
            }
        });

        add(panel, BorderLayout.CENTER);
        pack();
    }

    private void saveOptions() {
        // could persist the field settings to a config file here
        setVisible(false);
    }
}
