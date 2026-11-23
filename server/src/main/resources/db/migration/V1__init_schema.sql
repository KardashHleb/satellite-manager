-- Схема под JPA JOINED-наследование спутников и отдельные таблицы энергосистемы и состояния (3НФ).

CREATE TABLE satellite_constellation (
    id BIGSERIAL PRIMARY KEY,
    constellation_name VARCHAR(255) NOT NULL CONSTRAINT uq_constellation_name UNIQUE
);

CREATE TABLE satellite (
    id BIGSERIAL PRIMARY KEY,
    dtype VARCHAR(32) NOT NULL,
    constellation_id BIGINT NOT NULL REFERENCES satellite_constellation (id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uq_satellite_per_constellation UNIQUE (constellation_id, name)
);

-- Индекс для частых выборок спутников по группировке (имена группировок меняются реже, чем читаются спутники).
CREATE INDEX idx_satellite_constellation_id ON satellite (constellation_id);

CREATE TABLE communication_satellite (
    id BIGINT PRIMARY KEY REFERENCES satellite (id) ON DELETE CASCADE,
    bandwidth DOUBLE PRECISION NOT NULL,
    data_sent DOUBLE PRECISION NOT NULL DEFAULT 0
);

CREATE TABLE imaging_satellite (
    id BIGINT PRIMARY KEY REFERENCES satellite (id) ON DELETE CASCADE,
    resolution DOUBLE PRECISION NOT NULL,
    photos_taken INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE energy_system (
    id BIGSERIAL PRIMARY KEY,
    satellite_id BIGINT NOT NULL UNIQUE REFERENCES satellite (id) ON DELETE CASCADE,
    battery_level DOUBLE PRECISION NOT NULL
);

CREATE INDEX idx_energy_system_satellite_id ON energy_system (satellite_id);

CREATE TABLE satellite_state (
    id BIGSERIAL PRIMARY KEY,
    satellite_id BIGINT NOT NULL UNIQUE REFERENCES satellite (id) ON DELETE CASCADE,
    is_active BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_satellite_state_satellite_id ON satellite_state (satellite_id);
