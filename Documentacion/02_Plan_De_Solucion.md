# Plan de Solución — TPI: Sistema de Gestión Hospitalaria

## Relación entre el TPI y las unidades de la materia

El TPI integra los contenidos de las **Unidades 3 a 8**. Las unidades 1 y 2 son la base sobre
la que todo se construye (tipos, control de flujo, métodos). La Unidad 9 aparece de forma
implícita al usar colecciones genéricas de la biblioteca estándar. La Unidad 10 (JDBC) **no
se aplica**: el sistema trabaja completamente en memoria.

| Unidad                           | Contenido                                                                | Dónde aparece en la solución                                                                        |
| -------------------------------- | ------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------- |
| U1 — Introducción a Java         | Tipos primitivos, `String`, operadores                                   | Atributos `double costo`, `String dni`; comparaciones en validaciones                               |
| U2 — Programación estructurada   | `for`, `if`, métodos, arrays                                             | Búsqueda en listas con `for`; métodos auxiliares; lógica en `getEdad()`                             |
| U3 — Introducción a POO          | Clase, objeto, atributos privados, getters, constructores con validación | Todas las entidades: `Persona`, `Hospital`, `Cita`, etc.                                            |
| U4 — POO avanzada                | `this`, sobrecarga de constructores, `static final`                      | `this` en constructores; `HORAS_ENTRE_CITAS = 2` en `CitaManager`                                   |
| U5 — UML y relaciones            | Composición, agregación, bidireccionalidad                               | `HistoriaClinica` (composición), `Hospital`↔`Departamento` (bidireccional)                          |
| U6 — Colecciones y enumeraciones | `ArrayList`, `Collections`, búsqueda, `enum`                             | Listas de citas, médicos, pacientes; `TipoSangre`, `EspecialidadMedica`, `EstadoCita`               |
| U7 — Herencia y polimorfismo     | `extends`, clase abstracta, `super`, `@Override`                         | `Persona` ← `Paciente`, `Medico`; `CitaService` ← `CitaManager`                                     |
| U8 — Interfaces y excepciones    | `interface`, `throws`, excepción verificada, `try-catch`                 | `CitaService` (interfaz); `CitaException extends Exception`; `programarCita() throws CitaException` |
| U9 — Genéricos                   | Uso de tipos genéricos de la biblioteca estándar                         | `ArrayList<Cita>`, `List<Medico>`, `Collections.unmodifiableList()`                                 |
| U10 — JDBC                       | Persistencia con base de datos                                           | **No se usa.** Todo vive en memoria.                                                                |

---

## Criterio de orden de implementación

Las clases se implementan de menor a mayor dependencia: primero lo que no importa nada del
proyecto, último lo que depende de todo lo demás. En cada paso se puede ejecutar
`MainPruebas.java` y comprobar cuántos grupos van dando `[OK]`.

---

## Paso 1 — Estructura del proyecto (U1 / base técnica)

Crear los cuatro paquetes dentro de `src/main/java/org/jcr/`:

```text
src/main/java/org/jcr/
├── MainPruebas.java      (ya está, no se modifica)
├── entidades/
├── enums/
├── servicios/
└── excepciones/
```

`MainPruebas.java` importa todas las clases por su paquete. Si los nombres no coinciden
exactamente, el compilador falla con `cannot find symbol` antes de ejecutar una sola prueba.

---

## Paso 2 — Enumeraciones (U6 — Colecciones y enumeraciones)

**Archivos:** `TipoSangre.java`, `EspecialidadMedica.java`, `EstadoCita.java`

Las enumeraciones son el concepto central de U6 junto con las colecciones. Cada constante
encapsula su propia descripción en un atributo privado, asignado por el constructor del enum
y expuesto con `getDescripcion()`.

```java
public enum TipoSangre {
    A_POSITIVO("A+"), A_NEGATIVO("A-"),
    B_POSITIVO("B+"), B_NEGATIVO("B-"),
    AB_POSITIVO("AB+"), AB_NEGATIVO("AB-"),
    O_POSITIVO("O+"), O_NEGATIVO("O-");

    private final String descripcion;

    TipoSangre(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
```

`EspecialidadMedica` y `EstadoCita` siguen exactamente el mismo patrón.

**Puntos de atención (U1):**

- Las descripciones tienen tildes y signos exactos: `"Cardiología"`, `"No Asistió"`, `"AB+"`.
- Todos los valores deben estar: 8 en `TipoSangre`, 12 en `EspecialidadMedica`, 5 en
  `EstadoCita`.

**Verificación:** grupo 1 → 7/7 `[OK]`.

---

## Paso 3 — CitaException (U8 — Interfaces y excepciones)

**Archivo:** `CitaException.java`

El concepto de excepción verificada es el núcleo de U8. `CitaException` extiende `Exception`
(no `RuntimeException`): esto **obliga** a quien llame a `programarCita()` a manejarla con
`throws` o `try-catch`. Si extendiese `RuntimeException`, el compilador no lo detectaría y
`MainPruebas` fallaría en el grupo 9.

```java
public class CitaException extends Exception {

    public CitaException(String message) {
        super(message);
    }

    public CitaException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

---

## Paso 4 — Clase auxiliar `Validaciones` (U3 — Introducción a POO)

**Archivo:** `Validaciones.java` (paquete `entidades`, sin modificador de acceso)

Centraliza los chequeos repetidos en todos los constructores. Sin esta clase, la misma
validación de texto vacío aparecería copiada en `Persona`, `Hospital`, `Departamento` y
`Sala`, violando el principio de no repetir código (U3).

```java
class Validaciones {

    static String textoObligatorio(String valor, String mensajeError) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor;
    }

    static void noNulo(Object valor, String mensajeError) {
        if (valor == null) {
            throw new IllegalArgumentException(mensajeError);
        }
    }

    static boolean sonDigitos(String texto) {
        for (char c : texto.toCharArray()) {   // U2: for sobre array de chars
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
```

Visibilidad de paquete (sin `public`): solo accesible dentro de `org.jcr.entidades`. La clase
es un utilitario interno, no parte del contrato público del sistema.

---

## Paso 5 — Matricula (U3 — Introducción a POO)

**Archivo:** `Matricula.java`

`Matricula` es un ejemplo puro de encapsulamiento (U3): oculta el `String` interno y solo
lo expone con `getNumero()`. Toda la validación del formato `MP-XXXX` ocurre en el
constructor.

```java
public class Matricula {
    private final String numero;

    public Matricula(String numero) {
        validarMatricula(numero);
        this.numero = numero;
    }

    public String getNumero() {
        return numero;
    }

    private static void validarMatricula(String numero) {
        if (numero == null || !numero.startsWith("MP-")) {
            throw new IllegalArgumentException("Matrícula inválida: " + numero);
        }
        String digitos = numero.substring(3);
        if (digitos.length() < 4 || digitos.length() > 6
                || !Validaciones.sonDigitos(digitos)) {
            throw new IllegalArgumentException("Matrícula inválida: " + numero);
        }
    }
}
```

**Verificación:** grupo 4 → 8/8 `[OK]` (después de implementar `Medico` en el paso 6).

---

## Paso 6 — Persona, Paciente y Medico (U3, U4 y U7)

Este paso concentra los tres pilares de U7: **clase abstracta**, **herencia** y
**polimorfismo**.

### Persona (abstracta) — U7

`Persona` define la estructura común. No puede instanciarse: obliga a extenderla. El
constructor valida todos los datos antes de asignarlos (U3).

```java
public abstract class Persona {
    protected String nombre;
    protected String apellido;
    protected String dni;
    protected LocalDate fechaNacimiento;
    protected TipoSangre tipoSangre;

    public Persona(String nombre, String apellido, String dni,
                   LocalDate fechaNacimiento, TipoSangre tipoSangre) {
        this.nombre          = Validaciones.textoObligatorio(nombre, "El nombre es obligatorio");
        this.apellido        = Validaciones.textoObligatorio(apellido, "El apellido es obligatorio");
        this.dni             = validarDni(dni);
        Validaciones.noNulo(fechaNacimiento, "La fecha de nacimiento es obligatoria");
        Validaciones.noNulo(tipoSangre, "El tipo de sangre es obligatorio");
        this.fechaNacimiento = fechaNacimiento;
        this.tipoSangre      = tipoSangre;
    }

    private static String validarDni(String dni) {
        if (dni == null || (dni.length() != 7 && dni.length() != 8)
                || !Validaciones.sonDigitos(dni)) {
            throw new IllegalArgumentException("DNI inválido: " + dni);
        }
        return dni;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public int getEdad() {
        // U2: lógica con if y comparación de fechas
        LocalDate hoy = LocalDate.now();
        int anos = hoy.getYear() - fechaNacimiento.getYear();
        if (hoy.isBefore(fechaNacimiento.plusYears(anos))) {
            anos--;
        }
        return anos;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getDni() {
        return dni;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public TipoSangre getTipoSangre() {
        return tipoSangre;
    }
}
```

### Paciente — U7 (herencia) + U5 (composición con HistoriaClinica)

```java
public class Paciente extends Persona {
    private String telefono;
    private String direccion;
    private HistoriaClinica historiaClinica;   // composición (U5)
    private Hospital hospital;
    private final ArrayList<Cita> citas = new ArrayList<>();  // U6

    public Paciente(String nombre, String apellido, String dni,
                    LocalDate fechaNacimiento, TipoSangre tipoSangre,
                    String telefono, String direccion) {
        super(nombre, apellido, dni, fechaNacimiento, tipoSangre); // U7: llama al padre
        this.telefono        = Validaciones.textoObligatorio(telefono, "El teléfono es obligatorio");
        this.direccion       = Validaciones.textoObligatorio(direccion, "La dirección es obligatoria");
        this.historiaClinica = new HistoriaClinica(this); // composición: el Paciente crea su HC
    }

    public void addCita(Cita cita) {
        citas.add(cita);
    }

    public List<Cita> getCitas() {
        return Collections.unmodifiableList(citas); // U6: lista protegida
    }

    public HistoriaClinica getHistoriaClinica() {
        return historiaClinica;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setHospital(Hospital nuevo) {  // sincronización bidireccional (U5)
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
}
```

### Medico — U7 (herencia) + U4 (static, this)

Mismo patrón que `Paciente`, con `Matricula` y `EspecialidadMedica` en lugar de
`HistoriaClinica`. `setDepartamento()` agrega una validación de especialidad **antes** de
modificar cualquier estado: si la especialidad del médico no coincide con la del departamento
destino, lanza `IllegalArgumentException` sin tocar ninguna lista ni referencia. Esto es
necesario porque `setDepartamento()` puede llamarse directamente desde código externo, no
solo desde `agregarMedico()`.

**Verificación tras los pasos 5 y 6:** grupos 2, 3 y 4 → `[OK]` completos.

---

## Paso 7 — HistoriaClinica (U5 — Composición + U6 — Colecciones)

**Archivo:** `HistoriaClinica.java`

Es el ejemplo más claro de **composición** (U5): la historia no existe sin su paciente, y
solo el `Paciente` la puede crear. El constructor sin modificador de acceso garantiza esto
dentro del paquete `entidades`.

Las tres listas de `String` (diagnósticos, tratamientos, alergias) son colecciones (U6)
protegidas con `Collections.unmodifiableList()`.

```java
class HistoriaClinica {
    private final Paciente paciente;
    private final LocalDateTime fechaCreacion;
    private final ArrayList<String> diagnosticos = new ArrayList<>();
    private final ArrayList<String> tratamientos = new ArrayList<>();
    private final ArrayList<String> alergias     = new ArrayList<>();

    HistoriaClinica(Paciente paciente) {  // constructor de paquete: solo Paciente lo llama
        this.paciente      = paciente;
        this.fechaCreacion = LocalDateTime.now();
    }

    public String getNumeroHistoria() {
        // U2: concatenación de String; el año viene de fechaCreacion, no de nacimiento
        return "HC-" + paciente.getDni() + "-" + fechaCreacion.getYear();
    }

    public void agregarDiagnostico(String d) {
        if (d != null && !d.trim().isEmpty()) {  // U2: if + trim
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
```

**Verificación:** grupo 5 → `[OK]` completo.

---

## Paso 8 — Hospital (U5 — Relaciones bidireccionales)

**Archivo:** `Hospital.java`

`Hospital` implementa las relaciones bidireccionales Hospital↔Departamento y
Hospital↔Paciente (U5). La clave es que un extremo agrega y el otro actualiza su referencia,
sin caer en recursión infinita.

```java
public class Hospital {
    private String nombre;
    private String direccion;
    private String telefono;
    private final ArrayList<Departamento> departamentos = new ArrayList<>();
    private final ArrayList<Paciente>     pacientes     = new ArrayList<>();

    public Hospital(String nombre, String direccion, String telefono) {
        this.nombre    = Validaciones.textoObligatorio(nombre, "El nombre es obligatorio");
        this.direccion = Validaciones.textoObligatorio(direccion, "La dirección es obligatoria");
        this.telefono  = Validaciones.textoObligatorio(telefono, "El teléfono es obligatorio");
    }

    public void agregarDepartamento(Departamento d) {
        if (d != null && !departamentos.contains(d)) {  // U6: contains evita duplicados
            departamentos.add(d);
            d.setHospital(this); // sincroniza el otro extremo (U5)
        }
    }

    // Llamado desde Departamento.setHospital() para sacar el departamento de este hospital
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
        return Collections.unmodifiableList(departamentos); // U6: lista protegida
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
```

La sincronización se completa en `Departamento.setHospital()` (paso 9), que llama a
`hospital.quitarDepartamento(this)` y `hospital.agregarDepartamento(this)`.

**Verificación:** grupo 6 → `[OK]` completo (después del paso 9).

---

## Paso 9 — Departamento y Sala (U5 — Composición + U6 — Colecciones)

**Archivos:** `Departamento.java`, `Sala.java`

### Departamento

Combina la relación bidireccional con `Hospital` (U5) y el control de especialidad al agregar
médicos (U6 + regla de negocio). Crea sus `Sala`s por composición (U5).

```java
public class Departamento {
    private String nombre;
    private EspecialidadMedica especialidad;  // U6: enum
    private Hospital hospital;
    private final ArrayList<Medico> medicos = new ArrayList<>();
    private final ArrayList<Sala>   salas   = new ArrayList<>();

    public Departamento(String nombre, EspecialidadMedica especialidad) {
        this.nombre       = Validaciones.textoObligatorio(nombre, "El nombre es obligatorio");
        Validaciones.noNulo(especialidad, "La especialidad es obligatoria");
        this.especialidad = especialidad;
    }

    public void agregarMedico(Medico m) {
        if (m == null) {
            return;
        }
        if (m.getEspecialidad() != this.especialidad) {  // U6: comparación de enum con ==
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

    public Sala crearSala(String numero, String tipo) {  // composición (U5)
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

    public EspecialidadMedica getEspecialidad() {
        return especialidad;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Medico> getMedicos() {
        return Collections.unmodifiableList(medicos);
    }

    public List<Sala> getSalas() {
        return Collections.unmodifiableList(salas);
    }
}
```

### Sala — U5 (composición, constructor de paquete)

```java
class Sala {
    private final String numero;
    private final String tipo;
    private final Departamento departamento;
    private final ArrayList<Cita> citas = new ArrayList<>();

    // Constructor de paquete: solo Departamento.crearSala() lo llama
    Sala(String numero, String tipo, Departamento departamento) {
        this.numero       = Validaciones.textoObligatorio(numero, "El número de sala es obligatorio");
        this.tipo         = Validaciones.textoObligatorio(tipo, "El tipo de sala es obligatorio");
        this.departamento = departamento;
    }

    public void addCita(Cita cita) {
        citas.add(cita);
    }

    public List<Cita> getCitas() {
        return Collections.unmodifiableList(citas);
    }

    public String getNumero() {
        return numero;
    }

    public String getTipo() {
        return tipo;
    }

    public Departamento getDepartamento() {
        return departamento;
    }
}
```

**Verificación:** grupos 6 y 7 → `[OK]` completos.

---

## Paso 10 — Cita (U3 — POO + U6 — Enumeraciones)

**Archivo:** `Cita.java`

`Cita` es un objeto de estado: une cuatro entidades en una fecha/hora y registra el ciclo de
vida de la consulta médica mediante `EstadoCita` (U6). Sus atributos de identidad son
`final`; solo `estado` y `observaciones` son mutables.

```java
public class Cita {
    private final Paciente      paciente;
    private final Medico        medico;
    private final Sala          sala;
    private final LocalDateTime fechaHora;
    private final double        costo;
    private EstadoCita          estado;       // U6: enum mutable
    private String              observaciones;

    public Cita(Paciente paciente, Medico medico, Sala sala,
                LocalDateTime fechaHora, double costo) {
        Validaciones.noNulo(paciente,  "El paciente es obligatorio");
        Validaciones.noNulo(medico,    "El médico es obligatorio");
        Validaciones.noNulo(sala,      "La sala es obligatoria");
        Validaciones.noNulo(fechaHora, "La fecha y hora son obligatorias");
        this.paciente      = paciente;
        this.medico        = medico;
        this.sala          = sala;
        this.fechaHora     = fechaHora;
        this.costo         = costo;
        this.estado        = EstadoCita.PROGRAMADA;
        this.observaciones = "";
    }

    public void setEstado(EstadoCita estado) {
        Validaciones.noNulo(estado, "El estado no puede ser nulo");
        this.estado = estado;
    }

    public void setObservaciones(String observaciones) {
        // null se convierte silenciosamente en ""
        this.observaciones = (observaciones == null) ? "" : observaciones;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public Medico getMedico() {
        return medico;
    }

    public Sala getSala() {
        return sala;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public double getCosto() {
        return costo;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public String getObservaciones() {
        return observaciones;
    }
}
```

**Verificación:** grupo 8 → `[OK]` completo.

---

## Paso 11 — CitaService y CitaManager (U7 — Interface + U8 — Excepciones)

**Archivos:** `CitaService.java`, `CitaManager.java`

Este paso concentra U7 (interfaz como contrato) y U8 (excepciones verificadas). `CitaService`
define **qué** puede hacer el servicio. `CitaManager` define **cómo** lo hace.

### CitaService (interfaz) — U7

```java
public interface CitaService {
    Cita programarCita(Paciente paciente, Medico medico, Sala sala,
                       LocalDateTime fechaHora, double costo) throws CitaException; // U8

    List<Cita> getCitasPorPaciente(Paciente paciente);
    List<Cita> getCitasPorMedico(Medico medico);
    List<Cita> getCitasPorSala(Sala sala);
}
```

### CitaManager — U7 (`implements`) + U4 (`static final`) + U6 (`ArrayList`) + U8 (lanzar excepción)

```java
public class CitaManager implements CitaService {

    static final int HORAS_ENTRE_CITAS = 2; // U4: constante static final

    private final ArrayList<Cita> citas = new ArrayList<>(); // U6: colección interna

    @Override
    public Cita programarCita(Paciente paciente, Medico medico, Sala sala,
                              LocalDateTime fechaHora, double costo) throws CitaException {
        // U8: se lanza CitaException ante cualquier condición inválida
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

        // Todas las validaciones pasaron: crear y registrar
        Cita cita = new Cita(paciente, medico, sala, fechaHora, costo);
        citas.add(cita);
        paciente.addCita(cita);
        medico.addCita(cita);
        sala.addCita(cita);
        return cita;
    }

    // U2: método auxiliar con for-each; U6: recorre una colección
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

    // U2: operaciones matemáticas; U4: uso de la constante HORAS_ENTRE_CITAS
    boolean seSuperponen(LocalDateTime existente, LocalDateTime nueva) {
        long minutos = Math.abs(ChronoUnit.MINUTES.between(existente, nueva));
        return minutos < HORAS_ENTRE_CITAS * 60L; // < 120 min: se rechaza; == 120: se acepta
    }

    @Override
    public List<Cita> getCitasPorPaciente(Paciente paciente) {
        ArrayList<Cita> resultado = new ArrayList<>();
        for (Cita c : citas) {         // U2: for-each; U6: filtrado manual
            if (c.getPaciente() == paciente) {
                resultado.add(c);
            }
        }
        return resultado; // lista nueva: nunca null, nunca la lista interna
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
```

**Puntos de atención:**

- `seSuperponen()` usa `< 120` (estrictamente menor): exactamente 120 min **no** se superpone.
- `ChronoUnit.MINUTES.between()` con `Math.abs()` mide la distancia en ambas direcciones.
- Los métodos `getCitasPor...()` devuelven una lista nueva (nunca `null`, nunca la interna).

**Verificación:** grupos 9 y 10 → `[OK]` completos → **106/106**.

---

## Resumen: qué unidad resuelve qué

| Clase / concepto                                                                                     | Unidad principal    |
| ---------------------------------------------------------------------------------------------------- | ------------------- |
| Tipos, `String`, `char`, operadores                                                                  | U1                  |
| `for`, `if`, `Math.abs()`, métodos auxiliares                                                        | U2                  |
| Atributos privados, getters, constructores con validación, `IllegalArgumentException`                | U3                  |
| `this`, `static final HORAS_ENTRE_CITAS`, sobrecarga de constructores                                | U4                  |
| Composición (`HistoriaClinica`, `Matricula`, `Sala`), relaciones bidireccionales                     | U5                  |
| `ArrayList`, `Collections.unmodifiableList()`, enums con descripción, filtrado sin streams           | U6                  |
| `Persona` abstracta, `extends`, `super()`, `@Override`, `CitaService` implementada por `CitaManager` | U7                  |
| `CitaException extends Exception`, `throws`, `try-catch` en `MainPruebas`                            | U8                  |
| `List<Cita>`, `ArrayList<Medico>`, `Collections` (genéricos de la biblioteca estándar)               | U9 (implícito)      |
| ~~JDBC, DAO, SQL, transacciones~~                                                                    | U10 — **no se usa** |

---

## Estrategia para depurar fallas

1. **Corregir en orden de grupos**: un error en `Persona` (grupo 2) puede arrastrar fallas en
   grupos posteriores.
2. **Leer el mensaje de cada `[FALLA]`**: describe exactamente qué condición no se cumplió.
3. **Agregar `System.out.println` temporales** para inspeccionar valores; eliminarlos antes
   de entregar (restan puntos en el criterio de legibilidad).
4. **Verificar `seSuperponen()` con ejemplos de la mano**: cita base a las 10:00 →
   08:00 acepta, 08:30 rechaza, 12:00 acepta, 11:30 rechaza.
