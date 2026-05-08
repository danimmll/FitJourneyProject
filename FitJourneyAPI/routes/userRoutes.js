const express = require('express');
const { getAllUsers, getProfile, updateProfile, deleteUser } = require('../controllers/userController');
const { verifyToken, isAdmin } = require('../middlewares/auth'); // Importamos isAdmin
const router = express.Router();

/**
 * @swagger
 * tags:
 * name: Usuarios
 * description: Endpoints para la gestión de perfiles de usuario
 */

/**
 * @swagger
 * /api/users:
 * get:
 * summary: Obtiene la lista de todos los usuarios (Solo Admin)
 * tags: [Usuarios]
 * security:
 * - bearerAuth: []
 * responses:
 * 200:
 * description: Lista de usuarios obtenida con éxito
 * 401:
 * description: No autorizado (Token faltante o inválido)
 * 403:
 * description: No tienes permisos (No eres admin)
 */
router.get('/', verifyToken, isAdmin, getAllUsers);

/**
 * @swagger
 * /api/users/profile:
 * get:
 * summary: Obtiene el perfil del usuario autenticado
 * tags: [Usuarios]
 * security:
 * - bearerAuth: []
 * responses:
 * 200:
 * description: Perfil del usuario obtenido con éxito
 * 401:
 * description: No autorizado
 * 404:
 * description: Usuario no encontrado
 */
router.get('/profile', verifyToken, getProfile);

/**
 * @swagger
 * /api/users/profile:
 * put:
 * summary: Actualiza el perfil del usuario autenticado
 * tags: [Usuarios]
 * security:
 * - bearerAuth: []
 * requestBody:
 * required: true
 * content:
 * application/json:
 * schema:
 * type: object
 * properties:
 * name:
 * type: string
 * example: "Nuevo Nombre"
 * weight:
 * type: number
 * example: 75.5
 * height:
 * type: number
 * example: 180
 * photo_url:
 * type: string
 * example: "https://midominio.com/foto.jpg"
 * goal:
 * type: string
 * example: "Ganar masa muscular"
 * responses:
 * 200:
 * description: Perfil actualizado correctamente
 * 401:
 * description: No autorizado
 */
router.put('/profile', verifyToken, updateProfile);

/**
 * @swagger
 * /api/users/{id}:
 * delete:
 * summary: Desactiva un usuario (Borrado Lógico)
 * tags: [Usuarios]
 * security:
 * - bearerAuth: []
 * parameters:
 * - in: path
 * name: id
 * required: true
 * schema:
 * type: integer
 * description: ID del usuario a desactivar
 * responses:
 * 200:
 * description: Usuario desactivado correctamente
 * 401:
 * description: No autorizado
 * 403:
 * description: No tienes permiso para borrar esta cuenta
 */
router.delete('/:id', verifyToken, deleteUser);

module.exports = router;