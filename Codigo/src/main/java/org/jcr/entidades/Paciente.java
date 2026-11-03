package org.jcr.entidades;

import org.jcr.enums.TipoSangre;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Paciente extends Persona {

    private String telefono;
    private String direccion;
    private HistoriaClinica historiaClinica;
    private Hospital hospital;
    private final ArrayList<Cita> citas = new ArrayList<>();

    public Paciente(String nombre, String apellido, String dni,
                    LocalDate fechaNacimiento, TipoSangre tipoSangre,
                    String telefono, String direccion) {
        super(nombre, apellido, dni, fechaNacimiento, tipoSangre);
        this.telefono = Validaciones.textoObligatorio(telefono, "El teléfono es obligatorio");
        this.direccion = Validaciones.textoObligatorio(direccion, "La dirección es obligatoria");
        this.historiaClinica = new HistoriaClinica(this);
    }

    public void addCita(Cita cita) {
        citas.add(cita);
    }

    public void setHospital(Hospital nuevo) {
        if (this.hospital == nuevo) {
            return;
        }
        Hospital anterior = this.hospital;
        this.hospital = nuevo;
        if (anterior != null) {
            anterior.quitarPaciente(this);
        }
        if (nuevo != null) {
            nuevo.agregarPaciente(this);
        }
    }

    public HistoriaClinica getHistoriaClinica() {
        return historiaClinica;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public List<Cita> getCitas() {
        return Collections.unmodifiableList(citas);
    }
}
