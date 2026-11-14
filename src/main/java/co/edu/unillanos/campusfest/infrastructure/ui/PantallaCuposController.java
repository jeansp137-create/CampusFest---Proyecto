package co.edu.unillanos.campusfest.infrastructure.ui;

import co.edu.unillanos.campusfest.application.usecase.ConsultarCupos;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PantallaCuposController {

    @FXML
    private TextField txtEventoId;

    @FXML
    private Label lblResultado;

    private ConsultarCupos consultarCupos;

    public PantallaCuposController() {
    }

    public PantallaCuposController(ConsultarCupos consultarCupos) {
        this.consultarCupos = consultarCupos;
    }

    public void setCasoDeUso(ConsultarCupos consultarCupos) {
        this.consultarCupos = consultarCupos;
    }

    @FXML
    public void onConsultar() {
        String eventoId = txtEventoId.getText().trim();
        try {
            int disponibles = consultarCupos.consultar(eventoId);
            lblResultado.setText("Cupos disponibles: " + disponibles);
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblResultado.setText("Error: " + e.getMessage());
        }
    }
}