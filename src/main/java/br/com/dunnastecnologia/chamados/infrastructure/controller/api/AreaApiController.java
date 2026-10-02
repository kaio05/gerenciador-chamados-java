package br.com.dunnastecnologia.chamados.infrastructure.controller.api;

import br.com.dunnastecnologia.chamados.application.UserCase.AdminUseCases;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.WebControllerSupport;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.AreaForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/areas")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AreaApiController {

    private final AdminUseCases adminUseCases;
    private final WebControllerSupport support;

    public AreaApiController(AdminUseCases adminUseCases, WebControllerSupport support) {
        this.adminUseCases = adminUseCases;
        this.support = support;
    }

    @Operation(summary = "Cadastra uma nova area", tags = "15 - Admin Web - Areas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Area cadastrada com sucesso e redirecionamento para a listagem."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado.")
    })
    @PostMapping
    public String cadastrarArea(
            Authentication authentication,
            @ModelAttribute AreaForm areaForm,
            RedirectAttributes redirectAttributes
    ) {
        adminUseCases.cadastrarArea(
                support.authenticatedUser(authentication),
                areaForm.getIdentificacao()
        );
        redirectAttributes.addFlashAttribute("successMessage", "Area cadastrada com sucesso.");
        return "redirect:/admin/areas";
    }

    @Operation(summary = "Remove uma area", tags = "15 - Admin Web - Areas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "area removida com sucesso e redirecionamento para a tela de origem."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Area nao encontrada.")
    })
    @DeleteMapping("/{areaId}")
    public String removerArea(
            Authentication authentication,
            @PathVariable UUID areaId,
            RedirectAttributes redirectAttributes
    ) {
        adminUseCases.removerArea(support.authenticatedUser(authentication), areaId);
        redirectAttributes.addFlashAttribute("successMessage", "Area removida com sucesso.");
        return "redirect:/admin/areas";
    }

    @Operation(summary = "Atualiza os dados de uma area", tags = "15 - Admin Web - Areas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Area atualizada com sucesso e redirecionamento para o detalhe."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Area nao encontrada.")
    })
    @PatchMapping("/{areaId}")
    public String atualizarArea(
            Authentication authentication,
            @PathVariable UUID areaId,
            @ModelAttribute AreaForm areaForm,
            RedirectAttributes redirectAttributes
    ) {
        adminUseCases.atualizarArea(
                support.authenticatedUser(authentication),
                areaId,
                areaForm.getIdentificacao()
        );
        redirectAttributes.addFlashAttribute("successMessage", "Area atualizado com sucesso.");
        return "redirect:/admin/areas/" + areaId;
    }
}
