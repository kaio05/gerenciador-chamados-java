package br.com.dunnastecnologia.chamados.unit.service;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Area;
import br.com.dunnastecnologia.chamados.domain.model.Morador;
import br.com.dunnastecnologia.chamados.domain.model.Reserva;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.AreaRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.MoradorRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.ReservaRepository;
import br.com.dunnastecnologia.chamados.infrastructure.service.ReservaService;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.AuthenticatedUserValidator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservaService Unit Tests")
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private AreaRepository areaRepository;

    @Mock
    private MoradorRepository moradorRepository;

    @Mock
    private AuthenticatedUserValidator authenticatedUserValidator;

    @InjectMocks
    private ReservaService service;

    @Captor
    private ArgumentCaptor<Reserva> reservaCaptor;

    // --- Common Fixture Values ---
    private UUID moradorId;
    private UUID areaId;
    private UUID reservaId;
    private LocalDate futureDate;
    private LocalTime start;
    private LocalTime end;
    private AuthenticatedUser morador;
    private AuthenticatedUser admin;

    @BeforeEach
    void setUp() {
        moradorId = UUID.randomUUID();
        areaId = UUID.randomUUID();
        reservaId = UUID.randomUUID();

        futureDate = LocalDate.now().plusDays(2);
        start = LocalTime.of(14, 0);
        end = LocalTime.of(16, 0);

        // Value objects / Records instantiated directly without Mockito
        morador = new AuthenticatedUser(moradorId, "Morador Teste", "MORADOR");
        admin = new AuthenticatedUser(UUID.randomUUID(), "Admin Teste", "ADMINISTRADOR");
    }

    // --- Object Builders / Helpers ---

    private Area buildArea(UUID id, Area.AreaStatus status) {
        Area area = new Area();
        area.setId(id);
        area.setStatus(status);
        return area;
    }

    private Morador buildMorador(UUID id, boolean ativo) {
        Morador m = new Morador();
        m.setId(id);
        m.setAtivo(ativo);
        return m;
    }

    private Reserva buildReserva(UUID id, Area area, Morador moradorEntity, LocalDate date, LocalTime startTime, LocalTime endTime, Reserva.ReservaStatus status) {
        Reserva r = new Reserva();
        r.setId(id);
        r.setArea(area);
        r.setMorador(moradorEntity);
        r.setData(date);
        r.setHorarioInicio(startTime);
        r.setHorarioFim(endTime);
        r.setStatus(status);
        return r;
    }

    // ============================================================
    // CADASTRAR RESERVA
    // ============================================================

    @Nested
    @DisplayName("cadastrarReserva")
    class CadastrarReservaTests {

        @Test
        @DisplayName("Deve criar uma reserva SOLICITADA valida e persistir todos os atributos corretos")
        void deveCriarReservaValidaEComDadosCorretos() {
            // Arrange
            Area activeArea = buildArea(areaId, Area.AreaStatus.ATIVA);
            Morador activeMorador = buildMorador(moradorId, true);
            Reserva savedReserva = buildReserva(reservaId, activeArea, activeMorador, futureDate, start, end, Reserva.ReservaStatus.SOLICITADA);

            when(areaRepository.findById(areaId)).thenReturn(Optional.of(activeArea));
            when(areaRepository.getReferenceById(areaId)).thenReturn(activeArea);
            when(moradorRepository.getReferenceById(moradorId)).thenReturn(activeMorador);
            when(reservaRepository.buscarReservaConcorrente(areaId, futureDate, start, end)).thenReturn(Optional.empty());
            when(reservaRepository.save(any(Reserva.class))).thenReturn(savedReserva);
            when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(savedReserva));

            // Act
            Reserva result = service.cadastrarReserva(morador, areaId, futureDate, start, end);

            // Assert
            assertThat(result)
                    .isNotNull()
                    .extracting(Reserva::getId, Reserva::getStatus, Reserva::getData, Reserva::getHorarioInicio, Reserva::getHorarioFim)
                    .containsExactly(reservaId, Reserva.ReservaStatus.SOLICITADA, futureDate, start, end);

            verify(authenticatedUserValidator).assertMorador(morador);
            verify(reservaRepository).save(reservaCaptor.capture());

            Reserva captured = reservaCaptor.getValue();
            assertThat(captured)
                    .extracting(Reserva::getArea, Reserva::getMorador, Reserva::getData, Reserva::getHorarioInicio, Reserva::getHorarioFim, Reserva::getStatus)
                    .containsExactly(activeArea, activeMorador, futureDate, start, end, Reserva.ReservaStatus.SOLICITADA);
        }

        @Test
        @DisplayName("Deve rejeitar morador nao autorizado")
        void deveRejeitarMoradorNaoAutorizado() {
            // Arrange
            doThrow(new BusinessRuleException("Usuario nao e morador"))
                    .when(authenticatedUserValidator).assertMorador(morador);

            // Act & Assert
            assertThatThrownBy(() -> service.cadastrarReserva(morador, areaId, futureDate, start, end))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Usuario nao e morador");

            verifyNoInteractions(areaRepository, reservaRepository);
        }

        @Test
        @DisplayName("Deve rejeitar data/hora de inicio no passado")
        void deveRejeitarInicioNoPassado() {
            // Arrange
            LocalDate passado = LocalDate.now().minusDays(1);

            // Act & Assert
            assertThatThrownBy(() -> service.cadastrarReserva(morador, areaId, passado, start, end))
                    .isInstanceOf(BusinessRuleException.class);

            verifyNoInteractions(areaRepository);
            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve rejeitar quando inicio e depois do fim")
        void deveRejeitarInicioDepoisDoFim() {
            // Arrange
            LocalTime inicio = LocalTime.of(18, 0);
            LocalTime fim = LocalTime.of(16, 0);

            // Act & Assert
            assertThatThrownBy(() -> service.cadastrarReserva(morador, areaId, futureDate, inicio, fim))
                    .isInstanceOf(BusinessRuleException.class);

            verifyNoInteractions(areaRepository);
            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("REQUISITO: deve rejeitar quando inicio e fim sao iguais")
        void deveRejeitarIntervaloComInicioEFimIguais() {
            // Arrange
            LocalTime horario = LocalTime.of(14, 0);

            // Act & Assert
            assertThatThrownBy(() -> service.cadastrarReserva(morador, areaId, futureDate, horario, horario))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("Deve rejeitar area inexistente")
        void deveRejeitarAreaInexistente() {
            // Arrange
            when(areaRepository.findById(areaId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> service.cadastrarReserva(morador, areaId, futureDate, start, end))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(reservaRepository, never()).save(any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"DESATIVADA", "RETIRADA"})
        @DisplayName("Deve rejeitar area inativa ou retirada")
        void deveRejeitarAreaInativaOuRetirada(String statusName) {
            // Arrange
            Area.AreaStatus status = Area.AreaStatus.valueOf(statusName);
            Area areaInativa = buildArea(areaId, status);
            when(areaRepository.findById(areaId)).thenReturn(Optional.of(areaInativa));

            // Act & Assert
            assertThatThrownBy(() -> service.cadastrarReserva(morador, areaId, futureDate, start, end))
                    .isInstanceOf(BusinessRuleException.class);

            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve rejeitar quando existe reserva concorrente")
        void deveRejeitarReservaConcorrente() {
            // Arrange
            Area activeArea = buildArea(areaId, Area.AreaStatus.ATIVA);
            Morador activeMorador = buildMorador(moradorId, true);
            Reserva concorrente = buildReserva(UUID.randomUUID(), activeArea, activeMorador, futureDate, start, end, Reserva.ReservaStatus.APROVADA);

            when(areaRepository.findById(areaId)).thenReturn(Optional.of(activeArea));
            when(reservaRepository.buscarReservaConcorrente(areaId, futureDate, start, end)).thenReturn(Optional.of(concorrente));

            // Act & Assert
            assertThatThrownBy(() -> service.cadastrarReserva(morador, areaId, futureDate, start, end))
                    .isInstanceOf(BusinessRuleException.class);

            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve rejeitar morador inativo")
        void deveRejeitarMoradorInativo() {
            // Arrange
            Area activeArea = buildArea(areaId, Area.AreaStatus.ATIVA);
            Morador inactiveMorador = buildMorador(moradorId, false);

            when(areaRepository.findById(areaId)).thenReturn(Optional.of(activeArea));
            when(reservaRepository.buscarReservaConcorrente(any(), any(), any(), any())).thenReturn(Optional.empty());
            when(moradorRepository.getReferenceById(moradorId)).thenReturn(inactiveMorador);
            when(areaRepository.getReferenceById(areaId)).thenReturn(activeArea);

            // Act & Assert
            assertThatThrownBy(() -> service.cadastrarReserva(morador, areaId, futureDate, start, end))
                    .isInstanceOf(BusinessRuleException.class);

            verify(reservaRepository, never()).save(any());
        }
    }

    // ============================================================
    // REJEITAR RESERVA
    // ============================================================

    @Nested
    @DisplayName("rejeitarReserva")
    class RejeitarReservaTests {

        @Test
        @DisplayName("Deve rejeitar reserva com motivo valido")
        void deveRejeitarComMotivo() {
            // Arrange
            Area activeArea = buildArea(areaId, Area.AreaStatus.ATIVA);
            Morador activeMorador = buildMorador(moradorId, true);
            Reserva reserva = buildReserva(reservaId, activeArea, activeMorador, futureDate, start, end, Reserva.ReservaStatus.SOLICITADA);

            when(reservaRepository.getReferenceById(reservaId)).thenReturn(reserva);

            // Act
            service.rejeitarReserva(admin, reservaId, "Manutenção da área");

            // Assert
            assertThat(reserva.getStatus()).isEqualTo(Reserva.ReservaStatus.REJEITADA);
            assertThat(reserva.getMotivoCancelamento()).isEqualTo("Manutenção da área");

            verify(authenticatedUserValidator).assertAdministrador(admin);
            verify(reservaRepository).save(reserva);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Deve rejeitar motivo nulo, vazio ou composto apenas por espacos")
        void deveRejeitarMotivoInvalido(String motivoInvalido) {
            // Act & Assert
            assertThatThrownBy(() -> service.rejeitarReserva(admin, reservaId, motivoInvalido))
                    .isInstanceOf(BusinessRuleException.class);

            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve rejeitar administrador nao autorizado")
        void deveRejeitarAdminNaoAutorizado() {
            // Arrange
            doThrow(new BusinessRuleException("Não autorizado"))
                    .when(authenticatedUserValidator).assertAdministrador(admin);

            // Act & Assert
            assertThatThrownBy(() -> service.rejeitarReserva(admin, reservaId, "Motivo"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Não autorizado");

            verifyNoInteractions(reservaRepository);
        }

        @Test
        @DisplayName("Deve normalizar o motivo antes de salvar")
        void deveNormalizarMotivo() {
            // Arrange
            Area activeArea = buildArea(areaId, Area.AreaStatus.ATIVA);
            Morador activeMorador = buildMorador(moradorId, true);
            Reserva reserva = buildReserva(reservaId, activeArea, activeMorador, futureDate, start, end, Reserva.ReservaStatus.SOLICITADA);

            when(reservaRepository.getReferenceById(reservaId)).thenReturn(reserva);

            // Act
            service.rejeitarReserva(admin, reservaId, "  Área em manutenção  ");

            // Assert
            assertThat(reserva.getMotivoCancelamento()).isEqualTo("Área em manutenção");
            verify(reservaRepository).save(reserva);
        }
    }

    // ============================================================
    // CANCELAMENTO PELO MORADOR"
    // ============================================================

    @Nested
    @DisplayName("cancelarReservaMorador")
    class CancelarReservaMoradorTests {

        @ParameterizedTest
        @ValueSource(strings = {"SOLICITADA", "APROVADA"})
        @DisplayName("Deve cancelar reserva quando status for SOLICITADA ou APROVADA")
        void deveCancelarReservaValida(String statusName) {
            // Arrange
            Reserva.ReservaStatus status = Reserva.ReservaStatus.valueOf(statusName);
            Area activeArea = buildArea(areaId, Area.AreaStatus.ATIVA);
            Morador activeMorador = buildMorador(moradorId, true);
            Reserva reserva = buildReserva(reservaId, activeArea, activeMorador, futureDate, start, end, status);

            when(reservaRepository.getReferenceById(reservaId)).thenReturn(reserva);

            // Act
            service.cancelarReservaMorador(morador, reservaId);

            // Assert
            assertThat(reserva.getStatus()).isEqualTo(Reserva.ReservaStatus.CANCELADA);
            verify(reservaRepository).save(reserva);
        }

        @Test
        @DisplayName("Nao deve cancelar reserva rejeitada")
        void naoDeveCancelarRejeitada() {
            // Arrange
            Area activeArea = buildArea(areaId, Area.AreaStatus.ATIVA);
            Morador activeMorador = buildMorador(moradorId, true);
            Reserva reserva = buildReserva(reservaId, activeArea, activeMorador, futureDate, start, end, Reserva.ReservaStatus.REJEITADA);

            when(reservaRepository.getReferenceById(reservaId)).thenReturn(reserva);

            // Act & Assert
            assertThatThrownBy(() -> service.cancelarReservaMorador(morador, reservaId))
                    .isInstanceOf(BusinessRuleException.class);

            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Nao deve cancelar reserva cujo inicio ja passou")
        void naoDeveCancelarReservaExpirada() {
            // Arrange
            Area activeArea = buildArea(areaId, Area.AreaStatus.ATIVA);
            Morador activeMorador = buildMorador(moradorId, true);
            LocalDate passado = LocalDate.now().minusDays(1);
            Reserva reserva = buildReserva(reservaId, activeArea, activeMorador, passado, start, end, Reserva.ReservaStatus.SOLICITADA);

            when(reservaRepository.getReferenceById(reservaId)).thenReturn(reserva);

            // Act & Assert
            assertThatThrownBy(() -> service.cancelarReservaMorador(morador, reservaId))
                    .isInstanceOf(BusinessRuleException.class);

            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("REQUISITO: reserva cancelada nao deve poder ser cancelada novamente")
        void naoDeveCancelarReservaJaCancelada() {
            // Arrange
            Area activeArea = buildArea(areaId, Area.AreaStatus.ATIVA);
            Morador activeMorador = buildMorador(moradorId, true);
            Reserva reserva = buildReserva(reservaId, activeArea, activeMorador, futureDate, start, end, Reserva.ReservaStatus.CANCELADA);

            when(reservaRepository.getReferenceById(reservaId)).thenReturn(reserva);

            // Act & Assert
            assertThatThrownBy(() -> service.cancelarReservaMorador(morador, reservaId))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    // ============================================================
    // APROVAR RESERVA
    // ============================================================

    @Nested
    @DisplayName("aprovarReserva")
    class AprovarReservaTests {

        @Test
        @DisplayName("Deve aprovar reserva valida")
        void deveAprovarReservaValida() {
            // Arrange
            Area activeArea = buildArea(areaId, Area.AreaStatus.ATIVA);
            Morador activeMorador = buildMorador(moradorId, true);
            Reserva reserva = buildReserva(reservaId, activeArea, activeMorador, futureDate, start, end, Reserva.ReservaStatus.SOLICITADA);

            when(reservaRepository.getReferenceById(reservaId)).thenReturn(reserva);
            when(areaRepository.findById(areaId)).thenReturn(Optional.of(activeArea));
            when(reservaRepository.buscarReservaConcorrente(areaId, futureDate, start, end)).thenReturn(Optional.empty());

            // Act
            service.aprovarReserva(admin, reservaId);

            // Assert
            assertThat(reserva.getStatus()).isEqualTo(Reserva.ReservaStatus.APROVADA);
            verify(reservaRepository).save(reserva);
        }

        @ParameterizedTest
        @ValueSource(strings = {"REJEITADA", "CANCELADA"})
        @DisplayName("Nao deve aprovar reserva rejeitada ou cancelada")
        void naoDeveAprovarReservaEmEstadoTerminal(String statusName) {
            // Arrange
            Reserva.ReservaStatus status = Reserva.ReservaStatus.valueOf(statusName);
            Area activeArea = buildArea(areaId, Area.AreaStatus.ATIVA);
            Morador activeMorador = buildMorador(moradorId, true);
            Reserva reserva = buildReserva(reservaId, activeArea, activeMorador, futureDate, start, end, status);

            when(reservaRepository.getReferenceById(reservaId)).thenReturn(reserva);
            when(areaRepository.findById(areaId)).thenReturn(Optional.of(activeArea));

            // Act & Assert
            assertThatThrownBy(() -> service.aprovarReserva(admin, reservaId))
                    .isInstanceOf(BusinessRuleException.class);

            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("REQUISITO: administrador nao deve aprovar reserva ja aprovada")
        void naoDeveAprovarReservaJaAprovada() {
            // Arrange
            Area activeArea = buildArea(areaId, Area.AreaStatus.ATIVA);
            Morador activeMorador = buildMorador(moradorId, true);
            Reserva reserva = buildReserva(reservaId, activeArea, activeMorador, futureDate, start, end, Reserva.ReservaStatus.APROVADA);

            when(reservaRepository.getReferenceById(reservaId)).thenReturn(reserva);
            when(areaRepository.findById(areaId)).thenReturn(Optional.of(activeArea));

            // Act & Assert
            assertThatThrownBy(() -> service.aprovarReserva(admin, reservaId))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    // ============================================================
    // LISTAGEM
    // ============================================================

    @Nested
    @DisplayName("listarReservasDoMorador")
    class ListarReservasTests {

        @Test
        @DisplayName("Deve validar que o usuario e morador e retornar a pagina de reservas")
        void deveValidarMoradorERetornarPagina() {
            // Arrange
            PageRequest pageRequest = PageRequest.of(0, 10);
            Area activeArea = buildArea(areaId, Area.AreaStatus.ATIVA);
            Morador activeMorador = buildMorador(moradorId, true);
            Reserva r1 = buildReserva(UUID.randomUUID(), activeArea, activeMorador, futureDate, start, end, Reserva.ReservaStatus.SOLICITADA);

            Page<Reserva> pageResult = new PageImpl<>(List.of(r1), pageRequest, 1);
            when(reservaRepository.findByMoradorId(moradorId, pageRequest)).thenReturn(pageResult);

            // Act
            PageResult<Reserva> result = service.listarReservasDoMorador(morador, pageRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1).containsExactly(r1);

            verify(authenticatedUserValidator).assertMorador(morador);
            verify(reservaRepository).findByMoradorId(moradorId, pageRequest);
        }

        @Test
        @DisplayName("Nao deve listar reservas para usuario nao morador")
        void naoDeveListarParaNaoMorador() {
            // Arrange
            PageRequest pageRequest = PageRequest.of(0, 10);
            doThrow(new BusinessRuleException("Não é morador"))
                    .when(authenticatedUserValidator).assertMorador(morador);

            // Act & Assert
            assertThatThrownBy(() -> service.listarReservasDoMorador(morador, pageRequest))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessage("Não é morador");

            verify(reservaRepository, never()).findByMoradorId(any(), any());
        }
    }
}