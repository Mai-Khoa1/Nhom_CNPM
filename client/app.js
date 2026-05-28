/* ===== Equestria Admin — Shared App Logic =====
 * Supports both:
 *   - Multi-page navigation (horses / jockeys / schedule / results)
 *   - SPA section switching inside index.html (dashboard / stats / settings)
 */

/* ---------- Sample data ---------- */
const horses = [
  { name: 'Thunderbolt', age: 5, breed: 'Thoroughbred', speed: 72,
    owner: 'Admin Demo', status: 'active',
    img: 'https://images.unsplash.com/photo-1553284965-83fd3e82fa5a?w=200&h=200&fit=crop' },
];

const jockeys = [
  { name: 'Nguyễn Văn An', country: 'Việt Nam', wins: 1, races: 1,
    img: 'https://i.pravatar.cc/150?img=68' },
];

const races = [
  { name: 'Royal Cup Championship 2026', date: '28', month: 'TH 05', time: '14:00',
    venue: 'Phú Thọ Racetrack, HCM', prize: '₫ 500,000,000', horses: 1, status: 'upcoming' },
];

const results = [
  { race: 'Royal Cup Championship 2025', date: '20/05/2025',
    venue: 'Phú Thọ Racetrack, HCM', winner: 'Thunderbolt',
    jockey: 'Nguyễn Văn An', time: '1:42.35', prize: '₫ 500,000,000' },
];

const revenueData = [{ m: 'T1', v: 100 }];

const statusMap = {
  active:  { label: 'Đang thi đấu', cls: 'success' },
  rest:    { label: 'Nghỉ ngơi',    cls: 'info' },
  injured: { label: 'Chấn thương',  cls: 'danger' },
};

const raceStatusMap = {
  upcoming: { label: 'Sắp diễn ra',  cls: 'gold' },
  live:     { label: 'Đang diễn ra', cls: 'success' },
  done:     { label: 'Đã kết thúc',  cls: 'info' },
};

/* ---------- Renderers (each guards its target element) ---------- */
function renderTopHorses() {
  const el = document.getElementById('topHorses');
  if (!el) return;
  el.innerHTML = horses.slice(0, 5).map((h, i) => `
    <li>
      <div class="rank-badge ${i === 0 ? 'gold' : ''}">${i + 1}</div>
      <img src="${h.img}" alt="${h.name}" />
      <div class="h-info">
        <div class="h-name">${h.name}</div>
        <div class="h-meta">${h.breed} · ${h.age} tuổi</div>
      </div>
      <div class="h-score">${(98 - i * 3).toFixed(1)}</div>
    </li>`).join('');
}

function renderRaceTable() {
  const el = document.getElementById('raceTable');
  if (!el) return;
  el.innerHTML = races.map(r => `
    <tr>
      <td><b>${r.name}</b></td>
      <td>${r.date}/${r.month.split(' ')[1]} · ${r.time}</td>
      <td>${r.venue}</td>
      <td>${r.horses} ngựa</td>
      <td><b style="color:var(--gold)">${r.prize}</b></td>
      <td><span class="badge ${raceStatusMap[r.status].cls}">${raceStatusMap[r.status].label}</span></td>
    </tr>`).join('');
}

function renderHorseTable() {
  const el = document.getElementById('horseTable');
  if (!el) return;
  el.innerHTML = horses.map(h => `
    <tr>
      <td>
        <div class="cell-horse">
          <img src="${h.img}" alt="${h.name}" />
          <div><b>${h.name}</b><small>ID #${Math.floor(1000 + Math.random() * 9000)}</small></div>
        </div>
      </td>
      <td>${h.age} tuổi</td>
      <td>${h.breed}</td>
      <td>
        <div class="speed-bar">
          <div class="bar"><div class="fill" style="width:${(h.speed / 80) * 100}%"></div></div>
          <span>${h.speed} km/h</span>
        </div>
      </td>
      <td>${h.owner}</td>
      <td><span class="badge ${statusMap[h.status].cls}">${statusMap[h.status].label}</span></td>
      <td class="right">
        <div class="row-actions">
          <button title="Xem"><i class="fa-regular fa-eye"></i></button>
          <button title="Sửa"><i class="fa-regular fa-pen-to-square"></i></button>
          <button class="danger" title="Xoá"><i class="fa-regular fa-trash-can"></i></button>
        </div>
      </td>
    </tr>`).join('');
}

function renderJockeys() {
  const el = document.getElementById('jockeyGrid');
  if (!el) return;
  el.innerHTML = jockeys.map(j => `
    <div class="jockey-card">
      <img src="${j.img}" alt="${j.name}" />
      <h4>${j.name}</h4>
      <p class="country"><i class="fa-solid fa-location-dot"></i> ${j.country}</p>
      <div class="jockey-stats">
        <div><p>Thắng</p><b>${j.wins}</b></div>
        <div><p>Cuộc đua</p><b>${j.races}</b></div>
        <div><p>Tỉ lệ</p><b>${Math.round(j.wins / j.races * 100)}%</b></div>
      </div>
    </div>`).join('');
}

function renderTimeline() {
  const el = document.getElementById('timeline');
  if (!el) return;
  el.innerHTML = races.map(r => `
    <div class="timeline-item">
      <div class="race-card">
        <div class="race-date"><b>${r.date}</b><span>${r.month}</span></div>
        <div class="race-info">
          <h4>${r.name}</h4>
          <p><span class="badge ${raceStatusMap[r.status].cls}">${raceStatusMap[r.status].label}</span></p>
          <div class="meta">
            <span><i class="fa-regular fa-clock"></i> ${r.time}</span>
            <span><i class="fa-solid fa-location-dot"></i> ${r.venue}</span>
            <span><i class="fa-solid fa-trophy"></i> ${r.prize}</span>
            <span><i class="fa-solid fa-horse"></i> ${r.horses} ngựa tham gia</span>
          </div>
        </div>
        <div class="race-horses">
          ${horses.slice(0, 4).map(h => `<img src="${h.img}" alt="${h.name}" />`).join('')}
          ${r.horses > 4 ? `<div class="more">+${r.horses - 4}</div>` : ''}
        </div>
      </div>
    </div>`).join('');
}

function renderResults() {
  const el = document.getElementById('resultsTable');
  if (!el) return;
  el.innerHTML = results.map((r, i) => `
    <tr>
      <td><div class="rank-badge ${i === 0 ? 'gold' : ''}">${i + 1}</div></td>
      <td><b>${r.race}</b><small style="display:block;opacity:.6">${r.date}</small></td>
      <td>${r.venue}</td>
      <td>${r.winner}</td>
      <td>${r.jockey}</td>
      <td>${r.time}</td>
      <td><b style="color:var(--gold)">${r.prize}</b></td>
    </tr>`).join('');
}

function renderChart() {
  const svg = document.getElementById('revChart');
  if (!svg) return;
  const W = 600, H = 240, P = 20;
  const data = revenueData.length > 1 ? revenueData : [{ m: 'T1', v: 50 }, { m: 'T2', v: 100 }];
  const max = Math.max(...data.map(d => d.v)) * 1.1;
  const stepX = (W - P * 2) / Math.max(1, data.length - 1);
  const points = data.map((d, i) => ({
    x: P + i * stepX,
    y: H - P - (d.v / max) * (H - P * 2),
  }));
  const path = points.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ');
  const area = `${path} L ${points[points.length - 1].x} ${H - P} L ${P} ${H - P} Z`;
  const grid = [0, 1, 2, 3].map(i => {
    const y = P + ((H - P * 2) / 3) * i;
    return `<line x1="${P}" y1="${y}" x2="${W - P}" y2="${y}" stroke="currentColor" stroke-opacity=".08" stroke-dasharray="4 4" />`;
  }).join('');
  svg.innerHTML = `
    <defs><linearGradient id="grad" x1="0" x2="0" y1="0" y2="1">
      <stop offset="0%" stop-color="#d4af37" stop-opacity=".35"/>
      <stop offset="100%" stop-color="#d4af37" stop-opacity="0"/>
    </linearGradient></defs>
    ${grid}
    <path class="area" d="${area}" />
    <path class="line" d="${path}" />
    ${points.map(p => `<circle class="dot" cx="${p.x}" cy="${p.y}" r="4" />`).join('')}`;
  const x = document.getElementById('xAxis');
  if (x) x.innerHTML = data.map(d => `<span>${d.m}</span>`).join('');
}

/* ---------- Shared UI setup ---------- */
function setupSidebarToggle() {
  const btn = document.getElementById('toggleSidebar');
  const sb = document.getElementById('sidebar');
  if (!btn || !sb) return;
  if (localStorage.getItem('eq-sidebar') === 'collapsed') sb.classList.add('collapsed');
  btn.addEventListener('click', () => {
    sb.classList.toggle('collapsed');
    localStorage.setItem('eq-sidebar', sb.classList.contains('collapsed') ? 'collapsed' : 'open');
  });
}

function setupTheme() {
  const btn = document.getElementById('themeToggle');
  const saved = localStorage.getItem('eq-theme');
  if (saved === 'light') document.body.classList.remove('dark-mode');
  const sync = () => {
    if (!btn) return;
    const isDark = document.body.classList.contains('dark-mode');
    btn.innerHTML = `<i class="fa-solid fa-${isDark ? 'moon' : 'sun'}"></i>`;
  };
  sync();
  btn?.addEventListener('click', () => {
    document.body.classList.toggle('dark-mode');
    const isDark = document.body.classList.contains('dark-mode');
    localStorage.setItem('eq-theme', isDark ? 'dark' : 'light');
    sync();
  });
}

function setupDate() {
  const el = document.getElementById('todayDate');
  if (!el) return;
  el.textContent = new Date().toLocaleDateString('vi-VN', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric',
  });
}

function setupSegs() {
  document.querySelectorAll('.seg').forEach(seg => {
    seg.querySelectorAll('.seg-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        seg.querySelectorAll('.seg-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
      });
    });
  });
}

function setupModal() {
  const modal = document.getElementById('modal');
  if (!modal) return;
  const open = () => modal.classList.add('show');
  const close = () => modal.classList.remove('show');
  document.getElementById('addHorseBtn')?.addEventListener('click', open);
  document.getElementById('closeModal')?.addEventListener('click', close);
  document.getElementById('cancelModal')?.addEventListener('click', close);
  modal.addEventListener('click', e => { if (e.target === modal) close(); });
  document.getElementById('horseForm')?.addEventListener('submit', e => {
    e.preventDefault(); close();
    alert('Đã lưu thông tin ngựa thành công!');
  });
}

/* ---------- Active nav highlight ----------
 * Multi-page: highlight by body[data-page]
 * Inside index.html: highlight by current section (handled in setupSectionRouter)
 */
function setupActiveNav() {
  const page = document.body.dataset.page;
  if (!page) return;
  document.querySelectorAll('.nav-item').forEach(n => {
    n.classList.toggle('active', n.dataset.page === page);
  });
}

/* ---------- SPA section router (index.html only) ---------- */
const INDEX_SECTIONS = ['dashboard', 'stats', 'settings'];

function showSection(name) {
  if (!INDEX_SECTIONS.includes(name)) name = 'dashboard';
  document.querySelectorAll('.page-section').forEach(s => s.classList.remove('active'));
  document.getElementById(`${name}Page`)?.classList.add('active');
  document.querySelectorAll('.nav-item').forEach(n => {
    n.classList.toggle('active', n.dataset.page === name);
  });
  if (location.hash.replace('#', '') !== name) {
    history.replaceState(null, '', name === 'dashboard' ? '#' : `#${name}`);
  }
}

function setupSectionRouter() {
  // Only run on pages that actually contain index sections
  if (!document.getElementById('dashboardPage')) return;

  document.querySelectorAll('.nav-item').forEach(item => {
    const target = item.dataset.page;
    if (INDEX_SECTIONS.includes(target)) {
      item.addEventListener('click', e => {
        e.preventDefault();
        showSection(target);
      });
    }
  });

  const initial = location.hash.replace('#', '') || 'dashboard';
  showSection(initial);

  window.addEventListener('hashchange', () => {
    showSection(location.hash.replace('#', '') || 'dashboard');
  });
}

/* ---------- Per-page renderers ---------- */
const pageRenderers = {
  dashboard: () => { renderTopHorses(); renderRaceTable(); renderChart(); },
  horses:    () => { renderHorseTable(); },
  jockeys:   () => { renderJockeys(); },
  schedule:  () => { renderTimeline(); },
  results:   () => { renderResults(); },
};

/* ---------- Init ---------- */
document.addEventListener('DOMContentLoaded', () => {
  setupSidebarToggle();
  setupTheme();
  setupDate();
  setupSegs();
  setupModal();
  setupActiveNav();
  setupSectionRouter();

  const page = document.body.dataset.page;
  pageRenderers[page]?.();
});
