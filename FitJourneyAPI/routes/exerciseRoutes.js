const express = require('express');
const { getExercises, createExercise, updateExercise, deleteExercise, getExerciseById } = require('../controllers/exerciseController');
const { authenticateToken, isAdmin } = require('../middlewares/auth');
const router = express.Router();

/**
 * @swagger
 * tags:
 *   - name: Ejercicios
 *     description: Gestión del inventario de ejercicios disponibles en el sistema (solo administradores pueden crear, editar y eliminar)
 */

/**
 * @swagger
 * /api/exercises:
 *   get:
 *     summary: Obtiene la lista de todos los ejercicios disponibles
 *     description: Retorna un array con todos los ejercicios registrados en el sistema. Requiere autenticación.
 *     tags: [Ejercicios]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Lista de ejercicios obtenida correctamente
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 type: object
 *                 properties:
 *                   id_exercise:
 *                     type: integer
 *                     example: 1
 *                   name:
 *                     type: string
 *                     example: "Sentadilla Libre"
 *                   muscle_group:
 *                     type: string
 *                     example: "Piernas"
 *                   difficulty:
 *                     type: string
 *                     enum: [Principiante, Intermedio, Avanzado]
 *                     example: "Intermedio"
 *                   description:
 *                     type: string
 *                     example: "Ejercicio de fuerza para piernas"
 *                   image_url:
 *                     type: string
 *                     example: "https://images.unsplash.com/..."
 *                   video_url:
 *                     type: string
 *                     example: "https://videos.example.com/..."
 *                   equipment:
 *                     type: string
 *                     example: "Barra"
 *                   exercise_type:
 *                     type: string
 *                     example: "Fuerza"
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
 *     summary: Crea un nuevo ejercicio en el sistema (solo admin)
 *     description: Permite a los administradores crear un nuevo tipo de ejercicio. La URL de imagen se normaliza automáticamente si está vacía.
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
 *                 description: Nombre único del ejercicio
 *               muscle_group:
 *                 type: string
 *                 example: "Piernas"
 *                 description: Grupo muscular principal trabajado
 *               difficulty:
 *                 type: string
 *                 enum: [Principiante, Intermedio, Avanzado]
 *                 example: "Intermedio"
 *                 description: Nivel de dificultad del ejercicio
 *               description:
 *                 type: string
 *                 example: "Descienda flexionando rodillas manteniendo la espalda recta..."
 *                 description: Instrucciones detalladas del ejercicio
 *               image_url:
 *                 type: string
 *                 example: "https://midominio.com/sentadilla.jpg"
 *                 description: URL de imagen (se normaliza si está vacía)
 *               video_url:
 *                 type: string
 *                 example: "https://videos.example.com/sentadilla.mp4"
 *                 description: URL de video instructivo (opcional)
 *               equipment:
 *                 type: string
 *                 example: "Barra, Mancuernas"
 *                 description: Equipamiento necesario (opcional)
 *               exercise_type:
 *                 type: string
 *                 example: "Fuerza"
 *                 description: Tipo de ejercicio (Fuerza, Cardio, Flexibilidad, etc.)
 *     responses:
 *       201:
 *         description: Ejercicio creado exitosamente
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 id_exercise:
 *                   type: integer
 *                   example: 15
 *                 name:
 *                   type: string
 *                 muscle_group:
 *                   type: string
 *                 difficulty:
 *                   type: string
 *                 image_url:
 *                   type: string
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
 *         description: Permisos insuficientes - Usuario no es administrador
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Acceso denegado: se requieren permisos de administrador"
 */
router.get('/', authenticateToken, getExercises);
router.post('/', authenticateToken, isAdmin, createExercise);

/**
 * @swagger
 * /api/exercises/{id}:
 *   get:
 *     summary: Obtiene los detalles de un ejercicio específico
 *     description: Retorna la información completa de un ejercicio por su ID.
 *     tags: [Ejercicios]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: ID del ejercicio a consultar
 *         example: 1
 *     responses:
 *       200:
 *         description: Ejercicio encontrado
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 id_exercise:
 *                   type: integer
 *                   example: 1
 *                 name:
 *                   type: string
 *                   example: "Sentadilla Libre"
 *                 muscle_group:
 *                   type: string
 *                   example: "Piernas"
 *                 difficulty:
 *                   type: string
 *                   example: "Intermedio"
 *                 description:
 *                   type: string
 *                 image_url:
 *                   type: string
 *                 video_url:
 *                   type: string
 *                 equipment:
 *                   type: string
 *                 exercise_type:
 *                   type: string
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
 *       404:
 *         description: Ejercicio no encontrado
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Ejercicio no encontrado"
 *   put:
 *     summary: Actualiza un ejercicio existente (solo admin)
 *     description: Permite a los administradores actualizar total o parcialmente los datos de un ejercicio. Solo se actualizan los campos que se envíen.
 *     tags: [Ejercicios]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: ID del ejercicio a actualizar
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
 *                 example: "Sentadilla Libre Modificada"
 *               muscle_group:
 *                 type: string
 *                 example: "Piernas"
 *               difficulty:
 *                 type: string
 *                 enum: [Principiante, Intermedio, Avanzado]
 *                 example: "Avanzado"
 *               description:
 *                 type: string
 *                 example: "Versión avanzada con pausa de 3 segundos..."
 *               image_url:
 *                 type: string
 *               video_url:
 *                 type: string
 *               equipment:
 *                 type: string
 *               exercise_type:
 *                 type: string
 *     responses:
 *       200:
 *         description: Ejercicio actualizado correctamente
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 id_exercise:
 *                   type: integer
 *                 name:
 *                   type: string
 *                 muscle_group:
 *                   type: string
 *                 difficulty:
 *                   type: string
 *       400:
 *         description: ID de ejercicio inválido
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "ID de ejercicio no válido"
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
 *         description: Permisos insuficientes - Usuario no es administrador
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Acceso denegado: se requieren permisos de administrador"
 *   delete:
 *     summary: Elimina un ejercicio del sistema (solo admin)
 *     description: Permite a los administradores eliminar permanentemente un ejercicio. Esta operación no se puede deshacer.
 *     tags: [Ejercicios]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *         description: ID del ejercicio a eliminar
 *         example: 1
 *     responses:
 *       200:
 *         description: Ejercicio eliminado correctamente
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: "Ejercicio eliminado físicamente de la base de datos"
 *       400:
 *         description: ID de ejercicio inválido
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "ID de ejercicio no válido"
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
 *         description: Permisos insuficientes - Usuario no es administrador
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Acceso denegado: se requieren permisos de administrador"
 */
router.get('/:id', authenticateToken, getExerciseById);
router.put('/:id', authenticateToken, isAdmin, updateExercise);
router.delete('/:id', authenticateToken, isAdmin, deleteExercise);

module.exports = router;