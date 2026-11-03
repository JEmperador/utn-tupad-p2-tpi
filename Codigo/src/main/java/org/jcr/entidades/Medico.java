package org.jcr.entidades;

import org.jcr.enums.EspecialidadMedica;
import org.jcr.enums.TipoSangre;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Medico extends Persona {

    private Matricula matricula;
    private EspecialidadMedica especialidad;
    private Departamento departamento;
    private final ArrayList<Cita> citas = new ArrayList<>();

    public Medico(String nombre, String apellido, String dni,
                  LocalDate fechaNacimiento, TipoSangre tipoSangre,
                  String numeroMatricula, EspecialidadMedica especialidad) {
        super(nombre, apellido, dni, fechaNacimiento, tipoSangre);
        Validaciones.noNulo(especialidad, "La especialidad es obligatoria");
        this.matricula = new Matricula(numeroMatricula);
        this.especialidad = especialidad;
    }

    public void addCita(Cita cita) {
        citas.add(cita);
    }

    public void setDepartamento(Departamento nuevo) {
        if (this.departamento == nuevo) {
            return;
        }
        if (nuevo != null && nuevo.getEspecialidad() != this.especialidad) {
            throw new IllegalArgumentException(
                    "La especialidad del médico no coincide con la del departamento");
        }
        Departamento anterior = this.departamento;
        this.departamento = nuevo;
        if (anterior != null) {
            anterior.quitarMedico(this);
        }
        if (nuevo != null) {
            nuevo.agregarMedico(this);
        }
    }

    public Matricula getMatricula() {
        return matricula;
    }

    public EspecialidadMedica getEspecialidad() {
        return especialidad;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public List<Cita> getCitas() {
        return Collections.unmodifiableList(citas);
    }
}
