/** Mirrors `basePath` in next.config.mjs. Client fetches must be prefixed with this. */
export const BASE_PATH = '/Wedding';

export function apiUrl(path: string): string {
  return `${BASE_PATH}/api/${path.replace(/^\/+/, '')}`;
}
