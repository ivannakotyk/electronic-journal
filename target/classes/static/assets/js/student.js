async function studentCtx() {
    let users = await api('/api/users').catch(() => []);
    let me = users.find(u => String(u.id) === String(ses().userId)) || byRole(users, 'STUDENT')[0];

    return {
        users,
        me,
        id: me?.id || ses().userId
    };
}

async function studentGrades() {
    let c = await studentCtx();

    let g = await api('/api/grades/student/' + c.id);

    let a = await api('/api/grades/student/' + c.id + '/average')
        .catch(() => ({ averageScore: 0 }));

    let currentSummary = await api('/api/grades/student/' + c.id + '/current-summary')
        .catch(() => []);

    return {
        c,
        g,
        avg: a.averageScore || 0,
        currentSummary
    };
}

async function loadStudentDash() {
    let d = await studentGrades();

    qs('#avg').textContent = d.avg.toFixed(1);

    qs('#cur').textContent = d.g
        .filter(x => x.controlType === 'CURRENT')
        .length;

    qs('#sem').textContent = d.g
        .filter(x => x.controlType === 'SEMESTER')
        .length;
}

async function loadStudentGrades() {
    let d = await studentGrades();

    qs('#avg').textContent = d.avg.toFixed(1);

    const currentSummaryBox = qs('#currentSummary');
    const tb = qs('#grades');

    function drawCurrentSummary() {
        if (!currentSummaryBox) return;

        if (!d.currentSummary.length) {
            currentSummaryBox.innerHTML = `
                <div class="empty">Поточних балів поки немає</div>
            `;
            return;
        }

        currentSummaryBox.innerHTML = d.currentSummary.map(item => `
            <div class="card stat">
                <div>
                    <div class="muted">${item.disciplineName}</div>
                    <div class="num">${Number(item.totalScore).toFixed(1)}</div>
                    <small>Кількість оцінок: ${item.gradesCount}</small>
                </div>
                <span>🧾</span>
            </div>
        `).join('');
    }

    function draw() {
        let f = qs('#filter').value;

        let arr = d.g.filter(x => f === 'ALL' || x.controlType === f);

        tb.innerHTML = arr.map(x => `
            <tr>
                <td>${fmt(x.gradeDate)}</td>
                <td>${x.disciplineName}</td>
                <td>${x.controlType === 'CURRENT' ? 'Поточний' : 'Семестровий'}</td>
                <td><b>${x.value}</b></td>
                <td>${x.teacherName}</td>
                <td>${x.comment || '—'}</td>
            </tr>
        `).join('');

        rows(tb, arr);
    }

    qs('#filter').onchange = draw;

    drawCurrentSummary();
    draw();
}

async function loadStudentSchedule() {
    const ctx = await studentCtx();
    const groupId = ctx.me?.groupId;

    if (!groupId) {
        toast('Не знайдено групу користувача', 'error');
        return;
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
        const container = qs('#schedule');

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

        const title = qs('#stype');

        if (title) {
            title.textContent =
                currentMode === 'classes'
                    ? `Розклад занять (${currentWeek} тиждень)`
                    : 'Розклад сесії';
        }

        try {
            const url =
                currentMode === 'classes'
                    ? `/api/schedule/weekly?groupId=${groupId}`
                    : `/api/schedule/session?groupId=${groupId}`;

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
                container.innerHTML = `<div class="empty">📭 Розклад порожній</div>`;
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
                                    <span>👤 ${s.teacherName || '—'}</span>
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

    draw();
}