const express = require('express');
const { toggleFavorite, getUserFavorites } = require('../controllers/favoriteController');
const { authenticateToken } = require('../middlewares/auth');
const router = express.Router();

/**
 * @swagger
 * tags:
 * - name: Favoritos
 * description: Gestión de ejercicios favoritos del usuario
 */

/**
 * @swagger
 * /api/favorites:
 * get:
 * summary: Obtiene la lista de ejercicios marcados como favoritos por el usuario
 * tags: [Favoritos]
 * security:
 * - bearerAuth: []
 * responses:
 * 200:
 * description: Lista de favoritos obtenida con éxito
 * 401:
 * description: No autorizado
 */
router.get('/', authenticateToken, getUserFavorites);

/**
 * @swagger
 * /api/favorites:
 * post:
 * summary: Marca o desmarca un ejercicio como favorito (Toggle)
 * tags: [Favoritos]
 * security:
 * - bearerAuth: []
 * requestBody:
 * required: true
 * content:
 * application/json:
 * schema:
 * type: object
 * required:
 * - id_exercise
 * properties:
 * id_exercise:
 * type: integer
 * example: 1
 * responses:
 * 200:
 * description: Estado de favorito actualizado (Añadido o Eliminado)
 * 401:
 * description: No autorizado
 * 404:
 * description: Ejercicio no encontrado
 */
router.post('/', authenticateToken, toggleFavorite);

module.exports = router;