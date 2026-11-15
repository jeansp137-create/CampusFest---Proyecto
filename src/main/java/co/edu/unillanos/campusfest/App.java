package co.edu.unillanos.campusfest;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

import co.edu.unillanos.campusfest.application.port.Reloj;
import co.edu.unillanos.campusfest.application.port.RepositorioEventos;
import co.edu.unillanos.campusfest.application.usecase.ConsultarCupos;
import co.edu.unillanos.campusfest.application.usecase.EditarEvento;
import co.edu.unillanos.campusfest.application.usecase.RegistrarIngreso;
import co.edu.unillanos.campusfest.application.usecase.ReservarEntrada;
import co.edu.unillanos.campusfest.domain.Evento;
import co.edu.unillanos.campusfest.infrastructure.persistence.RepositorioEventosH2;
import co.edu.unillanos.campusfest.infrastructure.reloj.RelojDelSistema;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaCuposController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaEventoController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaIngresoController;
import co.edu.unillanos.campusfest.infrastructure.ui.PantallaReservaController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class App extends Application {

    private static final String RUTA_VISTAS = "/co/edu/unillanos/campusfest/vistas/";

    @Override
    public void start(Stage primaryStage) throws SQLException, IOException {
        Connection conexion = DriverManager.getConnection("jdbc:h2:./campusfest-db;AUTO_SERVER=TRUE", "sa", "");
        RepositorioEventos repositorio = new RepositorioEventosH2(conexion);
        Reloj reloj = new RelojDelSistema();

        if (repositorio.buscar("ev-1") == null) {
            Evento eventoInicial = new Evento(
                    "ev-1",
                    "Concierto de bienvenida CampusFest",
                    LocalDateTime.of(2026, 11, 28, 19, 0),
                    "Auditorio Barcelona",
                    120,
                    5
            );
            repositorio.guardar(eventoInicial);
        }

        ConsultarCupos consultarCupos = new ConsultarCupos(repositorio, reloj);
        ReservarEntrada reservarEntrada = new ReservarEntrada(repositorio, reloj);
        EditarEvento editarEvento = new EditarEvento(repositorio, reloj);
        RegistrarIngreso registrarIngreso = new RegistrarIngreso(repositorio, reloj);

        PantallaCuposController controlCupos = new PantallaCuposController(consultarCupos);
        PantallaReservaController controlReserva = new PantallaReservaController(reservarEntrada);
        PantallaEventoController controlEvento = new PantallaEventoController(editarEvento);
        PantallaIngresoController controlIngreso = new PantallaIngresoController(registrarIngreso);

        Parent vistaCupos = cargarVista("pantalla-cupos.fxml", controlCupos);
        Parent vistaReserva = cargarVista("pantalla-reserva.fxml", controlReserva);
        Parent vistaEvento = cargarVista("pantalla-evento.fxml", controlEvento);
        Parent vistaIngreso = cargarVista("pantalla-ingreso.fxml", controlIngreso);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().add(new Tab("CU1 · Consultar Cupos", vistaCupos));
        tabPane.getTabs().add(new Tab("CU2 · Reservar Entrada", vistaReserva));
        tabPane.getTabs().add(new Tab("CU3 · Editar Evento", vistaEvento));
        tabPane.getTabs().add(new Tab("CU4 · Registrar Ingreso", vistaIngreso));

        Scene scene = new Scene(tabPane, 650, 550);
        URL cssUrl = getClass().getResource(RUTA_VISTAS + "estilos.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setTitle("CampusFest · Sistema de Gestión de Eventos");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Parent cargarVista(String archivoFxml, Object controlador) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(RUTA_VISTAS + archivoFxml));
        loader.setController(controlador);
        return new ScrollPane(loader.load());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
