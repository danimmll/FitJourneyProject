const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const createRoutine = async (req, res, next) => {
    try {
        // Añadido is_public a la desestructuración del body
        const { name, description, photo_url, is_public, exercises } = req.body;

        const routine = await prisma.routine.create({
            data: {
                name,
                description,
                photo_url: photo_url || null,
                id_user: req.user.id, // O req.user.id_user dependiendo de cómo lo mande tu middleware
                is_public: is_public !== undefined ? is_public : 1, // Se guarda el valor recibido, por defecto 1 (pública)
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
        const routines = await prisma.routine.findMany({
            where: {
                OR: [
                    { id_user: req.user.id },
                    { is_public: 1 }
                ]
            },
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

        if (!routine) {
            return res.status(404).json({ error: "Rutina no encontrada" });
        }

        if (routine.id_user !== req.user.id && routine.is_public !== 1 && req.user.role !== 'admin') {
            return res.status(403).json({ error: "No tienes permiso para ver esta rutina" });
        }

        res.json(routine);
    } catch (error) {
        next(error);
    }
};

const updateRoutine = async (req, res, next) => {
    const { id } = req.params;
    const { name, description, photo_url, is_public, exercises } = req.body;

    try {
        const routineId = parseInt(id);

        const existingRoutine = await prisma.routine.findUnique({ where: { id_routine: routineId } });

        if (!existingRoutine) {
            return res.status(404).json({ error: "Rutina no encontrada" });
        }

        if (existingRoutine.id_user !== req.user.id && req.user.role !== 'admin') {
            return res.status(403).json({ error: "No tienes permiso para editar esta rutina" });
        }

        const updatedRoutine = await prisma.$transaction(async (tx) => {
            
            const routine = await tx.routine.update({
                where: { id_routine: routineId },
                data: {
                    name,
                    description,
                    photo_url: photo_url || null,
                    is_public: is_public !== undefined ? is_public : existingRoutine.is_public
                }
            });

            await tx.routineExercise.deleteMany({
                where: { id_routine: routineId }
            });

            if (exercises && exercises.length > 0) {
                const exercisesToInsert = exercises.map(ex => ({
                    id_routine: routineId,
                    id_exercise: ex.id_exercise,
                    target_sets: ex.target_sets,
                    target_reps: ex.target_reps,
                    target_weight: ex.target_weight || null
                }));

                await tx.routineExercise.createMany({
                    data: exercisesToInsert
                });
            }

            return await tx.routine.findUnique({
                where: { id_routine: routineId },
                include: { exercises: { include: { exercise: true } } }
            });
        });

        res.json({ message: "Rutina actualizada correctamente", data: updatedRoutine });

    } catch (error) {
        next(error);
    }
};

module.exports = { createRoutine, getUserRoutines, deleteRoutine, getRoutineById, updateRoutine };