const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

// URL por defecto para ejercicios sin imagen especificada
const DEFAULT_EXERCISE_IMAGE = "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=1200&auto=format&fit=crop";

/**
 * Normaliza URLs de imagen: valida que sea string no vacío,
 * retorna URL por defecto si es inválida o está vacía
 */
const normalizeImageUrl = (imageUrl) => {
    if (typeof imageUrl !== 'string') return DEFAULT_EXERCISE_IMAGE;
    const trimmed = imageUrl.trim();
    return trimmed.length > 0 ? trimmed : DEFAULT_EXERCISE_IMAGE;
};

/**
 * Obtiene todos los ejercicios de la base de datos
 * GET /exercises
 */
const getExercises = async (req, res, next) => {
    try {
        const exercises = await prisma.exercise.findMany();
        res.json(exercises);
    } catch (error) {
        console.log(error);
        next(error);
    }
};

/**
 * Crea un nuevo ejercicio en la base de datos
 * POST /exercises
 * Normaliza la URL de imagen automáticamente
 */
const createExercise = async (req, res, next) => {
    try {
        const {
            name,
            muscle_group,
            difficulty,
            description,
            image_url,
            video_url,
            equipment,
            exercise_type
        } = req.body;

        const exercise = await prisma.exercise.create({
            data: {
                name,
                muscle_group,
                difficulty,
                description,
                image_url: normalizeImageUrl(image_url),
                video_url,
                equipment,
                exercise_type
            }
        });
        res.status(201).json(exercise);
    } catch (error) {
        console.log(error);
        next(error);
    }
};

/**
 * Actualiza un ejercicio existente (parcial o completa)
 * PUT /exercises/:id
 * Solo actualiza los campos que vienen en el request body
 */
const updateExercise = async (req, res, next) => {
    const { id } = req.params;
    try {
        const {
            name,
            muscle_group,
            difficulty,
            description,
            image_url,
            video_url,
            equipment,
            exercise_type
        } = req.body;

        // Construye dinámicamente el objeto de actualización solo con los campos que vienen en el request
        const updateData = {};
        if (name !== undefined) updateData.name = name;
        if (muscle_group !== undefined) updateData.muscle_group = muscle_group;
        if (difficulty !== undefined) updateData.difficulty = difficulty;
        if (description !== undefined) updateData.description = description;
        if (image_url !== undefined) updateData.image_url = normalizeImageUrl(image_url);
        if (video_url !== undefined) updateData.video_url = video_url;
        if (equipment !== undefined) updateData.equipment = equipment;
        if (exercise_type !== undefined) updateData.exercise_type = exercise_type;

        const updated = await prisma.exercise.update({
            where: { id_exercise: parseInt(id) },
            data: updateData
        });
        res.json(updated);
    } catch (error) {
        console.log(error);
        next(error);
    }
};

/**
 * Elimina un ejercicio de la base de datos
 * DELETE /exercises/:id
 * Requiere un ID válido de tipo número
 */
const deleteExercise = async (req, res, next) => {
    const { id } = req.params;
    try {
        const exerciseId = parseInt(id, 10);
        if (Number.isNaN(exerciseId)) {
            // Valida que el ID sea un número válido antes de hacer la operación
            return res.status(400).json({ error: "ID de ejercicio no válido" });
        }

        await prisma.exercise.delete({
            where: { id_exercise: exerciseId }
        });
        res.json({ message: "Ejercicio eliminado físicamente de la base de datos" });
    } catch (error) {
        console.log(error);
        next(error);
    }
};

/**
 * Obtiene un ejercicio específico por su ID
 * GET /exercises/:id
 * Retorna 404 si el ejercicio no existe
 */
const getExerciseById = async (req, res, next) => {
    const { id } = req.params;
    try {
        const exercise = await prisma.exercise.findUnique({
            where: { id_exercise: parseInt(id) }
        });

        // Retorna 404 si no encuentra el ejercicio
        if (!exercise) {
            return res.status(404).json({ error: "Ejercicio no encontrado" });
        }
        res.json(exercise);
    } catch (error) {
        console.log(error);
        next(error);
    }
};

module.exports = { getExercises, createExercise, updateExercise, deleteExercise, getExerciseById };