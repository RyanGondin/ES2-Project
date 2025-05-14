import express from 'express';
import dotenv from 'dotenv';
import routes from './routes/index.js';
import { errorHandler, requestLogger } from './middleware/index.js';

dotenv.config();

const app = express();
app.use(express.json());
app.use(requestLogger);
app.use('/api', routes);
app.use(errorHandler);

app.listen(3000, () => {
  console.log('API running on http://localhost:3000');
});
