
/**
 * SCRIPT.JS - OptiStock Core & UI Logic
 */

document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    highlightActiveLink();
});

// ===== DASHBOARD CONTROLLER =====
function initDashboard() {
    const stats = getStats();

    // Render Stats Cards
    updateText('stat-productos', stats.totalProductos);
    updateText('stat-facturas', stats.totalFacturas);
    updateText('stat-ingresos', formatPrice(stats.ingresosTotal));
    updateText('stat-stock-bajo', stats.productosStockBajo);

    // Render Stock Table
    const stockContainer = document.getElementById('stock-bajo-lista');
    const lowStock = getLowStock();

    if (stockContainer) {
        if (!lowStock.length) {
            stockContainer.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon"></div>
                    <p class="empty-state-text">Todos los productos tienen stock suficiente</p>
                </div>`;
        } else {
            stockContainer.innerHTML = `
                <table>
                    <thead><tr><th>Producto</th><th>Stock</th></tr></thead>
                    <tbody>
                        ${lowStock.map(p => `
                            <tr>
                                <td>${p.nombre}</td>
                                <td><span class="badge badge-warning">${p.cantidad} unidades</span></td>
                            </tr>`).join('')}
                    </tbody>
                </table>`;
        }
    }

    // Render Recent Activity
    const activityContainer = document.getElementById('actividad-lista');
    const recent = getInvoices().slice(-5).reverse();

    if (activityContainer) {
        if (!recent.length) {
            activityContainer.innerHTML = `
                <li class="activity-item">
                    <div class="activity-icon"></div>
                    <div class="activity-content">
                        <div class="activity-title">Sistema inicializado</div>
                        <div class="activity-time">Sin actividad reciente</div>
                    </div>
                </li>`;
        } else {
            activityContainer.innerHTML = recent.map(f => `
                <li class="activity-item">
                    <div class="activity-icon"></div>
                    <div class="activity-content">
                        <div class="activity-title">Factura #${f.id} - ${f.cliente || 'Cliente General'}</div>
                        <div class="activity-time">${formatDate(f.fecha)}</div>
                    </div>
                </li>`).join('');
        }
    }
}

function updateText(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val;
}

// ===== NAVIGATION =====
function initNavigation() {
    const currentPage = window.location.pathname.split('/').pop();
    document.querySelectorAll('.nav-section').forEach(section => {
        const links = section.querySelectorAll('.nav-link');
        let shouldActivate = false;

        links.forEach(link => {
            const href = link.getAttribute('href');
            if (href === currentPage || href.endsWith('/' + currentPage)) {
                shouldActivate = true;
                link.classList.add('active');
            }
        });

        // Solo agregar active si no ya tiene la clase desde HTML
        if (shouldActivate && !section.classList.contains('active')) {
            section.classList.add('active');
        }
    });
}

function toggleSection(id) {
    const section = document.getElementById(id);
    if (section) {
        section.classList.toggle('active');
    }
}

function highlightActiveLink() {
    const page = window.location.pathname.split('/').pop() || 'Index.html';
    document.querySelectorAll('.nav-link').forEach(link => {
        const href = link.getAttribute('href');
        if (href === page || href.endsWith('/' + page)) link.classList.add('active');
        else link.classList.remove('active');
    });
}

function toggleSidebar() {
    document.querySelector('.sidebar')?.classList.toggle('active');
}

// ===== UI UTILITIES =====
function showNotification(msg, type = 'success') {
    const div = document.createElement('div');
    div.className = `alert alert-${type}`;
    Object.assign(div.style, { position: 'fixed', top: '20px', right: '20px', zIndex: 10000, animation: 'slideIn 0.3s' });
    div.textContent = msg;
    document.body.appendChild(div);
    setTimeout(() => {
        div.style.animation = 'slideOut 0.3s';
        setTimeout(() => div.remove(), 290);
    }, 3000);
}

function validateForm(id) {
    const form = document.getElementById(id);
    if (!form) return false;
    let valid = true;
    form.querySelectorAll('[required]').forEach(input => {
        const empty = !input.value.trim();
        input.style.borderColor = empty ? 'var(--color-error)' : 'var(--color-gris)';
        if (empty) valid = false;
    });
    return valid;
}

function filterTable(inputId, tableId) {
    const term = document.getElementById(inputId)?.value.toLowerCase();
    if (!term) return;
    document.querySelectorAll(`#${tableId} tbody tr`).forEach(row => {
        row.style.display = row.textContent.toLowerCase().includes(term) ? '' : 'none';
    });
}

function exportDataJSON() {
    const data = { products: getProducts(), invoices: getInvoices(), date: new Date() };
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `optistock_backup_${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    showNotification('Datos exportados correctamente');
}
