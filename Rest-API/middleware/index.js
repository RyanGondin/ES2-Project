import jwt from 'jsonwebtoken';

// Simulação de donos de app (use DB real na prática)
const getResourceOwner = (appid) => {
  const fakeDB = {
    'app123': 'user1',
    'app456': 'user2',
  };
  return fakeDB[appid] || null;
};

// Logging simples
export const requestLogger = (req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.originalUrl} by ${req.user?.id || 'unauthenticated'}`);
  next();
};

// Autenticação JWT
export const authenticate = (req, res, next) => {
  const authHeader = req.headers.authorization;
  if (!authHeader) return res.status(401).json({ error: 'Missing Authorization Header' });

  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET || 'default_secret');
    req.user = decoded;
    next();
  } catch (err) {
    console.log('Invalid token:', err.message);
    return res.status(401).json({ error: 'Unauthorized' });
  }
};

// Autorização baseada em relações (ReBAC)
export const authorize = (action) => {
  return (req, res, next) => {
    const owner = getResourceOwner(req.params.appid);
    if (!owner) {
      console.log(`App ${req.params.appid} not found`);
      return res.status(404).json({ error: 'Application not found' });
    }

    if (req.user.id !== owner) {
      console.log(`Access denied for user ${req.user.id} to ${req.params.appid}`);
      return res.status(403).json({ error: 'Forbidden: insufficient privileges' });
    }

    next();
  };
};

// Handler global de erros
export const errorHandler = (err, req, res, next) => {
  console.error('Internal server error:', err.stack);
  res.status(500).json({ error: 'Internal Server Error' });
};
