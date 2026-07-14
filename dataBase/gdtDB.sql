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

-- ------------------------------------------------------------
-- 3. MEDICO
-- id_usuario NOT NULL: el médico debe poder autenticarse.
-- ------------------------------------------------------------
CREATE TABLE medico (
    id_medico        INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario       INTEGER      NOT NULL UNIQUE REFERENCES usuario (id_usuario),
    nombre_apellido  VARCHAR(100) NOT NULL,
    dni              VARCHAR(10)  NOT NULL UNIQUE,
    matricula        VARCHAR(20)  NOT NULL UNIQUE,
    especialidad     VARCHAR(100) NOT NULL,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ------------------------------------------------------------
-- 4. DISPONIBILIDAD
-- Reglas semanales recurrentes. Los horarios puntuales se
-- calculan dinámicamente; no se almacenan slots.
-- ------------------------------------------------------------
CREATE TABLE disponibilidad (
    id_disponibilidad  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_medico          INTEGER    NOT NULL REFERENCES medico (id_medico),
    dia_semana         dia_semana NOT NULL,
    hora_inicio        TIME       NOT NULL,
    hora_fin           TIME       NOT NULL,
    duracion_turno_min INTEGER    NOT NULL,
    activo             BOOLEAN    NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_disp_horas    CHECK (hora_fin > hora_inicio),
    CONSTRAINT chk_disp_duracion CHECK (duracion_turno_min > 0)
);

-- ------------------------------------------------------------
-- 5. FINANCIADOR (Obra Social)
-- La baja es lógica: estado = 'INACTIVO'.
-- ------------------------------------------------------------
CREATE TABLE financiador (
    id_financiador INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre         VARCHAR(100)       NOT NULL UNIQUE,
    cuit           VARCHAR(13),
    estado         estado_financiador NOT NULL DEFAULT 'ACTIVO'
);

-- ------------------------------------------------------------
-- 6. COBERTURA
-- Relación paciente <-> financiador (afiliación).
-- ------------------------------------------------------------
CREATE TABLE cobertura (
    id_cobertura    INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_paciente     INTEGER     NOT NULL REFERENCES paciente (id_paciente),
    id_financiador  INTEGER     NOT NULL REFERENCES financiador (id_financiador),
    numero_afiliado VARCHAR(30) NOT NULL,
    plan            VARCHAR(50),
    activo          BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_cobertura UNIQUE (id_paciente, id_financiador, numero_afiliado)
);

-- ------------------------------------------------------------
-- 7. TURNO
-- id_disponibilidad garantiza que el horario provenga de un
-- bloque válido del médico. id_cobertura NULL = atención
-- particular. Nunca se eliminan: la cancelación es por estado.
-- ------------------------------------------------------------
CREATE TABLE turno (
    id_turno           INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_paciente        INTEGER      NOT NULL REFERENCES paciente (id_paciente),
    id_medico          INTEGER      NOT NULL REFERENCES medico (id_medico),
    id_disponibilidad  INTEGER      NOT NULL REFERENCES disponibilidad (id_disponibilidad),
    id_cobertura       INTEGER      REFERENCES cobertura (id_cobertura),
    fecha              DATE         NOT NULL,
    hora               TIME         NOT NULL,
    estado             estado_turno NOT NULL DEFAULT 'CONFIRMADO',
    motivo_consulta    VARCHAR(255),
    motivo_cancelacion VARCHAR(255),
    CONSTRAINT uq_turno_medico_horario UNIQUE (id_medico, fecha, hora)
);

-- ------------------------------------------------------------
-- 8. CONSULTA_MEDICA
-- Relación 1 a 1 con turno (id_turno UNIQUE). No almacena
-- fecha_hora propia: la fecha y hora surgen del turno.
-- ------------------------------------------------------------
CREATE TABLE consulta_medica (
    id_consulta   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_turno      INTEGER NOT NULL UNIQUE REFERENCES turno (id_turno),
    observaciones TEXT
);

-- ------------------------------------------------------------
-- 9. DIAGNOSTICO
-- Una consulta puede derivar en uno o varios diagnósticos.
-- ------------------------------------------------------------
CREATE TABLE diagnostico (
    id_diagnostico INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_consulta    INTEGER NOT NULL REFERENCES consulta_medica (id_consulta),
    descripcion    TEXT    NOT NULL
);

-- ------------------------------------------------------------
-- 10. TRATAMIENTO
-- Un diagnóstico puede derivar en uno o varios tratamientos.
-- ------------------------------------------------------------
CREATE TABLE tratamiento (
    id_tratamiento INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_diagnostico INTEGER            NOT NULL REFERENCES diagnostico (id_diagnostico),
    descripcion    TEXT               NOT NULL,
    duracion       VARCHAR(50),
    estado         estado_tratamiento NOT NULL DEFAULT 'EN_CURSO'
);

-- ------------------------------------------------------------
-- 11. BLOQUEO
-- Excepciones a la disponibilidad semanal: feriados, vacaciones,
-- ausencias. Un rango de fechas donde el médico no atiende.
-- El bloqueo tiene prioridad sobre la regla de disponibilidad.
-- ------------------------------------------------------------
CREATE TABLE bloqueo (
    id_bloqueo   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_medico    INTEGER NOT NULL REFERENCES medico (id_medico),
    fecha_desde  DATE    NOT NULL,
    fecha_hasta  DATE    NOT NULL,
    motivo       VARCHAR(100),
    activo       BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_bloqueo_rango CHECK (fecha_hasta >= fecha_desde)
);


-- ------------------------------------------------------------
-- ÍNDICES DE APOYO
-- Para las consultas más frecuentes (agenda y cálculo de
-- horarios disponibles).
-- ------------------------------------------------------------

CREATE INDEX idx_bloqueo_medico_fechas ON bloqueo (id_medico, fecha_desde, fecha_hasta)
CREATE INDEX idx_turno_medico_fecha      ON turno (id_medico, fecha);
CREATE INDEX idx_turno_paciente          ON turno (id_paciente);
CREATE INDEX idx_disponibilidad_medico   ON disponibilidad (id_medico, dia_semana);
CREATE INDEX idx_cobertura_paciente      ON cobertura (id_paciente);

-- ------------------------------------------------------------
-- DATOS DE PRUEBA
-- ------------------------------------------------------------
-- rol ADMINISTRADOR: todo médico es
-- administrador del sistema.
INSERT INTO usuario (dni, contrasena, rol) VALUES
('30111222', '$2a$10$hashDeEjemplo1', 'ADMINISTRADOR'),
('32111222', '$2a$10$hashDeEjemplo2', 'PACIENTE');

INSERT INTO medico (id_usuario, nombre_apellido, dni, matricula, especialidad) VALUES
(1, 'Dra. Ana López', '30111222', 'MP-12345', 'Clínica Médica');

INSERT INTO paciente (id_usuario, nombre_apellido, dni, fecha_nacimiento, email, celular) VALUES
(2, 'Juan Pérez', '32111222', '1990-02-15', 'juan@gmail.com', '1122334455');

INSERT INTO financiador (nombre, cuit) VALUES
('IOMA', '30-99999999-9'),
('OSDE', '30-88888888-8');

INSERT INTO cobertura (id_paciente, id_financiador, numero_afiliado, plan) VALUES
(1, 2, '123456789', '210');

INSERT INTO disponibilidad (id_medico, dia_semana, hora_inicio, hora_fin, duracion_turno_min) VALUES
(1, 'LUNES',     '09:00', '17:00', 30),
(1, 'MIERCOLES', '09:00', '13:00', 30);