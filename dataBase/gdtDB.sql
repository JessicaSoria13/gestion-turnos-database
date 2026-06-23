-- Database: gestionDeTurnos

-- DROP DATABASE IF EXISTS "gestionDeTurnos";

CREATE DATABASE "gestionDeTurnos"
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Spanish_Bolivia.1252'
    LC_CTYPE = 'Spanish_Bolivia.1252'
    LOCALE_PROVIDER = 'libc'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = False;

CREATE TABLE usuario (
    id_usuario   INT          NOT NULL GENERATED ALWAYS AS IDENTITY,
    dni          VARCHAR(20)  NOT NULL UNIQUE,
    contrasena   VARCHAR(255) NOT NULL,
    rol          VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id_usuario),
    CONSTRAINT chk_rol CHECK (rol IN ('SECRETARIA', 'MEDICO', 'PACIENTE', 'ADMINISTRADOR'))
);

    CREATE TABLE paciente (
    id_paciente      INT          NOT NULL GENERATED ALWAYS AS IDENTITY,
    id_usuario       INT          NOT NULL,
    dni              VARCHAR(20)  NOT NULL UNIQUE,
    nombre_apellido  VARCHAR(100) NOT NULL,
    email            VARCHAR(100),
    celular          VARCHAR(20),
    PRIMARY KEY (id_paciente),
    CONSTRAINT fk_paciente_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);


CREATE TABLE turno (
    id_turno     INT          NOT NULL GENERATED ALWAYS AS IDENTITY,
    id_paciente  INT          NOT NULL,
    id_medico    INT          NOT NULL,
    id_cobertura INT,
    fecha_hora   TIMESTAMP    NOT NULL,
    estado       VARCHAR(20)  NOT NULL DEFAULT 'pendiente',
    motivo       VARCHAR(255),
    PRIMARY KEY (id_turno),
    CONSTRAINT fk_turno_paciente
        FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente),
    CONSTRAINT fk_turno_medico
        FOREIGN KEY (id_medico) REFERENCES medico(id_medico),
    CONSTRAINT fk_turno_cobertura
        FOREIGN KEY (id_cobertura) REFERENCES cobertura(id_cobertura)
);

CREATE TABLE consulta_medica (
    id_consulta    INT      NOT NULL GENERATED ALWAYS AS IDENTITY,
    id_turno       INT      NOT NULL UNIQUE,
    observaciones  TEXT,
    fecha_hora     TIMESTAMP NOT NULL,
    PRIMARY KEY (id_consulta),
    CONSTRAINT fk_consulta_turno
        FOREIGN KEY (id_turno) REFERENCES turno(id_turno)
);