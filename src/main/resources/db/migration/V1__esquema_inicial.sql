CREATE TABLE usuario (
    id_usuario    INT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol           VARCHAR(20)  NOT NULL
);

CREATE TABLE zona (
    id_zona INT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE producto (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(150)   NOT NULL,
    precio      DECIMAL(20, 2) NOT NULL,
    activo      BOOLEAN        NOT NULL
);

CREATE TABLE reparto (
    id_reparto    INT AUTO_INCREMENT PRIMARY KEY,
    fk_repartidor INT         NOT NULL,
    fk_zona       INT         NOT NULL,
    fecha         DATE        NOT NULL,
    estado        VARCHAR(20) NOT NULL,
    CONSTRAINT fk_reparto_repartidor FOREIGN KEY (fk_repartidor) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_reparto_zona FOREIGN KEY (fk_zona) REFERENCES zona (id_zona)
);

CREATE TABLE pedido (
    id_pedido      INT AUTO_INCREMENT PRIMARY KEY,
    fk_cliente     INT         NOT NULL,
    fk_zona        INT         NOT NULL,
    fk_reparto     INT         NULL,
    estado         VARCHAR(20) NOT NULL,
    fecha_creacion TIMESTAMP   NOT NULL,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (fk_cliente) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_pedido_zona FOREIGN KEY (fk_zona) REFERENCES zona (id_zona),
    CONSTRAINT fk_pedido_reparto FOREIGN KEY (fk_reparto) REFERENCES reparto (id_reparto)
);

CREATE TABLE pedido_detalle (
    id_pedido_detalle    INT AUTO_INCREMENT PRIMARY KEY,
    fk_pedido            INT            NOT NULL,
    fk_producto          INT            NOT NULL,
    cantidad             INT            NOT NULL,
    precio_unitario      DECIMAL(20, 2) NOT NULL,
    cantidad_entregada   INT            NULL,
    agregado_de_sobrante BOOLEAN        NOT NULL,
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (fk_pedido) REFERENCES pedido (id_pedido),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (fk_producto) REFERENCES producto (id_producto)
);

CREATE TABLE sobrante (
    id_sobrante INT AUTO_INCREMENT PRIMARY KEY,
    fk_reparto  INT NOT NULL,
    fk_producto INT NOT NULL,
    cantidad    INT NOT NULL,
    CONSTRAINT fk_sobrante_reparto FOREIGN KEY (fk_reparto) REFERENCES reparto (id_reparto),
    CONSTRAINT fk_sobrante_producto FOREIGN KEY (fk_producto) REFERENCES producto (id_producto),
    CONSTRAINT uq_sobrante_reparto_producto UNIQUE (fk_reparto, fk_producto)
);
