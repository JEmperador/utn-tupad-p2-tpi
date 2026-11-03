package org.jcr.entidades;

import org.jcr.enums.EspecialidadMedica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Departamento {

    private String nombre;
    private EspecialidadMedica especialidad;
    private Hospital hospital;
    private final ArrayList<Medico> medicos = new ArrayList<>();
    private final ArrayList<Sala> salas = new ArrayList<>();

    public Departamento(String nombre, EspecialidadMedica especialidad) {
        this.nombre = Validaciones.textoObligatorio(nombre, "El nombre es obligatorio");
        Validaciones.noNulo(especialidad, "La especialidad es obligatoria");
        this.especialidad = especialidad;
    }

    public void agregarMedico(Medico m) {
        if (m == null) {
            return;
        }
        if (m.getEspecialidad() != this.especialidad) {
            throw new IllegalArgumentException(
                    "La especialidad del médico no coincide con la del departamento");
        }
        if (!medicos.contains(m)) {
            medicos.add(m);
            m.setDepartamento(this);
        }
    }

    void quitarMedico(Medico m) {
        medicos.remove(m);
    }

    public Sala crearSala(String numero, String tipo) {
        Sala sala = new Sala(numero, tipo, this);
        salas.add(sala);
        return sala;
    }

    public void setHospital(Hospital nuevo) {
        if (this.hospital == nuevo) {
            return;
        }
        Hospital anterior = this.hospital;
        this.hospital = nuevo;
        if (anterior != null) {
            anterior.quitarDepartamento(this);
        }
        if (nuevo != null) {
            nuevo.agregarDepartamento(this);
        }
    }

    public String getNombre() {
        return nombre;
    }

    public EspecialidadMedica getEspecialidad() {
        return especialidad;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public List<Medico> getMedicos() {
        return Collections.unmodifiableList(medicos);
    }

    public List<Sala> getSalas() {
        return Collections.unmodifiableList(salas);
    }
}
