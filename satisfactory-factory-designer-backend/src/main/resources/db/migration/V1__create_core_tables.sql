CREATE TABLE materials (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    game_key VARCHAR(120) NOT NULL,
    name VARCHAR(160) NOT NULL,
    material_type VARCHAR(20) NOT NULL,
    stack_size INT NULL,
    sinkable BOOLEAN NOT NULL DEFAULT TRUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    description CLOB NULL,
    CONSTRAINT uk_material_game_key UNIQUE (game_key)
);

CREATE TABLE machines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    game_key VARCHAR(120) NOT NULL,
    name VARCHAR(160) NOT NULL,
    machine_type VARCHAR(40) NOT NULL,
    power_mw DOUBLE NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_machine_game_key UNIQUE (game_key)
);

CREATE TABLE recipes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    game_key VARCHAR(160) NOT NULL,
    name VARCHAR(180) NOT NULL,
    machine_id BIGINT NOT NULL,
    cycle_time_seconds DOUBLE NOT NULL,
    is_alternate BOOLEAN NOT NULL DEFAULT FALSE,
    source VARCHAR(60) NOT NULL DEFAULT 'OFFICIAL',
    game_version VARCHAR(60) NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_recipe_game_key UNIQUE (game_key),
    CONSTRAINT fk_recipes_machine FOREIGN KEY (machine_id) REFERENCES machines(id)
);

CREATE TABLE recipe_inputs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipe_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    amount_per_cycle DOUBLE NOT NULL,
    CONSTRAINT uk_recipe_input_material UNIQUE (recipe_id, material_id),
    CONSTRAINT fk_recipe_inputs_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    CONSTRAINT fk_recipe_inputs_material FOREIGN KEY (material_id) REFERENCES materials(id)
);

CREATE TABLE recipe_outputs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipe_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    amount_per_cycle DOUBLE NOT NULL,
    CONSTRAINT uk_recipe_output_material UNIQUE (recipe_id, material_id),
    CONSTRAINT fk_recipe_outputs_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    CONSTRAINT fk_recipe_outputs_material FOREIGN KEY (material_id) REFERENCES materials(id)
);

CREATE TABLE transport_levels (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transport_type VARCHAR(20) NOT NULL,
    level INT NOT NULL,
    name VARCHAR(80) NOT NULL,
    capacity_per_min DOUBLE NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_transport_type_level UNIQUE (transport_type, level)
);

CREATE TABLE factories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(160) NOT NULL,
    factory_type VARCHAR(20) NOT NULL DEFAULT 'SUB',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    max_belt_level INT NOT NULL DEFAULT 3,
    max_pipe_level INT NOT NULL DEFAULT 1,
    description CLOB NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE production_buckets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    factory_id BIGINT NOT NULL,
    name VARCHAR(160) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    description CLOB NULL,
    position_x DOUBLE NULL,
    position_y DOUBLE NULL,
    collapsed BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_buckets_factory FOREIGN KEY (factory_id) REFERENCES factories(id) ON DELETE CASCADE
);

CREATE TABLE production_nodes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bucket_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    machine_count DOUBLE NOT NULL DEFAULT 1,
    clock_percent DOUBLE NOT NULL DEFAULT 100,
    output_multiplier DOUBLE NOT NULL DEFAULT 1,
    name VARCHAR(160) NULL,
    position_x DOUBLE NULL,
    position_y DOUBLE NULL,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_nodes_bucket FOREIGN KEY (bucket_id) REFERENCES production_buckets(id) ON DELETE CASCADE,
    CONSTRAINT fk_nodes_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id)
);

CREATE TABLE bus_lines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    factory_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    name VARCHAR(160) NOT NULL,
    description CLOB NULL,
    offset_amount DOUBLE NOT NULL DEFAULT 0,
    visible_to_other_factories BOOLEAN NOT NULL DEFAULT FALSE,
    external_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    collapsed BOOLEAN NOT NULL DEFAULT FALSE,
    created_manually BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_bus_line_factory_material UNIQUE (factory_id, material_id),
    CONSTRAINT fk_bus_lines_factory FOREIGN KEY (factory_id) REFERENCES factories(id) ON DELETE CASCADE,
    CONSTRAINT fk_bus_lines_material FOREIGN KEY (material_id) REFERENCES materials(id)
);

CREATE TABLE external_connections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_bus_line_id BIGINT NOT NULL,
    target_bus_line_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_external_source_bus_line UNIQUE (source_bus_line_id),
    CONSTRAINT fk_external_source FOREIGN KEY (source_bus_line_id) REFERENCES bus_lines(id) ON DELETE CASCADE,
    CONSTRAINT fk_external_target FOREIGN KEY (target_bus_line_id) REFERENCES bus_lines(id) ON DELETE CASCADE
);

CREATE INDEX idx_external_target ON external_connections(target_bus_line_id);
CREATE INDEX idx_bus_lines_factory ON bus_lines(factory_id);
CREATE INDEX idx_nodes_bucket ON production_nodes(bucket_id);

CREATE TABLE factory_snapshots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    factory_id BIGINT NOT NULL,
    name VARCHAR(160) NOT NULL,
    snapshot_json CLOB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_snapshots_factory FOREIGN KEY (factory_id) REFERENCES factories(id) ON DELETE CASCADE
);
