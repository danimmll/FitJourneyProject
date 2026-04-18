const express = require('express');
const router = express.Router();
const userCtrl = require('../controllers/userController');
const { authenticateToken, isAdmin } = require('../middlewares/auth');

router.get('/', authenticateToken, isAdmin, userCtrl.getAllUsers);
router.get('/profile', authenticateToken, userCtrl.getProfile);
router.put('/profile', authenticateToken, userCtrl.updateProfile);
router.delete('/:id', authenticateToken, isAdmin, userCtrl.deleteUser);

module.exports = router;