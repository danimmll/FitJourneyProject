const express = require('express');
const { createRoutine, getUserRoutines, deleteRoutine, getRoutineById,updateRoutine } = require('../controllers/routineController');
const { authenticateToken } = require('../middlewares/auth');
const router = express.Router();

/**
 * @swagger
 * tags:
 *   - name: Rutinas
 *     description: Compendio de lógicas relativas a matrices de entrenamiento compuestas
 */

/**
 * @swagger
 * /api/routines:
 *   get:
 *     summary: Consulta agnóstica de rutinas vigentes ligadas al nodo activo
 *     tags: [Rutinas]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Bloque iterable de rutinas compilado y devuelto
 *       401:
 *         description: Rechazo por ausencia del estándar Bearer
 *   post:
 *     summary: Instancia transaccionalmente una agrupación de bloques de ejercicio
 *     tags: [Rutinas]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - name
 *             properties:
 *               name:
 *                 type: string
 *                 example: "Día de Pecho"
 *               description:
 *                 type: string
 *                 example: "Diseño para sobrecarga progresiva miofibrilar"
 *               photo_url:
 *                 type: string
 *                 example: "https://midominio.com/pecho.jpg"
 *               exercises:
 *                 type: array
 *                 items:
 *                   type: object
 *                   properties:
 *                     id_exercise:
 *                       type: integer
 *                       example: 1
 *                     target_sets:
 *                       type: integer
 *                       example: 4
 *                     target_reps:
 *                       type: integer
 *                       example: 10
 *                     target_weight:
 *                       type: number
 *                       example: 60.5
 *     responses:
 *       201:
 *         description: Transacción de alta concurrencia finalizada exitosamente
 *       401:
 *         description: Parámetros del token insuficientes
 */
router.get('/', authenticateToken, getUserRoutines);
router.post('/', authenticateToken, createRoutine);

/**
 * @swagger
 * /api/routines/{id}:
 *   get:
 *     summary: Despliega una lectura detallada incluyendo uniones con ejercicios hijos
 *     tags: [Rutinas]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: UUID o Serial de la Rutina
 *     responses:
 *       200:
 *         description: Respuesta híbrida que une rutinas con catálogo mediante INNER JOIN
 *       401:
 *         description: Flujo bloqueado en el preprocesamiento de request
 *       404:
 *         description: Carga arrojó un set de resultados vacío
 *   delete:
 *     summary: Impulsa una orden de limpieza perenne (Validación de autoría forzada)
 *     tags: [Rutinas]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: Referencia absoluta del set de datos
 *     responses:
 *       200:
 *         description: Objeto en cascada y modelo primordial han sido borrados
 *       401:
 *         description: Sin firma de validación perimetral
 *       403:
 *         description: Disonancia de propiedad; el usuario en el token no es dueño del recurso
 */
router.get('/:id', authenticateToken, getRoutineById);
router.delete('/:id', authenticateToken, deleteRoutine);
router.put('/:id', authenticateToken, updateRoutine);

module.exports = router;