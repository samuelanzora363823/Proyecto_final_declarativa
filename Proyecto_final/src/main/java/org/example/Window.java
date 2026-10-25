package org.example;

import org.example.screen.components.*;
import org.example.screen.components.TextField;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class Window extends JFrame {
    private final TextField txtEntrada = new TextField("Ej: 3*x");
    private final TextField txtLimiteA = new TextField("Límite inferior (a)");
    private final TextField txtLimiteB = new TextField("Límite superior (b)");
    private final PanelResult panelResultado = new PanelResult();
    private final JLabel lblEstado = new JLabel("");

    public Window(Connection motor) {
        super("Cálculo de Integrales en Prolog");

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setOpaque(false);

        agregar(contenido, etiqueta("Función a integrar f(x)"), 6);
        agregar(contenido, txtEntrada, 10);
        agregar(contenido, etiqueta("Límites de integración"), 6);

        JPanel panelLimites = new JPanel(new GridLayout(1, 2, 10, 0));
        panelLimites.setOpaque(false);
        panelLimites.add(txtLimiteA);
        panelLimites.add(txtLimiteB);
        panelLimites.setAlignmentX(Component.LEFT_ALIGNMENT);

        agregar(contenido, panelLimites, 18);
        agregar(contenido, crearBotones(motor), 22);
        agregar(contenido, panelResultado, 10);
        agregar(contenido, crearPie(), 0);

        JPanel raiz = new JPanel(new BorderLayout(0, 20));
        raiz.setBackground(Theme.BG);
        raiz.setBorder(new EmptyBorder(24, 28, 24, 28));
        raiz.add(crearEncabezado(), BorderLayout.NORTH);
        raiz.add(contenido, BorderLayout.CENTER);

        setContentPane(raiz);
        setMinimumSize(new Dimension(660, 560));
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JPanel crearEncabezado() {
        JLabel titulo = new JLabel("Resolutor de Integrales");
        titulo.setFont(Theme.sans(Font.BOLD, 24));
        titulo.setForeground(Theme.TEXT);
        JLabel subtitulo = new JLabel("Solución de integrales y área bajo la curva con SWI-Prolog");
        subtitulo.setFont(Theme.sans(Font.PLAIN, 13));
        subtitulo.setForeground(Theme.MUTED);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.add(titulo);
        p.add(Box.createVerticalStrut(4));
        p.add(subtitulo);
        return p;
    }

    private JPanel crearBotones(Connection motor) {
        Buttons btnResolver = new Buttons("Resolver Integral", Theme.BLUE, Theme.BG, 26, 11, 14);
        btnResolver.addActionListener(e -> ejecutar(motor));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.add(btnResolver);
        return p;
    }

    private JPanel crearPie() {
        lblEstado.setFont(Theme.sans(Font.PLAIN, 12));
        lblEstado.setForeground(Theme.MUTED);
        Buttons btnCopiar = new Buttons("Copiar", Theme.CARD, Theme.TEXT, 16, 7, 12);
        btnCopiar.addActionListener(e -> copiarResultado());

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(lblEstado, BorderLayout.WEST);
        p.add(btnCopiar, BorderLayout.EAST);
        return p;
    }

    private void ejecutar(Connection motor) {
        String entrada = txtEntrada.getText().trim();
        String a = txtLimiteA.getText().trim();
        String b = txtLimiteB.getText().trim();

        if (entrada.isEmpty() || a.isEmpty() || b.isEmpty()) {
            mostrar("", "Llena la función y ambos límites.", Theme.AMBER);
            return;
        }

        try {
            String resultado = motor.resolver(entrada, a, b);
            if (resultado.startsWith("Sin solución")) {
                mostrar("", resultado, Theme.AMBER);
            } else {
                mostrar(resultado, "Integral calculada con éxito", Theme.GREEN);
            }
        } catch (Exception ex) {
            mostrar("", "Error: " + ex.getMessage(), Theme.RED);
        }
    }

    private void mostrar(String resultado, String estado, Color colorEstado) {
        panelResultado.setTexto(resultado);
        lblEstado.setText(estado);
        lblEstado.setForeground(colorEstado);
    }

    private void copiarResultado() {
        String t = panelResultado.getTexto();
        if (t.isEmpty()) return;
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(t), null);
        lblEstado.setText("Copiado al portapapeles");
        lblEstado.setForeground(Theme.BLUE);
    }

    private static void agregar(JPanel panel, JComponent c, int espacioDespues) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(c);
        if (espacioDespues > 0) panel.add(Box.createVerticalStrut(espacioDespues));
    }

    private static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Theme.sans(Font.BOLD, 12));
        l.setForeground(Theme.MUTED);
        return l;
    }
}