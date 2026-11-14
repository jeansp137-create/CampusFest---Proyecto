package co.edu.unillanos.campusfest.infrastructure.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Asistencia;
import co.edu.unillanos.campusfest.domain.EstadoReserva;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.domain.Reserva;

public class RepositorioEventosH2 implements RepositorioEventos {

    private static final String RUTA_SCHEMA =
            "/co/edu/unillanos/campusfest/infrastructure/persistence/schema.sql";

    private final String jdbcUrl;
    private final String usuario;
    private final String clave;

    public RepositorioEventosH2() {
        this("jdbc:h2:./campusfest-db;AUTO_SERVER=TRUE", "sa", "");
    }

    public RepositorioEventosH2(String jdbcUrl) {
        this(jdbcUrl, "sa", "");
    }

    public RepositorioEventosH2(String jdbcUrl, String usuario, String clave) {
        this.jdbcUrl = jdbcUrl;
        this.usuario = usuario;
        this.clave = clave;
        inicializarEsquema();
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, usuario, clave);
    }

    private void inicializarEsquema() {
        String sql = leerSchemaSql();
        try (Connection conn = conectar(); Statement st = conn.createStatement()) {
            for (String sentencia : sql.split(";")) {
                String limpia = sentencia.trim();
                if (!limpia.isEmpty()) {
                    st.execute(limpia);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible inicializar el esquema H2.", e);
        }
    }

    private String leerSchemaSql() {
        try (InputStream in = getClass().getResourceAsStream(RUTA_SCHEMA)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el script SQL: " + RUTA_SCHEMA);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder contenido = new StringBuilder();
                String linea;
                while ((linea = reader.readLine()) != null) {
                    contenido.append(linea).append('\n');
                }
                return contenido.toString();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error al leer el script SQL de H2.", e);
        }
    }

    @Override
    public Evento buscar(String eventoId) {
        String sqlEvento = "SELECT id, nombre, fecha, lugar, aforo, bloqueados FROM eventos WHERE id = ?";
        try (Connection conn = conectar();
             PreparedStatement ps = conn.prepareStatement(sqlEvento)) {
            ps.setString(1, eventoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String id = rs.getString("id");
                String nombre = rs.getString("nombre");
                LocalDateTime fecha = rs.getTimestamp("fecha").toLocalDateTime();
                String lugar = rs.getString("lugar");
                int aforo = rs.getInt("aforo");
                int bloqueados = rs.getInt("bloqueados");

                List<Reserva> reservas = cargarReservasYAsistencias(conn, id);
                return new Evento(id, nombre, fecha, lugar, aforo, bloqueados, reservas);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al consultar el evento en H2.", e);
        }
    }

    private List<Reserva> cargarReservasYAsistencias(Connection conn, String eventoId) throws SQLException {
        String sql = "SELECT r.codigo, r.estudiante_id, r.vence_en, r.estado, a.hora "
                + "FROM reservas r "
                + "LEFT JOIN asistencias a ON r.codigo = a.codigo_reserva "
                + "WHERE r.evento_id = ?";
        List<Reserva> reservas = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, eventoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp tsAsistencia = rs.getTimestamp("hora");
                    Asistencia asistencia = tsAsistencia != null
                            ? new Asistencia(tsAsistencia.toLocalDateTime())
                            : null;
                    Reserva reserva = Reserva.reconstruir(
                            rs.getString("codigo"),
                            rs.getString("estudiante_id"),
                            rs.getTimestamp("vence_en").toLocalDateTime(),
                            EstadoReserva.valueOf(rs.getString("estado")),
                            asistencia);
                    reservas.add(reserva);
                }
            }
        }
        return reservas;
    }

    @Override
    public void guardar(Evento evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento no puede ser nulo.");
        }
        String sqlEvento = "MERGE INTO eventos (id, nombre, fecha, lugar, aforo, bloqueados) "
                + "KEY(id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = conectar()) {
            boolean autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlEvento)) {
                ps.setString(1, evento.getId());
                ps.setString(2, evento.getNombre());
                ps.setTimestamp(3, Timestamp.valueOf(evento.getFecha()));
                ps.setString(4, evento.getLugar());
                ps.setInt(5, evento.getAforo());
                ps.setInt(6, evento.getBloqueados());
                ps.executeUpdate();

                guardarReservasYAsistencias(conn, evento);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitOriginal);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al guardar el evento en H2.", e);
        }
    }

    private void guardarReservasYAsistencias(Connection conn, Evento evento) throws SQLException {
        String sqlReserva = "MERGE INTO reservas (codigo, evento_id, estudiante_id, vence_en, estado) "
                + "KEY(codigo) VALUES (?, ?, ?, ?, ?)";
        String sqlAsistencia = "MERGE INTO asistencias (codigo_reserva, hora) KEY(codigo_reserva) VALUES (?, ?)";

        try (PreparedStatement psReserva = conn.prepareStatement(sqlReserva);
             PreparedStatement psAsistencia = conn.prepareStatement(sqlAsistencia)) {
            for (Reserva reserva : evento.getReservas()) {
                psReserva.setString(1, reserva.getCodigo());
                psReserva.setString(2, evento.getId());
                psReserva.setString(3, reserva.getEstudianteId());
                psReserva.setTimestamp(4, Timestamp.valueOf(reserva.getVenceEn()));
                psReserva.setString(5, reserva.getEstado().name());
                psReserva.executeUpdate();

                if (reserva.getAsistencia() != null) {
                    psAsistencia.setString(1, reserva.getCodigo());
                    psAsistencia.setTimestamp(2, Timestamp.valueOf(reserva.getAsistencia().getHora()));
                    psAsistencia.executeUpdate();
                }
            }
        }
    }
}
