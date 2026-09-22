package br.com.dunnastecnologia.chamados.infrastructure.repository;

import br.com.dunnastecnologia.chamados.domain.model.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AreaRepository extends JpaRepository<Area, UUID> {

    /**
     * Aciona a funcao responsavel por gerar as unidades de um bloco com validacao administrativa no banco.
     */
    @Query(value = """
            select fn_gerar_unidades_bloco(:adminId, :blocoId)
            """,
            nativeQuery = true)
    Integer gerarUnidadesDoArea(
            @Param("adminId") UUID adminId,
            @Param("blocoId") UUID blocoId
    );
}
