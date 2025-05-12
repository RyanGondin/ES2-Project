const express = require('express');
const router = express.Router();
const controller = require('../controllers/index');

// Define routes
router.post('/:appid', controller.createApp);
router.put('/:appid', controller.updateApp);
router.get('/:appid', controller.getApp);
router.post('/', controller.createApp);
router.get('/', controller.getAllApps);

// Export the router
module.exports = router;