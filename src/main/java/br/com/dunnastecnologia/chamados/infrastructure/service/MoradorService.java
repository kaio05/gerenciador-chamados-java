package br.com.dunnastecnologia.chamados.infrastructure.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.UserCase.MoradorUseCases;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Area;
import br.com.dunnastecnologia.chamados.domain.model.Chamado;
import br.com.dunnastecnologia.chamados.domain.model.Comentario;
import br.com.dunnastecnologia.chamados.domain.model.Reserva;
import br.com.dunnastecnologia.chamados.domain.model.Unidade;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.AuthenticatedUserValidator;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.PageResultMapper;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.ResourceNotFoundException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.AreaRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.MoradorRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.ReservaRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.UnidadeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MoradorService implements MoradorUseCases {

    private final UnidadeRepository unidadeRepository;
    private final AreaRepository areaRepository;
    private final MoradorRepository moradorRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaService reservaService;
    private final ChamadoService chamadoService;
    private final ComentarioService comentarioService;
    private final AuthenticatedUserValidator authenticatedUserValidator;

    public MoradorService(
            UnidadeRepository unidadeRepository,
            AreaRepository areaRepository,
            MoradorRepository moradorRepository,
            ReservaRepository reservaRepository,
            ReservaService reservaService,
            ChamadoService chamadoService,
            ComentarioService comentarioService,
            AuthenticatedUserValidator authenticatedUserValidator
    ) {
        this.unidadeRepository = unidadeRepository;
        this.areaRepository = areaRepository;
        this.moradorRepository = moradorRepository;
        this.reservaRepository = reservaRepository;
        this.reservaService = reservaService;
        this.chamadoService = chamadoService;
        this.comentarioService = comentarioService;
        this.authenticatedUserValidator = authenticatedUserValidator;
    }

    @Override
    public PageResult<Unidade> listarMinhasUnidades(AuthenticatedUser morador, PageRequest pageRequest) {
        authenticatedUserValidator.assertMorador(morador);
        return PageResultMapper.fromPage(unidadeRepository.findByMoradorId(morador.id(), pageRequest));
    }

    @Override
    @Transactional
    public Chamado abrirChamado(AuthenticatedUser morador, UUID unidadeId, UUID tipoChamadoId, String descricao) {
        return chamadoService.abrirChamado(morador, unidadeId, tipoChamadoId, descricao);
    }

    @Override
    public PageResult<Chamado> listarMeusChamados(
            AuthenticatedUser morador,
            UUID statusId,
            UUID unidadeId,
            UUID tipoChamadoId,
            LocalDate dataAbertura,
            PageRequest pageRequest
    ) {
        return chamadoService.listarChamadosDoMorador(morador, statusId, unidadeId, tipoChamadoId, dataAbertura, pageRequest);
    }

    @Override
    public Chamado buscarMeuChamadoPorId(AuthenticatedUser morador, UUID chamadoId) {
        return chamadoService.buscarChamadoDoMorador(morador, chamadoId);
    }

    @Override
    @Transactional
    public Comentario comentarChamado(AuthenticatedUser morador, UUID chamadoId, String mensagem) {
        return comentarioService.comentar(morador, chamadoId, mensagem);
    }

    @Override
    @Transactional
    public Chamado reabrirChamado(AuthenticatedUser morador, UUID chamadoId) {
        return chamadoService.reabrirComoMorador(morador, chamadoId);
    }

    @Override
    public PageResult<Reserva> listarMinhasReservas(AuthenticatedUser morador, PageRequest pageRequest) {
        return reservaService.listarReservasDoMorador(morador, pageRequest);
    }

    @Override
    @Transactional
    public Reserva cadastrarReserva(AuthenticatedUser morador, UUID areaId, LocalDate data, LocalTime horarioInicio, LocalTime horarioFim) {
        authenticatedUserValidator.assertMorador(morador);
        LocalDateTime reservaInicio = LocalDateTime.of(data, horarioInicio);
        LocalDateTime reservaFim = LocalDateTime.of(data, horarioFim);

        if(reservaInicio.isBefore(LocalDateTime.now()) || reservaInicio.isAfter(reservaFim))
            throw new BusinessRuleException("Insira um intervalo de tempo válido.");
        Area area = areaRepository.findById(areaId)
        .orElseThrow(() -> new IllegalArgumentException("Área não encontrada"));
        if(area.getStatus() == Area.AreaStatus.DESATIVADA || area.getStatus() == Area.AreaStatus.RETIRADA)
            throw new BusinessRuleException("Morador não pode solicitar reserva para uma area desativada ou retirada");
        Optional<Reserva> concorrente = reservaRepository.buscarReservaConcorrente(areaId, data, horarioInicio, horarioFim);
        if(concorrente.isPresent()) throw new BusinessRuleException("Morador não pode solicitar reserva para uma area ja reservada");

        Reserva reserva = new Reserva();
        reserva.setArea(areaRepository.getReferenceById(areaId));
        reserva.setData(data);
        reserva.setMorador(moradorRepository.getReferenceById(morador.id()));
        reserva.setHorarioInicio(horarioInicio);
        reserva.setHorarioFim(horarioFim);
        reserva.setStatus(Reserva.ReservaStatus.SOLICITADA);

        Reserva savedReserva = reservaRepository.save(reserva);
        return reservaRepository.findById(savedReserva.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva nao encontrada apos cadastro"));
    }
}
