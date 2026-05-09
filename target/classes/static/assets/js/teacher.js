async function teacherId(users) {
    return (users.find(u => String(u.id) === String(ses().userId)) || byRole(users, 'TEACHER')[0])?.id;
}

async function drawGrades(tbid) {
    let { out } = await allGrades();
    let tb = qs(tbid);
    tb.innerHTML = out.map(g => `
        <tr>
            <td>${g.id}</td>
            <td>${g.studentName}</td>
            <td>${g.disciplineName}</td>
            <td>${g.controlType}</td>
            <td><b>${g.value}</b></td>
            <td>${fmt(g.gradeDate)}</td>
            <td>${g.comment || '—'}</td>
        </tr>
    `).join('');
    rows(tb, out);
    return out;
}

async function teacherDash() {
    let arr = await drawGrades('#recent');
    qs('#recent').innerHTML = arr.slice(-6).reverse().map(g => `
        <tr>
            <td>${g.studentName}</td>
            <td>${g.disciplineName}</td>
            <td><b>${g.value}</b></td>
            <td>${fmt(g.gradeDate)}</td>
            <td>${g.controlType}</td>
        </tr>
    `).join('');
    rows(qs('#recent'), arr.slice(-6));
}

async function gradebook() {
    let users = await api('/api/users'),
        disc = await api('/api/disciplines'),
        tid = await teacherId(users),
        f = qs('#gradeForm');

    f.studentId.innerHTML = opt(byRole(users, 'STUDENT'), u => `${u.fullName} (${u.groupCode || 'без групи'})`);
    f.disciplineId.innerHTML = opt(disc, d => d.name);
    f.gradeDate.value = new Date().toISOString().slice(0, 10);

    async function reload() {
        await drawGrades('#gradeRows');
    }

    f.onsubmit = async e => {
        e.preventDefault();
        let body = {
            studentId: +f.studentId.value,
            teacherId: +tid,
            disciplineId: +f.disciplineId.value,
            controlType: f.controlType.value,
            gradeDate: f.gradeDate.value,
            value: +f.value.value,
            comment: f.comment.value
        };
        await api('/api/grades', { method: 'POST', body });
        toast('Оцінку збережено');
        f.reset();
        f.gradeDate.value = new Date().toISOString().slice(0, 10);
        reload();
    };

    reload();
}
async function teacherSchedule() {

    let groups = await api('/api/groups').catch(() => []);

    if (!Array.isArray(groups) || !groups.length) {
        groups = [
            { id: 1, code: 'ІА-33' },
            { id: 2, code: 'ІС-33' }
        ];
    }

    const groupSelect = qs('#group');
    if (groupSelect) {
        groupSelect.innerHTML = opt(groups, g => g.code);
    }

    let currentMode = 'classes';
    let currentWeek = '1';

    const getPairNum = (time) => {
        const slots = {
            '08:30': 1,
            '10:25': 2,
            '12:20': 3,
            '14:15': 4,
            '16:10': 5
        };
        return slots[(time || '').slice(0, 5)] || '—';
    };

    async function draw() {

        const gid = groupSelect?.value;
        const container = qs('#schedule');

        if (!gid) {
            container.innerHTML = `<div class="empty">Оберіть групу</div>`;
            return;
        }

        qsa('[data-type]').forEach(btn =>
            btn.classList.toggle('active', btn.dataset.type === currentMode)
        );

        qsa('[data-week]').forEach(btn =>
            btn.classList.toggle('active', btn.dataset.week === currentWeek)
        );

        const weekToggle = qs('#weekToggle');
        if (weekToggle) {
            weekToggle.classList.toggle('hide', currentMode !== 'classes');
        }

        try {

            const url =
                currentMode === 'classes'
                    ? `/api/schedule/weekly?groupId=${gid}`
                    : `/api/schedule/session?groupId=${gid}`;

            const data = await api(url);

            let lessons = [];

            if (currentMode === 'classes') {
                const weekData = currentWeek === '2'
                    ? data.secondWeek
                    : data.firstWeek;

                lessons = (weekData || []).flatMap(day =>
                    (day.lessons || []).map(l => ({
                        ...l,
                        dayName: day.dayName
                    }))
                );
            } else {
                lessons = (Array.isArray(data) ? data : []).map(l => ({
                    ...l,
                    dayName: fmt(l.date)
                }));
            }

            if (!lessons.length) {
                container.innerHTML = `<div class="empty">📭 Розклад відсутній</div>`;
                return;
            }

            const grouped = {};

            lessons.forEach(l => {
                if (!grouped[l.dayName]) grouped[l.dayName] = [];
                grouped[l.dayName].push(l);
            });

            container.innerHTML = Object.entries(grouped).map(([day, items]) => `
                <div class="day-block">

                    <div class="day-title">${day.toUpperCase()}</div>

                    ${items.map(s => `
                        <div class="lesson-card">

                            <div class="lesson-time-cell">
                                <div class="pair-number">${getPairNum(s.time)}</div>
                                <div class="pair-time">${(s.time || '').slice(0, 5)}</div>
                            </div>

                            <div class="lesson-info-cell">

                                <div class="discipline-name">
                                    ${s.disciplineName || '—'}
                                </div>

                                <div class="lesson-meta">
                                    <span>👥 ${s.groupCode || '—'}</span>

                                    <span class="separator">•</span>

                                    <span>
                                        📍 <span class="room-pill">
                                            ${s.room || '—'}
                                        </span>
                                    </span>
                                </div>

                            </div>

                        </div>
                    `).join('')}

                </div>
            `).join('');

        } catch (err) {
            console.error(err);
            container.innerHTML = `<div class="empty">❌ Помилка завантаження</div>`;
            toast('Помилка завантаження розкладу', 'error');
        }
    }

    qsa('[data-type]').forEach(btn =>
        btn.onclick = () => {
            currentMode = btn.dataset.type;
            draw();
        }
    );

    qsa('[data-week]').forEach(btn =>
        btn.onclick = () => {
            currentWeek = btn.dataset.week;
            draw();
        }
    );

    if (groupSelect) groupSelect.onchange = draw;

    draw();
}
function reportPreviewHtml(r) {
    return `<section class="section card">
        <div class="toolbar">
            <div>
                <h2>Перегляд звіту №${r.id}</h2>
                <p class="muted">${r.period} · ${r.groupCode} · ${r.disciplineName}</p>
            </div>
            <div>
                <a class="btn2" href="/api/reports/${r.id}/pdf">PDF</a> 
                <a class="btn2" href="/api/reports/${r.id}/excel">Excel</a>
            </div>
        </div>
        <div class="grid cols3">
            <div class="stat">
                <div>
                    <div class="muted">Середній бал</div>
                    <div class="num">${(r.averageScore || 0).toFixed(2)}</div>
                </div>
                <span>📊</span>
            </div>
            <div class="stat">
                <div>
                    <div class="muted">З оцінкою</div>
                    <div class="num">${r.studentsWithSemesterScore}</div>
                </div>
                <span>✅</span>
            </div>
            <div class="stat">
                <div>
                    <div class="muted">Без оцінки</div>
                    <div class="num">${r.studentsWithoutSemesterScore}</div>
                </div>
                <span>⚠️</span>
            </div>
        </div>
        <h3>Розподіл результатів</h3>
        <div>
            ${r.distribution.map(x => `
                <div style="display:grid;grid-template-columns:90px 1fr 70px;gap:10px;align-items:center;margin:8px 0">
                    <b>${x.label}</b>
                    <div style="height:14px;border-radius:999px;background:linear-gradient(90deg,#2457c5 ${x.percent}%,#e8edf8 ${x.percent}%);"></div>
                    <span>${x.count} (${x.percent.toFixed(1)}%)</span>
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
                            <td><b>${x.semesterScore == null ? '—' : Number(x.semesterScore).toFixed(2)}</b></td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>
    </section>`;
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
    box.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

async function reports() {
    let groups = await api('/api/groups'),
        disc = await api('/api/disciplines'),
        users = await api('/api/users').catch(() => []),
        tid = await teacherId(users),
        f = qs('#reportForm');

    f.groupId.innerHTML = opt(groups, g => g.code);
    f.disciplineId.innerHTML = opt(disc, d => d.name);

    async function draw() {
        let data = await api('/api/reports');
        let tb = qs('#reports');
        tb.innerHTML = data.map(r => `
            <tr>
                <td>${r.id}</td>
                <td>${r.period}</td>
                <td>${r.groupCode}</td>
                <td>${r.disciplineName || 'Усі'}</td>
                <td>
                    <button class="btn2" onclick="showReport(${r.id})">Переглянути</button>
                    <a class="btn2" href="/api/reports/${r.id}/pdf">PDF</a>
                    <a class="btn2" href="/api/reports/${r.id}/excel">Excel</a>
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
                teacherId: +tid
            }
        });
        toast('Звіт сформовано');
        await draw();
        showReport(created.id);
    };

    draw();
}