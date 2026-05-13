const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const getExercises = async (req, res, next) => {
    try {
        // Si es admin, no filtramos (ve todos). Si es usuario normal, solo los activos (active: 1)
        const filter = req.user.role === 'admin' ? {} : { active: 1 };

        const exercises = await prisma.exercise.findMany({
            where: filter
        });
        res.json(exercises);
    } catch (error) {
        next(error);
    }
};

const createExercise = async (req, res, next) => {
    try {
        const {
            name,
            muscle_group,
            difficulty,
            description,
            image_url,
            video_url,
            equipment
        } = req.body;

        const exercise = await prisma.exercise.create({
            data: {
                name,
                muscle_group,
                difficulty,
                description,
                image_url,
                video_url,
                equipment,
                active: 1
            }
        });
        res.status(201).json(exercise);
    } catch (error) {
        next(error);
    }
};

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
            equipment
        } = req.body;

        const updateData = {};
        if (name !== undefined) updateData.name = name;
        if (muscle_group !== undefined) updateData.muscle_group = muscle_group;
        if (difficulty !== undefined) updateData.difficulty = difficulty;
        if (description !== undefined) updateData.description = description;
        if (image_url !== undefined) updateData.image_url = image_url;
        if (video_url !== undefined) updateData.video_url = video_url;
        if (equipment !== undefined) updateData.equipment = equipment;


        const updated = await prisma.exercise.update({
            where: { id_exercise: parseInt(id) },
            data: updateData
        });
        res.json(updated);
    } catch (error) {
        next(error);
    }
};

const deleteExercise = async (req, res, next) => {
    const { id } = req.params;
    try {
        const exerciseId = parseInt(id, 10);
        if (Number.isNaN(exerciseId)) {
            return res.status(400).json({ error: "ID de ejercicio no válido" });
        }

        await prisma.exercise.delete({
            where: { id_exercise: exerciseId }
        });
        res.json({ message: "Ejercicio eliminado físicamente de la base de datos" });
    } catch (error) {
        next(error);
    }
};

const getExerciseById = async (req, res, next) => {
    const { id } = req.params;
    try {
        const exercise = await prisma.exercise.findUnique({
            where: { id_exercise: parseInt(id) }
        });

        if (!exercise || exercise.active === 0) {
            return res.status(404).json({ error: "Ejercicio no encontrado" });
        }
        res.json(exercise);
    } catch (error) {
        next(error);
    }
};

const toggleExercise = async (req, res, next) => {
    const { id } = req.params;
    try {
        const exercise = await prisma.exercise.findUnique({ where: { id_exercise: parseInt(id) } });
        if (!exercise) return res.status(404).json({ message: 'Ejercicio no encontrado' });

        const updated = await prisma.exercise.update({
            where: { id_exercise: parseInt(id) },
            data: { active: exercise.active === 1 ? 0 : 1 }
        });
        res.json(updated);
    } catch (error) {
        next(error);
    }
};

module.exports = { getExercises, createExercise, updateExercise, deleteExercise, getExerciseById, toggleExercise };