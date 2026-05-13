const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const toggleFavorite = async (req, res, next) => {
    try {
        const { id_exercise } = req.body;
        const id_user = req.user.id;

        const exerciseId = parseInt(id_exercise, 10);
        if (Number.isNaN(exerciseId)) {
            return res.status(400).json({ error: "ID de ejercicio no válido" });
        }

        const existing = await prisma.favorite.findFirst({
            where: { id_user, id_exercise: exerciseId }
        });

        if (existing) {
            await prisma.favorite.delete({ where: { id_favorite: existing.id_favorite } });
            return res.json({ message: "Eliminado de favoritos", isFavorite: false });
        } else {
            await prisma.favorite.create({ data: { id_user, id_exercise: exerciseId } });
            return res.status(201).json({ message: "Añadido a favoritos", isFavorite: true });
        }
    } catch (error) {
        next(error);
    }
};

const getUserFavorites = async (req, res, next) => {
    try {
        const favorites = await prisma.favorite.findMany({
            where: { id_user: req.user.id },
            include: { exercise: true }
        });
        res.json(favorites.map(fav => fav.exercise));
    } catch (error) {
        next(error);
    }
};

module.exports = { toggleFavorite, getUserFavorites };