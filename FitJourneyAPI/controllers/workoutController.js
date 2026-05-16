const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

/**
 * Crea un nuevo entrenamiento registrado del usuario autenticado
 * POST /workouts
 * Incluye detalles de ejercicios realizados con sets, reps, peso, duración y velocidad
 * Acepta múltiples formatos de nombres de propiedades (flexibilidad)
 */
const createWorkout = async (req, res, next) => {
    try {
        const { id_routine, duration_minutes, calories_burned, notes, workoutDetails, workout_details, details } = req.body;
        // Soporta múltiples nombres de propiedades para el array de detalles
        const arrayDetalles = workoutDetails || workout_details || details || [];

        // Crea entrenamiento con detalles de cada ejercicio realizado
        const workout = await prisma.workout.create({
            data: {
                id_user: req.user.id,
                id_routine,
                // Registra fecha/hora actual del entrenamiento
                workout_date: new Date(),
                duration_minutes,
                calories_burned,
                notes,
                // Crea detalles de cada ejercicio realizado con múltiples formatos de propiedades
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
            // Retorna entrenamiento con rutina asociada y detalles de ejercicios
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
        console.log(error);
        next(error);
    }
};

/**
 * Obtiene historial de entrenamientos del usuario (o todos si es admin)
 * GET /workouts
 * Retorna entrenamientos ordenados por fecha descendente
 * Incluye rutina, detalles de ejercicios y datos del usuario
 */
const getWorkoutHistory = async (req, res, next) => {
    try {
        // Si no es admin, solo obtiene sus propios entrenamientos
        let condicionBusqueda = {};

        if (req.user.role !== 'admin') {
            condicionBusqueda.id_user = req.user.id;
        }

        // Busca entrenamientos con información completa
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
            // Ordena por fecha más reciente primero
            orderBy: {
                workout_date: 'desc'
            }
        });

        res.json(workouts);
    } catch (error) {
        console.log(error);
        next(error);
    }
};

/**
 * Elimina un entrenamiento del historial
 * DELETE /workouts/:id
 * Solo el propietario del entrenamiento o admin pueden eliminar
 * Usa transacción para garantizar integridad (elimina detalles + workout)
 */
const deleteWorkout = async (req, res, next) => {
    const { id } = req.params;
    try {
        const idEntrenamiento = parseInt(id);

        // Busca entrenamiento para validar permisos
        const workout = await prisma.workout.findUnique({
            where: { id_workout: idEntrenamiento },
            include: { routine: true }
        });

        if (!workout) {
            return res.status(404).json({ error: "Entrenamiento no encontrado" });
        }

        // Verifica que sea propietario o admin antes de eliminar
        if (req.user.role !== 'admin' && workout.id_user !== req.user.id) {
            return res.status(403).json({ error: "No tienes permiso. Solo puedes borrar tus propios entrenamientos." });
        }

        // Usa transacción: primero elimina detalles, luego el entrenamiento
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
        console.log(error);
        next(error);
    }
};

/**
 * Obtiene detalles de un entrenamiento específico
 * GET /workouts/:id
 * Valida permisos: solo propietario, admin o 404 si no existe
 * Retorna entrenamiento completo con rutina, ejercicios y usuario
 */
const getWorkoutById = async (req, res, next) => {
    const { id } = req.params;

    try {
        const idEntrenamiento = parseInt(id, 10);

        // Busca entrenamiento con información completa
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

        // Valida que entrenamiento exista y que usuario tenga permisos (propietario o admin)
        if (!workout || (req.user.role !== 'admin' && workout.id_user !== req.user.id)) {
            return res.status(404).json({
                error: "Entrenamiento no encontrado o acceso denegado"
            });
        }

        res.json(workout);

    } catch (error) {
        console.log(error);
        next(error);
    }
};

module.exports = {
    createWorkout,
    getWorkoutHistory,
    deleteWorkout,
    getWorkoutById
};