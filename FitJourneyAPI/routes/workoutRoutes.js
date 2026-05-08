const express = require('express');
const router = express.Router();
const workoutCtrl = require('../controllers/workoutController');
const { authenticateToken } = require('../middlewares/auth');
const { validateWorkout } = require('../middlewares/validators');

/**
 * @swagger
 * tags:
 * - name: Historial
 * description: Registro y consulta de entrenamientos realizados
 */

/**
 * @swagger
 * /api/workouts/history:
 * get:
 * summary: Obtiene el historial de entrenamientos del usuario logueado
 * tags: [Historial]
 * security:
 * - bearerAuth: []
 * responses:
 * 200:
 * description: Historial obtenido con éxito
 * 401:
 * description: No autorizado
 */
router.get('/history', authenticateToken, workoutCtrl.getWorkoutHistory);

/**
 * @swagger
 * /api/workouts/{id}:
 * get:
 * summary: Obtiene los detalles de un entrenamiento específico del historial
 * tags: [Historial]
 * security:
 * - bearerAuth: []
 * parameters:
 * - in: path
 * name: id
 * required: true
 * schema:
 * type: integer
 * description: ID del entrenamiento
 * responses:
 * 200:
 * description: Datos del entrenamiento
 * 401:
 * description: No autorizado
 * 403:
 * description: No tienes permiso para ver este entrenamiento
 * 404:
 * description: Entrenamiento no encontrado
 */
router.get('/:id', authenticateToken, workoutCtrl.getWorkoutById);

/**
 * @swagger
 * /api/workouts:
 * post:
 * summary: Registra un nuevo entrenamiento completado
 * tags: [Historial]
 * security:
 * - bearerAuth: []
 * requestBody:
 * required: true
 * content:
 * application/json:
 * schema:
 * type: object
 * properties:
 * id_routine:
 * type: integer
 * description: ID de la rutina (Opcional si es entreno libre)
 * example: 10
 * duration_minutes:
 * type: integer
 * example: 60
 * calories_burned:
 * type: number
 * example: 450.5
 * notes:
 * type: string
 * example: "Me sentí con mucha energía hoy"
 * details:
 * type: array
 * items:
 * type: object
 * properties:
 * id_exercise:
 * type: integer
 * example: 1
 * sets_performed:
 * type: integer
 * example: 4
 * reps_performed:
 * type: string
 * example: "10,10,8,8"
 * max_weight_used:
 * type: number
 * example: 80.5
 * responses:
 * 201:
 * description: Entrenamiento guardado en el historial
 * 401:
 * description: No autorizado
 */
router.post('/', authenticateToken, validateWorkout, workoutCtrl.createWorkout);

/**
 * @swagger
 * /api/workouts/{id}:
 * delete:
 * summary: Elimina físicamente un entrenamiento del historial
 * tags: [Historial]
 * security:
 * - bearerAuth: []
 * parameters:
 * - in: path
 * name: id
 * required: true
 * schema:
 * type: integer
 * description: ID del entrenamiento a borrar
 * responses:
 * 200:
 * description: Entrenamiento eliminado del historial
 * 401:
 * description: No autorizado
 * 403:
 * description: No tienes permiso para eliminar este entrenamiento
 */
router.delete('/:id', authenticateToken, workoutCtrl.deleteWorkout);

module.exports = router;