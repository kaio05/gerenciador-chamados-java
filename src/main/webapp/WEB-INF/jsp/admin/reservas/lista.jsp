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

            <section class="two-column-grid">
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
