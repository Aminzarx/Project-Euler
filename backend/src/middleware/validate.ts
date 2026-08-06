import type { RequestHandler } from 'express';
import type { ZodSchema } from 'zod';

/** Parses+validates req.body against `schema`, replacing it with the parsed (and thus typed)
 *  value on success, or responding 400 with field-level detail on failure. */
export function validateBody(schema: ZodSchema): RequestHandler {
  return (req, res, next) => {
    const result = schema.safeParse(req.body);
    if (!result.success) {
      res.status(400).json({ error: 'Validation failed', details: result.error.flatten() });
      return;
    }
    req.body = result.data;
    next();
  };
}
