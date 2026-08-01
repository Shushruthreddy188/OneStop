import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getWishlist } from '../api/wishlist';
import { useAuth } from '../auth/AuthContext';

export const WISHLIST_QUERY_KEY = ['wishlist'] as const;

/** Wishlist query (only when signed in) plus a Set of saved product ids. */
export function useWishlist() {
  const { isAuthenticated } = useAuth();
  const query = useQuery({
    queryKey: WISHLIST_QUERY_KEY,
    queryFn: getWishlist,
    enabled: isAuthenticated,
  });

  const productIds = useMemo(
    () => new Set(query.data?.items.map((i) => i.productId) ?? []),
    [query.data],
  );

  return { ...query, productIds };
}
