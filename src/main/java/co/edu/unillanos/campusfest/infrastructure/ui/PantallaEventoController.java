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
    private Label lblResultado;

    @FXML
    private Label etiquetaMensaje;

    private EditarEvento editarEvento;

    public PantallaEventoController() {
    }

    public PantallaEventoController(EditarEvento editarEvento) {
        this.editarEvento = editarEvento;
    }

    public void setCasoDeUso(EditarEvento editarEvento) {
        this.editarEvento = editarEvento;
    }

    @FXML
    public void onObtener() {
        String eventoId = txtEventoId != null ? txtEventoId.getText().trim() : "";
        try {
            Evento evento = editarEvento.obtener(eventoId);
            if (txtNombre != null) {
                txtNombre.setText(evento.getNombre());
            }
            if (txtFecha != null && evento.getFecha() != null) {
                txtFecha.setText(evento.getFecha().format(FORMATO_FECHA));
            }
            if (txtLugar != null) {
                txtLugar.setText(evento.getLugar());
            }
            if (txtAforo != null) {
                txtAforo.setText(String.valueOf(evento.getAforo()));
            }
            mostrarMensaje("Evento cargado correctamente.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensaje("Error: " + e.getMessage());
        } catch (Exception e) {
            mostrarMensaje("Error: " + e.getMessage());
        }
    }

    @FXML
    public void onEditar() {
        String eventoId = txtEventoId != null ? txtEventoId.getText().trim() : "";
        String nombre = txtNombre != null ? txtNombre.getText().trim() : "";
        String lugar = txtLugar != null ? txtLugar.getText().trim() : "";
        String strFecha = txtFecha != null ? txtFecha.getText().trim() : "";
        String strAforo = txtAforo != null ? txtAforo.getText().trim() : "";

        try {
            LocalDateTime fecha;
            try {
                fecha = LocalDateTime.parse(strFecha, FORMATO_FECHA);
            } catch (DateTimeParseException ex) {
                fecha = LocalDateTime.parse(strFecha);
            }

            int aforo = Integer.parseInt(strAforo);
            editarEvento.editar(eventoId, nombre, fecha, lugar, aforo);
            mostrarMensaje("Evento actualizado con éxito.");
        } catch (NumberFormatException e) {
            mostrarMensaje("Error: El aforo debe ser un número entero válido.");
        } catch (DateTimeParseException e) {
            mostrarMensaje("Error: Formato de fecha inválido. Use yyyy-MM-dd HH:mm (ej. 2026-11-28 19:00)");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarMensaje("Error: " + e.getMessage());
        } catch (Exception e) {
            mostrarMensaje("Error: " + e.getMessage());
        }
    }

    private void mostrarMensaje(String mensaje) {
        if (lblResultado != null) {
            lblResultado.setText(mensaje);
        }
        if (etiquetaMensaje != null) {
            etiquetaMensaje.setText(mensaje);
        }
    }
}
