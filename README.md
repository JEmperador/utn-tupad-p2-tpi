# Sistema de Gestión Hospitalaria

## Descripción

Sistema que modela el funcionamiento interno de un hospital en Java. Organiza médicos y
pacientes a través de departamentos y salas, y coordina citas médicas entre ellos. Todo el
procesamiento ocurre en memoria: sin archivos, sin base de datos.

El sistema responde a las siguientes consultas del dominio:

- ¿En qué departamento trabaja este médico?
- ¿Qué pacientes tiene registrados el hospital?
- ¿Cuál es la historia clínica de este paciente?
- ¿Está disponible este médico o esta sala para una cita en determinada fecha y hora?

---

## Estructura del repositorio

```
TPI/
├── Codigo/                         Proyecto Maven
│   ├── pom.xml
│   └── src/main/java/org/jcr/
│       ├── MainPruebas.java        Archivo de pruebas (provisto por la cátedra)
│       ├── entidades/              Persona, Paciente, Medico, Hospital, Departamento,
│       │                           Sala, Cita, HistoriaClinica, Matricula, Validaciones
│       ├── enums/                  TipoSangre, EspecialidadMedica, EstadoCita
│       ├── servicios/              CitaService (interfaz), CitaManager
│       └── excepciones/            CitaException
└── Documentacion/
    ├── 01_Analisis_Del_Problema.md
    ├── 02_Plan_De_Solucion.md
    ├── 03_Informe_Final.md
    └── UmlHospital.png
```

---

## Cómo ejecutar

Requiere Java 17 y Maven.

```bash
cd Codigo
mvn compile
mvn exec:java -Dexec.mainClass="org.jcr.MainPruebas"
```

El resultado esperado es `106 de 106 verificaciones correctas`.

---

## Tecnologías

- Java 17 · solo biblioteca estándar
- Maven (gestión del proyecto)
- Colecciones: `ArrayList` / `List` (`java.util`)
- Fechas: `LocalDate` / `LocalDateTime` (`java.time`)

---

## Documentación

| Archivo | Contenido |
|---|---|
| [`01_Analisis_Del_Problema.md`](Documentacion/01_Analisis_Del_Problema.md) | Entidades, relaciones y reglas de negocio |
| [`02_Plan_De_Solucion.md`](Documentacion/02_Plan_De_Solucion.md) | Orden de implementación paso a paso |
| [`03_Informe_Final.md`](Documentacion/03_Informe_Final.md) | Informe técnico completo con resultado de pruebas |
| [`UmlHospital.png`](Documentacion/UmlHospital.png) | Diagrama UML de clases |
