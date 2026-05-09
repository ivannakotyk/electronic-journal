const ROLE_UA = {
    STUDENT: 'Студент',
    TEACHER: 'Викладач',
    ADMINISTRATOR: 'Адміністратор',
    METHODOLOGIST: 'Методист'
};

const HOME = {
    STUDENT: '/pages/student/dashboard.html',
    TEACHER: '/pages/teacher/dashboard.html',
    ADMINISTRATOR: '/pages/admin/dashboard.html',
    METHODOLOGIST: '/pages/methodologist/dashboard.html'
};

function qs(s, r = document) {
    return r.querySelector(s);
}

function qsa(s, r = document) {
    return [...r.querySelectorAll(s)];
}

function ses() {
    return {
        token: localStorage.token,
        role: localStorage.role,
        fullName: localStorage.fullName,
        userId: localStorage.userId
    };
}

function headers() {
    return {
        'Content-Type': 'application/json',
        ...(ses().token ? { 'Authorization': 'Bearer ' + ses().token } : {})
    };
}

async function api(url, opt = {}) {
    opt.headers = opt.headers || headers();
    if (opt.body && typeof opt.body !== 'string') {
        opt.body = JSON.stringify(opt.body);
    }

    let r = await fetch(url, opt);

    if (r.status === 401 || r.status === 403) {
        throw new Error('Немає доступу для цієї ролі або потрібно увійти.');
    }

    let ct = r.headers.get('content-type') || '';

    if (!r.ok) {
        let t = ct.includes('json') ? JSON.stringify(await r.json()) : await r.text();
        throw new Error(t || 'Помилка запиту');
    }

    return ct.includes('json') ? r.json() : r.text();
}

function roleName(r) {
    return ROLE_UA[r] || r || 'Користувач';
}

function requireAuth(roles) {
    let s = ses();
    if (!s.token) {
        location.href = '/login.html';
        return false;
    }
    if (roles && !roles.includes(s.role)) {
        location.href = HOME[s.role] || '/login.html';
        return false;
    }
    return true;
}

function logout() {
    localStorage.clear();
    location.href = '/login.html';
}

function init(active) {
    qsa('[data-name]').forEach(e => e.textContent = ses().fullName || 'Користувач');
    qsa('[data-role]').forEach(e => e.textContent = roleName(ses().role));
    qsa('[data-logout]').forEach(e => e.onclick = logout);
    qsa('.nav a').forEach(a => {
        if (a.dataset.active === active) a.classList.add('active');
    });
}

function toast(m, t = 'success') {
    let e = qs('#toast');
    if (!e) {
        e = document.createElement('div');
        e.id = 'toast';
        e.className = 'toast';
        document.body.appendChild(e);
    }
    e.className = 'toast ' + t;
    e.textContent = m;
    e.style.display = 'block';
    setTimeout(() => e.style.display = 'none', 3500);
}

function fmt(x) {
    if (!x) return '—';
    try {
        return new Date(x).toLocaleDateString('uk-UA');
    } catch {
        return x;
    }
}

function rows(tbody, arr, msg = 'Даних поки немає') {
    if (!arr || !arr.length) {
        tbody.innerHTML = `<tr><td colspan="10" class="empty">${msg}</td></tr>`;
    }
}

function opt(items, fn) {
    return items.map(i => `<option value="${i.id}">${fn(i)}</option>`).join('');
}

function byRole(users, r) {
    return users.filter(u => u.role === r);
}

async function allGrades() {
    let users = await api('/api/users').catch(() => []),
        studs = byRole(users, 'STUDENT'),
        out = [];
    for (const s of studs) {
        try {
            out = out.concat(await api('/api/grades/student/' + s.id));
        } catch {}
    }
    return { users, studs, out };
}