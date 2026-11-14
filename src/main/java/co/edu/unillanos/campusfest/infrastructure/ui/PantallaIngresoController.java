package co.edu.unillanos.campusfest.infrastructure.ui;

import java.time.format.DateTimeFormatter;

import co.edu.unillanos.campusfest.application.usecase.RegistrarIngreso;
import co.edu.unillanos.campusfest.domain.Asistencia;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PantallaIngresoController {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private TextField txtEventoId;

    @FXML
    private TextField txtCodigoQr;

    @FXML
    private Label lblResultado;

    private RegistrarIngreso registrarIngreso;

    public void setCasoDeUso(RegistrarIngreso registrarIngreso) {
        this.registrarIngreso = registrarIngreso;
    }

    @FXML
    public void onRegistrarIngreso() {
        String eventoId = txtEventoId.getText().trim();
        String codigo = txtCodigoQr.getText().trim();
        try {
            Asistencia asistencia = registrarIngreso.registrar(eventoId, codigo);
            lblResultado.setText("Ingreso registrado (" + asistencia.getHora().format(FORMATO_HORA) + ")");
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblResultado.setText("Error: " + e.getMessage());
        }
    }
}
