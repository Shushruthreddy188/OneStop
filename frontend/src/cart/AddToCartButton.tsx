import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { addItem } from '../api/cart';
import { useAuth } from '../auth/AuthContext';
import { CART_QUERY_KEY } from './useCart';

interface Props {
  productId: number;
  quantity?: number;
  label?: string;
  disabled?: boolean;
}

/** Adds a product to the cart; sends signed-out users to login first. */
export default function AddToCartButton({ productId, quantity = 1, label = 'Add to cart', disabled = false }: Props) {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: () => addItem(productId, quantity),
    onSuccess: (cart) => {
      queryClient.setQueryData(CART_QUERY_KEY, cart);
    },
  });

  function handleClick() {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: '/products' } });
      return;
    }
    mutation.mutate();
  }

  return (
    <button
      type="button"
      className="add-to-cart"
      onClick={handleClick}
      disabled={mutation.isPending || disabled}
    >
      {mutation.isPending ? 'Adding…' : mutation.isSuccess ? 'Added ✓' : label}
    </button>
  );
}
