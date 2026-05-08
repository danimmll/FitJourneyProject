const express = require('express');
const { register, login, reactivateAccount, deleteAccount } = require('../controllers/authController');
const { verifyToken, isAdmin } = require('../middlewares/auth'); // Importamos la seguridad
const router = express.Router();

/**
 * @swagger
 * tags:
 * name: Autenticación
 * description: Endpoints para el registro y acceso de usuarios
 */

/**
 * @swagger
 * /api/auth/register:
 * post:
 * summary: Registra un nuevo usuario
 * tags: [Autenticación]
 * security: []
 * requestBody:
 * required: true
 * content:
 * application/json:
 * schema:
 * type: object
 * required:
 * - email
 * - name
 * - password
 * properties:
 * email:
 * type: string
 * example: "nuevo@usuario.com"
 * name:
 * type: string
 * example: "Juan Pérez"
 * password:
 * type: string
 * example: "secreta123"
 * responses:
 * 201:
 * description: Usuario registrado con éxito
 * 400:
 * description: Error de validación (ej. email ya existe)
 */
router.post('/register', register);

/**
 * @swagger
 * /api/auth/login:
 * post:
 * summary: Inicia sesión en la aplicación
 * tags: [Autenticación]
 * security: []
 * requestBody:
 * required: true
 * content:
 * application/json:
 * schema:
 * type: object
 * required:
 * - email
 * - password
 * properties:
 * email:
 * type: string
 * example: "test@test.com"
 * password:
 * type: string
 * example: "123456"
 * responses:
 * 200:
 * description: Login exitoso, devuelve el Token JWT y los datos del usuario
 * 401:
 * description: Credenciales inválidas
 */
router.post('/login', login);

/**
 * @swagger
 * /api/auth/reactivate:
 * post:
 * summary: Reactiva una cuenta previamente borrada
 * tags: [Autenticación]
 * security: []
 * requestBody:
 * required: true
 * content:
 * application/json:
 * schema:
 * type: object
 * required:
 * - email
 * - password
 * properties:
 * email:
 * type: string
 * example: "borrado@test.com"
 * password:
 * type: string
 * example: "123456"
 * responses:
 * 200:
 * description: Cuenta reactivada, devuelve el Token JWT
 * 400:
 * description: Cuenta no válida para reactivación
 */
router.post('/reactivate', reactivateAccount);

/**
 * @swagger
 * /api/auth/delete:
 * post:
 * summary: (Admin) Borrado Físico Permanente de un usuario
 * description: Elimina definitivamente a un usuario de la base de datos por su email. Requiere token de Administrador.
 * tags: [Autenticación]
 * security:
 * - bearerAuth: []
 * requestBody:
 * required: true
 * content:
 * application/json:
 * schema:
 * type: object
 * required:
 * - email
 * properties:
 * email:
 * type: string
 * example: "usuario_a_eliminar@test.com"
 * responses:
 * 200:
 * description: Usuario eliminado físicamente de la base de datos
 * 401:
 * description: No autorizado (Token faltante o inválido)
 * 403:
 * description: Prohibido (Se requieren permisos de administrador)
 */
router.post('/delete', verifyToken, isAdmin, deleteAccount);

module.exports = router;