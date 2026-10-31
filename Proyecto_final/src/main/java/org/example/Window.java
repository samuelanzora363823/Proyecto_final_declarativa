package org.example;

import org.example.screen.components.*;
import org.example.screen.components.TextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class Window extends JFrame {

    private static final String MODO_INTEGRAL = "Integral definida";
    private static final String MODO_DERIVADA = "Derivada";

    private final TextField txtEntrada = new TextField("Ej: 3*x, tan(x), (2*x+1)^3");
    private final TextField txtLimiteA = new TextField("Límite inferior (a)");
    private final TextField txtLimiteB = new TextField("Límite superior (b)");
    private final JComboBox<String> cmbOperacion =
            new JComboBox<>(new String[]{MODO_INTEGRAL, MODO_DERIVADA});
    private final JLabel lblEtiquetaLimites = etiqueta("Límites de integración");
    private final JPanel panelLimites = new JPanel(new GridLayout(1, 2, 10, 0));
    private final PanelResult panelResultado = new PanelResult();
    private final JLabel lblEstado = new JLabel("");
    private Buttons btnResolver;

    public Window(Connection motor) {
        super("Calculadora de Integrales y Derivadas");

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setOpaque(false);

        agregar(contenido, etiqueta("Operación"), 6);
        cmbOperacion.setFont(Theme.sans(Font.PLAIN, 14));
        cmbOperacion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        cmbOperacion.setAlignmentX(Component.LEFT_ALIGNMENT);
        agregar(contenido, cmbOperacion, 10);

        agregar(contenido, etiqueta("Función f(x)"), 6);
        agregar(contenido, txtEntrada, 10);
        agregar(contenido, lblEtiquetaLimites, 6);

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
        cmbOperacion.addActionListener(e -> actualizarModo());
        txtEntrada.addActionListener(e -> ejecutar(motor));
        txtLimiteA.addActionListener(e -> ejecutar(motor));
        txtLimiteB.addActionListener(e -> ejecutar(motor));

        setMinimumSize(new Dimension(660, 600));
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        actualizarModo();
    }

    private boolean modoIntegral() {
        return MODO_INTEGRAL.equals(cmbOperacion.getSelectedItem());
    }

    private void actualizarModo() {
        boolean integral = modoIntegral();

        lblEtiquetaLimites.setVisible(integral);
        panelLimites.setVisible(integral);
        btnResolver.setText(integral ? "Resolver integral" : "Derivar función");
        panelResultado.setTitulo(integral ? "Resultado" : "Derivada");

        revalidate();
        repaint();
        pack();
    }

    private JPanel crearEncabezado() {
        JLabel titulo = new JLabel("Calculadora simbólica");
        titulo.setFont(Theme.sans(Font.BOLD, 24));
        titulo.setForeground(Theme.TEXT);

        JLabel subtitulo = new JLabel(
                "Integrales definidas y derivadas con Java y SWI-Prolog");
        subtitulo.setFont(Theme.sans(Font.PLAIN, 13));
        subtitulo.setForeground(Theme.MUTED);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(subtitulo);

        Buttons btnAyuda = new Buttons("Ayuda", Theme.CARD, Theme.TEXT, 18, 8, 12);
        btnAyuda.addActionListener(e -> new DialogoAyuda(this).setVisible(true));

        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.add(textos, BorderLayout.WEST);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.setOpaque(false);
        derecha.add(btnAyuda);
        fila.add(derecha, BorderLayout.EAST);

        return fila;
    }

    private JPanel crearBotones(Connection motor) {
        btnResolver = new Buttons("Resolver integral", Theme.BLUE, Theme.BG, 26, 11, 14);
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

    /**
     * Antes este metodo llamaba siempre a la integral, sin mirar la operacion
     * elegida, y exigia los dos limites incluso en modo Derivada, donde los
     * campos estan ocultos. El resultado era que la opcion Derivada no hacia
     * nada y pedia llenar campos invisibles.
     */
    private void ejecutar(Connection motor) {
        if (modoIntegral()) {
            resolverIntegral(motor);
        } else {
            resolverDerivada(motor);
        }
    }

    private void resolverIntegral(Connection motor) {
        String entrada = txtEntrada.getText().trim();
        String a = txtLimiteA.getText().trim();
        String b = txtLimiteB.getText().trim();

        if (entrada.isEmpty() || a.isEmpty() || b.isEmpty()) {
            mostrar("", "Llena la función y ambos límites.", Theme.AMBER);
            return;
        }

        try {
            ResultadoIntegral r = motor.integrar(entrada, a, b);

            String texto = "Antiderivada:\n"
                    + "  F(x) = " + r.antiderivada() + " + C\n\n"
                    + "Integral definida entre " + a + " y " + b + ":\n"
                    + "  F(b) - F(a) = " + numero(r.integralDefinida()) + "\n\n"
                    + "Área bajo la curva:\n"
                    + "  " + numero(r.areaBajoCurva());

            if (Math.abs(r.integralDefinida() - r.areaBajoCurva()) > 1e-9) {
                texto += "\n\nLos dos valores difieren porque f cambia de signo "
                        + "en el intervalo: la integral definida resta las "
                        + "regiones bajo el eje, el área las suma.";
            }

            mostrar(texto, "Integral calculada con éxito", Theme.GREEN);

        } catch (IllegalArgumentException ex) {
            mostrar("", ex.getMessage(), Theme.AMBER);
        } catch (Exception ex) {
            mostrar("", "Error: " + ex.getMessage(), Theme.RED);
        }
    }

    private void resolverDerivada(Connection motor) {
        String entrada = txtEntrada.getText().trim();

        if (entrada.isEmpty()) {
            mostrar("", "Escribe la función que quieres derivar.", Theme.AMBER);
            return;
        }

        try {
            String derivada = motor.derivar(entrada);
            mostrar("Derivada:\n  f'(x) = " + derivada,
                    "Derivada calculada con éxito", Theme.GREEN);

        } catch (IllegalArgumentException ex) {
            mostrar("", ex.getMessage(), Theme.AMBER);
        } catch (Exception ex) {
            mostrar("", "Error: " + ex.getMessage(), Theme.RED);
        }
    }

    /** Los enteros se muestran sin decimales; el resto con seis cifras. */
    private static String numero(double v) {
        if (v == Math.rint(v) && Math.abs(v) < 1e15) {
            return String.format("%.0f", v);
        }
        return String.format("%.6f", v);
    }

    private void mostrar(String resultado, String estado, Color colorEstado) {
        panelResultado.setTexto(resultado);
        lblEstado.setText(estado);
        lblEstado.setForeground(colorEstado);
    }

    private void copiarResultado() {
        String t = panelResultado.getTexto();

        if (t.isEmpty()) {
            return;
        }

        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(t), null);

        lblEstado.setText("Copiado al portapapeles");
        lblEstado.setForeground(Theme.BLUE);
    }

    private static void agregar(
            JPanel panel, JComponent componente, int espacioDespues) {

        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(componente);

        if (espacioDespues > 0) {
            panel.add(Box.createVerticalStrut(espacioDespues));
        }
    }

    private static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Theme.sans(Font.BOLD, 12));
        l.setForeground(Theme.MUTED);
        return l;
    }
}
