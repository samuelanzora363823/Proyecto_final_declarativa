package org.example;

import org.jpl7.Query;
import org.jpl7.Term;

import java.io.File;
import java.util.Map;

public class Connection {
    public Connection() throws Exception {
        File plFile = new File("base_conocimiento.pl");

        if (!plFile.exists()) {
            plFile = new File("src/main/resources/base_conocimiento.pl");
        }

        if (!plFile.exists()) {
            plFile = new File(
                    "Proyecto_final/src/main/resources/base_conocimiento.pl"
            );
        }

        if (!plFile.exists()) {
            throw new Exception(
                    "No se encuentra base_conocimiento.pl. " +
                            "Verifica la carpeta src/main/resources."
            );
        }

        String path = plFile.getAbsolutePath().replace("\\", "/").replace("'", "\\'");
        Query consult = new Query("consult('" + path + "')");
        if (!consult.hasSolution()) {
            throw new Exception("No se pudo cargar el archivo Prolog.");
        }
    }

    public String resolver(String expresion, String a, String b) throws Exception {
        String queryStr = String.format("resolver_integral((%s), (%s), (%s), Expr, Area)", expresion, a, b);
        Query consultaProlog = new Query(queryStr);
        Map<String, Term> solucion = consultaProlog.oneSolution();

        if (solucion != null) {
            return "Antiderivada:\n" + solucion.get("Expr")
                    + "\n\nIntegral definida entre " + a + " y " + b + ":\n" + solucion.get("Area");
        }
        return "Sin solución para esa expresión. Revisa la sintaxis o si la función está soportada.";
    }

    public String derivar(String expresion) throws Exception {
        String queryStr = String.format("resolver_derivada((%s), Resultado)", expresion);
        Query consultaProlog = new Query(queryStr);
        Map<String, Term> solucion = consultaProlog.oneSolution();

        if (solucion != null) {
            return "Derivada:\n" + solucion.get("Resultado");
        }
        return "Sin regla de derivación para esa expresión. Revisa la sintaxis o si la función está soportada.";
    }
}
