import { Router } from 'express';

const router = Router();

// In-memory store, resets on restart. Swap for a database when you need persistence.
let items = [];
let nextId = 1;

router.get('/', (req, res) => {
  res.json(items);
});

router.get('/:id', (req, res) => {
  const item = items.find((i) => i.id === Number(req.params.id));
  if (!item) return res.status(404).json({ error: 'Item not found' });
  res.json(item);
});

router.post('/', (req, res) => {
  const { name } = req.body ?? {};
  if (typeof name !== 'string' || !name.trim()) {
    return res.status(400).json({ error: 'name is required' });
  }

  const item = { id: nextId++, name: name.trim(), createdAt: new Date().toISOString() };
  items.push(item);
  res.status(201).json(item);
});

router.patch('/:id', (req, res) => {
  const item = items.find((i) => i.id === Number(req.params.id));
  if (!item) return res.status(404).json({ error: 'Item not found' });

  const { name } = req.body ?? {};
  if (typeof name !== 'string' || !name.trim()) {
    return res.status(400).json({ error: 'name is required' });
  }

  item.name = name.trim();
  res.json(item);
});

router.delete('/:id', (req, res) => {
  const id = Number(req.params.id);
  const before = items.length;
  items = items.filter((i) => i.id !== id);

  if (items.length === before) return res.status(404).json({ error: 'Item not found' });
  res.status(204).end();
});

export default router;
