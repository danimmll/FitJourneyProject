const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const getAllUsers = async (req, res, next) => {
    try {
        const users = await prisma.user.findMany({
            where: { active: 1 },
            select: { id_user: true, name: true, email: true, role: true, photo_url: true }
        });
        res.json(users);
    } catch (error) {
        next(error);
    }
};

const getProfile = async (req, res, next) => {
    try {
        const user = await prisma.user.findUnique({
            where: { id_user: req.user.id }
        });
        if (!user || user.active === 0) return res.status(404).json({ error: "Usuario no encontrado" });
        
        const { password, ...userData } = user;
        res.json(userData);
    } catch (error) {
        next(error);
    }
};

const updateProfile = async (req, res, next) => {
    try {
        const updatedUser = await prisma.user.update({
            where: { id_user: req.user.id },
            data: req.body
        });
        const { password, ...userData } = updatedUser;
        res.json(userData);
    } catch (error) {
        next(error); 
    }
};

const deleteUser = async (req, res, next) => {
    const { id } = req.params;
    try {
        await prisma.user.update({
            where: { id_user: parseInt(id) },
            data: { active: 0 }
        });
        res.json({ message: "Usuario desactivado correctamente" });
    } catch (error) {
        next(error);
    }
};

module.exports = { getAllUsers, getProfile, updateProfile, deleteUser };