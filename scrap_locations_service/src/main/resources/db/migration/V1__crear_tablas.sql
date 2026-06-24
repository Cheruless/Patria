CREATE TABLE comuna
(
    id          INT NOT NULL,
    nombre      VARCHAR(255) NULL,
    distrito_id INT NULL,
    CONSTRAINT pk_comuna PRIMARY KEY (id)
);

CREATE TABLE distrito
(
    id        INT NOT NULL,
    region_id INT NULL,
    CONSTRAINT pk_distrito PRIMARY KEY (id)
);

CREATE TABLE region
(
    id     INT NOT NULL,
    nombre VARCHAR(255) NULL,
    numero VARCHAR(255) NULL,
    CONSTRAINT pk_region PRIMARY KEY (id)
);

CREATE TABLE revchanges
(
    rev        BIGINT NOT NULL,
    entityname VARCHAR(255) NULL
);

CREATE TABLE revinfo
(
    rev      BIGINT NOT NULL,
    revtstmp BIGINT NULL,
    CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

ALTER TABLE comuna
    ADD CONSTRAINT FK_COMUNA_ON_DISTRITO FOREIGN KEY (distrito_id) REFERENCES distrito (id);

ALTER TABLE distrito
    ADD CONSTRAINT FK_DISTRITO_ON_REGION FOREIGN KEY (region_id) REFERENCES region (id);

ALTER TABLE revchanges
    ADD CONSTRAINT fk_revchanges_on_default_tracking_modified_entities_changelog FOREIGN KEY (rev) REFERENCES revinfo (rev);