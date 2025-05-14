import express from 'express';
import jwt from 'jsonwebtoken';
import { authenticate, authorize } from '../middleware/index.js';

const router = express.Router();

// Simulação de usuários
const users = [
  { id: 'user1', username: 'alice', password: '1234' },
  { id: 'user2', username: 'bob', password: 'abcd' },
];

// LOGIN
router.post('/login', (req, res) => {
  const { username, password } = req.body;

  const user = users.find(u => u.username === username && u.password === password);
  if (!user) {
    return res.status(401).json({ error: 'Invalid credentials' });
  }

  const token = jwt.sign(
    { id: user.id, username: user.username },
    process.env.JWT_SECRET || 'default_secret',
    { expiresIn: process.env.JWT_EXPIRES_IN || '1h' }
  );

  console.log(`Login realizado para ${user.username}`);
  res.json({ token });
});

// Criar app
router.post('/app', authenticate, (req, res) => {
  console.log(`Criar app para ${req.user.id}`);
  res.status(201).json({ message: 'App criada com sucesso' });
});

// Listar apps
router.get('/apps', authenticate, (req, res) => {
  console.log(`Listar apps de ${req.user.id}`);
  res.json([{ appid: 'app123', owner: req.user.id }]);
});

// Criar password
router.post('/password/:appid', authenticate, authorize('create'), (req, res) => {
  console.log(`Password criada para ${req.params.appid} por ${req.user.id}`);
  res.status(201).json({ message: 'Password criada com sucesso' });
});

// Atualizar password
router.put('/password/:appid', authenticate, authorize('update'), (req, res) => {
  console.log(`Password atualizada para ${req.params.appid} por ${req.user.id}`);
  res.json({ message: 'Password atualizada com sucesso' });
});

// Obter password
router.get('/password/:appid', authenticate, authorize('read'), (req, res) => {
  console.log(`Password lida para ${req.params.appid} por ${req.user.id}`);
  res.json({ password: 'example-password' });
});

export default router;
