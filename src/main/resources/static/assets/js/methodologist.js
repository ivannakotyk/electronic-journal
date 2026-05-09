async function methodDash(){let [r,g,c]=await Promise.all([api('/api/reports'),api('/api/groups'),allGrades()]);qs('#reports').textContent=r.length;qs('#groups').textContent=g.length;qs('#avg').textContent=(c.out.length?c.out.reduce((s,x)=>s+Number(x.value),0)/c.out.length:0).toFixed(1)}
async function monitoring() {
    const form = qs('#monitorForm');
    const rowsBox = qs('#monRows');
    const errorBox = qs('#monitoringError');

    const disciplineFilter = qs('#disciplineFilter');
    const teacherFilter = qs('#teacherFilter');
    const refreshButton = qs('#refreshMonitoring');

    const formatDateTime = value => {
        if (!value) return '—';

        try {
            return new Date(value).toLocaleString('uk-UA', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch {
            return value;
        }
    };

    const formatDate = value => {
        if (!value) return '—';

        try {
            return new Date(value).toLocaleDateString('uk-UA');
        } catch {
            return value;
        }
    };

    const completionBadge = value => {
        const n = Number(value || 0);

        if (n >= 80) return `<b style="color:#15803d">${n.toFixed(1)}%</b>`;
        if (n >= 60) return `<b style="color:#ca8a04">${n.toFixed(1)}%</b>`;

        return `<b style="color:#dc2626">${n.toFixed(1)}%</b>`;
    };

    const buildQuery = () => {
        const params = new URLSearchParams();

        if (form.disciplineId.value) {
            params.set('disciplineId', form.disciplineId.value);
        }

        if (form.teacherId.value) {
            params.set('teacherId', form.teacherId.value);
        }

        if (ses().userId) {
            params.set('methodologistId', ses().userId);
        }

        return params.toString();
    };

    const loadFilters = async () => {
        const disciplines = await api('/api/disciplines').catch(() => []);
        const users = await api('/api/users').catch(() => []);

        const teachers = users
            .filter(u => u.role === 'TEACHER')
            .sort((a, b) => a.fullName.localeCompare(b.fullName, 'uk'));

        disciplineFilter.innerHTML =
            `<option value="">Усі дисципліни</option>` +
            disciplines.map(d => `
                <option value="${d.id}">
                    ${d.name}
                </option>
            `).join('');

        teacherFilter.innerHTML =
            `<option value="">Усі викладачі</option>` +
            teachers.map(t => `
                <option value="${t.id}">
                    ${t.fullName}
                </option>
            `).join('');
    };

    const detailHtml = teacher => {
        if (!teacher.details || !teacher.details.length) {
            return `
                <tr class="monitor-detail hide" data-parent="${teacher.teacherId}">
                    <td colspan="6" class="empty">
                        Детальна інформація відсутня
                    </td>
                </tr>
            `;
        }

        return teacher.details.map(d => `
            <tr class="monitor-detail hide" data-parent="${teacher.teacherId}">
                <td colspan="6">
                    <div class="card" style="margin:8px 0">
                        <div class="grid cols4">
                            <div>
                                <div class="muted">Група</div>
                                <b>${d.groupCode}</b>
                            </div>

                            <div>
                                <div class="muted">Дисципліна</div>
                                <b>${d.disciplineName}</b>
                            </div>

                            <div>
                                <div class="muted">Заповненість</div>
                                ${completionBadge(d.completionPercent)}
                            </div>

                            <div>
                                <div class="muted">Незаповнених занять</div>
                                <b>${d.unfilledLessonsCount}</b>
                            </div>
                        </div>

                        <p class="muted">
                            Студентів: ${d.studentsCount}.
                            Заповнено позицій журналу: ${d.filledGradeCells}/${d.expectedGradeCells}.
                            Занять у розкладі: ${d.scheduleLessonsCount}.
                            Остання оцінка: ${formatDate(d.lastGradeDate)}.
                        </p>
                    </div>
                </td>
            </tr>
        `).join('');
    };

    const draw = data => {
        qs('#teachersCount').textContent = data.teachersCount ?? 0;
        qs('#avgCompletion').textContent = `${Number(data.averageCompletionPercent || 0).toFixed(1)}%`;
        qs('#unfilledCount').textContent = data.totalUnfilledLessonsCount ?? 0;
        qs('#lowCompletionCount').textContent = data.lowCompletionTeachersCount ?? 0;

        const teachers = data.teachers || [];

        if (!teachers.length) {
            rowsBox.innerHTML = `
                <tr>
                    <td colspan="6" class="empty">
                        Інформація відсутня
                    </td>
                </tr>
            `;
            return;
        }

        rowsBox.innerHTML = teachers.map(t => `
            <tr>
                <td>
                    <button class="teacher-link" data-teacher="${t.teacherId}">
                        ${t.teacherName}
                    </button>
                    <div class="muted">${t.position || 'Викладач'}</div>
                </td>

                <td>${formatDateTime(t.lastLoginAt)}</td>

                <td>${formatDate(t.lastGradeDate)}</td>

                <td>
                    ${completionBadge(t.journalCompletionPercent)}
                    <div class="muted">
                        ${t.filledGradeCells}/${t.expectedGradeCells}
                    </div>
                </td>

                <td>
                    <b>${t.gradesCount}</b>
                    <div class="muted">
                        Поточних: ${t.currentGradesCount},
                        семестрових: ${t.semesterGradesCount}
                    </div>
                </td>

                <td>
                    <b>${t.unfilledLessonsCount}</b>
                </td>
            </tr>

            ${detailHtml(t)}
        `).join('');

        qsa('[data-teacher]').forEach(btn => {
            btn.onclick = () => {
                const teacherId = btn.dataset.teacher;

                qsa(`[data-parent="${teacherId}"]`).forEach(row => {
                    row.classList.toggle('hide');
                });
            };
        });
    };

    const load = async () => {
        errorBox.classList.add('hide');

        try {
            const query = buildQuery();

            const data = await api(
                '/api/monitoring/teachers' + (query ? '?' + query : '')
            );

            draw(data);

            await api(
                `/api/monitoring/audit?methodologistId=${ses().userId || ''}&filters=${encodeURIComponent(query || 'all')}`,
                { method: 'POST' }
            ).catch(() => {});

        } catch (e) {
            console.error(e);

            errorBox.classList.remove('hide');

            rowsBox.innerHTML = `
                <tr>
                    <td colspan="6" class="empty">
                        Технічна помилка. Оновіть сторінку.
                    </td>
                </tr>
            `;

            toast('Не вдалося отримати статистику моніторингу', 'error');
        }
    };

    await loadFilters();

    form.onsubmit = e => {
        e.preventDefault();
        load();
    };

    refreshButton.onclick = load;

    load();
}
function reportPreviewHtml(r) {

    return `
        <section class="section card">

            <div class="toolbar">

                <div>
                    <h2>Перегляд звіту №${r.id}</h2>

                    <p class="muted">
                        ${r.period} · ${r.groupCode} · ${r.disciplineName}
                    </p>
                </div>

                <div>
                    <a
                        class="btn2"
                        href="/api/reports/${r.id}/pdf"
                    >
                        PDF
                    </a>

                    <a
                        class="btn2"
                        href="/api/reports/${r.id}/excel"
                    >
                        Excel
                    </a>
                </div>

            </div>

            <div class="grid cols3">

                <div class="stat">
                    <div>
                        <div class="muted">Середній бал</div>

                        <div class="num">
                            ${(r.averageScore || 0).toFixed(2)}
                        </div>
                    </div>

                    <span>📊</span>
                </div>

                <div class="stat">
                    <div>
                        <div class="muted">З оцінкою</div>

                        <div class="num">
                            ${r.studentsWithSemesterScore}
                        </div>
                    </div>

                    <span>✅</span>
                </div>

                <div class="stat">
                    <div>
                        <div class="muted">Без оцінки</div>

                        <div class="num">
                            ${r.studentsWithoutSemesterScore}
                        </div>
                    </div>

                    <span>⚠️</span>
                </div>

            </div>

            <h3>Розподіл результатів</h3>

            <div>
                ${r.distribution.map(x => `
                    <div
                        style="
                            display:grid;
                            grid-template-columns:90px 1fr 70px;
                            gap:10px;
                            align-items:center;
                            margin:8px 0
                        "
                    >
                        <b>${x.label}</b>

                        <div
                            style="
                                height:14px;
                                border-radius:999px;
                                background:linear-gradient(
                                    90deg,
                                    #2457c5 ${x.percent}%,
                                    #e8edf8 ${x.percent}%
                                );
                            "
                        ></div>

                        <span>
                            ${x.count} (${x.percent.toFixed(1)}%)
                        </span>
                    </div>
                `).join('')}
            </div>

            <h3>Таблиця студентів</h3>

            <div class="table">

                <table>

                    <thead>
                    <tr>
                        <th>№</th>
                        <th>Студент</th>
                        <th>Група</th>
                        <th>Дисципліна</th>
                        <th>Семестровий бал</th>
                    </tr>
                    </thead>

                    <tbody>

                    ${r.rows.map((x, i) => `
                        <tr>
                            <td>${i + 1}</td>

                            <td>${x.studentName}</td>

                            <td>${x.groupCode}</td>

                            <td>${x.disciplineName}</td>

                            <td>
                                <b>
                                    ${
        x.semesterScore == null
            ? '—'
            : Number(x.semesterScore).toFixed(2)
    }
                                </b>
                            </td>
                        </tr>
                    `).join('')}

                    </tbody>

                </table>

            </div>

        </section>
    `;
}
async function showReport(id) {

    let r = await api('/api/reports/' + id);

    let box = qs('#reportPreview');

    if (!box) {

        box = document.createElement('div');

        box.id = 'reportPreview';

        qs('main').appendChild(box);
    }

    box.innerHTML = reportPreviewHtml(r);

    box.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
    });
}

async function methodReports() {

    let [groups, disc, users] = await Promise.all([
        api('/api/groups'),
        api('/api/disciplines'),
        api('/api/users').catch(() => [])
    ]);

    let f = qs('#reportForm');

    f.groupId.innerHTML = opt(groups, g => g.code);

    f.disciplineId.innerHTML = opt(disc, d => d.name);

    f.teacherId.innerHTML =
        '<option value="">Усі / не зазначено</option>' +
        opt(byRole(users, 'TEACHER'), u => u.fullName);

    async function draw() {

        let data = await api('/api/reports');

        let tb = qs('#reportsRows');

        tb.innerHTML = data.map(r => `
            <tr>

                <td>${r.id}</td>

                <td>${r.period}</td>

                <td>${r.groupCode}</td>

                <td>${r.disciplineName || 'Усі'}</td>

                <td>${r.teacherName || 'Усі'}</td>

                <td>

                    <button
                        class="btn2"
                        onclick="showReport(${r.id})"
                    >
                        Переглянути
                    </button>

                    <a
                        class="btn2"
                        href="/api/reports/${r.id}/pdf"
                    >
                        PDF
                    </a>

                    <a
                        class="btn2"
                        href="/api/reports/${r.id}/excel"
                    >
                        Excel
                    </a>

                </td>

            </tr>
        `).join('');

        rows(tb, data);
    }

    f.onsubmit = async e => {

        e.preventDefault();

        let created = await api('/api/reports/generate', {
            method: 'POST',
            body: {
                period: f.period.value,
                groupId: +f.groupId.value,
                disciplineId: +f.disciplineId.value,
                teacherId: f.teacherId.value
                    ? +f.teacherId.value
                    : null,
                methodologistId: +ses().userId
            }
        });

        toast('Звіт сформовано');

        await draw();

        showReport(created.id);
    };

    draw();
}

