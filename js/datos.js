// ===== DATOS.JS =====
/**
 * Core Data Management
 * Productos : API REST /api/productos
 * Facturas  : API REST /api/facturas   
 * Clientes  : API REST /api/clientes  
 */

const CONFIG = {
    IVA: 0.19,
    MIN_STOCK: 5,
    API_BASE: 'http://localhost:8080/api/v1'
};

// Interceptor global de fetch para inyectar token JWT y corregir rutas
const _originalFetch = window.fetch;
window.fetch = async function (resource, options = {}) {
    let url = resource;

    // 1. Corregir endpoint antiguo /api/ sin versionar
    if (typeof url === 'string' && url.includes('/api/') && !url.includes('/api/v1/')) {
        url = url.replace('/api/', '/api/v1/');
    }

    // 2. Extraer token del objeto de sesión estructurado
    let token = null;
    let esUsuarioAdmin = false;
    try {
        const sesionObj = JSON.parse(localStorage.getItem('optistock_session'));
        token = sesionObj ? sesionObj.token : null;
        esUsuarioAdmin = sesionObj?.rol === 'ADMIN'; // Detectamos si eres admin
    } catch (_) { }

    // 3. Agregar cabeceras
    const newOptions = { ...options };
    newOptions.headers = { ...options.headers };

    if (token && typeof url === 'string' && url.includes('/api/v1/')) {
        newOptions.headers['Authorization'] = `Bearer ${token}`;
    }

    // 4. Realizar petición original
    const response = await _originalFetch(url, newOptions);

    // 5. Si devuelve 401/403, evaluar si expulsar o reportar el error en consola
    if ((response.status === 401 || response.status === 403) &&
        typeof url === 'string' &&
        url.includes('/api/v1/') &&
        !url.includes('/usuarios/login') &&
        !url.includes('/usuarios/registro')) {

        // MEJORA: Si eres ADMIN y te da 403, es un error de desarrollo o de permisos del backend.
        // NO te expulsamos de golpe; te mostramos el error en consola para poder debuggearlo con F12.
        if (response.status === 403 && esUsuarioAdmin) {
            console.error(`[datos.js] Error 403 Forbidden detectado en la ruta: ${url}. Revisa los roles del Backend, no se cerrará la sesión para permitir el debugeo.`);
            return response;
        }

        console.warn('[datos.js] Sesión inválida o expirada. Redirigiendo a login...');
        localStorage.removeItem('optistock_session');
        const path = window.location.pathname;
        const loginUrl = (path.endsWith('/') || path.endsWith('Index.html') || path.endsWith('index.html'))
            ? 'paginas/login.html'
            : '../paginas/login.html';
        window.location.href = loginUrl;
    }

    return response;
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
    // 1. Construimos el objeto tal cual como el DTO lo pide
    const payload = {
        nombre: dto.nombre,
        precio: parseFloat(dto.precio),
        cantidad: parseInt(dto.cantidad),
        descripcion: dto.descripcion,
        // Usamos el ID que viene del HTML
        idCategoria: parseInt(dto.idCategoria)
    };

    console.log("JSON final enviado al servidor:", JSON.stringify(payload));

    const res = await fetch(`${CONFIG.API_BASE}/productos`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });

    // 2. Aquí está la clave: ¿Qué nos dice el servidor exactamente?
    if (!res.ok) {
        const errorDetalle = await res.text();
        console.error("--- ERROR DEL SERVIDOR ---");
        console.error("Estado:", res.status);
        console.error("Cuerpo del error:", errorDetalle); // <--- ESTO ES LO QUE NECESITO
        throw new Error('Error al crear: ' + errorDetalle);
    }

    return await res.json();
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
