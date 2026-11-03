package org.jcr.entidades;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoriaClinica {

    private final Paciente paciente;
    private final LocalDateTime fechaCreacion;
    private final ArrayList<String> diagnosticos = new ArrayList<>();
    private final ArrayList<String> tratamientos = new ArrayList<>();
    private final ArrayList<String> alergias = new ArrayList<>();

    HistoriaClinica(Paciente paciente) {
        this.paciente = paciente;
        this.fechaCreacion = LocalDateTime.now();
    }

    public String getNumeroHistoria() {
        return "HC-" + paciente.getDni() + "-" + fechaCreacion.getYear();
    }

    public void agregarDiagnostico(String d) {
        if (d != null && !d.trim().isEmpty()) {
            diagnosticos.add(d);
        }
    }

    public void agregarTratamiento(String t) {
        if (t != null && !t.trim().isEmpty()) {
            tratamientos.add(t);
        }
    }

    public void agregarAlergia(String a) {
        if (a != null && !a.trim().isEmpty()) {
            alergias.add(a);
        }
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public List<String> getDiagnosticos() {
        return Collections.unmodifiableList(diagnosticos);
    }

    public List<String> getTratamientos() {
        return Collections.unmodifiableList(tratamientos);
    }

    public List<String> getAlergias() {
        return Collections.unmodifiableList(alergias);
    }
}
