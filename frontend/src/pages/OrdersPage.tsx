import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { listOrders } from '../api/orders';
import { formatMoney } from '../lib/format';
import './OrdersPage.css';

export default function OrdersPage() {
  const { data, isLoading, isError } = useQuery({ queryKey: ['orders'], queryFn: listOrders });

  if (isLoading) return <p>Loading…</p>;
  if (isError) return <p className="error">Could not load your orders.</p>;

  if (!data || data.length === 0) {
    return (
      <section>
        <h1>Your orders</h1>
        <p className="muted">You haven't placed any orders yet.</p>
        <p><Link to="/products">Browse products →</Link></p>
      </section>
    );
  }

  return (
    <section>
      <h1>Your orders</h1>
      <ul className="order-list">
        {data.map((o) => (
          <li key={o.id} className="order-row">
            <Link to={`/orders/${o.id}`} className="order-id">Order #{o.id}</Link>
            <span className={`status status-${o.status.toLowerCase()}`}>{o.status}</span>
            <span className="muted">{o.itemCount} item(s) · {o.paymentMethod}</span>
            <span className="order-total">{formatMoney(o.total)}</span>
          </li>
        ))}
      </ul>
    </section>
  );
}
