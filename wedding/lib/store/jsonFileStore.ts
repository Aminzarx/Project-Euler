import { mkdir, readFile, writeFile } from 'node:fs/promises';
import path from 'node:path';

const DATA_DIR = path.join(process.cwd(), 'data');

/**
 * Minimal JSON-file-backed list persistence for local/self-hosted demo use.
 * Not suited to serverless (read-only/ephemeral filesystem) — swap for a
 * real database repository before deploying there. See
 * /docs/future-extension.md for the migration path.
 */
export async function readJsonList<T>(fileName: string): Promise<T[]> {
  try {
    const raw = await readFile(path.join(DATA_DIR, fileName), 'utf-8');
    return JSON.parse(raw) as T[];
  } catch {
    return [];
  }
}

export async function appendJsonList<T>(fileName: string, item: T): Promise<T> {
  const list = await readJsonList<T>(fileName);
  list.push(item);
  await mkdir(DATA_DIR, { recursive: true });
  await writeFile(path.join(DATA_DIR, fileName), JSON.stringify(list, null, 2), 'utf-8');
  return item;
}
