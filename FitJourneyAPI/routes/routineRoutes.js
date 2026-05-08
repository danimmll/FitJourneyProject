const express = require('express');
const { createRoutine, getUserRoutines, deleteRoutine, getRoutineById } = require('../controllers/routineController');
const { authenticateToken } = require('../middlewares/auth');
const router = express.Router();

/**
 * @swagger
 * tags:
 * - name: Rutinas
 * description: Gestión de rutinas de entrenamiento y sus ejercicios
 */

/**
 * @swagger
 * /api/routines:
 * get:
 * summary: Obtiene todas las rutinas activas (Públicas y del usuario actual)
 * tags: [Rutinas]
 * security:
 * - bearerAuth: []
 * responses:
 * 200:
 * description: Lista de rutinas obtenida con éxito
 * 401:
 * description: No autorizado
 */
router.get('/', authenticateToken, getUserRoutines);

/**
 * @swagger
 * /api/routines/{id}:
 * get:
 * summary: Obtiene los detalles de una rutina por su ID (incluye ejercicios)
 * tags: [Rutinas]
 * security:
 * - bearerAuth: []
 * parameters:
 * - in: path
 * name: id
 * required: true
 * schema:
 * type: integer
 * description: ID de la rutina
 * responses:
 * 200:
 * description: Detalles de la rutina y sus ejercicios
 * 401:
 * description: No autorizado
 * 404:
 * description: Rutina no encontrada
 */
router.get('/:id', authenticateToken, getRoutineById);

/**
 * @swagger
 * /api/routines:
 * post:
 * summary: Crea una nueva rutina
 * tags: [Rutinas]
 * security:
 * - bearerAuth: []
 * requestBody:
 * required: true
 * content:
 * application/json:
 * schema:
 * type: object
 * required:
 * - name
 * properties:
 * name:
 * type: string
 * example: "Día de Pecho"
 * description:
 * type: string
 * example: "Rutina enfocada en hipertrofia pectoral"
 * photo_url:
 * type: string
 * example: "https://midominio.com/pecho.jpg"
 * exercises:
 * type: array
 * items:
 * type: object
 * properties:
 * id_exercise:
 * type: integer
 * example: 1
 * target_sets:
 * type: integer
 * example: 4
 * target_reps:
 * type: integer
 * example: 10
 * target_weight:
 * type: number
 * example: 60.5
 * responses:
 * 201:
 * description: Rutina creada con éxito
 * 401:
 * description: No autorizado
 */
router.post('/', authenticateToken, createRoutine);

/**
 * @swagger
 * /api/routines/{id}:
 * delete:
 * summary: Elimina físicamente una rutina (Solo el creador o Admin)
 * tags: [Rutinas]
 * security:
 * - bearerAuth: []
 * parameters:
 * - in: path
 * name: id
 * required: true
 * schema:
 * type: integer
 * description: ID de la rutina a borrar
 * responses:
 * 200:
 * description: Rutina eliminada de la base de datos
 * 401:
 * description: No autorizado
 * 403:
 * description: No tienes permiso para borrar esta rutina
 */
router.delete('/:id', authenticateToken, deleteRoutine);

module.exports = router;