-- ============================================================
-- Datos de prueba (seed) - Sistema de Gestión de Turnos Médicos
-- ============================================================
-- Ejecutar contra la base turnos_medicos DESPUÉS de que el backend
-- haya levantado el esquema al menos una vez (spring.jpa.hibernate.ddl-auto=update
-- crea las tablas a partir de las entidades JPA).
--
-- Idempotente: al arrancar trunca todas las tablas del dominio
-- y reinicia los IDENTITY, así los IDs usados abajo (FKs
-- hardcodeadas) son siempre predecibles.
-- ============================================================
-- ============================================================
-- Datos de prueba (seed) - Sistema de Gestión de Turnos Médicos
-- ============================================================
-- Ejecutar contra la base turnos_medicos DESPUÉS de que el backend
-- haya levantado el esquema al menos una vez (spring.jpa.hibernate.ddl-auto=update
-- crea las tablas a partir de las entidades JPA).
--
-- Objetivo: que las 4 personas del equipo carguen EXACTAMENTE los
-- mismos datos en su base LOCAL (no compartida). Como las tablas
-- están vacías y los ID son autoincrementales, si todas ejecutan
-- este script una sola vez sobre un esquema recién creado, van a
-- terminar con los mismos ID (usuario 1, paciente 1, turno 1, etc.)
-- y por lo tanto las mismas URLs funcionan igual para todas.
--
-- Contraseña de todos los usuarios de prueba: "123456"
-- (hash BCrypt real, válido para cuando se implemente /auth/login).
--
-- Orden de carga: respeta las relaciones (FK) entre tablas.
-- ============================================================

-- ------------------------------------------------------------
-- 1. FINANCIADOR (sin dependencias)
-- ------------------------------------------------------------
INSERT INTO financiador (nombre, cuit, activo) VALUES
('IOMA', '30-99999999-9', true),  -- id_financiador = 1
('OSDE', '30-88888888-8', true);  -- id_financiador = 2

-- ------------------------------------------------------------
-- 2. USUARIO (credenciales de acceso)
-- ------------------------------------------------------------
INSERT INTO usuario (dni, contrasena, rol, activo) VALUES
('30111222', '$2b$10$Nvf/LrVFh0D7rpNvKSu7G.Svz4qTZ04PfW0sPU3DorP1LL4/OphQm', 'ADMINISTRADOR', true), -- id_usuario = 1 (médica)
('32111222', '$2b$10$Nvf/LrVFh0D7rpNvKSu7G.Svz4qTZ04PfW0sPU3DorP1LL4/OphQm', 'PACIENTE',       true), -- id_usuario = 2 (Juan Pérez)
('33222111', '$2b$10$Nvf/LrVFh0D7rpNvKSu7G.Svz4qTZ04PfW0sPU3DorP1LL4/OphQm', 'PACIENTE',       true); -- id_usuario = 3 (María Gómez)

-- ------------------------------------------------------------
-- 3. MEDICO (1-1 con usuario)
-- ------------------------------------------------------------
INSERT INTO medico (id_usuario, nombre_apellido, dni, matricula, activo) VALUES
(1, 'Dra. Ana López', '30111222', 'MP-12345', true); -- id_medico = 1

-- Especialidades del médico (tabla aparte, generada por @ElementCollection)
INSERT INTO medico_especialidad (id_medico, especialidad) VALUES
(1, 'Clínica Médica'),
(1, 'Pediatría');

-- ------------------------------------------------------------
-- 4. PACIENTE (1-1 con usuario)
-- ------------------------------------------------------------
INSERT INTO paciente (id_usuario, nombre_apellido, dni, fecha_nacimiento, email, celular, domicilio, activo) VALUES
(2, 'Juan Pérez',   '32111222', '1990-02-15', 'juan.perez@example.com',  '1122334455', 'Av. Siempre Viva 123', true), -- id_paciente = 1
(3, 'María Gómez',  '33222111', '1985-07-20', 'maria.gomez@example.com', '1133445566', 'Calle Falsa 456',      true); -- id_paciente = 2

-- ------------------------------------------------------------
-- 5. DISPONIBILIDAD (reglas semanales del médico)
-- ------------------------------------------------------------
INSERT INTO disponibilidad (id_medico, dia_semana, hora_inicio, hora_fin, duracion_turno_min, activo) VALUES
(1, 'LUNES',     '09:00', '17:00', 30, true), -- id_disponibilidad = 1
(1, 'MIERCOLES', '09:00', '13:00', 30, true); -- id_disponibilidad = 2

-- ------------------------------------------------------------
-- 6. BLOQUEO (excepción: feriado)
-- ------------------------------------------------------------
INSERT INTO bloqueo (id_medico, fecha_desde, fecha_hasta, motivo, activo) VALUES
(1, '2026-12-25', '2026-12-25', 'Feriado - Navidad', true); -- id_bloqueo = 1

-- ------------------------------------------------------------
-- 7. COBERTURA (afiliación paciente <-> financiador)
-- ------------------------------------------------------------
INSERT INTO cobertura (id_paciente, id_financiador, numero_afiliado, plan, activo) VALUES
(1, 2, '123456789', '210',    true), -- id_cobertura = 1 (Juan Pérez - OSDE)
(2, 1, '987654321', 'Plan 3', true); -- id_cobertura = 2 (María Gómez - IOMA)

-- ------------------------------------------------------------
-- 8. TURNO
-- Fechas elegidas para que coincidan con los días de la
-- disponibilidad de arriba (LUNES / MIERCOLES):
--   - 2026-08-24 es LUNES (turno ya pasado, para probar consulta/diagnóstico/tratamiento)
--   - 2026-08-26 es MIERCOLES (turno cancelado, de ejemplo)
--   - 2026-08-31 es LUNES (turnos futuros, para probar la agenda)
-- ------------------------------------------------------------
INSERT INTO turno (id_paciente, id_medico, id_disponibilidad, id_cobertura, fecha, hora, estado, motivo_consulta, motivo_cancelacion) VALUES
(1, 1, 1, 1,    '2026-08-24', '10:00', 'ATENDIDO',   'Control anual',      NULL),                                          -- id_turno = 1
(2, 1, 2, NULL, '2026-08-26', '09:00', 'CANCELADO',  'Consulta general',   'Paciente reprogramó por motivos laborales'),   -- id_turno = 2
(1, 1, 1, 1,    '2026-08-31', '09:00', 'CONFIRMADO', 'Consulta de rutina', NULL),                                          -- id_turno = 3
(2, 1, 1, NULL, '2026-08-31', '09:30', 'CONFIRMADO', 'Dolor de cabeza',    NULL);                                          -- id_turno = 4

-- ------------------------------------------------------------
-- 9. CONSULTA_MEDICA (1-1 con el turno ATENDIDO)
-- ------------------------------------------------------------
INSERT INTO consulta_medica (id_turno, observaciones) VALUES
(1, 'Paciente presenta buen estado general. Sin hallazgos relevantes.'); -- id_consulta = 1

-- ------------------------------------------------------------
-- 10. DIAGNOSTICO
-- ------------------------------------------------------------
INSERT INTO diagnostico (id_consulta, descripcion) VALUES
(1, 'Hipertensión arterial leve'); -- id_diagnostico = 1

-- ------------------------------------------------------------
-- 11. TRATAMIENTO
-- ------------------------------------------------------------
INSERT INTO tratamiento (id_diagnostico, descripcion, duracion, estado) VALUES
(1, 'Enalapril 10mg, 1 comprimido por día', '3 meses', 'EN_CURSO'); -- id_tratamiento = 1
