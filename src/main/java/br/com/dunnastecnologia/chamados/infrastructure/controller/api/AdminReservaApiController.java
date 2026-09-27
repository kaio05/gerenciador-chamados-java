package br.com.dunnastecnologia.chamados.infrastructure.controller.api;

import br.com.dunnastecnologia.chamados.application.UserCase.AdminUseCases;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.WebControllerSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reservas")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminReservaApiController {

    private final AdminUseCases adminUseCases;
    private final WebControllerSupport support;

    public AdminReservaApiController(AdminUseCases adminUseCases, WebControllerSupport support) {
        this.adminUseCases = adminUseCases;
        this.support = support;
    }

    @Operation(summary = "Rejeita uma reserva", tags = "06 - Morador Web - Reservas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "reserva rejeitada com sucesso e redirecionamento para a tela de origem."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Reserva nao encontrada.")
    })
    @DeleteMapping("/{reservaId}")
    public String rejeitarReserva(
            Authentication authentication,
            @PathVariable UUID reservaId,
            @RequestParam String motivo,
            RedirectAttributes redirectAttributes
    ) {
        adminUseCases.rejeitarReserva(support.authenticatedUser(authentication), reservaId, motivo);
        redirectAttributes.addFlashAttribute("successMessage", "Reserva rejeitada com sucesso.");
        return "redirect:/admin/reservas";
    }

    @PatchMapping ("/{reservaId}")
    public String aprovarReserva(
            Authentication authentication,
            @PathVariable UUID reservaId,
            RedirectAttributes redirectAttributes
    ) {
        adminUseCases.aprovarReserva(support.authenticatedUser(authentication), reservaId);
        redirectAttributes.addFlashAttribute("successMessage", "Reserva aprovada com sucesso.");
        return "redirect:/admin/reservas";
    }
}
