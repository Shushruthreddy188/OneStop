import { Navigate, useLocation } from 'react-router';
import type { ReactNode } from 'react';
import { useAuth } from './AuthContext';

/** Redirects to /login (remembering where the user was headed) when signed out. */
export default function ProtectedRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  return <>{children}</>;
}
