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
                            <p class="eyebrow">Cadastro</p>
                            <h2>Nova reserva</h2>
                        </div>
                    </div>
                    <form method="post" action="${ctx}/admin/reservas" class="stack-form">
                        <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>

                        <label class="field">
                            <span>Area</span>
                            <select name="areaId" required>
                                <option value="">Selecione uma area</option>

                                <c:forEach var="area" items="${areas}">
                                    <option value="${area.id}"
                                        ${reservaForm.areaId == area.id ? 'selected' : ''}>
                                        ${area.identificacao}
                                    </option>
                                </c:forEach>
                            </select>
                        </label>

                        <label class="field">
                            <span>Data</span>
                            <input
                                type="date"
                                name="data"
                                value="${reservaForm.data}"
                                required>
                        </label>

                        <label class="field">
                            <span>Horario de inicio</span>
                            <input
                                type="time"
                                name="horarioInicio"
                                value="${reservaForm.horarioInicio}"
                                required>
                        </label>

                        <label class="field">
                            <span>Horario de fim</span>
                            <input
                                type="time"
                                name="horarioFim"
                                value="${reservaForm.horarioFim}"
                                required>
                        </label>

                        <button type="submit" class="btn btn-primary">
                            Cadastrar reserva
                        </button>
                    </form>
                </article>

                <article class="card">
                    <div class="section-header">
                        <div>
                            <p class="eyebrow">Lista</p>
                            <h2>Reservas cadastradas</h2>
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
                                        <th>Identificacao</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach items="${reservas}" var="reserva">
                                        <tr>
                                            <td>${reserva.id}</td>
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
