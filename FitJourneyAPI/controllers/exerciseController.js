const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const getExercises = async (req, res, next) => {
    try {
        const exercises = await prisma.exercise.findMany({
            where: { active: 1 },
            orderBy: { name: 'asc' }
        });
        res.json(exercises);
    } catch (error) {
        next(error);
    }
};

const createExercise = async (req, res, next) => {
    try {
        const exercise = await prisma.exercise.create({ data: req.body });
        res.status(201).json(exercise);
    } catch (error) {
        next(error); 
    }
};

const updateExercise = async (req, res, next) => {
    const { id } = req.params;
    try {
        const updated = await prisma.exercise.update({
            where: { id_exercise: parseInt(id) },
            data: req.body
        });
        res.json(updated);
    } catch (error) {
        next(error);
    }
};

const deleteExercise = async (req, res, next) => {
    const { id } = req.params;
    try {
        // Borrado físico real
        await prisma.exercise.delete({
            where: { id_exercise: parseInt(id) }
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

module.exports = { getExercises, createExercise, updateExercise, deleteExercise, getExerciseById };