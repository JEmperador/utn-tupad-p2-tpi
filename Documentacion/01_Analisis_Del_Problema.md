# Análisis del Problema — TPI: Sistema de Gestión Hospitalaria

## 1. Descripción general del dominio

El trabajo modela el funcionamiento interno de un hospital en Java. Un hospital organiza a sus
médicos y pacientes a través de departamentos y salas, y coordina citas médicas entre ellos.
Todo sucede en memoria: los datos existen mientras el programa se ejecuta y no se persisten en
archivos ni en bases de datos.

El sistema debe responder correctamente a las siguientes preguntas del dominio:

- ¿En qué departamento trabaja este médico?
- ¿Qué pacientes tiene registrados el hospital?
- ¿Cuál es la historia clínica de este paciente?
- ¿Está disponible este médico o esta sala para una cita en determinada fecha y hora?

---

## 2. Entidades del dominio

### 2.1 Persona (abstracta)

Representa a cualquier persona dentro del sistema. No puede instanciarse sola: existe como base
común de `Paciente` y `Medico`. Sus datos son:

- Nombre, apellido, DNI, fecha de nacimiento y tipo de sangre.

Dos operaciones calculadas:

- **`getNombreCompleto()`**: nombre + espacio + apellido.
- **`getEdad()`**: años cumplidos a la fecha de hoy. Si la persona todavía no llegó al día de
  su cumpleaños este año, la edad es un año menor. Por ejemplo: alguien nacido el 15 de octubre
  de 1994 tiene 30 años el 15 de octubre de 2024, pero todavía 29 el 14 de octubre de 2024.

### 2.2 Paciente

Extiende `Persona`. Agrega teléfono y dirección. Al crearse, genera automáticamente su propia
`HistoriaClinica`. Mantiene referencias a:

- Su hospital (puede no tener uno todavía).
- Sus citas programadas.

### 2.3 Medico

Extiende `Persona`. Agrega número de matrícula y especialidad médica. Al crearse, genera
automáticamente su propia `Matricula`. Mantiene referencias a:

- Su departamento (puede no tener uno todavía).
- Sus citas programadas.

### 2.4 Matricula

Objeto simple que encapsula el número de matrícula de un médico. Su único propósito es validar
el formato en el momento de la creación. Formato requerido: `MP-` seguido de entre 4 y 6
dígitos (`MP-1234` a `MP-123456`).

### 2.5 HistoriaClinica

Registro médico perteneciente a un paciente. Solo la puede crear el propio `Paciente` (su
constructor no es de acceso público). Contiene tres listas independientes: diagnósticos,
tratamientos y alergias. Su número de identificación sigue el formato `HC-DNI-AÑO`, donde el
año es el del momento en que se creó la historia.

### 2.6 Hospital

Entidad raíz del sistema. Registra departamentos y pacientes. No tiene lógica de negocio
propia: es un contenedor que mantiene las relaciones sincronizadas con sus partes.

### 2.7 Departamento

Pertenece a un hospital y agrupa médicos de una misma especialidad. Crea sus propias salas
mediante el método `crearSala()`. Un departamento tiene una sola especialidad, y todos sus
médicos deben compartirla.

### 2.8 Sala

Solo puede crearse a través de `Departamento.crearSala()`. Su constructor no es público:
esto garantiza que ninguna sala exista por fuera de un departamento. Mantiene la lista de
citas que se han programado en ella.

### 2.9 Cita

Une a un paciente, un médico y una sala en una fecha y hora específicas, con un costo
asociado. Su estado inicial es `PROGRAMADA` y sus observaciones iniciales son vacías (`""`).

### 2.10 CitaManager / CitaService

`CitaService` es la interfaz que define el contrato del servicio de citas. `CitaManager` la
implementa. Es el único lugar donde se validan y registran las citas. Cuando una cita no puede
programarse, lanza `CitaException`.

### 2.11 CitaException

Excepción **verificada** (hereda de `Exception`, no de `RuntimeException`). Esto obliga a
quien llame a `programarCita()` a declarar la excepción con `throws` o capturarla con
`try-catch`. Tiene dos constructores: uno con mensaje y otro con mensaje y causa.

---

## 3. Relaciones entre entidades

### 3.1 Herencia

```text
Persona (abstracta)
├── Paciente
└── Medico
```

Tanto `Paciente` como `Medico` son personas. Un objeto de cualquiera de los dos tipos puede
tratarse como `Persona` (polimorfismo). Esto permite, por ejemplo, iterar una lista mixta y
llamar a `getNombreCompleto()` sin saber el tipo concreto.

### 3.2 Composición (la parte no existe sin el todo)

| Dueño          | Parte             | Cómo se crea                     |
| -------------- | ----------------- | -------------------------------- |
| `Paciente`     | `HistoriaClinica` | En el constructor del `Paciente` |
| `Medico`       | `Matricula`       | En el constructor del `Medico`   |
| `Departamento` | `Sala`            | Con `Departamento.crearSala()`   |

En los tres casos, el constructor de la parte no es público: solo el dueño puede crearlo.

### 3.3 Relaciones bidireccionales (ambos extremos se conocen)

Estas tres relaciones deben mantenerse sincronizadas desde las dos puntas:

| Relación                | Lado "uno"                                                      | Lado "muchos"                 |
| ----------------------- | --------------------------------------------------------------- | ----------------------------- |
| Hospital — Departamento | `Hospital.agregarDepartamento()` / `Departamento.setHospital()` | `hospital.getDepartamentos()` |
| Hospital — Paciente     | `Hospital.agregarPaciente()` / `Paciente.setHospital()`         | `hospital.getPacientes()`     |
| Departamento — Medico   | `Departamento.agregarMedico()` / `Medico.setDepartamento()`     | `departamento.getMedicos()`   |

**Invariante clave**: si `a` está en la lista de `b`, entonces `a.getB()` devuelve `b`, y
viceversa. Modificar un extremo actualiza automáticamente el otro.

**Regla adicional** en la relación Departamento–Medico: un médico solo puede asignarse a un
departamento cuya especialidad coincida con la suya. Si no coincide, se lanza
`IllegalArgumentException` y el médico no cambia de departamento.

### 3.4 Agregación de citas

Las citas son el punto de encuentro de las demás entidades. Cuando `CitaManager.programarCita()`
crea una cita exitosa, la registra en cuatro lugares:

- Su propio `ArrayList` interno.
- La lista de citas del paciente.
- La lista de citas del médico.
- La lista de citas de la sala.

---

## 4. Reglas de negocio

### 4.1 Validaciones al crear objetos

Los objetos inválidos no llegan a existir. Las validaciones se hacen dentro del constructor,
antes de asignar cualquier atributo, y ante cualquier incumplimiento se lanza
`IllegalArgumentException`.

| Dato                                                                                                                | Regla                                                 |
| ------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------- |
| Textos obligatorios (nombre, apellido, teléfono, dirección, nombre de hospital/departamento, número y tipo de sala) | No puede ser `null`, vacío ni contener solo espacios. |
| DNI                                                                                                                 | Exactamente 7 u 8 caracteres, todos dígitos.          |
| Matrícula                                                                                                           | Empieza con `MP-` seguido de 4 a 6 dígitos.           |
| Fecha de nacimiento, tipo de sangre, especialidad                                                                   | No puede ser `null`.                                  |
| Paciente, médico, sala o fecha en una `Cita`                                                                        | No puede ser `null`.                                  |

Casos especiales que **no** lanzan excepción:

- `cita.setObservaciones(null)` → las observaciones quedan como `""`.
- `historia.agregarDiagnostico("")` o `null` → no se registra nada.

### 4.2 Protección de las listas internas

Ningún getter de lista devuelve la lista interna directamente. Las opciones válidas son:

- `Collections.unmodifiableList(lista)` — lanza `UnsupportedOperationException` si se intenta
  modificar.
- Una copia nueva con `new ArrayList<>(lista)` — permite modificar la copia, pero no el
  original.

Esto se aplica en `Hospital`, `Departamento`, `Sala`, `Paciente`, `Medico`, `HistoriaClinica`
y `CitaManager`.

### 4.3 Regla de las 2 horas entre citas

`programarCita()` lanza `CitaException` si el médico o la sala ya tienen otra cita dentro de
la ventana de exclusión.

La ventana se mide en **ambas direcciones** respecto a cada cita existente:

```text
──────────────────────────────────────────────────────►  tiempo
          [zona excluida]      [cita existente]      [zona excluida]
    ◄──── < 2 horas ────►          10:00          ◄──── < 2 horas ────►
         se rechaza                               se rechaza
         (ej. 8:30)                               (ej. 11:30)

    exactamente 8:00 → se acepta       exactamente 12:00 → se acepta
```

La condición de rechazo para un par (cita existente, cita nueva) es:

```text
|fechaNueva - fechaExistente| < 2 horas
```

En Java, con `LocalDateTime`:

```java
long diferenciaMinutos = Math.abs(
    ChronoUnit.MINUTES.between(existente.getFechaHora(), nueva)
);
boolean seSuperponen = diferenciaMinutos < 120;
```

Otras causas de rechazo en `programarCita()`:

- Paciente, médico o sala son `null`.
- La fecha es `null` o ya pasó (`fechaHora.isBefore(LocalDateTime.now())`).
- El costo es 0 o negativo.
- La especialidad del médico no coincide con la del departamento de la sala.

Si se lanza `CitaException` por cualquier motivo, la cita **no queda registrada** en ningún
lugar: ni en el servicio, ni en el paciente, ni en el médico, ni en la sala.

---

## 5. Restricciones técnicas

- **Java 17 o superior**, solo biblioteca estándar.
- Colecciones: únicamente `ArrayList` y `List` (paquete `java.util`).
- Fechas: únicamente `LocalDate` y `LocalDateTime` (paquete `java.time`).
- **Prohibido**: archivos, bases de datos, Lombok, JPA, Hibernate, `Map`, streams, lambdas,
  clases genéricas propias.
- Estructura de paquetes obligatoria: `org.jcr.entidades`, `org.jcr.enums`,
  `org.jcr.servicios`, `org.jcr.excepciones`.
- Las firmas de clases y métodos deben coincidir exactamente con el enunciado y el UML.

---

## 6. Dificultades principales del problema

### 6.1 Sincronización bidireccional sin recursión infinita

El mayor riesgo de implementación. Si `hospital.agregarDepartamento(d)` llama a
`d.setHospital(hospital)`, y este a su vez llama a `hospital.agregarDepartamento(d)`, se
produce una recursión infinita.

La solución es usar métodos de visibilidad de paquete (`quitarX` / `agregarX`) que solo
modifican la lista sin volver a llamar a `setHospital()`. El guard `if (this.hospital == nuevo) return`
corta la recursión cuando el segundo extremo llama de vuelta:

```java
// En Departamento
public void setHospital(Hospital nuevo) {
    if (this.hospital == nuevo) return; // corta la recursión
    Hospital anterior = this.hospital;
    this.hospital = nuevo;
    if (anterior != null) anterior.quitarDepartamento(this); // package-private
    if (nuevo != null) nuevo.agregarDepartamento(this);      // contiene guard contains()
}

// En Hospital — package-private, solo modifica la lista
void quitarDepartamento(Departamento d) {
    departamentos.remove(d);
}
```

La misma lógica aplica a Hospital–Paciente y Departamento–Medico. En este último caso,
`setDepartamento()` además valida la especialidad **antes** de modificar cualquier estado,
para garantizar que un rechazo no deje referencias inconsistentes:

```java
// En Medico
public void setDepartamento(Departamento nuevo) {
    if (this.departamento == nuevo) return;
    if (nuevo != null && nuevo.getEspecialidad() != this.especialidad) {
        throw new IllegalArgumentException("La especialidad no coincide");
    }
    // recién acá se modifica el estado
    ...
}
```

### 6.2 Protección de listas con acceso interno necesario

Los getters públicos devuelven listas no modificables, pero el sistema internamente necesita
agregar elementos a esas listas. La solución más limpia es mantener el `ArrayList` privado y
exponer la lista protegida solo en el getter público. Los métodos internos como `addCita()` o
`setHospital()` acceden directamente al atributo privado.

### 6.3 La regla de las 2 horas en ambas direcciones

Es fácil olvidar que la restricción aplica tanto hacia adelante como hacia atrás en el tiempo.
Si el médico tiene una cita a las 10:00, no puede tener otra ni a las 11:30 ni a las 08:30.
Exactamente a las 08:00 o las 12:00 sí puede.

### 6.4 Constructor no público de Sala e HistoriaClinica

Java permite que un constructor tenga visibilidad de paquete (sin modificador) o protegida
(`protected`), pero no `private` si la clase creadora está en otro paquete. La solución más
simple es usar visibilidad de paquete: el constructor no lleva modificador de acceso, lo que
lo hace accesible solo dentro de `org.jcr.entidades`.

### 6.5 getEdad() con mes y día

No alcanza con restar años. Hay que verificar si la persona ya cumplió años este año:

```java
public int getEdad() {
    LocalDate hoy = LocalDate.now();
    int anos = hoy.getYear() - fechaNacimiento.getYear();
    if (hoy.isBefore(fechaNacimiento.plusYears(anos))) anos--;
    return anos;
}
```
