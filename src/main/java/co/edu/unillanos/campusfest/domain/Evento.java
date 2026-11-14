package co.edu.unillanos.campusfest.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Evento {

    private final String id;
    private String nombre;
    private LocalDateTime fecha;
    private String lugar;
    private int aforo;
    private final int bloqueados;
    private final List<Reserva> reservas;

    public Evento(String id, String nombre, LocalDateTime fecha, String lugar, int aforo, int bloqueados) {
        this(id, nombre, fecha, lugar, aforo, bloqueados, new ArrayList<>());
    }

    public Evento(String id, String nombre, LocalDateTime fecha, String lugar,
                  int aforo, int bloqueados, List<Reserva> reservas) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.lugar = lugar;
        this.aforo = aforo;
        this.bloqueados = bloqueados;
        this.reservas = reservas != null ? new ArrayList<>(reservas) : new ArrayList<>();
    }

    public int cuposDisponibles(LocalDateTime ahora) {
        return aforo - bloqueados - cuposOcupadosPorReservas(ahora);
    }

    public int cuposOcupadosPorReservas(LocalDateTime ahora) {
        int ocupados = 0;
        for (Reserva reserva : reservas) {
            if (reserva.ocupaCupo(ahora)) {
                ocupados++;
            }
        }
        return ocupados;
    }

    public Reserva reservar(String estudianteId, LocalDateTime ahora) {
        if (cuposDisponibles(ahora) <= 0) {
            throw new IllegalStateException("No hay cupos disponibles para este evento.");
        }
        Reserva nuevaReserva = Reserva.pendiente(estudianteId, ahora);
        reservas.add(nuevaReserva);
        return nuevaReserva;
    }

    public Reserva buscarReservaPorCodigo(String codigo) {
        for (Reserva reserva : reservas) {
            if (reserva.getCodigo().equals(codigo)) {
                return reserva;
            }
        }
        throw new IllegalArgumentException("El código de reserva no pertenece al evento: " + codigo);
    }

    public void cambiarDatos(String nombre, LocalDateTime fecha, String lugar) {
        throw new UnsupportedOperationException("Pendiente de implementar por Integrante 3");
    }

    public void cambiarAforo(int nuevoAforo, LocalDateTime ahora) {
        throw new UnsupportedOperationException("Pendiente de implementar por Integrante 3");
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getLugar() {
        return lugar;
    }

    public int getAforo() {
        return aforo;
    }

    public int getBloqueados() {
        return bloqueados;
    }

    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }
}
