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
            throw new Exception("Error físico: El archivo base_conocimiento.pl no está en el proyecto.");
        }

        String path = plFile.getAbsolutePath().replace("\\", "/");
        Query consult = new Query("consult('" + path + "')");
        if (!consult.hasSolution()) {
            throw new Exception("No se pudo cargar el archivo Prolog.");
        }
    }

    public String resolver(String expresion, String a, String b) throws Exception {
        String queryStr = String.format("resolver_integral(%s, %s, %s, Expr, Area)", expresion, a, b);
        Query consultaProlog = new Query(queryStr);

        if (consultaProlog.hasSolution()) {
            Map<String, Term> solucion = consultaProlog.oneSolution();
            return "Expresión integrada:\n" + solucion.get("Expr").toString() +
                    "\n\nÁrea bajo la curva:\n" + solucion.get("Area").toString();
        } else {
            return "Sin solución para esa expresión";
        }
    }
}