const { PrismaClient } = require('@prisma/client');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

const prisma = new PrismaClient();

/**
 * Registra un nuevo usuario en el sistema
 * POST /auth/register
 * Valida campos obligatorios, encripta contraseña y genera JWT
 * Maneja reactivación de cuentas inactivas
 */
const register = async (req, res, next) => {
    try {
        const { email, name, password, gender, goal, height, weight, birth_date, photo_url } = req.body;

        // Valida campos obligatorios para crear usuario
        if (!email || !name || !password) {
            return res.status(400).json({ error: "Email, nombre y contraseña son obligatorios" });
        }

        // Verifica si el email ya está registrado
        const existingUser = await prisma.user.findUnique({ where: { email } });

        if (existingUser) {
            // Si la cuenta ya existe y está activa no dejamos registrarse otra vez
            if (existingUser.active === 1) {
                return res.status(400).json({ error: "El email ya está registrado y en uso." });
            } else {
                // Si existe pero está inactiva avisamos al frontend para ofrecer reactivarla
                return res.status(409).json({
                    error: "Cuenta inactiva detectada.",
                    action_required: "PROMPT_REACTIVATION"
                });
            }
        }

        // Encripta la contraseña con bcrypt (12 rounds de salt)
        const hashedPassword = await bcrypt.hash(password, 12);

        // Crea nuevo usuario con datos validados y formateados
        const newUser = await prisma.user.create({
            data: {
                email,
                name,
                password: hashedPassword,
                gender,
                goal,
                height: height !== undefined && height !== null && height !== '' ? parseFloat(height) : null,
                weight: weight !== undefined && weight !== null && weight !== '' ? parseFloat(weight) : null,

                // Convierte string a objeto Date para almacenar correctamente en BD
                birth_date: birth_date ? new Date(birth_date) : null,

                photo_url
            }
        });
        
        // Genera JWT token válido por 24 horas
        const token = jwt.sign(
            { id: newUser.id_user, role: newUser.role },
            process.env.JWT_SECRET,
            { expiresIn: '24h' }
        );

        // Excluye la contraseña antes de enviar al cliente
        const { password: _, ...userWithoutPassword } = newUser;

        res.status(201).json({
            message: "Usuario registrado con éxito",
            token,
            user: userWithoutPassword
        });

    } catch (error) {
        console.log(error);
        next(error);
    }
};

/**
 * Autentica un usuario existente con email y contraseña
 * POST /auth/login
 * Valida credenciales, verifica estado de cuenta y retorna JWT
 */
const login = async (req, res, next) => {
    try {
        const { email, password } = req.body;

        // Busca usuario por email
        const user = await prisma.user.findUnique({ where: { email } });

        // Rechaza login si usuario no existe o está desactivado
        if (!user || user.active === 0) {
            return res.status(401).json({ error: "Credenciales inválidas o cuenta desactivada" });
        }

        // Compara contraseña ingresada con hash almacenado
        const validPassword = await bcrypt.compare(password, user.password);

        if (!validPassword) {
            return res.status(401).json({ error: "Credenciales inválidas" });
        }

        // Genera JWT token para mantener sesión autenticada (24h)
        const token = jwt.sign(
            { id: user.id_user, role: user.role },
            process.env.JWT_SECRET,
            { expiresIn: '24h' }
        );

        // Excluye contraseña usando destructuring con Rest operator
        const { password: _, ...userWithoutPassword } = user;

        res.json({
            message: "Login exitoso",
            token,
            user: userWithoutPassword
        });

    } catch (error) {
        console.log(error);
        next(error);
    }
};

/**
 * Reactiva una cuenta inactiva con verificación de contraseña
 * POST /auth/reactivate
 * Solo permite reactivar cuentas que existen y están desactivadas
 */
const reactivateAccount = async (req, res, next) => {
    try {
        const { email, password } = req.body;

        // Busca usuario por email
        const user = await prisma.user.findUnique({ where: { email } });

        // Valida que la cuenta exista y esté inactiva para reactivarla
        if (!user || user.active === 1) {
            return res.status(400).json({ error: "La cuenta no es válida para reactivación" });
        }

        // Valida contraseña antes de permitir reactivación
        const validPassword = await bcrypt.compare(password, user.password);

        if (!validPassword) {
            return res.status(401).json({
                error: "Contraseña incorrecta. No puedes reactivar esta cuenta."
            });
        }

        // Activa la cuenta en BD (establece active = 1)
        const reactivatedUser = await prisma.user.update({
            where: { email },
            data: { active: 1 }
        });

        // Genera nuevo token JWT con sesión activa
        const token = jwt.sign(
            { id: reactivatedUser.id_user, role: reactivatedUser.role },
            process.env.JWT_SECRET,
            { expiresIn: '24h' }
        );

        // Excluye contraseña antes de enviar respuesta
        const { password: _, ...userWithoutPassword } = reactivatedUser;

        res.json({
            message: "Cuenta reactivada con éxito",
            token,
            user: userWithoutPassword
        });

    } catch (error) {
        console.log(error);
        next(error);
    }
};

/**
 * Elimina una cuenta y sus datos de forma permanente
 * DELETE /auth/delete
 * Solo permite eliminar cuentas inactivas (soft-delete seguro)
 */
const deleteAccount = async (req, res, next) => {
    try {
        const { email, password } = req.body;

        // Busca usuario para validar que existe y está inactivo
        const user = await prisma.user.findUnique({ where: { email } });

        if (!user || user.active === 1) {
            return res.status(400).json({
                error: "Solo se pueden borrar físicamente cuentas inactivas"
            });
        }

        // Valida contraseña como confirmación final antes de eliminar datos
        const validPassword = await bcrypt.compare(password, user.password);

        if (!validPassword) {
            return res.status(401).json({
                error: "Contraseña incorrecta. No puedes eliminar esta cuenta."
            });
        }

        // Borra permanentemente el usuario y todos sus datos asociados
        await prisma.user.delete({
            where: { email }
        });

        res.json({
            message: "Cuenta y datos históricos eliminados para siempre. Ya puedes registrarte de nuevo."
        });

    } catch (error) {
        console.log(error);
        next(error);
    }
};

module.exports = { register, login, reactivateAccount, deleteAccount };