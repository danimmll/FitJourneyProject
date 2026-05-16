const jwt = require('jsonwebtoken');

/**
 * Middleware de autenticación JWT
 * Extrae y valida el token del header Authorization
 * Formato esperado: "Bearer <token>"
 * Adjunta datos del usuario al objeto req si el token es válido
 * Retorna 401 si no hay token, 403 si es inválido/expirado
 */
const authenticateToken = (req, res, next) => {
    // Extrae header Authorization
    const authHeader = req.headers['authorization'];
    // Obtiene token del formato "Bearer TOKEN" (split por espacio, toma índice 1)
    const token = authHeader && authHeader.split(' ')[1];

    // Valida que el token esté presente
    if (!token) return res.status(401).json({ error: "Token no proporcionado" });

    // Verifica y decodifica el JWT usando la clave secreta
    jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
        if (err) return res.status(403).json({ error: "Token inválido o expirado" });
        // Adjunta datos del usuario decodificado (id, role) al request
        req.user = user;
        // Continúa con el siguiente middleware
        next();
    });
};

/**
 * Middleware de autorización para admin
 * Verifica que el usuario tenga rol de administrador
 * Debe usarse DESPUÉS de authenticateToken
 * Retorna 403 si no es admin
 */
const isAdmin = (req, res, next) => {
    // Valida que usuario esté autenticado y su role sea 'admin'
    if (req.user && req.user.role === 'admin') {
        // Usuario es admin, continúa con siguiente middleware
        next();
    } else {
        // Usuario no es admin, acceso denegado
        res.status(403).json({ error: "Acceso denegado: se requieren permisos de administrador" });
    }
};

module.exports = { authenticateToken, isAdmin };