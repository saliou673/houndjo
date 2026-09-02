import { AxiosError } from 'axios';
import type { ValidationErrorResponseDTO } from '@api-client';

/**
 * The backend's ValidationErrorResponseDTO puts a generic title in `message`
 * (e.g. "Authentication Error") and the actual resolved description in
 * `errors.message` (e.g. "Email or password invalid"). Prefer the latter.
 */
export function extractApiErrorMessage(error: unknown, fallback: string): string {
  if (!(error instanceof AxiosError)) return fallback;

  const data = error.response?.data as ValidationErrorResponseDTO | undefined;
  return data?.errors?.message ?? data?.message ?? fallback;
}
