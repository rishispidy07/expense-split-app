document.addEventListener('DOMContentLoaded', () => {
    if (!API.requireAuth()) return;

    const user = API.getUser();
    document.getElementById('userName').textContent = user ? user.fullName || user.username : '';
    document.getElementById('logoutBtn').addEventListener('click', async () => {
        await API.post('/api/auth/logout');
        API.clearToken();
        window.location.href = '/login.html';
    });

    const now = new Date();
    const yearSel = document.getElementById('yearSelect');
    const monthSel = document.getElementById('monthSelect');
    const currentYear = now.getFullYear();
    for (let y = currentYear; y >= currentYear - 3; y--) {
        const o = document.createElement('option'); o.value = y; o.textContent = y;
        yearSel.appendChild(o);
    }
    yearSel.value = currentYear;
    monthSel.value = now.getMonth() + 1;

    const COLORS = ['#6366f1','#f59e0b','#10b981','#ef4444','#ec4899','#0ea5e9','#8b5cf6','#06b6d4','#16a34a','#b45309','#dc2626','#6b7280'];
    const catColorMap = {
        Food:'#f59e0b',Transport:'#6366f1',Entertainment:'#ec4899',Shopping:'#10b981',
        Bills:'#ef4444',Health:'#0ea5e9',Education:'#8b5cf6',Travel:'#06b6d4',
        Groceries:'#16a34a',Coffee:'#b45309',Rent:'#dc2626',General:'#6b7280',Income:'#10b981'
    };
    const catIcons = {
        Food:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M18 8h1a4 4 0 0 1 0 8h-1"/><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/></svg>',
        Transport:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><rect x="1" y="3" width="15" height="13" rx="2"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>',
        Entertainment:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><rect x="2" y="2" width="20" height="20" rx="2.18"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/></svg>',
        Shopping:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>',
        Bills:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>',
        Health:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>',
        Education:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>',
        Travel:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/></svg>',
        Groceries:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/></svg>',
        Coffee:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M18 8h1a4 4 0 0 1 0 8h-1"/><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/></svg>',
        Rent:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>',
        General:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/></svg>'
    };

    const MONTH_NAMES = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
    const FULL_MONTH_NAMES = ['January','February','March','April','May','June','July','August','September','October','November','December'];

    let charts = { cat: null, monthly: null, group: null, daily: null };

    function getDaysInMonth(year, month) {
        return new Date(year, month, 0).getDate();
    }

    function getMonthName(m) {
        return MONTH_NAMES[m - 1] || '';
    }

    function getPeriodLabel(year, month) {
        if (month === 0) return 'Full Year ' + year;
        return FULL_MONTH_NAMES[month - 1] + ' ' + year;
    }

    function updateChartTitles(year, month) {
        const period = getPeriodLabel(year, month);
        const monthlyCard = document.getElementById('monthlyCard');
        const dailyCard = document.getElementById('dailyCard');
        if (monthlyCard) {
            if (month === 0) {
                monthlyCard.querySelector('h3').textContent = 'Monthly Trend';
                monthlyCard.style.display = '';
            } else {
                monthlyCard.querySelector('h3').textContent = 'Month Summary - ' + period;
                monthlyCard.style.display = '';
            }
        }
        if (dailyCard) {
            if (month === 0) {
                dailyCard.querySelector('h3').textContent = 'Monthly Breakdown - ' + year;
            } else {
                dailyCard.querySelector('h3').textContent = 'Daily Spending - ' + period;
            }
        }
        const catCard = document.getElementById('catCard');
        if (catCard) catCard.querySelector('h3').textContent = 'Spending by Category - ' + period;
        const groupCard = document.getElementById('groupCard');
        if (groupCard) groupCard.querySelector('h3').textContent = 'Spending by Group - ' + period;
    }

    async function loadCharts() {
        const year = parseInt(yearSel.value);
        const month = parseInt(monthSel.value);

        updateChartTitles(year, month);

        const [sumRes, catRes, monthRes, grpRes, dailyRes] = await Promise.all([
            API.get(`/api/stats/summary?year=${year}&month=${month}`),
            API.get(`/api/stats/category?year=${year}&month=${month}`),
            API.get(`/api/stats/monthly?year=${year}`),
            API.get(`/api/stats/groups?year=${year}&month=${month}`),
            API.get(`/api/stats/daily?year=${year}&month=${month}`)
        ]);

        if (sumRes && sumRes.success) {
            const total = sumRes.summary.total || 0;
            const count = sumRes.summary.count || 0;
            document.getElementById('totalSpent').textContent = '₹' + total.toLocaleString('en-IN');
            document.getElementById('totalTxns').textContent = count;
            document.getElementById('avgSpend').textContent = '₹' + (count > 0 ? Math.round(total / count).toLocaleString('en-IN') : '0');
        }

        if (catRes && catRes.success) {
            renderCategory(catRes.categories);
            renderTopCategories(catRes.categories);
        }
        if (monthRes && monthRes.success) renderMonthly(monthRes.months, year, month);
        if (grpRes && grpRes.success) renderGroup(grpRes.groups);
        if (dailyRes && dailyRes.success) renderDaily(dailyRes.daily, year, month);
    }

    function renderCategory(data) {
        const filtered = (data || []).filter(d => d.category !== 'Income');
        const canvas = document.getElementById('categoryChart');
        if (!canvas) return;
        
        if (filtered.length === 0) {
            document.getElementById('catLegend').innerHTML = '<div style="padding:16px;text-align:center;color:var(--muted);font-size:13px">No expenses yet</div>';
            if (charts.cat) { charts.cat.destroy(); charts.cat = null; }
            canvas.style.display = 'none';
            return;
        }
        
        canvas.style.display = 'block';
        const labels = filtered.map(d => d.category || 'General');
        const values = filtered.map(d => d.total);
        const colors = labels.map((l, i) => catColorMap[l] || COLORS[i % COLORS.length]);
        const total = values.reduce((a, b) => a + b, 0);

        if (charts.cat) charts.cat.destroy();
        charts.cat = new Chart(canvas, {
            type: 'doughnut',
            data: { labels, datasets: [{ data: values, backgroundColor: colors, borderWidth: 3, borderColor: '#fff', hoverBorderWidth: 0, hoverOffset: 8 }] },
            options: {
                responsive: true, maintainAspectRatio: false, cutout: '65%',
                plugins: { legend: { display: false }, tooltip: { callbacks: { label: ctx => ctx.label + ': ₹' + ctx.raw.toLocaleString('en-IN') + ' (' + Math.round(ctx.raw / total * 100) + '%)' } } }
            }
        });

        document.getElementById('catLegend').innerHTML = filtered.map((d, i) => {
            const color = catColorMap[d.category] || COLORS[i % COLORS.length];
            const pct = total > 0 ? Math.round(d.total / total * 100) : 0;
            return `<div class="legend-item"><div class="legend-dot" style="background:${color}"></div>${d.category} ${pct}%</div>`;
        }).join('');
    }

    function renderMonthly(data, year, selectedMonth) {
        const map = {}; (data || []).forEach(d => { map[d.month] = d.total; });
        const values = MONTH_NAMES.map((_, i) => map[i + 1] || 0);
        const maxVal = Math.max(...values, 1);

        let bgColors;
        if (selectedMonth > 0) {
            bgColors = values.map((v, i) => (i + 1) === selectedMonth ? '#6366f1' : 'rgba(196,181,253,.4)');
        } else {
            bgColors = values.map(v => v === maxVal ? '#6366f1' : '#c4b5fd');
        }

        if (charts.monthly) charts.monthly.destroy();
        charts.monthly = new Chart(document.getElementById('monthlyChart'), {
            type: 'bar',
            data: {
                labels: MONTH_NAMES,
                datasets: [{
                    label: 'Spent',
                    data: values,
                    backgroundColor: bgColors,
                    borderRadius: 6, borderSkipped: false, barPercentage: 0.6
                }]
            },
            options: {
                responsive: true, maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            title: ctx => FULL_MONTH_NAMES[ctx[0].dataIndex] + ' ' + year,
                            label: ctx => '₹' + ctx.raw.toLocaleString('en-IN')
                        }
                    }
                },
                scales: { y: { beginAtZero: true, grid: { color: '#f1f5f9' }, ticks: { callback: v => v >= 1000 ? (v/1000)+'k' : v } }, x: { grid: { display: false } } }
            }
        });
    }

    function renderGroup(data) {
        const canvas = document.getElementById('groupChart');
        if (!data || data.length === 0) {
            if (charts.group) { charts.group.destroy(); charts.group = null; }
            canvas.style.display = 'none';
            let emptyDiv = canvas.parentElement.querySelector('.empty-state');
            if (!emptyDiv) {
                emptyDiv = document.createElement('div');
                emptyDiv.className = 'empty-state';
                emptyDiv.innerHTML = '<svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="opacity:.3;margin-bottom:8px"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg><p>No group expenses for this period</p>';
                canvas.parentElement.appendChild(emptyDiv);
            }
            return;
        }
        canvas.style.display = 'block';
        const existingEmpty = canvas.parentElement.querySelector('.empty-state');
        if (existingEmpty) existingEmpty.remove();

        const labels = data.map(d => d.name);
        const values = data.map(d => d.total);

        if (charts.group) charts.group.destroy();
        charts.group = new Chart(canvas, {
            type: 'bar',
            data: { labels, datasets: [{ data: values, backgroundColor: COLORS.slice(0, values.length), borderRadius: 6, borderSkipped: false, barPercentage: 0.7 }] },
            options: {
                indexAxis: 'y', responsive: true, maintainAspectRatio: false,
                plugins: { legend: { display: false }, tooltip: { callbacks: { label: ctx => '₹' + ctx.raw.toLocaleString('en-IN') } } },
                scales: { x: { beginAtZero: true, grid: { color: '#f1f5f9' }, ticks: { callback: v => v >= 1000 ? (v/1000)+'k' : v } }, y: { grid: { display: false } } }
            }
        });
    }

    function renderTopCategories(data) {
        const container = document.getElementById('topCategories');
        const filtered = (data || []).filter(d => d.category !== 'Income');
        if (filtered.length === 0) {
            container.innerHTML = '<div class="empty-state">No data</div>';
            return;
        }

        const sorted = filtered.sort((a, b) => b.total - a.total);
        const maxAmount = sorted[0].total;
        const total = sorted.reduce((s, d) => s + d.total, 0);

        container.innerHTML = sorted.map((d, i) => {
            const cat = d.category || 'General';
            const icon = catIcons[cat] || catIcons.General;
            const color = catColorMap[cat] || COLORS[i % COLORS.length];
            const pct = total > 0 ? Math.round(d.total / total * 100) : 0;
            const barWidth = maxAmount > 0 ? (d.total / maxAmount * 100) : 0;
            const rankClass = i === 0 ? 'rank-1' : i === 1 ? 'rank-2' : i === 2 ? 'rank-3' : 'rank-n';
            return `<div class="cat-rank">
                <div class="rank-num ${rankClass}">${i + 1}</div>
                <div class="rank-icon" style="background:${color}20;color:${color}">${icon}</div>
                <div class="rank-info">
                    <div class="rank-name">${cat}</div>
                    <div class="rank-bar-wrap"><div class="rank-bar" style="width:${barWidth}%;background:${color}"></div></div>
                </div>
                <div class="rank-amt">
                    <div class="rank-val">₹${d.total.toLocaleString('en-IN')}</div>
                    <div class="rank-pct">${pct}%</div>
                </div>
            </div>`;
        }).join('');
    }

    function renderDaily(dailyData, year, month) {
        const canvas = document.getElementById('dailyChart');
        if (charts.daily) { charts.daily.destroy(); charts.daily = null; }

        if (month === 0) {
            const monthMap = {};
            (dailyData || []).forEach(d => {
                const m = d.month;
                if (!monthMap[m]) monthMap[m] = 0;
                monthMap[m] += d.total || 0;
            });
            const labels = [];
            const values = [];
            for (let i = 1; i <= 12; i++) {
                labels.push(MONTH_NAMES[i - 1]);
                values.push(monthMap[i] || 0);
            }
            const maxVal = Math.max(...values, 1);

            charts.daily = new Chart(canvas, {
                type: 'bar',
                data: {
                    labels,
                    datasets: [{
                        label: 'Monthly Total',
                        data: values,
                        backgroundColor: values.map(v => v === maxVal ? '#6366f1' : '#c4b5fd'),
                        borderRadius: 6, borderSkipped: false, barPercentage: 0.6
                    }]
                },
                options: {
                    responsive: true, maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            callbacks: {
                                title: ctx => FULL_MONTH_NAMES[ctx[0].dataIndex] + ' ' + year,
                                label: ctx => '₹' + ctx.raw.toLocaleString('en-IN')
                            }
                        }
                    },
                    scales: { y: { beginAtZero: true, grid: { color: '#f1f5f9' }, ticks: { callback: v => v >= 1000 ? (v/1000)+'k' : v } }, x: { grid: { display: false } } }
                }
            });
        } else {
            const daysInMonth = getDaysInMonth(year, month);
            const dayMap = {};
            (dailyData || []).forEach(d => {
                const day = d.day;
                if (!dayMap[day]) dayMap[day] = 0;
                dayMap[day] += d.total || 0;
            });
            const labels = [];
            const values = [];
            for (let i = 1; i <= daysInMonth; i++) {
                labels.push(i);
                values.push(dayMap[i] || 0);
            }
            const maxVal = Math.max(...values, 1);

            charts.daily = new Chart(canvas, {
                type: 'line',
                data: {
                    labels,
                    datasets: [{
                        label: 'Daily Spending',
                        data: values,
                        borderColor: '#10b981', backgroundColor: 'rgba(16,185,129,.1)',
                        fill: true, tension: 0.35, pointRadius: 3, pointHoverRadius: 6, borderWidth: 2.5,
                        pointBackgroundColor: values.map(v => v === maxVal ? '#10b981' : '#fff'),
                        pointBorderColor: '#10b981',
                        pointBorderWidth: values.map(v => v === maxVal ? 3 : 2)
                    }]
                },
                options: {
                    responsive: true, maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            callbacks: {
                                title: ctx => getMonthName(month) + ' ' + ctx[0].label + ', ' + year,
                                label: ctx => ctx.raw > 0 ? '₹' + ctx.raw.toLocaleString('en-IN') : 'No spending'
                            }
                        }
                    },
                    scales: {
                        y: { beginAtZero: true, grid: { color: '#f1f5f9' }, ticks: { callback: v => v >= 1000 ? (v/1000)+'k' : v } },
                        x: { grid: { display: false }, ticks: { maxTicksLimit: daysInMonth <= 10 ? daysInMonth : 15 } }
                    }
                }
            });
        }
    }

    yearSel.addEventListener('change', loadCharts);
    monthSel.addEventListener('change', loadCharts);
    loadCharts();
});
