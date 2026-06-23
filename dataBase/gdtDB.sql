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
<<<<<<< HEAD

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
=======
    
    CREATE TABLE medico (
    id_medico SERIAL NOT NULL,
    nombre_apellido VARCHAR(100) NOT NULL,
    especialidad VARCHAR(100),
    matricula VARCHAR(50),
    PRIMARY KEY (id_medico)
);
>>>>>>> feature/jessica
