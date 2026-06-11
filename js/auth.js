/**
 * auth.js — Módulo central de sesión y permisos de OptiStock
 *
 * Diseño escalable:
 *  - La sesión vive en localStorage['optistock_session'] como JSON.
 *  - getSession() es el único punto de lectura; cuando se implemente JWT
 *    solo se cambia esta función para decodificar el token.
 *  - Los permisos se declaran en PERMISOS_ROL: agregar un rol o un permiso
 *    nuevo solo requiere editar ese mapa.
 *  - requireAuth() / hasPermiso() se llaman desde cada página para proteger
 *    rutas — cuando llegue el login real, la redirección ya está cableada.
 */

// ─── Mapa de permisos por rol ────────────────────────────────────────────────
// Valores posibles de rol: ADMIN, VENDEDOR, BODEGUERO, CONTADOR
const PERMISOS_ROL = {
    ADMIN: [
        'dashboard', 'inventario', 'facturacion', 'reportes', 'configuracion',
        'usuarios', 'ajustes_inventario', 'transferencias', 'proveedores'
    ],
    VENDEDOR: [
        'dashboard', 'facturacion', 'reportes_ventas', 'clientes', 'inventario_ver'
    ],
    BODEGUERO: [
        'dashboard', 'inventario', 'ajustes_inventario', 'transferencias',
        'proveedores', 'historial'
    ],
    CONTADOR: [
        'dashboard', 'reportes', 'facturacion_ver', 'financiero'
    ]
};

// ─── Sesión ───────────────────────────────────────────────────────────────────

/**
 * Devuelve el objeto de sesión activo o null.
 * Estructura: { id, nombre, apellido, login, rol, idRol }
 *
 * PUNTO DE EXTENSIÓN: cuando se implemente JWT, reemplazar el cuerpo
 * de esta función por la decodificación del token almacenado.
 */
function getSession() {
    try {
        return JSON.parse(localStorage.getItem('optistock_session'));
    } catch {
        return null;
    }
}

function setSession(usuarioDTO) {
    const sesion = {
        id: usuarioDTO.idUsuario,
        nombre: usuarioDTO.nombre,
        apellido: usuarioDTO.apellido,
        login: usuarioDTO.usuarioLogin,
        rol: (usuarioDTO.nombreRol || 'ADMIN').toUpperCase(),
        idRol: usuarioDTO.idRol
    };
    localStorage.setItem('optistock_session', JSON.stringify(sesion));
    return sesion;
}

function cerrarSesion() {
    localStorage.removeItem('optistock_session');
    // Cuando exista login.html: window.location.href = '../login.html';
    // Por ahora vuelve al dashboard
    window.location.href = '../Index.html';
}

// ─── Guardia de ruta ─────────────────────────────────────────────────────────

/**
 * Llama esto al inicio de cada página protegida.
 * Si no hay sesión, redirige al login (cuando exista).
 * Devuelve el objeto de sesión para uso inmediato.
 */
function requireAuth() {
    const s = getSession();
    if (!s) {
        // Ruta preparada para cuando se implemente login:
        // window.location.href = '../login.html';
        // Por ahora: crear sesión de demo con rol ADMIN
        return setSession({
            idUsuario: 1, nombre: 'Administrador', apellido: 'Sistema',
            usuarioLogin: 'admin', nombreRol: 'ADMIN', idRol: 1
        });
    }
    return s;
}

// ─── Permisos ─────────────────────────────────────────────────────────────────

function hasPermiso(permiso) {
    const s = getSession();
    if (!s) return false;
    const permisos = PERMISOS_ROL[s.rol] || [];
    return permisos.includes(permiso);
}

function esAdmin() {
    const s = getSession();
    return s?.rol === 'ADMIN';
}

// ─── UI helpers ──────────────────────────────────────────────────────────────

/**
 * Inyecta en el sidebar el nombre y rol del usuario activo,
 * y agrega el botón de cerrar sesión.
 * Llamar después de requireAuth() en cada página.
 */
function renderUserBadge() {
    const s = getSession();
    if (!s) return;

    // Badge en el header del sidebar
    const header = document.querySelector('.sidebar-header');
    if (header && !document.getElementById('user-badge')) {
        const badge = document.createElement('div');
        badge.id = 'user-badge';
        badge.style.cssText = 'margin-top:.75rem;padding:.5rem .75rem;background:rgba(255,255,255,.1);border-radius:8px;font-size:.78rem;';
        badge.innerHTML = `
            <div style="font-weight:600;color:#fff;">${s.nombre} ${s.apellido}</div>
            <div style="color:rgba(255,255,255,.7);margin-top:2px;">${s.rol}</div>
            <button onclick="cerrarSesion()" style="margin-top:.5rem;width:100%;padding:.3rem;background:rgba(255,255,255,.15);border:none;border-radius:5px;color:#fff;cursor:pointer;font-size:.75rem;">
                🚪 Cerrar sesión
            </button>`;
        header.appendChild(badge);
    }

    // Ocultar secciones del menú según rol
    if (!esAdmin()) {
        const secSettings = document.getElementById('section-settings');
        if (secSettings && !hasPermiso('configuracion')) {
            secSettings.style.display = 'none';
        }
    }
}