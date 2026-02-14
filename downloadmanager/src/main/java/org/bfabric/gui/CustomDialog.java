/*
    MIT License

    Copyright (c) 2005-2026 Functional Genomics Center Zurich, UZH/ETH Zurich

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package org.bfabric.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;

import org.bfabric.Constants;
import org.bfabric.DownloadManager;

public class CustomDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1;

    private final JButton[] buttons;

    private int choice = -1;

    /**
     * Constructor
     *
     * @param frame the frame
     * @param modal the modal
     * @param messages the messages
     * @param buttonNames the buttonNames
     */
    public CustomDialog(JFrame frame, boolean modal, List<String> messages, List<String> buttonNames) {
        super(frame, modal);

        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, Color.WHITE));

        Box messageBox = Box.createVerticalBox();
        panel.add(messageBox, BorderLayout.NORTH);

        JLabel[] labels = new JLabel[messages.size()];
        for (int i = 0; i < messages.size(); i++) {
            labels[i] = new JLabel(messages.get(i));
            labels[i].setFont(Constants.TABLE_FONT);
            labels[i].setForeground(Constants.TEXT_FONT_COLOR);
            messageBox.add(labels[i]);
            messageBox.add(Box.createVerticalStrut(10));
        }

        Box buttonBox = Box.createHorizontalBox();
        panel.add(buttonBox, BorderLayout.WEST);

        buttons = new JButton[buttonNames.size()];
        for (int i = 0; i < buttonNames.size(); i++) {
            buttons[i] = new JButton(buttonNames.get(i));
            buttons[i].addActionListener(this);
            buttonBox.add(buttons[i]);
            buttonBox.add(Box.createHorizontalStrut(10));
        }

        setTitle(DownloadManager.getApplicationTitle());
        setLocationByPlatform(true);
        setLocationRelativeTo(frame);
        setResizable(false);
        pack(); // Arrange the components.
        setVisible(true); // Make the frame visible.
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] == e.getSource()) {
                choice = i;
                setVisible(false);
                break;
            }
        }
    }

    /**
     * @return choice the choice
     */
    public int getChoice() {
        return choice;
    }
}
