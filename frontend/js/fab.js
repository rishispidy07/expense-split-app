document.addEventListener('DOMContentLoaded', () => {
    if (!API.isLoggedIn()) return;

    const fab = document.getElementById('fabCreate');
    const overlay = document.getElementById('quickActionOverlay');
    const panel = document.getElementById('quickActionPanel');

    if (!fab || !overlay) return;

    fab.addEventListener('click', (e) => {
        e.preventDefault();
        overlay.classList.add('active');
        panel.classList.add('active');
    });

    overlay.addEventListener('click', () => closeQuickAction());

    document.querySelectorAll('.quick-action-item').forEach(item => {
        item.addEventListener('click', () => closeQuickAction());
    });
});

function closeQuickAction() {
    const overlay = document.getElementById('quickActionOverlay');
    const panel = document.getElementById('quickActionPanel');
    if (overlay) overlay.classList.remove('active');
    if (panel) panel.classList.remove('active');
}
