/**
 * auth.js — Modulo de sesión y permisos de OptiStock
 *
 * Diseño escalable:
 * - La sesión vive en localStorage['optistock_session'] como JSON.
 * - getSession() es el único punto de lectura; cuando se implemente JWT
 * solo se cambia esta función para decodificar el token.
 * - Los permisos se declaran en PERMISOS_ROL: agregar un rol o un permiso
 * nuevo solo requiere editar ese mapa.
 * - requireAuth() / hasPermiso() se llaman desde cada página para proteger
 * rutas — cuando llegue el login real, la redirección ya está cableada.
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

function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (e) {
        return null;
    }
}

/**
 * Devuelve el objeto de sesión activo o null.
 * Estructura: { id, nombre, apellido, login, rol, idRol, token }
 */
function getSession() {
    try {
        const sesion = JSON.parse(localStorage.getItem('optistock_session'));
        if (sesion && sesion.token) {
            const decoded = parseJwt(sesion.token);
            if (decoded) {
                // El JWT de OptiStock puede tener el rol en un claim específico o en 'roles'
                // Ajustamos el rol dinámicamente desde el token si existe
                let rolToken = decoded.roles || decoded.rol || sesion.rol;
                if (Array.isArray(rolToken) && rolToken.length > 0) {
                    rolToken = rolToken[0];
                }
                if (typeof rolToken === 'string' && rolToken.startsWith('ROLE_')) {
                    rolToken = rolToken.replace('ROLE_', '');
                }
                sesion.rol = rolToken.toUpperCase();
                sesion.login = decoded.sub || sesion.login;
            }
        }
        return sesion;
    } catch {
        return null;
    }
}

// AJUSTE: Ahora recibe el objeto del usuario Y el token JWT devuelto por Spring Boot
function setSession(usuarioDTO, token) {
    // PARCHE DE COMPATIBILIDAD
    let rolLimpio = usuarioDTO.nombreRol || 'ADMIN';
    if (typeof rolLimpio === 'string' && rolLimpio.startsWith('ROLE_')) {
        rolLimpio = rolLimpio.replace('ROLE_', '');
    }

    const sesion = {
        id: usuarioDTO.idUsuario,
        nombre: usuarioDTO.nombre,
        apellido: usuarioDTO.apellido,
        login: usuarioDTO.usuarioLogin,
        rol: rolLimpio.toUpperCase(),
        idRol: usuarioDTO.idRol,
        token: token
    };
    localStorage.setItem('optistock_session', JSON.stringify(sesion));
    return sesion;
}

function cerrarSesion() {
    localStorage.removeItem('optistock_session');
    const path = window.location.pathname;
    if (path.includes('/facturacion/') || path.includes('/inventario/') || path.includes('/configuracion/') || path.includes('/paginas/')) {
        window.location.href = '../../Index.html';
    } else {
        window.location.href = '../Index.html';
    }
}

// ─── Guardia de ruta ─────────────────────────────────────────────────────────

function requireAuth() {

    const path = window.location.pathname;

    // Si ya estamos en el login, detenemos la ejecución para evitar el bucle infinito
    if (path.endsWith('login.html')) {
        return null;
    }

    const s = getSession();
    // Ahora que el token sí se guarda en setSession
    if (!s || !s.token) {
        console.warn('[auth.js] requireAuth: No hay sesión activa o falta el token. Redirigiendo a login...');
        const path = window.location.pathname;
        const loginUrl = (path.endsWith('/') || path.endsWith('Index.html') || path.endsWith('index.html'))
            ? 'paginas/login.html'
            : '../paginas/login.html';
        window.location.href = loginUrl;
        return null;
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
 * Inyecta en el sidebar el nombre y rol del usuario activo.
 */
function renderUserBadge() {
    const s = getSession();
    if (!s) return;

    // Badge en el header del sidebar
    const header = document.querySelector('.sidebar-header');
    if (header && !document.getElementById('user-badge')) {
        const badge = document.createElement('div');
        badge.id = 'user-badge';
        // CAMBIO: Fondo oscuro sólido (#1e293b) para que contraste con el texto blanco
        badge.style.cssText = 'margin-top:.75rem;padding:.5rem .75rem;background:#1e293b;border-radius:8px;font-size:.78rem;box-shadow: 0 2px 4px rgba(0,0,0,0.1);';
        badge.innerHTML = `
            <div style="font-weight:600;color:#fff;">${s.nombre} ${s.apellido}</div>
            <div style="color:#94a3b8;margin-top:2px;">${s.rol}</div>`;
        header.appendChild(badge);
    }

    // Ocultar o remover secciones del menú según rol
    const permisos = PERMISOS_ROL[s.rol] || [];

    // Mapeo de secciones del DOM vs permisos requeridos
    const sectionMap = {
        'section-dashboard': 'dashboard',
        'section-inventory': 'inventario',
        'section-billing': 'facturacion',
        'section-reports': 'reportes',
        'section-settings': 'configuracion'
    };

    for (const [id, reqPermiso] of Object.entries(sectionMap)) {
        const sec = document.getElementById(id);
        if (sec && !permisos.includes(reqPermiso)) {
            sec.remove(); // purgar del DOM
        }
    }

}

// ─── Inicialización Automática al cargar el script ────────────────────────────

// Se ejecuta inmediatamente al importar el script para proteger la ruta
const sesionValidada = requireAuth();

// Una vez que el DOM esté completamente cargado, renderizamos el badge y aplicamos permisos
if (sesionValidada) {
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', ejecutarLimpiezaUI);
    } else {
        ejecutarLimpiezaUI();
    }
}

function ejecutarLimpiezaUI() {
    renderUserBadge();
    aplicarPermisosElementosEspeciales();
}

/**
 * Recorre la página buscando elementos con el atributo 'data-permiso'
 * y los remueve del DOM si el rol del usuario no tiene ese permiso.
 */
function aplicarPermisosElementosEspeciales() {
    const s = getSession();
    if (!s) return;

    const permisos = PERMISOS_ROL[s.rol] || [];
    const elementosProtegidos = document.querySelectorAll('[data-permiso]');

    elementosProtegidos.forEach(el => {
        const permisoRequerido = el.getAttribute('data-permiso');
        if (!permisos.includes(permisoRequerido)) {
            el.remove(); // Remueve el elemento para evitar que sea visible o inspeccionable fácilmente
        }
    });
}
