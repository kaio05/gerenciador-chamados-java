package br.com.dunnastecnologia.chamados.unit.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Area;
import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.ResourceNotFoundException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.UnauthorizedOperationException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.AreaRepository;
import br.com.dunnastecnologia.chamados.infrastructure.service.AreaService;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.AuthenticatedUserValidator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AreaService Unit Tests")
class AreaServiceTest {

    @Mock
    private AreaRepository areaRepository;

    @Mock
    private AuthenticatedUserValidator authenticatedUserValidator;

    @Mock
    private AuthenticatedUser admin;

    @InjectMocks
    private AreaService areaService;

    @Captor
    private ArgumentCaptor<Area> areaCaptor;

    // --- Test Data Builders / Helpers ---

    private Area buildArea(UUID id, String identificacao, Area.AreaStatus status) {
        Area area = new Area();
        area.setId(id);
        area.setIdentificacao(identificacao);
        area.setStatus(status);
        return area;
    }

    private Area buildActiveArea(UUID id, String identificacao) {
        return buildArea(id, identificacao, Area.AreaStatus.ATIVA);
    }

    // --- Tests ---

    @Nested
    @DisplayName("cadastrarArea")
    class CadastrarArea {

        @Test
        @DisplayName("Deve cadastrar area com sucesso e normalizar a identificacao")
        void deveCadastrarAreaComIdentificacaoNormalizada() {
            // Arrange
            String identificacaoComEspacos = "  Área Administrativa  ";
            String identificacaoNormalizada = "Área Administrativa";
            UUID areaId = UUID.randomUUID();
            Area savedArea = buildActiveArea(areaId, identificacaoNormalizada);

            when(areaRepository.save(any(Area.class))).thenReturn(savedArea);
            when(areaRepository.findById(areaId)).thenReturn(Optional.of(savedArea));

            // Act
            Area result = areaService.cadastrarArea(admin, identificacaoComEspacos);

            // Assert
            assertThat(result)
                    .isNotNull()
                    .extracting(Area::getId, Area::getIdentificacao, Area::getStatus)
                    .containsExactly(areaId, identificacaoNormalizada, Area.AreaStatus.ATIVA);

            verify(authenticatedUserValidator).assertAdministrador(admin);
            
            // Capture state passed to save() to verify normalization logic directly
            verify(areaRepository).save(areaCaptor.capture());
            Area capturedArea = areaCaptor.getValue();
            assertThat(capturedArea.getIdentificacao()).isEqualTo(identificacaoNormalizada);
            assertThat(capturedArea.getStatus()).isEqualTo(Area.AreaStatus.ATIVA);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Deve lancar excecao quando a identificacao for nula, vazia ou apenas espacos")
        void deveLancarExcecaoQuandoIdentificacaoForInvalida(String identificacaoInvalida) {
            // Act & Assert
            assertThatThrownBy(() -> areaService.cadastrarArea(admin, identificacaoInvalida))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Identificacao da area e obrigatoria");

            verify(authenticatedUserValidator).assertAdministrador(admin);
            verifyNoInteractions(areaRepository);
        }

        @Test
        @DisplayName("Deve permitir cadastrar area com o limite maximo exato de caracteres")
        void deveCadastrarAreaComLimiteMaximoDeCaracteres() {
            // Arrange
            String identificacaoLimite = "a".repeat(ValidationLimits.AREA_IDENTIFICACAO_MAX_LENGTH);
            UUID areaId = UUID.randomUUID();
            Area savedArea = buildActiveArea(areaId, identificacaoLimite);

            when(areaRepository.save(any(Area.class))).thenReturn(savedArea);
            when(areaRepository.findById(areaId)).thenReturn(Optional.of(savedArea));

            // Act
            Area result = areaService.cadastrarArea(admin, identificacaoLimite);

            // Assert
            assertThat(result.getIdentificacao()).hasSize(ValidationLimits.AREA_IDENTIFICACAO_MAX_LENGTH);
        }

        @Test
        @DisplayName("Deve lancar excecao quando identificacao exceder o limite maximo")
        void deveLancarExcecaoQuandoIdentificacaoExcederLimite() {
            // Arrange
            String identificacaoExcedente = "a".repeat(ValidationLimits.AREA_IDENTIFICACAO_MAX_LENGTH + 1);

            // Act & Assert
            assertThatThrownBy(() -> areaService.cadastrarArea(admin, identificacaoExcedente))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Identificacao da area deve ter no maximo 255 caracteres");

            verify(authenticatedUserValidator).assertAdministrador(admin);
            verifyNoInteractions(areaRepository);
        }

        @Test
        @DisplayName("Deve lancar ResourceNotFoundException quando area nao for encontrada apos o cadastro")
        void deveLancarResourceNotFoundQuandoAreaNaoForEncontradaAposCadastro() {
            // Arrange
            UUID areaId = UUID.randomUUID();
            Area savedArea = buildActiveArea(areaId, "Financeiro");

            when(areaRepository.save(any(Area.class))).thenReturn(savedArea);
            when(areaRepository.findById(areaId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> areaService.cadastrarArea(admin, "Financeiro"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Area nao encontrada apos cadastro");
        }

        @Test
        @DisplayName("Nao deve cadastrar area quando o usuario nao for administrador")
        void naoDeveCadastrarAreaQuandoUsuarioNaoForAdministrador() {
            // Arrange
            doThrow(new UnauthorizedOperationException("Usuario nao e administrador"))
                    .when(authenticatedUserValidator).assertAdministrador(admin);

            // Act & Assert
            assertThatThrownBy(() -> areaService.cadastrarArea(admin, "Financeiro"))
                    .isInstanceOf(UnauthorizedOperationException.class)
                    .hasMessage("Usuario nao e administrador");

            verifyNoInteractions(areaRepository);
        }
    }

    @Nested
    @DisplayName("listarAreas")
    class ListarAreas {

        @Test
        @DisplayName("Deve listar areas ativas com paginacao e validar conteudo retornado")
        void deveListarAreasAtivas() {
            // Arrange
            PageRequest pageRequest = PageRequest.of(0, 10);
            Area area1 = buildActiveArea(UUID.randomUUID(), "Financeiro");
            Area area2 = buildActiveArea(UUID.randomUUID(), "Administrativo");

            PageImpl<Area> page = new PageImpl<>(List.of(area1, area2), pageRequest, 2);
            when(areaRepository.listarAreasAtivas(pageRequest)).thenReturn(page);

            // Act
            PageResult<Area> result = areaService.listarAreas(pageRequest);

            // Assert - Explicitly verifying list contents and properties
            assertThat(result).isNotNull();
            assertThat(result.content())
                    .hasSize(2)
                    .extracting(Area::getIdentificacao)
                    .containsExactly("Financeiro", "Administrativo");

            verify(areaRepository).listarAreasAtivas(pageRequest);
        }
    }

    @Nested
    @DisplayName("atualizarArea")
    class AtualizarArea {

        @Test
        @DisplayName("Deve atualizar identificacao da area e aplicar normalizacao")
        void deveAtualizarIdentificacaoDaArea() {
            // Arrange
            UUID areaId = UUID.randomUUID();
            Area areaExistente = buildActiveArea(areaId, "Financeiro");

            when(areaRepository.getReferenceById(areaId)).thenReturn(areaExistente);
            when(areaRepository.save(areaExistente)).thenReturn(areaExistente);

            // Act
            Area result = areaService.atualizarArea(admin, areaId, "  Administrativo  ");

            // Assert
            assertThat(result.getIdentificacao()).isEqualTo("Administrativo");

            verify(authenticatedUserValidator).assertAdministrador(admin);
            verify(areaRepository).save(areaExistente);
        }

        @Test
        @DisplayName("Nao deve atualizar area quando usuario nao for administrador")
        void naoDeveAtualizarAreaQuandoUsuarioNaoForAdministrador() {
            // Arrange
            UUID areaId = UUID.randomUUID();
            doThrow(new UnauthorizedOperationException("Usuario nao e administrador"))
                    .when(authenticatedUserValidator).assertAdministrador(admin);

            // Act & Assert
            assertThatThrownBy(() -> areaService.atualizarArea(admin, areaId, "Administrativo"))
                    .isInstanceOf(UnauthorizedOperationException.class)
                    .hasMessage("Usuario nao e administrador");

            verify(areaRepository, never()).getReferenceById(any());
            verify(areaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("removerArea")
    class RemoverArea {

        @Test
        @DisplayName("Deve desativar area com sucesso")
        void deveDesativarArea() {
            // Arrange
            UUID areaId = UUID.randomUUID();
            Area area = buildActiveArea(areaId, "Financeiro");

            when(areaRepository.getReferenceById(areaId)).thenReturn(area);
            when(areaRepository.save(area)).thenReturn(area);

            // Act
            areaService.removerArea(admin, areaId);

            // Assert
            assertThat(area.getStatus()).isEqualTo(Area.AreaStatus.DESATIVADA);

            verify(authenticatedUserValidator).assertAdministrador(admin);
            verify(areaRepository).save(area);
        }

        @Test
        @DisplayName("Nao deve desativar area quando usuario nao for administrador")
        void naoDeveDesativarAreaQuandoUsuarioNaoForAdministrador() {
            // Arrange
            UUID areaId = UUID.randomUUID();
            doThrow(new UnauthorizedOperationException("Usuario nao e administrador"))
                    .when(authenticatedUserValidator).assertAdministrador(admin);

            // Act & Assert
            assertThatThrownBy(() -> areaService.removerArea(admin, areaId))
                    .isInstanceOf(UnauthorizedOperationException.class)
                    .hasMessage("Usuario nao e administrador");

            verify(areaRepository, never()).getReferenceById(any());
            verify(areaRepository, never()).save(any());
        }
    }
}