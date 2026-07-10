-- ============================================================
-- SISTEMA DE GESTIÓN DE TURNOS MÉDICOS
-- Script de creación de base de datos - PostgreSQL
-- ============================================================
-- Ejecutar primero (conectado a la base "postgres"):
--   CREATE DATABASE turnos_medicos WITH ENCODING 'UTF8';
-- Luego conectarse a turnos_medicos y ejecutar el resto.
-- ============================================================

-- ------------------------------------------------------------
-- TIPOS ENUMERADOS
-- ------------------------------------------------------------
-- Todo médico es administrador del sistema: el rol define
-- permisos de acceso, y la condición de médico se determina
-- por la existencia de una fila en la tabla medico.
CREATE TYPE rol_usuario AS ENUM ('ADMINISTRADOR', 'PACIENTE');

CREATE TYPE dia_semana AS ENUM ('LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO');

-- El turno nace CONFIRMADO: el paciente reserva directamente
-- sobre un horario validado como libre, sin instancia de
-- aprobación intermedia.
CREATE TYPE estado_turno AS ENUM ('CONFIRMADO', 'CANCELADO', 'ATENDIDO');

CREATE TYPE estado_financiador AS ENUM ('ACTIVO', 'INACTIVO');

CREATE TYPE estado_tratamiento AS ENUM ('EN_CURSO', 'FINALIZADO', 'SUSPENDIDO');

-- ------------------------------------------------------------
-- 1. USUARIO
-- Credenciales de acceso al sistema.
-- ------------------------------------------------------------
CREATE TABLE usuario (
    id_usuario      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dni             VARCHAR(10)  NOT NULL UNIQUE,
    contrasena      VARCHAR(255) NOT NULL,
    rol             rol_usuario  NOT NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ------------------------------------------------------------
-- 2. PACIENTE
-- id_usuario NOT NULL: todo paciente debe estar registrado,
-- ya que él mismo solicita sus turnos. Si el usuario se da de
-- baja, pasa a activo=false pero el registro persiste, por lo
-- que el paciente conserva su historial.
-- ------------------------------------------------------------
CREATE TABLE paciente (
    id_paciente      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario       INTEGER      NOT NULL UNIQUE REFERENCES usuario (id_usuario),
    nombre_apellido  VARCHAR(100) NOT NULL,
    dni              VARCHAR(10)  NOT NULL UNIQUE,
    fecha_nacimiento DATE         NOT NULL,
    email            VARCHAR(100),
    celular          VARCHAR(20),
    domicilio        VARCHAR(150),
    activo           BOOLEAN      NOT NULL DEFAULT TRUE
);