const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const createWorkout = async (req, res, next) => {
    try {
        const { id_routine, duration_minutes, calories_burned, notes, workoutDetails, workout_details, details } = req.body;
        const arrayDetalles = workoutDetails || workout_details || details || [];

        const workout = await prisma.workout.create({
            data: {
                id_user: req.user.id,
                id_routine,
                workout_date: new Date(),
                duration_minutes,
                calories_burned,
                notes,
                details: {
                    create: arrayDetalles.length > 0
                        ? arrayDetalles.map((d) => ({
                            id_exercise: d.id_exercise,
                            sets_performed: d.sets_performed || d.sets || d.setsPerformed || 1,
                            reps_string: d.reps_string || d.repsString || null,
                            max_weight: d.max_weight || d.maxWeight || null,
                            duration_minutes: d.duration_minutes || d.durationMinutes || null,
                            speed_kmh: d.speed_kmh || d.speedKmh || null
                        }))
                        : []
                }
            },
            include: {
                routine: true,
                details: {
                    include: {
                        exercise: true
                    }
                }
            }
        });

        res.status(201).json({
            message: "¡Entrenamiento registrado con éxito!",
            data: workout
        });
    } catch (error) {
        next(error);
    }
};

const getWorkoutHistory = async (req, res, next) => {
    try {
        let condicionBusqueda = {};

        if (req.user.role !== 'admin') {
            condicionBusqueda.id_user = req.user.id;
        }

        const workouts = await prisma.workout.findMany({
            where: condicionBusqueda,
            include: {
                routine: {
                    select: { name: true }
                },
                details: {
                    include: {
                        exercise: {
                            select: {
                                name: true,
                                image_url: true,
                                exercise_type: true
                            }
                        }
                    }
                },
                user: {
                    select: {
                        name: true,
                        email: true
                    }
                }
            },
            orderBy: {
                workout_date: 'desc'
            }
        });

        res.json(workouts);
    } catch (error) {
        next(error);
    }
};

const deleteWorkout = async (req, res, next) => {
    const { id } = req.params;
    try {
        const idEntrenamiento = parseInt(id);

        const workout = await prisma.workout.findUnique({
            where: { id_workout: idEntrenamiento },
            include: { routine: true }
        });

        if (!workout) {
            return res.status(404).json({ error: "Entrenamiento no encontrado" });
        }

        if (req.user.role !== 'admin' && workout.id_user !== req.user.id) {
            return res.status(403).json({ error: "No tienes permiso. Solo puedes borrar tus propios entrenamientos." });
        }

        await prisma.$transaction([
            prisma.workoutDetail.deleteMany({
                where: { id_workout: idEntrenamiento }
            }),
            prisma.workout.delete({
                where: { id_workout: idEntrenamiento }
            })
        ]);

        res.json({ message: "Entrenamiento eliminado correctamente de tu historial" });

    } catch (error) {
        next(error);
    }
};

const getWorkoutById = async (req, res, next) => {
    const { id } = req.params;

    try {
        const idEntrenamiento = parseInt(id, 10);

        const workout = await prisma.workout.findUnique({
            where: { id_workout: idEntrenamiento },
            include: {
                routine: {
                    select: { name: true }
                },
                details: {
                    include: {
                        exercise: {
                            select: {
                                name: true,
                                image_url: true,
                                exercise_type: true
                            }
                        }
                    }
                },
                user: {
                    select: {
                        name: true,
                        email: true
                    }
                }
            }
        });

        if (!workout || (req.user.role !== 'admin' && workout.id_user !== req.user.id)) {
            return res.status(404).json({
                error: "Entrenamiento no encontrado o acceso denegado"
            });
        }

        res.json(workout);

    } catch (error) {
        next(error);
    }
};

module.exports = {
    createWorkout,
    getWorkoutHistory,
    deleteWorkout,
    getWorkoutById
};