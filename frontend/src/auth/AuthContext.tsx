import { createContext, useContext, useMemo, useState, type ReactNode } from 'react';
import type { AuthResponse, User } from '../api/auth';

const TOKEN_KEY = 'onestop.accessToken'; // read by the axios interceptor in api/client.ts
const USER_KEY = 'onestop.user';

interface AuthContextValue {
  user: User | null;
  isAuthenticated: boolean;
  signIn: (res: AuthResponse) => void;
  signOut: () => void;
  setUser: (user: User) => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function readStoredUser(): User | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as User;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY));
  const [user, setUserState] = useState<User | null>(() => readStoredUser());

  const value = useMemo<AuthContextValue>(() => ({
    user,
    isAuthenticated: !!token,
    signIn: (res: AuthResponse) => {
      localStorage.setItem(TOKEN_KEY, res.accessToken);
      localStorage.setItem(USER_KEY, JSON.stringify(res.user));
      setToken(res.accessToken);
      setUserState(res.user);
    },
    signOut: () => {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      setToken(null);
      setUserState(null);
    },
    setUser: (updated: User) => {
      localStorage.setItem(USER_KEY, JSON.stringify(updated));
      setUserState(updated);
    },
  }), [token, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
