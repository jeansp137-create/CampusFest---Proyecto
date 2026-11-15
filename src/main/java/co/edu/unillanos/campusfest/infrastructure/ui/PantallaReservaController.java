package co.edu.unillanos.campusfest.infrastructure.ui;

import co.edu.unillanos.campusfest.application.usecase.ReservarEntrada;
import co.edu.unillanos.campusfest.domain.Reserva;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PantallaReservaController {

    private final ReservarEntrada reservarEntrada;

    @FXML
    private TextField txtEventoId;

    @FXML
    private TextField txtEstudianteId;

    @FXML
    private TextField txtCodigoReserva;

    @FXML
    private Label etiquetaMensaje;

    public PantallaReservaController(ReservarEntrada reservarEntrada) {
        this.reservarEntrada = reservarEntrada;
    }

    @FXML
    public void onSolicitar() {
        String eventoId = txtEventoId.getText().trim();
        String estudianteId = txtEstudianteId.getText().trim();
        try {
            Reserva reserva = reservarEntrada.solicitarReserva(eventoId, estudianteId);
            txtCodigoReserva.setText(reserva.getCodigo());
            etiquetaMensaje.setText("Reserva creada. Código: " + reserva.getCodigo());
        } catch (IllegalArgumentException | IllegalStateException e) {
            etiquetaMensaje.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void onConfirmar() {
        String eventoId = txtEventoId.getText().trim();
        String codigo = txtCodigoReserva.getText().trim();
        try {
            Reserva confirmada = reservarEntrada.confirmarReserva(eventoId, codigo);
            etiquetaMensaje.setText("Reserva confirmada. Código: " + confirmada.getCodigo());
        } catch (IllegalArgumentException | IllegalStateException e) {
            etiquetaMensaje.setText("Error: " + e.getMessage());
        }
    }
}
