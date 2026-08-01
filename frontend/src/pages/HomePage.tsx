import { Link } from 'react-router';
import { useAuth } from '../auth/AuthContext';
import './HomePage.css';

export default function HomePage() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="home">
      <section className="hero">
        <span className="hero-eyebrow">Groceries & household, delivered</span>
        <h1 className="hero-title">Your one stop for everyday essentials</h1>
        <p className="hero-sub">
          Browse thousands of products at everyday-low prices — add to cart, check out,
          and track your orders, all in one place.
        </p>
        <div className="hero-cta">
          <Link to="/products" className="cta-primary">Shop products →</Link>
          {!isAuthenticated && <Link to="/register" className="cta-ghost">Create an account</Link>}
        </div>
      </section>

      <section className="feature-row">
        <div className="feature card">
          <div className="feature-icon">🛒</div>
          <h3>Thousands of products</h3>
          <p className="muted">Grocery, household and more, imported from the DMart catalog.</p>
        </div>
        <div className="feature card">
          <div className="feature-icon">⚡</div>
          <h3>Real-time stock</h3>
          <p className="muted">Live availability with reservation at checkout — no overselling.</p>
        </div>
        <div className="feature card">
          <div className="feature-icon">📦</div>
          <h3>Order tracking</h3>
          <p className="muted">Place orders, view history, and cancel — with instant confirmations.</p>
        </div>
      </section>
    </div>
  );
}
