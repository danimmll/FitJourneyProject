require('dotenv').config();
const express = require('express');
const admin = require('firebase-admin');
const cors = require('cors');
const bodyParser = require('body-parser');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

const serviceAccount = require('./serviceAccountKey.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});
const db = admin.firestore();

const app = express();
app.use(cors());
app.use(bodyParser.json());

const JWT_SECRET = process.env.JWT_SECRET;

const authenticate = (req, res, next) => {
  const authHeader = req.headers.authorization;
  if (!authHeader)
    return res.status(401).json({ message: 'No autorizado (sin token)' });

  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.user = decoded;
    next();
  } catch {
    return res.status(401).json({ message: 'Token inválido' });
  }
};

app.post('/users', async (req, res) => {
  const body = req.body;

  const requiredFields = [
    'id_user','email','name','password','gender',
    'goal','height','weight','birth_date'
  ];
  const missingFields = requiredFields.filter(f => !body[f]);
  if (missingFields.length > 0) {
    return res.status(400).json({
      message: 'Faltan datos obligatorios',
      missing_fields: missingFields
    });
  }

  try {
    const emailNormalized = body.email.trim().toLowerCase();

    const existingUser = await db.collection('users')
      .where('email', '==', emailNormalized)
      .get();
    if (!existingUser.empty)
      return res.status(400).json({ message: 'Email ya registrado' });

    const passwordHash = await bcrypt.hash(body.password, 10);

    const userDataToSave = {
      ...body,
      email: emailNormalized,
      password: passwordHash,
      register_date: admin.firestore.FieldValue.serverTimestamp()
    };

    await db.collection('users').doc(body.id_user).set(userDataToSave);

    const token = jwt.sign(
      { id_user: body.id_user, email: emailNormalized },
      JWT_SECRET,
      { expiresIn: '24h' }
    );

    const { password, ...userResponse } = body;

    res.status(201).json({ user: userResponse, token });

  } catch (err) {
    console.error('Error en /users:', err);
    res.status(500).json({ message: 'Error interno: ' + err.message });
  }
});

app.post('/login', async (req, res) => {
  const { email, password } = req.body;
  if (!email || !password)
    return res.status(400).json({ message: 'Faltan datos' });

  try {
    const emailNormalized = email.trim().toLowerCase();
    const query = await db.collection('users')
      .where('email', '==', emailNormalized)
      .get();
    if (query.empty)
      return res.status(404).json({ message: 'Usuario no encontrado' });

    const user = query.docs[0].data();
    const match = await bcrypt.compare(password, user.password);
    if (!match)
      return res.status(401).json({ message: 'Contraseña incorrecta' });

    const token = jwt.sign(
      { id_user: user.id_user, email: user.email },
      JWT_SECRET,
      { expiresIn: '24h' }
    );

    const { password: pw, ...userResponse } = user;

    res.status(200).json({ user: userResponse, token });

  } catch (err) {
    console.error('Error en /login:', err);
    res.status(500).json({ message: 'Error interno: ' + err.message });
  }
});

app.get('/profile', authenticate, async (req, res) => {
  const userId = req.user.id_user;
  try {
    const doc = await db.collection('users').doc(userId).get();
    if (!doc.exists)
      return res.status(404).json({ message: 'Usuario no encontrado' });

    const { password, ...userData } = doc.data();
    res.status(200).json(userData);

  } catch (err) {
    console.error('Error en /profile:', err);
    res.status(500).json({ message: 'Error interno: ' + err.message });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`🔥 Servidor FitJourney listo en el puerto ${PORT}`);
});
