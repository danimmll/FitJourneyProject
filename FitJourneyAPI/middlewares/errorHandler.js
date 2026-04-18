const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const errorHandler = async (err, req, res, next) => {
    console.error(`[ERROR:] ${req.method} ${req.originalUrl} >>`, err.message);

    try {
        await prisma.incidencia.create({
            data: {
                mensaje: err.message || "Error interno desconocido",
                endpoint: req.originalUrl,
                metodo: req.method,
                id_user: req.user ? req.user.id : null 
            }
        });
    } catch (dbError) {
        console.error("Fallo crítico: No se pudo guardar la incidencia en la BD:", dbError.message);
    }

    res.status(500).json({ error: "Ha ocurrido un error interno en el servidor." });
};

module.exports = errorHandler;