import { randomUUID } from 'node:crypto';
import { appendJsonList, readJsonList } from '@/lib/store/jsonFileStore';
import type { RsvpEntry, RsvpRepository } from '@/lib/store/types';

const FILE = 'rsvp.json';

export const rsvpStore: RsvpRepository = {
  async list() {
    return readJsonList<RsvpEntry>(FILE);
  },
  async add(input) {
    const entry: RsvpEntry = {
      id: randomUUID(),
      createdAt: new Date().toISOString(),
      ...input,
    };
    return appendJsonList(FILE, entry);
  },
};
