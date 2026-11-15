package co.edu.unillanos.campusfest.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Asistencia;
import co.edu.unillanos.campusfest.domain.EstadoReserva;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.domain.Reserva;

/**
 * Pruebas del adaptador RepositorioEventosH2.
 * 16 pruebas según el contrato docente: esquema, claves, guardar y leer, actualizar, errores.
 */
class RepositorioEventosH2Test {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 11, 28, 19, 0);
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 20, 18, 0);

    private Connection conexion;
    private RepositorioEventosH2 repositorio;

    @BeforeEach
    void setUp() throws SQLException {
        conexion = DriverManager.getConnection(
                "jdbc:h2:mem:testdb_" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1",
                "sa", "");
        repositorio = new RepositorioEventosH2(conexion);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (conexion != null && !conexion.isClosed()) {
            conexion.close();
        }
    }

    @Test
    void implementaElPuerto() {
        assertTrue(repositorio instanceof RepositorioEventos);
    }

    @Test
    void crearElRepositorioCreaElEsquema() throws SQLException {
        DatabaseMetaData meta = conexion.getMetaData();
        List<String> tablas = new ArrayList<>();
        try (ResultSet rs = meta.getTables(null, null, "%", new String[] {"TABLE"})) {
            while (rs.next()) {
                tablas.add(rs.getString("TABLE_NAME").toUpperCase());
            }
        }
        assertTrue(tablas.contains("EVENTO"), "Debe existir la tabla EVENTO");
        assertTrue(tablas.contains("RESERVA"), "Debe existir la tabla RESERVA");
        assertTrue(tablas.contains("ASISTENCIA"), "Debe existir la tabla ASISTENCIA");
    }

    @Test
    void crearloDosVecesSobreLaMismaBaseNoFalla() {
        RepositorioEventosH2 segundo = new RepositorioEventosH2(conexion);
        assertNotNull(segundo);
    }

    @Test
    void buscarUnEventoQueNoExisteDevuelveNull() {
        assertNull(repositorio.buscar("no-existe"));
    }

    @Test
    void guardarYBuscarUnEventoSinReservas() {
        Evento evento = new Evento("ev-1", "Concierto", FECHA, "Auditorio", 100, 5);
        repositorio.guardar(evento);

        Evento recuperado = repositorio.buscar("ev-1");

        assertNotNull(recuperado);
        assertEquals("ev-1", recuperado.getId());
        assertEquals("Concierto", recuperado.getNombre());
        assertEquals(FECHA, recuperado.getFecha());
        assertEquals("Auditorio", recuperado.getLugar());
        assertEquals(100, recuperado.getAforo());
        assertEquals(5, recuperado.getBloqueados());
        assertTrue(recuperado.getReservas().isEmpty());
    }

    @Test
    void guardarDosVecesElMismoEventoLoActualiza() {
        Evento evento = new Evento("ev-1", "Concierto", FECHA, "Auditorio", 100, 5);
        repositorio.guardar(evento);

        evento.cambiarDatos("Concierto Final", FECHA.plusDays(1), "Estadio");
        repositorio.guardar(evento);

        Evento recuperado = repositorio.buscar("ev-1");
        assertEquals("Concierto Final", recuperado.getNombre());
        assertEquals(FECHA.plusDays(1), recuperado.getFecha());
        assertEquals("Estadio", recuperado.getLugar());
    }

    @Test
    void guardarEventoConReservasPendientes() {
        Evento evento = new Evento("ev-1", "Concierto", FECHA, "Auditorio", 100, 5);
        Reserva reserva = evento.reservar("est-1", AHORA);
        repositorio.guardar(evento);

        Evento recuperado = repositorio.buscar("ev-1");
        assertEquals(1, recuperado.getReservas().size());

        Reserva reservaRecuperada = recuperado.buscarReservaPorCodigo(reserva.getCodigo());
        assertEquals("est-1", reservaRecuperada.getEstudianteId());
        assertEquals(EstadoReserva.PENDIENTE, reservaRecuperada.getEstado());
        assertEquals(reserva.getVenceEn(), reservaRecuperada.getVenceEn());
        assertNull(reservaRecuperada.getAsistencia());
    }

    @Test
    void guardarEventoConReservasConfirmadas() {
        Evento evento = new Evento("ev-1", "Concierto", FECHA, "Auditorio", 100, 5);
        Reserva reserva = evento.reservar("est-1", AHORA);
        reserva.confirmar(AHORA.plusMinutes(2));
        repositorio.guardar(evento);

        Evento recuperado = repositorio.buscar("ev-1");
        Reserva reservaRecuperada = recuperado.buscarReservaPorCodigo(reserva.getCodigo());
        assertEquals(EstadoReserva.CONFIRMADA, reservaRecuperada.getEstado());
    }

    @Test
    void guardarEventoConReservasYAsistencias() {
        Evento evento = new Evento("ev-1", "Concierto", FECHA, "Auditorio", 100, 5);
        Reserva reserva = evento.reservar("est-1", AHORA);
        reserva.confirmar(AHORA.plusMinutes(2));
        LocalDateTime horaIngreso = FECHA.minusMinutes(30);
        reserva.registrarIngreso(horaIngreso);
        repositorio.guardar(evento);

        Evento recuperado = repositorio.buscar("ev-1");
        Reserva reservaRecuperada = recuperado.buscarReservaPorCodigo(reserva.getCodigo());
        Asistencia asistencia = reservaRecuperada.getAsistencia();
        assertNotNull(asistencia);
        assertEquals(horaIngreso, asistencia.getHora());
    }

    @Test
    void guardarDosVecesNoDuplicaReservas() {
        Evento evento = new Evento("ev-1", "Concierto", FECHA, "Auditorio", 100, 5);
        evento.reservar("est-1", AHORA);
        evento.reservar("est-2", AHORA);
        repositorio.guardar(evento);

        evento.cambiarDatos("Concierto Modificado", FECHA, "Auditorio");
        repositorio.guardar(evento);

        Evento recuperado = repositorio.buscar("ev-1");
        assertEquals(2, recuperado.getReservas().size());
    }

    @Test
    void buscarDevuelveUnObjetoNuevoReconstruido() {
        Evento evento = new Evento("ev-1", "Concierto", FECHA, "Auditorio", 100, 5);
        repositorio.guardar(evento);

        Evento recuperado = repositorio.buscar("ev-1");
        assertNotSame(evento, recuperado);
    }

    @Test
    void lasTablasTienenClavePrimaria() throws SQLException {
        DatabaseMetaData meta = conexion.getMetaData();
        try (ResultSet pkEvento = meta.getPrimaryKeys(null, null, "EVENTO")) {
            assertTrue(pkEvento.next(), "EVENTO debe tener clave primaria");
            assertEquals("ID", pkEvento.getString("COLUMN_NAME").toUpperCase());
        }
        try (ResultSet pkReserva = meta.getPrimaryKeys(null, null, "RESERVA")) {
            assertTrue(pkReserva.next(), "RESERVA debe tener clave primaria");
            assertEquals("CODIGO", pkReserva.getString("COLUMN_NAME").toUpperCase());
        }
        try (ResultSet pkAsistencia = meta.getPrimaryKeys(null, null, "ASISTENCIA")) {
            assertTrue(pkAsistencia.next(), "ASISTENCIA debe tener clave primaria");
            assertEquals("CODIGO_RESERVA", pkAsistencia.getString("COLUMN_NAME").toUpperCase());
        }
    }

    @Test
    void lasTablasTienenClavesForaneas() throws SQLException {
        DatabaseMetaData meta = conexion.getMetaData();
        boolean fkReservaAEvento = false;
        try (ResultSet fkReserva = meta.getImportedKeys(null, null, "RESERVA")) {
            while (fkReserva.next()) {
                if ("EVENTO".equalsIgnoreCase(fkReserva.getString("PKTABLE_NAME"))) {
                    fkReservaAEvento = true;
                }
            }
        }
        assertTrue(fkReservaAEvento, "RESERVA debe tener foreign key hacia EVENTO");

        boolean fkAsistenciaAReserva = false;
        try (ResultSet fkAsistencia = meta.getImportedKeys(null, null, "ASISTENCIA")) {
            while (fkAsistencia.next()) {
                if ("RESERVA".equalsIgnoreCase(fkAsistencia.getString("PKTABLE_NAME"))) {
                    fkAsistenciaAReserva = true;
                }
            }
        }
        assertTrue(fkAsistenciaAReserva, "ASISTENCIA debe tener foreign key hacia RESERVA");
    }

    @Test
    void guardarEventoNuloLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> repositorio.guardar(null));
    }

    @Test
    void constructorConConexionNulaLanzaExcepcion() {
        assertThrows(IllegalStateException.class, () -> new RepositorioEventosH2(null));
    }

    @Test
    void erroresDeBaseDeDatosSalenComoIllegalStateException() throws SQLException {
        conexion.close();
        Evento evento = new Evento("ev-1", "Concierto", FECHA, "Auditorio", 100, 5);
        assertThrows(IllegalStateException.class, () -> repositorio.buscar("ev-1"));
        assertThrows(IllegalStateException.class, () -> repositorio.guardar(evento));
    }
}
