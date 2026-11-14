package co.edu.unillanos.campusfest.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Reserva {

    private static final int MINUTOS_PLAZO = 10;

    private final String codigo;
    private final String estudianteId;
    private final LocalDateTime venceEn;
    private EstadoReserva estado;
    private Asistencia asistencia;

    private Reserva(String codigo, String estudianteId, LocalDateTime venceEn) {
        this.codigo = codigo;
        this.estudianteId = estudianteId;
        this.venceEn = venceEn;
        this.estado = EstadoReserva.PENDIENTE;
        this.asistencia = null;
    }

    public static Reserva pendiente(String estudianteId, LocalDateTime ahora) {
        String codigoGenerado = UUID.randomUUID().toString();
        return new Reserva(codigoGenerado, estudianteId, ahora.plusMinutes(MINUTOS_PLAZO));
    }

    public static Reserva reconstruir(String codigo, String estudianteId, LocalDateTime venceEn,
                                      EstadoReserva estado, Asistencia asistencia) {
        Reserva reserva = new Reserva(codigo, estudianteId, venceEn);
        reserva.estado = estado;
        reserva.asistencia = asistencia;
        return reserva;
    }

    public boolean ocupaCupo(LocalDateTime ahora) {
        actualizarVencimiento(ahora);
        if (estado == EstadoReserva.CONFIRMADA) {
            return true;
        }
        if (estado == EstadoReserva.PENDIENTE) {
            return true;
        }
        return false;
    }

    public void confirmar(LocalDateTime ahora) {
        actualizarVencimiento(ahora);
        if (estado != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException("Solo se puede confirmar una reserva pendiente y vigente.");
        }
        this.estado = EstadoReserva.CONFIRMADA;
    }

    public void cancelar() {
        if (estado == EstadoReserva.CANCELADA) {
            throw new IllegalStateException("La reserva ya está cancelada.");
        }
        this.estado = EstadoReserva.CANCELADA;
    }

    public Asistencia registrarIngreso(LocalDateTime ahora) {
        if (estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Solo una reserva confirmada puede ingresar.");
        }
        if (asistencia != null) {
            throw new IllegalStateException("La reserva ya registró un ingreso anteriormente.");
        }
        this.asistencia = new Asistencia(ahora);
        return this.asistencia;
    }

    private void actualizarVencimiento(LocalDateTime ahora) {
        if (estado == EstadoReserva.PENDIENTE && !ahora.isBefore(venceEn)) {
            estado = EstadoReserva.VENCIDA;
        }
    }

    public String getCodigo() {
        return codigo;
    }

    public String getEstudianteId() {
        return estudianteId;
    }

    public LocalDateTime getVenceEn() {
        return venceEn;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public Asistencia getAsistencia() {
        return asistencia;
    }
}
