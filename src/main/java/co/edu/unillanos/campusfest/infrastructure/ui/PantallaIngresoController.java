package co.edu.unillanos.campusfest.infrastructure.ui;

import java.time.format.DateTimeFormatter;

import co.edu.unillanos.campusfest.application.usecase.RegistrarIngreso;
import co.edu.unillanos.campusfest.domain.Asistencia;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PantallaIngresoController {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final RegistrarIngreso registrarIngreso;

    @FXML
    private TextField txtEventoId;

    @FXML
    private TextField txtCodigoQr;

    @FXML
    private Label etiquetaMensaje;

    public PantallaIngresoController(RegistrarIngreso registrarIngreso) {
        this.registrarIngreso = registrarIngreso;
    }

    @FXML
    public void onRegistrar() {
        String eventoId = txtEventoId.getText().trim();
        String codigo = txtCodigoQr.getText().trim();
        try {
            Asistencia asistencia = registrarIngreso.registrar(eventoId, codigo);
            etiquetaMensaje.setText("Ingreso registrado (" + asistencia.getHora().format(FORMATO_HORA) + ")");
        } catch (IllegalArgumentException | IllegalStateException e) {
            etiquetaMensaje.setText("Error: " + e.getMessage());
        }
    }
}
