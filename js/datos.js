// ===== DATOS.JS =====
/**
 * Core Data Management
 * Productos : API REST /api/productos
 * Facturas  : API REST /api/facturas   ✅ migrado desde localStorage
 * Clientes  : API REST /api/clientes   ✅ nuevo
 */

const CONFIG = {
    IVA: 0.19,
    MIN_STOCK: 5,
    API_BASE: 'http://localhost:8080/api'
};

// ─── CACHE LOCAL ──────────────────────────────────────────────────────────────
let _productosCache = [];
let _facturasCache = [];
let _clientesCache = [];

// ═══════════════════════════════════════════════════════════════════════════════
// PRODUCTOS
// ═══════════════════════════════════════════════════════════════════════════════

async function loadProducts() {
    try {
        const res = await fetch(`${CONFIG.API_BASE}/productos`);
        if (!res.ok) throw new Error('Error al cargar productos');
        _productosCache = await res.json();
        return _productosCache;
    } catch (e) {
        console.error('[datos.js] loadProducts:', e);
        return _productosCache;
    }
}

const getProducts = () => _productosCache;
const getProduct = (id) => _productosCache.find(p => p.id === id);
const getLowStock = () => _productosCache.filter(p => (p.cantidad ?? 0) <= CONFIG.MIN_STOCK);

const addProduct = async (dto) => {
    const res = await fetch(`${CONFIG.API_BASE}/productos`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            nombre: dto.nombre,
            precio: dto.precio,
            cantidad: dto.cantidad,
            categoria: dto.categoria,
            descripcion: dto.descripcion
        })
    });
    if (!res.ok) throw new Error('Error al crear producto');
    const nuevo = await res.json();
    _productosCache.push(nuevo);
    return nuevo;
};

const updateProduct = async (id, data) => {
    const res = await fetch(`${CONFIG.API_BASE}/productos/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            nombre: data.nombre,
            precio: data.precio,
            cantidad: data.cantidad,
            categoria: data.categoria,
            descripcion: data.descripcion
        })
    });
    if (!res.ok) throw new Error('Error al actualizar producto');
    const actualizado = await res.json();
    const idx = _productosCache.findIndex(p => p.id === id);
    if (idx > -1) _productosCache[idx] = actualizado;
    return actualizado;
};

const deleteProduct = async (id) => {
    const res = await fetch(`${CONFIG.API_BASE}/productos/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Error al eliminar producto');
    _productosCache = _productosCache.filter(p => p.id !== id);
};

// ═══════════════════════════════════════════════════════════════════════════════
// CLIENTES  (API REST)
// ═══════════════════════════════════════════════════════════════════════════════

async function loadClientes() {
    try {
        const res = await fetch(`${CONFIG.API_BASE}/clientes`);
        if (!res.ok) throw new Error('Error al cargar clientes');
        _clientesCache = await res.json();
        return _clientesCache;
    } catch (e) {
        console.error('[datos.js] loadClientes:', e);
        return _clientesCache;
    }
}

const getClientes = () => _clientesCache;
const getCliente = (id) => _clientesCache.find(c => c.idCliente === id);

const addCliente = async (dto) => {
    const res = await fetch(`${CONFIG.API_BASE}/clientes`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dto)
    });
    if (!res.ok) throw new Error('Error al crear cliente');
    const nuevo = await res.json();
    _clientesCache.push(nuevo);
    return nuevo;
};

const updateCliente = async (id, data) => {
    const res = await fetch(`${CONFIG.API_BASE}/clientes/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error('Error al actualizar cliente');
    const actualizado = await res.json();
    const idx = _clientesCache.findIndex(c => c.idCliente === id);
    if (idx > -1) _clientesCache[idx] = actualizado;
    return actualizado;
};

const deleteCliente = async (id) => {
    const res = await fetch(`${CONFIG.API_BASE}/clientes/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Error al eliminar cliente');
    _clientesCache = _clientesCache.filter(c => c.idCliente !== id);
};

// ═══════════════════════════════════════════════════════════════════════════════
// FACTURAS  (API REST — migrado desde localStorage)
// ═══════════════════════════════════════════════════════════════════════════════

async function loadInvoices() {
    try {
        const res = await fetch(`${CONFIG.API_BASE}/facturas`);
        if (!res.ok) throw new Error('Error al cargar facturas');
        _facturasCache = await res.json();
        return _facturasCache;
    } catch (e) {
        console.error('[datos.js] loadInvoices:', e);
        return _facturasCache;
    }
}

const getInvoices = () => _facturasCache;
const getInvoice = (id) => _facturasCache.find(f => f.id === id);

const createInvoice = async (inv) => {
    const payload = {
        cliente: inv.cliente,
        documento: inv.documento || '0000000000',
        email: inv.email || '',
        telefono: inv.telefono || '',
        items: inv.items.map(i => ({
            productoId: i.productoId,
            cantidad: i.cantidad
        }))
    };

    const res = await fetch(`${CONFIG.API_BASE}/facturas`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });

    if (!res.ok) {
        const err = await res.text();
        throw new Error('Error al crear factura: ' + err);
    }

    const nueva = await res.json();
    _facturasCache.unshift(nueva);

    await loadProducts();

    return nueva;
};

// ═══════════════════════════════════════════════════════════════════════════════
// STATS
// ═══════════════════════════════════════════════════════════════════════════════

const getStats = () => ({
    totalProductos: _productosCache.length,
    totalFacturas: _facturasCache.length,
    ingresosTotal: _facturasCache.reduce((s, f) => s + (parseFloat(f.total) || 0), 0),
    productosStockBajo: getLowStock().length,
    countProductos: _productosCache.length
});

// ═══════════════════════════════════════════════════════════════════════════════
// UTILS
// ═══════════════════════════════════════════════════════════════════════════════

const formatPrice = (p) =>
    new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(p);

const formatDate = (d) =>
    new Date(d).toLocaleDateString('es-CO', { dateStyle: 'medium' });
