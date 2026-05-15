const { PrismaClient } = require('@prisma/client');
const bcrypt = require('bcryptjs');
const prisma = new PrismaClient();

const getAllUsers = async (req, res, next) => {
    try {
        const users = await prisma.user.findMany({
            select: { id_user: true, name: true, email: true, role: true, photo_url: true, goal: true, height: true, weight: true, active: true },
            where: { active: 1, role: { not: 'admin' } }
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

const updateProfile = async (req, res, next) => {
    try {
        const { name, height, weight, goal, photo_url, password } = req.body;

        let dataToUpdate = {};
        if (name) dataToUpdate.name = name;
        if (height !== undefined && height !== null) dataToUpdate.height = Number(height);
        if (weight !== undefined && weight !== null) dataToUpdate.weight = Number(weight);
        if (goal !== undefined) dataToUpdate.goal = goal;
        if (photo_url !== undefined) dataToUpdate.photo_url = photo_url;

        if (password && password.trim() !== "") {
            const salt = await bcrypt.genSalt(12);
            dataToUpdate.password = await bcrypt.hash(password, salt);
        }

        const updatedUser = await prisma.user.update({
            where: { id_user: req.user.id },
            data: dataToUpdate
        });

        const { password: pw, ...userData } = updatedUser;
        res.json(userData);
    } catch (error) {
        next(error);
    }
};

const updateUserByAdmin = async (req, res, next) => {
    const { id } = req.params;
    try {
        const { name, height, weight, goal, photo_url, active, role } = req.body;

        let dataToUpdate = {};
        if (name) dataToUpdate.name = name;
        if (height !== undefined && height !== null) dataToUpdate.height = Number(height);
        if (weight !== undefined && weight !== null) dataToUpdate.weight = Number(weight);
        if (goal !== undefined) dataToUpdate.goal = goal;
        if (photo_url !== undefined) dataToUpdate.photo_url = photo_url;
        if (role) dataToUpdate.role = role;

        if (typeof active === "number" && (active === 0 || active === 1)) {
            dataToUpdate.active = active;
        }

        if (Object.keys(dataToUpdate).length === 0) {
            return res.status(400).json({ error: "No se recibieron campos válidos para actualizar" });
        }

        const updatedUser = await prisma.user.update({
            where: { id_user: parseInt(id) },
            data: dataToUpdate
        });
        const { password, ...userData } = updatedUser;
        res.json(userData);
    } catch (error) {
        next(error);
    }
};

const deactivateMyAccount = async (req, res, next) => {
    try {
        const { password } = req.body;
        
        const user = await prisma.user.findUnique({
            where: { id_user: req.user.id }
        });

        if (!user) return res.status(404).json({ error: "Usuario no encontrado" });

        const validPassword = await bcrypt.compare(password, user.password);
        if (!validPassword) return res.status(401).json({ error: "Contraseña incorrecta" });

        await prisma.user.update({
            where: { id_user: req.user.id },
            data: { active: 0 }
        });

        res.json({ message: "Cuenta desactivada correctamente" });
    } catch (error) {
        next(error);
    }
};

module.exports = { getAllUsers, getProfile, updateProfile, deleteUser, updateUserByAdmin, deactivateMyAccount };