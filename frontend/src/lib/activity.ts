import { apiClient } from '../api/client';

const SESSION_KEY = 'onestop.sessionId';

function uuid(): string {
  return globalThis.crypto?.randomUUID?.()
    ?? `${Date.now()}-${Math.random().toString(36).slice(2)}-${Math.random().toString(36).slice(2)}`;
}

/** Stable per-browser session id so anonymous activity can be correlated. */
function sessionId(): string {
  let id = localStorage.getItem(SESSION_KEY);
  if (!id) {
    id = uuid();
    localStorage.setItem(SESSION_KEY, id);
  }
  return id;
}

export type ActivityType =
  | 'SEARCH_PERFORMED'
  | 'PRODUCT_IMPRESSION'
  | 'PRODUCT_VIEWED'
  | 'WISHLIST_ADDED'
  | 'CART_ADDED'
  | 'CHECKOUT_STARTED';

interface TrackDetails {
  productId?: number;
  query?: string;
  position?: number;
}

/**
 * Fire-and-forget activity capture. The axios interceptor attaches the JWT when
 * signed in (so the server can attribute to a customer); otherwise it's anonymous.
 * Failures are swallowed — analytics must never break the UI.
 */
export function track(eventType: ActivityType, details: TrackDetails = {}): void {
  const event = {
    eventId: uuid(),
    eventType,
    sessionId: sessionId(),
    source: 'web',
    occurredAt: new Date().toISOString(),
    ...details,
  };
  apiClient.post('/api/activity/events', { events: [event] }).catch(() => {
    /* ignore */
  });
}
