import jwt from 'jsonwebtoken';

// Simulação de donos de app (use DB real na prática)
export const fakeDB = {
  'app1': {
    owner: 'Martim',
    editors: ['Henrique'],
    viewers: ['Rodrigo']
  },
  'app2': {
    owner: 'Henrique',
    editors: [],
    viewers: ['Martim']
  },
  'app3': {
    owner: 'Rodrigo',
    editors: ['Henrique'],
    viewers: ['']
  }
};

const getResourceRelations = (appid) => fakeDB[appid] || null;

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
    const relations = getResourceRelations(req.params.appid);
    if (!relations) {
      return res.status(404).json({ error: 'Application not found' });
    }
    const userId = req.user.id;

    let rel = null;
    if (relations.owner === userId) rel = 'owner';
    else if (relations.editors?.includes(userId)) rel = 'editor';
    else if (relations.viewers?.includes(userId)) rel = 'viewer';

    const permissions = {
      owner: ['read', 'update', 'create'],
      editor: ['read', 'update'],
      viewer: ['read']
    };

    if (!rel || !permissions[rel].includes(action)) {
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
