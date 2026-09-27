<%@ include file="/WEB-INF/jsp/fragments/taglibs.jspf" %>
<!DOCTYPE html>
<html lang="pt-BR">
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<body data-page="admin-reservas">
<div class="app-shell">
    <%@ include file="/WEB-INF/jsp/fragments/sidebar.jspf" %>
    <div class="app-main">
        <%@ include file="/WEB-INF/jsp/fragments/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="/WEB-INF/jsp/fragments/alerts.jspf" %>

            <section class="one-column-grid">
                <article class="card">
                    <div class="section-header">
                        <div>
                            <p class="eyebrow">Lista</p>
                            <h2>Reservas dos moradores</h2>
                        </div>
                        <div class="toolbar-inline">
                            <input type="search" class="table-search" placeholder="Filtrar localmente" data-filter-input data-filter-target="reservas-table">
                        </div>
                    </div>

                    <c:choose>
                        <c:when test="${empty reservas}">
                            <div class="empty-state">
                                <h3>Nenhuma reserva cadastrada</h3>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-wrap">
                                <table class="data-table" data-filter-table="reservas-table">
                                    <thead>
                                    <tr>
                                        <th>Status</th>
                                        <th>Area</th>
                                        <th>Data</th>
                                        <th>Horario inicial</th>
                                        <th>Horario final</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach items="${reservas}" var="reserva">
                                        <tr>
                                            <td>${reserva.status}</td>
                                            <td>${reserva.areaIdentificacao}</td>
                                            <td>${reserva.data}</td>
                                            <td>${reserva.horarioInicio}</td>
                                            <td>${reserva.horarioFim}</td>
                                            <td class="cell-actions">
                                                <form method="post" action="${ctx}/admin/reservas/${reserva.id}" onsubmit="return confirm('Deseja aprovar esta reserva?');">
                                                    <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                                                    <input type="hidden" name="_method" value="patch">
                                                    <button type="submit" class="btn btn-secondary">Aprovar</button>
                                                </form>
                                                <form method="post"
                                                    action="${ctx}/admin/reservas/${reserva.id}"
                                                    onsubmit="return setMotivoRejeicao(this);">

                                                    <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>

                                                    <input type="hidden" name="_method" value="delete">
                                                    <input type="hidden" name="motivo">

                                                    <button type="submit" class="btn btn-secondary">Rejeitar</button>
                                                </form>

                                                <script>
                                                function setMotivoRejeicao(form) {
                                                    const motivo = prompt('Por qual motivo deseja rejeitar esta reserva?');

                                                    if (motivo === null || motivo.trim() === '') {
                                                        return false;
                                                    }

                                                    form.elements['motivo'].value = motivo;

                                                    return true;
                                                }
                                                </script>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div class="pagination">
                        <c:if test="${reservasPage.hasPrevious}">
                            <a class="btn btn-secondary" href="${ctx}/admin/reservas?page=${reservasPage.page - 1}&size=${reservasPage.size}">Anterior</a>
                        </c:if>
                        <span>Pagina ${reservasPage.page + 1} de ${reservasPage.totalPages == 0 ? 1 : reservasPage.totalPages}</span>
                        <c:if test="${reservasPage.hasNext}">
                            <a class="btn btn-secondary" href="${ctx}/admin/reservas?page=${reservasPage.page + 1}&size=${reservasPage.size}">Proxima</a>
                        </c:if>
                    </div>
                </article>
            </section>
        </main>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/fragments/scripts.jspf" %>
</body>
</html>
