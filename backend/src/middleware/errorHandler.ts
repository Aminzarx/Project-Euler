import type { ErrorRequestHandler } from 'express';

/** Every engine/repository error surfaces here as a 400 with its message — none of them throw
 *  anything containing sensitive internals, so this is safe to pass straight through. Anything
 *  truly unexpected still gets logged server-side for investigation. */
export const errorHandler: ErrorRequestHandler = (err, _req, res, _next) => {
  const message = err instanceof Error ? err.message : 'Unexpected error';
  // eslint-disable-next-line no-console
  console.error(err);
  res.status(400).json({ error: message });
};
