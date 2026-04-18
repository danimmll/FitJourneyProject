const express = require('express');
const router = express.Router();
const favCtrl = require('../controllers/favoriteController');
const { authenticateToken } = require('../middlewares/auth');

router.post('/toggle', authenticateToken, favCtrl.toggleFavorite);
router.get('/', authenticateToken, favCtrl.getUserFavorites);

module.exports = router;