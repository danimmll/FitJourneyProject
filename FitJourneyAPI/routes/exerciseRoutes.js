const express = require('express');
const { getExercises, createExercise, updateExercise, deleteExercise, getExerciseById,toggleExercise } = require('../controllers/exerciseController');
const { authenticateToken, isAdmin } = require('../middlewares/auth');
const router = express.Router();

/**
 * @swagger
 * tags:
 *   - name: Ejercicios
 *     description: Módulo gestor del inventario estático de modalidades de entrenamiento
 */

/**
 * @swagger
 * /api/exercises:
 *   get:
 *     summary: Proyecta la tabla general de ejercicios del sistema
 *     tags: [Ejercicios]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Array JSON suministrado correctamente
 *       401:
 *         description: Token en zona de invalidación
 *   post:
 *     summary: Ensambla y persiste una nueva taxonomía de ejercicio
 *     tags: [Ejercicios]
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
 *               - muscle_group
 *               - difficulty
 *               - description
 *             properties:
 *               name:
 *                 type: string
 *                 example: "Sentadilla Libre"
 *               muscle_group:
 *                 type: string
 *                 example: "Piernas"
 *               difficulty:
 *                 type: string
 *                 example: "Intermedio"
 *               description:
 *                 type: string
 *                 example: "Descienda mediante retroversión de cadera manteniendo la verticalidad espinal..."
 *               image_url:
 *                 type: string
 *                 example: "https://midominio.com/sentadilla.jpg"
 *     responses:
 *       201:
 *         description: Recurso consolidado en persistencia de datos
 *       401:
 *         description: Carece de autenticidad base
 *       403:
 *         description: Interceptación por evaluación de políticas administrativas
 */
router.get('/', authenticateToken, getExercises);
router.post('/', authenticateToken, isAdmin, createExercise);

/**
 * @swagger
 * /api/exercises/{id}:
 *   get:
 *     summary: Recupera una manifestación atómica de un ejercicio por PK
 *     tags: [Ejercicios]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: Componente numérico de enrutamiento
 *     responses:
 *       200:
 *         description: Bloque de metadatos transferido exitosamente
 *       401:
 *         description: Falla en middleware de validación HTTP
 *       404:
 *         description: Inexistencia del nodo solicitado en disco
 *   put:
 *     summary: Escribe alteraciones parciales en una métrica de ejercicio preexistente
 *     tags: [Ejercicios]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: Índice en tabla relacional
 *     requestBody:
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               name:
 *                 type: string
 *                 example: "Sentadilla Libre Editada"
 *               description:
 *                 type: string
 *                 example: "Instrucciones de reingeniería biomecánica para el módulo..."
 *     responses:
 *       200:
 *         description: Metadatos sincronizados correctamente
 *       401:
 *         description: Rechazo a nivel de encapsulamiento
 *       403:
 *         description: RBAC en conflicto con la regla isAdmin
 *   delete:
 *     summary: Proceso restrictivo de destrucción física del bloque del ejercicio
 *     tags: [Ejercicios]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: Target a erradicar de la memoria perenne
 *     responses:
 *       200:
 *         description: Filas dependientes borradas o actualizadas por cascada
 *       401:
 *         description: Inconsistencia del Header de Autorización
 *       403:
 *         description: Operación prohibida por esquema de roles
 */
router.get('/:id', authenticateToken, getExerciseById);
router.put('/:id', authenticateToken, isAdmin, updateExercise);
router.delete('/:id', authenticateToken, isAdmin, deleteExercise);
router.patch('/:id/toggle', authenticateToken, isAdmin, toggleExercise);

module.exports = router;