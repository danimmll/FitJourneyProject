const express = require('express');
const router = express.Router();
const workoutCtrl = require('../controllers/workoutController');
const { authenticateToken } = require('../middlewares/auth');
const { validateWorkout } = require('../middlewares/validators');

router.get('/history', authenticateToken, workoutCtrl.getWorkoutHistory);
router.post('/', authenticateToken, validateWorkout, workoutCtrl.createWorkout);
router.delete('/:id', authenticateToken, workoutCtrl.deleteWorkout);
router.get('/:id', authenticateToken, workoutCtrl.getWorkoutById);

module.exports = router;