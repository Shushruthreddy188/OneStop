import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router';
import { listAddresses } from '../api/addresses';
import { placeOrder, type CheckoutRequest } from '../api/orders';
import { validateCoupon, type CouponValidation } from '../api/coupons';
import { useCart, CART_QUERY_KEY } from '../cart/useCart';
import { formatMoney } from '../lib/format';
import { apiErrorMessage } from '../lib/apiError';
import '../pages/AuthForm.css';

type FormValues = Omit<CheckoutRequest, 'idempotencyKey'>;

function createIdempotencyKey() {
  return globalThis.crypto.randomUUID?.()
    ?? `${Date.now()}-${Math.random().toString(36).slice(2)}-${Math.random().toString(36).slice(2)}`;
}

export default function CheckoutPage() {
  const { data: cart } = useCart();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  // One idempotency key per checkout attempt: safe to retry without double-ordering.
  const [idempotencyKey] = useState(createIdempotencyKey);

  const [couponInput, setCouponInput] = useState('');
  const [coupon, setCoupon] = useState<CouponValidation | null>(null);
  const subtotal = cart?.subtotal ?? 0;
  const discount = coupon?.valid ? coupon.discountAmount : 0;
  const payable = Math.max(0, subtotal - discount);

  const couponMutation = useMutation({
    mutationFn: () => validateCoupon(couponInput.trim(), subtotal),
    onSuccess: (result) => setCoupon(result),
  });

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
    mutationFn: (values: FormValues) =>
      placeOrder({ ...values, couponCode: coupon?.valid ? coupon.code : undefined, idempotencyKey }),
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
      <div className="checkout-totals">
        <div className="total-line"><span>Subtotal</span><span>{formatMoney(subtotal)}</span></div>

        <div className="coupon-row">
          <input
            placeholder="Coupon code (try WELCOME10)"
            value={couponInput}
            onChange={(e) => setCouponInput(e.target.value.toUpperCase())}
          />
          {coupon?.valid ? (
            <button type="button" onClick={() => { setCoupon(null); setCouponInput(''); }}>Remove</button>
          ) : (
            <button type="button" disabled={!couponInput.trim() || couponMutation.isPending}
              onClick={() => couponMutation.mutate()}>
              {couponMutation.isPending ? 'Checking…' : 'Apply'}
            </button>
          )}
        </div>
        {coupon && !coupon.valid && <p className="error small">{coupon.message}</p>}

        {discount > 0 && (
          <div className="total-line discount-line">
            <span>Discount ({coupon?.code})</span><span>−{formatMoney(discount)}</span>
          </div>
        )}
        <div className="total-line grand"><span>Total</span><strong>{formatMoney(payable)}</strong></div>
      </div>

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
          {mutation.isPending ? 'Placing order…' : `Place order · ${formatMoney(payable)}`}
        </button>
      </form>
    </section>
  );
}
