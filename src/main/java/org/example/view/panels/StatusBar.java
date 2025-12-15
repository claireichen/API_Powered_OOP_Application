package org.example.view.panels;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel {

    private final JLabel lblMessage = new JLabel("Ready.");

    public StatusBar() {
        setLayout(new BorderLayout());
        add(lblMessage, BorderLayout.WEST);
    }

    public void setMessage(String message) {
        lblMessage.setText(message);
    }
}

