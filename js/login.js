// ===== LOGIN.JS (VERSIÓN MEJORADA) =====

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
        box.classList.remove('alert-error');
    } else {
        box.classList.add('alert-error');
        box.classList.remove('alert-success');
    }

    // Scroll a la alerta para que sea visible
    box.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function hideAlert() {
    const box = document.getElementById('alert-box');
    box.style.display = 'none';
    box.textContent = '';
    box.classList.remove('alert-success', 'alert-error');
}

// 4. Función auxiliar para deshabilitar/habilitar botón
function setButtonState(buttonId, disabled) {
    const btn = document.getElementById(buttonId) ||
        document.querySelector(`button[type="submit"]`);
    if (btn) {
        btn.disabled = disabled;
        btn.style.opacity = disabled ? '0.6' : '1';
        btn.style.cursor = disabled ? 'not-allowed' : 'pointer';
    }
}

// 5. Validaciones de frontend
function validarLogin(usuarioLogin, contrasena) {
    // Usuario requerido
    if (!usuarioLogin || usuarioLogin.trim() === '') {
        showAlert('El usuario es obligatorio.');
        return false;
    }

    // Contraseña requerida
    if (!contrasena || contrasena === '') {
        showAlert('La contraseña es obligatoria.');
        return false;
    }

    return true;
}

function validarRegistro(nombre, apellido, usuarioLogin, contrasena) {
    // Validar nombre
    if (!nombre || nombre.trim() === '') {
        showAlert('El nombre es obligatorio.');
        return false;
    }
    if (nombre.trim().length < 2) {
        showAlert('El nombre debe tener al menos 2 caracteres.');
        return false;
    }
    if (nombre.trim().length > 100) {
        showAlert('El nombre no puede exceder 100 caracteres.');
        return false;
    }

    // Validar apellido
    if (!apellido || apellido.trim() === '') {
        showAlert('El apellido es obligatorio.');
        return false;
    }
    if (apellido.trim().length < 2) {
        showAlert('El apellido debe tener al menos 2 caracteres.');
        return false;
    }
    if (apellido.trim().length > 100) {
        showAlert('El apellido no puede exceder 100 caracteres.');
        return false;
    }

    // Validar usuario
    if (!usuarioLogin || usuarioLogin.trim() === '') {
        showAlert('El usuario (login) es obligatorio.');
        return false;
    }
    if (usuarioLogin.trim().length < 4) {
        showAlert('El usuario debe tener al menos 4 caracteres.');
        return false;
    }
    if (usuarioLogin.trim().length > 50) {
        showAlert('El usuario no puede exceder 50 caracteres.');
        return false;
    }
    // Validar caracteres permitidos
    if (!/^[a-zA-Z0-9_.-]+$/.test(usuarioLogin)) {
        showAlert('El usuario solo puede contener letras, números, guiones, puntos y guiones bajos.');
        return false;
    }

    // Validar contraseña (SINCRONIZADA CON BACKEND: 8 caracteres mínimo)
    if (!contrasena || contrasena === '') {
        showAlert('La contraseña es obligatoria.');
        return false;
    }
    if (contrasena.length < 8) {
        showAlert('La contraseña debe tener al menos 8 caracteres.');
        return false;
    }

    return true;
}

// 6. Envío de Formulario de Login
async function handleLoginSubmit(e) {
    e.preventDefault();
    hideAlert();

    const usuarioLogin = document.getElementById('login-username').value.trim();
    const contrasena = document.getElementById('login-password').value;

    // Validar antes de enviar
    if (!validarLogin(usuarioLogin, contrasena)) {
        return;
    }

    const btnSubmit = document.getElementById('login-form').querySelector('button[type="submit"]');
    setButtonState(btnSubmit.id || 'login-btn', true);

    try {
        const res = await fetch(`${CONFIG.API_BASE}/usuarios/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ usuarioLogin, contrasena })
        });

        // ─── MANEJO DINÁMICO DE ERRORES DEL BACKEND ──────────────────────────
        if (!res.ok) {
            // Intentamos parsear el JSON de error que genera Spring
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

        // 3. Volvemos a guardar la sesión ya enriquecida con el token
        localStorage.setItem('optistock_session', JSON.stringify(sesion));

        console.log('[login.js] Login exitoso:', usuarioLogin);

        // Mostrar alerta de éxito ANTES de redirigir
        showAlert('¡Login exitoso! Redirigiendo...', 'success');

        // Redirigir después de 1 segundo para que se vea la alerta
        setTimeout(() => {
            window.location.href = '../Index.html';
        }, 1000);

    } catch (err) {
        console.error('[login.js] Login error:', err);
        showAlert(err.message);
        setButtonState(btnSubmit.id || 'login-btn', false);
    }
}

// 7. Envío de Formulario de Registro
async function handleRegisterSubmit(e) {
    e.preventDefault();
    hideAlert();

    const nombre = document.getElementById('reg-nombre').value.trim();
    const apellido = document.getElementById('reg-apellido').value.trim();
    const usuarioLogin = document.getElementById('reg-username').value.trim();
    const contrasena = document.getElementById('reg-password').value;
    const rolElement = document.getElementById('reg-rol');
    const idRol = rolElement ? parseInt(rolElement.value) : 2;

    // VALIDACIÓN COMPLETA ANTES DE ENVIAR
    if (!validarRegistro(nombre, apellido, usuarioLogin, contrasena)) {
        return;
    }

    const btnSubmit = document.getElementById('register-form').querySelector('button[type="submit"]');
    setButtonState(btnSubmit.id || 'register-btn', true);

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

        const data = await res.json();

        showAlert('¡Usuario registrado con éxito! Ya puedes iniciar sesión.', 'success');

        // Pre-cargar el nombre en la pestaña de login
        document.getElementById('login-username').value = usuarioLogin;
        document.getElementById('login-password').value = '';

        // Limpiar formulario de registro
        document.getElementById('register-form').reset();

        // Cambiar a tab de login después de 2 segundos
        setTimeout(() => {
            switchTab('login');
            setButtonState(btnSubmit.id || 'register-btn', false);
        }, 2000);

    } catch (err) {
        console.error('[login.js] Register error:', err);
        showAlert(err.message);
        setButtonState(btnSubmit.id || 'register-btn', false);
    }
}