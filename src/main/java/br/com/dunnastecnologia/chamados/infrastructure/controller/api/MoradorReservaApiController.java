package br.com.dunnastecnologia.chamados.infrastructure.controller.api;

import br.com.dunnastecnologia.chamados.application.UserCase.MoradorUseCases;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.WebControllerSupport;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.ReservaForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/morador/reservas")
@PreAuthorize("hasRole('MORADOR')")
public class MoradorReservaApiController {

    private final MoradorUseCases moradorUseCases;
    private final WebControllerSupport support;

    public MoradorReservaApiController(MoradorUseCases moradorUseCases, WebControllerSupport support) {
        this.moradorUseCases = moradorUseCases;
        this.support = support;
    }

    @Operation(summary = "Cadastra uma nova reserva", tags = "03 - Admin Web - Reservas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Reserva cadastrada com sucesso e redirecionamento para a listagem."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado.")
    })
    @PostMapping
    public String cadastrarReserva(
            Authentication authentication,
            @ModelAttribute ReservaForm reservaForm,
            RedirectAttributes redirectAttributes
    ) {
        moradorUseCases.cadastrarReserva(
                support.authenticatedUser(authentication),
                reservaForm.getAreaId(),
                reservaForm.getData(),
                reservaForm.getHorarioInicio(),
                reservaForm.getHorarioFim()
        );
        redirectAttributes.addFlashAttribute("successMessage", "Reserva cadastrada com sucesso.");
        return "redirect:/morador/reservas";
    }
}
