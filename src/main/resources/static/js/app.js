(() => {
  const reduce = matchMedia('(prefers-reduced-motion: reduce)').matches;
  const $ = id => document.getElementById(id);
  const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

  async function api(path, options) {
    const res = await fetch(path, { headers: { 'Content-Type': 'application/json' }, ...options });
    const data = await res.json().catch(() => null);
    if (!res.ok) throw Object.assign(new Error(data?.message || 'Server cavab vermədi, bir az sonra yenidən yoxlayın'), { data, status: res.status });
    return data;
  }

  /* ---------- buxar ---------- */
  const cv = $('steam');
  const ctx = cv.getContext('2d');
  let W = 0, H = 0, parts = [];
  function size() {
    const r = cv.getBoundingClientRect(), d = devicePixelRatio || 1;
    W = r.width; H = r.height; cv.width = W * d; cv.height = H * d; ctx.setTransform(d, 0, 0, d, 0, 0);
  }
  size(); addEventListener('resize', size);
  const steamColor = () => getComputedStyle(document.documentElement).getPropertyValue('--steam').trim();
  function spawn() {
    parts.push({ x: W / 2 + (Math.random() - .5) * W * .3, y: H * .22, r: 6 + Math.random() * 10, life: 0, max: 160 + Math.random() * 80, ph: Math.random() * 6.28, sp: .35 + Math.random() * .35 });
  }
  function step(p) { p.life++; p.y -= p.sp; p.x += Math.sin(p.ph + p.life / 28) * .45; p.r += .07; }
  function tick() {
    ctx.clearRect(0, 0, W, H);
    if (parts.length < 40 && Math.random() < .35) spawn();
    const c = steamColor();
    parts = parts.filter(p => p.life < p.max);
    for (const p of parts) {
      step(p);
      ctx.globalAlpha = Math.sin(Math.PI * p.life / p.max); ctx.fillStyle = c;
      ctx.beginPath(); ctx.arc(p.x, p.y, p.r, 0, 6.283); ctx.fill();
    }
    ctx.globalAlpha = 1;
    requestAnimationFrame(tick);
  }
  if (!reduce) {
    for (let i = 0; i < 160; i++) { if (Math.random() < .35) spawn(); parts.forEach(step); }
    tick();
  }

  /* ---------- menyu ---------- */
  const menuEl = $('menu');
  const price = v => Number(v).toFixed(2).replace('.', ',') + ' ₼';
  async function loadMenu(category) {
    try {
      const items = await api('/api/menu' + (category ? '?category=' + category : ''));
      menuEl.innerHTML = items.map(m =>
        `<div class="item"><h3>${esc(m.name)}${m.tag ? `<span class="tag">${esc(m.tag)}</span>` : ''}</h3><span class="price">${price(m.price)}</span><p>${esc(m.description)}</p></div>`).join('');
    } catch (e) {
      menuEl.innerHTML = `<p class="muted">Menyunu yükləmək olmadı. ${esc(e.message)}</p>`;
    }
  }
  document.querySelectorAll('.chip').forEach(b => b.addEventListener('click', () => {
    document.querySelectorAll('.chip').forEach(x => x.setAttribute('aria-pressed', x === b));
    loadMenu(b.dataset.f);
  }));
  loadMenu('');

  /* ---------- dəmləmə saatı ---------- */
  const prog = $('prog'), clock = $('clock'), phase = $('phase');
  const steps = [...document.querySelectorAll('#steps li')];
  const startBtn = $('startBtn'), lenSel = $('brewLen');
  const C = 2 * Math.PI * 52;
  prog.style.strokeDasharray = C;
  let total = 300, left = 300, timer = null;
  const mmss = s => `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`;
  function draw() {
    const frac = 1 - left / total;
    prog.style.strokeDashoffset = C * (1 - frac);
    clock.textContent = mmss(left);
    let cur = -1;
    steps.forEach((li, i) => { if (frac >= +li.dataset.at) cur = i; });
    steps.forEach((li, i) => { li.classList.toggle('on', i === cur && (timer || left === 0)); li.classList.toggle('done', i < cur); });
    phase.textContent = left === 0 ? 'dəm hazırdır' : timer ? 'dəmlənir…' : left === total ? 'hazırdır' : 'fasilə';
  }
  function stop() { clearInterval(timer); timer = null; startBtn.textContent = left === total ? 'Başla' : 'Davam et'; }
  startBtn.addEventListener('click', () => {
    if (timer) { stop(); draw(); return; }
    if (left === 0) left = total;
    startBtn.textContent = 'Dayandır';
    timer = setInterval(() => {
      left--;
      if (left <= 0) { left = 0; stop(); startBtn.textContent = 'Yenidən'; }
      draw();
    }, 1000);
    draw();
  });
  $('resetBtn').addEventListener('click', () => { stop(); left = total; startBtn.textContent = 'Başla'; draw(); });
  lenSel.addEventListener('change', () => { stop(); total = left = +lenSel.value; startBtn.textContent = 'Başla'; draw(); });
  draw();

  /* ---------- mürəbbə ---------- */
  const jamsEl = $('jams'), pairEl = $('pairing');
  let jams = [];
  function pick(i) {
    jamsEl.querySelectorAll('.jam').forEach(b => b.setAttribute('aria-pressed', +b.dataset.i === i));
    pairEl.innerHTML = `<strong>${esc(jams[i].name)} mürəbbəsi:</strong> ${esc(jams[i].pairing)}`;
  }
  jamsEl.addEventListener('click', e => { const b = e.target.closest('.jam'); if (b) pick(+b.dataset.i); });
  api('/api/jams').then(list => {
    jams = list;
    $('jamCount').textContent = list.length;
    jamsEl.innerHTML = list.map((j, i) =>
      `<button class="jam" data-i="${i}" aria-pressed="false"><span class="jar" style="background:${/^#[0-9a-f]{6}$/i.test(j.color) ? j.color : '#999'}"></span><b>${esc(j.name)}</b><small>${esc(j.description)}</small></button>`).join('');
    if (list.length) pick(0);
  }).catch(e => { pairEl.textContent = 'Mürəbbə rəfini yükləmək olmadı. ' + e.message; });

  /* ---------- rezerv ---------- */
  const form = $('resForm'), conf = $('confirm'), tSel = $('rTime'), gSel = $('rGuests'), dIn = $('rDate'), spotSel = $('rSpot'), submit = $('resSubmit');
  const months = ['yanvar', 'fevral', 'mart', 'aprel', 'may', 'iyun', 'iyul', 'avqust', 'sentyabr', 'oktyabr', 'noyabr', 'dekabr'];
  const localIso = d => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;

  for (let g = 1; g <= 12; g++) gSel.add(new Option(g + ' nəfər', g));
  gSel.value = '2';
  const today = new Date(), tomorrow = new Date(today), maxDay = new Date(today);
  tomorrow.setDate(today.getDate() + 1); maxDay.setDate(today.getDate() + 60);
  dIn.min = localIso(today); dIn.max = localIso(maxDay); dIn.value = localIso(tomorrow);

  api('/api/reservations/spots').then(list => list.forEach(s => spotSel.add(new Option(s.label, s.value))));

  async function loadSlots() {
    if (!dIn.value) return;
    const keep = tSel.value || '19:00';
    tSel.innerHTML = '<option value="">Yüklənir…</option>';
    try {
      const slots = await api('/api/reservations/slots?date=' + dIn.value);
      tSel.innerHTML = '';
      for (const s of slots) {
        const o = new Option(s.remaining > 0 ? `${s.time} · ${s.remaining} yer` : `${s.time} · dolu`, s.time);
        o.disabled = s.remaining <= 0;
        tSel.add(o);
      }
      const want = slots.find(s => s.time === keep && s.remaining > 0) || slots.find(s => s.remaining > 0);
      tSel.value = want ? want.time : '';
      if (!want) tSel.add(new Option('Bu gün üçün boş saat yoxdur', ''), 0), tSel.value = '';
    } catch (e) {
      tSel.innerHTML = '<option value="">Saatları yükləmək olmadı</option>';
    }
  }
  dIn.addEventListener('change', loadSlots);
  loadSlots();

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
      name: form.name.value.trim(),
      phone: form.phone.value.trim(),
      date: dIn.value || null,
      time: tSel.value || null,
      guests: +gSel.value,
      spot: spotSel.value || null,
      note: form.note.value.trim()
    };
    submit.disabled = true; submit.textContent = 'Göndərilir…';
    try {
      const r = await api('/api/reservations', { method: 'POST', body: JSON.stringify(payload) });
      const d = new Date(r.date + 'T00:00');
      conf.className = 'confirm';
      conf.innerHTML = `<b>Sağ olun, ${esc(r.name.split(' ')[0])}!</b><p>Rezerv nömrəniz <strong>#${r.id}</strong>. ${d.getDate()} ${months[d.getMonth()]}, saat ${esc(r.time)} · ${r.guests} nəfər · ${esc(r.spotLabel)}.</p>`;
      conf.hidden = false;
      form.note.value = '';
      loadSlots();
    } catch (err) {
      showErrors(err.data?.fields);
      conf.className = 'confirm bad';
      conf.innerHTML = `<b>Rezerv alınmadı</b><p>${esc(err.message)}</p>`;
      conf.hidden = false;
      if (err.status === 409) loadSlots();
    } finally {
      submit.disabled = false; submit.textContent = 'Rezervi təsdiqlə';
    }
  });
})();
