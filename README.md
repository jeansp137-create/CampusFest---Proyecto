# CampusFest

Sistema de gestión de eventos universitarios desarrollado en **Java 21**, **JavaFX 21**, **JUnit 5** y **H2 Database**, siguiendo **Arquitectura Limpia** y **Patrones GRASP**.

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
4. **CU4 · Registrar ingreso QR (`RegistrarIngreso`)**: registra una única asistencia por reserva en estado `CONFIRMADA` usando la hora del puerto `Reloj`.

## Evidencias de Diseño y Evolución

- **Transición a H2 (Aspecto 6.5)**: en el commit `dcb8c64` se modificó únicamente `App.java` para sustituir `RepositorioEventosEnMemoria` por `RepositorioEventosH2`, demostrando que ni las entidades de `domain` ni los casos de uso de `application` cambian al reemplazar la persistencia. El esquema relacional se encuentra en `src/main/resources/co/edu/unillanos/campusfest/infrastructure/persistence/schema.sql` con claves primarias y foráneas (`eventos`, `reservas`, `asistencias`) y manejo de `SQLException` encapsulado en el adaptador.
- **Corrección de sugerencia de IA (Aspecto 8.2)**: tras el commit `fd7cae8` que incluía una validación de vencimiento en el controlador de CU2, el commit `95eb04f` eliminó dicha lógica de la capa UI para delegar la regla al dominio (`Reserva`) a través del caso de uso `ReservarEntrada`.

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
