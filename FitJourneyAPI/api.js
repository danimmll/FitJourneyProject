require('dotenv').config();
const express = require('express');
const cors = require('cors');

const app = express();

app.use(cors());
app.use(express.json());

const authRoutes = require('./routes/authRoutes');
const userRoutes = require('./routes/userRoutes');
const exerciseRoutes = require('./routes/exerciseRoutes');
const favoriteRoutes = require('./routes/favoriteRoutes');
const routineRoutes = require('./routes/routineRoutes');  
const workoutRoutes = require('./routes/workoutRoutes');  

app.use('/auth', authRoutes);
app.use('/users', userRoutes);
app.use('/exercises', exerciseRoutes);
app.use('/favorites', favoriteRoutes); 
app.use('/routines', routineRoutes);   
app.use('/workouts', workoutRoutes);  

app.get('/', (req, res) => res.send("FitJourney API"));

app.use((req, res) => {
    res.status(404).json({ error: "Ruta no encontrada" });
});

const errorHandler = require('./middlewares/errorHandler');
app.use(errorHandler);

const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {
    console.log(`Servidor funcionando en el puerto ${PORT}`);
});