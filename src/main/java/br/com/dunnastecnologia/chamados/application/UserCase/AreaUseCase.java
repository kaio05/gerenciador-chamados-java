package br.com.dunnastecnologia.chamados.application.UserCase;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Area;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

public interface AreaUseCase {

    Area cadastrarArea(
            AuthenticatedUser admin,
            String identificacao
    );


    public Area atualizarArea(AuthenticatedUser admin, UUID areaId, String identificacao);

    PageResult<Area> listarAreas(PageRequest pageRequest);

//     Area buscarAreaPorId(UUID areaId);

    void removerArea(
            AuthenticatedUser admin,
            UUID areaId
    );
}
