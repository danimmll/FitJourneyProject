const express = require('express');
const router = express.Router();
const workoutCtrl = require('../controllers/workoutController');
const { authenticateToken } = require('../middlewares/auth');
const { validateWorkout } = require('../middlewares/validators');

/**
 * @swagger
 * tags:
 * - name: Historial
 * description: Operadores dedicados a la telemetría histórica de las iteraciones de usuario
 */

/**
 * @swagger
 * /api/workouts/history:
 * get:
 * summary: Provee un registro cronológico descendente de las sesiones consumadas
 * tags: [Historial]
 * security:
 * - bearerAuth: []
 * responses:
 * 200:
 * description: Transmisión íntegra de la serie de tiempo telemetrada
 * 401:
 * description: Firma de encriptación no homologada
 */
router.get('/history', authenticateToken, workoutCtrl.getWorkoutHistory);

/**
 * @swagger
 * /api/workouts/{id}:
 * get:
 * summary: Focaliza una extracción puntual sobre una ocurrencia de log transaccional
 * tags: [Historial]
 * security:
 * - bearerAuth: []
 * parameters:
 * - in: path
 * name: id
 * required: true
 * schema:
 * type: integer
 * description: ID numérico incremental del Log de Actividad
 * responses:
 * 200:
 * description: Reconstrucción granular de la sesión generada
 * 401:
 * description: Carga interrumpida por credencial vacía
 * 403:
 * description: Barrera de encapsulamiento; prohibida la lectura a logs foráneos
 * 404:
 * description: Referencia a la entidad nula
 */
router.get('/:id', authenticateToken, workoutCtrl.getWorkoutById);

/**
 * @swagger
 * /api/workouts:
 * post:
 * summary: Consolida las métricas finales de una iteración temporal de esfuerzo
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
 * description: Vínculo referencial a la tabla Rutinas (Nullable para modo libre)
 * example: 10
 * duration_minutes:
 * type: integer
 * example: 60
 * calories_burned:
 * type: number
 * example: 450.5
 * notes:
 * type: string
 * example: "Curva de esfuerzo máxima alcanzada en el tercer cuadrante"
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
 * description: Log grabado e indexado en capa profunda
 * 401:
 * description: Identidad del solicitante es anónima o corrompida
 */
router.post('/', authenticateToken, validateWorkout, workoutCtrl.createWorkout);

/**
 * @swagger
 * /api/workouts/{id}:
 * delete:
 * summary: Anula transacciones cronológicas consolidadas erróneamente
 * tags: [Historial]
 * security:
 * - bearerAuth: []
 * parameters:
 * - in: path
 * name: id
 * required: true
 * schema:
 * type: integer
 * description: Clave de la sesión que requiere anulación
 * responses:
 * 200:
 * description: Borrado de histórico verificado con comprobación de cascada
 * 401:
 * description: Abortado al no encontrar el token RSA
 * 403:
 * description: Excepcion arrojada en análisis de propiedad de la fila
 */
router.delete('/:id', authenticateToken, workoutCtrl.deleteWorkout);

module.exports = router;