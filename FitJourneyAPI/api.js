/**
 * Entrada principal de la API FitJourney
 * - Configura Express, CORS y JSON body parser
 * - Registra rutas de la aplicación
 * - Configura Swagger UI para documentación
 * - Arranca el servidor en el puerto indicado
 */
const express = require('express');
const cors = require('cors');
require('dotenv').config();

// Rutas
const authRoutes = require('./routes/authRoutes');
const userRoutes = require('./routes/userRoutes');
const exerciseRoutes = require('./routes/exerciseRoutes');
const routineRoutes = require('./routes/routineRoutes');
const workoutRoutes = require('./routes/workoutRoutes');

const errorHandler = require('./middlewares/errorHandler');

const app = express();

app.use(cors());
app.use(express.json());

const swaggerUi = require('swagger-ui-express');
const swaggerJsdoc = require('swagger-jsdoc');

// Opciones de Swagger/OpenAPI — lee los JSDoc dentro de las rutas y controladores
const swaggerOptions = {
    definition: {
        openapi: '3.0.0',
        info: {
            title: 'FitJourney API',
            version: '1.0.0',
            description: 'Documentación de la API de FitJourney',
            contact: {
                name: 'Daniel Martín López'
            },
        },
        servers: [
            {
                url: 'http://localhost:3000',
                description: 'Servidor Local'
            },
            {
                url: 'https://fitjourneyproject.onrender.com',
                description: 'Servidor Producción (Render)'
            }
        ],
        components: {
            securitySchemes: {
                bearerAuth: {
                    type: 'http',
                    scheme: 'bearer',
                    bearerFormat: 'JWT',
                }
            }
        },
        security: [{
            bearerAuth: []
        }]
    },
        // Le indicamos que busque la documentación en los comentarios de estos archivos
        apis: ['./routes/*.js', './controllers/*.js'],
};

const swaggerDocs = swaggerJsdoc(swaggerOptions);
// Levantamos la interfaz de documentación Swagger en la ruta `/api-docs`
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerDocs));

// Montaje de routers: cada módulo gestiona su propio prefijo y documentación
app.use('/api/auth', authRoutes);       
app.use('/api/users', userRoutes);      
app.use('/api/exercises', exerciseRoutes);
app.use('/api/routines', routineRoutes);
app.use('/api/workouts', workoutRoutes);


// Arranque del servidor
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Servidor corriendo en el puerto ${PORT}`);
});