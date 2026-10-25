package org.example;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Connection motor = new Connection();
                new Window(motor).setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Fallo al iniciar motor Prolog:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}