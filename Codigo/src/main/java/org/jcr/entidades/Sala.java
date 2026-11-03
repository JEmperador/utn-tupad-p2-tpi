package org.jcr.entidades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sala {

    private final String numero;
    private final String tipo;
    private final Departamento departamento;
    private final ArrayList<Cita> citas = new ArrayList<>();

    Sala(String numero, String tipo, Departamento departamento) {
        this.numero = Validaciones.textoObligatorio(numero, "El número de sala es obligatorio");
        this.tipo = Validaciones.textoObligatorio(tipo, "El tipo de sala es obligatorio");
        this.departamento = departamento;
    }

    public void addCita(Cita cita) {
        citas.add(cita);
    }

    public List<Cita> getCitas() {
        return Collections.unmodifiableList(citas);
    }

    public String getNumero() {
        return numero;
    }

    public String getTipo() {
        return tipo;
    }

    public Departamento getDepartamento() {
        return departamento;
    }
}
