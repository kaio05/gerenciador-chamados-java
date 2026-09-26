package br.com.dunnastecnologia.chamados.infrastructure.repository;

import br.com.dunnastecnologia.chamados.domain.model.Area;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AreaRepository extends JpaRepository<Area, UUID> {
    @Query(value = """
    SELECT a.*
    FROM areas a
    WHERE a.status = 'ATIVA'
    """,
    countQuery = """
    SELECT COUNT(a.id)
    FROM areas a
    WHERE a.status = 'ATIVA'
    """,
    nativeQuery = true)
    Page<Area> listarAreasAtivas(PageRequest pageRequest);
}
