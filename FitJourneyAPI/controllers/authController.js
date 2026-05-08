const { PrismaClient } = require('@prisma/client');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

const prisma = new PrismaClient();

const register = async (req, res, next) => {
    try {
        const { email, name, password, gender, goal, height, weight, birth_date, photo_url } = req.body;

        const existingUser = await prisma.user.findUnique({ where: { email } });

        if (existingUser) {
            if (existingUser.active === 1) {
                return res.status(400).json({ error: "El email ya está registrado y en uso." });
            } else {
                return res.status(409).json({ 
                    error: "Cuenta inactiva detectada.", 
                    action_required: "PROMPT_REACTIVATION" 
                });
            }
        }

        const hashedPassword = await bcrypt.hash(password, 10);
        const newUser = await prisma.user.create({
            data: {
                email, name, password: hashedPassword, gender, goal,
                height: height ? parseFloat(height) : null,
                weight: weight ? parseFloat(weight) : null,
                birth_date: birth_date ? new Date(birth_date) : null,
                photo_url
            }
        });

        const token = jwt.sign({ id: newUser.id_user, role: newUser.role }, process.env.JWT_SECRET, { expiresIn: '24h' });
        const { password: _, ...userWithoutPassword } = newUser;

        res.status(201).json({ message: "Usuario registrado con éxito", token, user: userWithoutPassword });

    } catch (error) {
        next(error); 
    }
};

const login = async (req, res, next) => {
    try {
        const { email, password } = req.body;

        const user = await prisma.user.findUnique({ where: { email } });
        
        if (!user || user.active === 0) {
            return res.status(401).json({ error: "Credenciales inválidas o cuenta desactivada" });
        }

        const validPassword = await bcrypt.compare(password, user.password);
        if (!validPassword) {
            return res.status(401).json({ error: "Credenciales inválidas" });
        }

        const token = jwt.sign({ id: user.id_user, role: user.role }, process.env.JWT_SECRET, { expiresIn: '24h' });
        const { password: _, ...userWithoutPassword } = user;

        res.json({ message: "Login exitoso", token, user: userWithoutPassword });

    } catch (error) {
        next(error);
    }
};

const reactivateAccount = async (req, res, next) => {
    try {
        const { email, password } = req.body;

        const user = await prisma.user.findUnique({ where: { email } });
        if (!user || user.active === 1) return res.status(400).json({ error: "La cuenta no es válida para reactivación" });

        const validPassword = await bcrypt.compare(password, user.password);
        if (!validPassword) return res.status(401).json({ error: "Contraseña incorrecta. No puedes reactivar esta cuenta." });

        // Reactivamos usuario
        const reactivatedUser = await prisma.user.update({
            where: { email },
            data: { active: 1 }
        });

        // Reactivamos todas sus rutinas
        await prisma.routine.updateMany({
            where: { id_user: reactivatedUser.id_user },
            data: { active: 1 }
        });

        // Reactivamos todos sus entrenamientos
        await prisma.workout.updateMany({
            where: { id_user: reactivatedUser.id_user },
            data: { active: 1 }
        });

        const token = jwt.sign({ id: reactivatedUser.id_user, role: reactivatedUser.role }, process.env.JWT_SECRET, { expiresIn: '24h' });
        const { password: _, ...userWithoutPassword } = reactivatedUser;

        res.json({ message: "Cuenta reactivada con éxito", token, user: userWithoutPassword });

    } catch (error) {
        next(error);
    }
};

const deleteAccount = async (req, res, next) => {
    try {
        const { email, password } = req.body;

        const user = await prisma.user.findUnique({ where: { email } });
        if (!user || user.active === 1) return res.status(400).json({ error: "Solo se pueden borrar físicamente cuentas inactivas" });

        const validPassword = await bcrypt.compare(password, user.password);
        if (!validPassword) return res.status(401).json({ error: "Contraseña incorrecta. No puedes eliminar esta cuenta." });

        await prisma.user.delete({
            where: { email }
        });

        res.json({ message: "Cuenta y datos históricos eliminados para siempre. Ya puedes registrarte de nuevo." });

    } catch (error) {
        next(error);
    }
};

module.exports = { register, login, reactivateAccount, deleteAccount };