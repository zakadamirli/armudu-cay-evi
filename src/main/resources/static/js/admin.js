(() => {
  const $ = id => document.getElementById(id);
  const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const months = ['yanvar', 'fevral', 'mart', 'aprel', 'may', 'iyun', 'iyul', 'avqust', 'sentyabr', 'oktyabr', 'noyabr', 'dekabr'];
  const fmtDate = iso => { const d = new Date(iso + 'T00:00'); return `${d.getDate()} ${months[d.getMonth()]}`; };

  const loginForm = $('loginForm'), panel = $('panel'), rows = $('rows'), loginErr = $('loginErr');
  let auth = null; // "Basic ..." yalnız yaddaşda saxlanılır

  async function call(path, options = {}) {
    const res = await fetch(path, { ...options, headers: { ...(options.headers || {}), Authorization: auth } });
    if (res.status === 401 || res.status === 403) throw Object.assign(new Error('Giriş rədd edildi'), { auth: true });
    if (!res.ok && res.status !== 204) throw new Error('Server xətası (' + res.status + ')');
    return res.status === 204 ? null : res.json();
  }

  async function load() {
    rows.innerHTML = '<tr><td colspan="8" class="empty">Yüklənir…</td></tr>';
    try {
      const list = await call('/api/reservations');
      $('count').textContent = list.length ? `${list.length} rezerv` : '';
      if (!list.length) { rows.innerHTML = '<tr><td colspan="8" class="empty">Hələ rezerv yoxdur.</td></tr>'; return; }
      rows.innerHTML = list.map(r =>
        `<tr data-id="${r.id}"><td>#${r.id}</td><td>${esc(fmtDate(r.date))}</td><td>${esc(r.time)}</td><td>${esc(r.name)}</td><td>${r.guests}</td><td>${esc(r.spotLabel)}</td><td class="note">${esc(r.note || '—')}</td><td><button class="cancel" type="button" data-id="${r.id}">Ləğv et</button></td></tr>`).join('');
    } catch (e) {
      if (e.auth) return showLogin('Sessiya bitdi, yenidən daxil olun.');
      rows.innerHTML = `<tr><td colspan="8" class="empty">${esc(e.message)}</td></tr>`;
    }
  }

  function showLogin(msg) {
    auth = null;
    panel.hidden = true;
    loginForm.hidden = false;
    $('logout').hidden = true;
    loginErr.textContent = msg || '';
  }
  function showPanel() {
    loginForm.hidden = true;
    panel.hidden = false;
    $('logout').hidden = false;
    load();
  }

  loginForm.addEventListener('submit', async e => {
    e.preventDefault();
    loginErr.textContent = '';
    const btn = $('loginBtn');
    auth = 'Basic ' + btoa($('user').value + ':' + $('pass').value);
    $('pass').value = '';
    btn.disabled = true; btn.textContent = 'Yoxlanılır…';
    try {
      await call('/api/reservations');
      showPanel();
    } catch (err) {
      showLogin(err.auth ? 'İstifadəçi adı və ya parol yanlışdır.' : err.message);
    } finally {
      btn.disabled = false; btn.textContent = 'Daxil ol';
    }
  });

  $('logout').addEventListener('click', () => showLogin('Çıxış edildi.'));
  $('refresh').addEventListener('click', load);

  rows.addEventListener('click', async e => {
    const btn = e.target.closest('.cancel');
    if (!btn) return;
    const id = btn.dataset.id;
    btn.disabled = true; btn.textContent = 'Ləğv olunur…';
    try {
      await call('/api/reservations/' + id, { method: 'DELETE' });
      const tr = rows.querySelector(`tr[data-id="${id}"]`);
      if (tr) tr.remove();
      if (!rows.children.length) rows.innerHTML = '<tr><td colspan="8" class="empty">Hələ rezerv yoxdur.</td></tr>';
    } catch (err) {
      btn.disabled = false; btn.textContent = 'Ləğv et';
      if (err.auth) showLogin('Sessiya bitdi, yenidən daxil olun.');
    }
  });
})();
