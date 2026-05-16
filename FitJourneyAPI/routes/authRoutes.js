const express = require('express');
const { register, login, reactivateAccount, deleteAccount } = require('../controllers/authController');
const { authenticateToken, isAdmin } = require('../middlewares/auth');
const router = express.Router();

/**
 * @swagger
 * tags:
 *   - name: Autenticacion
 *     description: Endpoints para el registro, login y gestión de cuentas de usuario
 */

/**
 * @swagger
 * /api/auth/register:
 *   post:
 *     summary: Registra un nuevo usuario en el sistema
 *     description: Crea una nueva cuenta de usuario con datos básicos. La contraseña se encripta automáticamente. Retorna JWT para iniciar sesión inmediatamente.
 *     tags: [Autenticacion]
 *     security: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - email
 *               - name
 *               - password
 *             properties:
 *               email:
 *                 type: string
 *                 format: email
 *                 example: "nuevo@usuario.com"
 *                 description: Email único del usuario
 *               name:
 *                 type: string
 *                 example: "Juan Pérez"
 *                 description: Nombre completo del usuario
 *               password:
 *                 type: string
 *                 example: "secreta123"
 *                 description: Contraseña (será encriptada con bcrypt)
 *               gender:
 *                 type: string
 *                 enum: [M, F, O]
 *                 example: "M"
 *                 description: Género (opcional)
 *               goal:
 *                 type: string
 *                 example: "Pérdida de peso"
 *                 description: Objetivo fitness (opcional)
 *               height:
 *                 type: number
 *                 example: 180
 *                 description: Altura en cm (opcional)
 *               weight:
 *                 type: number
 *                 example: 75.5
 *                 description: Peso en kg (opcional)
 *               birth_date:
 *                 type: string
 *                 format: date
 *                 example: "1990-05-15"
 *                 description: Fecha de nacimiento (opcional)
 *               photo_url:
 *                 type: string
 *                 example: "https://example.com/photo.jpg"
 *                 description: URL de foto de perfil (opcional)
 *     responses:
 *       201:
 *         description: Usuario registrado exitosamente
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: "Usuario registrado con éxito"
 *                 token:
 *                   type: string
 *                   example: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
 *                   description: JWT válido por 24 horas
 *                 user:
 *                   type: object
 *                   properties:
 *                     id_user:
 *                       type: integer
 *                     email:
 *                       type: string
 *                     name:
 *                       type: string
 *                     role:
 *                       type: string
 *                       example: "user"
 *       400:
 *         description: Validación fallida - Email ya existe o campos obligatorios faltantes
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "El email ya está registrado y en uso."
 */
router.post('/register', register);

/**
 * @swagger
 * /api/auth/login:
 *   post:
 *     summary: Autentica un usuario y genera token JWT
 *     description: Valida credenciales (email + contraseña) y retorna un token JWT válido por 24 horas para las siguientes peticiones autenticadas.
 *     tags: [Autenticacion]
 *     security: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - email
 *               - password
 *             properties:
 *               email:
 *                 type: string
 *                 format: email
 *                 example: "test@test.com"
 *                 description: Email del usuario
 *               password:
 *                 type: string
 *                 example: "123456"
 *                 description: Contraseña en texto plano
 *     responses:
 *       200:
 *         description: Autenticación exitosa
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: "Login exitoso"
 *                 token:
 *                   type: string
 *                   example: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
 *                   description: JWT con id y role del usuario
 *                 user:
 *                   type: object
 *                   properties:
 *                     id_user:
 *                       type: integer
 *                     email:
 *                       type: string
 *                     name:
 *                       type: string
 *                     role:
 *                       type: string
 *       401:
 *         description: Credenciales inválidas o cuenta desactivada
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Credenciales inválidas o cuenta desactivada"
 */
router.post('/login', login);

/**
 * @swagger
 * /api/auth/reactivate:
 *   post:
 *     summary: Reactiva una cuenta desactivada
 *     description: Permite reactivar una cuenta que fue desactivada previamente. Requiere email y contraseña correcta como verificación.
 *     tags: [Autenticacion]
 *     security: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - email
 *               - password
 *             properties:
 *               email:
 *                 type: string
 *                 format: email
 *                 example: "borrado@test.com"
 *                 description: Email de la cuenta desactivada
 *               password:
 *                 type: string
 *                 example: "123456"
 *                 description: Contraseña de la cuenta
 *     responses:
 *       200:
 *         description: Cuenta reactivada exitosamente
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: "Cuenta reactivada con éxito"
 *                 token:
 *                   type: string
 *                   example: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
 *                 user:
 *                   type: object
 *       400:
 *         description: La cuenta no es válida para reactivación (no existe o ya está activa)
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "La cuenta no es válida para reactivación"
 *       401:
 *         description: Contraseña incorrecta
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Contraseña incorrecta. No puedes reactivar esta cuenta."
 */
router.post('/reactivate', reactivateAccount);

/**
 * @swagger
 * /api/auth/delete:
 *   post:
 *     summary: Elimina permanentemente una cuenta de usuario (solo admin)
 *     description: Permite eliminar de forma física (hard delete) una cuenta inactiva de la base de datos. Solo accesible para administradores. Requiere autenticación con token JWT válido.
 *     tags: [Autenticacion]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - email
 *               - password
 *             properties:
 *               email:
 *                 type: string
 *                 format: email
 *                 example: "usuario_a_eliminar@test.com"
 *                 description: Email de la cuenta a eliminar
 *               password:
 *                 type: string
 *                 example: "123456"
 *                 description: Contraseña para confirmación final
 *     responses:
 *       200:
 *         description: Cuenta eliminada permanentemente
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: "Cuenta y datos históricos eliminados para siempre. Ya puedes registrarte de nuevo."
 *       400:
 *         description: Solo se pueden eliminar cuentas inactivas
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Solo se pueden borrar físicamente cuentas inactivas"
 *       401:
 *         description: Token JWT ausente, inválido o expirado
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Token no proporcionado"
 *       403:
 *         description: Permisos insuficientes - Usuario no es administrador
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Acceso denegado: se requieren permisos de administrador"
 */
router.post('/delete', authenticateToken, isAdmin, deleteAccount);

module.exports = router;