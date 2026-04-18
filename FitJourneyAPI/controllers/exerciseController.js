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
        await prisma.exercise.update({
            where: { id_exercise: parseInt(id) },
            data: { active: 0 } 
        });
        res.json({ message: "Ejercicio desactivado correctamente" });
    } catch (error) {
        next(error);
    }
};

module.exports = { getExercises, createExercise, updateExercise, deleteExercise };