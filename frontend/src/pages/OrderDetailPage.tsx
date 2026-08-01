import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useLocation, useParams } from 'react-router';
import { cancelOrder, getOrder } from '../api/orders';
import { fetchOrderPayments } from '../api/payments';
import DeliveryTimeline from '../components/DeliveryTimeline';
import { formatMoney } from '../lib/format';
import { apiErrorMessage } from '../lib/apiError';
import './OrdersPage.css';

export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>();
  const orderId = Number(id);
  const location = useLocation();
  const justPlaced = (location.state as { justPlaced?: boolean } | null)?.justPlaced;
  const queryClient = useQueryClient();

  const { data: order, isLoading, isError } = useQuery({
    queryKey: ['order', orderId],
    queryFn: () => getOrder(orderId),
    enabled: Number.isFinite(orderId),
  });

  const paymentsQuery = useQuery({
    queryKey: ['payments', orderId],
    queryFn: () => fetchOrderPayments(orderId),
    enabled: Number.isFinite(orderId),
  });

  const cancelMutation = useMutation({
    mutationFn: () => cancelOrder(orderId),
    onSuccess: (updated) => {
      queryClient.setQueryData(['order', orderId], updated);
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });

  if (isLoading) return <p>Loading…</p>;
  if (isError || !order) return <p className="error">Could not load this order.</p>;

  return (
    <article className="order-detail">
      <p><Link to="/orders">‹ All orders</Link></p>

      {justPlaced && (
        <p className="success">✓ Order placed! A confirmation notification has been sent.</p>
      )}

      <div className="products-header">
        <h1>Order #{order.id}</h1>
        <span className={`status status-${order.status.toLowerCase()}`}>{order.status}</span>
      </div>

      <ul className="address-list">
        {order.items.map((i) => (
          <li key={i.id}>
            {i.quantity} × {i.productName} <span className="muted">({i.sku})</span> — {formatMoney(i.lineTotal)}
          </li>
        ))}
      </ul>

      {order.discount > 0 && (
        <p className="muted small">
          Subtotal {formatMoney(order.subtotal)} · Discount −{formatMoney(order.discount)}
          {order.couponCode ? ` (${order.couponCode})` : ''}
        </p>
      )}
      <p className="subtotal">Total: <strong>{formatMoney(order.total)}</strong> · {order.paymentMethod}</p>

      {paymentsQuery.data && paymentsQuery.data.length > 0 && (
        <p className="muted small">
          Payment: {paymentsQuery.data[paymentsQuery.data.length - 1].status}
          {' '}via {paymentsQuery.data[paymentsQuery.data.length - 1].provider}
        </p>
      )}

      {order.address && (
        <>
          <h2>Delivery to</h2>
          <p className="muted">
            {order.address.recipientName}
            {order.address.phone ? ` · ${order.address.phone}` : ''}<br />
            {order.address.line1}{order.address.line2 ? `, ${order.address.line2}` : ''}, {order.address.city}
            {order.address.state ? `, ${order.address.state}` : ''} {order.address.postalCode}, {order.address.country}
          </p>
        </>
      )}

      <DeliveryTimeline orderId={order.id} />

      {order.status === 'CONFIRMED' && (
        <div>
          <button type="button" className="cancel-order" disabled={cancelMutation.isPending}
            onClick={() => cancelMutation.mutate()}>
            {cancelMutation.isPending ? 'Cancelling…' : 'Cancel order'}
          </button>
          {cancelMutation.isError && <p className="error">{apiErrorMessage(cancelMutation.error)}</p>}
        </div>
      )}
    </article>
  );
}
