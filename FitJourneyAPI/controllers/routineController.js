const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const DEFAULT_ROUTINE_IMAGE = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=1200&auto=format&fit=crop";

const normalizeRoutineImage = (photoUrl) => {
    if (typeof photoUrl !== 'string') return DEFAULT_ROUTINE_IMAGE;
    const trimmed = photoUrl.trim();
    return trimmed.length > 0 ? trimmed : DEFAULT_ROUTINE_IMAGE;
};

const createRoutine = async (req, res, next) => {
    try {
        const { name, description, photo_url, is_public, exercises } = req.body;

        const routine = await prisma.routine.create({
            data: {
                name,
                description,
                photo_url: normalizeRoutineImage(photo_url),
                id_user: req.user.id, 
                is_public: is_public !== undefined ? is_public : 1,
                exercises: {
                    create: exercises && exercises.length > 0 ? exercises.map(ex => ({
                        id_exercise: ex.id_exercise,
                        target_sets: ex.target_sets,
                        target_reps: ex.target_reps,
                        target_weight: ex.target_weight
                    })) : [] 
                }
            },
            include: { exercises: { include: { exercise: true } } }
        });

        res.status(201).json({ message: "Rutina creada con éxito", routine });
    } catch (error) {
        console.log(error);
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
        console.log(error);
        next(error);
    }
};

const deleteRoutine = async (req, res, next) => {
    const { id } = req.params;
    try {
        const routine = await prisma.routine.findUnique({ where: { id_routine: parseInt(id) } });

        if (!routine || (routine.id_user !== req.user.id && req.user.role !== 'admin')) {
            return res.status(403).json({ error: "No tienes permiso para eliminar esta rutina" });
        }

        await prisma.routine.delete({
            where: { id_routine: parseInt(id) }
        });

        res.json({ message: "Rutina eliminada físicamente" });
    } catch (error) {
        console.log(error);
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
        console.log(error);
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
                    photo_url: normalizeRoutineImage(photo_url),
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
        console.log(error);
        next(error);
    }
};

module.exports = { createRoutine, getUserRoutines, deleteRoutine, getRoutineById, updateRoutine };