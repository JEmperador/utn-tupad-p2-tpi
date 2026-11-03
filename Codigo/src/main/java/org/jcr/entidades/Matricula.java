package org.jcr.entidades;

public class Matricula {

    private final String numero;

    public Matricula(String numero) {
        validarMatricula(numero);
        this.numero = numero;
    }

    public String getNumero() {
        return numero;
    }

    private static void validarMatricula(String numero) {
        if (numero == null || !numero.startsWith("MP-")) {
            throw new IllegalArgumentException("Matrícula inválida: " + numero);
        }
        String digitos = numero.substring(3);
        if (digitos.length() < 4 || digitos.length() > 6 || !Validaciones.sonDigitos(digitos)) {
            throw new IllegalArgumentException("Matrícula inválida: " + numero);
        }
    }
}
