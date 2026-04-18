const express = require('express');
const router = express.Router();
const exCtrl = require('../controllers/exerciseController');
const { authenticateToken, isAdmin } = require('../middlewares/auth');

router.get('/', authenticateToken, exCtrl.getExercises);

router.post('/', authenticateToken, isAdmin, exCtrl.createExercise);
router.put('/:id', authenticateToken, isAdmin, exCtrl.updateExercise);
router.delete('/:id', authenticateToken, isAdmin, exCtrl.deleteExercise);

module.exports = router;