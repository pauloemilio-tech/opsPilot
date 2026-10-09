# OpsPilot frontend

Angular 22 single-page application for the OpsPilot operational intelligence MVP.

For the product overview, complete local setup, backend configuration, API surface, and MVP limitations, see the [repository README](../README.md).

## Development

Start the backend first, then run:

```bash
npm install
npm start
```

Open `http://localhost:4200`. The development server proxies `/api` to `http://localhost:8080` through `proxy.conf.json`.

## Validation

```bash
npm test
npm run build
```

Unit and component tests run with Vitest through Angular's unit-test builder. No end-to-end test runner is configured in the current MVP.
