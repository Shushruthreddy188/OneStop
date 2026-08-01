import { useQuery } from '@tanstack/react-query';
import { getCart } from '../api/cart';
import { useAuth } from '../auth/AuthContext';

export const CART_QUERY_KEY = ['cart'] as const;

/** Cart query, enabled only when signed in (the endpoint requires a token). */
export function useCart() {
  const { isAuthenticated } = useAuth();
  return useQuery({
    queryKey: CART_QUERY_KEY,
    queryFn: getCart,
    enabled: isAuthenticated,
  });
}
