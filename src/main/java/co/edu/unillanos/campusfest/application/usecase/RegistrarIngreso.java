package co.edu.unillanos.campusfest.application.usecase;

import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Asistencia;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.domain.Reserva;

public class RegistrarIngreso {

    private final RepositorioEventos repositorio;
    private final Reloj reloj;

    public RegistrarIngreso(RepositorioEventos repositorio, Reloj reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    public Asistencia registrar(String eventoId, String codigo) {
        Evento evento = repositorio.buscar(eventoId);
        if (evento == null) {
            throw new IllegalArgumentException("El evento no existe: " + eventoId);
        }
        Reserva reserva = evento.buscarReservaPorCodigo(codigo);
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no pertenece al evento: " + codigo);
        }
        Asistencia asistencia = reserva.registrarIngreso(reloj.ahora());
        repositorio.guardar(evento);
        return asistencia;
    }
}
