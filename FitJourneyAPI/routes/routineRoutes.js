const express = require('express');
const router = express.Router();
const routineCtrl = require('../controllers/routineController');
const { authenticateToken } = require('../middlewares/auth');
const { validateRoutine } = require('../middlewares/validators');

router.get('/', authenticateToken, routineCtrl.getUserRoutines);
router.post('/', authenticateToken, validateRoutine, routineCtrl.createRoutine);
router.delete('/:id', authenticateToken, routineCtrl.deleteRoutine);
router.get('/:id', authenticateToken, routineCtrl.getRoutineById);

module.exports = router;