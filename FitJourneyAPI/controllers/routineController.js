const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const createRoutine = async (req, res, next) => {
    try {
        const { name, description, photo_url, exercises } = req.body;

        const routine = await prisma.routine.create({
            data: {
                name,
                description,
                photo_url: photo_url || null,
                id_user: req.user.id,
                exercises: {
                    create: exercises && exercises.length > 0 ? exercises.map(ex => ({
                        id_exercise: ex.id_exercise,
                        target_sets: ex.target_sets,
                        target_reps: ex.target_reps,
                        target_weight: ex.target_weight
                    })) : [] // Previene error si el array viene vacío
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
        // Trae TODAS las rutinas activas de cualquier usuario (Admin o Comunidad)
        const routines = await prisma.routine.findMany({
            where: { active: 1 },
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
        
        // Verifica si existe y si es el dueño o admin
        if (!routine || (routine.id_user !== req.user.id && req.user.role !== 'admin')) {
            return res.status(403).json({ error: "No tienes permiso para eliminar esta rutina" });
        }

        // Borrado físico real
        await prisma.routine.delete({
            where: { id_routine: parseInt(id) }
        });

        res.json({ message: "Rutina eliminada físicamente" });
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

        if (!routine || routine.active === 0) {
            return res.status(404).json({ error: "Rutina no encontrada" });
        }
        res.json(routine);
    } catch (error) {
        next(error);
    }
};

module.exports = { createRoutine, getUserRoutines, deleteRoutine, getRoutineById };