-- =========================================================
-- V19 - Funcionalidade Reserva de áreas comuns
-- =========================================================

-- Estrutura fisica do condominio
CREATE TABLE areas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identificacao VARCHAR(255)
);

CREATE TYPE reserva_status AS ENUM ('SOLICITADA', 'APROVADA', 'REJEITADA', 'CANCELADA');

CREATE TABLE reservas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    area_id UUID NOT NULL,
    morador_id UUID NOT NULL,
    data TIMESTAMP(6) WITHOUT TIME ZONE,
    horario_inicio TIME NOT NULL,
    horario_fim TIME NOT NULL,
    status reserva_status DEFAULT 'SOLICITADA',
    CONSTRAINT fk_reservas_area
        FOREIGN KEY (area_id) REFERENCES areas (id),
    CONSTRAINT fk_morador_reserva
        FOREIGN KEY (morador_id) REFERENCES moradores (id)
);