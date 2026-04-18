const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const createWorkout = async (req, res, next) => {
    try {
        const { id_routine, duration_minutes, calories_burned, notes, details } = req.body;

        const workout = await prisma.workout.create({
            data: {
                id_user: req.user.id,
                id_routine,
                workout_date: new Date(),
                duration_minutes,
                calories_burned,
                notes,
                details: {
                    create: details.map(d => ({
                        id_exercise: d.id_exercise,
                        sets_performed: d.sets_performed,
                        reps_performed: d.reps_performed,
                        max_weight_used: d.max_weight_used
                    }))
                }
            }
        });

        res.status(201).json({ message: "¡Entrenamiento registrado!", workout });
    } catch (error) {
        next(error);
    }
};

const getWorkoutHistory = async (req, res, next) => {
    try {
        const history = await prisma.workout.findMany({
            where: { id_user: req.user.id, active: 1 },
            orderBy: { workout_date: 'desc' },
            include: {
                routine: { select: { name: true } },
                details: { include: { exercise: { select: { name: true, image_url: true } } } }
            }
        });
        res.json(history);
    } catch (error) {
        next(error);
    }
};

const deleteWorkout = async (req, res, next) => {
    const { id } = req.params;
    try {
        const workout = await prisma.workout.findUnique({ where: { id_workout: parseInt(id) } });
        if (!workout || workout.id_user !== req.user.id) {
            return res.status(403).json({ error: "No tienes permiso" });
        }

        await prisma.workout.update({
            where: { id_workout: parseInt(id) },
            data: { active: 0 }
        });
        res.json({ message: "Entrenamiento eliminado" });
    } catch (error) {
        next(error);
    }
};

const getWorkoutById = async (req, res, next) => {
    const { id } = req.params;
    try {
        const workout = await prisma.workout.findUnique({
            where: { id_workout: parseInt(id) },
            include: {
                routine: { select: { name: true } },
                details: { include: { exercise: { select: { name: true, image_url: true } } } }
            }
        });

        if (!workout || workout.active === 0 || workout.id_user !== req.user.id) {
            return res.status(404).json({ error: "Entrenamiento no encontrado o acceso denegado" });
        }
        res.json(workout);
    } catch (error) {
        next(error);
    }
};

module.exports = { createWorkout, getWorkoutHistory, deleteWorkout, getWorkoutById };