package org.example.screen.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class PanelResult extends JPanel {
    private final JLabel lblTitulo = new JLabel("Resultado");
    private final JTextArea txtResultado = new JTextArea(10, 30);

    public PanelResult() {
        super(new BorderLayout(0, 6));
        setOpaque(false);
        lblTitulo.setFont(Theme.sans(Font.BOLD, 12));
        lblTitulo.setForeground(Theme.MUTED);
        txtResultado.setEditable(false);
        txtResultado.setLineWrap(true);
        txtResultado.setWrapStyleWord(true);
        txtResultado.setFont(Theme.mono(Font.PLAIN, 15));
        txtResultado.setBackground(Theme.CARD);
        txtResultado.setForeground(Theme.TEXT);
        txtResultado.setCaretColor(Theme.CARD);
        txtResultado.setBorder(new EmptyBorder(12, 14, 12, 14));

        JScrollPane scroll = new JScrollPane(txtResultado);
        scroll.setBorder(new LineBorder(Theme.BORDER, 1, true));
        scroll.getViewport().setBackground(Theme.CARD);

        add(lblTitulo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void setTitulo(String titulo) { lblTitulo.setText(titulo); }
    public void setTexto(String texto) { txtResultado.setText(texto); }
    public String getTexto() { return txtResultado.getText(); }
}
