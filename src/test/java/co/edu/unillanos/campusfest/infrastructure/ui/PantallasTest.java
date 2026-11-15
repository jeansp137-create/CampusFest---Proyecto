package co.edu.unillanos.campusfest.infrastructure.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import co.edu.unillanos.campusfest.application.usecase.ConsultarCupos;
import co.edu.unillanos.campusfest.application.usecase.EditarEvento;
import co.edu.unillanos.campusfest.application.usecase.RegistrarIngreso;
import co.edu.unillanos.campusfest.application.usecase.ReservarEntrada;
import javafx.fxml.FXML;

/**
 * Pruebas de integración estética y arquitectónica de Pantallas y App.
 * 14 pruebas según el contrato docente, leyendo FXML y código sin abrir ventanas.
 */
class PantallasTest {

    private static final String RUTA_VISTAS = "src/main/resources/co/edu/unillanos/campusfest/vistas/";
    private static final String[] ARCHIVOS_FXML = {
        "pantalla-cupos.fxml",
        "pantalla-reserva.fxml",
        "pantalla-evento.fxml",
        "pantalla-ingreso.fxml"
    };

    @Test
    void lasCuatroVistasFxmlExistenEnLaCarpetaVistas() {
        for (String archivo : ARCHIVOS_FXML) {
            File f = new File(RUTA_VISTAS + archivo);
            assertTrue(f.exists(), "Debe existir la vista: " + archivo);
        }
    }

    @Test
    void losArchivosFxmlNoDeclaranFxController() throws IOException {
        for (String archivo : ARCHIVOS_FXML) {
            String contenido = Files.readString(new File(RUTA_VISTAS + archivo).toPath());
            assertFalse(contenido.contains("fx:controller="),
                    "El FXML " + archivo + " no debe declarar fx:controller; App lo asigna con setController.");
        }
    }

    @Test
    void cadaVistaFxmlTieneEtiquetaMensaje() throws IOException {
        for (String archivo : ARCHIVOS_FXML) {
            String contenido = Files.readString(new File(RUTA_VISTAS + archivo).toPath());
            assertTrue(contenido.contains("fx:id=\"etiquetaMensaje\""),
                    "El FXML " + archivo + " debe tener fx:id=\"etiquetaMensaje\"");
        }
    }

    @Test
    void cadaVistaFxmlTieneAlMenosUnaAccion() throws IOException {
        for (String archivo : ARCHIVOS_FXML) {
            String contenido = Files.readString(new File(RUTA_VISTAS + archivo).toPath());
            assertTrue(contenido.contains("onAction=\"#"),
                    "El FXML " + archivo + " debe tener al menos una acción onAction=\"#metodo\"");
        }
    }

    @Test
    void losControladoresTienenConstructorConCasoDeUso() {
        assertNotNull(obtenerConstructor(PantallaCuposController.class, ConsultarCupos.class));
        assertNotNull(obtenerConstructor(PantallaReservaController.class, ReservarEntrada.class));
        assertNotNull(obtenerConstructor(PantallaEventoController.class, EditarEvento.class));
        assertNotNull(obtenerConstructor(PantallaIngresoController.class, RegistrarIngreso.class));
    }

    private Constructor<?> obtenerConstructor(Class<?> clase, Class<?> parametro) {
        try {
            return clase.getConstructor(parametro);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Test
    void losCamposFxIdTienenSuAtributoPrivadoConAnotacion() {
        Class<?>[] controladores = {
            PantallaCuposController.class,
            PantallaReservaController.class,
            PantallaEventoController.class,
            PantallaIngresoController.class
        };
        for (Class<?> clase : controladores) {
            for (Field f : clase.getDeclaredFields()) {
                if (f.isAnnotationPresent(FXML.class)) {
                    assertTrue(Modifier.isPrivate(f.getModifiers()),
                            "El campo @FXML " + f.getName() + " en " + clase.getSimpleName() + " debe ser privado.");
                }
            }
        }
    }

    @Test
    void lasAccionesDelFxmlTienenSuMetodoConAnotacionFxml() {
        Class<?>[] controladores = {
            PantallaCuposController.class,
            PantallaReservaController.class,
            PantallaEventoController.class,
            PantallaIngresoController.class
        };
        for (Class<?> clase : controladores) {
            boolean tieneMetodoFxml = false;
            for (Method m : clase.getDeclaredMethods()) {
                if (m.isAnnotationPresent(FXML.class)) {
                    tieneMetodoFxml = true;
                    break;
                }
            }
            assertTrue(tieneMetodoFxml, "El controlador " + clase.getSimpleName() + " debe tener métodos @FXML");
        }
    }

    @Test
    void losControladoresNoTienenReglasDelNegocioNiMetodosDeEstado() throws IOException {
        File dirUI = new File("src/main/java/co/edu/unillanos/campusfest/infrastructure/ui");
        String[] prohibidos = {
            "cuposDisponibles(",
            "ocupaCupo(",
            "getEstado(",
            "reservar(",
            "confirmar(",
            "cancelar(",
            "registrarIngreso(",
            "cambiarAforo("
        };
        for (File f : dirUI.listFiles((d, name) -> name.endsWith("Controller.java"))) {
            List<String> lineas = Files.readAllLines(f.toPath());
            for (int i = 0; i < lineas.size(); i++) {
                String linea = lineas.get(i).trim();
                if (linea.startsWith("//") || linea.startsWith("*")) {
                    continue;
                }
                for (String p : prohibidos) {
                    assertFalse(linea.contains(p),
                            "Regla de negocio o mutación de estado en " + f.getName() + ":" + (i + 1) + ": " + linea);
                }
            }
        }
    }

    @Test
    void losControladoresNoConocenRepositorioRelojNiBaseDeDatos() throws IOException {
        File dirUI = new File("src/main/java/co/edu/unillanos/campusfest/infrastructure/ui");
        String[] prohibidos = {
            "import co.edu.unillanos.campusfest.application.port.RepositorioEventos",
            "import co.edu.unillanos.campusfest.application.port.Reloj",
            "import java.sql.",
            "import org.h2."
        };
        for (File f : dirUI.listFiles((d, name) -> name.endsWith("Controller.java"))) {
            String contenido = Files.readString(f.toPath());
            for (String p : prohibidos) {
                assertFalse(contenido.contains(p),
                        "Import indebido en " + f.getName() + ": " + p);
            }
        }
    }

    @Test
    void losControladoresCapturanExcepcionesYMuestranElMensaje() throws IOException {
        File dirUI = new File("src/main/java/co/edu/unillanos/campusfest/infrastructure/ui");
        for (File f : dirUI.listFiles((d, name) -> name.endsWith("Controller.java"))) {
            String contenido = Files.readString(f.toPath());
            assertTrue(contenido.contains("catch (") || contenido.contains("catch("),
                    "El controlador " + f.getName() + " debe tener bloques catch");
            assertTrue(contenido.contains("etiquetaMensaje.setText("),
                    "El controlador " + f.getName() + " debe asignar el resultado o error a etiquetaMensaje");
            assertTrue(contenido.contains("getMessage()"),
                    "El controlador " + f.getName() + " debe mostrar getMessage() en caso de error");
        }
    }

    @Test
    void elCssEstaEnVistasYTodasLasPropiedadesLlevanPrefijoFx() throws IOException {
        File css = new File(RUTA_VISTAS + "estilos.css");
        assertTrue(css.exists(), "Debe existir estilos.css en " + RUTA_VISTAS);
        List<String> lineas = Files.readAllLines(css.toPath());
        for (String linea : lineas) {
            String l = linea.trim();
            if (l.contains(":") && !l.startsWith("/*") && !l.startsWith("*") && !l.endsWith("{") && !l.startsWith(".")) {
                assertTrue(l.startsWith("-fx-"), "La propiedad CSS debe iniciar con -fx-: " + l);
            }
        }
    }

    @Test
    void appInstanciaLosAdaptadoresConcretos() throws IOException {
        String contenido = Files.readString(new File("src/main/java/co/edu/unillanos/campusfest/App.java").toPath());
        assertTrue(contenido.contains("new RepositorioEventosH2("), "App debe instanciar RepositorioEventosH2");
        assertTrue(contenido.contains("new RelojDelSistema()"), "App debe instanciar RelojDelSistema");
    }

    @Test
    void appEsLaUnicaClaseQueNombraLasImplementacionesConcretas() throws IOException {
        File srcDir = new File("src/main/java/co/edu/unillanos/campusfest");
        List<File> archivosJava = new ArrayList<>();
        recolectarArchivos(srcDir, archivosJava);

        for (File f : archivosJava) {
            if (f.getName().equals("App.java")) {
                continue;
            }
            String contenido = Files.readString(f.toPath());
            assertFalse(contenido.contains("new RepositorioEventosH2"),
                    "Solo App puede nombrar RepositorioEventosH2, encontrado en: " + f.getName());
            assertFalse(contenido.contains("new RelojDelSistema"),
                    "Solo App puede nombrar RelojDelSistema, encontrado en: " + f.getName());
            assertFalse(contenido.contains("new RepositorioEventosEnMemoria"),
                    "Solo App puede nombrar RepositorioEventosEnMemoria, encontrado en: " + f.getName());
        }
    }

    @Test
    void appCargaLosFxmlConSetController() throws IOException {
        String contenido = Files.readString(new File("src/main/java/co/edu/unillanos/campusfest/App.java").toPath());
        assertTrue(contenido.contains("setController("), "App debe usar setController para inyectar controladores");
    }

    private void recolectarArchivos(File dir, List<File> lista) {
        File[] hijos = dir.listFiles();
        if (hijos == null) return;
        for (File h : hijos) {
            if (h.isDirectory()) {
                recolectarArchivos(h, lista);
            } else if (h.getName().endsWith(".java")) {
                lista.add(h);
            }
        }
    }
}
