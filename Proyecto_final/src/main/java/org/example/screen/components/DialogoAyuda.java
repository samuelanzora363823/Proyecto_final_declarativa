package org.example.screen.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Tabla de los casos que el sistema resuelve y de como se escribe cada uno.
 *
 * Hace falta por una razon practica: el sistema espera asin(x) o sh(x), y
 * nadie adivina eso. Quien escribe una notacion que el sistema no reconoce
 * recibe un error y lo interpreta como que el caso no esta implementado,
 * cuando si lo esta.
 */
public class DialogoAyuda extends JDialog {

    private static final String[][] CASOS = {
            {"a)", "Constante",              "k",              "5",             "5*x"},
            {"b)", "Identidad",              "x",              "x",             "x^2/2"},
            {"c)", "Constante por x",         "k*x",            "3*x",           "3*x^2/2"},
            {"d)", "Potencia",               "x^n",            "x^4",           "x^5/5"},
            {"d)", "Potencia n = -1",         "1/x  o  x^(-1)", "1/x",           "log(abs(x))"},
            {"e)", "Potencia de función",     "u^n",            "(2*x+1)^3",     "(2*x+1)^4/8"},
            {"f)", "Constante por función",   "k*u",            "3*tan(x)",      "-(3*log(abs(cos(x))))"},
            {"g)", "Suma",                   "u+v",            "x^2+3*x+5",     "x^3/3+3*x^2/2+5*x"},
            {"h)", "Resta",                  "u-v",            "x^2-sin(x)",    "x^3/3+cos(x)"},
            {"i)", "Exponencial base a",      "a^x",            "2^x",           "2^x/log(2)"},
            {"j)", "Exponencial de función",  "a^u",            "2^(3*x)",       "2^(3*x)/(3*log(2))"},
            {"k)", "Seno",                   "sin(x)",         "sin(x)",        "-cos(x)"},
            {"l)", "Coseno",                 "cos(x)",         "cos(x)",        "sin(x)"},
            {"m)", "Tangente",               "tan(x)",         "tan(x)",        "-log(abs(cos(x)))"},
            {"n)", "Arcoseno",               "asin(x)",        "asin(x)",       "x*asin(x)+sqrt(1-x^2)"},
            {"o)", "Arcocoseno",             "acos(x)",        "acos(x)",       "x*acos(x)-sqrt(1-x^2)"},
            {"p)", "Arcotangente",           "atan(x)",        "atan(x)",       "x*atan(x)-log(1+x^2)/2"},
            {"q)", "Seno hiperbólico",        "sh(x)",          "sh(x)",         "cosh(x)"},
            {"r)", "Coseno hiperbólico",      "cosh(x)",        "cosh(x)",       "sinh(x)"},
            {"s)", "Tangente hiperbólica",    "tanh(x)",        "tanh(x)",       "log(cosh(x))"},
            {"t)", "Arcoseno hiperbólico",    "arcsinh(x)",     "arcsinh(x)",    "x*asinh(x)-sqrt(x^2+1)"},
            {"u)", "Arcocoseno hiperbólico",  "arccosh(x)",     "arccosh(x)",    "x*acosh(x)-sqrt(x^2-1)"},
            {"v)", "Arcotangente hiperb.",    "arctanh(x)",     "arctanh(x)",    "x*atanh(x)+log(1-x^2)/2"}
    };

    private static final String NOTAS =
            "Notación alterna aceptada: sen, seno, coseno, tg, arcsen, arctg, "
                    + "senh, ch, th, arcsenh, ln y e^x se traducen "
                    + "automáticamente al nombre interno equivalente.\n"
                    + "Límites de integración: se admiten números y expresiones "
                    + "como pi, pi/2, 2*pi o e.\n"
                    + "Fuera de alcance: integración por partes (x*sin(x)) e "
                    + "identidades trigonométricas (sin(x)^2). El sistema lo "
                    + "informa en lugar de dar un resultado incorrecto.";

    public DialogoAyuda(Frame padre) {
        super(padre, "Funciones que el sistema resuelve", true);

        JPanel raiz = new JPanel(new BorderLayout(0, 14));
        raiz.setBackground(Theme.BG);
        raiz.setBorder(new EmptyBorder(20, 22, 20, 22));

        raiz.add(encabezado(), BorderLayout.NORTH);
        raiz.add(tabla(), BorderLayout.CENTER);
        raiz.add(pie(), BorderLayout.SOUTH);

        setContentPane(raiz);
        setSize(860, 620);
        setLocationRelativeTo(padre);
    }

    private JPanel encabezado() {
        JLabel titulo = new JLabel("Casos soportados");
        titulo.setFont(Theme.sans(Font.BOLD, 18));
        titulo.setForeground(Theme.TEXT);

        JLabel sub = new JLabel(
                "Los 22 casos del enunciado, con un ejemplo escrito tal como hay que teclearlo");
        sub.setFont(Theme.sans(Font.PLAIN, 12));
        sub.setForeground(Theme.MUTED);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.add(titulo);
        p.add(Box.createVerticalStrut(4));
        p.add(sub);
        return p;
    }

    private JScrollPane tabla() {
        String[] columnas = {"Caso", "Nombre", "Forma", "Se escribe", "Antiderivada"};

        DefaultTableModel modelo = new DefaultTableModel(CASOS, columnas) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        JTable t = new JTable(modelo);
        t.setFont(Theme.mono(Font.PLAIN, 12));
        t.setForeground(Theme.TEXT);
        t.setBackground(Theme.CARD);
        t.setGridColor(Theme.BORDER);
        t.setRowHeight(26);
        t.setSelectionBackground(Theme.BORDER);
        t.setSelectionForeground(Theme.TEXT);

        t.getTableHeader().setFont(Theme.sans(Font.BOLD, 12));
        t.getTableHeader().setBackground(Theme.BG);
        t.getTableHeader().setForeground(Theme.MUTED);

        DefaultTableCellRenderer celda = new DefaultTableCellRenderer();
        celda.setBackground(Theme.CARD);
        celda.setForeground(Theme.TEXT);
        celda.setBorder(new EmptyBorder(0, 8, 0, 8));
        for (int i = 0; i < columnas.length; i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(celda);
        }

        int[] anchos = {44, 180, 110, 150, 240};
        for (int i = 0; i < anchos.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        JScrollPane scroll = new JScrollPane(t);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scroll.getViewport().setBackground(Theme.CARD);
        return scroll;
    }

    private JPanel pie() {
        JTextArea notas = new JTextArea(NOTAS);
        notas.setEditable(false);
        notas.setLineWrap(true);
        notas.setWrapStyleWord(true);
        notas.setFont(Theme.sans(Font.PLAIN, 12));
        notas.setForeground(Theme.MUTED);
        notas.setBackground(Theme.BG);
        notas.setBorder(new EmptyBorder(0, 0, 10, 0));

        Buttons cerrar = new Buttons("Cerrar", Theme.CARD, Theme.TEXT, 20, 8, 12);
        cerrar.addActionListener(e -> dispose());

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        fila.setOpaque(false);
        fila.add(cerrar);

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(notas, BorderLayout.CENTER);
        p.add(fila, BorderLayout.SOUTH);
        return p;
    }
}
