import express from 'express';
import healthRouter from './routes/health.js';
import itemsRouter from './routes/items.js';

const app = express();

app.use(express.json());

app.use('/health', healthRouter);
app.use('/api/items', itemsRouter);

// 404 for anything unmatched
app.use((req, res) => {
  res.status(404).json({ error: 'Not found', path: req.originalUrl });
});

// Centralized error handler
app.use((err, req, res, next) => {
  console.error(err);
  res.status(err.status ?? 500).json({
    error: err.status ? err.message : 'Internal server error',
  });
});

export default app;
