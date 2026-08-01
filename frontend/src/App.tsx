import { Link, NavLink, Route, Routes } from 'react-router';
import HomePage from './pages/HomePage';
import ProductsPage from './pages/ProductsPage';
import ProductDetailPage from './pages/ProductDetailPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProfilePage from './pages/ProfilePage';
import CartPage from './pages/CartPage';
import CheckoutPage from './pages/CheckoutPage';
import OrdersPage from './pages/OrdersPage';
import OrderDetailPage from './pages/OrderDetailPage';
import WishlistPage from './pages/WishlistPage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import ProtectedRoute from './auth/ProtectedRoute';
import AdminRoute from './auth/AdminRoute';
import { useAuth } from './auth/AuthContext';
import { useCart } from './cart/useCart';
import './App.css';

export default function App() {
  const { isAuthenticated, user } = useAuth();
  const isAdmin = user?.roles?.includes('ROLE_ADMIN') ?? false;
  const { data: cart } = useCart();
  const cartCount = cart?.totalItems ?? 0;

  return (
    <>
      <header className="site-header">
        <div className="header-inner">
          <Link to="/" className="brand">
            <span className="brand-mark" aria-hidden="true">🛒</span>
            <span className="brand-name">OneStop</span>
          </Link>

          <nav className="main-nav">
            <NavLink to="/" end>Home</NavLink>
            <NavLink to="/products">Products</NavLink>
            {isAuthenticated && <NavLink to="/wishlist">Wishlist</NavLink>}
            {isAuthenticated && <NavLink to="/orders">Orders</NavLink>}
            {isAdmin && <NavLink to="/admin">Admin</NavLink>}
          </nav>

          <div className="header-actions">
            {isAuthenticated ? (
              <>
                <NavLink to="/cart" className="cart-link">
                  <span aria-hidden="true">🛒</span> Cart
                  {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
                </NavLink>
                <NavLink to="/profile" className="profile-link">
                  {user?.firstName ? `Hi, ${user.firstName}` : 'Profile'}
                </NavLink>
              </>
            ) : (
              <>
                <NavLink to="/login" className="profile-link">Sign in</NavLink>
                <NavLink to="/register" className="btn-register">Register</NavLink>
              </>
            )}
          </div>
        </div>
      </header>

      <main className="app-main">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/products" element={<ProductsPage />} />
          <Route path="/products/:id" element={<ProductDetailPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
          <Route path="/cart" element={<ProtectedRoute><CartPage /></ProtectedRoute>} />
          <Route path="/checkout" element={<ProtectedRoute><CheckoutPage /></ProtectedRoute>} />
          <Route path="/orders" element={<ProtectedRoute><OrdersPage /></ProtectedRoute>} />
          <Route path="/wishlist" element={<ProtectedRoute><WishlistPage /></ProtectedRoute>} />
          <Route path="/admin" element={<AdminRoute><AdminDashboardPage /></AdminRoute>} />
          <Route path="/orders/:id" element={<ProtectedRoute><OrderDetailPage /></ProtectedRoute>} />
          <Route path="*" element={<p>Not found.</p>} />
        </Routes>
      </main>

      <footer className="site-footer">
        <span className="muted small">OneStop · demo storefront · products from the DMart dataset</span>
      </footer>
    </>
  );
}
