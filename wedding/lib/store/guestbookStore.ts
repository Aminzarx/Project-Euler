import { randomUUID } from 'node:crypto';
import { appendJsonList, readJsonList } from '@/lib/store/jsonFileStore';
import type { GuestbookEntry, GuestbookRepository } from '@/lib/store/types';

const FILE = 'guestbook.json';

export const guestbookStore: GuestbookRepository = {
  async list() {
    const entries = await readJsonList<GuestbookEntry>(FILE);
    return entries.sort((a, b) => b.createdAt.localeCompare(a.createdAt));
  },
  async add({ name, message }) {
    const entry: GuestbookEntry = {
      id: randomUUID(),
      name,
      message,
      createdAt: new Date().toISOString(),
    };
    return appendJsonList(FILE, entry);
  },
};
