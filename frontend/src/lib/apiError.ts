import { AxiosError } from 'axios';

interface ApiErrorBody {
  message?: string;
  fields?: Record<string, string>;
}

/** Extract a human-readable message from an axios/API error. */
export function apiErrorMessage(err: unknown, fallback = 'Something went wrong'): string {
  if (err instanceof AxiosError) {
    if (err.response) {
      const body = err.response.data as ApiErrorBody | undefined;
      if (body?.fields) {
        const first = Object.entries(body.fields)[0];
        if (first) return `${first[0]}: ${first[1]}`;
      }
      if (body?.message) return body.message;
      return `Request failed (${err.response.status})`;
    }
    return 'Network error — is the backend running?';
  }
  return err instanceof Error ? err.message : fallback;
}
