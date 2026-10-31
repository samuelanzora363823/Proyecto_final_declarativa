package org.example;

/**
 * Lo que la base de conocimiento devuelve para una integral definida.
 *
 * Se guardan por separado la integral definida y el area bajo la curva porque
 * no son lo mismo: la integral de sin(x) entre 0 y 2*pi vale 0, pero el area
 * encerrada entre la curva y el eje es 4. El enunciado pide el area, asi que
 * la interfaz muestra ambos valores.
 */
public record ResultadoIntegral(
        String antiderivada,
        double integralDefinida,
        double areaBajoCurva) {
}
