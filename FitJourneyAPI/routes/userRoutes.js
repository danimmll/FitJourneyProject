const express = require('express');
const { getAllUsers, getProfile, updateProfile, deleteUser } = require('../controllers/userController');
const { authenticateToken, isAdmin } = require('../middlewares/auth');
const router = express.Router();

/**
 * @swagger
 * tags:
 *   - name: Usuarios
 *     description: Controladores para la arquitectura y mutación de perfiles demográficos
 */

/**
 * @swagger
 * /api/users:
 *   get:
 *     summary: Expone el catálogo absoluto de usuarios (Superposición jerárquica)
 *     tags: [Usuarios]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Carga de directorio generada exitosamente
 *       401:
 *         description: Certificado de autenticación ausente o mutado
 *       403:
 *         description: Interceptado por política restrictiva (No administrador)
 */
router.get('/', authenticateToken, isAdmin, getAllUsers);

/**
 * @swagger
 * /api/users/profile:
 *   get:
 *     summary: Carga en memoria el registro demográfico de la sesión en curso
 *     tags: [Usuarios]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Nodo de perfil extraído con precisión
 *       401:
 *         description: Denegación en capa de seguridad
 *       404:
 *         description: Identificador de objeto ausente en la capa de datos
 *   put:
 *     summary: Procesa transacciones de sobrescritura sobre atributos del perfil propio
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
 *               weight:
 *                 type: number
 *                 example: 75.5
 *               height:
 *                 type: number
 *                 example: 180
 *               photo_url:
 *                 type: string
 *                 example: "https://midominio.com/foto.jpg"
 *               goal:
 *                 type: string
 *                 example: "Ganar masa muscular"
 *     responses:
 *       200:
 *         description: Modificación de los vértices ejecutada
 *       401:
 *         description: Sesión criptográfica no constatada
 */
router.get('/profile', authenticateToken, getProfile);
router.put('/profile', authenticateToken, updateProfile);

/**
 * @swagger
 * /api/users/{id}:
 *   delete:
 *     summary: Ejecuta rutina de borrado lógico preservando integridad referencial
 *     tags: [Usuarios]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: Clave primaria (PK) correspondiente al modelo de usuario
 *     responses:
 *       200:
 *         description: Cambio de bandera a estado inactivo verificado
 *       401:
 *         description: Capa de autorización vulnerada
 *       403:
 *         description: Conflicto de propiedades; el token no pertenece a la fila afectada
 */
router.delete('/:id', authenticateToken, deleteUser);

module.exports = router;