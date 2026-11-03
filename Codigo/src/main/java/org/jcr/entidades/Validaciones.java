package org.jcr.entidades;

class Validaciones {

    static String textoObligatorio(String valor, String mensajeError) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor;
    }

    static void noNulo(Object valor, String mensajeError) {
        if (valor == null) {
            throw new IllegalArgumentException(mensajeError);
        }
    }

    static boolean sonDigitos(String texto) {
        for (char c : texto.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
