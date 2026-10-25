let currentGroupId = null;
let currentGroupCreatedBy = null;

document.addEventListener('DOMContentLoaded', async () => {
    if (!API.requireAuth()) return;
    const user = API.getUser();
    document.getElementById('userName').textContent = user ? user.fullName || user.username : '';

    document.getElementById('logoutBtn').addEventListener('click', async () => {
        await API.post('/api/auth/logout');
        API.clearToken();
        window.location.href = '/login.html';
    });

    document.getElementById('createGroupBtn').addEventListener('click', () => {
        document.getElementById('createGroupModal').style.display = 'flex';
    });

    document.getElementById('createGroupForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('groupName').value.trim();
        const description = document.getElementById('groupDesc').value.trim();
        const result = await API.post('/api/groups', { name, description });
        if (result && result.success) {
            closeModal('createGroupModal');
            loadGroups();
        } else {
            alert(result ? result.message : 'Failed');
        }
    });

    document.getElementById('editGroupForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('editGroupId').value;
        const name = document.getElementById('editGroupName').value.trim();
        const description = document.getElementById('editGroupDesc').value.trim();
        if (!name) { alert('Group name is required'); return; }
        const result = await API.put('/api/groups/' + id, { name, description });
        if (result && result.success) {
            closeModal('editGroupModal');
            loadGroups();
        } else {
            alert(result ? result.message : 'Failed');
        }
    });

    loadGroups();
});

async function loadGroups() {
    const result = await API.get('/api/groups');
    const container = document.getElementById('groupsList');
    const user = API.getUser();

    if (!result || !result.success || !result.groups || result.groups.length === 0) {
        container.innerHTML = '<div style="text-align:center;padding:40px 20px;color:var(--muted)"><p style="font-size:32px;margin-bottom:8px">+</p><p>No groups yet</p></div>';
        return;
    }

    container.innerHTML = result.groups.map(g => `
        <div class="group-card" onclick="openGroupDetail(${g.id})">
            <div class="group-card-header">
                <h3>${escapeHtml(g.name)}</h3>
                <div style="display:flex;align-items:center;gap:6px">
                    <span class="wallet-badge">₹${(g.walletBalance || 0).toLocaleString('en-IN')}</span>
                    ${g.createdBy === (user ? user.id : -1) ? `<button class="group-edit-btn" onclick="event.stopPropagation();openEditGroup(${g.id},'${escapeHtml(g.name).replace(/'/g,"\\'")}','${escapeHtml(g.description || '').replace(/'/g,"\\'")}')" title="Edit Group">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                    </button>` : ''}
                </div>
            </div>
            <p style="margin-bottom:8px">${escapeHtml(g.description || 'No description')}</p>
            <div class="group-meta">
                <span class="role-badge">${g.role}</span>
            </div>
        </div>
    `).join('');
}

async function openGroupDetail(groupId) {
    currentGroupId = groupId;
    const result = await API.get('/api/groups/' + groupId);
    if (!result || !result.success) return alert('Failed to load');

    const group = result.group;
    currentGroupCreatedBy = group.createdBy;

    document.getElementById('detailGroupName').textContent = group.name;
    document.getElementById('detailGroupDesc').textContent = group.description || 'No description';
    document.getElementById('groupWalletBalance').textContent = '₹' + (group.walletBalance || 0).toLocaleString('en-IN');

    document.getElementById('groupDetailModal').style.display = 'flex';
    showTab('splits', document.querySelector('.tab-btn'));
}

function showTab(tabName, btn) {
    document.querySelectorAll('.tab-content').forEach(t => t.style.display = 'none');
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    const el = document.getElementById('tab-' + tabName);
    if (el) el.style.display = 'block';
    if (btn) btn.classList.add('active');

    if (tabName === 'members') loadMembers();
    else if (tabName === 'splits') loadSplits();
    else if (tabName === 'balances') loadBalances();
    else if (tabName === 'wallet') loadWalletTransactions();
    else if (tabName === 'expenses') loadGroupExpenses();
}

async function loadMembers() {
    const result = await API.get('/api/groups/' + currentGroupId);
    if (!result || !result.success) return;
    const members = result.group.members || [];
    const user = API.getUser();
    const isCreator = user && currentGroupCreatedBy === user.id;
    const isAdmin = members.some(m => m.id === (user ? user.id : -1) && m.role === 'admin');

    document.getElementById('membersList').innerHTML = members.map(m => {
        const initial = (m.fullName || m.username || '?').charAt(0).toUpperCase();
        const canRemove = isCreator || isAdmin;
        const isSelf = user && m.id === user.id;
        return `<div class="member-item">
            <div class="member-info">
                <div class="member-avatar">${initial}</div>
                <div>
                    <div class="member-name">${escapeHtml(m.fullName || m.username)}${isSelf ? ' (You)' : ''}</div>
                    <div class="member-role">${m.role}</div>
                </div>
            </div>
            ${canRemove && !isSelf ? `<button class="member-remove" onclick="removeMember(${m.id})" title="Remove">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>` : ''}
        </div>`;
    }).join('') || '<div class="empty-msg">No members</div>';
}

async function addMember() {
    const username = document.getElementById('addMemberUsername').value.trim();
    if (!username) return;
    const result = await API.post('/api/groups/' + currentGroupId + '/members', { username });
    if (result && result.success) {
        document.getElementById('addMemberUsername').value = '';
        loadMembers();
    } else {
        alert(result ? result.message : 'Failed to add member');
    }
}

async function removeMember(memberId) {
    if (!confirm('Remove this member?')) return;
    const result = await API.del('/api/groups/' + currentGroupId + '/members/' + memberId);
    if (result && result.success) loadMembers();
    else alert(result ? result.message : 'Failed');
}

async function loadSplits() {
    const container = document.getElementById('splitsContent');
    container.innerHTML = '<div class="empty-msg">Loading...</div>';

    const result = await API.get('/api/groups/' + currentGroupId + '/splits');
    if (!result || !result.success || !result.splits) {
        container.innerHTML = '<div class="empty-msg">No split data</div>';
        return;
    }

    const s = result.splits;
    const members = s.members || [];
    const settlements = s.settlements || [];
    const expenses = s.expenses || [];
    const groupTotal = s.groupTotal || 0;
    const perPerson = s.perPerson || 0;

    let html = '';

    // Summary cards
    html += `<div class="split-summary">
        <div class="split-card"><div class="split-val">₹${groupTotal.toLocaleString('en-IN')}</div><div class="split-lbl">Group Total</div></div>
        <div class="split-card"><div class="split-val">₹${perPerson.toLocaleString('en-IN')}</div><div class="split-lbl">Per Person</div></div>
        <div class="split-card"><div class="split-val">${members.length}</div><div class="split-lbl">Members</div></div>
    </div>`;

    // Per-person breakdown
    if (members.length > 0) {
        html += '<div class="section-title"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg> Per Person Breakdown</div>';
        members.forEach(m => {
            const initial = (m.fullName || m.username || '?').charAt(0).toUpperCase();
            const net = m.netBalance || 0;
            const paidBar = groupTotal > 0 ? Math.min(m.totalPaid / groupTotal * 100, 100) : 0;
            const shareBar = groupTotal > 0 ? Math.min(m.totalShare / groupTotal * 100, 100) : 0;
            let statusColor = '#6b7280';
            let statusText = 'Settled';
            if (net > 0.01) { statusColor = '#10b981'; statusText = 'Gets ₹' + net.toLocaleString('en-IN'); }
            else if (net < -0.01) { statusColor = '#ef4444'; statusText = 'Owes ₹' + Math.abs(net).toLocaleString('en-IN'); }

            html += `<div class="person-card">
                <div class="person-top">
                    <div class="person-avatar">${initial}</div>
                    <div class="person-name">${escapeHtml(m.fullName || m.username)}</div>
                    <div class="person-status" style="color:${statusColor}">${statusText}</div>
                </div>
                <div class="person-bars">
                    <div class="bar-group">
                        <label>Paid <span>₹${m.totalPaid.toLocaleString('en-IN')}</span></label>
                        <div class="bar-track"><div class="bar-fill" style="width:${paidBar}%;background:#6366f1"></div></div>
                    </div>
                    <div class="bar-group">
                        <label>Share <span>₹${m.totalShare.toLocaleString('en-IN')}</span></label>
                        <div class="bar-track"><div class="bar-fill" style="width:${shareBar}%;background:#f59e0b"></div></div>
                    </div>
                </div>
            </div>`;
        });
    }

    // Settlements
    if (settlements.length > 0) {
        html += '<div class="section-title"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg> Who Pays Whom</div>';
        settlements.forEach(st => {
            html += `<div class="settle-item">
                <div class="settle-from">${escapeHtml(st.fromName)}</div>
                <div class="settle-arrow">→</div>
                <div class="settle-amt">₹${st.amount.toLocaleString('en-IN')}</div>
                <div class="settle-arrow">→</div>
                <div class="settle-to">${escapeHtml(st.toName)}</div>
            </div>`;
        });
    } else if (members.length > 0) {
        html += '<div class="empty-msg" style="margin-top:8px">All settled up!</div>';
    }

    // Expenses
    if (expenses.length > 0) {
        const catColors = { Food:'#f59e0b',Transport:'#6366f1',Entertainment:'#ec4899',Shopping:'#10b981',Bills:'#ef4444',Health:'#0ea5e9',Education:'#8b5cf6',Travel:'#06b6d4',General:'#6b7280' };
        html += '<div class="section-title" style="margin-top:16px"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg> Expenses</div>';
        expenses.forEach(e => {
            const color = catColors[e.category] || '#6b7280';
            const splitInfo = (e.splits || []).map(sp => {
                const member = members.find(m => m.id === sp.userId);
                const name = member ? (member.fullName || member.username).split(' ')[0] : 'User';
                return `${name}: ₹${sp.amount.toLocaleString('en-IN')}`;
            }).join(' · ');
            const date = new Date(e.createdAt).toLocaleDateString('en-IN', { day:'numeric', month:'short', year:'numeric' });
            html += `<div class="exp-item">
                <div class="exp-top">
                    <div><span class="exp-desc">${escapeHtml(e.description)}</span><span class="exp-cat" style="background:${color}18;color:${color}">${e.category}</span></div>
                    <div class="exp-amount">₹${e.amount.toLocaleString('en-IN')}</div>
                </div>
                <div class="exp-meta">Paid by ${escapeHtml(e.paidByName)} · ${date}</div>
                <div class="exp-splits">${splitInfo}</div>
            </div>`;
        });
    }

    container.innerHTML = html || '<div class="empty-msg">No expenses yet</div>';
}

async function loadBalances() {
    const container = document.getElementById('balancesList');
    container.innerHTML = '<div class="empty-msg">Loading...</div>';
    const result = await API.get('/api/groups/' + currentGroupId + '/balances');
    const user = API.getUser();

    if (!result || !result.success || !result.balances || result.balances.length === 0) {
        container.innerHTML = '<div class="empty-msg">No balances yet</div>';
        return;
    }

    container.innerHTML = result.balances.map(b => {
        const initial = (b.fullName || b.username || '?').charAt(0).toUpperCase();
        const net = b.netBalance || 0;
        let color = 'var(--sub)';
        let text = 'Settled';
        if (net > 0) { color = '#10b981'; text = 'Gets ₹' + Math.abs(net).toLocaleString('en-IN'); }
        else if (net < 0) { color = '#ef4444'; text = 'Owes ₹' + Math.abs(net).toLocaleString('en-IN'); }
        return `<div class="member-item">
            <div class="member-info">
                <div class="member-avatar">${initial}</div>
                <div>
                    <div class="member-name">${escapeHtml(b.fullName || b.username)}</div>
                    <div class="member-role" style="color:${color}">${text}</div>
                </div>
            </div>
        </div>`;
    }).join('');
}

async function loadWalletTransactions() {
    const container = document.getElementById('walletTransactions');
    container.innerHTML = '<div class="empty-msg">Loading...</div>';
    const result = await API.get('/api/wallet/' + currentGroupId + '/transactions');

    if (!result || !result.success || !result.transactions || result.transactions.length === 0) {
        container.innerHTML = '<div class="empty-msg">No transactions</div>';
        return;
    }

    container.innerHTML = result.transactions.map(t => `
        <div class="wallet-transaction">
            <div>
                <span class="txn-type">${t.type}</span>
                <span style="color:var(--sub);margin-left:6px">${escapeHtml(t.userName)}</span>
            </div>
            <span class="txn-amount ${t.type}">${t.type === 'withdrawal' ? '-' : '+'}₹${(t.amount || 0).toLocaleString('en-IN')}</span>
        </div>
    `).join('');
}

async function depositToWallet() {
    const amount = parseFloat(document.getElementById('depositAmount').value);
    if (!amount || amount <= 0) return;
    await API.post('/api/wallet/' + currentGroupId + '/deposit', { amount });
    document.getElementById('depositAmount').value = '';
    openGroupDetail(currentGroupId);
}

async function withdrawFromWallet() {
    const amount = parseFloat(document.getElementById('withdrawAmount').value);
    if (!amount || amount <= 0) return;
    await API.post('/api/wallet/' + currentGroupId + '/withdraw', { amount });
    document.getElementById('withdrawAmount').value = '';
    openGroupDetail(currentGroupId);
}

function showAddExpense() { document.getElementById('addExpenseForm').style.display = 'block'; }
function hideAddExpense() { document.getElementById('addExpenseForm').style.display = 'none'; }

async function submitExpense() {
    const description = document.getElementById('expenseDesc').value.trim();
    const amount = parseFloat(document.getElementById('expenseAmount').value);
    const category = document.getElementById('expenseCategory').value;
    if (!description || !amount || amount <= 0) return;
    const result = await API.post('/api/expenses', { groupId: currentGroupId, description, amount, category, splitType: 'equal' });
    if (result && result.success) {
        document.getElementById('expenseDesc').value = '';
        document.getElementById('expenseAmount').value = '';
        hideAddExpense();
        loadGroupExpenses();
    } else {
        alert(result ? result.message : 'Failed');
    }
}

async function loadGroupExpenses() {
    const container = document.getElementById('groupExpenses');
    container.innerHTML = '<div class="empty-msg">Loading...</div>';
    const result = await API.get('/api/groups/' + currentGroupId + '/expenses');

    if (!result || !result.success || !result.expenses || result.expenses.length === 0) {
        container.innerHTML = '<div class="empty-msg">No expenses</div>';
        return;
    }

    const catColors = { Food:'#f59e0b',Transport:'#6366f1',Entertainment:'#ec4899',Shopping:'#10b981',Bills:'#ef4444',Health:'#0ea5e9',Education:'#8b5cf6',Travel:'#06b6d4',General:'#6b7280' };
    container.innerHTML = result.expenses.map(e => {
        const color = catColors[e.category] || '#6b7280';
        const date = new Date(e.createdAt).toLocaleDateString('en-IN', { day:'numeric', month:'short', year:'numeric' });
        return `<div class="exp-item">
            <div class="exp-top">
                <div><span class="exp-desc">${escapeHtml(e.description)}</span><span class="exp-cat" style="background:${color}18;color:${color}">${e.category}</span></div>
                <div class="exp-amount">₹${(e.amount || 0).toLocaleString('en-IN')}</div>
            </div>
            <div class="exp-meta">Paid by ${escapeHtml(e.paidByName)} · ${date}</div>
        </div>`;
    }).join('');
}

function closeModal(id) { document.getElementById(id).style.display = 'none'; }
function escapeHtml(str) { if (!str) return ''; const d = document.createElement('div'); d.textContent = str; return d.innerHTML; }

function openEditGroup(id, name, description) {
    document.getElementById('editGroupId').value = id;
    document.getElementById('editGroupName').value = name;
    document.getElementById('editGroupDesc').value = description;
    document.getElementById('editGroupModal').style.display = 'flex';
}
