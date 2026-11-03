package org.jcr;

import org.jcr.entidades.Cita;
import org.jcr.entidades.Departamento;
import org.jcr.entidades.HistoriaClinica;
import org.jcr.entidades.Hospital;
import org.jcr.entidades.Matricula;
import org.jcr.entidades.Medico;
import org.jcr.entidades.Paciente;
import org.jcr.entidades.Persona;
import org.jcr.entidades.Sala;
import org.jcr.enums.EspecialidadMedica;
import org.jcr.enums.EstadoCita;
import org.jcr.enums.TipoSangre;
import org.jcr.excepciones.CitaException;
import org.jcr.servicios.CitaManager;
import org.jcr.servicios.CitaService;

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Pruebas del TPI "Sistema de Gestión Hospitalaria".
 *
 * El trabajo se considera funcionando cuando este archivo compila sin cambios contra el
 * proyecto y, al ejecutarlo, todas las verificaciones dan [OK].
 *
 * NO se debe modificar este archivo: se corrige ejecutándolo tal cual.
 */
public class MainPruebas {

    private static final int CANTIDAD_DE_GRUPOS = 10;

    private static int correctas = 0;
    private static int fallidas = 0;

    public static void main(String[] args) {
        System.out.println("===== PRUEBAS DEL SISTEMA DE GESTIÓN HOSPITALARIA =====");

        for (int grupo = 1; grupo <= CANTIDAD_DE_GRUPOS; grupo++) {
            // Si un grupo lanza una excepción que no se esperaba, se cuenta como falla
            // y se sigue con el próximo grupo.
            try {
                ejecutarGrupo(grupo);
            } catch (Exception e) {
                fallo("El grupo " + grupo + " se interrumpió por una excepción inesperada: "
                        + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
        }

        int total = correctas + fallidas;
        System.out.println();
        System.out.println("===== RESULTADO: " + correctas + " de " + total + " verificaciones correctas =====");
        if (fallidas == 0) {
            System.out.println("Todas las pruebas pasaron.");
        } else {
            if (fallidas == 1) {
                System.out.println("Hay 1 verificación con [FALLA]. Revisarla arriba.");
            } else {
                System.out.println("Hay " + fallidas + " verificaciones con [FALLA]. Revisarlas arriba.");
            }
            System.exit(1);
        }
    }

    private static void ejecutarGrupo(int grupo) throws Exception {
        switch (grupo) {
            case 1:
                probarEnumeraciones();
                break;
            case 2:
                probarPersonaYHerencia();
                break;
            case 3:
                probarValidacionesDePersona();
                break;
            case 4:
                probarMatricula();
                break;
            case 5:
                probarHistoriaClinica();
                break;
            case 6:
                probarHospitalYRelacionesBidireccionales();
                break;
            case 7:
                probarDepartamentoMedicosYSalas();
                break;
            case 8:
                probarCita();
                break;
            case 9:
                probarProgramacionDeCitas();
                break;
            case 10:
                probarReglasDeDisponibilidad();
                break;
        }
    }

    // ================================================================
    // 1. Enumeraciones
    // ================================================================
    private static void probarEnumeraciones() {
        titulo("1. Enumeraciones");
        verificar(TipoSangre.values().length == 8, "TipoSangre tiene 8 valores");
        verificar("A+".equals(TipoSangre.A_POSITIVO.getDescripcion()), "TipoSangre.A_POSITIVO se describe como \"A+\"");
        verificar("O-".equals(TipoSangre.O_NEGATIVO.getDescripcion()), "TipoSangre.O_NEGATIVO se describe como \"O-\"");
        verificar(EspecialidadMedica.values().length == 12, "EspecialidadMedica tiene 12 valores");
        verificar("Cardiología".equals(EspecialidadMedica.CARDIOLOGIA.getDescripcion()),
                "EspecialidadMedica.CARDIOLOGIA se describe como \"Cardiología\"");
        verificar(EstadoCita.values().length == 5, "EstadoCita tiene 5 valores");
        verificar("No Asistió".equals(EstadoCita.NO_ASISTIO.getDescripcion()),
                "EstadoCita.NO_ASISTIO se describe como \"No Asistió\"");
    }

    // ================================================================
    // 2. Persona, Paciente y Medico: herencia y polimorfismo
    // ================================================================
    private static void probarPersonaYHerencia() {
        titulo("2. Persona, Paciente y Medico (herencia)");
        // Esta línea consulta si la clase fue declarada abstract (no hace falta entender cómo)
        verificar(Modifier.isAbstract(Persona.class.getModifiers()), "Persona es una clase abstracta");

        Paciente paciente = nuevoPaciente("11111111");
        Medico medico = nuevoMedico("22222222", "MP-1234", EspecialidadMedica.CARDIOLOGIA);

        // Polimorfismo: un Paciente y un Medico se pueden tratar como Persona
        Persona personaPaciente = paciente;
        Persona personaMedico = medico;
        verificar("María López".equals(personaPaciente.getNombreCompleto()),
                "getNombreCompleto() de un Paciente devuelve \"María López\"");
        verificar("Carlos González".equals(personaMedico.getNombreCompleto()),
                "getNombreCompleto() de un Medico devuelve \"Carlos González\"");
        verificar("11111111".equals(paciente.getDni()), "El Paciente conserva su DNI");
        verificar(paciente.getTipoSangre() == TipoSangre.A_POSITIVO, "El Paciente conserva su tipo de sangre");
        verificar("011-1111-1111".equals(paciente.getTelefono()), "El Paciente conserva su teléfono");
        verificar(medico.getEspecialidad() == EspecialidadMedica.CARDIOLOGIA, "El Medico conserva su especialidad");
        verificar("MP-1234".equals(medico.getMatricula().getNumero()), "El Medico crea su Matricula con el número indicado");

        // Edad: se cuenta si ya cumplió años este año
        Paciente cumpleHoy = nuevoPacienteNacidoEl(LocalDate.now().minusYears(30), "33333333");
        Paciente cumpleManiana = nuevoPacienteNacidoEl(LocalDate.now().minusYears(30).plusDays(1), "44444444");
        verificar(cumpleHoy.getEdad() == 30, "Quien cumple 30 años hoy tiene 30 años");
        verificar(cumpleManiana.getEdad() == 29, "Quien cumple 30 años mañana todavía tiene 29 años");
    }

    // ================================================================
    // 3. Validaciones de los datos de una persona
    // ================================================================
    private static void probarValidacionesDePersona() {
        titulo("3. Validaciones de Persona y Paciente");
        LocalDate nacimiento = LocalDate.of(1985, 12, 5);

        try {
            new Paciente("", "López", "11111111", nacimiento, TipoSangre.A_POSITIVO, "011", "Calle 1");
            fallo("Un nombre vacío debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ok("Un nombre vacío lanza IllegalArgumentException");
        }
        try {
            new Paciente("María", "   ", "11111111", nacimiento, TipoSangre.A_POSITIVO, "011", "Calle 1");
            fallo("Un apellido formado solo por espacios debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ok("Un apellido formado solo por espacios lanza IllegalArgumentException");
        }
        try {
            new Paciente(null, "López", "11111111", nacimiento, TipoSangre.A_POSITIVO, "011", "Calle 1");
            fallo("Un nombre nulo debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ok("Un nombre nulo lanza IllegalArgumentException");
        }
        try {
            new Paciente("María", "López", "123456", nacimiento, TipoSangre.A_POSITIVO, "011", "Calle 1");
            fallo("Un DNI de 6 dígitos debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ok("Un DNI de 6 dígitos lanza IllegalArgumentException");
        }
        try {
            new Paciente("María", "López", "123456789", nacimiento, TipoSangre.A_POSITIVO, "011", "Calle 1");
            fallo("Un DNI de 9 dígitos debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ok("Un DNI de 9 dígitos lanza IllegalArgumentException");
        }
        try {
            new Paciente("María", "López", "1234567A", nacimiento, TipoSangre.A_POSITIVO, "011", "Calle 1");
            fallo("Un DNI con letras debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ok("Un DNI con letras lanza IllegalArgumentException");
        }
        try {
            Paciente dniDeSiete = new Paciente("María", "López", "1234567", nacimiento,
                    TipoSangre.A_POSITIVO, "011", "Calle 1");
            verificar("1234567".equals(dniDeSiete.getDni()), "Un DNI de 7 dígitos es válido");
        } catch (IllegalArgumentException e) {
            fallo("Un DNI de 7 dígitos debería ser válido");
        }
        try {
            new Paciente("María", "López", "11111111", null, TipoSangre.A_POSITIVO, "011", "Calle 1");
            fallo("Una fecha de nacimiento nula debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ok("Una fecha de nacimiento nula lanza IllegalArgumentException");
        }
        try {
            new Paciente("María", "López", "11111111", nacimiento, null, "011", "Calle 1");
            fallo("Un tipo de sangre nulo debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ok("Un tipo de sangre nulo lanza IllegalArgumentException");
        }
        try {
            new Paciente("María", "López", "11111111", nacimiento, TipoSangre.A_POSITIVO, "", "Calle 1");
            fallo("Un teléfono vacío debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ok("Un teléfono vacío lanza IllegalArgumentException");
        }
        try {
            new Paciente("María", "López", "11111111", nacimiento, TipoSangre.A_POSITIVO, "011", " ");
            fallo("Una dirección vacía debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ok("Una dirección vacía lanza IllegalArgumentException");
        }
        try {
            new Medico("Carlos", "González", "22222222", nacimiento, TipoSangre.A_POSITIVO, "MP-1234", null);
            fallo("Un médico sin especialidad debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ok("Un médico sin especialidad lanza IllegalArgumentException");
        }
    }

    // ================================================================
    // 4. Matrícula
    // ================================================================
    private static void probarMatricula() {
        titulo("4. Matrícula (formato MP- seguido de 4 a 6 dígitos)");
        verificar(matriculaEsValida("MP-1234"), "MP-1234 es válida (4 dígitos)");
        verificar(matriculaEsValida("MP-123456"), "MP-123456 es válida (6 dígitos)");
        verificar(!matriculaEsValida("MP-123"), "MP-123 se rechaza (3 dígitos)");
        verificar(!matriculaEsValida("MP-1234567"), "MP-1234567 se rechaza (7 dígitos)");
        verificar(!matriculaEsValida("12345"), "12345 se rechaza (falta el prefijo MP-)");
        verificar(!matriculaEsValida("MP-12A45"), "MP-12A45 se rechaza (tiene una letra)");
        verificar(!matriculaEsValida(null), "Una matrícula nula se rechaza");
        try {
            new Medico("Carlos", "González", "22222222", LocalDate.of(1975, 5, 15),
                    TipoSangre.A_POSITIVO, "XX-1", EspecialidadMedica.CARDIOLOGIA);
            fallo("Un médico con matrícula inválida debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ok("Un médico con matrícula inválida lanza IllegalArgumentException");
        }
    }

    // ================================================================
    // 5. Historia clínica (composición con Paciente)
    // ================================================================
    private static void probarHistoriaClinica() {
        titulo("5. Historia clínica");
        Paciente paciente = nuevoPaciente("11111111");
        HistoriaClinica historia = paciente.getHistoriaClinica();

        verificar(historia != null, "Cada Paciente tiene su historia clínica desde que se crea");
        verificar(historia != null && historia.getPaciente() == paciente, "La historia clínica conoce a su paciente");
        String esperado = "HC-11111111-" + LocalDate.now().getYear();
        verificar(historia != null && esperado.equals(historia.getNumeroHistoria()),
                "El número de historia tiene el formato HC-DNI-AÑO (" + esperado + ")");

        historia.agregarDiagnostico("Hipertensión arterial");
        historia.agregarTratamiento("Enalapril 10mg");
        historia.agregarAlergia("Penicilina");
        historia.agregarAlergia("Ibuprofeno");
        verificar(historia.getDiagnosticos().size() == 1, "Se registra un diagnóstico");
        verificar(historia.getTratamientos().size() == 1, "Se registra un tratamiento");
        verificar(historia.getAlergias().size() == 2, "Se registran dos alergias");

        historia.agregarDiagnostico("");
        historia.agregarDiagnostico(null);
        historia.agregarAlergia("   ");
        verificar(historia.getDiagnosticos().size() == 1, "Un diagnóstico vacío o nulo no se registra");
        verificar(historia.getAlergias().size() == 2, "Una alergia vacía no se registra");

        try {
            historia.getDiagnosticos().add("Diagnóstico agregado desde afuera");
        } catch (UnsupportedOperationException e) {
            // Devolver una lista no modificable es una forma válida de protegerla
        }
        verificar(historia.getDiagnosticos().size() == 1,
                "La lista de diagnósticos no se puede modificar desde afuera de la historia");
    }

    // ================================================================
    // 6. Hospital: relaciones bidireccionales con Departamento y Paciente
    // ================================================================
    private static void probarHospitalYRelacionesBidireccionales() {
        titulo("6. Hospital y relaciones bidireccionales");
        try {
            new Hospital("", "Av. Libertador 1234", "011-4567-8901");
            fallo("Un hospital sin nombre debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ok("Un hospital sin nombre lanza IllegalArgumentException");
        }

        Hospital central = new Hospital("Hospital Central", "Av. Libertador 1234", "011-4567-8901");
        Hospital norte = new Hospital("Hospital Norte", "Ruta 9 km 20", "011-9999-0000");
        Departamento cardiologia = new Departamento("Cardiología", EspecialidadMedica.CARDIOLOGIA);
        Paciente paciente = nuevoPaciente("11111111");

        central.agregarDepartamento(cardiologia);
        verificar(central.getDepartamentos().size() == 1, "Agregar un departamento lo deja una sola vez en el hospital");
        verificar(cardiologia.getHospital() == central, "El departamento conoce a su hospital");
        central.agregarDepartamento(cardiologia);
        verificar(central.getDepartamentos().size() == 1, "Agregar dos veces el mismo departamento no lo duplica");

        central.agregarPaciente(paciente);
        verificar(central.getPacientes().size() == 1, "Agregar un paciente lo deja una sola vez en el hospital");
        verificar(paciente.getHospital() == central, "El paciente conoce a su hospital");
        central.agregarPaciente(paciente);
        verificar(central.getPacientes().size() == 1, "Agregar dos veces el mismo paciente no lo duplica");

        // Cambiar de hospital desde la otra punta de la relación
        paciente.setHospital(norte);
        verificar(central.getPacientes().isEmpty(), "Al pasar el paciente a otro hospital, sale del anterior");
        verificar(norte.getPacientes().size() == 1 && norte.getPacientes().get(0) == paciente,
                "Al pasar el paciente a otro hospital, entra en el nuevo");

        cardiologia.setHospital(norte);
        verificar(central.getDepartamentos().isEmpty() && norte.getDepartamentos().size() == 1,
                "Al pasar el departamento a otro hospital, cambia de lista");

        try {
            norte.getPacientes().add(nuevoPaciente("55555555"));
        } catch (UnsupportedOperationException e) {
            // Devolver una lista no modificable es una forma válida de protegerla
        }
        verificar(norte.getPacientes().size() == 1,
                "La lista de pacientes no se puede modificar desde afuera del hospital");
    }

    // ================================================================
    // 7. Departamento: médicos (bidireccional) y salas (composición)
    // ================================================================
    private static void probarDepartamentoMedicosYSalas() {
        titulo("7. Departamento, médicos y salas");
        Departamento cardiologia = new Departamento("Cardiología", EspecialidadMedica.CARDIOLOGIA);
        Departamento cardiologiaInfantil = new Departamento("Cardiología Infantil", EspecialidadMedica.CARDIOLOGIA);
        Departamento pediatria = new Departamento("Pediatría", EspecialidadMedica.PEDIATRIA);
        Medico cardiologo = nuevoMedico("22222222", "MP-1234", EspecialidadMedica.CARDIOLOGIA);

        cardiologia.agregarMedico(cardiologo);
        verificar(cardiologia.getMedicos().size() == 1, "Agregar un médico lo deja una sola vez en el departamento");
        verificar(cardiologo.getDepartamento() == cardiologia, "El médico conoce a su departamento");
        cardiologia.agregarMedico(cardiologo);
        verificar(cardiologia.getMedicos().size() == 1, "Agregar dos veces el mismo médico no lo duplica");

        try {
            pediatria.agregarMedico(cardiologo);
            fallo("Agregar un cardiólogo a Pediatría debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ok("Agregar un médico a un departamento de otra especialidad lanza IllegalArgumentException");
        }
        verificar(pediatria.getMedicos().isEmpty() && cardiologo.getDepartamento() == cardiologia,
                "El médico rechazado no cambia de departamento");

        cardiologo.setDepartamento(cardiologiaInfantil);
        verificar(cardiologia.getMedicos().isEmpty() && cardiologiaInfantil.getMedicos().size() == 1,
                "Al pasar el médico a otro departamento, sale del anterior y entra en el nuevo");

        Sala sala = cardiologia.crearSala("CARD-101", "Consultorio");
        verificar(cardiologia.getSalas().size() == 1, "crearSala() agrega la sala al departamento");
        verificar(sala.getDepartamento() == cardiologia, "La sala conoce a su departamento");
        verificar("CARD-101".equals(sala.getNumero()) && "Consultorio".equals(sala.getTipo()),
                "La sala conserva su número y su tipo");
        try {
            cardiologia.crearSala("", "Consultorio");
            fallo("Una sala sin número debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ok("Una sala sin número lanza IllegalArgumentException");
        }
        verificar(cardiologia.getSalas().size() == 1, "La sala inválida no queda agregada");
        try {
            cardiologia.getSalas().add(sala);
        } catch (UnsupportedOperationException e) {
            // Devolver una lista no modificable es una forma válida de protegerla
        }
        verificar(cardiologia.getSalas().size() == 1,
                "La lista de salas no se puede modificar desde afuera del departamento");
    }

    // ================================================================
    // 8. Cita
    // ================================================================
    private static void probarCita() {
        titulo("8. Cita");
        Departamento cardiologia = new Departamento("Cardiología", EspecialidadMedica.CARDIOLOGIA);
        Sala sala = cardiologia.crearSala("CARD-101", "Consultorio");
        Paciente paciente = nuevoPaciente("11111111");
        Medico medico = nuevoMedico("22222222", "MP-1234", EspecialidadMedica.CARDIOLOGIA);
        LocalDateTime fecha = fechaFutura(7, 10);

        Cita cita = new Cita(paciente, medico, sala, fecha, 150000.0);
        verificar(cita.getEstado() == EstadoCita.PROGRAMADA, "Una cita nueva empieza en estado PROGRAMADA");
        verificar("".equals(cita.getObservaciones()), "Una cita nueva empieza sin observaciones (texto vacío)");
        verificar(cita.getCosto() == 150000.0, "La cita conserva su costo");
        verificar(cita.getFechaHora().equals(fecha), "La cita conserva su fecha y hora");

        cita.setEstado(EstadoCita.COMPLETADA);
        verificar(cita.getEstado() == EstadoCita.COMPLETADA, "Se puede cambiar el estado de la cita");
        try {
            cita.setEstado(null);
            fallo("Un estado nulo debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ok("Un estado nulo lanza IllegalArgumentException");
        }
        verificar(cita.getEstado() == EstadoCita.COMPLETADA, "El estado rechazado no cambia el estado anterior");

        cita.setObservaciones("Paciente con antecedentes de hipertensión");
        verificar("Paciente con antecedentes de hipertensión".equals(cita.getObservaciones()),
                "Se pueden cargar observaciones");
        cita.setObservaciones(null);
        verificar("".equals(cita.getObservaciones()), "Observaciones nulas quedan como texto vacío");

        try {
            new Cita(null, medico, sala, fecha, 1000.0);
            fallo("Una cita sin paciente debería lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ok("Una cita sin paciente lanza IllegalArgumentException");
        }
    }

    // ================================================================
    // 9. Programación de citas con CitaService / CitaManager
    // ================================================================
    private static void probarProgramacionDeCitas() throws CitaException {
        titulo("9. Programación de citas (CitaService)");
        Exception excepcion = new CitaException("prueba");
        verificar(!(excepcion instanceof RuntimeException),
                "CitaException es una excepción verificada (hereda de Exception, no de RuntimeException)");

        Departamento cardiologia = new Departamento("Cardiología", EspecialidadMedica.CARDIOLOGIA);
        Departamento pediatria = new Departamento("Pediatría", EspecialidadMedica.PEDIATRIA);
        Sala salaCardio = cardiologia.crearSala("CARD-101", "Consultorio");
        Sala salaPediatria = pediatria.crearSala("PED-201", "Consultorio");
        Medico cardiologo = nuevoMedico("22222222", "MP-1234", EspecialidadMedica.CARDIOLOGIA);
        cardiologia.agregarMedico(cardiologo);
        Paciente paciente = nuevoPaciente("11111111");
        Paciente otroPaciente = nuevoPaciente("33333333");

        // Se programa a través de la interfaz: CitaManager es una implementación de CitaService
        CitaService servicio = new CitaManager();
        verificar(servicio.getCitasPorPaciente(paciente) != null && servicio.getCitasPorPaciente(paciente).isEmpty(),
                "Un paciente sin citas devuelve una lista vacía (no null)");

        Cita cita = servicio.programarCita(paciente, cardiologo, salaCardio, fechaFutura(7, 10), 150000.0);
        verificar(cita != null && cita.getEstado() == EstadoCita.PROGRAMADA, "Se programa una cita válida");
        verificar(servicio.getCitasPorPaciente(paciente).size() == 1, "La cita aparece en las citas del paciente (servicio)");
        verificar(servicio.getCitasPorMedico(cardiologo).size() == 1, "La cita aparece en las citas del médico (servicio)");
        verificar(servicio.getCitasPorSala(salaCardio).size() == 1, "La cita aparece en las citas de la sala (servicio)");
        verificar(paciente.getCitas().size() == 1 && paciente.getCitas().get(0) == cita,
                "El paciente registra su cita");
        verificar(cardiologo.getCitas().size() == 1, "El médico registra su cita");
        verificar(salaCardio.getCitas().size() == 1, "La sala registra su cita");
        verificar(servicio.getCitasPorPaciente(otroPaciente).isEmpty(), "Las citas de un paciente no aparecen en otro");

        try {
            servicio.programarCita(paciente, cardiologo, salaCardio, LocalDateTime.now().minusDays(1), 1000.0);
            fallo("Una cita en el pasado debería lanzar CitaException");
        } catch (CitaException e) {
            ok("Una cita en el pasado lanza CitaException");
        }
        try {
            servicio.programarCita(paciente, cardiologo, salaCardio, fechaFutura(20, 10), 0);
            fallo("Una cita con costo 0 debería lanzar CitaException");
        } catch (CitaException e) {
            ok("Una cita con costo 0 lanza CitaException");
        }
        try {
            servicio.programarCita(paciente, cardiologo, salaCardio, fechaFutura(20, 10), -500.0);
            fallo("Una cita con costo negativo debería lanzar CitaException");
        } catch (CitaException e) {
            ok("Una cita con costo negativo lanza CitaException");
        }
        try {
            servicio.programarCita(paciente, cardiologo, salaPediatria, fechaFutura(20, 10), 1000.0);
            fallo("Un cardiólogo en una sala de Pediatría debería lanzar CitaException");
        } catch (CitaException e) {
            ok("Un médico en una sala de otra especialidad lanza CitaException");
        }
        try {
            servicio.programarCita(null, cardiologo, salaCardio, fechaFutura(20, 10), 1000.0);
            fallo("Una cita sin paciente debería lanzar CitaException");
        } catch (CitaException e) {
            ok("Programar una cita sin paciente lanza CitaException");
        }

        verificar(servicio.getCitasPorPaciente(paciente).size() == 1 && paciente.getCitas().size() == 1
                        && cardiologo.getCitas().size() == 1 && salaCardio.getCitas().size() == 1,
                "Las citas rechazadas no quedan registradas en ningún lado");
        int citasAntes = paciente.getCitas().size();
        try {
            paciente.getCitas().add(cita);
        } catch (UnsupportedOperationException e) {
            // Devolver una lista no modificable es una forma válida de protegerla
        }
        verificar(paciente.getCitas().size() == citasAntes,
                "La lista de citas del paciente no se puede modificar desde afuera");
    }

    // ================================================================
    // 10. Disponibilidad: 2 horas entre citas del mismo médico o de la misma sala
    // ================================================================
    private static void probarReglasDeDisponibilidad() throws CitaException {
        titulo("10. Disponibilidad de médicos y salas (2 horas entre citas)");
        Departamento cardiologia = new Departamento("Cardiología", EspecialidadMedica.CARDIOLOGIA);
        Sala sala1 = cardiologia.crearSala("CARD-101", "Consultorio");
        Sala sala2 = cardiologia.crearSala("CARD-102", "Consultorio");
        Medico medicoA = nuevoMedico("22222222", "MP-1234", EspecialidadMedica.CARDIOLOGIA);
        Medico medicoB = nuevoMedico("44444444", "MP-5678", EspecialidadMedica.CARDIOLOGIA);
        Paciente paciente = nuevoPaciente("11111111");
        CitaService servicio = new CitaManager();

        LocalDateTime diezHoras = fechaFutura(7, 10);
        servicio.programarCita(paciente, medicoA, sala1, diezHoras, 1000.0);

        try {
            servicio.programarCita(paciente, medicoA, sala2, diezHoras.plusMinutes(90), 1000.0);
            fallo("El mismo médico 1 h 30 min después debería lanzar CitaException");
        } catch (CitaException e) {
            ok("El mismo médico 1 h 30 min después lanza CitaException");
        }
        try {
            servicio.programarCita(paciente, medicoA, sala2, diezHoras.minusMinutes(90), 1000.0);
            fallo("El mismo médico 1 h 30 min antes debería lanzar CitaException");
        } catch (CitaException e) {
            ok("El mismo médico 1 h 30 min antes lanza CitaException");
        }
        try {
            servicio.programarCita(paciente, medicoB, sala1, diezHoras.plusMinutes(30), 1000.0);
            fallo("La misma sala 30 min después (con otro médico) debería lanzar CitaException");
        } catch (CitaException e) {
            ok("La misma sala 30 min después, con otro médico, lanza CitaException");
        }

        try {
            servicio.programarCita(paciente, medicoA, sala2, diezHoras.plusHours(2), 1000.0);
            ok("El mismo médico exactamente 2 horas después se acepta");
        } catch (CitaException e) {
            fallo("El mismo médico exactamente 2 horas después debería aceptarse: " + e.getMessage());
        }
        try {
            servicio.programarCita(paciente, medicoA, sala1, diezHoras.plusDays(1), 1000.0);
            ok("El mismo médico en la misma sala al día siguiente se acepta");
        } catch (CitaException e) {
            fallo("El mismo médico al día siguiente debería aceptarse: " + e.getMessage());
        }
        try {
            servicio.programarCita(paciente, medicoB, sala2, diezHoras.minusHours(3), 1000.0);
            ok("Otro médico en otra sala, sin superposición, se acepta");
        } catch (CitaException e) {
            fallo("Otro médico en otra sala sin superposición debería aceptarse: " + e.getMessage());
        }

        verificar(servicio.getCitasPorMedico(medicoA).size() == 3, "El médico A quedó con 3 citas");
        verificar(servicio.getCitasPorMedico(medicoB).size() == 1, "El médico B quedó con 1 cita");
        verificar(servicio.getCitasPorSala(sala1).size() == 2, "La sala 1 quedó con 2 citas");
        verificar(servicio.getCitasPorPaciente(paciente).size() == 4, "El paciente quedó con 4 citas");
    }

    // ================================================================
    // Auxiliares de las pruebas
    // ================================================================

    private static Paciente nuevoPaciente(String dni) {
        return new Paciente("María", "López", dni, LocalDate.of(1985, 12, 5),
                TipoSangre.A_POSITIVO, "011-1111-1111", "Calle Falsa 123");
    }

    private static Paciente nuevoPacienteNacidoEl(LocalDate fecha, String dni) {
        return new Paciente("Pedro", "García", dni, fecha,
                TipoSangre.O_POSITIVO, "011-2222-2222", "Av. Siempreviva 456");
    }

    private static Medico nuevoMedico(String dni, String matricula, EspecialidadMedica especialidad) {
        return new Medico("Carlos", "González", dni, LocalDate.of(1975, 5, 15),
                TipoSangre.A_POSITIVO, matricula, especialidad);
    }

    // Fecha futura: dentro de "dias" días, a la hora indicada en punto
    private static LocalDateTime fechaFutura(int dias, int hora) {
        return LocalDate.now().plusDays(dias).atTime(hora, 0);
    }

    private static boolean matriculaEsValida(String numero) {
        try {
            new Matricula(numero);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("--- " + texto + " ---");
    }

    private static void verificar(boolean condicion, String descripcion) {
        if (condicion) {
            ok(descripcion);
        } else {
            fallo(descripcion);
        }
    }

    private static void ok(String descripcion) {
        correctas++;
        System.out.println("  [OK]    " + descripcion);
    }

    private static void fallo(String descripcion) {
        fallidas++;
        System.out.println("  [FALLA] " + descripcion);
    }
}
