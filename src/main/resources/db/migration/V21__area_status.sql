-- =========================================================
-- V21 - Adiciona status para áreas
-- =========================================================

CREATE TYPE area_status AS ENUM ('ATIVA', 'DESATIVADA', 'RETIRADA');
ALTER TABLE areas 
ADD COLUMN status area_status DEFAULT 'ATIVA';