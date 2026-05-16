const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

// URL por defecto para rutinas sin imagen especificada
const DEFAULT_ROUTINE_IMAGE = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=1200&auto=format&fit=crop";

/**
 * Normaliza URLs de imagen de rutina: valida string no vacío,
 * retorna URL por defecto si es inválida o está vacía
 */
const normalizeRoutineImage = (photoUrl) => {
    if (typeof photoUrl !== 'string') return DEFAULT_ROUTINE_IMAGE;
    const trimmed = photoUrl.trim();
    return trimmed.length > 0 ? trimmed : DEFAULT_ROUTINE_IMAGE;
};

/**
 * Crea una nueva rutina para el usuario autenticado
 * POST /routines
 * Incluye ejercicios con sets, reps y peso objetivo
 * Permite rutinas públicas o privadas
 */
const createRoutine = async (req, res, next) => {
    try {
        const { name, description, photo_url, is_public, exercises } = req.body;

        // Crea rutina con ejercicios asociados (relación one-to-many)
        const routine = await prisma.routine.create({
            data: {
                name,
                description,
                photo_url: normalizeRoutineImage(photo_url),
                id_user: req.user.id, 
                // Por defecto rutinas públicas (1) si no se especifica
                is_public: is_public !== undefined ? is_public : 1,
                // Crea ejercicios de rutina con parámetros objetivo
                exercises: {
                    create: exercises && exercises.length > 0 ? exercises.map(ex => ({
                        id_exercise: ex.id_exercise,
                        target_sets: ex.target_sets,
                        target_reps: ex.target_reps,
                        target_weight: ex.target_weight
                    })) : [] 
                }
            },
            // Retorna rutina con detalles completos de ejercicios
            include: { exercises: { include: { exercise: true } } }
        });

        res.status(201).json({ message: "Rutina creada con éxito", routine });
    } catch (error) {
        console.log(error);
        next(error);
    }
};

/**
 * Obtiene todas las rutinas del usuario y rutinas públicas
 * GET /routines
 * Retorna rutinas propias + rutinas públicas de otros usuarios
 */
const getUserRoutines = async (req, res, next) => {
    try {
        // Busca rutinas donde el usuario es propietario O rutinas públicas
        const routines = await prisma.routine.findMany({
            where: {
                OR: [
                    { id_user: req.user.id },
                    { is_public: 1 }
                ]
            },
            // Incluye detalles de ejercicios de cada rutina
            include: { exercises: { include: { exercise: true } } }
        });
        res.json(routines);
    } catch (error) {
        console.log(error);
        next(error);
    }
};

/**
 * Elimina una rutina de la base de datos
 * DELETE /routines/:id
 * Solo el propietario o admin pueden eliminar
 * Requiere autorización
 */
const deleteRoutine = async (req, res, next) => {
    const { id } = req.params;
    try {
        // Busca rutina para validar permisos
        const routine = await prisma.routine.findUnique({ where: { id_routine: parseInt(id) } });

        // Verifica que sea propietario o admin antes de eliminar
        if (!routine || (routine.id_user !== req.user.id && req.user.role !== 'admin')) {
            return res.status(403).json({ error: "No tienes permiso para eliminar esta rutina" });
        }

        // Elimina rutina de forma física (hard delete)
        await prisma.routine.delete({
            where: { id_routine: parseInt(id) }
        });

        res.json({ message: "Rutina eliminada físicamente" });
    } catch (error) {
        console.log(error);
        next(error);
    }
};

/**
 * Obtiene una rutina específica por ID
 * GET /routines/:id
 * Valida permisos: propietario, rutina pública, o admin
 * Retorna 404 si no existe, 403 si no hay permisos
 */
const getRoutineById = async (req, res, next) => {
    const { id } = req.params;
    try {
        // Busca rutina con sus ejercicios asociados
        const routine = await prisma.routine.findUnique({
            where: { id_routine: parseInt(id) },
            // Incluye detalles de ejercicios con información del ejercicio base
            include: { exercises: { include: { exercise: true } } }
        });

        if (!routine) {
            return res.status(404).json({ error: "Rutina no encontrada" });
        }

        // Valida permisos: propietario, admin, o rutina pública
        if (routine.id_user !== req.user.id && routine.is_public !== 1 && req.user.role !== 'admin') {
            return res.status(403).json({ error: "No tienes permiso para ver esta rutina" });
        }

        res.json(routine);
    } catch (error) {
        console.log(error);
        next(error);
    }
};

/**
 * Actualiza una rutina existente con nuevos ejercicios
 * PUT /routines/:id
 * Usa transacción para garantizar integridad (update + delete + create)
 * Solo propietario o admin pueden editar
 */
const updateRoutine = async (req, res, next) => {
    const { id } = req.params;
    const { name, description, photo_url, is_public, exercises } = req.body;

    try {
        const routineId = parseInt(id);

        // Valida que la rutina exista
        const existingRoutine = await prisma.routine.findUnique({ where: { id_routine: routineId } });

        if (!existingRoutine) {
            return res.status(404).json({ error: "Rutina no encontrada" });
        }

        // Verifica permisos de edición
        if (existingRoutine.id_user !== req.user.id && req.user.role !== 'admin') {
            return res.status(403).json({ error: "No tienes permiso para editar esta rutina" });
        }

        // Usa transacción para garantizar consistencia: actualiza datos + reemplaza ejercicios
        const updatedRoutine = await prisma.$transaction(async (tx) => {
            
            // Actualiza información básica de la rutina
            const routine = await tx.routine.update({
                where: { id_routine: routineId },
                data: {
                    name,
                    description,
                    photo_url: normalizeRoutineImage(photo_url),
                    is_public: is_public !== undefined ? is_public : existingRoutine.is_public
                }
            });

            // Elimina todos los ejercicios anteriores de la rutina
            await tx.routineExercise.deleteMany({
                where: { id_routine: routineId }
            });

            // Crea nuevos ejercicios si los proporciona
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

            // Retorna rutina actualizada con ejercicios nuevos
            return await tx.routine.findUnique({
                where: { id_routine: routineId },
                // Incluye detalles completos de ejercicios nuevos
                include: { exercises: { include: { exercise: true } } }
            });
        });

        // Retorna rutina con ejercicios actualizados
        res.json({ message: "Rutina actualizada correctamente", data: updatedRoutine });

    } catch (error) {
        console.log(error);
        next(error);
    }
};

module.exports = { createRoutine, getUserRoutines, deleteRoutine, getRoutineById, updateRoutine };