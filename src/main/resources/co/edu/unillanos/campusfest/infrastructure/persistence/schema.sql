CREATE TABLE IF NOT EXISTS eventos (
    id VARCHAR(64) PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    fecha TIMESTAMP NOT NULL,
    lugar VARCHAR(200) NOT NULL,
    aforo INT NOT NULL,
    bloqueados INT NOT NULL
);

CREATE TABLE IF NOT EXISTS reservas (
    codigo VARCHAR(64) PRIMARY KEY,
    evento_id VARCHAR(64) NOT NULL,
    estudiante_id VARCHAR(64) NOT NULL,
    vence_en TIMESTAMP NOT NULL,
    estado VARCHAR(20) NOT NULL,
    CONSTRAINT fk_reservas_evento FOREIGN KEY (evento_id) REFERENCES eventos(id)
);

CREATE TABLE IF NOT EXISTS asistencias (
    codigo_reserva VARCHAR(64) PRIMARY KEY,
    hora TIMESTAMP NOT NULL,
    CONSTRAINT fk_asistencias_reserva FOREIGN KEY (codigo_reserva) REFERENCES reservas(codigo)
);
