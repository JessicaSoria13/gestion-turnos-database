-- ============================================================
-- Migración manual - septiembre 2026
-- ============================================================
-- Excepción puntual a "las tablas las genera Hibernate": estos dos
-- cambios NO los puede aplicar ddl-auto=update solo. Correr este
-- script UNA VEZ, a mano, en cada base local que ya exista (una base
-- nueva, creada recién con crear-base.sql, no lo necesita: Hibernate
-- ya va a crear las tablas con las anotaciones actuales).
--
-- 1) Usuario: la unicidad pasa de ser sobre "dni" solo a ser sobre
--    (dni, rol) - un mismo dni puede tener una fila PACIENTE y otra
--    ADMINISTRADOR a la vez. ddl-auto=update no borra una unique
--    constraint vieja para reemplazarla por otra,
--    así que si no se corre esto a mano, el constraint viejo sobre
--    "dni" solo sigue ahí y bloquea la segunda fila.
-- 2) Turno: la unicidad (id_medico, fecha, hora) tiene que excluir los
--    turnos CANCELADO (un horario cancelado se vuelve a ofrecer).
--    Eso es un índice único PARCIAL (con WHERE), y Hibernate no
--    puede generar ese tipo de índice desde las
--    anotaciones de la entidad.
-- ============================================================

-- ------------------------------------------------------------
-- 1. USUARIO: unicidad (dni, rol) en vez de dni solo
-- ------------------------------------------------------------
-- El nombre del constraint viejo lo pone Hibernate (en Hibernate 6 suele
-- ser un hash tipo "ukxxxxxxxxxxxxxxxxxxxxxxxxxxx", no algo predecible
-- como "usuario_dni_key"). En vez de adivinarlo, este bloque busca en el
-- catálogo de Postgres cualquier constraint UNIQUE que sea sobre la
-- columna "dni" sola (una sola columna) y lo borra, sea cual sea su
-- nombre real.
DO $$
DECLARE
    nombre_constraint text;
BEGIN
    SELECT con.conname INTO nombre_constraint
    FROM pg_constraint con
    JOIN pg_class rel ON rel.oid = con.conrelid
    WHERE rel.relname = 'usuario'
      AND con.contype = 'u'
      AND con.conkey = ARRAY[
            (SELECT attnum FROM pg_attribute
             WHERE attrelid = rel.oid AND attname = 'dni')
        ];

    IF nombre_constraint IS NOT NULL THEN
        EXECUTE format('ALTER TABLE usuario DROP CONSTRAINT %I', nombre_constraint);
        RAISE NOTICE 'Se borró la constraint vieja: %', nombre_constraint;
    ELSE
        RAISE NOTICE 'No había constraint UNIQUE sobre dni solo (nada que borrar).';
    END IF;
END $$;

-- Si Hibernate ya la creó sola al arrancar con la entidad nueva, no hace
-- falta agregarla de nuevo (y agregarla de nuevo tiraría error).
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_usuario_dni_rol'
    ) THEN
        ALTER TABLE usuario ADD CONSTRAINT uq_usuario_dni_rol UNIQUE (dni, rol);
    END IF;
END $$;

-- ------------------------------------------------------------
-- 2. TURNO: unicidad (id_medico, fecha, hora) solo entre turnos
--    que NO estén CANCELADO
-- ------------------------------------------------------------
ALTER TABLE turno DROP CONSTRAINT IF EXISTS uq_turno_medico_horario;

CREATE UNIQUE INDEX IF NOT EXISTS uq_turno_medico_horario_activo
    ON turno (id_medico, fecha, hora)
    WHERE estado <> 'CANCELADO';
