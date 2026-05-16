const { PrismaClient } = require('@prisma/client');
const bcrypt = require('bcryptjs');
const prisma = new PrismaClient();

/**
 * Obtiene lista de todos los usuarios activos
 * GET /users
 * Excluye contraseña y solo retorna activos
 */
const getAllUsers = async (req, res, next) => {
    try {
        // Busca usuarios activos, excluyendo admins y contraseña
        const users = await prisma.user.findMany({
            select: { id_user: true, name: true, email: true, role: true, photo_url: true, goal: true, height: true, weight: true, active: true },
            where: { active: 1, role: { not: 'admin' } }
        });

        res.json(users);
    } catch (error) {
        console.log(error);
        next(error);
    }
};

/**
 * Obtiene el perfil del usuario autenticado
 * GET /profile
 * Retorna datos del usuario sin contraseña
 * Valida que usuario exista y esté activo
 */
const getProfile = async (req, res, next) => {
    try {
        // Busca usuario por su ID desde el token JWT
        const user = await prisma.user.findUnique({
            where: { id_user: req.user.id }
        });
        // Valida que usuario exista y no esté desactivado
        if (!user || user.active === 0) return res.status(404).json({ error: "Usuario no encontrado" });

        // Excluye contraseña antes de retornar
        const { password, ...userData } = user;
        res.json(userData);
    } catch (error) {
        console.log(error);
        next(error);
    }
};

/**
 * Desactiva un usuario por su ID
 * DELETE /users/:id
 * No elimina datos, solo marca como inactivo
 */
const deleteUser = async (req, res, next) => {
    const { id } = req.params;
    try {
        // Marca usuario como inactivo (active = 0)
        await prisma.user.update({
            where: { id_user: parseInt(id) },
            data: { active: 0 }
        });

        res.json({ message: "Usuario desactivado correctamente" });
    } catch (error) {
        console.log(error);
        next(error);
    }
};

/**
 * Actualiza perfil del usuario autenticado
 * PUT /profile
 * Permite cambiar: nombre, altura, peso, objetivo, foto y contraseña
 * Solo valida y encripta si contraseña se proporciona
 */
const updateProfile = async (req, res, next) => {
    try {
        const { name, height, weight, goal, photo_url, password } = req.body;

        // Construye dinámicamente objeto de actualización
        let dataToUpdate = {};
        if (name) dataToUpdate.name = name;
        if (height !== undefined && height !== null) dataToUpdate.height = Number(height);
        if (weight !== undefined && weight !== null) dataToUpdate.weight = Number(weight);
        if (goal !== undefined) dataToUpdate.goal = goal;
        if (photo_url !== undefined) dataToUpdate.photo_url = photo_url;

        // Si proporciona contraseña, la encripta antes de actualizar
        if (password && password.trim() !== "") {
            const salt = await bcrypt.genSalt(12);
            dataToUpdate.password = await bcrypt.hash(password, salt);
        }

        // Actualiza datos en BD
        const updatedUser = await prisma.user.update({
            where: { id_user: req.user.id },
            data: dataToUpdate
        });

        // Excluye contraseña antes de retornar
        const { password: pw, ...userData } = updatedUser;
        res.json(userData);
    } catch (error) {
        console.log(error);
        next(error);
    }
};

/**
 * Actualiza perfil de cualquier usuario (admin only)
 * PUT /users/:id
 * Permite cambiar: nombre, altura, peso, objetivo, foto, role y estado activo
 * Valida que al menos un campo válido sea proporcionado
 */
const updateUserByAdmin = async (req, res, next) => {
    const { id } = req.params;
    try {
        const { name, height, weight, goal, photo_url, active, role } = req.body;

        // Construye objeto de actualización solo con campos válidos
        let dataToUpdate = {};
        if (name) dataToUpdate.name = name;
        if (height !== undefined && height !== null) dataToUpdate.height = Number(height);
        if (weight !== undefined && weight !== null) dataToUpdate.weight = Number(weight);
        if (goal !== undefined) dataToUpdate.goal = goal;
        if (photo_url !== undefined) dataToUpdate.photo_url = photo_url;
        if (role) dataToUpdate.role = role;

        // Valida que active sea 0 o 1 antes de actualizar
        if (typeof active === "number" && (active === 0 || active === 1)) {
            dataToUpdate.active = active;
        }

        // Retorna error si no hay campos válidos para actualizar
        if (Object.keys(dataToUpdate).length === 0) {
            return res.status(400).json({ error: "No se recibieron campos válidos para actualizar" });
        }

        // Actualiza usuario en BD
        const updatedUser = await prisma.user.update({
            where: { id_user: parseInt(id) },
            data: dataToUpdate
        });
        // Excluye contraseña antes de retornar
        const { password, ...userData } = updatedUser;
        res.json(userData);
    } catch (error) {
        console.log(error);
        next(error);
    }
};

/**
 * Desactiva la cuenta del usuario autenticado
 * POST /profile/deactivate
 * Requiere contraseña correcta como confirmación de seguridad
 * Marca usuario como inactivo sin eliminar datos
 */
const deactivateMyAccount = async (req, res, next) => {
    try {
        const { password } = req.body;
        
        // Busca usuario actual autenticado
        const user = await prisma.user.findUnique({
            where: { id_user: req.user.id }
        });

        if (!user) return res.status(404).json({ error: "Usuario no encontrado" });

        // Valida contraseña antes de permitir desactivación
        const validPassword = await bcrypt.compare(password, user.password);
        if (!validPassword) return res.status(401).json({ error: "Contraseña incorrecta" });

        // Marca cuenta como inactiva
        await prisma.user.update({
            where: { id_user: req.user.id },
            data: { active: 0 }
        });

        res.json({ message: "Cuenta desactivada correctamente" });
    } catch (error) {
        console.log(error);
        next(error);
    }
};

module.exports = { getAllUsers, getProfile, updateProfile, deleteUser, updateUserByAdmin, deactivateMyAccount };