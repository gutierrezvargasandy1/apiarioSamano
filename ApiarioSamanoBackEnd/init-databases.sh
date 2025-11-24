#!/bin/sh

# ==================== CONFIGURACIÓN GENERAL ====================
export PGPASSWORD="apiario123"

# ==================== FUNCIONES AUXILIARES ====================

crear_base_y_tablas() {
  DB_NAME=$1
  SQL_SCRIPT=$2

  echo "Creando base de datos: $DB_NAME (si no existe)..."
  psql -v ON_ERROR_STOP=1 -U apiario -d postgres <<-EOSQL
    DO \$\$
    BEGIN
      IF NOT EXISTS (SELECT FROM pg_database WHERE datname = '$DB_NAME') THEN
        CREATE DATABASE "$DB_NAME";
        RAISE NOTICE 'Base de datos $DB_NAME creada.';
      ELSE
        RAISE NOTICE 'Base de datos $DB_NAME ya existe.';
      END IF;
    END
    \$\$;
EOSQL

  echo "Ejecutando script para $DB_NAME..."
  psql -v ON_ERROR_STOP=1 -U apiario -d "$DB_NAME" <<EOSQL
$SQL_SCRIPT
EOSQL
  echo "✅ $DB_NAME configurada correctamente."
}

# ==================== BASE DE DATOS: UsuariosDB ====================

SQL_USUARIOS="
CREATE TABLE IF NOT EXISTS Usuarios (
    nombre VARCHAR(100) NOT NULL,
    apellido_ma VARCHAR(100) NOT NULL,
    otp_expiracion TIMESTAMP,
    rol VARCHAR(50) NOT NULL CHECK (rol IN ('OPERADOR', 'ADMINISTRADOR', 'CLIENTE')),
    id SERIAL PRIMARY KEY,
    estado BOOLEAN DEFAULT TRUE,
    apellido_pa VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    otp VARCHAR(6)
);

INSERT INTO Usuarios (nombre, apellido_ma, otp_expiracion, rol, estado, apellido_pa, email, contrasena, otp)
VALUES 
('Juan', 'García', NULL, 'ADMINISTRADOR', TRUE, 'Pérez', 'androoz706@gmail.com', '\$2a\$10\$3McdT1jxOvlhPwlagpGl0ud3kaqb52B.wspUNPM/K0QLg63VolV4W', NULL),
('María', 'Martínez', NULL, 'CLIENTE', TRUE, 'López', 'maria@email.com', 'contraseña456', NULL),
('Carlos', 'Sánchez', NULL, 'OPERADOR', TRUE, 'Ramírez', 'carlos@email.com', 'clave789', NULL),
('Ana', 'Hernández', NULL, 'ADMINISTRADOR', TRUE, 'Torres', 'ana@email.com', 'pass321', NULL),
('Luis', 'Rojas', NULL, 'CLIENTE', TRUE, 'Gómez', 'luis@email.com', 'seguro654', NULL);
"

crear_base_y_tablas "UsuariosDB" "$SQL_USUARIOS"

# ==================== BASE DE DATOS: ApiariosDB ====================

SQL_APIARIOS="
CREATE TABLE IF NOT EXISTS HistorialMedico (
    id SERIAL PRIMARY KEY,
    medicamentos VARCHAR(255),
    detalles VARCHAR(500),
    notas VARCHAR(500)
);

INSERT INTO HistorialMedico (medicamentos, detalles, notas)
VALUES 
('Antibiótico Apistán, Vitaminas B12', 'Control de varroa aplicado', 'Colmena en buen estado de salud'),
('Azúcar glas, Suplemento proteico', 'Alimentación complementaria', 'Revisar en 15 días'),
('Fumagilina, Timol', 'Prevención de nosemosis', 'Monitorear temperatura'),
('Amitraz, Jarabe de maíz', 'Tratamiento contra ácaros', 'Excelente respuesta al tratamiento'),
('Extracto de menta, Polen', 'Fortalecimiento post-cosecha', 'Colmena recuperándose bien');

CREATE TABLE IF NOT EXISTS Apiarios (
    id SERIAL PRIMARY KEY,
    numero_apiario INT NOT NULL,
    ubicacion VARCHAR(200) NOT NULL,
    salud VARCHAR(100),
    medicion_actual DECIMAL(10,2),
    id_historial_medico INT REFERENCES HistorialMedico(id)
);

INSERT INTO Apiarios (numero_apiario, ubicacion, salud, medicion_actual, id_historial_medico)
VALUES 
(1, 'Campo Norte - Coordenadas 19.4326, -99.1332', 'Excelente', 85.50, 1),
(2, 'Valle Florido - Coordenadas 19.4350, -99.1350', 'Buena', 78.25, 2),
(3, 'Loma Alta - Coordenadas 19.4300, -99.1300', 'Regular', 65.80, 3),
(4, 'Pradera Verde - Coordenadas 19.4380, -99.1380', 'Excelente', 92.10, 4),
(5, 'Cerro Dulce - Coordenadas 19.4330, -99.1320', 'Buena', 81.75, 5);
"

crear_base_y_tablas "ApiariosDB" "$SQL_APIARIOS"

# ==================== BASE DE DATOS: ProduccionDB ====================

SQL_PRODUCCION="
CREATE TABLE IF NOT EXISTS Lotes (
    id SERIAL PRIMARY KEY,
    numero_seguimiento VARCHAR(50) NOT NULL,
    tipo_producto VARCHAR(100) NOT NULL,
    fecha_creacion DATE NOT NULL,
    ubicacion VARCHAR(200)
);

INSERT INTO Lotes (numero_seguimiento, tipo_producto, fecha_creacion, ubicacion)
VALUES 
('LT-2024-001', 'Miel de Flor de Naranjo', '2024-01-15', 'Almacén Principal - Estante A1'),
('LT-2024-002', 'Miel Multifloral', '2024-01-20', 'Almacén Principal - Estante B2'),
('LT-2024-003', 'Propóleo Puro', '2024-02-01', 'Almacén Secundario - Estante C3'),
('LT-2024-004', 'Jalea Real Fresca', '2024-02-10', 'Cámara Fría - Sección 1'),
('LT-2024-005', 'Cera de Abejas', '2024-02-15', 'Almacén Principal - Estante D4');

CREATE TABLE IF NOT EXISTS Cosechas (
    id SERIAL PRIMARY KEY,
    fecha_cosecha DATE NOT NULL,
    id_lote INT REFERENCES Lotes(id),
    calidad VARCHAR(100),
    cantidad DECIMAL(10,2),
    id_apiario INT
);

INSERT INTO Cosechas (fecha_cosecha, id_lote, calidad, cantidad, id_apiario)
VALUES 
('2024-01-10', 1, 'Premium', 150.50, 1),
('2024-01-18', 2, 'Estándar', 200.75, 2),
('2024-01-25', 3, 'Extra', 45.25, 3),
('2024-02-05', 4, 'Premium', 12.80, 4),
('2024-02-12', 5, 'Estándar', 85.60, 5),
('2024-02-20', 1, 'Extra', 180.25, 1),
('2024-02-25', 2, 'Premium', 220.40, 2);
"

crear_base_y_tablas "ProduccionDB" "$SQL_PRODUCCION"

# ==================== BASE DE DATOS: AlmacenDB ====================

SQL_ALMACEN="
CREATE TABLE IF NOT EXISTS Almacen (
    id SERIAL PRIMARY KEY,
    numero_seguimiento VARCHAR(50) NOT NULL,
    ubicacion VARCHAR(200),
    espacios_ocupados INT,
    capacidad INT
);

INSERT INTO Almacen (numero_seguimiento, ubicacion, espacios_ocupados, capacidad)
VALUES 
('ALM-001', 'Edificio Principal - Nivel 1', 45, 100),
('ALM-002', 'Edificio Principal - Nivel 2', 30, 80),
('ALM-003', 'Bodega Exterior - Zona A', 25, 50),
('ALM-004', 'Bodega Refrigerada', 15, 30),
('ALM-005', 'Almacén de Herramientas', 20, 40);

CREATE TABLE IF NOT EXISTS Herramientas (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    foto BYTEA,
    id_almacen INT REFERENCES Almacen(id)
);

INSERT INTO Herramientas (nombre, id_almacen)
VALUES 
('Ahumador de Fuelle', 5),
('Pinza Apícola', 5),
('Traje de Protección Completo', 5),
('Cuchillo Desoperculador', 5),
('Extractor de Miel Manual', 5),
('Cepillo para Abejas', 5),
('Guantes de Cuero', 5),
('Botas de Goma', 5);

CREATE TABLE IF NOT EXISTS MateriasPrimas (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    foto BYTEA,
    cantidad DECIMAL(10,2),
    id_almacen INT REFERENCES Almacen(id),
    id_proveedor INT
);

INSERT INTO MateriasPrimas (nombre, cantidad, id_almacen, id_proveedor)
VALUES 
('Frascos de Vidrio 500ml', 1000.00, 1, 1),
('Etiquetas Miel Premium', 5000.00, 1, 2),
('Cera Estampada', 250.50, 2, 3),
('Azúcar Refinado', 1500.75, 3, 1),
('Jarabe de Maíz', 800.25, 3, 4),
('Botes de Propóleo', 200.00, 4, 5),
('Cajas de Embalaje', 300.00, 1, 2),
('Tapas Herméticas', 1200.00, 1, 1);
"

crear_base_y_tablas "AlmacenDB" "$SQL_ALMACEN"

# ==================== BASE DE DATOS: ProveedoresDB ====================

SQL_PROVEEDORES="
CREATE TABLE IF NOT EXISTS Proveedores (
    id SERIAL PRIMARY KEY,
    fotografia BYTEA,
    nombre_empresa VARCHAR(200) NOT NULL,
    num_telefono VARCHAR(20),
    material_provee VARCHAR(200)
);

INSERT INTO Proveedores (nombre_empresa, num_telefono, material_provee)
VALUES 
('Envases y Empaques S.A.', '+52-55-1234-5678', 'Frascos, tapas y material de embalaje'),
('Etiquetas Creativas México', '+52-55-2345-6789', 'Etiquetas personalizadas y diseño'),
('Cera Natural del Valle', '+52-55-3456-7890', 'Cera estampada y panales'),
('Dulces y Jarabes Industriales', '+52-55-4567-8901', 'Azúcar, jarabes y suplementos'),
('Productos Apícolas Premium', '+52-55-5678-9012', 'Propóleo, jalea real y derivados'),
('Equipos Apícolas Profesionales', '+52-55-6789-0123', 'Herramientas y equipo de protección'),
('Laboratorio Veterinario Especializado', '+52-55-7890-1234', 'Medicamentos y tratamientos apícolas');
"

crear_base_y_tablas "ProveedoresDB" "$SQL_PROVEEDORES"

echo "🎉 Todas las bases de datos y tablas han sido creadas correctamente."