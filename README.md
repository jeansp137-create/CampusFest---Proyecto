# CampusFest

Sistema de gestión de eventos universitarios desarrollado en **Java 21**, **JavaFX 21**, **JUnit 5** y **H2 Database**, siguiendo **Arquitectura Limpia** y **Patrones GRASP**.

## Integrantes del Equipo

| Integrante | Usuario GitHub | Rol en el Proyecto | Responsabilidad Principal |
| --- | --- | --- | --- |
| **Julián David Fernandez Ruiz** | `@jeansp137-create` | **Arquitecto Base y Desarrollador CU1** | Configuración Maven, pruebas unitarias base, dominio base (`Reserva`, `Asistencia`, `EstadoReserva`), puertos (`Reloj`, `RepositorioEventos`) y CU1 (`ConsultarCupos` + `PantallaCupos`). |
| **Juan Sebastian Correa Aguilera** | `@mono21131` | **Desarrollador de Dominio Evento, Memoria y CU2** | Entidad `Evento` (disponibilidad, reservas y búsqueda), `RepositorioEventosEnMemoria`, `RelojDelSistema` y CU2 (`ReservarEntrada` + `PantallaReserva`). |
| **Diego Mauricio Lancheros Molina** | `@Diegomau26` / `@dimalancheros` | **Desarrollador de Edición (CU3) y Composición UI** | Métodos de edición y aforo en `Evento`, CU3 (`EditarEvento` + `PantallaEvento` + `estilos.css`), ensamblaje inicial de `App` y `Launcher`. |
| **Youbry Camilo Mendoza Martínez** | `@camilomendoza527-sketch` | **Desarrollador de Ingreso QR (CU4), Persistencia H2 y Documentación** | CU4 (`RegistrarIngreso` + `PantallaIngreso`), esquema relacional y adaptador H2 (`schema.sql`, `RepositorioEventosH2`), transición a H2 en `App` y documentación (`README.md`). |

## Arquitectura y Paquetes

| Anillo | Paquete | Responsabilidad |
| --- | --- | --- |
| **Entidades (Dominio)** | `co.edu.unillanos.campusfest.domain` | `Evento`, `Reserva`, `EstadoReserva`, `Asistencia`. Reglas de negocio puras, sin dependencias externas ni llamadas a `now()`. |
| **Puertos** | `co.edu.unillanos.campusfest.application.port` | Interfaces `RepositorioEventos` y `Reloj`. |
| **Casos de Uso** | `co.edu.unillanos.campusfest.application.usecase` | `ConsultarCupos` (CU1), `ReservarEntrada` (CU2), `EditarEvento` (CU3), `RegistrarIngreso` (CU4). |
| **Adaptadores (Persistencia)** | `co.edu.unillanos.campusfest.infrastructure.persistence` | `RepositorioEventosEnMemoria`, `RepositorioEventosH2` (`schema.sql` con tablas `eventos`, `reservas`, `asistencias`) y `RelojDelSistema`. |
| **Adaptadores (UI JavaFX)** | `co.edu.unillanos.campusfest.infrastructure.ui` | Controladores y vistas FXML/CSS (`PantallaCupos`, `PantallaReserva`, `PantallaEvento`, `PantallaIngreso`). |
| **Raíz de Composición** | `co.edu.unillanos.campusfest` | `App` (inyecta dependencias concretas en los casos de uso y controladores) y `Launcher`. |

## Casos de Uso Implementados

1. **CU1 · Consultar cupos (`ConsultarCupos`)**: calcula `aforo - bloqueados - confirmadas - pendientes vigentes` usando el puerto `Reloj`, sin guardar cambios.
2. **CU2 · Reservar y confirmar (`ReservarEntrada`)**: crea reservas pendientes con plazo de 10 minutos y permite confirmarlas dentro del plazo.
3. **CU3 · Editar evento (`EditarEvento`)**: actualiza nombre, fecha, lugar y aforo, validando que el nuevo aforo no sea menor que los cupos ocupados (incluyendo el conteo en el mensaje de error).
4. **CU4 · Registrar ingreso QR (`RegistrarIngreso`)**: registra una única asistencia por reserva en estado `CONFIRMADA` usando la hora del puerto `Reloj`, recibiendo el código QR como `String`.

## Evidencia de Transición a H2 (Aspecto 6.5)

En el commit `dcb8c64` se modificó únicamente `App.java` para sustituir `new RepositorioEventosEnMemoria()` por `new RepositorioEventosH2()`, demostrando que ni las entidades de `domain` ni los casos de uso de `application` cambian al reemplazar el adaptador de persistencia. El esquema relacional está en `src/main/resources/co/edu/unillanos/campusfest/infrastructure/persistence/schema.sql` con claves primarias y foráneas (`eventos`, `reservas`, `asistencias`) y manejo de `SQLException` encapsulado dentro de `RepositorioEventosH2`.

## Declaración de Uso de IA (Aspectos 8.1 y 8.2)

### Herramientas usadas, propósito y clases (Aspecto 8.1)

Durante el desarrollo del proyecto el equipo utilizó las siguientes herramientas de IA como apoyo, revisando manualmente el cumplimiento del contrato, los principios GRASP y la Arquitectura Limpia:

- **Antigravity**: asistencia en la implementación de CU4 (`RegistrarIngreso`), controlador y vista JavaFX (`PantallaIngresoController`, `PantallaIngreso.fxml`), adaptador y esquema SQL de H2 (`RepositorioEventosH2`, `schema.sql`), verificación de pruebas unitarias, flujo de Git/PRs y documentación (`README.md`).
- **Gemini**: apoyo en la estructuración inicial del proyecto Maven (`pom.xml`), entidades base (`Reserva`, `Asistencia`, `EstadoReserva`), puertos (`Reloj`, `RepositorioEventos`), casos de uso (`ConsultarCupos`, `ReservarEntrada`) y vistas FXML (`PantallaCupos.fxml`, `PantallaReserva.fxml`).
- **Claude**: apoyo en la implementación de edición en dominio y caso de uso (`Evento.cambiarDatos`, `Evento.cambiarAforo`, `EditarEvento`), vista/estilos de CU3 (`PantallaEventoController`, `PantallaEvento.fxml`, `estilos.css`) y composición inicial de `App` y `Launcher`.

### Propuesta de la IA que violaba el diseño y su corrección (Aspecto 8.2)

En la implementación inicial de la interfaz de CU2 (`PantallaReservaController`, commit `fd7cae8`), la IA generó una validación de negocio directamente en el controlador JavaFX usando `LocalDateTime.now()` para comprobar si la última reserva solicitada ya había vencido (`!LocalDateTime.now().isBefore(ultimaReservaSolicitada.getVenceEn())`), además de mantener estado de la entidad en el controlador (`ultimaReservaSolicitada`).

Esto violaba la **Arquitectura Limpia** (reglas de negocio en un adaptador UI), **GRASP Experto en Información / Controlador** y **Variaciones Protegidas** (uso directo del reloj del sistema en lugar del puerto `Reloj`).

**Corrección realizada (commit `95eb04f`)**: se eliminó por completo la validación temporal, la referencia a `LocalDateTime.now()` y el campo `ultimaReservaSolicitada` de `PantallaReservaController`, delegando la verificación del plazo únicamente a la entidad `Reserva` a través del caso de uso `ReservarEntrada.confirmarReserva(...)`.

## Ejecución de Pruebas y Aplicación

### Pruebas unitarias (60 pruebas en 8 clases)
```bash
mvn test
```

### Ejecutar la aplicación JavaFX
```bash
mvn javafx:run
```
*(Clase principal alternativa para IDE: `co.edu.unillanos.campusfest.Launcher`)*
