const categoryIcons = {
    Food: { cls: 'cat-food', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M18 8h1a4 4 0 0 1 0 8h-1"/><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/><line x1="6" y1="1" x2="6" y2="4"/><line x1="10" y1="1" x2="10" y2="4"/><line x1="14" y1="1" x2="14" y2="4"/></svg>' },
    Transport: { cls: 'cat-transport', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><rect x="1" y="3" width="15" height="13" rx="2"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>' },
    Entertainment: { cls: 'cat-entertainment', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><rect x="2" y="2" width="20" height="20" rx="2.18"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/><line x1="2" y1="12" x2="22" y2="12"/></svg>' },
    Shopping: { cls: 'cat-shopping', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>' },
    Bills: { cls: 'cat-bills', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>' },
    Health: { cls: 'cat-health', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>' },
    Education: { cls: 'cat-education', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>' },
    Travel: { cls: 'cat-travel', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>' },
    Groceries: { cls: 'cat-groceries', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/></svg>' },
    Coffee: { cls: 'cat-coffee', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M18 8h1a4 4 0 0 1 0 8h-1"/><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/></svg>' },
    Rent: { cls: 'cat-rent', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>' },
    Income: { cls: 'cat-income', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M12 2v20"/><path d="M17 7l-5-5-5 5"/></svg>' },
    General: { cls: 'cat-general', icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>' }
};

document.addEventListener('DOMContentLoaded', async () => {
    if (!API.requireAuth()) return;

    const user = API.getUser();
    document.getElementById('userName').textContent = user ? user.fullName || user.username : '';

    const initials = user ? (user.fullName || user.username || '?').charAt(0).toUpperCase() : 'K';
    document.getElementById('userAvatar').innerHTML = `<span class="avatar-pulse"></span>${initials}`;

    const now = new Date();
    const hours = now.getHours();
    let greeting = 'Good evening';
    let wave = '\uD83D\uDC4B';
    if (hours < 12) { greeting = 'Good morning'; wave = '\u2600\uFE0F'; }
    else if (hours < 17) { greeting = 'Good afternoon'; wave = '\u26C5'; }
    else if (hours < 21) { greeting = 'Good evening'; wave = '\uD83C\uDF19'; }
    else { greeting = 'Good night'; wave = '\uD83C\uDF1B'; }
    document.getElementById('welcomeMsg').textContent = `${greeting}, ${user ? user.fullName || user.username : ''}!`;
    document.getElementById('heroWave').textContent = wave;

    function updateClock() {
        const now = new Date();
        const time = now.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: true });
        const date = now.toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
        document.getElementById('liveClock').textContent = time;
        document.getElementById('liveDate').textContent = date;
    }
    updateClock();
    setInterval(updateClock, 1000);

    document.getElementById('logoutBtn').addEventListener('click', async () => {
        await API.post('/api/auth/logout');
        API.clearToken();
        window.location.href = '/login.html';
    });

    await loadDashboard();
});

async function loadDashboard() {
    try {
        const [profileRes, groupsRes, expensesRes] = await Promise.all([
            API.get('/api/user/profile'),
            API.get('/api/groups'),
            API.get('/api/expenses')
        ]);

        const profile = profileRes && profileRes.success ? profileRes.profile : null;
        const groups = groupsRes && groupsRes.success ? groupsRes.groups : [];
        const expenses = expensesRes && expensesRes.success ? expensesRes.expenses : [];

        const balance = profile ? (profile.balance || 0) : 0;
        const incomeEntries = expenses.filter(e => e.category === 'Income');
        const expenseEntries = expenses.filter(e => e.category !== 'Income');
        const income = incomeEntries.reduce((s, e) => s + Math.abs(e.amount || 0), 0);
        const spent = expenseEntries.reduce((s, e) => s + (e.amount || 0), 0);

        document.getElementById('dashBalance').textContent = '\u20B9' + balance.toLocaleString('en-IN');
        document.getElementById('dashIncome').textContent = '\u20B9' + income.toLocaleString('en-IN');
        document.getElementById('dashExpenses').textContent = '\u20B9' + spent.toLocaleString('en-IN');

        const incomeCountEl = document.getElementById('incomeCount');
        const expenseCountEl = document.getElementById('expenseCount');
        if (incomeCountEl) incomeCountEl.textContent = incomeEntries.length + ' entries';
        if (expenseCountEl) expenseCountEl.textContent = expenseEntries.length + ' entries';

        const tagEl = document.getElementById('heroTag');
        if (tagEl) {
            const today = new Date().toISOString().split('T')[0];
            const todayExpenses = expenses.filter(e => {
                const d = new Date(e.createdAt);
                return d.toISOString().split('T')[0] === today && e.category !== 'Income';
            });
            const todayTotal = todayExpenses.reduce((s, e) => s + (e.amount || 0), 0);

            if (todayExpenses.length > 0) {
                tagEl.innerHTML = `<span class="hero-tag tag-expense">\uD83D\uDCB8 ${todayExpenses.length} expense${todayExpenses.length > 1 ? 's' : ''} today \u00B7 \u20B9${todayTotal.toLocaleString('en-IN')}</span>`;
            } else if (balance > 0) {
                tagEl.innerHTML = `<span class="hero-tag tag-bal">\uD83D\uDCB0 Balance: \u20B9${balance.toLocaleString('en-IN')}</span>`;
            } else {
                tagEl.innerHTML = `<span class="hero-tag tag-income">\u2728 Start tracking your expenses today</span>`;
            }
        }

        renderGroups(groups);
        renderTransactions(expenses);
    } catch (err) {
        console.error('Dashboard load error:', err);
    }
}

function renderGroups(groups) {
    const el = document.getElementById('recentGroups');
    if (!groups || groups.length === 0) {
        el.innerHTML = `<div class="empty-state">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="opacity:.3;margin-bottom:8px"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
            <p>No groups yet</p>
            <p style="font-size:11px;margin-top:4px;color:var(--muted)">Create a group to start splitting expenses</p>
        </div>`;
        return;
    }
    el.innerHTML = groups.slice(0, 5).map(g => {
        const memberCount = g.memberCount || 0;
        return `<a href="/groups.html?id=${g.id}" class="item-row" style="text-decoration:none;color:inherit;cursor:pointer">
            <div class="item-icon group-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            </div>
            <div class="item-details">
                <div class="item-name">${escapeHtml(g.name)}</div>
                <div class="item-sub">${g.description ? escapeHtml(g.description) : memberCount + ' member' + (memberCount !== 1 ? 's' : '')} \u00B7 \u20B9${(g.walletBalance || 0).toLocaleString('en-IN')} wallet</div>
            </div>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--muted)" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
        </a>`;
    }).join('');
}

function renderTransactions(expenses) {
    const el = document.getElementById('recentExpenses');
    if (!expenses || expenses.length === 0) {
        el.innerHTML = `<div class="empty-state">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="opacity:.3;margin-bottom:8px"><rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20"/></svg>
            <p>No transactions yet</p>
            <p style="font-size:11px;margin-top:4px;color:var(--muted)">Add income or expenses to see them here</p>
        </div>`;
        return;
    }
    el.innerHTML = expenses.slice(0, 10).map(e => {
        const cat = categoryIcons[e.category] || categoryIcons.General;
        const isIncome = e.category === 'Income';
        const date = new Date(e.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short' });
        return `<a href="/expenses.html" class="item-row" style="text-decoration:none;color:inherit;cursor:pointer">
            <div class="item-icon ${cat.cls}">${cat.icon}</div>
            <div class="item-details">
                <div class="item-name">${escapeHtml(e.description)}</div>
                <div class="item-sub">${escapeHtml(e.category || 'General')} \u00B7 ${date}</div>
            </div>
            <span class="item-amount" style="color:${isIncome ? '#10b981' : '#ef4444'}">${isIncome ? '+' : '-'}\u20B9${Math.abs(e.amount || 0).toLocaleString('en-IN')}</span>
        </a>`;
    }).join('');
}

function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
