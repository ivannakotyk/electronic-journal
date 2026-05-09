async function adminDash() {
    let [u, g, d] = await Promise.all([
        api('/api/users'),
        api('/api/groups'),
        api('/api/disciplines')
    ]);
    qs('#users').textContent = u.length;
    qs('#students').textContent = u.filter(x => x.role === 'STUDENT').length;
    qs('#groups').textContent = g.length;
    qs('#disc').textContent = d.length;
}
async function usersPage() {
    const f = qs('#userForm');
    const tb = qs('#userRows');
    const groups = await api('/api/groups');
    if (f.groupId) {
        f.groupId.innerHTML = '<option value="">Не застосовується</option>' + opt(groups, g => g.code);
    }

    async function draw() {
        try {
            const u = await api('/api/users');
            tb.innerHTML = u.map(x => `
                <tr>
                    <td>${x.id}</td>
                    <td>${x.fullName}</td>
                    <td>${x.login}</td>
                    <td>${x.email}</td>
                    <td><span class="badge">${roleName(x.role)}</span></td>
                    <td>${x.groupCode || x.position || '—'}</td>
                    <td><button class="danger" data-del="${x.id}">Видалити</button></td>
                </tr>
            `).join('');

            rows(tb, u);

            qsa('[data-del]').forEach(b => {
                b.onclick = async () => {
                    if (confirm('Видалити користувача?')) {
                        try {
                            await api('/api/users/' + b.dataset.del, { method: 'DELETE' });
                            toast('Користувача видалено');
                            draw();
                        } catch (err) {
                            toast(err.message, 'error');
                        }
                    }
                };
            });
        } catch (e) {
            toast('Помилка завантаження списку', 'error');
        }
    }

    f.onsubmit = async e => {
        e.preventDefault();
        const payload = {
            fullName: f.fullName.value,
            login: f.login.value,
            password: f.password.value,
            email: f.email.value,
            role: f.role.value,
            position: f.position.value || null,
            studentCardNumber: f.studentCardNumber.value || null,
            groupId: f.groupId.value ? +f.groupId.value : null
        };

        try {
            await api('/api/users', { method: 'POST', body: payload });
            toast('Користувача створено');
            f.reset();
            draw();
        } catch (err) {
            toast(err.message, 'error');
        }
    };

    draw();
}


async function groupsPage() {
    const f = qs('#groupForm');
    async function draw() {
        let g = await api('/api/groups');
        let tb = qs('#groupRows');
        tb.innerHTML = g.map(x => `
            <tr>
                <td>${x.id}</td>
                <td><b>${x.code}</b></td>
                <td>${x.course}</td>
                <td>${x.specialty}</td>
            </tr>
        `).join('');
        rows(tb, g);
    }

    f.onsubmit = async e => {
        e.preventDefault();
        await api('/api/groups', {
            method: 'POST',
            body: { code: f.code.value, course: +f.course.value, specialty: f.specialty.value }
        });
        f.reset();
        draw();
    };
    draw();
}


async function disciplinesPage() {
    const f = qs('#discForm');
    async function draw() {
        let d = await api('/api/disciplines');
        let tb = qs('#discRows');
        tb.innerHTML = d.map(x => `
            <tr>
                <td>${x.id}</td>
                <td><b>${x.name}</b></td>
                <td>${x.semester}</td>
            </tr>
        `).join('');
        rows(tb, d);
    }

    f.onsubmit = async e => {
        e.preventDefault();
        await api('/api/disciplines', {
            method: 'POST',
            body: { name: f.name.value, semester: +f.semester.value }
        });
        f.reset();
        draw();
    };
    draw();
}