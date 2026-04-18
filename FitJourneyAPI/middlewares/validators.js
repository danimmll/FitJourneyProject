const { check, validationResult } = require('express-validator');

const handleValidationErrors = (req, res, next) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        return res.status(400).json({ error: "Datos no válidos", detalles: errors.array() });
    }
    next();
};

const validateRoutine = [
    check('name').notEmpty().withMessage('La rutina debe tener un nombre'),
    check('exercises').isArray({ min: 1 }).withMessage('Debe contener al menos un ejercicio'),
    handleValidationErrors
];

const validateWorkout = [
    check('id_routine').isInt().withMessage('ID de rutina no válido'),
    check('details').isArray({ min: 1 }).withMessage('El entrenamiento debe incluir detalles sobre él'),
    handleValidationErrors
];

module.exports = { validateRoutine, validateWorkout, handleValidationErrors };