package br.com.dunnastecnologia.chamados.infrastructure.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.UserCase.AreaUseCase;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Area;
import br.com.dunnastecnologia.chamados.infrastructure.repository.ColaboradorRepository;
import br.com.dunnastecnologia.chamados.infrastructure.exception.ResourceNotFoundException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.MoradorRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.TipoChamadoRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.UnidadeRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.AreaRepository;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.AuthenticatedUserValidator;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.InputValidationSupport;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.PageResultMapper;
import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AreaService implements AreaUseCase {

    private final AreaRepository areaRepository;
    private final AuthenticatedUserValidator authenticatedUserValidator;

    public AreaService(
            AreaRepository areaRepository,
            ColaboradorRepository colaboradorRepository,
            MoradorRepository moradorRepository,
            UnidadeRepository unidadeRepository,
            TipoChamadoRepository tipoChamadoRepository,
            AuthenticatedUserValidator authenticatedUserValidator,
            PasswordEncoder passwordEncoder
    ) {
        this.areaRepository = areaRepository;
        this.authenticatedUserValidator = authenticatedUserValidator;
    }

   @Override
    @Transactional
    public Area cadastrarArea(AuthenticatedUser admin, String identificacao) {
        authenticatedUserValidator.assertAdministrador(admin);
        String identificacaoNormalizada = InputValidationSupport.normalizeRequiredText(
            identificacao,
            "Identificacao da area e obrigatoria",
            "Identificacao da area deve ter no maximo 255 caracteres",
            ValidationLimits.AREA_IDENTIFICACAO_MAX_LENGTH
        );

        Area area = new Area();
        area.setIdentificacao(identificacaoNormalizada);
        area.setStatus(Area.AreaStatus.ATIVA);

        Area savedArea = areaRepository.save(area);
        return areaRepository.findById(savedArea.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Area nao encontrada apos cadastro"));
    }

    @Override
    public PageResult<Area> listarAreas(PageRequest pageRequest) {
        return PageResultMapper.fromPage(areaRepository.findAll(pageRequest));
    }

    @Override
    @Transactional
    public void removerArea(AuthenticatedUser admin, UUID areaId) {
        authenticatedUserValidator.assertAdministrador(admin);
        Area area = areaRepository.getReferenceById(areaId);
        area.setStatus(Area.AreaStatus.DESATIVADA);
        areaRepository.save(area);
    }
}
