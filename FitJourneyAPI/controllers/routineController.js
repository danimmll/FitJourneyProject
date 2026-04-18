const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const createRoutine = async (req, res, next) => {
    try {
        const { name, description, exercises } = req.body;

        const routine = await prisma.routine.create({
            data: {
                name,
                description,
                id_user: req.user.id,
                exercises: {
                    create: exercises.map(ex => ({
                        id_exercise: ex.id_exercise,
                        target_sets: ex.target_sets,
                        target_reps: ex.target_reps,
                        target_weight: ex.target_weight
                    }))
                }
            },
            include: { exercises: { include: { exercise: true } } }
        });

        res.status(201).json({ message: "Rutina creada con éxito", routine });
    } catch (error) {
        next(error);
    }
};

const getUserRoutines = async (req, res, next) => {
    try {
        const routines = await prisma.routine.findMany({
            where: { id_user: req.user.id, active: 1 },
            include: { exercises: { include: { exercise: true } } }
        });
        res.json(routines);
    } catch (error) {
        next(error);
    }
};

const deleteRoutine = async (req, res, next) => {
    const { id } = req.params;
    try {
        const routine = await prisma.routine.findUnique({ where: { id_routine: parseInt(id) } });
        if (!routine || routine.id_user !== req.user.id) {
            return res.status(403).json({ error: "No tienes permiso" });
        }

        await prisma.routine.update({
            where: { id_routine: parseInt(id) },
            data: { active: 0 }
        });
        res.json({ message: "Rutina eliminada" });
    } catch (error) {
        next(error);
    }
};

const getRoutineById = async (req, res, next) => {
    const { id } = req.params;
    try {
        const routine = await prisma.routine.findUnique({
            where: { id_routine: parseInt(id) },
            include: { exercises: { include: { exercise: true } } } 
        });

        if (!routine || routine.active === 0 || routine.id_user !== req.user.id) {
            return res.status(404).json({ error: "Rutina no encontrada o acceso denegado" });
        }
        res.json(routine);
    } catch (error) {
        next(error);
    }
};

module.exports = { createRoutine, getUserRoutines, deleteRoutine, getRoutineById };