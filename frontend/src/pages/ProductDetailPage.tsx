import { useLocation, useNavigate, useParams } from 'react-router';
import { useQuery } from '@tanstack/react-query';
import { fetchProduct } from '../api/catalog';
import { fetchAvailability } from '../api/inventory';
import { fetchReviewSummary } from '../api/reviews';
import { formatMoney } from '../lib/format';
import AddToCartButton from '../cart/AddToCartButton';
import WishlistButton from '../wishlist/WishlistButton';
import Stars from '../components/Stars';
import ProductReviews from '../components/ProductReviews';
import './ProductDetailPage.css';

export default function ProductDetailPage() {
  const { id } = useParams<{ id: string }>();
  const productId = Number(id);
  const navigate = useNavigate();
  const location = useLocation();

  // Go back to wherever the user came from (list keeps its page/filters). If the
  // product was opened directly (no in-app history), fall back to the list.
  function goBack() {
    if (location.key !== 'default') navigate(-1);
    else navigate('/products');
  }

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['product', productId],
    queryFn: () => fetchProduct(productId),
    enabled: Number.isFinite(productId),
  });

  const availabilityQuery = useQuery({
    queryKey: ['availability', productId],
    queryFn: () => fetchAvailability(productId),
    enabled: Number.isFinite(productId),
  });

  const reviewSummaryQuery = useQuery({
    queryKey: ['reviewSummary', productId],
    queryFn: () => fetchReviewSummary(productId),
    enabled: Number.isFinite(productId),
  });

  if (isLoading) return <p>Loading…</p>;
  if (isError) {
    return (
      <p className="error">Could not load product: {(error as Error).message}</p>
    );
  }
  if (!data) return null;

  const discounted = data.mrp != null && data.mrp > data.sellingPrice;
  const stock = availabilityQuery.data?.availableQuantity;
  const outOfStock = stock === 0;

  return (
    <article className="product-detail">
      <p>
        <button type="button" className="back-link" onClick={goBack}>‹ Back</button>
      </p>
      <div className="detail-card card">
        <h1>{data.name}</h1>
        <p className="muted">
          {data.brandName ?? 'Unbranded'}
          {data.packageSize ? ` · ${data.packageSize}` : ''} · {data.categoryName}
        </p>
        {reviewSummaryQuery.data && reviewSummaryQuery.data.count > 0 && (
          <p className="detail-rating">
            <Stars value={reviewSummaryQuery.data.average} size={16} />
            <span className="muted small">
              {' '}{reviewSummaryQuery.data.average.toFixed(1)} · {reviewSummaryQuery.data.count} review
              {reviewSummaryQuery.data.count > 1 ? 's' : ''}
            </span>
          </p>
        )}
        <p className="detail-price">
          <span className="price">{formatMoney(data.sellingPrice)}</span>{' '}
          {discounted && <span className="mrp">{formatMoney(data.mrp)}</span>}
        </p>
        <p className="muted small">SKU: {data.sku}</p>

        {availabilityQuery.isLoading && <p className="muted">Checking stock…</p>}
        {stock != null && (
          <p>
            <span className={`stock-pill ${outOfStock ? 'stock-out' : 'stock-in'}`}>
              {outOfStock ? 'Out of stock' : `In stock: ${stock}`}
            </span>
          </p>
        )}

        <div className="detail-actions">
          <AddToCartButton productId={data.id} label="Add to cart" disabled={outOfStock} />
          <WishlistButton productId={data.id} />
        </div>
      </div>

      {data.description && (
        <div className="detail-card card">
          <h2 style={{ marginTop: 0 }}>Description</h2>
          <p style={{ whiteSpace: 'pre-wrap' }} className="muted">{data.description}</p>
        </div>
      )}

      <ProductReviews productId={data.id} />
    </article>
  );
}
