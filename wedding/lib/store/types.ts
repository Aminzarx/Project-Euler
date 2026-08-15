export type GuestbookEntry = {
  id: string;
  name: string;
  message: string;
  createdAt: string;
};

export interface GuestbookRepository {
  list(): Promise<GuestbookEntry[]>;
  add(entry: { name: string; message: string }): Promise<GuestbookEntry>;
}

export type RsvpSide = 'bride' | 'groom';
export type RsvpStatus = 'attending' | 'maybe' | 'declined';

export type RsvpEntry = {
  id: string;
  status: RsvpStatus;
  fullName: string;
  message?: string;
  side?: RsvpSide;
  createdAt: string;
};

export interface RsvpRepository {
  list(): Promise<RsvpEntry[]>;
  add(entry: Omit<RsvpEntry, 'id' | 'createdAt'>): Promise<RsvpEntry>;
}
