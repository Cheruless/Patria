CREATE TABLE profile
(
    id              INT NOT NULL,
    nombre_completo VARCHAR(255) NULL,
    partido         VARCHAR(255) NULL,
    distrito_id     INT NULL,
    region          VARCHAR(255) NULL,
    CONSTRAINT pk_profile PRIMARY KEY (id)
);

CREATE TABLE profile_comunas
(
    profile_id INT NOT NULL,
    comuna     VARCHAR(255) NULL,
    CONSTRAINT fk_profile_comunas FOREIGN KEY (profile_id) REFERENCES profile (id)
);