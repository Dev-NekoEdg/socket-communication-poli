package com.poli.client;

import javax.swing.SwingUtilities;

public class ClientApplication {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatWindow().setVisible(true));
    }
}
