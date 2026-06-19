// ===== LOGIN.JS =====

// 1. Manejo de Pestañas (Tabs)
function switchTab(tab) {
    // Esconder alertas activas
    hideAlert();

    const loginTab = document.getElementById('tab-login-btn');
    const registerTab = document.getElementById('tab-register-btn');
    const loginForm = document.getElementById('form-login');
    const registerForm = document.getElementById('form-register');

    if (tab === 'login') {
        loginTab.classList.add('active');
        registerTab.classList.remove('active');
        loginForm.classList.add('active');
        registerForm.classList.remove('active');
    } else {
        registerTab.classList.add('active');
        loginTab.classList.remove('active');
        registerForm.classList.add('active');
        loginForm.classList.remove('active');
    }
}

// 2. Mostrar/Ocultar Contraseña
function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    if (input.type === 'password') {
        input.type = 'text';
    } else {
        input.type = 'password';
    }
}

// 3. Manejo de Alertas / Notificaciones en Pantalla
function showAlert(message, type = 'error') {
    const box = document.getElementById('alert-box');
    box.textContent = message;
    box.style.display = 'flex';

    if (type === 'success') {
        box.classList.add('alert-success');
    } else {
        box.classList.remove('alert-success');
    }
}

function hideAlert() {
    const box = document.getElementById('alert-box');
    box.style.display = 'none';
    box.textContent = '';
}

// 4. Envío de Formulario de Login
async function handleLoginSubmit(e) {
    e.preventDefault();
    hideAlert();

    const usuarioLogin = document.getElementById('login-username').value.trim();
    const contrasena = document.getElementById('login-password').value;

    try {
        const res = await fetch(`${CONFIG.API_BASE}/usuarios/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ usuarioLogin, contrasena })
        });

        // ─── MANEJO DINÁMICO DE ERRORES DEL BACKEND ──────────────────────────
        if (!res.ok) {
            // Intentamos parsear el JSON de error que genera Spring (ResponseStatusException)
            const errData = await res.json().catch(() => ({}));
            // Si el backend trae un mensaje específico lo usa; si no, cae en el texto por defecto
            throw new Error(errData.message || 'Usuario o contraseña incorrectos.');
        }

        const data = await res.json();

        // ─── INTEGRACIÓN CON AUTH.JS ──────────────────────────────────────────
        // 1. Delegamos en setSession() para estructurar el usuario y limpiar el prefijo 'ROLE_'
        const sesion = setSession(data.usuario);

        // 2. Le inyectamos el token JWT que devolvió la API al mismo objeto
        sesion.token = data.token;

        // 3. Volvemos a guardar la sesión ya enriquecida con el token para que requireAuth() le dé luz verde
        localStorage.setItem('optistock_session', JSON.stringify(sesion));

        // Redirigir al Dashboard principal
        window.location.href = '../Index.html';

    } catch (err) {
        console.error('[login.js] Login error:', err);
        showAlert(err.message);
    }
}

// 5. Envío de Formulario de Registro
async function handleRegisterSubmit(e) {
    e.preventDefault();
    hideAlert();

    const nombre = document.getElementById('reg-nombre').value.trim();
    const apellido = document.getElementById('reg-apellido').value.trim();
    const usuarioLogin = document.getElementById('reg-username').value.trim();
    const contrasena = document.getElementById('reg-password').value;
    const idRol = parseInt(document.getElementById('reg-role') ? document.getElementById('reg-role').value : document.getElementById('reg-rol').value);

    // Validación mínima local
    if (contrasena.length < 5) {
        showAlert('La contraseña debe tener al menos 5 caracteres.');
        return;
    }

    try {
        const res = await fetch(`${CONFIG.API_BASE}/usuarios/registro`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                nombre,
                apellido,
                usuarioLogin,
                contrasena,
                idRol
            })
        });

        // ─── MANEJO DINÁMICO DE ERRORES DEL BACKEND ──────────────────────────
        if (!res.ok) {
            // Intentamos parsear el JSON de error (ej: el 409 Conflict de Spring)
            const errData = await res.json().catch(() => ({}));
            // Muestra el mensaje exacto enviado por el backend o un texto de respaldo general
            throw new Error(errData.message || 'Error al registrar el usuario.');
        }

        showAlert('¡Usuario registrado con éxito! Ya puedes iniciar sesión.', 'success');

        // Pre-cargar el nombre en la pestaña de login y cambiar de pestaña
        document.getElementById('login-username').value = usuarioLogin;
        document.getElementById('login-password').value = '';

        setTimeout(() => {
            switchTab('login');
        }, 1500);

    } catch (err) {
        console.error('[login.js] Register error:', err);
        showAlert(err.message);
    }
}