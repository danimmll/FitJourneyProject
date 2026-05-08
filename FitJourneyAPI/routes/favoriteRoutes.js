const express = require('express');
const { toggleFavorite, getUserFavorites } = require('../controllers/favoriteController');
const { authenticateToken } = require('../middlewares/auth');
const router = express.Router();

/**
 * @swagger
 * tags:
 *   - name: Favoritos
 *     description: Procesamiento dinámico para matrices de vinculación rápida entre usuarios y ejercicios
 */

/**
 * @swagger
 * /api/favorites:
 *   get:
 *     summary: Recompila y sirve la vista normalizada de las uniones favoritas
 *     tags: [Favoritos]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Vector de favoritos instanciado con su metadato correspondiente
 *       401:
 *         description: Sesión desconocida
 *   post:
 *     summary: Operador mutacional conmutativo (Toggle Insert/Delete)
 *     tags: [Favoritos]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - id_exercise
 *             properties:
 *               id_exercise:
 *                 type: integer
 *                 example: 1
 *     responses:
 *       200:
 *         description: Operación lógica conmutada (Creación si inexistente, Desvinculación si latente)
 *       401:
 *         description: Validación JWT bloqueó la capa conmutativa
 *       404:
 *         description: Imposible generar enlace externo, índice no rastreable
 */
router.get('/', authenticateToken, getUserFavorites);
router.post('/', authenticateToken, toggleFavorite);

module.exports = router;