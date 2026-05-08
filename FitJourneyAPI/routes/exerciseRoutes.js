const express = require('express');
const { getExercises, createExercise, updateExercise, deleteExercise, getExerciseById } = require('../controllers/exerciseController');
const { authenticateToken, isAdmin } = require('../middlewares/auth');
const router = express.Router();

/**
 * @swagger
 * tags:
 * name: Ejercicios
 * description: Catálogo global de ejercicios
 */

/**
 * @swagger
 * /api/exercises:
 * get:
 * summary: Obtiene todos los ejercicios disponibles
 * tags: [Ejercicios]
 * security:
 * - bearerAuth: []
 * responses:
 * 200:
 * description: Lista de ejercicios obtenida con éxito
 * 401:
 * description: No autorizado (Token faltante o inválido)
 */
router.get('/', authenticateToken, getExercises);

/**
 * @swagger
 * /api/exercises/{id}:
 * get:
 * summary: Obtiene los detalles de un ejercicio por su ID
 * tags: [Ejercicios]
 * security:
 * - bearerAuth: []
 * parameters:
 * - in: path
 * name: id
 * required: true
 * schema:
 * type: integer
 * description: ID del ejercicio
 * responses:
 * 200:
 * description: Datos del ejercicio
 * 401:
 * description: No autorizado
 * 404:
 * description: Ejercicio no encontrado
 */
router.get('/:id', authenticateToken, getExerciseById);

/**
 * @swagger
 * /api/exercises:
 * post:
 * summary: Crea un nuevo ejercicio (Solo Admin)
 * tags: [Ejercicios]
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
 * - muscle_group
 * - difficulty
 * - description
 * properties:
 * name:
 * type: string
 * example: "Sentadilla Libre"
 * muscle_group:
 * type: string
 * example: "Piernas"
 * difficulty:
 * type: string
 * example: "Intermedio"
 * description:
 * type: string
 * example: "Baja flexionando las rodillas manteniendo la espalda recta..."
 * image_url:
 * type: string
 * example: "https://midominio.com/sentadilla.jpg"
 * responses:
 * 201:
 * description: Ejercicio creado
 * 401:
 * description: No autorizado
 * 403:
 * description: Acceso denegado, se requiere rol de Admin
 */
router.post('/', authenticateToken, isAdmin, createExercise);

/**
 * @swagger
 * /api/exercises/{id}:
 * put:
 * summary: Actualiza un ejercicio existente (Solo Admin)
 * tags: [Ejercicios]
 * security:
 * - bearerAuth: []
 * parameters:
 * - in: path
 * name: id
 * required: true
 * schema:
 * type: integer
 * description: ID del ejercicio a actualizar
 * requestBody:
 * content:
 * application/json:
 * schema:
 * type: object
 * properties:
 * name:
 * type: string
 * example: "Sentadilla Libre Editada"
 * description:
 * type: string
 * example: "Nueva descripción para este ejercicio..."
 * responses:
 * 200:
 * description: Ejercicio actualizado
 * 401:
 * description: No autorizado
 * 403:
 * description: Acceso denegado, se requiere rol de Admin
 */
router.put('/:id', authenticateToken, isAdmin, updateExercise);

/**
 * @swagger
 * /api/exercises/{id}:
 * delete:
 * summary: Elimina físicamente un ejercicio (Solo Admin)
 * tags: [Ejercicios]
 * security:
 * - bearerAuth: []
 * parameters:
 * - in: path
 * name: id
 * required: true
 * schema:
 * type: integer
 * description: ID del ejercicio a eliminar
 * responses:
 * 200:
 * description: Ejercicio eliminado físicamente de la base de datos
 * 401:
 * description: No autorizado
 * 403:
 * description: Acceso denegado, se requiere rol de Admin
 */
router.delete('/:id', authenticateToken, isAdmin, deleteExercise);

module.exports = router;