# freebuff1

A small Node.js / Express API.

## Requirements

- Node.js 20+

## Setup

```bash
npm install
cp .env.example .env   # optional, PORT defaults to 3000
```

## Run

```bash
npm start   # production
npm run dev # reload on file changes
```

The server listens on `http://localhost:3000`.

## Endpoints

| Method | Path             | Description                    |
| ------ | ---------------- | ------------------------------ |
| GET    | `/health`        | Health check                   |
| GET    | `/api/items`     | List items                     |
| GET    | `/api/items/:id` | Get one item                   |
| POST   | `/api/items`     | Create an item `{ "name": "" }`|
| PATCH  | `/api/items/:id` | Rename an item `{ "name": "" }`|
| DELETE | `/api/items/:id` | Delete an item                 |

### Example

```bash
curl -X POST http://localhost:3000/api/items \
  -H 'Content-Type: application/json' \
  -d '{"name":"first"}'

curl http://localhost:3000/api/items
```

## Notes

Items are stored in memory, so they reset when the server restarts. Swap the store in
`src/routes/items.js` for a database when you need persistence.

## Project layout

```
src/
  app.js            Express app and middleware
  server.js         Entrypoint / listen
  routes/
    health.js       GET /health
    items.js        /api/items CRUD
```
