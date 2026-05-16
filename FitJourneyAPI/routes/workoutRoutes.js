const express = require('express');
const router = express.Router();

const workoutCtrl = require('../controllers/workoutController');
const { authenticateToken } = require('../middlewares/auth');
const { validateWorkout } = require('../middlewares/validators');

/**
 * @swagger
 * tags:
 *   - name: Workouts
 *     description: Gestión del historial y registro de entrenamientos
 */

/**
 * @swagger
 * /api/workouts/history:
 *   get:
 *     summary: Obtener historial de entrenamientos
 *     description: |
 *       - Los usuarios normales solo pueden ver sus propios entrenamientos.
 *       - Los administradores pueden visualizar el historial completo de todos los usuarios.
 *     tags: [Workouts]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Historial obtenido correctamente
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 type: object
 *                 properties:
 *                   id_workout:
 *                     type: integer
 *                   id_user:
 *                     type: integer
 *                   workout_date:
 *                     type: string
 *                     format: date-time
 *                   duration_minutes:
 *                     type: integer
 *                   calories_burned:
 *                     type: number
 *                   notes:
 *                     type: string
 *                   routine:
 *                     type: object
 *                     properties:
 *                       name:
 *                         type: string
 *                   details:
 *                     type: array
 *                     items:
 *                       type: object
 *                       properties:
 *                         id_exercise:
 *                           type: integer
 *                         sets_performed:
 *                           type: integer
 *                         reps_string:
 *                           type: string
 *                         max_weight:
 *                           type: number
 *       401:
 *         description: Token inválido o no proporcionado
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Token no proporcionado"
 *       403:
 *         description: Acceso denegado
 */
router.get(
    '/history',
    authenticateToken,
    workoutCtrl.getWorkoutHistory
);

/**
 * @swagger
 * /api/workouts:
 *   post:
 *     summary: Registrar un nuevo entrenamiento
 *     description: Registra un nuevo entrenamiento para el usuario autenticado. El middleware `validateWorkout` valida la estructura de `details`.
 *     tags: [Workouts]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               id_routine:
 *                 type: integer
 *                 nullable: true
 *                 example: 10
 *                 description: ID de rutina asociada (opcional)
 *               duration_minutes:
 *                 type: integer
 *                 example: 60
 *               calories_burned:
 *                 type: number
 *                 example: 450.5
 *               notes:
 *                 type: string
 *                 example: "Buen rendimiento en entrenamiento de pecho"
 *               details:
 *                 type: array
 *                 description: Lista de detalles por ejercicio (sets, reps, peso, duración, velocidad)
 *                 items:
 *                   type: object
 *                   properties:
 *                     id_exercise:
 *                       type: integer
 *                       example: 1
 *                     sets_performed:
 *                       type: integer
 *                       example: 4
 *                     reps_string:
 *                       type: string
 *                       example: "12,10,10,8"
 *                     max_weight:
 *                       type: number
 *                       example: 80
 *                     duration_minutes:
 *                       type: number
 *                       example: 5
 *                     speed_kmh:
 *                       type: number
 *                       example: 10.5
 *     responses:
 *       201:
 *         description: Entrenamiento registrado correctamente
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                 data:
 *                   type: object
 *       400:
 *         description: Datos inválidos
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *       401:
 *         description: Usuario no autenticado
 */
router.post(
    '/',
    authenticateToken,
    validateWorkout,
    workoutCtrl.createWorkout
);

/**
 * @swagger
 * /api/workouts/{id}:
 *   get:
 *     summary: Obtener un entrenamiento por ID
 *     description: |
 *       - Los usuarios normales solo pueden acceder a sus entrenamientos.
 *       - Los administradores pueden consultar cualquier entrenamiento.
 *     tags: [Workouts]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: ID del entrenamiento
 *     responses:
 *       200:
 *         description: Entrenamiento encontrado
 *       401:
 *         description: Token inválido
 *       403:
 *         description: Acceso denegado
 *       404:
 *         description: Entrenamiento no encontrado
 */
router.get(
    '/:id',
    authenticateToken,
    workoutCtrl.getWorkoutById
);

/**
 * @swagger
 * /api/workouts/{id}:
 *   delete:
 *     summary: Eliminar un entrenamiento
 *     description: |
 *       El usuario solo puede eliminar entrenamientos propios,
 *       salvo que tenga rol de administrador.
 *     tags: [Workouts]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: ID del entrenamiento
 *     responses:
 *       200:
 *         description: Entrenamiento eliminado correctamente
 *       401:
 *         description: Token inválido
 *       403:
 *         description: No autorizado para eliminar este entrenamiento
 *       404:
 *         description: Entrenamiento no encontrado
 */
router.delete(
    '/:id',
    authenticateToken,
    workoutCtrl.deleteWorkout
);

module.exports = router;