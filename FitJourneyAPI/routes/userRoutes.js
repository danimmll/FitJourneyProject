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
 *     description: Gestión de perfiles de usuario
 */

/**
 * @swagger
 * /api/users:
 *   get:
 *     summary: Obtiene todos los usuarios activos (solo admin)
 *     tags: [Usuarios]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Lista de usuarios obtenida correctamente
 *       401:
 *         description: Token no válido o ausente
 *       403:
 *         description: Acceso denegado por rol
 */
router.get('/', authenticateToken, isAdmin, getAllUsers);

/**
 * @swagger
 * /api/users/profile:
 *   get:
 *     summary: Obtiene el perfil del usuario autenticado
 *     tags: [Usuarios]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Perfil obtenido correctamente
 *       401:
 *         description: Token no válido
 *       404:
 *         description: Usuario no encontrado
 *
 *   put:
 *     summary: Actualiza el perfil del usuario autenticado
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
 *                 example: Nuevo Nombre
 *               weight:
 *                 type: number
 *                 example: 75.5
 *               height:
 *                 type: number
 *                 example: 180
 *               photo_url:
 *                 type: string
 *                 example: https://midominio.com/foto.jpg
 *               goal:
 *                 type: string
 *                 example: Ganar masa muscular
 *     responses:
 *       200:
 *         description: Perfil actualizado correctamente
 *       401:
 *         description: Token no válido
 */
router.get('/profile', authenticateToken, getProfile);
router.put('/profile', authenticateToken, updateProfile);

/**
 * @swagger
 * /api/users/{id}:
 *   put:
 *     summary: Actualiza un usuario específico (solo admin)
 *     tags: [Usuarios]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: Usuario actualizado por admin
 *       401:
 *         description: Token inválido
 *       403:
 *         description: Rol insuficiente
 *
 *   delete:
 *     summary: Desactiva lógicamente un usuario (solo admin)
 *     tags: [Usuarios]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: ID del usuario
 *     responses:
 *       200:
 *         description: Usuario desactivado correctamente
 *       401:
 *         description: Token inválido
 *       403:
 *         description: Rol insuficiente
 */
router.put('/:id', authenticateToken, isAdmin, updateUserByAdmin);

router.delete('/:id', authenticateToken, isAdmin, deleteUser);
router.post('/deactivate', authenticateToken, deactivateMyAccount);

module.exports = router;