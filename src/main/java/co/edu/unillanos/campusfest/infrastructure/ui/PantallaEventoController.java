package co.edu.unillanos.campusfest.infrastructure.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import co.edu.unillanos.campusfest.application.usecase.EditarEvento;
import co.edu.unillanos.campusfest.domain.Evento;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PantallaEventoController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final EditarEvento editarEvento;

    @FXML
    private TextField txtEventoId;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtFecha;

    @FXML
    private TextField txtLugar;

    @FXML
    private TextField txtAforo;

    @FXML
    private Label etiquetaMensaje;

    public PantallaEventoController(EditarEvento editarEvento) {
        this.editarEvento = editarEvento;
    }

    @FXML
    public void onObtener() {
        String eventoId = txtEventoId.getText().trim();
        try {
            Evento evento = editarEvento.obtener(eventoId);
            txtNombre.setText(evento.getNombre());
            txtFecha.setText(evento.getFecha().format(FORMATO_FECHA));
            txtLugar.setText(evento.getLugar());
            txtAforo.setText(String.valueOf(evento.getAforo()));
            etiquetaMensaje.setText("Evento cargado correctamente.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            etiquetaMensaje.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void onEditar() {
        String eventoId = txtEventoId.getText().trim();
        String nombre = txtNombre.getText().trim();
        String lugar = txtLugar.getText().trim();
        String textoFecha = txtFecha.getText().trim();
        String textoAforo = txtAforo.getText().trim();
        try {
            LocalDateTime fecha = LocalDateTime.parse(textoFecha, FORMATO_FECHA);
            int nuevoAforo = Integer.parseInt(textoAforo);
            editarEvento.editar(eventoId, nombre, fecha, lugar, nuevoAforo);
            etiquetaMensaje.setText("Evento actualizado con éxito.");
        } catch (DateTimeParseException | IllegalArgumentException | IllegalStateException e) {
            etiquetaMensaje.setText("Error: " + e.getMessage());
        }
    }
}
