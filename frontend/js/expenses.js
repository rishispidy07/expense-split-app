let allExpenses = [];
let allGroups = [];

const catIcons = {
    Food:{cls:'icon-food',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M18 8h1a4 4 0 0 1 0 8h-1"/><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/></svg>'},
    Transport:{cls:'icon-transport',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><rect x="1" y="3" width="15" height="13" rx="2"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>'},
    Entertainment:{cls:'icon-entertainment',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><rect x="2" y="2" width="20" height="20" rx="2.18"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/><line x1="2" y1="12" x2="22" y2="12"/></svg>'},
    Shopping:{cls:'icon-shopping',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>'},
    Bills:{cls:'icon-bills',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>'},
    Health:{cls:'icon-health',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>'},
    Education:{cls:'icon-education',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>'},
    Travel:{cls:'icon-travel',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>'},
    Groceries:{cls:'icon-groceries',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/></svg>'},
    Coffee:{cls:'icon-coffee',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M18 8h1a4 4 0 0 1 0 8h-1"/><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/></svg>'},
    Rent:{cls:'icon-rent',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>'},
    Income:{cls:'icon-income',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M12 2v20"/><path d="M17 7l-5-5-5 5"/></svg>'},
    General:{cls:'icon-general',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>'}
};

document.addEventListener('DOMContentLoaded', async () => {
    if (!API.requireAuth()) return;

    const user = API.getUser();
    document.getElementById('userName').textContent = user ? user.fullName || user.username : '';

    document.getElementById('logoutBtn').addEventListener('click', async () => {
        await API.post('/api/auth/logout');
        API.clearToken();
        window.location.href = '/login.html';
    });

    const now = new Date();
    const currentYear = now.getFullYear();
    const yearSel = document.getElementById('yearSelect');
    for (let y = currentYear; y >= currentYear - 3; y--) {
        const o = document.createElement('option'); o.value = y; o.textContent = y;
        yearSel.appendChild(o);
    }
    yearSel.value = currentYear;
    document.getElementById('monthSelect').value = now.getMonth() + 1;
    const expYearSel = document.getElementById('expYear');
    if (expYearSel) expYearSel.value = currentYear;
    const expMonthSel = document.getElementById('expMonth');
    if (expMonthSel) expMonthSel.value = now.getMonth() + 1;

    const [expRes, grpRes] = await Promise.all([
        API.get('/api/expenses'),
        API.get('/api/groups')
    ]);

    allExpenses = expRes && expRes.success ? expRes.expenses : [];
    allGroups = grpRes && grpRes.success ? grpRes.groups : [];

    const grpSel = document.getElementById('expGroup');
    allGroups.forEach(g => {
        const o = document.createElement('option');
        o.value = g.id; o.textContent = g.name;
        grpSel.appendChild(o);
    });

    yearSel.addEventListener('change', filterExpenses);
    document.getElementById('monthSelect').addEventListener('change', filterExpenses);

    filterExpenses();
});

function filterExpenses() {
    const year = parseInt(document.getElementById('yearSelect').value);
    const month = parseInt(document.getElementById('monthSelect').value);
    const filtered = allExpenses.filter(e => {
        if (e.category === 'Income') return false;
        if (e.expenseYear && e.expenseYear !== year) return false;
        if (month > 0 && e.expenseMonth && e.expenseMonth !== month) return false;
        return true;
    });

    const total = filtered.reduce((s, e) => s + (e.amount || 0), 0);
    document.getElementById('filterTotal').textContent = '\u20B9' + total.toLocaleString('en-IN');
    document.getElementById('filterCount').textContent = filtered.length;

    const container = document.querySelector('#expensesList .card-body');
    if (filtered.length === 0) {
        container.innerHTML = '<p class="empty-state">No expenses for this period</p>';
        return;
    }
    container.innerHTML = filtered.map(e => {
        const cat = catIcons[e.category] || catIcons.General;
        const date = new Date(e.createdAt).toLocaleDateString('en-IN', {day:'numeric',month:'short'});
        return `<div class="item-row">
            <div class="item-icon ${cat.cls}">${cat.icon}</div>
            <div class="item-details">
                <div class="item-name">${escapeHtml(e.description)}</div>
                <div class="item-sub">${escapeHtml(e.groupName || 'Personal')} \u00B7 ${date}</div>
            </div>
            <span class="item-amount">\u20B9${(e.amount || 0).toLocaleString('en-IN')}</span>
            <button class="item-del" onclick="deleteExpense(${e.id})">Del</button>
        </div>`;
    }).join('');
}

function showAddExpense() {
    document.getElementById('expenseModal').style.display = 'flex';
}

function closeModal() {
    document.getElementById('expenseModal').style.display = 'none';
    document.getElementById('expError').style.display = 'none';
}

async function createExpense() {
    const desc = document.getElementById('expDesc').value.trim();
    const amount = parseFloat(document.getElementById('expAmount').value);
    const category = document.getElementById('expCategory').value;
    const groupId = document.getElementById('expGroup').value || null;
    const splitType = document.getElementById('expSplit').value;
    const year = parseInt(document.getElementById('expYear').value);
    const month = parseInt(document.getElementById('expMonth').value);
    const errDiv = document.getElementById('expError');

    if (!desc) { errDiv.textContent = 'Enter a description'; errDiv.style.display = 'block'; return; }
    if (!amount || amount <= 0) { errDiv.textContent = 'Enter a valid amount'; errDiv.style.display = 'block'; return; }

    const body = { description: desc, amount, category, groupId: groupId ? parseInt(groupId) : null, splitType, year, month };
    const res = await API.post('/api/expenses', body);

    if (res && res.success) {
        closeModal();
        document.getElementById('expDesc').value = '';
        document.getElementById('expAmount').value = '';
        location.reload();
    } else {
        errDiv.textContent = res ? res.message : 'Failed to create expense';
        errDiv.style.display = 'block';
    }
}

async function deleteExpense(id) {
    if (!confirm('Delete this expense?')) return;
    const res = await API.del('/api/expenses/' + id);
    if (res && res.success) {
        allExpenses = allExpenses.filter(e => e.id !== id);
        filterExpenses();
    }
}

function escapeHtml(str) { if (!str) return ''; const d = document.createElement('div'); d.textContent = str; return d.innerHTML; }
