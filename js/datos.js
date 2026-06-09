
// ===== DATOS.JS =====
/**
 * Core Data Management
 * Productos: API REST (Spring Boot /api/productos)
 * Facturas:  localStorage (sin cambios)
 */

const CONFIG = {
    KEYS: { FACT: 'optistock_invoices', CONF: 'optistock_config' },
    IVA: 0.19,
    MIN_STOCK: 5,
    API_BASE: 'http://localhost:8080/api'
};

// ─── CACHE LOCAL DE PRODUCTOS (sincronizado con la API) ───────────────────────
// El cache permite que las funciones síncronas (getProduct, getLowStock, getStats)
// sigan funcionando mientras la UI usa loadProducts() al iniciar la página.
let _productosCache = [];

/**
 * Carga todos los productos desde la API y actualiza el cache.
 * Llamar esta función al inicio de cada página que usa productos.
 * Devuelve una Promise<Array>.
 */
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

// === Products (API REST) ===

/**
 * Devuelve el cache local (síncrono).
 * Usar solo después de haber llamado loadProducts().
 */
const getProducts = () => _productosCache;

/**
 * Busca un producto por id en el cache local (síncrono).
 */
const getProduct = (id) => _productosCache.find(p => p.id === id);

/**
 * Productos con cantidad <= MIN_STOCK (síncrono).
 */
const getLowStock = () => _productosCache.filter(p => p.cantidad <= CONFIG.MIN_STOCK);

/**
 * Agrega un producto vía API.
 * dto: { nombre, precio, cantidad, categoria, descripcion }
 * Devuelve Promise<producto>.
 */
const addProduct = async (dto) => {
    const res = await fetch(`${CONFIG.API_BASE}/productos`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            nombre: dto.nombre,
            precio: dto.precio,
            categoria: dto.categoria,
            descripcion: dto.descripcion
        })
    });
    if (!res.ok) throw new Error('Error al crear producto');
    const nuevo = await res.json();
    _productosCache.push(nuevo);
    return nuevo;
};

/**
 * Actualiza un producto vía API.
 * Devuelve Promise<producto>.
 */
const updateProduct = async (id, data) => {
    const res = await fetch(`${CONFIG.API_BASE}/productos/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            nombre: data.nombre,
            precio: data.precio,
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

/**
 * Elimina un producto vía API.
 * Devuelve Promise<void>.
 */
const deleteProduct = async (id) => {
    const res = await fetch(`${CONFIG.API_BASE}/productos/${id}`, {
        method: 'DELETE'
    });
    if (!res.ok) throw new Error('Error al eliminar producto');
    _productosCache = _productosCache.filter(p => p.id !== id);
};

// === Invoices (localStorage — sin cambios) ===
const getInvoices = () => JSON.parse(localStorage.getItem(CONFIG.KEYS.FACT) || '[]');
const saveInvoices = (data) => localStorage.setItem(CONFIG.KEYS.FACT, JSON.stringify(data));
const getInvoice = (id) => getInvoices().find(f => f.id === id);

const createInvoice = (inv) => {
    const list = getInvoices();
    inv.id = list.length ? Math.max(...list.map(i => i.id)) + 1 : 1;
    inv.fecha = new Date().toISOString();
    inv.subtotal = inv.items.reduce((s, i) => s + (i.precio * i.cantidad), 0);
    inv.iva = inv.subtotal * CONFIG.IVA;
    inv.total = inv.subtotal + inv.iva;
    list.push(inv);
    saveInvoices(list);
    return inv;
};

// === Stats ===
const getStats = () => {
    const prod = getProducts();
    const inv = getInvoices();
    return {
        totalProductos: prod.reduce((s, p) => s + (p.cantidad || 0), 0),
        totalFacturas: inv.length,
        ingresosTotal: inv.reduce((s, f) => s + f.total, 0),
        productosStockBajo: getLowStock().length,
        countProductos: prod.length
    };
};

// === Utils ===
const formatPrice = (p) => new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(p);
const formatDate = (d) => new Date(d).toLocaleDateString('es-CO', { dateStyle: 'medium', timeStyle: 'short' });

// === Initialize ===
// Facturas: asegurar localStorage inicializado
if (!localStorage.getItem(CONFIG.KEYS.FACT)) {
    saveInvoices([]);
}
// Productos: cargar desde API al arrancar
loadProducts();
