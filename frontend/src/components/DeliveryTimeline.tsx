import { useQuery } from '@tanstack/react-query';
import { fetchDeliveryByOrder } from '../api/delivery';
import './DeliveryTimeline.css';

const STAGES = [
  { key: 'CONFIRMED', label: 'Order confirmed' },
  { key: 'PACKED', label: 'Packed' },
  { key: 'SHIPPED', label: 'Shipped' },
  { key: 'OUT_FOR_DELIVERY', label: 'Out for delivery' },
  { key: 'DELIVERED', label: 'Delivered' },
];

export default function DeliveryTimeline({ orderId }: { orderId: number }) {
  const { data: shipment } = useQuery({
    queryKey: ['delivery', orderId],
    queryFn: () => fetchDeliveryByOrder(orderId),
    // Poll so the timeline advances live as the simulator moves the shipment.
    refetchInterval: 12000,
  });

  if (!shipment) return null;

  const eventByStatus = new Map(shipment.events.map((e) => [e.status, e]));

  return (
    <section className="card tracking">
      <div className="tracking-head">
        <h2 style={{ margin: 0 }}>Tracking</h2>
        <span className="muted small">
          {shipment.courier}{shipment.trackingNumber ? ` · ${shipment.trackingNumber}` : ''}
        </span>
      </div>
      <ul className="timeline">
        {STAGES.map((stage) => {
          const ev = eventByStatus.get(stage.key);
          const done = !!ev;
          const current = shipment.status === stage.key;
          return (
            <li key={stage.key} className={`tl-item${done ? ' done' : ''}${current ? ' current' : ''}`}>
              <span className="tl-dot" aria-hidden="true" />
              <div className="tl-body">
                <div className="tl-label">{stage.label}</div>
                {ev && (
                  <div className="muted small">
                    {ev.note}
                    {ev.occurredAt ? ` · ${new Date(ev.occurredAt).toLocaleString('en-IN')}` : ''}
                  </div>
                )}
              </div>
            </li>
          );
        })}
      </ul>
    </section>
  );
}
