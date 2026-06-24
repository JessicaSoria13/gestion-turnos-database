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

    CREATE TABLE diagnostico (
    id_diagnostico  SERIAL  NOT NULL,
    id_consulta     INT     NOT NULL,
    descripcion     TEXT    NOT NULL,
    PRIMARY KEY (id_diagnostico),
    CONSTRAINT fk_diagnostico_consulta
        FOREIGN KEY (id_consulta) REFERENCES consulta_medica(id_consulta)
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
    
    CREATE TABLE medico (
    id_medico SERIAL NOT NULL,
    nombre_apellido VARCHAR(100) NOT NULL,
    especialidad VARCHAR(100),
    matricula VARCHAR(50),
    PRIMARY KEY (id_medico)
);
CREATE TABLE disponibilidad (
    id_disponibilidad  SERIAL      NOT NULL,
    id_medico          INT         NOT NULL,
    dia_semana         VARCHAR(20) NOT NULL,
    hora_inicio        TIME        NOT NULL,
    hora_fin           TIME        NOT NULL,
    PRIMARY KEY (id_disponibilidad),
    CONSTRAINT fk_disponibilidad_medico
        FOREIGN KEY (id_medico) REFERENCES medico(id_medico)
);

CREATE TABLE obra_social (
    id_obra_social  SERIAL       PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    estado          VARCHAR(20)
);

CREATE TABLE cobertura (
    id_cobertura    SERIAL      PRIMARY KEY,
    id_paciente     INT         NOT NULL,
    id_obra_social  INT,
    num_afiliado    VARCHAR(50),
    plan            VARCHAR(50),
    tiene_cobertura BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_cobertura_paciente
        FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente),
    CONSTRAINT fk_cobertura_obra_social
        FOREIGN KEY (id_obra_social) REFERENCES obra_social(id_obra_social)
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