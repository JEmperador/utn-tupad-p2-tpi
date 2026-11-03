package org.jcr.entidades;

import org.jcr.enums.TipoSangre;

import java.time.LocalDate;

public abstract class Persona {

    protected String nombre;
    protected String apellido;
    protected String dni;
    protected LocalDate fechaNacimiento;
    protected TipoSangre tipoSangre;

    public Persona(String nombre, String apellido, String dni,
                   LocalDate fechaNacimiento, TipoSangre tipoSangre) {
        this.nombre = Validaciones.textoObligatorio(nombre, "El nombre es obligatorio");
        this.apellido = Validaciones.textoObligatorio(apellido, "El apellido es obligatorio");
        this.dni = validarDni(dni);
        Validaciones.noNulo(fechaNacimiento, "La fecha de nacimiento es obligatoria");
        Validaciones.noNulo(tipoSangre, "El tipo de sangre es obligatorio");
        this.fechaNacimiento = fechaNacimiento;
        this.tipoSangre = tipoSangre;
    }

    private static String validarDni(String dni) {
        if (dni == null || (dni.length() != 7 && dni.length() != 8)
                || !Validaciones.sonDigitos(dni)) {
            throw new IllegalArgumentException("DNI inválido: " + dni);
        }
        return dni;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public int getEdad() {
        LocalDate hoy = LocalDate.now();
        int anos = hoy.getYear() - fechaNacimiento.getYear();
        if (hoy.isBefore(fechaNacimiento.plusYears(anos))) {
            anos--;
        }
        return anos;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getDni() {
        return dni;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public TipoSangre getTipoSangre() {
        return tipoSangre;
    }
}
