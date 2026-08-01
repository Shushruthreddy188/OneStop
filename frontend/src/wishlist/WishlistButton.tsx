import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { addToWishlist, removeFromWishlist, type Wishlist } from '../api/wishlist';
import { useAuth } from '../auth/AuthContext';
import { useWishlist, WISHLIST_QUERY_KEY } from './useWishlist';
import './WishlistButton.css';

/** Heart toggle to save/unsave a product. Sends signed-out users to login. */
export default function WishlistButton({ productId }: { productId: number }) {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { productIds } = useWishlist();
  const saved = productIds.has(productId);

  const mutation = useMutation({
    mutationFn: () => (saved ? removeFromWishlist(productId) : addToWishlist(productId)),
    onSuccess: (wishlist: Wishlist) => queryClient.setQueryData(WISHLIST_QUERY_KEY, wishlist),
  });

  function handleClick() {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    mutation.mutate();
  }

  return (
    <button
      type="button"
      className={`wishlist-btn${saved ? ' saved' : ''}`}
      onClick={handleClick}
      disabled={mutation.isPending}
      aria-label={saved ? 'Remove from wishlist' : 'Save to wishlist'}
      title={saved ? 'Saved — click to remove' : 'Save to wishlist'}
    >
      {saved ? '♥' : '♡'}
    </button>
  );
}
