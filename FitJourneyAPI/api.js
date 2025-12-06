const express = require('express');
const admin = require('firebase-admin');
const cors = require('cors');
const bodyParser = require('body-parser');
const bcrypt = require('bcrypt');

const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const app = express();

app.use(cors());
app.use(bodyParser.json());

app.post('/users', async (req, res) => {
    const body = req.body;

    const requiredFields = [
        'id_user',      
        'email', 
        'name',
        'password',
        'gender', 
        'goal', 
        'height', 
        'weight', 
        'birth_date'
    ];

    const missingFields = [];

    requiredFields.forEach(field => {
        if (!body[field]) { 
            missingFields.push(field);
        }
    });

    if (missingFields.length > 0) {
        return res.status(400).json({
            message: 'Faltan datos obligatorios para el registro',
            missing_fields: missingFields
        });
    }

    try {
        const saltRounds = 10;
        const passwordHash = await bcrypt.hash(body.password, saltRounds);

        await db.collection('users').doc(body.id_user).set({
            id_user: body.id_user,
            email: body.email,
            password: passwordHash,
            name: body.name,
            gender: body.gender,
            goal: body.goal,
            height: Number(body.height), 
            weight: Number(body.weight), 
            birth_date: body.birth_date,
            register_date: admin.firestore.FieldValue.serverTimestamp()
        });

        res.status(201).json({ message: 'Perfil de usuario creado correctamente' });

    } catch (error) {
        console.error("Error al guardar:", error);
        res.status(500).send('Error interno al guardar usuario: ' + error.message);
    }
});

app.get('/users/:id', async (req, res) => {
    const userId = req.params.id;

    try {
        const doc = await db.collection('users').doc(userId).get();

        if (!doc.exists) {
            return res.status(404).json({ message: 'Usuario no encontrado en base de datos' });
        }

        const userData = doc.data();

        if(userData.password) delete userData.password; 

        if (userData.register_date && typeof userData.register_date.toDate === 'function') {
            userData.register_date = userData.register_date.toDate().toISOString().split('T')[0];
        }

        if (userData.birth_date && typeof userData.birth_date.toDate === 'function') {
            userData.birth_date = userData.birth_date.toDate().toISOString().split('T')[0];
        }

        res.status(200).json(userData);

    } catch (error) {
        console.error("Error al leer:", error);
        res.status(500).send('Error interno al obtener usuario: ' + error.message);
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`✅ Servidor FitJourney listo en el puerto ${PORT}`);
});