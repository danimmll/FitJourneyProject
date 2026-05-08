const express = require('express');
const { register, login, reactivateAccount, deleteAccount } = require('../controllers/authController');
const { authenticateToken, isAdmin } = require('../middlewares/auth');
const router = express.Router();

/**
 * @swagger
 * tags:
 * - name: Autenticacion
 * description: Endpoints para el registro y control de acceso del sistema
 */

/**
 * @swagger
 * /api/auth/register:
 * post:
 * summary: Registra un nuevo usuario en la base de datos
 * tags: [Autenticacion]
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
 * description: El registro del usuario se completó con éxito
 * 400:
 * description: Infracción de validación (por ejemplo, el correo ya existe)
 */
router.post('/register', register);

/**
 * @swagger
 * /api/auth/login:
 * post:
 * summary: Autentica un usuario y genera credenciales efímeras
 * tags: [Autenticacion]
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
 * description: Transacción de login aprobada, adjunta payload JWT
 * 401:
 * description: Se han denegado las credenciales por ser inválidas
 */
router.post('/login', login);

/**
 * @swagger
 * /api/auth/reactivate:
 * post:
 * summary: Reactiva entidades de usuario previamente marcadas para borrado lógico
 * tags: [Autenticacion]
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
 * description: Restauración de estado completada, se entrega JWT
 * 400:
 * description: Estado de cuenta incompatible con los criterios de reactivación
 */
router.post('/reactivate', reactivateAccount);

/**
 * @swagger
 * /api/auth/delete:
 * post:
 * summary: (Restringido) Purga definitiva de registros en la base de datos
 * description: Permite eliminar permanentemente a un usuario. La transacción está sujeta a verificación de tokens de Administrador.
 * tags: [Autenticacion]
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
 * description: Destrucción del nodo de usuario concluida sin anomalías
 * 401:
 * description: Violación de interceptor por token JWT inexistente o comprometido
 * 403:
 * description: Permisos insuficientes en jerarquía de Control de Acceso Basado en Roles (RBAC)
 */
router.post('/delete', authenticateToken, isAdmin, deleteAccount);

module.exports = router;