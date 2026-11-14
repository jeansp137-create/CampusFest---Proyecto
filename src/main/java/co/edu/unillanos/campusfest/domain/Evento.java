package co.edu.unillanos.campusfest.domain;

import java.time.LocalDateTime;

public class Evento {

    private final String id;
    private String nombre;
    private LocalDateTime fecha;
    private String lugar;
    private int aforo;
    private final int bloqueados;

    public Evento(String id, String nombre, LocalDateTime fecha, String lugar, int aforo, int bloqueados) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.lugar = lugar;
        this.aforo = aforo;
        this.bloqueados = bloqueados;
    }

    public int cuposDisponibles(LocalDateTime ahora) {
        throw new UnsupportedOperationException("Pendiente de implementar por Integrante 2");
    }

    public Reserva reservar(String estudianteId, LocalDateTime ahora) {
        throw new UnsupportedOperationException("Pendiente de implementar por Integrante 2");
    }

    public Reserva buscarReservaPorCodigo(String codigo) {
        throw new UnsupportedOperationException("Pendiente de implementar por Integrante 2");
    }

    public void cambiarDatos(String nombre, LocalDateTime fecha, String lugar) {
        throw new UnsupportedOperationException("Pendiente de implementar por Integrante 3");
    }

    public void cambiarAforo(int nuevoAforo, LocalDateTime ahora) {
        throw new UnsupportedOperationException("Pendiente de implementar por Integrante 3");
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public LocalDateTime getFecha() { return fecha; }
    public String getLugar() { return lugar; }
    public int getAforo() { return aforo; }
    public int getBloqueados() { return bloqueados; }
}
