<%@ include file="/WEB-INF/jsp/fragments/taglibs.jspf" %>
<!DOCTYPE html>
<html lang="pt-BR">
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<body data-page="admin-area-detalhe">
<div class="app-shell">
    <%@ include file="/WEB-INF/jsp/fragments/sidebar.jspf" %>
    <div class="app-main">
        <%@ include file="/WEB-INF/jsp/fragments/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="/WEB-INF/jsp/fragments/alerts.jspf" %>

            <section class="two-column-grid">
                <article class="card">
                    <div class="section-header">
                        <div>
                            <p class="eyebrow">Edicao</p>
                            <h2>${area.identificacao}</h2>
                        </div>
                        <span class="status-pill neutral">${area.status}</span>
                    </div>

                    <form method="post" action="${ctx}/admin/areas/${area.id}" class="stack-form">
                        <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                        <input type="hidden" name="_method" value="patch">
                        <label class="field">
                            <span>Identificacao</span>
                            <input type="text" name="identificacao" value="${areaForm.identificacao}" maxlength="255" required>
                        </label>
                        <div class="button-row">
                            <button type="submit" class="btn btn-primary">Salvar alteracoes</button>
                            <a href="${ctx}/admin/areas" class="btn btn-secondary">Voltar</a>
                        </div>
                    </form>
                    <c:if test="${area.status eq 'ATIVA'}">
                        <form method="post" action="${ctx}/admin/areas/${area.id}" data-confirm="Remover esta area? A acao nao pode ser desfeita." class="inline-form danger-zone">
                            <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                            <input type="hidden" name="_method" value="delete">
                            <button type="submit" class="btn btn-danger">Remover area</button>
                        </form>
                    </c:if>
                </article>
            </section>
        </main>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/fragments/scripts.jspf" %>
</body>
</html>
