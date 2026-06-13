CREATE TABLE diputado
(
    diputado_id     INT NOT NULL,
    nombre_completo VARCHAR(255) NULL,
    alias_partido   VARCHAR(255) NULL,
    distrito_num    INT NULL,
    CONSTRAINT pk_diputado PRIMARY KEY (diputado_id)
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

ALTER TABLE revchanges
    ADD CONSTRAINT fk_revchanges_on_default_tracking_modified_entities_changelog FOREIGN KEY (rev) REFERENCES revinfo (rev);