package org.jcr.entidades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hospital {

    private String nombre;
    private String direccion;
    private String telefono;
    private final ArrayList<Departamento> departamentos = new ArrayList<>();
    private final ArrayList<Paciente> pacientes = new ArrayList<>();

    public Hospital(String nombre, String direccion, String telefono) {
        this.nombre = Validaciones.textoObligatorio(nombre, "El nombre es obligatorio");
        this.direccion = Validaciones.textoObligatorio(direccion, "La dirección es obligatoria");
        this.telefono = Validaciones.textoObligatorio(telefono, "El teléfono es obligatorio");
    }

    public void agregarDepartamento(Departamento d) {
        if (d != null && !departamentos.contains(d)) {
            departamentos.add(d);
            d.setHospital(this);
        }
    }

    void quitarDepartamento(Departamento d) {
        departamentos.remove(d);
    }

    public void agregarPaciente(Paciente p) {
        if (p != null && !pacientes.contains(p)) {
            pacientes.add(p);
            p.setHospital(this);
        }
    }

    void quitarPaciente(Paciente p) {
        pacientes.remove(p);
    }

    public List<Departamento> getDepartamentos() {
        return Collections.unmodifiableList(departamentos);
    }

    public List<Paciente> getPacientes() {
        return Collections.unmodifiableList(pacientes);
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }
}
