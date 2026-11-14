package co.edu.unillanos.campusfest.infrastructure.persistence;

import java.time.LocalDateTime;

import co.edu.unillanos.campusfest.application.port.Reloj;

public class RelojDelSistema implements Reloj {

    @Override
    public LocalDateTime ahora() {
        return LocalDateTime.now();
    }
}
