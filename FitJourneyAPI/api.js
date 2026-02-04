require('dotenv').config(); 
const express = require('express');
const { Pool } = require('pg'); 
const cors = require('cors');
const bodyParser = require('body-parser');
const bcrypt = require('bcryptjs'); 
const jwt = require('jsonwebtoken'); 

const app = express();
app.use(cors());
app.use(bodyParser.json());


const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: { rejectUnauthorized: false } 
});

const JWT_SECRET = process.env.JWT_SECRET; 


const authenticate = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  
  // Si no me mandan cabecera de autorización corto el acceso
  if (!authHeader) {
    return res.status(401).send('Falta el token de autorización');
  }

  // El token suele venir como bearer y algo mas así que lo separo y me quedo con la parte 1
  const token = authHeader.split(' ')[1];

  jwt.verify(token, JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(403).send('Token no válido');
    }
    req.user = user;
    next(); 
  });
};

app.post('/users', async (req, res) => {
  const { id_user, email, name, password, gender, goal, height, weight, birth_date, photoUrl, role } = req.body;

  if (!id_user || !email || !name || !password) {
    return res.status(400).send('Faltan datos obligatorios (email, pass, nombre...)');
  }

  try {
    const checkEmail = await pool.query('SELECT * FROM users WHERE email = $1', [email]);
    
    if (checkEmail.rows.length > 0) {
      return res.status(400).send('El usuario ya existe');
    }

    // Encriptar la contraseña 
    const hashedPassword = await bcrypt.hash(password, 10);

    const userRole = role ? role : 'user';

    const text = 'INSERT INTO users (id_user, email, name, password, gender, goal, height, weight, birth_date, photo_url, role) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11) RETURNING *';
    const values = [id_user, email, name, hashedPassword, gender, goal, height, weight, birth_date, photoUrl ? photoUrl : null, userRole];

    const result = await pool.query(text, values);
    const newUser = result.rows[0];

    const token = jwt.sign({ id_user: newUser.id_user, role: newUser.role }, JWT_SECRET, { expiresIn: '24h' });

    res.status(201).json({
      user: {
        id_user: newUser.id_user,
        email: newUser.email,
        name: newUser.name,
        role: newUser.role 
      },
      token: token
    });

  } catch (error) {
    console.log(error);
    res.status(500).send('Error al registrar usuario');
  }
});

app.post('/login', async (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(400).send('Introduce email y contraseña');
  }

  try {
    const result = await pool.query('SELECT * FROM users WHERE email = $1', [email]);

    if (result.rows.length === 0) {
      return res.status(404).send('Usuario no encontrado');
    }

    const user = result.rows[0];

    const validPassword = await bcrypt.compare(password, user.password);
    if (!validPassword) {
      return res.status(401).send('Contraseña incorrecta');
    }

    const token = jwt.sign({ id_user: user.id_user, role: user.role }, JWT_SECRET, { expiresIn: '24h' });

    res.json({
      user: {
        id_user: user.id_user,
        email: user.email,
        name: user.name,
        photoUrl: user.photo_url,
        role: user.role
      },
      token: token
    });

  } catch (error) {
    console.log(error);
    res.status(500).send('Error en el servidor');
  }
});

app.get('/profile', authenticate, async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM users WHERE id_user = $1', [req.user.id_user]);
    
    if (result.rows.length === 0) {
      return res.status(404).send('Usuario no encontrado');
    }

    const user = result.rows[0];
    delete user.password; 

    res.json(user);

  } catch (error) {
    console.log(error);
    res.status(500).send('Error al obtener perfil');
  }
});

//ARRANCA EL SERVER
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log('Servidor activo en el puerto ' + PORT);
});