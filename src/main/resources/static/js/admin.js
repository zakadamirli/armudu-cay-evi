(() => {
  const $ = id => document.getElementById(id);
  const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const root = document.documentElement;
  const DICT = window.ARMUDU_I18N;

  let lang = 'az';
  try { const s = localStorage.getItem('armudu-lang'); if (s && DICT[s]) lang = s; } catch { /* keçildi */ }
  function t(key, params) {
    let s = (DICT[lang] && DICT[lang][key]) ?? (DICT.az[key] ?? key);
    if (params) for (const k in params) s = s.replaceAll('{' + k + '}', params[k]);
    return s;
  }
  function applyStatic() {
    root.lang = lang;
    document.querySelectorAll('[data-i18n]').forEach(el => { el.textContent = t(el.dataset.i18n); });
    document.querySelectorAll('.lang button').forEach(b => b.setAttribute('aria-pressed', String(b.dataset.lang === lang)));
  }
  const fmtDate = iso => { const d = new Date(iso + 'T00:00'); return `${d.getDate()} ${DICT[lang].months[d.getMonth()]}`; };

  const loginForm = $('loginForm'), panel = $('panel'), rows = $('rows'), loginErr = $('loginErr');
  let auth = null; // "Basic ..." yalnız yaddaşda saxlanılır

  async function call(path, options = {}) {
    const res = await fetch(path, { ...options, headers: { ...(options.headers || {}), Authorization: auth, 'Accept-Language': lang } });
    if (res.status === 401 || res.status === 403) throw Object.assign(new Error(t('adm.login.bad')), { auth: true });
    if (!res.ok && res.status !== 204) throw new Error(t('adm.err.server') + ' (' + res.status + ')');
    return res.status === 204 ? null : res.json();
  }

  async function load() {
    rows.innerHTML = `<tr><td colspan="8" class="empty">${esc(t('adm.loading'))}</td></tr>`;
    try {
      const list = await call('/api/reservations');
      $('count').textContent = list.length ? t('adm.count', { n: list.length }) : '';
      if (!list.length) { rows.innerHTML = `<tr><td colspan="8" class="empty">${esc(t('adm.empty'))}</td></tr>`; return; }
      rows.innerHTML = list.map(r =>
        `<tr data-id="${r.id}"><td>#${r.id}</td><td>${esc(fmtDate(r.date))}</td><td>${esc(r.time)}</td><td>${esc(r.name)}</td><td>${r.guests}</td><td>${esc(r.spotLabel)}</td><td class="note">${esc(r.note || '—')}</td><td><button class="cancel" type="button" data-id="${r.id}">${esc(t('adm.cancel'))}</button></td></tr>`).join('');
    } catch (e) {
      if (e.auth) return showLogin(t('adm.login.expired'));
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
    btn.disabled = true; btn.textContent = t('adm.login.checking');
    try {
      await call('/api/reservations');
      showPanel();
    } catch (err) {
      showLogin(err.auth ? t('adm.login.bad') : err.message);
    } finally {
      btn.disabled = false; btn.textContent = t('adm.login.btn');
    }
  });

  $('logout').addEventListener('click', () => showLogin(t('adm.login.out')));
  $('refresh').addEventListener('click', load);

  rows.addEventListener('click', async e => {
    const btn = e.target.closest('.cancel');
    if (!btn) return;
    const id = btn.dataset.id;
    btn.disabled = true; btn.textContent = t('adm.cancelling');
    try {
      await call('/api/reservations/' + id, { method: 'DELETE' });
      const tr = rows.querySelector(`tr[data-id="${id}"]`);
      if (tr) tr.remove();
      if (!rows.children.length) rows.innerHTML = `<tr><td colspan="8" class="empty">${esc(t('adm.empty'))}</td></tr>`;
    } catch (err) {
      btn.disabled = false; btn.textContent = t('adm.cancel');
      if (err.auth) showLogin(t('adm.login.expired'));
    }
  });

  document.querySelectorAll('.lang button').forEach(b => b.addEventListener('click', () => {
    lang = DICT[b.dataset.lang] ? b.dataset.lang : 'az';
    try { localStorage.setItem('armudu-lang', lang); } catch { /* keçildi */ }
    applyStatic();
    if (auth) load(); // açıq sessiyada siyahını yeni dildə yenilə
  }));

  applyStatic();
})();
