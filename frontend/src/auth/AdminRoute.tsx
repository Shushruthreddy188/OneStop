import { Navigate } from 'react-router';
import type { ReactNode } from 'react';
import { useAuth } from './AuthContext';

/** Guards admin-only routes. Non-admins are bounced home; signed-out to login. */
export default function AdminRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (!user?.roles?.includes('ROLE_ADMIN')) return <Navigate to="/" replace />;
  return <>{children}</>;
}
