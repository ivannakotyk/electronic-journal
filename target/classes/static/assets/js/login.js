async function doLogin(l, p) {
    try {
        let d = await api('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: {
                login: l,
                password: p
            }
        });

        localStorage.token = d.token;
        localStorage.userId = d.userId;
        localStorage.fullName = d.fullName;
        localStorage.role = d.role;

        location.href = HOME[d.role] || '/';
    } catch (e) {
        toast(e.message, 'error');
    }
}

qs('#loginForm').onsubmit = e => {
    e.preventDefault();
    doLogin(e.target.login.value.trim(), e.target.password.value);
};

qsa('[data-demo]').forEach(b => b.onclick = () => {
    let [l, p] = b.dataset.demo.split(':');
    qs('[name=login]').value = l;
    qs('[name=password]').value = p;
    doLogin(l, p);
});