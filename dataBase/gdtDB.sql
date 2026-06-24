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
);