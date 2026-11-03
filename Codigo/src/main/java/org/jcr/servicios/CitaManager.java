package org.jcr.servicios;

import org.jcr.entidades.Cita;
import org.jcr.entidades.Medico;
import org.jcr.entidades.Paciente;
import org.jcr.entidades.Sala;
import org.jcr.excepciones.CitaException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class CitaManager implements CitaService {

    static final int HORAS_ENTRE_CITAS = 2;

    private final ArrayList<Cita> citas = new ArrayList<>();

    @Override
    public Cita programarCita(Paciente paciente, Medico medico, Sala sala,
                              LocalDateTime fechaHora, double costo) throws CitaException {
        if (paciente == null) {
            throw new CitaException("El paciente es obligatorio");
        }
        if (medico == null) {
            throw new CitaException("El médico es obligatorio");
        }
        if (sala == null) {
            throw new CitaException("La sala es obligatoria");
        }
        if (fechaHora == null) {
            throw new CitaException("La fecha es obligatoria");
        }
        if (fechaHora.isBefore(LocalDateTime.now())) {
            throw new CitaException("La fecha ya pasó");
        }
        if (costo <= 0) {
            throw new CitaException("El costo debe ser mayor a 0");
        }
        if (sala.getDepartamento() == null
                || medico.getEspecialidad() != sala.getDepartamento().getEspecialidad()) {
            throw new CitaException("La especialidad del médico no coincide con la de la sala");
        }
        if (!esMedicoDisponible(medico, fechaHora)) {
            throw new CitaException("El médico no está disponible en ese horario");
        }
        if (!esSalaDisponible(sala, fechaHora)) {
            throw new CitaException("La sala no está disponible en ese horario");
        }

        Cita cita = new Cita(paciente, medico, sala, fechaHora, costo);
        citas.add(cita);
        paciente.addCita(cita);
        medico.addCita(cita);
        sala.addCita(cita);
        return cita;
    }

    boolean esMedicoDisponible(Medico medico, LocalDateTime nueva) {
        for (Cita c : medico.getCitas()) {
            if (seSuperponen(c.getFechaHora(), nueva)) {
                return false;
            }
        }
        return true;
    }

    boolean esSalaDisponible(Sala sala, LocalDateTime nueva) {
        for (Cita c : sala.getCitas()) {
            if (seSuperponen(c.getFechaHora(), nueva)) {
                return false;
            }
        }
        return true;
    }

    boolean seSuperponen(LocalDateTime existente, LocalDateTime nueva) {
        long minutos = Math.abs(ChronoUnit.MINUTES.between(existente, nueva));
        return minutos < HORAS_ENTRE_CITAS * 60L;
    }

    @Override
    public List<Cita> getCitasPorPaciente(Paciente paciente) {
        ArrayList<Cita> resultado = new ArrayList<>();
        for (Cita c : citas) {
            if (c.getPaciente() == paciente) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    @Override
    public List<Cita> getCitasPorMedico(Medico medico) {
        ArrayList<Cita> resultado = new ArrayList<>();
        for (Cita c : citas) {
            if (c.getMedico() == medico) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    @Override
    public List<Cita> getCitasPorSala(Sala sala) {
        ArrayList<Cita> resultado = new ArrayList<>();
        for (Cita c : citas) {
            if (c.getSala() == sala) {
                resultado.add(c);
            }
        }
        return resultado;
    }
}
