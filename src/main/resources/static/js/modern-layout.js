/* WeSchedule - Modern Layout JS */

// Toggle sidebar collapsed
function toggleSidebar() {
  const sidebar = document.getElementById('ws-sidebar');
  if (!sidebar) return;
  const isCollapsed = sidebar.classList.toggle('collapsed');
  document.body.classList.toggle('sidebar-collapsed', isCollapsed);
  localStorage.setItem('sidebarCollapsed', isCollapsed);
}

// Restore sidebar state on page load
(function () {
  const sidebar = document.getElementById('ws-sidebar');
  if (!sidebar) return;
  if (localStorage.getItem('sidebarCollapsed') === 'true') {
    sidebar.classList.add('collapsed');
    document.body.classList.add('sidebar-collapsed');
  }
})();

// Toggle dropdown
function toggleDropdown(id) {
  const el = document.getElementById(id);
  if (!el) return;
  el.classList.toggle('open');
}

// Close dropdowns on outside click
document.addEventListener('click', function (e) {
  document.querySelectorAll('.ws-dropdown.open').forEach(function (d) {
    if (!d.contains(e.target)) d.classList.remove('open');
  });
});

// Fullscreen
function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen();
  } else {
    document.exitFullscreen();
  }
}

// Mobile sidebar overlay
(function () {
  const overlay = document.createElement('div');
  overlay.id = 'ws-overlay';
  overlay.style.cssText =
    'display:none;position:fixed;inset:0;background:rgba(0,0,0,0.4);z-index:99;backdrop-filter:blur(2px);';
  document.body.appendChild(overlay);

  overlay.addEventListener('click', function () {
    const sidebar = document.getElementById('ws-sidebar');
    if (sidebar) sidebar.classList.remove('mobile-open');
    overlay.style.display = 'none';
  });

  window.openMobileSidebar = function () {
    const sidebar = document.getElementById('ws-sidebar');
    if (sidebar) sidebar.classList.add('mobile-open');
    overlay.style.display = 'block';
  };
})();
