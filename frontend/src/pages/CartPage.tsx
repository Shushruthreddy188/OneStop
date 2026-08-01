import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router';
import { clearCart, removeItem, updateItem, type Cart } from '../api/cart';
import { useCart, CART_QUERY_KEY } from '../cart/useCart';
import { formatMoney } from '../lib/format';
import './CartPage.css';

export default function CartPage() {
  const { data: cart, isLoading, isError } = useCart();
  const queryClient = useQueryClient();

  const setCart = (updated: Cart) => queryClient.setQueryData(CART_QUERY_KEY, updated);

  const updateMutation = useMutation({
    mutationFn: ({ itemId, quantity }: { itemId: number; quantity: number }) =>
      updateItem(itemId, quantity),
    onSuccess: setCart,
  });
  const removeMutation = useMutation({ mutationFn: removeItem, onSuccess: setCart });
  const clearMutation = useMutation({ mutationFn: clearCart, onSuccess: setCart });

  const busy = updateMutation.isPending || removeMutation.isPending || clearMutation.isPending;

  function changeQty(itemId: number, quantity: number) {
    if (quantity < 1) {
      removeMutation.mutate(itemId);
    } else {
      updateMutation.mutate({ itemId, quantity });
    }
  }

  if (isLoading) return <p>Loading…</p>;
  if (isError) return <p className="error">Could not load your cart.</p>;
  if (!cart) return null;

  if (cart.items.length === 0) {
    return (
      <section>
        <h1>Your cart</h1>
        <p className="muted">Your cart is empty.</p>
        <p>
          <Link to="/products">Browse products →</Link>
        </p>
      </section>
    );
  }

  return (
    <section>
      <div className="products-header">
        <h1>Your cart</h1>
        <span className="muted">{cart.totalItems} items</span>
      </div>

      <ul className="cart-list">
        {cart.items.map((item) => (
          <li key={item.itemId} className="cart-row">
            <Link to={`/products/${item.productId}`} className="cart-name">
              {item.productName ?? `Product #${item.productId}`}
            </Link>
            <span className="muted">{formatMoney(item.unitPrice)}</span>
            <div className="qty">
              <button type="button" disabled={busy}
                onClick={() => changeQty(item.itemId, item.quantity - 1)}>−</button>
              <span>{item.quantity}</span>
              <button type="button" disabled={busy}
                onClick={() => changeQty(item.itemId, item.quantity + 1)}>+</button>
            </div>
            <span className="line-total">{formatMoney(item.lineTotal)}</span>
            <button type="button" className="link-button" disabled={busy}
              onClick={() => removeMutation.mutate(item.itemId)}>Remove</button>
          </li>
        ))}
      </ul>

      <div className="cart-footer">
        <button type="button" className="link-button" disabled={busy}
          onClick={() => clearMutation.mutate()}>Clear cart</button>
        <div className="subtotal">
          Subtotal: <strong>{formatMoney(cart.subtotal)}</strong>
        </div>
      </div>

      <Link to="/checkout" className="checkout-link">
        Proceed to checkout →
      </Link>
    </section>
  );
}
