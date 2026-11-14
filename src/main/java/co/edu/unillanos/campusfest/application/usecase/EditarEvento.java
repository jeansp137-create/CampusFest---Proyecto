package co.edu.unillanos.campusfest.application.usecase;
import java.time.LocalDateTime;
import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.domain.Evento;
public class EditarEvento {
    public EditarEvento(RepositorioEventos repositorio, Reloj reloj) {}
    public Evento obtener(String eventoId) { throw new UnsupportedOperationException(); }
    public void editar(String eventoId, String nombre, LocalDateTime fecha, String lugar, int nuevoAforo) { throw new UnsupportedOperationException(); }
}
