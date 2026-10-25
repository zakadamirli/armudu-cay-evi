(() => {
  const reduce = matchMedia('(prefers-reduced-motion: reduce)').matches;
  const $ = id => document.getElementById(id);
  const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const root = document.documentElement;
  const DICT = window.ARMUDU_I18N;

  /* ---------- dil ---------- */
  // Prioritet: URL (?lang=) → localStorage → az. Dəyişəndə URL-ə də yazılır (paylaşıla bilən link).
  function langFromUrl() {
    try { const u = new URLSearchParams(location.search).get('lang'); if (u && DICT[u]) return u; } catch { /* keçildi */ }
    return null;
  }
  function syncUrl() {
    try { const url = new URL(location.href); url.searchParams.set('lang', lang); history.replaceState(null, '', url); } catch { /* keçildi */ }
  }
  let lang = 'az';
  const urlLang = langFromUrl();
  if (urlLang) lang = urlLang;
  else { try { const s = localStorage.getItem('armudu-lang'); if (s && DICT[s]) lang = s; } catch { /* keçildi */ } }
  const notePh = {
    az: 'Ad günü, nərd taxtası, xüsusi mürəbbə...',
    en: 'Birthday, a backgammon board, a special jam...',
    ru: 'День рождения, доска для нард, особое варенье...'
  };
  function t(key, params) {
    let s = (DICT[lang] && DICT[lang][key]) ?? (DICT.az[key] ?? key);
    if (params) for (const k in params) s = s.replaceAll('{' + k + '}', params[k]);
    return s;
  }

  function applyStatic() {
    root.lang = lang;
    document.querySelectorAll('[data-i18n]').forEach(el => { el.textContent = t(el.dataset.i18n); });
    document.querySelectorAll('[data-i18n-html]').forEach(el => { el.innerHTML = t(el.dataset.i18nHtml); });
    document.querySelectorAll('[data-i18n-aria]').forEach(el => { el.setAttribute('aria-label', t(el.dataset.i18nAria)); });
    const note = $('rNote'); if (note) note.placeholder = notePh[lang] || notePh.az;
    document.querySelectorAll('.lang button').forEach(b => b.setAttribute('aria-pressed', String(b.dataset.lang === lang)));
  }

  async function api(path, options = {}) {
    const headers = { 'Content-Type': 'application/json', 'Accept-Language': lang, ...(options.headers || {}) };
    const res = await fetch(path, { ...options, headers });
    const data = await res.json().catch(() => null);
    if (!res.ok) throw Object.assign(new Error(data?.message || t('menu.loadError')), { data, status: res.status });
    return data;
  }

  /* ---------- tema ---------- */
  const toggle = $('themeToggle');
  function applyTheme(mode) {
    if (mode === 'dark' || mode === 'light') root.dataset.theme = mode;
    else delete root.dataset.theme;
    const dark = root.dataset.theme === 'dark' || (!root.dataset.theme && matchMedia('(prefers-color-scheme: dark)').matches);
    toggle.setAttribute('aria-pressed', String(dark));
  }
  let savedTheme = null;
  try { savedTheme = localStorage.getItem('armudu-theme'); } catch { /* keçildi */ }
  applyTheme(savedTheme);
  toggle.addEventListener('click', () => {
    const dark = root.dataset.theme === 'dark' || (!root.dataset.theme && matchMedia('(prefers-color-scheme: dark)').matches);
    const next = dark ? 'light' : 'dark';
    applyTheme(next);
    try { localStorage.setItem('armudu-theme', next); } catch { /* keçildi */ }
  });

  /* ---------- scroll-spy ---------- */
  const navLinks = [...document.querySelectorAll('nav a')];
  const byId = Object.fromEntries(navLinks.map(a => [a.getAttribute('href').slice(1), a]));
  const spy = new IntersectionObserver(entries => {
    entries.forEach(e => {
      const link = byId[e.target.id];
      if (link && e.isIntersecting) {
        navLinks.forEach(a => a.removeAttribute('aria-current'));
        link.setAttribute('aria-current', 'true');
      }
    });
  }, { rootMargin: '-45% 0px -50% 0px' });
  Object.keys(byId).forEach(id => { const el = $(id); if (el) spy.observe(el); });

  /* ---------- bu gün açıqdır? ---------- */
  function hours() {
    const el = $('todayHours');
    if (!el) return;
    const h = new Date().getHours();
    el.textContent = (h >= 10 || h < 0) ? t('hours.open') : t('hours.closed');
  }

  /* ---------- buxar ---------- */
  const cv = $('steam');
  const glass = document.querySelector('.glass-stage svg');
  const ctx = cv.getContext('2d');
  let W = 0, H = 0, rimX = 0, rimY = 0, rimW = 0, parts = [];
  function size() {
    const r = cv.getBoundingClientRect(), g = glass.getBoundingClientRect(), d = devicePixelRatio || 1;
    W = r.width; H = r.height; cv.width = W * d; cv.height = H * d; ctx.setTransform(d, 0, 0, d, 0, 0);
    rimX = g.left - r.left + g.width / 2;
    rimY = g.top - r.top + g.height * 30 / 260;
    rimW = g.width * 124 / 200;
  }
  size(); addEventListener('resize', size);
  const steamColor = () => getComputedStyle(root).getPropertyValue('--steam').trim();
  function spawn() {
    parts.push({ x: rimX + (Math.random() - .5) * rimW * .7, y: rimY - 4, r: 3 + Math.random() * 5, life: 0, max: 140 + Math.random() * 80, ph: Math.random() * 6.28, sp: .3 + Math.random() * .3 });
  }
  function stepP(p) { p.life++; p.y -= p.sp; p.x += Math.sin(p.ph + p.life / 28) * .45; p.r += .09; }
  function tick() {
    ctx.clearRect(0, 0, W, H);
    if (parts.length < 30 && Math.random() < .3) spawn();
    const c = steamColor();
    parts = parts.filter(p => p.life < p.max);
    for (const p of parts) {
      stepP(p);
      ctx.globalAlpha = .55 * Math.sin(Math.PI * p.life / p.max); ctx.fillStyle = c;
      ctx.beginPath(); ctx.arc(p.x, p.y, p.r, 0, 6.283); ctx.fill();
    }
    ctx.globalAlpha = 1;
    requestAnimationFrame(tick);
  }
  if (!reduce) {
    for (let i = 0; i < 160; i++) { if (parts.length < 30 && Math.random() < .3) spawn(); parts.forEach(stepP); parts = parts.filter(p => p.life < p.max); }
    tick();
  }

  /* ---------- menyu ---------- */
  const menuEl = $('menu');
  let activeCat = '';
  const price = v => Number(v).toFixed(2).replace('.', ',') + ' ₼';
  async function loadMenu() {
    try {
      const items = await api('/api/menu' + (activeCat ? '?category=' + activeCat : ''));
      menuEl.innerHTML = items.map(m => {
        const brew = Number.isInteger(m.brewMinutes)
          ? `<button class="brew-chip" type="button" data-brew="${m.brewMinutes}" data-tea="${esc(m.name)}">${esc(t('brew.chip', { n: m.brewMinutes }))}</button>`
          : '';
        return `<div class="item"><h3>${esc(m.name)}${m.tag ? `<span class="tag">${esc(m.tag)}</span>` : ''}</h3><span class="price">${price(m.price)}</span><p>${esc(m.description)}</p>${brew}</div>`;
      }).join('');
    } catch (e) {
      menuEl.innerHTML = `<p class="muted">${esc(t('menu.loadError'))}</p>`;
    }
  }
  menuEl.addEventListener('click', e => {
    const chip = e.target.closest('.brew-chip');
    if (chip) setBrew(parseInt(chip.dataset.brew, 10), chip.dataset.tea);
  });
  document.querySelectorAll('.chip').forEach(b => b.addEventListener('click', () => {
    document.querySelectorAll('.chip').forEach(x => x.setAttribute('aria-pressed', String(x === b)));
    activeCat = b.dataset.f;
    loadMenu();
  }));

  /* ---------- dəmləmə saatı ---------- */
  const ring = $('ring'), prog = $('prog'), clock = $('clock'), phase = $('phase');
  const steps = [...document.querySelectorAll('#steps li')];
  const startBtn = $('startBtn'), lenSel = $('brewLen');
  const C = 2 * Math.PI * 52;
  prog.style.strokeDasharray = C;
  let total = 300, left = 300, timer = null;
  const mmss = s => `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`;
  function startLabel() {
    if (timer) return t('brew.stop');
    if (left === 0) return t('brew.again');
    if (left === total) return t('brew.start');
    return t('brew.continue');
  }
  function draw() {
    const frac = 1 - left / total;
    prog.style.strokeDashoffset = C * (1 - frac);
    clock.textContent = mmss(left);
    let cur = -1;
    steps.forEach((li, i) => { if (frac >= +li.dataset.at) cur = i; });
    steps.forEach((li, i) => { li.classList.toggle('on', i === cur && (timer || left === 0)); li.classList.toggle('done', i < cur); });
    phase.textContent = left === 0 ? t('phase.done') : timer ? t('phase.brewing') : left === total ? t('phase.ready') : t('phase.rest');
    ring.classList.toggle('done', left === 0);
    startBtn.textContent = startLabel();
  }
  function stop() { clearInterval(timer); timer = null; }
  function finish() {
    left = 0; stop();
    ring.classList.add('pulse');
    setTimeout(() => ring.classList.remove('pulse'), 800);
  }
  startBtn.addEventListener('click', () => {
    if (timer) { stop(); draw(); return; }
    if (left === 0) left = total;
    timer = setInterval(() => { left--; if (left <= 0) finish(); draw(); }, 1000);
    draw();
  });
  $('resetBtn').addEventListener('click', () => { stop(); left = total; draw(); });
  lenSel.addEventListener('change', () => { stop(); total = left = +lenSel.value; draw(); });

  function setBrew(minutes, teaName) {
    if (!Number.isInteger(minutes) || minutes <= 0) return;
    stop();
    total = left = minutes * 60;
    const val = String(total);
    let opt = [...lenSel.options].find(o => o.value === val);
    if (!opt) { opt = new Option('', val); lenSel.add(opt); }
    opt.textContent = t('brew.lenCustom', { n: minutes, name: teaName });
    opt.dataset.custom = '1';
    lenSel.value = val;
    ring.classList.remove('done');
    draw();
    $('demle').scrollIntoView({ behavior: reduce ? 'auto' : 'smooth', block: 'start' });
    startBtn.focus({ preventScroll: true });
  }

  /* ---------- mürəbbə ---------- */
  const jamsEl = $('jams'), pairEl = $('pairing');
  let jams = [], pickedJam = 0;
  function renderPairing() {
    if (!jams.length) return;
    pairEl.innerHTML = `<strong>${esc(t('jam.pairing', { name: jams[pickedJam].name }))}</strong> ${esc(jams[pickedJam].pairing)}`;
  }
  function pick(i) {
    pickedJam = i;
    jamsEl.querySelectorAll('.jam').forEach(b => b.setAttribute('aria-pressed', String(+b.dataset.i === i)));
    renderPairing();
  }
  jamsEl.addEventListener('click', e => { const b = e.target.closest('.jam'); if (b) pick(+b.dataset.i); });
  async function loadJams() {
    try {
      jams = await api('/api/jams');
      $('jamCount').textContent = jams.length;
      jamsEl.innerHTML = jams.map((j, i) =>
        `<button class="jam" data-i="${i}" aria-pressed="${i === pickedJam}"><span class="jar"></span><b>${esc(j.name)}</b><small>${esc(j.description)}</small></button>`).join('');
      jams.forEach((j, i) => {
        const jar = jamsEl.querySelector(`.jam[data-i="${i}"] .jar`);
        if (jar) jar.style.background = /^#[0-9a-f]{6}$/i.test(j.color) ? j.color : '#999';
      });
      if (pickedJam >= jams.length) pickedJam = 0;
      renderPairing();
    } catch (e) {
      pairEl.textContent = t('jam.loadError');
    }
  }

  /* ---------- rezerv ---------- */
  const form = $('resForm'), conf = $('confirm'), tSel = $('rTime'), gSel = $('rGuests'), dIn = $('rDate'), spotSel = $('rSpot'), submit = $('resSubmit');
  const localIso = d => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;

  function fillGuests() {
    const cur = gSel.value || '2';
    gSel.innerHTML = '';
    for (let g = 1; g <= 12; g++) gSel.add(new Option(t('guests.unit', { n: g }), g));
    gSel.value = cur;
  }
  const today = new Date(), tomorrow = new Date(today), maxDay = new Date(today);
  tomorrow.setDate(today.getDate() + 1); maxDay.setDate(today.getDate() + 60);
  dIn.min = localIso(today); dIn.max = localIso(maxDay); dIn.value = localIso(tomorrow);

  async function loadSpots() {
    const cur = spotSel.value;
    try {
      const list = await api('/api/reservations/spots');
      spotSel.innerHTML = '';
      list.forEach(s => spotSel.add(new Option(s.label, s.value)));
      if (cur) spotSel.value = cur;
    } catch { /* keçildi */ }
  }

  async function loadSlots() {
    if (!dIn.value) return;
    const keep = tSel.value || '19:00';
    tSel.innerHTML = `<option value="">${t('slot.loading')}</option>`;
    try {
      const slots = await api('/api/reservations/slots?date=' + dIn.value);
      tSel.innerHTML = '';
      for (const s of slots) {
        const label = s.remaining > 0 ? t('slot.seats', { t: s.time, n: s.remaining }) : t('slot.full', { t: s.time });
        const o = new Option(label, s.time);
        o.disabled = s.remaining <= 0;
        tSel.add(o);
      }
      const want = slots.find(s => s.time === keep && s.remaining > 0) || slots.find(s => s.remaining > 0);
      if (want) tSel.value = want.time;
      else { tSel.innerHTML = `<option value="">${t('slot.none')}</option>`; tSel.value = ''; }
    } catch {
      tSel.innerHTML = `<option value="">${t('slot.error')}</option>`;
    }
  }
  dIn.addEventListener('change', loadSlots);

  function showErrors(fields = {}) {
    form.querySelectorAll('.err').forEach(el => {
      const msg = fields[el.dataset.for] || '';
      el.textContent = msg;
      const input = form.elements[el.dataset.for];
      if (input) input.setAttribute('aria-invalid', msg ? 'true' : 'false');
    });
  }

  form.addEventListener('submit', async e => {
    e.preventDefault();
    showErrors();
    conf.hidden = true;
    const payload = {
      name: form.name.value.trim(), phone: form.phone.value.trim(),
      date: dIn.value || null, time: tSel.value || null,
      guests: +gSel.value, spot: spotSel.value || null, note: form.note.value.trim()
    };
    submit.disabled = true; submit.textContent = t('res.submitting');
    try {
      const r = await api('/api/reservations', { method: 'POST', body: JSON.stringify(payload) });
      const d = new Date(r.date + 'T00:00');
      const date = `${d.getDate()} ${DICT[lang].months[d.getMonth()]}`;
      conf.className = 'confirm';
      conf.innerHTML = `<b>${esc(t('confirm.thanks', { name: r.name.split(' ')[0] }))}</b><p>${esc(t('confirm.body', { id: r.id, date, time: r.time, guests: t('guests.unit', { n: r.guests }), spot: r.spotLabel }))}</p>`;
      conf.hidden = false;
      form.note.value = '';
      loadSlots();
    } catch (err) {
      showErrors(err.data?.fields);
      conf.className = 'confirm bad';
      conf.innerHTML = `<b>${esc(t('confirm.badTitle'))}</b><p>${esc(err.message)}</p>`;
      conf.hidden = false;
      if (err.status === 409) loadSlots();
    } finally {
      submit.disabled = false; submit.textContent = t('res.submit');
    }
  });

  /* ---------- dil dəyişimi ---------- */
  function setLang(next, reload) {
    lang = DICT[next] ? next : 'az';
    try { localStorage.setItem('armudu-lang', lang); } catch { /* keçildi */ }
    syncUrl();
    applyStatic();
    fillGuests();
    hours();
    draw();
    if (reload) { loadMenu(); loadJams(); loadSpots(); loadSlots(); }
    else renderPairing();
  }
  document.querySelectorAll('.lang button').forEach(b =>
    b.addEventListener('click', () => setLang(b.dataset.lang, true)));

  /* ---------- başlanğıc ---------- */
  try { localStorage.setItem('armudu-lang', lang); } catch { /* keçildi */ }
  syncUrl();
  applyStatic();
  fillGuests();
  hours();
  draw();
  loadMenu();
  loadJams();
  loadSpots();
  loadSlots();
})();
