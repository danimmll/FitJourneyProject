const express = require('express');
const { createRoutine, getUserRoutines, deleteRoutine, getRoutineById,updateRoutine } = require('../controllers/routineController');
const { authenticateToken } = require('../middlewares/auth');
const router = express.Router();

/**
 * @swagger
 * tags:
 *   - name: Rutinas
 *     description: Gestión de rutinas de entrenamiento (colecciones de ejercicios). Los usuarios pueden crear rutinas propias y ver rutinas públicas de otros usuarios.
 */

/**
 * @swagger
 * /api/routines:
 *   get:
 *     summary: Obtiene todas las rutinas del usuario y rutinas públicas
 *     description: Retorna las rutinas propias del usuario autenticado más todas las rutinas públicas del sistema. Útil para ver opciones de entrenamiento disponibles.
 *     tags: [Rutinas]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Lista de rutinas obtenida correctamente
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 type: object
 *                 properties:
 *                   id_routine:
 *                     type: integer
 *                     example: 1
 *                   name:
 *                     type: string
 *                     example: "Día de Pecho"
 *                   description:
 *                     type: string
 *                     example: "Rutina enfocada en hipertrofia de pecho"
 *                   photo_url:
 *                     type: string
 *                     example: "https://images.unsplash.com/..."
 *                   is_public:
 *                     type: integer
 *                     enum: [0, 1]
 *                     example: 1
 *                     description: "0 = privada, 1 = pública"
 *                   id_user:
 *                     type: integer
 *                     example: 5
 *                   exercises:
 *                     type: array
 *                     items:
 *                       type: object
 *                       properties:
 *                         id_routine_exercise:
 *                           type: integer
 *                         id_exercise:
 *                           type: integer
 *                         target_sets:
 *                           type: integer
 *                           example: 4
 *                         target_reps:
 *                           type: integer
 *                           example: 10
 *                         target_weight:
 *                           type: number
 *                           example: 60.5
 *                         exercise:
 *                           type: object
 *                           properties:
 *                             id_exercise:
 *                               type: integer
 *                             name:
 *                               type: string
 *       401:
 *         description: Token ausente, inválido o expirado
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Token no proporcionado"
 *   post:
 *     summary: Crea una nueva rutina de entrenamiento
 *     description: Permite al usuario crear una nueva rutina con ejercicios asociados. Puede ser privada (solo visible para el propietario) o pública (visible para todos).
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
 *                 description: Nombre único y descriptivo de la rutina
 *               description:
 *                 type: string
 *                 example: "Rutina diseñada para sobrecarga progresiva e hipertrofia de pecho"
 *                 description: Descripción detallada (opcional)
 *               photo_url:
 *                 type: string
 *                 example: "https://midominio.com/pecho.jpg"
 *                 description: URL de imagen para la rutina (opcional)
 *               is_public:
 *                 type: integer
 *                 enum: [0, 1]
 *                 example: 1
 *                 description: "0 = privada, 1 = pública (default: 1)"
 *               exercises:
 *                 type: array
 *                 description: Lista de ejercicios que componen la rutina
 *                 items:
 *                   type: object
 *                   required:
 *                     - id_exercise
 *                   properties:
 *                     id_exercise:
 *                       type: integer
 *                       example: 1
 *                       description: ID del ejercicio del catálogo
 *                     target_sets:
 *                       type: integer
 *                       example: 4
 *                       description: Número objetivo de series
 *                     target_reps:
 *                       type: integer
 *                       example: 10
 *                       description: Número objetivo de repeticiones por serie
 *                     target_weight:
 *                       type: number
 *                       example: 60.5
 *                       description: Peso objetivo en kg (opcional)
 *     responses:
 *       201:
 *         description: Rutina creada exitosamente
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: "Rutina creada con éxito"
 *                 routine:
 *                   type: object
 *                   properties:
 *                     id_routine:
 *                       type: integer
 *                     name:
 *                       type: string
 *                     exercises:
 *                       type: array
 *       401:
 *         description: Token ausente, inválido o expirado
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Token no proporcionado"
 */
router.get('/', authenticateToken, getUserRoutines);
router.post('/', authenticateToken, createRoutine);

/**
 * @swagger
 * /api/routines/{id}:
 *   get:
 *     summary: Obtiene detalles de una rutina específica
 *     description: Retorna la información completa de una rutina con todos sus ejercicios asociados. Valida permisos (propietario, público o admin).
 *     tags: [Rutinas]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: ID de la rutina a consultar
 *         example: 1
 *     responses:
 *       200:
 *         description: Rutina encontrada
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 id_routine:
 *                   type: integer
 *                   example: 1
 *                 name:
 *                   type: string
 *                   example: "Día de Pecho"
 *                 description:
 *                   type: string
 *                 photo_url:
 *                   type: string
 *                 is_public:
 *                   type: integer
 *                   enum: [0, 1]
 *                 id_user:
 *                   type: integer
 *                 exercises:
 *                   type: array
 *                   items:
 *                     type: object
 *       401:
 *         description: Token ausente, inválido o expirado
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Token no proporcionado"
 *       403:
 *         description: Acceso denegado - No es propietario ni es pública
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "No tienes permiso para ver esta rutina"
 *       404:
 *         description: Rutina no encontrada
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Rutina no encontrada"
 *   put:
 *     summary: Actualiza una rutina existente
 *     description: Permite al propietario (o admin) actualizar los datos y ejercicios de una rutina. Solo se actualizan los campos que se envíen.
 *     tags: [Rutinas]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: ID de la rutina a actualizar
 *         example: 1
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               name:
 *                 type: string
 *                 example: "Día de Pecho Avanzado"
 *               description:
 *                 type: string
 *                 example: "Versión mejorada con más volumen"
 *               photo_url:
 *                 type: string
 *               is_public:
 *                 type: integer
 *                 enum: [0, 1]
 *               exercises:
 *                 type: array
 *                 description: Reemplaza todos los ejercicios anteriores por estos nuevos
 *                 items:
 *                   type: object
 *                   properties:
 *                     id_exercise:
 *                       type: integer
 *                     target_sets:
 *                       type: integer
 *                     target_reps:
 *                       type: integer
 *                     target_weight:
 *                       type: number
 *     responses:
 *       200:
 *         description: Rutina actualizada correctamente
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: "Rutina actualizada correctamente"
 *                 data:
 *                   type: object
 *       401:
 *         description: Token ausente, inválido o expirado
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Token no proporcionado"
 *       403:
 *         description: Acceso denegado - No es propietario
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "No tienes permiso para editar esta rutina"
 *       404:
 *         description: Rutina no encontrada
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Rutina no encontrada"
 *   delete:
 *     summary: Elimina una rutina
 *     description: Permite al propietario (o admin) eliminar una rutina. Esta operación no se puede deshacer.
 *     tags: [Rutinas]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: ID de la rutina a eliminar
 *         example: 1
 *     responses:
 *       200:
 *         description: Rutina eliminada correctamente
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: "Rutina eliminada físicamente"
 *       401:
 *         description: Token ausente, inválido o expirado
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Token no proporcionado"
 *       403:
 *         description: Acceso denegado - No es propietario
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "No tienes permiso para eliminar esta rutina"
 *       404:
 *         description: Rutina no encontrada
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Rutina no encontrada"
 */
router.get('/:id', authenticateToken, getRoutineById);
router.delete('/:id', authenticateToken, deleteRoutine);
router.put('/:id', authenticateToken, updateRoutine);

module.exports = router;