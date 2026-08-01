import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router';
import { listAddresses } from '../api/auth';
import { placeOrder, type CheckoutRequest } from '../api/orders';
import { useCart, CART_QUERY_KEY } from '../cart/useCart';
import { formatMoney } from '../lib/format';
import { apiErrorMessage } from '../lib/apiError';
import '../pages/AuthForm.css';

type FormValues = Omit<CheckoutRequest, 'idempotencyKey'>;

export default function CheckoutPage() {
  const { data: cart } = useCart();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  // One idempotency key per checkout attempt: safe to retry without double-ordering.
  const [idempotencyKey] = useState(() => crypto.randomUUID());

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormValues>({
    defaultValues: { country: 'India', paymentMethod: 'COD' },
  });

  // Prefill from the customer's default saved address, if any.
  const addressesQuery = useQuery({ queryKey: ['addresses'], queryFn: listAddresses });
  useEffect(() => {
    const def = addressesQuery.data?.[0];
    if (def) {
      reset({
        recipientName: '',
        phone: '',
        line1: def.line1,
        line2: def.line2 ?? '',
        city: def.city,
        state: def.state ?? '',
        postalCode: def.postalCode ?? '',
        country: def.country,
        paymentMethod: 'COD',
      });
    }
  }, [addressesQuery.data, reset]);

  const mutation = useMutation({
    mutationFn: (values: FormValues) => placeOrder({ ...values, idempotencyKey }),
    onSuccess: (order) => {
      queryClient.invalidateQueries({ queryKey: CART_QUERY_KEY });
      navigate(`/orders/${order.id}`, { replace: true, state: { justPlaced: true } });
    },
  });

  if (!cart || cart.items.length === 0) {
    return (
      <section>
        <h1>Checkout</h1>
        <p className="muted">Your cart is empty.</p>
        <p><Link to="/products">Browse products →</Link></p>
      </section>
    );
  }

  return (
    <section className="auth-form wide">
      <h1>Checkout</h1>

      <h2>Order summary</h2>
      <ul className="address-list">
        {cart.items.map((i) => (
          <li key={i.itemId}>
            {i.quantity} × {i.productName ?? `#${i.productId}`} — {formatMoney(i.lineTotal)}
          </li>
        ))}
      </ul>
      <p className="subtotal">Total: <strong>{formatMoney(cart.subtotal)}</strong></p>

      <h2>Delivery address</h2>
      <form onSubmit={handleSubmit((v) => mutation.mutate(v))}>
        <label>
          Recipient name
          <input {...register('recipientName', { required: 'Required' })} />
          {errors.recipientName && <span className="field-error">{errors.recipientName.message}</span>}
        </label>
        <label>
          Phone
          <input {...register('phone')} />
        </label>
        <label>
          Address line 1
          <input {...register('line1', { required: 'Required' })} />
          {errors.line1 && <span className="field-error">{errors.line1.message}</span>}
        </label>
        <label>
          Address line 2
          <input {...register('line2')} />
        </label>
        <div className="row">
          <label>
            City
            <input {...register('city', { required: 'Required' })} />
            {errors.city && <span className="field-error">{errors.city.message}</span>}
          </label>
          <label>
            State
            <input {...register('state')} />
          </label>
        </div>
        <div className="row">
          <label>
            Postal code
            <input {...register('postalCode')} />
          </label>
          <label>
            Country
            <input {...register('country', { required: 'Required' })} />
          </label>
        </div>
        <label>
          Payment method
          <select {...register('paymentMethod')}>
            <option value="COD">Cash on delivery</option>
            <option value="CARD">Card (simulated)</option>
          </select>
        </label>

        {mutation.isError && <p className="error">{apiErrorMessage(mutation.error)}</p>}

        <button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? 'Placing order…' : `Place order · ${formatMoney(cart.subtotal)}`}
        </button>
      </form>
    </section>
  );
}
