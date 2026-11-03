# Trabajo Práctico Integrador — Programación II

## Sistema de Gestión Hospitalaria

**Institución:** Universidad Tecnológica Nacional  
**Materia:** Programación II  
**Ciclo lectivo:** 2026  
**Estudiante:** Javier Emperador  
**Fecha de entrega:** 16/11/2026

---

## 1. Descripción del sistema

El sistema modela el funcionamiento interno de un hospital en Java. Organiza médicos y pacientes
a través de departamentos y salas, y coordina citas médicas entre ellos. Todo el procesamiento
ocurre en memoria: los datos existen mientras el programa se ejecuta y no se persisten en archivos
ni bases de datos.

El sistema responde a las siguientes consultas del dominio:

- ¿En qué departamento trabaja este médico?
- ¿Qué pacientes tiene registrados el hospital?
- ¿Cuál es la historia clínica de este paciente?
- ¿Está disponible este médico o esta sala para una cita en determinada fecha y hora?

---

## 2. Estructura del proyecto

Los paquetes siguen la estructura obligatoria del enunciado (sección 2.4):

```text
src/main/java/org/jcr/
├── MainPruebas.java          (provisto por la cátedra, sin modificar)
├── entidades/                Persona, Paciente, Medico, Matricula, HistoriaClinica,
│                             Hospital, Departamento, Sala, Cita
│                             Validaciones (clase auxiliar interna al paquete)
├── enums/                    TipoSangre, EspecialidadMedica, EstadoCita
├── servicios/                CitaService, CitaManager
└── excepciones/              CitaException
```

Se agregó la clase auxiliar `Validaciones` dentro del paquete `entidades` para centralizar
los chequeos repetidos (texto obligatorio, nulos, dígitos) y evitar su duplicación en cada
constructor.

`Validaciones` no lleva modificador `public`: tiene visibilidad de paquete, lo que la hace
accesible solo desde clases dentro de `org.jcr.entidades`. Es un utilitario interno del
sistema, no parte del contrato público. Hacerla `public` permitiría que código externo la
use, lo cual no tiene sentido para una clase que solo existe para no repetir lógica de
validación en cada constructor.

---

## 3. Enumeraciones (Criterio A)

Se implementaron las tres enumeraciones en el paquete `org.jcr.enums`:

| Enumeración          | Constantes | Descripción representativa      |
| -------------------- | ---------- | ------------------------------- |
| `TipoSangre`         | 8          | `A_POSITIVO` → `"A+"`           |
| `EspecialidadMedica` | 12         | `CARDIOLOGIA` → `"Cardiología"` |
| `EstadoCita`         | 5          | `NO_ASISTIO` → `"No Asistió"`   |

Cada enumeración guarda su descripción en un atributo `private final String descripcion`,
asignado por el constructor del enum y expuesto mediante `getDescripcion()`. Las descripciones
incluyen tildes y signos exactos conforme al enunciado (sección 3.1).

---

## 4. Herencia y encapsulamiento (Criterio B)

### Jerarquía de clases

```text
Persona (abstracta)
├── Paciente
└── Medico
```

`Persona` es abstracta: no puede instanciarse sola. Define los cinco datos comunes (nombre,
apellido, DNI, fecha de nacimiento y tipo de sangre) con atributos `protected` y un constructor
que los valida. `Paciente` y `Medico` extienden `Persona`, llaman a `super(...)` y agregan
sus propios atributos sin redeclarar los heredados.

### Métodos calculados

- **`getNombreCompleto()`**: concatena nombre, espacio y apellido. Devuelve `"María López"`.
- **`getEdad()`**: calcula los años cumplidos a la fecha actual. Resta un año si el cumpleaños
  de este año todavía no llegó (se compara mes y día, no solo el año):

```java
public int getEdad() {
    LocalDate hoy = LocalDate.now();
    int anos = hoy.getYear() - fechaNacimiento.getYear();
    if (hoy.isBefore(fechaNacimiento.plusYears(anos))) anos--;
    return anos;
}
```

### Número de historia clínica

`HistoriaClinica.getNumeroHistoria()` devuelve `"HC-" + paciente.getDni() + "-" + año`.
El año es el de creación de la historia (guardado en `fechaCreacion`), no el de nacimiento
del paciente ni un valor fijo.

### Encapsulamiento

Todos los atributos son `private` (o `protected` en `Persona`). No hay setters públicos
que permitan saltear las validaciones. Las listas internas se exponen con
`Collections.unmodifiableList()` o con una copia, nunca directamente.

---

## 5. Validaciones (Criterio C)

Todas las validaciones se hacen dentro del constructor antes de asignar cualquier atributo.
Si alguna falla, se lanza `IllegalArgumentException` y el objeto no llega a crearse.

La clase auxiliar `Validaciones` (visibilidad de paquete) centraliza las reglas repetidas:

| Método                             | Qué valida                          |
| ---------------------------------- | ----------------------------------- |
| `textoObligatorio(valor, mensaje)` | No nulo, no vacío, no solo espacios |
| `noNulo(valor, mensaje)`           | No nulo                             |
| `sonDigitos(texto)`                | Todos los caracteres son dígitos    |

Reglas específicas:

- **DNI**: exactamente 7 u 8 caracteres, todos dígitos (`Character.isDigit()`).
- **Matrícula**: empieza con `"MP-"` seguido de 4 a 6 dígitos.
- **`setEstado(null)`**: lanza `IllegalArgumentException`; el estado anterior no cambia.
- **`setObservaciones(null)`**: no lanza excepción; las observaciones quedan como `""`.
- **Diagnóstico/tratamiento/alergia nulo o vacío**: no se registra, sin excepción.

---

## 6. Relaciones y composición (Criterios D y E)

### Relaciones bidireccionales

Se implementaron tres relaciones bidireccionales sincronizadas desde ambas puntas:

| Relación                | Método lado "uno"                | Método lado "muchos"         |
| ----------------------- | -------------------------------- | ---------------------------- |
| Hospital — Departamento | `Hospital.agregarDepartamento()` | `Departamento.setHospital()` |
| Hospital — Paciente     | `Hospital.agregarPaciente()`     | `Paciente.setHospital()`     |
| Departamento — Medico   | `Departamento.agregarMedico()`   | `Medico.setDepartamento()`   |

**Invariante**: si `a` está en la lista de `b`, entonces `a.getB()` devuelve `b`.

**Sin recursión infinita**: `setHospital()` (y equivalentes) guarda el objeto anterior,
actualiza la referencia y luego llama a `quitarX()` del anterior y `agregarX()` del nuevo.
`quitarX()` es un método de visibilidad de paquete que solo hace `lista.remove()`, sin llamar
de vuelta a `setHospital()`. `agregarX()` tiene un guard con `contains()` que corta el ciclo
cuando el segundo extremo llama de regreso.

**Sin duplicados**: `agregarX()` verifica con `contains()` antes de agregar.

**Control de especialidad**: se valida en dos lugares:

- `Departamento.agregarMedico()`: lanza `IllegalArgumentException` antes de modificar cualquier lista.
- `Medico.setDepartamento()`: también valida la especialidad **antes** de modificar el estado, porque puede llamarse directamente desde código externo. Un rechazo en este punto no deja ninguna referencia inconsistente.

### Composición

| Dueño          | Parte             | Cómo se crea                               |
| -------------- | ----------------- | ------------------------------------------ |
| `Paciente`     | `HistoriaClinica` | En el constructor de `Paciente`            |
| `Medico`       | `Matricula`       | En el constructor de `Medico`              |
| `Departamento` | `Sala`            | Con `Departamento.crearSala(numero, tipo)` |

Los constructores de `HistoriaClinica` y `Sala` tienen visibilidad de paquete (sin modificador),
lo que impide instanciarlos desde fuera de `org.jcr.entidades`.

### Listas protegidas

Ningún getter de lista (`Hospital`, `Departamento`, `Sala`, `Paciente`, `HistoriaClinica`,
`CitaManager`) devuelve la lista interna. Se usa `Collections.unmodifiableList(lista)`, que
lanza `UnsupportedOperationException` si se intenta modificar desde afuera.

---

## 7. Programación de citas (Criterios F y G)

### Contrato del servicio

`CitaService` es la interfaz que define el contrato. `CitaManager` la implementa y guarda
las citas en un `ArrayList` privado.

```java
public interface CitaService {
    Cita programarCita(Paciente, Medico, Sala, LocalDateTime, double) throws CitaException;
    List<Cita> getCitasPorPaciente(Paciente paciente);
    List<Cita> getCitasPorMedico(Medico medico);
    List<Cita> getCitasPorSala(Sala sala);
}
```

### CitaException

`CitaException` extiende `Exception` (excepción verificada, no `RuntimeException`). Esto
obliga a quien llame a `programarCita()` a capturarla con `try-catch` o declararla con
`throws`. Tiene dos constructores: `CitaException(String)` y `CitaException(String, Throwable)`.

### Validaciones de programarCita()

`programarCita()` lanza `CitaException` y no registra nada si:

1. El paciente, el médico o la sala son nulos.
2. La fecha es nula o ya pasó (`isBefore(LocalDateTime.now())`).
3. El costo es 0 o negativo.
4. La especialidad del médico no coincide con la del departamento de la sala.
5. El médico ya tiene otra cita a menos de 2 horas.
6. La sala ya tiene otra cita a menos de 2 horas.

Si todas las validaciones pasan, crea la cita y la registra en cuatro lugares:
el `ArrayList` interno de `CitaManager`, la lista del paciente, la del médico y la de la sala.

### Regla de las 2 horas

La ventana de exclusión se aplica en ambas direcciones. Si hay una cita a las 10:00:

| Nueva cita              | Diferencia | Resultado  |
| ----------------------- | ---------- | ---------- |
| 08:30 u 11:30           | 90 min     | Se rechaza |
| 08:00 o 12:00           | 120 min    | Se acepta  |
| 10:00 del día siguiente | > 24 h     | Se acepta  |

La condición de rechazo usa `ChronoUnit.MINUTES.between()` con `Math.abs()` y la constante
`HORAS_ENTRE_CITAS = 2` definida en `CitaManager`:

```java
boolean seSuperponen(LocalDateTime existente, LocalDateTime nueva) {
    long minutos = Math.abs(ChronoUnit.MINUTES.between(existente, nueva));
    return minutos < HORAS_ENTRE_CITAS * 60L; // < 120: rechaza; == 120: acepta
}
```

---

## 8. Restricciones técnicas cumplidas (Criterio H)

- Java 17, solo biblioteca estándar.
- Colecciones: únicamente `ArrayList` y `List` (`java.util`).
- Fechas: únicamente `LocalDate` y `LocalDateTime` (`java.time`).
- Sin archivos, bases de datos, Lombok, JPA, Hibernate.
- Sin `System.out.println` de depuración dentro de las entidades o servicios.
- Validaciones comunes centralizadas en `Validaciones`, sin repetición.
- Sin código muerto, sin imports sobrantes, sin variables sin usar.

---

## 9. Resultado de MainPruebas

```text
===== PRUEBAS DEL SISTEMA DE GESTIÓN HOSPITALARIA =====

--- 1. Enumeraciones ---
  [OK]    TipoSangre tiene 8 valores
  [OK]    TipoSangre.A_POSITIVO se describe como "A+"
  [OK]    TipoSangre.O_NEGATIVO se describe como "O-"
  [OK]    EspecialidadMedica tiene 12 valores
  [OK]    EspecialidadMedica.CARDIOLOGIA se describe como "Cardiología"
  [OK]    EstadoCita tiene 5 valores
  [OK]    EstadoCita.NO_ASISTIO se describe como "No Asistió"

--- 2. Persona, Paciente y Medico (herencia) ---
  [OK]    Persona es una clase abstracta
  [OK]    getNombreCompleto() de un Paciente devuelve "María López"
  [OK]    getNombreCompleto() de un Medico devuelve "Carlos González"
  [OK]    El Paciente conserva su DNI
  [OK]    El Paciente conserva su tipo de sangre
  [OK]    El Paciente conserva su teléfono
  [OK]    El Medico conserva su especialidad
  [OK]    El Medico crea su Matricula con el número indicado
  [OK]    Quien cumple 30 años hoy tiene 30 años
  [OK]    Quien cumple 30 años mañana todavía tiene 29 años

--- 3. Validaciones de Persona y Paciente ---
  [OK]    Un nombre vacío lanza IllegalArgumentException
  [OK]    Un apellido formado solo por espacios lanza IllegalArgumentException
  [OK]    Un nombre nulo lanza IllegalArgumentException
  [OK]    Un DNI de 6 dígitos lanza IllegalArgumentException
  [OK]    Un DNI de 9 dígitos lanza IllegalArgumentException
  [OK]    Un DNI con letras lanza IllegalArgumentException
  [OK]    Un DNI de 7 dígitos es válido
  [OK]    Una fecha de nacimiento nula lanza IllegalArgumentException
  [OK]    Un tipo de sangre nulo lanza IllegalArgumentException
  [OK]    Un teléfono vacío lanza IllegalArgumentException
  [OK]    Una dirección vacía lanza IllegalArgumentException
  [OK]    Un médico sin especialidad lanza IllegalArgumentException

--- 4. Matrícula (formato MP- seguido de 4 a 6 dígitos) ---
  [OK]    MP-1234 es válida (4 dígitos)
  [OK]    MP-123456 es válida (6 dígitos)
  [OK]    MP-123 se rechaza (3 dígitos)
  [OK]    MP-1234567 se rechaza (7 dígitos)
  [OK]    12345 se rechaza (falta el prefijo MP-)
  [OK]    MP-12A45 se rechaza (tiene una letra)
  [OK]    Una matrícula nula se rechaza
  [OK]    Un médico con matrícula inválida lanza IllegalArgumentException

--- 5. Historia clínica ---
  [OK]    Cada Paciente tiene su historia clínica desde que se crea
  [OK]    La historia clínica conoce a su paciente
  [OK]    El número de historia tiene el formato HC-DNI-AÑO (HC-11111111-2026)
  [OK]    Se registra un diagnóstico
  [OK]    Se registra un tratamiento
  [OK]    Se registran dos alergias
  [OK]    Un diagnóstico vacío o nulo no se registra
  [OK]    Una alergia vacía no se registra
  [OK]    La lista de diagnósticos no se puede modificar desde afuera de la historia

--- 6. Hospital y relaciones bidireccionales ---
  [OK]    Un hospital sin nombre lanza IllegalArgumentException
  [OK]    Agregar un departamento lo deja una sola vez en el hospital
  [OK]    El departamento conoce a su hospital
  [OK]    Agregar dos veces el mismo departamento no lo duplica
  [OK]    Agregar un paciente lo deja una sola vez en el hospital
  [OK]    El paciente conoce a su hospital
  [OK]    Agregar dos veces el mismo paciente no lo duplica
  [OK]    Al pasar el paciente a otro hospital, sale del anterior
  [OK]    Al pasar el paciente a otro hospital, entra en el nuevo
  [OK]    Al pasar el departamento a otro hospital, cambia de lista
  [OK]    La lista de pacientes no se puede modificar desde afuera del hospital

--- 7. Departamento, médicos y salas ---
  [OK]    Agregar un médico lo deja una sola vez en el departamento
  [OK]    El médico conoce a su departamento
  [OK]    Agregar dos veces el mismo médico no lo duplica
  [OK]    Agregar un médico a un departamento de otra especialidad lanza IllegalArgumentException
  [OK]    El médico rechazado no cambia de departamento
  [OK]    Al pasar el médico a otro departamento, sale del anterior y entra en el nuevo
  [OK]    crearSala() agrega la sala al departamento
  [OK]    La sala conoce a su departamento
  [OK]    La sala conserva su número y su tipo
  [OK]    Una sala sin número lanza IllegalArgumentException
  [OK]    La sala inválida no queda agregada
  [OK]    La lista de salas no se puede modificar desde afuera del departamento

--- 8. Cita ---
  [OK]    Una cita nueva empieza en estado PROGRAMADA
  [OK]    Una cita nueva empieza sin observaciones (texto vacío)
  [OK]    La cita conserva su costo
  [OK]    La cita conserva su fecha y hora
  [OK]    Se puede cambiar el estado de la cita
  [OK]    Un estado nulo lanza IllegalArgumentException
  [OK]    El estado rechazado no cambia el estado anterior
  [OK]    Se pueden cargar observaciones
  [OK]    Observaciones nulas quedan como texto vacío
  [OK]    Una cita sin paciente lanza IllegalArgumentException

--- 9. Programación de citas (CitaService) ---
  [OK]    CitaException es una excepción verificada (hereda de Exception, no de RuntimeException)
  [OK]    Un paciente sin citas devuelve una lista vacía (no null)
  [OK]    Se programa una cita válida
  [OK]    La cita aparece en las citas del paciente (servicio)
  [OK]    La cita aparece en las citas del médico (servicio)
  [OK]    La cita aparece en las citas de la sala (servicio)
  [OK]    El paciente registra su cita
  [OK]    El médico registra su cita
  [OK]    La sala registra su cita
  [OK]    Las citas de un paciente no aparecen en otro
  [OK]    Una cita en el pasado lanza CitaException
  [OK]    Una cita con costo 0 lanza CitaException
  [OK]    Una cita con costo negativo lanza CitaException
  [OK]    Un médico en una sala de otra especialidad lanza CitaException
  [OK]    Programar una cita sin paciente lanza CitaException
  [OK]    Las citas rechazadas no quedan registradas en ningún lado
  [OK]    La lista de citas del paciente no se puede modificar desde afuera

--- 10. Disponibilidad de médicos y salas (2 horas entre citas) ---
  [OK]    El mismo médico 1 h 30 min después lanza CitaException
  [OK]    El mismo médico 1 h 30 min antes lanza CitaException
  [OK]    La misma sala 30 min después, con otro médico, lanza CitaException
  [OK]    El mismo médico exactamente 2 horas después se acepta
  [OK]    El mismo médico en la misma sala al día siguiente se acepta
  [OK]    Otro médico en otra sala, sin superposición, se acepta
  [OK]    El médico A quedó con 3 citas
  [OK]    El médico B quedó con 1 cita
  [OK]    La sala 1 quedó con 2 citas
  [OK]    El paciente quedó con 4 citas

===== RESULTADO: 106 de 106 verificaciones correctas =====
Todas las pruebas pasaron.
```

---

## Bibliografía

- Deitel, P. J., y Deitel, H. M. (2016). _Cómo programar en Java_ (10.ª ed.). Pearson Educación.
- Oracle. (s. f.). Class LocalDate. En _Java SE 17 & JDK 17 documentation_. https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/LocalDate.html
- Oracle. (s. f.). Class LocalDateTime. En _Java SE 17 & JDK 17 documentation_. https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/LocalDateTime.html
- Oracle. (s. f.). Lesson: Exceptions. En _The Java Tutorials_. https://docs.oracle.com/javase/tutorial/essential/exceptions/
