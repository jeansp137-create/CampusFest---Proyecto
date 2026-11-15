package co.edu.unillanos.campusfest.infrastructure.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
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

    private static final String RUTA_SCHEMA = "/db/schema.sql";

    private final Connection conexion;

    public RepositorioEventosH2(Connection conexion) {
        if (conexion == null) {
            throw new IllegalStateException("La conexión a la base de datos no puede ser nula.");
        }
        this.conexion = conexion;
        inicializarEsquema();
    }

    private void inicializarEsquema() {
        String sql = leerSchemaSql();
        try (Statement st = conexion.createStatement()) {
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
        String sqlEvento = "SELECT id, nombre, fecha, lugar, aforo, bloqueados FROM EVENTO WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sqlEvento)) {
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

                List<Reserva> reservas = cargarReservasYAsistencias(id);
                return new Evento(id, nombre, fecha, lugar, aforo, bloqueados, reservas);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al consultar el evento en H2.", e);
        }
    }

    private List<Reserva> cargarReservasYAsistencias(String eventoId) throws SQLException {
        String sql = "SELECT r.codigo, r.estudiante_id, r.vence_en, r.estado, a.hora "
                + "FROM RESERVA r "
                + "LEFT JOIN ASISTENCIA a ON r.codigo = a.codigo_reserva "
                + "WHERE r.evento_id = ?";
        List<Reserva> reservas = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
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
        String sqlEvento = "MERGE INTO EVENTO (id, nombre, fecha, lugar, aforo, bloqueados) "
                + "KEY(id) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            boolean autoCommitOriginal = conexion.getAutoCommit();
            conexion.setAutoCommit(false);
            try (PreparedStatement ps = conexion.prepareStatement(sqlEvento)) {
                ps.setString(1, evento.getId());
                ps.setString(2, evento.getNombre());
                ps.setTimestamp(3, Timestamp.valueOf(evento.getFecha()));
                ps.setString(4, evento.getLugar());
                ps.setInt(5, evento.getAforo());
                ps.setInt(6, evento.getBloqueados());
                ps.executeUpdate();

                guardarReservasYAsistencias(evento);
                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            } finally {
                conexion.setAutoCommit(autoCommitOriginal);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al guardar el evento en H2.", e);
        }
    }

    private void guardarReservasYAsistencias(Evento evento) throws SQLException {
        String sqlReserva = "MERGE INTO RESERVA (codigo, evento_id, estudiante_id, vence_en, estado) "
                + "KEY(codigo) VALUES (?, ?, ?, ?, ?)";
        String sqlAsistencia = "MERGE INTO ASISTENCIA (codigo_reserva, hora) KEY(codigo_reserva) VALUES (?, ?)";

        try (PreparedStatement psReserva = conexion.prepareStatement(sqlReserva);
             PreparedStatement psAsistencia = conexion.prepareStatement(sqlAsistencia)) {
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
