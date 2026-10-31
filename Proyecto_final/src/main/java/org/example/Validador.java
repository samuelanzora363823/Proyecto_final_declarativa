package org.example;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Valida lo que escribe el usuario ANTES de construir la consulta de Prolog.
 *
 * Sin esta clase, el texto del campo se interpolaba crudo en la consulta: un
 * parentesis desbalanceado producia un error de sintaxis de Prolog en lugar de
 * un mensaje util, y una entrada construida a proposito podia ejecutar
 * predicados arbitrarios. La validacion es por lista blanca: todo lo que no
 * este explicitamente permitido se rechaza.
 */
public final class Validador {

    private Validador() {
    }

    /** Nombres de funcion que la base de conocimiento sabe interpretar. */
    private static final Set<String> FUNCIONES = Set.of(
            // nombres internos de SWI-Prolog
            "sin", "cos", "tan", "asin", "acos", "atan",
            "sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
            "exp", "log", "sqrt", "abs",
            // notacion hispana, que la base de conocimiento normaliza
            "sen", "seno", "coseno", "tg", "tang",
            "arcsen", "arcsin", "arccos", "arctan", "arctg",
            "sh", "ch", "th", "senh",
            "arcsinh", "arcsenh", "arccosh", "arctanh", "ln"
    );

    /** Identificadores que no son funciones. */
    private static final Set<String> CONSTANTES = Set.of("x", "pi", "e");

    /** Caracteres admitidos. Todo lo demas se rechaza. */
    private static final Pattern CARACTERES_VALIDOS =
            Pattern.compile("[A-Za-z0-9.+\\-*/^() ]+");

    /** Un identificador: secuencia de letras y digitos que empieza por letra. */
    private static final Pattern IDENTIFICADOR =
            Pattern.compile("[A-Za-z][A-Za-z0-9]*");

    /**
     * Valida la funcion a integrar o derivar.
     *
     * @throws IllegalArgumentException con un mensaje dirigido al usuario
     */
    public static void validarExpresion(String texto) {
        validar(texto, "La función", true);
    }

    /**
     * Valida un limite de integracion. Igual que una expresion, pero sin x:
     * un limite tiene que ser un valor concreto.
     */
    public static void validarLimite(String texto, String cual) {
        validar(texto, "El límite " + cual, false);
    }

    private static void validar(String texto, String queEs, boolean admiteX) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Falta escribir " + queEs.toLowerCase() + ".");
        }

        if (!CARACTERES_VALIDOS.matcher(texto).matches()) {
            throw new IllegalArgumentException(
                    queEs + " contiene caracteres no permitidos. "
                            + "Solo se admiten letras, números, espacios y los "
                            + "símbolos + - * / ^ ( )");
        }

        verificarParentesis(texto, queEs);
        verificarIdentificadores(texto, queEs, admiteX);
    }

    private static void verificarParentesis(String texto, String queEs) {
        int abiertos = 0;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == '(') {
                abiertos++;
            } else if (c == ')') {
                abiertos--;
                if (abiertos < 0) {
                    throw new IllegalArgumentException(
                            queEs + " tiene un paréntesis de cierre sin abrir.");
                }
            }
        }
        if (abiertos > 0) {
            throw new IllegalArgumentException(
                    queEs + " tiene " + abiertos + " paréntesis sin cerrar.");
        }
    }

    private static void verificarIdentificadores(
            String texto, String queEs, boolean admiteX) {

        Matcher m = IDENTIFICADOR.matcher(texto);
        while (m.find()) {
            String id = m.group();
            String idMin = id.toLowerCase();

            if (!id.equals(idMin)) {
                throw new IllegalArgumentException(
                        queEs + " usa mayúsculas en \"" + id + "\". "
                                + "Escribe los nombres en minúscula.");
            }

            if (FUNCIONES.contains(idMin)) {
                continue;
            }

            if (CONSTANTES.contains(idMin)) {
                if (idMin.equals("x") && !admiteX) {
                    throw new IllegalArgumentException(
                            queEs + " no puede depender de x: "
                                    + "tiene que ser un valor concreto, "
                                    + "por ejemplo 0, 2.5 o pi/2.");
                }
                continue;
            }

            throw new IllegalArgumentException(
                    "\"" + id + "\" no es una función que el sistema conozca. "
                            + "Abre Ayuda para ver la lista completa.");
        }
    }
}
