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
                    create: details && details.length > 0
                        ? details.map(d => ({
                            id_exercise: d.id_exercise,
                            sets_performed: d.sets_performed,
                            reps_performed: d.reps_performed,
                            max_weight_used: d.max_weight_used
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
            workout
        });
    } catch (error) {
        next(error);
    }
};

const getWorkoutHistory = async (req, res, next) => {
    try {
        let whereCondition = {
            active: 1
        };

        if (req.user.role !== 'admin') {
            whereCondition.id_user = req.user.id;
        }

        const workouts = await prisma.workout.findMany({
            where: whereCondition,
            include: {
                routine: {
                    select: {
                        name: true
                    }
                },
                details: {
                    include: {
                        exercise: {
                            select: {
                                name: true,
                                image_url: true
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
        const workoutId = parseInt(id);

        const workout = await prisma.workout.findUnique({
            where: { id_workout: workoutId },
            include: { routine: true }
        });

        if (!workout) {
            return res.status(404).json({ error: "Entrenamiento no encontrado" });
        }

        if (workout.id_user !== req.user.id) {
            return res.status(403).json({ error: "No tienes permiso. Solo puedes borrar tus propios entrenamientos." });
        }

        await prisma.$transaction([
            prisma.workoutDetail.deleteMany({
                where: { id_workout: workoutId }
            }),
            prisma.workout.delete({
                where: { id_workout: workoutId }
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
        const workoutId = parseInt(id, 10);

        const workout = await prisma.workout.findUnique({
            where: {
                id_workout: workoutId
            },
            include: {
                routine: {
                    select: {
                        name: true
                    }
                },
                details: {
                    include: {
                        exercise: {
                            select: {
                                name: true,
                                image_url: true
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

        if (
            !workout ||
            workout.active === 0 ||
            (
                req.user.role !== 'admin' &&
                workout.id_user !== req.user.id
            )
        ) {
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