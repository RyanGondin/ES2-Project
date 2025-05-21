import express from 'express';
import dotenv from 'dotenv';
import routes from './routes/index.js';
import { errorHandler, requestLogger } from './middleware/index.js';
import swaggerUi from 'swagger-ui-express';
import swaggerJsdoc from 'swagger-jsdoc';

dotenv.config();

const app = express();
app.use(express.json());

// Swagger setup
const swaggerOptions = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'ReBAC API',
      version: '1.0.0',
      description: 'API with Relationship-Based Access Control (ReBAC)',
    },
    servers: [{ url: 'http://localhost:3000/api' }],
    components: {
      securitySchemes: {
        bearerAuth: {
          type: 'http',
          scheme: 'bearer',
          bearerFormat: 'JWT'
        }
      }
    }
  },
  apis: ['./routes/*.js'], // Path to your route files for JSDoc comments
};
const swaggerSpec = swaggerJsdoc(swaggerOptions);
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec));

app.use('/api', routes);
app.use(errorHandler);

app.listen(3000, () => {
  console.log('API running on http://localhost:3000');
  console.log('Swagger docs at http://localhost:3000/api-docs');
});
