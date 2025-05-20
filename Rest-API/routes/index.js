import express from 'express';
import jwt from 'jsonwebtoken';
import { authenticate, authorize, fakeDB } from '../middleware/index.js';

const router = express.Router();

// Simulação de usuários
const users = [
  { id: 'Martim', username: 'Martim', password: 'qwerty' },
  { id: 'Henrique', username: 'Henrique', password: 'abcd' },
  { id: 'Rodrigo', username: 'Rodrigo', password: 'wxyz' } 
];
/**
 * @swagger
 * tags:
 *   - name: Auth
 *     description: Authentication and login
 *   - name: Apps
 *     description: Application management
 *   - name: Passwords
 *     description: Password management
 */

/**
 * @swagger
 * /login:
 *   post:
 *     tags: [Auth]
 *     summary: User login
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               username:
 *                 type: string
 *               password:
 *                 type: string
 *     responses:
 *       200:
 *         description: JWT token
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 token:
 *                   type: string
 */
router.post('/login', (req, res) => {
  const { username, password } = req.body;
  const user = users.find(u => u.username === username && u.password === password);
  if (!user) {
    return res.status(401).json({ error: 'Invalid credentials' });
  }

  // encontrar as roles
  const userRoles = [];
  for (const [appid, rel] of Object.entries(fakeDB)) {
    if (rel.owner === user.id) userRoles.push({ appid, role: 'owner' });
    if (rel.editors?.includes(user.id)) userRoles.push({ appid, role: 'editor' });
    if (rel.viewers?.includes(user.id)) userRoles.push({ appid, role: 'viewer' });
  }

  const token = jwt.sign(
    { id: user.id, username: user.username, roles: userRoles },
    process.env.JWT_SECRET || 'default_secret',
    { expiresIn: process.env.JWT_EXPIRES_IN || '1h' }
  );

  console.log(`Login realizado para ${user.username}`);
  res.json({ token });
});

/**
 * @swagger
 * /app:
 *   post:
 *     tags: [Apps]
 *     summary: Create a new app
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               name:
 *                 type: string
 *               owner:
 *                 type: string
 *               editors:
 *                 type: array
 *                 items:
 *                   type: string
 *               viewers:
 *                 type: array
 *                 items:
 *                   type: string
 *     responses:
 *       201:
 *         description: App created
 */
router.post('/app', authenticate, (req, res) => {
  const { name, owner, editors, viewers } = req.body;
  if (!name || !owner) {
    return res.status(400).json({ error: 'App name and owner are required' });
  }
  const newAppId = `app${Object.keys(fakeDB).length + 1}`;
  fakeDB[newAppId] = {
    name,
    owner,
    editors: Array.isArray(editors) ? editors : [],
    viewers: Array.isArray(viewers) ? viewers : []
  };
  console.log(`App ${newAppId} (${name}) criada para ${owner}`);
  res.status(201).json({ appid: newAppId, name, owner, editors: fakeDB[newAppId].editors, viewers: fakeDB[newAppId].viewers });
});

/**
 * @swagger
 * /apps:
 *   get:
 *     tags: [Apps]
 *     summary: List all apps for the authenticated user
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: List of apps
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 type: object
 *                 properties:
 *                   appid:
 *                     type: string
 *                   owner:
 *                     type: string
 */
router.get('/apps', authenticate, (req, res) => {
  const userId = req.user.id;
  const apps = Object.entries(fakeDB)
    .filter(([appid, rel]) =>
      rel.owner === userId ||
      rel.editors.includes(userId) ||
      rel.viewers.includes(userId)
    )
    .map(([appid, rel]) => ({
      appid,
      owner: rel.owner,
      role:
        rel.owner === userId
          ? 'owner'
          : rel.editors.includes(userId)
          ? 'editor'
          : 'viewer'
    }));

  res.json(apps);
});

/**
 * @swagger
 * /password/{appid}:
 *   post:
 *     tags: [Passwords]
 *     summary: Create a password for an app
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: appid
 *         required: true
 *         schema:
 *           type: string
 *         description: App ID
 *     responses:
 *       201:
 *         description: Password created
 */
router.post('/password/:appid', authenticate, authorize('create'), (req, res) => {
  console.log(`Password criada para ${req.params.appid} por ${req.user.id}`);
  res.status(201).json({ message: 'Password criada com sucesso' });
});

/**
 * @swagger
 * /password/{appid}:
 *   put:
 *     tags: [Passwords]
 *     summary: Update a password for an app
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: appid
 *         required: true
 *         schema:
 *           type: string
 *         description: App ID
 *     responses:
 *       200:
 *         description: Password updated
 */
router.put('/password/:appid', authenticate, authorize('update'), (req, res) => {
  console.log(`Password atualizada para ${req.params.appid} por ${req.user.id}`);
  res.json({ message: 'Password atualizada com sucesso' });
});

/**
 * @swagger
 * /password/{appid}:
 *   get:
 *     tags: [Passwords]
 *     summary: Get a password for an app
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: appid
 *         required: true
 *         schema:
 *           type: string
 *         description: App ID
 *     responses:
 *       200:
 *         description: Password retrieved
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 password:
 *                   type: string
 */
router.get('/password/:appid', authenticate, authorize('read'), (req, res) => {
  console.log(`Password lida para ${req.params.appid} por ${req.user.id}`);
  res.json({ password: 'example-password' });
});

export default router;
