document.addEventListener('DOMContentLoaded', async () => {
    if (!API.requireAuth()) return;

    const user = API.getUser();
    document.getElementById('userName').textContent = user ? user.fullName || user.username : '';

    document.getElementById('logoutBtn').addEventListener('click', async () => {
        await API.post('/api/auth/logout');
        API.clearToken();
        window.location.href = '/login.html';
    });

    await loadProfile();
});

async function loadProfile() {
    const res = await API.get('/api/user/profile');
    if (res && res.success && res.profile) {
        const p = res.profile;
        document.getElementById('profileAvatar').textContent = (p.fullName || p.username || 'U').charAt(0).toUpperCase();
        document.getElementById('profileName').textContent = p.fullName || p.username;
        document.getElementById('profileEmail').textContent = p.email;
        document.getElementById('statGroups').textContent = p.groupCount || 0;
        document.getElementById('statExpenses').textContent = p.expenseCount || 0;
        document.getElementById('statTotal').textContent = '\u20B9' + Number(p.totalSpent || 0).toLocaleString('en-IN');
        document.getElementById('infoUsername').textContent = p.username;
        document.getElementById('infoFullName').textContent = p.fullName || '-';
        document.getElementById('infoEmail').textContent = p.email;
        document.getElementById('infoPhone').textContent = p.phone || '-';

        document.getElementById('editUsername').value = p.username || '';
        document.getElementById('editFullName').value = p.fullName || '';
        document.getElementById('editEmail').value = p.email || '';
        document.getElementById('editPhone').value = p.phone || '';
    }
}

async function openEditModal() {
    const res = await API.get('/api/user/profile');
    if (res && res.success && res.profile) {
        const p = res.profile;
        document.getElementById('editUsername').value = p.username || '';
        document.getElementById('editFullName').value = p.fullName || '';
        document.getElementById('editEmail').value = p.email || '';
        document.getElementById('editPhone').value = p.phone || '';
    }
    document.getElementById('editModal').classList.add('active');
}

function closeEditModal() {
    document.getElementById('editModal').classList.remove('active');
    const modal = document.querySelector('.modal');
    modal.classList.remove('maximized');
    updateToggleIcon(false);
}

function toggleModalSize() {
    const modal = document.querySelector('.modal');
    const isMaximized = modal.classList.toggle('maximized');
    updateToggleIcon(isMaximized);
}

function updateToggleIcon(isMaximized) {
    const icon = document.getElementById('toggleIcon');
    const btn = document.getElementById('toggleBtn');
    if (isMaximized) {
        icon.innerHTML = '<polyline points="4 14 10 14 10 20"/><polyline points="20 10 14 10 14 4"/><line x1="14" y1="10" x2="21" y2="3"/><line x1="3" y1="21" x2="10" y2="14"/>';
        btn.title = 'Minimize';
    } else {
        icon.innerHTML = '<polyline points="15 3 21 3 21 9"/><polyline points="9 21 3 21 3 15"/><line x1="21" y1="3" x2="14" y2="10"/><line x1="3" y1="21" x2="10" y2="14"/>';
        btn.title = 'Maximize';
    }
}

async function saveProfile() {
    const btn = document.getElementById('saveProfileBtn');
    const username = document.getElementById('editUsername').value.trim();
    const fullName = document.getElementById('editFullName').value.trim();
    const email = document.getElementById('editEmail').value.trim();
    const phone = document.getElementById('editPhone').value.trim();

    if (!username) { alert('Username is required'); return; }
    if (!fullName) { alert('Full name is required'); return; }
    if (!email) { alert('Email is required'); return; }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { alert('Invalid email format'); return; }

    btn.disabled = true;
    btn.textContent = 'Saving...';

    try {
        const res = await API.put('/api/user/profile', { username, fullName, email, phone });
        if (res && res.success) {
            const user = API.getUser();
            if (user) {
                user.username = username;
                user.fullName = fullName;
                user.email = email;
                user.phone = phone;
                localStorage.setItem('user', JSON.stringify(user));
            }
            document.getElementById('userName').textContent = fullName || username;
            await loadProfile();
            closeEditModal();
        } else {
            alert(res ? (res.error || res.message) : 'Failed to update profile');
        }
    } catch (err) {
        alert('Error updating profile');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Save Changes';
    }
}
