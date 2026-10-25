const catIconMap = {
    Food:{cls:'icon-food',color:'#f59e0b',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M18 8h1a4 4 0 0 1 0 8h-1"/><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/></svg>'},
    Transport:{cls:'icon-transport',color:'#6366f1',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><rect x="1" y="3" width="15" height="13" rx="2"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>'},
    Entertainment:{cls:'icon-entertainment',color:'#ec4899',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><rect x="2" y="2" width="20" height="20" rx="2.18"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/><line x1="2" y1="12" x2="22" y2="12"/></svg>'},
    Shopping:{cls:'icon-shopping',color:'#10b981',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>'},
    Bills:{cls:'icon-bills',color:'#ef4444',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>'},
    Health:{cls:'icon-health',color:'#0ea5e9',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>'},
    Education:{cls:'icon-education',color:'#8b5cf6',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>'},
    Travel:{cls:'icon-travel',color:'#06b6d4',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>'},
    Groceries:{cls:'icon-groceries',color:'#16a34a',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/></svg>'},
    Coffee:{cls:'icon-coffee',color:'#b45309',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M18 8h1a4 4 0 0 1 0 8h-1"/><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/></svg>'},
    Rent:{cls:'icon-rent',color:'#dc2626',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>'},
    General:{cls:'icon-general',color:'#6b7280',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>'},
    Income:{cls:'icon-income',color:'#10b981',icon:'<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M12 2v20"/><path d="M17 7l-5-5-5 5"/></svg>'}
};

const trendColors = ['#6366f1','#818cf8','#a78bfa','#c4b5fd','#818cf8','#6366f1','#4f46e5','#7c3aed','#8b5cf6','#a78bfa','#c4b5fd','#6366f1'];

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

    yearSel.addEventListener('change', loadReports);
    document.getElementById('monthSelect').addEventListener('change', loadReports);
    await loadReports();
});

async function loadReports() {
    const year = parseInt(document.getElementById('yearSelect').value);
    const month = parseInt(document.getElementById('monthSelect').value);

    const [expRes, monthlyRes] = await Promise.all([
        API.get('/api/expenses'),
        API.get(`/api/stats/monthly?year=${year}`)
    ]);

    const expenses = expRes && expRes.success ? expRes.expenses : [];
    const monthlyData = monthlyRes && monthlyRes.success ? monthlyRes.months : [];

    const filtered = expenses.filter(e => {
        if (e.category === 'Income') return false;
        if (month > 0 && e.expenseMonth !== month) return false;
        return true;
    });

    lastFilteredExpenses = filtered;

    const totalSpent = filtered.reduce((s, e) => s + (e.amount || 0), 0);
    const totalEntries = filtered.length;
    const avgSpend = totalEntries > 0 ? totalSpent / totalEntries : 0;

    document.getElementById('totalSpent').textContent = '\u20B9' + totalSpent.toLocaleString('en-IN');
    document.getElementById('totalEntries').textContent = totalEntries;
    document.getElementById('avgSpend').textContent = '\u20B9' + avgSpend.toLocaleString('en-IN', {maximumFractionDigits:0});

    renderCategories(filtered, totalSpent);
    renderTrend(monthlyData, year);
    renderHistory(filtered);
}

function renderCategories(expenses, total) {
    const container = document.getElementById('categoryReport');
    if (expenses.length === 0) {
        container.innerHTML = '<p class="empty-state">No expenses for this period</p>';
        return;
    }

    const catMap = {};
    expenses.forEach(e => {
        const cat = e.category || 'General';
        if (!catMap[cat]) catMap[cat] = 0;
        catMap[cat] += e.amount || 0;
    });

    const sorted = Object.entries(catMap).sort((a, b) => b[1] - a[1]);
    const maxAmount = sorted[0][1];

    container.innerHTML = sorted.map(([cat, amount]) => {
        const info = catIconMap[cat] || catIconMap.General;
        const pct = total > 0 ? ((amount / total) * 100).toFixed(1) : 0;
        const barWidth = maxAmount > 0 ? ((amount / maxAmount) * 100) : 0;
        return `<div class="cat-row">
            <div class="cat-icon ${info.cls}">${info.icon}</div>
            <div class="cat-info">
                <div class="cat-name">${escapeHtml(cat)}</div>
                <div class="cat-bar-wrap"><div class="cat-bar" style="width:${barWidth}%;background:${info.color}"></div></div>
            </div>
            <div class="cat-right">
                <div class="cat-amount">\u20B9${amount.toLocaleString('en-IN')}</div>
                <div class="cat-pct">${pct}%</div>
            </div>
        </div>`;
    }).join('');
}

function renderTrend(monthlyData, year) {
    const container = document.getElementById('monthlyTrend');
    const monthNames = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

    if (!monthlyData || monthlyData.length === 0) {
        container.innerHTML = '<p class="empty-state">No monthly data</p>';
        return;
    }

    const maxVal = Math.max(...monthlyData.map(m => m.total), 1);

    container.innerHTML = monthlyData.map(m => {
        const h = Math.max((m.total / maxVal) * 120, 4);
        const color = trendColors[m.month - 1] || '#6366f1';
        return `<div class="trend-bar-wrap">
            <div class="trend-val">\u20B9${m.total >= 1000 ? (m.total/1000).toFixed(1)+'k' : m.total}</div>
            <div class="trend-bar" style="height:${h}px;background:${color}"></div>
            <div class="trend-label">${monthNames[m.month - 1]}</div>
        </div>`;
    }).join('');
}

function renderHistory(expenses) {
    const container = document.getElementById('expenseHistory');
    if (expenses.length === 0) {
        container.innerHTML = '<p class="empty-state">No expenses</p>';
        return;
    }

    container.innerHTML = expenses.slice(0, 15).map(e => {
        const info = catIconMap[e.category] || catIconMap.General;
        const date = new Date(e.createdAt).toLocaleDateString('en-IN', {day:'numeric',month:'short'});
        return `<div class="hist-item">
            <div class="hist-icon ${info.cls}">${info.icon}</div>
            <div class="hist-info">
                <div class="hist-name">${escapeHtml(e.description)}</div>
                <div class="hist-sub">${escapeHtml(e.category || 'General')} \u00B7 ${date}</div>
            </div>
            <span class="hist-amount">-\u20B9${(e.amount || 0).toLocaleString('en-IN')}</span>
        </div>`;
    }).join('');
}

function escapeHtml(s) { if(!s) return ''; const d=document.createElement('div'); d.textContent=s; return d.innerHTML; }

let lastFilteredExpenses = [];

function exportCSV() {
    if (lastFilteredExpenses.length === 0) {
        alert('No transactions to export');
        return;
    }

    const headers = ['Date', 'Description', 'Category', 'Amount', 'Group', 'Year', 'Month'];
    const rows = lastFilteredExpenses.map(e => {
        const date = new Date(e.createdAt).toLocaleDateString('en-IN');
        const desc = (e.description || '').replace(/,/g, ' ');
        const cat = e.category || 'General';
        const amt = e.amount || 0;
        const grp = (e.groupName || 'Personal').replace(/,/g, ' ');
        return [date, desc, cat, amt, grp, e.expenseYear || '', e.expenseMonth || ''].join(',');
    });

    const csv = headers.join(',') + '\n' + rows.join('\n');
    const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    const now = new Date();
    const monthNames = ['','Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
    const month = parseInt(document.getElementById('monthSelect').value);
    const year = document.getElementById('yearSelect').value;
    const fileName = 'SplitEase_' + (month > 0 ? monthNames[month] + '_' : '') + year + '_transactions.csv';
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}
