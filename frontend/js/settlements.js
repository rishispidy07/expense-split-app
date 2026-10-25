document.addEventListener('DOMContentLoaded', async () => {
    if (!API.requireAuth()) return;

    const user = API.getUser();
    document.getElementById('userName').textContent = user ? user.fullName || user.username : '';

    document.getElementById('logoutBtn').addEventListener('click', async () => {
        await API.post('/api/auth/logout');
        API.clearToken();
        window.location.href = '/login.html';
    });

    document.getElementById('newSettlementBtn').addEventListener('click', async () => {
        await loadUsersForSettlement();
        document.getElementById('newSettlementModal').style.display = 'flex';
    });

    document.getElementById('settlementForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const toUserId = parseInt(document.getElementById('settleTo').value);
        const amount = parseFloat(document.getElementById('settleAmount').value);
        if (!toUserId || !amount || amount <= 0) return;

        const result = await API.post('/api/settlements', { toUserId, amount });
        if (result && result.success) {
            closeModal('newSettlementModal');
            document.getElementById('settleTo').value = '';
            document.getElementById('settleAmount').value = '';
            loadSettlements();
        } else {
            alert(result ? result.message : 'Failed');
        }
    });

    loadSettlements();
});

async function loadSettlements() {
    const result = await API.get('/api/settlements');
    const container = document.getElementById('settlementsList');

    if (!result || !result.success || !result.settlements || result.settlements.length === 0) {
        container.innerHTML = '<p class="empty-state">No settlements yet</p>';
        return;
    }

    const user = API.getUser();
    container.innerHTML = result.settlements.map(s => {
        const isFromMe = s.fromUserId === (user ? user.id : 0);
        const otherName = isFromMe ? s.toUserName : s.fromUserName;
        return '<div class="settlement-item"><div class="settlement-info"><p>' + (isFromMe ? 'You paid' : 'Received from') + ' <strong>' + escapeHtml(otherName) + '</strong></p><div class="settlement-amount">₹' + (s.amount || 0).toLocaleString('en-IN') + '</div><p>' + (s.createdAt || '') + '</p></div><span class="status-badge ' + s.status + '">' + s.status + '</span></div>';
    }).join('');
}

async function loadUsersForSettlement() {
    const result = await API.get('/api/users');
    const select = document.getElementById('settleTo');
    select.innerHTML = '<option value="">Select user...</option>';
    if (result && result.success && result.users) {
        const user = API.getUser();
        result.users.filter(u => u.id !== (user ? user.id : 0)).forEach(u => {
            const opt = document.createElement('option');
            opt.value = u.id;
            opt.textContent = u.fullName || u.username;
            select.appendChild(opt);
        });
    }
}

function closeModal(id) { document.getElementById(id).style.display = 'none'; }
function escapeHtml(str) { if (!str) return ''; const d = document.createElement('div'); d.textContent = str; return d.innerHTML; }
