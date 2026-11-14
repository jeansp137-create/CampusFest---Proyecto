package co.edu.unillanos.campusfest.infrastructure.ui;

import co.edu.unillanos.campusfest.application.usecase.ReservarEntrada;
import co.edu.unillanos.campusfest.domain.Reserva;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PantallaReservaController {

    @FXML
    private TextField txtEventoId;

    @FXML
    private TextField txtEstudianteId;

    @FXML
    private TextField txtCodigoReserva;

    @FXML
    private Label lblResultado;

    private ReservarEntrada reservarEntrada;

    public PantallaReservaController() {
    }

    public PantallaReservaController(ReservarEntrada reservarEntrada) {
        this.reservarEntrada = reservarEntrada;
    }

    public void setCasoDeUso(ReservarEntrada reservarEntrada) {
        this.reservarEntrada = reservarEntrada;
    }

    @FXML
    public void onSolicitarReserva() {
        String eventoId = txtEventoId.getText().trim();
        String estudianteId = txtEstudianteId.getText().trim();
        try {
            Reserva reserva = reservarEntrada.solicitarReserva(eventoId, estudianteId);
            txtCodigoReserva.setText(reserva.getCodigo());
            lblResultado.setText("Reserva pendiente creada. Código: " + reserva.getCodigo());
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblResultado.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void onConfirmarReserva() {
        String eventoId = txtEventoId.getText().trim();
        String codigo = txtCodigoReserva.getText().trim();
        try {
            Reserva confirmada = reservarEntrada.confirmarReserva(eventoId, codigo);
            lblResultado.setText("Reserva confirmada con éxito. Código: " + confirmada.getCodigo());
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblResultado.setText("Error: " + e.getMessage());
        }
    }
}
