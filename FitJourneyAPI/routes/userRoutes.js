const express = require('express');

const {
  getAllUsers,
  getProfile,
  updateProfile,
  deleteUser,
  updateUserByAdmin,
  deactivateMyAccount
} = require('../controllers/userController');

const {
  authenticateToken,
  isAdmin
} = require('../middlewares/auth');

const router = express.Router();

/**
 * @swagger
 * tags:
 *   - name: Usuarios
 *     description: Gestión de perfiles de usuario y datos personales
 */

/**
 * @swagger
 * /api/users:
 *   get:
 *     summary: Obtiene lista de todos los usuarios activos (solo admin)
 *     description: Retorna un array con todos los usuarios activos del sistema. Excluye administradores. Solo accesible por administradores.
 *     tags: [Usuarios]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Lista de usuarios obtenida correctamente
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 type: object
 *                 properties:
 *                   id_user:
 *                     type: integer
 *                     example: 1
 *                   name:
 *                     type: string
 *                     example: "Juan Pérez"
 *                   email:
 *                     type: string
 *                     example: "juan@example.com"
 *                   role:
 *                     type: string
 *                     example: "user"
 *                   photo_url:
 *                     type: string
 *                     example: "https://example.com/photo.jpg"
 *                   goal:
 *                     type: string
 *                     example: "Pérdida de peso"
 *                   height:
 *                     type: number
 *                     example: 180
 *                   weight:
 *                     type: number
 *                     example: 75.5
 *                   active:
 *                     type: integer
 *                     enum: [0, 1]
 *                     example: 1
 *       401:
 *         description: Token ausente, inválido o expirado
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Token no proporcionado"
 *       403:
 *         description: Acceso denegado - Usuario no es administrador
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Acceso denegado: se requieren permisos de administrador"
 */
router.get('/', authenticateToken, isAdmin, getAllUsers);

/**
 * @swagger
 * /api/users/profile:
 *   get:
 *     summary: Obtiene el perfil del usuario autenticado
 *     description: Retorna los datos personales del usuario que realiza la solicitud. Excluye la contraseña.
 *     tags: [Usuarios]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Perfil obtenido correctamente
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 id_user:
 *                   type: integer
 *                   example: 5
 *                 email:
 *                   type: string
 *                   example: "usuario@example.com"
 *                 name:
 *                   type: string
 *                   example: "Juan Pérez"
 *                 role:
 *                   type: string
 *                   example: "user"
 *                 photo_url:
 *                   type: string
 *                   example: "https://example.com/photo.jpg"
 *                 goal:
 *                   type: string
 *                   example: "Pérdida de peso"
 *                 height:
 *                   type: number
 *                   example: 180
 *                 weight:
 *                   type: number
 *                   example: 75.5
 *                 birth_date:
 *                   type: string
 *                   format: date
 *                   example: "1990-05-15"
 *                 gender:
 *                   type: string
 *                   example: "M"
 *                 active:
 *                   type: integer
 *                   enum: [0, 1]
 *                   example: 1
 *       401:
 *         description: Token ausente, inválido o expirado
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Token no proporcionado"
 *       404:
 *         description: Usuario no encontrado o desactivado
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Usuario no encontrado"
 *
 *   put:
 *     summary: Actualiza el perfil del usuario autenticado
 *     description: Permite al usuario actualizar sus datos personales. Solo se actualizan los campos que se envíen. Si proporciona contraseña, se encriptará automáticamente.
 *     tags: [Usuarios]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               name:
 *                 type: string
 *                 example: "Nuevo Nombre"
 *                 description: Nombre completo del usuario (opcional)
 *               weight:
 *                 type: number
 *                 example: 75.5
 *                 description: Peso en kg (opcional)
 *               height:
 *                 type: number
 *                 example: 180
 *                 description: Altura en cm (opcional)
 *               photo_url:
 *                 type: string
 *                 example: "https://midominio.com/foto.jpg"
 *                 description: URL de foto de perfil (opcional)
 *               goal:
 *                 type: string
 *                 example: "Ganar masa muscular"
 *                 description: Objetivo fitness (opcional)
 *               password:
 *                 type: string
 *                 example: "nueva_contraseña_123"
 *                 description: Nueva contraseña (se encriptará automáticamente). Solo se actualiza si se proporciona (opcional)
 *     responses:
 *       200:
 *         description: Perfil actualizado correctamente
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 id_user:
 *                   type: integer
 *                 name:
 *                   type: string
 *                 email:
 *                   type: string
 *                 weight:
 *                   type: number
 *                 height:
 *                   type: number
 *                 photo_url:
 *                   type: string
 *                 goal:
 *                   type: string
 *       401:
 *         description: Token ausente, inválido o expirado
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Token no proporcionado"
 */
router.get('/profile', authenticateToken, getProfile);
router.put('/profile', authenticateToken, updateProfile);

/**
 * @swagger
 * /api/users/{id}:
 *   put:
 *     summary: Actualiza un usuario específico (solo admin)
 *     description: Permite a los administradores actualizar datos de cualquier usuario. Solo se actualizan los campos que se envíen. No permite cambiar contraseña (usar endpoint de auth para eso).
 *     tags: [Usuarios]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: ID del usuario a actualizar
 *         example: 5
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               name:
 *                 type: string
 *                 example: "Nombre Actualizado"
 *                 description: Nombre completo (opcional)
 *               height:
 *                 type: number
 *                 example: 180
 *                 description: Altura en cm (opcional)
 *               weight:
 *                 type: number
 *                 example: 75.5
 *                 description: Peso en kg (opcional)
 *               goal:
 *                 type: string
 *                 example: "Pérdida de peso"
 *                 description: Objetivo fitness (opcional)
 *               photo_url:
 *                 type: string
 *                 example: "https://example.com/photo.jpg"
 *                 description: URL de foto de perfil (opcional)
 *               role:
 *                 type: string
 *                 enum: [user, admin]
 *                 example: "user"
 *                 description: Rol del usuario (opcional)
 *               active:
 *                 type: integer
 *                 enum: [0, 1]
 *                 example: 1
 *                 description: "0 = desactivado, 1 = activo (opcional)"
 *     responses:
 *       200:
 *         description: Usuario actualizado correctamente
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 id_user:
 *                   type: integer
 *                 name:
 *                   type: string
 *                 email:
 *                   type: string
 *                 role:
 *                   type: string
 *                 active:
 *                   type: integer
 *       400:
 *         description: No se recibieron campos válidos para actualizar
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "No se recibieron campos válidos para actualizar"
 *       401:
 *         description: Token ausente, inválido o expirado
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Token no proporcionado"
 *       403:
 *         description: Acceso denegado - Usuario no es administrador
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Acceso denegado: se requieren permisos de administrador"
 *
 *   delete:
 *     summary: Desactiva un usuario (solo admin)
 *     description: Permite a los administradores desactivar un usuario (soft delete). El usuario sigue existiendo en la base de datos pero no puede usar la cuenta.
 *     tags: [Usuarios]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: ID del usuario a desactivar
 *         example: 5
 *     responses:
 *       200:
 *         description: Usuario desactivado correctamente
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: "Usuario desactivado correctamente"
 *       401:
 *         description: Token ausente, inválido o expirado
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Token no proporcionado"
 *       403:
 *         description: Acceso denegado - Usuario no es administrador
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Acceso denegado: se requieren permisos de administrador"
 */
router.put('/:id', authenticateToken, isAdmin, updateUserByAdmin);

router.delete('/:id', authenticateToken, isAdmin, deleteUser);

/**
 * @swagger
 * /api/users/deactivate:
 *   post:
 *     summary: Desactiva la propia cuenta del usuario autenticado
 *     description: Permite al usuario desactivar su propia cuenta de forma segura. Requiere verificación con su contraseña. La cuenta se marca como inactiva (soft delete) pero puede reactivarse.
 *     tags: [Usuarios]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - password
 *             properties:
 *               password:
 *                 type: string
 *                 example: "mi_contraseña_123"
 *                 description: Contraseña actual como verificación de seguridad
 *     responses:
 *       200:
 *         description: Cuenta desactivada correctamente
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: "Cuenta desactivada correctamente"
 *       401:
 *         description: Token ausente, inválido, expirado o contraseña incorrecta
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Contraseña incorrecta"
 *       404:
 *         description: Usuario no encontrado
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Usuario no encontrado"
 */
router.post('/deactivate', authenticateToken, deactivateMyAccount);

module.exports = router;