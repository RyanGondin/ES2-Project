import express from 'express';
import jwt from 'jsonwebtoken';
import { authenticate, authorize, fakeDB } from '../middleware/index.js';
import { fetchExternalApps, mockExternalApiFailure } from '../services/externalApi.js';

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
 *   - name: Audit
 *     description: Audit and logging functionality
 *   - name: Import
 *     description: Import data from external APIs
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
 *       401:
 *         description: Invalid credentials
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Invalid credentials"
 *       400:
 *         description: Bad request - missing username or password
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 */
router.post('/login', (req, res) => {
  const { username, password } = req.body;
  const user = users.find(u => u.username === username && u.password === password);
  
  // Mock de logging para auditoria
  const auditEntry = {
    timestamp: new Date().toISOString(),
    username: username,
    ip: req.ip || req.connection.remoteAddress,
    userAgent: req.get('User-Agent'),
    status: user ? 'success' : 'failed'
  };
  
  loginAuditLog.push(auditEntry);
  console.log('Login attempt logged:', auditEntry);
  
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
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 appid:
 *                   type: string
 *                 name:
 *                   type: string
 *                 owner:
 *                   type: string
 *                 editors:
 *                   type: array
 *                   items:
 *                     type: string
 *                 viewers:
 *                   type: array
 *                   items:
 *                     type: string
 *       400:
 *         description: Bad request - app name and owner are required
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "App name and owner are required"
 *       401:
 *         description: Unauthorized - missing or invalid token
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Missing Authorization Header"
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
 *                   role:
 *                     type: string
 *                     enum: [owner, editor, viewer]
 *       401:
 *         description: Unauthorized - missing or invalid token
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Missing Authorization Header"
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
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               password:
 *                 type: string
 *     responses:
 *       201:
 *         description: Password created
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: "Password criada com sucesso"
 *       400:
 *         description: Bad request - password is required
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Password is required"
 *       401:
 *         description: Unauthorized - missing or invalid token
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Missing Authorization Header"
 *       403:
 *         description: Forbidden - insufficient privileges (not owner)
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Forbidden: insufficient privileges"
 *       404:
 *         description: App not found
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "App not found"
 */
router.post('/password/:appid', authenticate, authorize('create'), (req, res) => {
  const { password } = req.body;
  const app = fakeDB[req.params.appid];
  if (!app) {
    return res.status(404).json({ error: 'App not found' });
  }
  if (!password) {
    return res.status(400).json({ error: 'Password is required' });
  }
  app.password = password;
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
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               password:
 *                 type: string
 *     responses:
 *       200:
 *         description: Password updated
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: "Password atualizada com sucesso"
 *       400:
 *         description: Bad request - password is required
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Password is required"
 *       401:
 *         description: Unauthorized - missing or invalid token
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Missing Authorization Header"
 *       403:
 *         description: Forbidden - insufficient privileges (not owner or editor)
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Forbidden: insufficient privileges"
 *       404:
 *         description: App not found
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "App not found"
 */
router.put('/password/:appid', authenticate, authorize('update'), (req, res) => {
  const { password } = req.body;
  const app = fakeDB[req.params.appid];
  if (!app) {
    return res.status(404).json({ error: 'App not found' });
  }
  if (!password) {
    return res.status(400).json({ error: 'Password is required' });
  }
  app.password = password;
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
 *       401:
 *         description: Unauthorized - missing or invalid token
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Missing Authorization Header"
 *       403:
 *         description: Forbidden - insufficient privileges (not owner, editor, or viewer)
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Forbidden: insufficient privileges"
 *       404:
 *         description: Password not found or app not found
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Password not found"
 */
router.get('/password/:appid', authenticate, authorize('read'), (req, res) => {
  const app = fakeDB[req.params.appid];
  if (!app || !app.password) {
    return res.status(404).json({ error: 'Password not found' });
  }
  console.log(`Password lida para ${req.params.appid} por ${req.user.id}`);
  res.json({ password: app.password });
});

// Mock para auditoria de logins
const loginAuditLog = [];

/**
 * @swagger
 * /audit/logins:
 *   get:
 *     tags: [Audit]
 *     summary: Get login audit logs (mock)
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Login audit logs
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 type: object
 *                 properties:
 *                   timestamp:
 *                     type: string
 *                   username:
 *                     type: string
 *                   status:
 *                     type: string
 *                     enum: [success, failed]
 *                   ip:
 *                     type: string
 */
router.get('/audit/logins', authenticate, (req, res) => {
  console.log(`Audit logs acessados por ${req.user.id}`);
  res.json(loginAuditLog);
});

/**
 * @swagger
 * /import/apps:
 *   post:
 *     tags: [Import]
 *     summary: Import apps from external API
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: false
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               simulate_failure:
 *                 type: boolean
 *                 description: Set to true to simulate API failure
 *     responses:
 *       200:
 *         description: Apps imported successfully
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                 imported_count:
 *                   type: number
 *                 apps:
 *                   type: array
 *                   items:
 *                     type: object
 *       500:
 *         description: External API error
 *       401:
 *         description: Unauthorized
 */
router.post('/import/apps', authenticate, async (req, res) => {
  console.log('User from JWT:', req.user);
  
  try {
    const { simulate_failure } = req.body;
    
    if (simulate_failure) {
      mockExternalApiFailure();
    }
    
    console.log('Iniciando importação de apps da API externa...');
    const externalApps = await fetchExternalApps();
    
    // Converter apps externas para formato interno
    const importedApps = externalApps.map(extApp => {
      const newAppId = `imported_${Object.keys(fakeDB).length + 1}`;
      
      const newApp = {
        name: extApp.name,
        owner: req.user.id, // Usuário que fez a importação torna-se owner
        editors: [],
        viewers: [],
        external_id: extApp.external_id,
        imported_at: new Date().toISOString(),
        original_owner: extApp.owner
      };
      
      fakeDB[newAppId] = newApp;
      
      return {
        appid: newAppId,
        ...newApp
      };
    });
    
    console.log(`${importedApps.length} apps importadas com sucesso por ${req.user.id}`);
    
    res.json({
      message: 'Apps imported successfully',
      imported_count: importedApps.length,
      apps: importedApps
    });
    
  } catch (error) {
    console.error('Erro na importação de apps:', error.message);
    res.status(500).json({ 
      error: 'Failed to import apps from external API',
      details: error.message 
    });
  }
});

module.exports = app;
export default router;


