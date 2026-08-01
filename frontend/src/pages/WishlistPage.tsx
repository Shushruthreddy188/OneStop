import { Link } from 'react-router';
import { useWishlist } from '../wishlist/useWishlist';
import { formatMoney } from '../lib/format';
import AddToCartButton from '../cart/AddToCartButton';
import WishlistButton from '../wishlist/WishlistButton';
import './ProductsPage.css';

export default function WishlistPage() {
  const { data, isLoading, isError } = useWishlist();

  if (isLoading) return <p>Loading…</p>;
  if (isError) return <p className="error">Could not load your wishlist.</p>;

  if (!data || data.items.length === 0) {
    return (
      <section>
        <h1>Your wishlist</h1>
        <p className="muted">Nothing saved yet. Tap the ♡ on a product to save it here.</p>
        <p><Link to="/products">Browse products →</Link></p>
      </section>
    );
  }

  return (
    <section>
      <div className="products-header">
        <h1>Your wishlist</h1>
        <span className="muted">{data.count} saved</span>
      </div>
      <ul className="product-grid">
        {data.items.map((i) => (
          <li key={i.id} className="product-card">
            <Link to={`/products/${i.productId}`} className="product-name">
              {i.productName ?? `Product #${i.productId}`}
            </Link>
            <div className="price-row">
              <span className="price">{formatMoney(i.sellingPrice)}</span>
              {i.mrp != null && i.sellingPrice != null && i.mrp > i.sellingPrice && (
                <span className="mrp">{formatMoney(i.mrp)}</span>
              )}
            </div>
            <div className="card-actions">
              <AddToCartButton productId={i.productId} />
              <WishlistButton productId={i.productId} />
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
}
