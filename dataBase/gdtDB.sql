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
    
    CREATE TABLE medico (
    id_medico SERIAL NOT NULL,
    nombre_apellido VARCHAR(100) NOT NULL,
    especialidad VARCHAR(100),
    matricula VARCHAR(50),
    PRIMARY KEY (id_medico)
);
