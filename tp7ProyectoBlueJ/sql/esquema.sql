-- Esquema de la base biblioteca.db (SQLite).
-- La verdad operativa esta en ConexionBD.DDL (se ejecuta al abrir la base);
-- este archivo es la referencia documental para renders y revision.
-- Tipos de fecha: epoch millis a medianoche (dia calendario, mismo criterio del dominio).


CREATE TABLE IF NOT EXISTS biblioteca (
    nombre TEXT PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS libro (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo    TEXT    NOT NULL,
    edicion   INTEGER NOT NULL,
    editorial TEXT    NOT NULL,
    anio      INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS socio (
    dni            INTEGER PRIMARY KEY,
    nombre         TEXT    NOT NULL,
    tipo           TEXT    NOT NULL CHECK (tipo IN ('Docente', 'Estudiante')),
    dias_prestamos INTEGER NOT NULL,
    area           TEXT,  -- solo Docente
    carrera        TEXT   -- solo Estudiante
);

CREATE TABLE IF NOT EXISTS prestamo (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    socio_dni        INTEGER NOT NULL REFERENCES socio (dni),
    libro_id         INTEGER NOT NULL REFERENCES libro (id),
    fecha_retiro     INTEGER NOT NULL,
    fecha_devolucion INTEGER  -- NULL = prestamo activo (sin devolver)
);

-- Regla de negocio a nivel base: un mismo ejemplar no puede quedar con
-- mas de un prestamo activo a la vez.
CREATE UNIQUE INDEX IF NOT EXISTS idx_libro_un_prestamo_activo
    ON prestamo (libro_id)
    WHERE fecha_devolucion IS NULL;
