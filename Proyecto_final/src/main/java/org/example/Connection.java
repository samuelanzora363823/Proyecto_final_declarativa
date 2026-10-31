package org.example;

import org.jpl7.Query;
import org.jpl7.Term;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * Puente entre la interfaz de Java y la base de conocimiento de SWI-Prolog.
 *
 * Esta clase es la unica que sabe de JPL. Traduce las respuestas de Prolog,
 * que llegan como terminos de estado, a objetos y mensajes de Java.
 */
public class Connection {

    private static final String RECURSO = "/base_conocimiento.pl";

    public Connection() throws Exception {
        Path base = localizarBaseConocimiento();
        String ruta = base.toAbsolutePath().toString()
                .replace("\\", "/")
                .replace("'", "\\'");

        try {
            if (!new Query("consult('" + ruta + "')").hasSolution()) {
                throw new Exception(
                        "SWI-Prolog no pudo cargar la base de conocimiento:\n" + ruta);
            }
        } catch (UnsatisfiedLinkError e) {
            throw new Exception(
                    "No se encuentra la biblioteca nativa de SWI-Prolog.\n\n"
                            + "JPL necesita, además de jpl.jar, la biblioteca nativa que\n"
                            + "viene con SWI-Prolog. Define estas variables de entorno\n"
                            + "apuntando a tu instalación:\n\n"
                            + "  Windows\n"
                            + "    SWI_HOME_DIR = C:\\Program Files\\swipl\n"
                            + "    PATH        += C:\\Program Files\\swipl\\bin\n\n"
                            + "  Linux\n"
                            + "    SWI_HOME_DIR = /usr/lib/swi-prolog\n"
                            + "    LD_LIBRARY_PATH += $SWI_HOME_DIR/lib/x86_64-linux\n\n"
                            + "Después cierra y vuelve a abrir la terminal o el IDE.", e);
        }
    }

    /**
     * Busca la base de conocimiento primero en el classpath, de donde se
     * extrae a un archivo temporal. Asi el programa funciona desde cualquier
     * carpeta y tambien empaquetado en un jar, donde un File con ruta
     * relativa nunca habria existido. Las rutas de desarrollo quedan como
     * respaldo para cuando se ejecuta desde el IDE sin recursos copiados.
     */
    private Path localizarBaseConocimiento() throws IOException {
        try (InputStream in = Connection.class.getResourceAsStream(RECURSO)) {
            if (in != null) {
                Path temporal = Files.createTempFile("base_conocimiento", ".pl");
                temporal.toFile().deleteOnExit();
                Files.copy(in, temporal, StandardCopyOption.REPLACE_EXISTING);
                return temporal;
            }
        }

        String[] respaldos = {
                "base_conocimiento.pl",
                "src/main/resources/base_conocimiento.pl",
                "Proyecto_final/src/main/resources/base_conocimiento.pl"
        };
        for (String candidato : respaldos) {
            Path p = Paths.get(candidato);
            if (Files.exists(p)) {
                return p;
            }
        }

        throw new FileNotFoundException(
                "No se encuentra base_conocimiento.pl, ni en el classpath "
                        + "ni en src/main/resources.");
    }

    /**
     * Resuelve la integral definida entre a y b.
     *
     * @throws IllegalArgumentException si la entrada no es valida o si la base
     *                                  de conocimiento rechaza el problema; el
     *                                  mensaje ya esta escrito para el usuario
     */
    public ResultadoIntegral integrar(String expresion, String a, String b)
            throws Exception {

        Validador.validarExpresion(expresion);
        Validador.validarLimite(a, "inferior (a)");
        Validador.validarLimite(b, "superior (b)");

        Term r = consultar(
                String.format("integrar((%s), (%s), (%s), R)", expresion, a, b));

        if (esError(r)) {
            throw new IllegalArgumentException(mensajeDeError(r.arg(1)));
        }

        return new ResultadoIntegral(
                r.arg(1).toString(),
                aDouble(r.arg(2)),
                aDouble(r.arg(3)));
    }

    /**
     * Deriva la expresion. No lo pide el enunciado, pero el sistema lo ofrece
     * y las reglas de los casos e) y j) usan la derivacion internamente.
     */
    public String derivar(String expresion) throws Exception {
        Validador.validarExpresion(expresion);

        Term r = consultar(String.format("derivar((%s), R)", expresion));

        if (esError(r)) {
            throw new IllegalArgumentException(mensajeDeError(r.arg(1)));
        }

        return r.arg(1).toString();
    }

    /** Ejecuta la consulta y devuelve el termino de estado que ligo R. */
    private Term consultar(String consulta) throws Exception {
        Map<String, Term> solucion;
        try {
            solucion = new Query(consulta).oneSolution();
        } catch (Exception e) {
            throw new Exception(
                    "SWI-Prolog no pudo interpretar la consulta.\n"
                            + "Revisa la sintaxis de la expresión.", e);
        }

        if (solucion == null || solucion.get("R") == null) {
            throw new Exception(
                    "La base de conocimiento no devolvió ninguna respuesta. "
                            + "Puede que no se haya cargado correctamente.");
        }
        return solucion.get("R");
    }

    private static boolean esError(Term r) {
        return "error".equals(r.name());
    }

    /**
     * Traduce los terminos de error de Prolog a mensajes en castellano.
     *
     * Distinguir estos casos importa: "no hay regla" y "la regla existe pero
     * no se puede evaluar en este intervalo" son situaciones muy distintas y
     * el usuario merece saber cual le toco.
     */
    private static String mensajeDeError(Term error) {
        switch (error.name()) {
            case "sin_regla":
                return "El sistema no tiene una regla para esa expresión.\n\n"
                        + "Puede ser que la función no esté entre las que soporta, "
                        + "o que la integral necesite integración por partes o "
                        + "identidades trigonométricas, que están fuera del "
                        + "alcance del sistema.\n"
                        + "Abre Ayuda para ver los casos disponibles.";

            case "limite_invalido":
                return "Alguno de los límites no es un valor numérico.\n"
                        + "Se admiten números y expresiones como pi/2 o 2*pi.";

            case "fuera_de_dominio":
                return "El valor " + error.arg(1) + " está fuera del dominio "
                        + "de la función.\n\n"
                        + "Por ejemplo, arcsen y arccos solo aceptan valores "
                        + "entre -1 y 1; arccosh necesita valores mayores o "
                        + "iguales que 1; y log y 1/x no están definidos en 0.";

            case "discontinua":
                return "La funcion no es continua en el intervalo: hay una "
                        + "discontinuidad en x = " + error.arg(1) + ".\n\n"
                        + "El teorema fundamental del cálculo exige continuidad "
                        + "en todo el intervalo, así que la integral no se puede "
                        + "calcular como F(b) - F(a). Prueba con un intervalo "
                        + "que no contenga ese punto.";

            default:
                return "Error no previsto de la base de conocimiento: " + error;
        }
    }

    private static double aDouble(Term t) {
        try {
            return t.doubleValue();
        } catch (Exception e) {
            return Double.parseDouble(t.toString());
        }
    }
}
