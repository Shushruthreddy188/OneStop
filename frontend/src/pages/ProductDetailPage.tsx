import { Link, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { fetchProduct } from '../api/catalog';
import { fetchAvailability } from '../api/inventory';
import { formatMoney } from '../lib/format';
import AddToCartButton from '../cart/AddToCartButton';
import './ProductDetailPage.css';

export default function ProductDetailPage() {
  const { id } = useParams<{ id: string }>();
  const productId = Number(id);

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
        <Link to="/products">‹ Back to products</Link>
      </p>
      <div className="detail-card card">
        <h1>{data.name}</h1>
        <p className="muted">
          {data.brandName ?? 'Unbranded'}
          {data.packageSize ? ` · ${data.packageSize}` : ''} · {data.categoryName}
        </p>
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
        </div>
      </div>

      {data.description && (
        <div className="detail-card card">
          <h2 style={{ marginTop: 0 }}>Description</h2>
          <p style={{ whiteSpace: 'pre-wrap' }} className="muted">{data.description}</p>
        </div>
      )}
    </article>
  );
}
