import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router';
import type { RecommendationRow } from '../api/recommendations';
import { formatMoney } from '../lib/format';
import AddToCartButton from '../cart/AddToCartButton';
import './RecommendationRow.css';

interface Props {
  /** Stable cache key, e.g. ['recs', 'trending']. */
  queryKey: unknown[];
  /** Fetcher returning the row (trending / for-you). */
  queryFn: () => Promise<RecommendationRow>;
  /** Optional title override; otherwise the server-provided title is used. */
  title?: string;
}

/**
 * A horizontally scrollable strip of recommended products. Renders nothing when
 * the row is empty (e.g. brand-new install with no activity yet), so the home
 * page degrades gracefully rather than showing an empty shelf.
 */
export default function RecommendationRow({ queryKey, queryFn, title }: Props) {
  const query = useQuery({ queryKey, queryFn, staleTime: 60 * 1000 });

  if (query.isLoading || query.isError) {
    return null;
  }
  const row = query.data;
  if (!row || row.items.length === 0) {
    return null;
  }

  return (
    <section className="rec-row">
      <div className="rec-row-head">
        <h2>{title ?? row.title}</h2>
        {row.coldStart && <span className="rec-badge">Popular picks</span>}
      </div>
      <ul className="rec-strip">
        {row.items.map((p) => {
          const discounted = p.mrp != null && p.sellingPrice != null && p.mrp > p.sellingPrice;
          return (
            <li key={p.productId} className="rec-card">
              <Link to={`/products/${p.productId}`} className="rec-name">
                {p.name}
              </Link>
              <div className="muted small">{p.brandName ?? 'Unbranded'}</div>
              <div className="rec-price-row">
                {p.sellingPrice != null && (
                  <span className="price">{formatMoney(p.sellingPrice)}</span>
                )}
                {discounted && p.mrp != null && <span className="mrp">{formatMoney(p.mrp)}</span>}
              </div>
              <div className="rec-reason">{p.reason}</div>
              <AddToCartButton productId={p.productId} />
            </li>
          );
        })}
      </ul>
    </section>
  );
}
