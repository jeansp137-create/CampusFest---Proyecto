package co.edu.unillanos.campusfest;

import java.net.URL;
import java.time.LocalDateTime;

import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.application.usecase.ConsultarCupos;
import co.edu.unillanos.campusfest.application.usecase.EditarEvento;
import co.edu.unillanos.campusfest.application.usecase.RegistrarIngreso;
import co.edu.unillanos.campusfest.application.usecase.ReservarEntrada;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.infrastructure.persistence.RelojDelSistema;
import co.edu.unillanos.campusfest.infrastructure.persistence.RepositorioEventosH2;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaCuposController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaEventoController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaIngresoController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaReservaController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    private RepositorioEventos repositorio;
    private Reloj reloj;

    private ConsultarCupos consultarCupos;
    private ReservarEntrada reservarEntrada;
    private EditarEvento editarEvento;
    private RegistrarIngreso registrarIngreso;

    @Override
    public void init() {
        // Fase 4: transición de memoria a persistencia en H2 sin tocar dominio ni casos de uso
        this.repositorio = new RepositorioEventosH2();
        this.reloj = new RelojDelSistema();

        // Precargar evento demo si aún no existe en la base de datos H2
        if (this.repositorio.buscar("ev-1") == null) {
            Evento eventoDemo = new Evento(
                    "ev-1",
                    "Concierto de bienvenida CampusFest",
                    LocalDateTime.of(2026, 11, 28, 19, 0),
                    "Auditorio Barcelona",
                    120,
                    5
            );
            this.repositorio.guardar(eventoDemo);
        }

        // Inicializar los cuatro casos de uso
        this.consultarCupos = new ConsultarCupos(repositorio, reloj);
        this.reservarEntrada = new ReservarEntrada(repositorio, reloj);
        this.editarEvento = new EditarEvento(repositorio, reloj);
        this.registrarIngreso = new RegistrarIngreso(repositorio, reloj);
    }

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Cargar vistas FXML de los cuatro casos de uso
        Parent vistaCupos = cargarVista("/co/edu/unillanos/campusfest/infrastructure/ui/PantallaCupos.fxml");
        Parent vistaReserva = cargarVista("/co/edu/unillanos/campusfest/infrastructure/ui/PantallaReserva.fxml");
        Parent vistaEvento = cargarVista("/co/edu/unillanos/campusfest/infrastructure/ui/PantallaEvento.fxml");
        Parent vistaIngreso = cargarVista("/co/edu/unillanos/campusfest/infrastructure/ui/PantallaIngreso.fxml");

        tabPane.getTabs().add(new Tab("CU1 · Consultar Cupos", vistaCupos));
        tabPane.getTabs().add(new Tab("CU2 · Reservar Entrada", vistaReserva));
        tabPane.getTabs().add(new Tab("CU3 · Editar Evento", vistaEvento));
        tabPane.getTabs().add(new Tab("CU4 · Registrar Ingreso", vistaIngreso));

        Scene scene = new Scene(tabPane, 650, 550);

        URL cssUrl = getClass().getResource("/co/edu/unillanos/campusfest/infrastructure/ui/estilos.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setTitle("CampusFest · Sistema de Gestión de Eventos");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Parent cargarVista(String rutaFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            Parent vista = loader.load();
            configurarControlador(loader.getController());
            return new ScrollPane(vista);
        } catch (Exception e) {
            return new VBox(new Label("Error cargando vista " + rutaFxml + ": " + e.getMessage()));
        }
    }

    private void configurarControlador(Object controlador) {
        if (controlador instanceof PantallaCuposController c) {
            c.setCasoDeUso(consultarCupos);
        } else if (controlador instanceof PantallaReservaController c) {
            c.setCasoDeUso(reservarEntrada);
        } else if (controlador instanceof PantallaEventoController c) {
            c.setCasoDeUso(editarEvento);
        } else if (controlador instanceof PantallaIngresoController c) {
            c.setCasoDeUso(registrarIngreso);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
