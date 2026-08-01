import { useState } from 'react';
import { Link } from 'react-router';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchReviews, submitReview } from '../api/reviews';
import { useAuth } from '../auth/AuthContext';
import { apiErrorMessage } from '../lib/apiError';
import Stars from './Stars';
import './ProductReviews.css';

export default function ProductReviews({ productId }: { productId: number }) {
  const { isAuthenticated } = useAuth();
  const queryClient = useQueryClient();
  const queryKey = ['reviews', productId];

  const { data, isLoading } = useQuery({ queryKey, queryFn: () => fetchReviews(productId) });

  const [rating, setRating] = useState(0);
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');

  const mutation = useMutation({
    mutationFn: () => submitReview({ productId, rating, title, body }),
    onSuccess: () => {
      setTitle('');
      setBody('');
      queryClient.invalidateQueries({ queryKey });
    },
  });

  const summary = data?.summary;

  return (
    <section className="reviews card">
      <div className="reviews-head">
        <h2 style={{ margin: 0 }}>Reviews</h2>
        {summary && summary.count > 0 && (
          <span className="reviews-summary">
            <Stars value={summary.average} /> <strong>{summary.average.toFixed(1)}</strong>
            <span className="muted"> · {summary.count} review{summary.count > 1 ? 's' : ''}</span>
          </span>
        )}
      </div>

      {isAuthenticated ? (
        <form
          className="review-form"
          onSubmit={(e) => {
            e.preventDefault();
            if (rating >= 1) mutation.mutate();
          }}
        >
          <div className="rating-row">
            <span className="muted small">Your rating:</span>
            <Stars value={rating} onChange={setRating} size={24} />
          </div>
          <input
            placeholder="Title (optional)"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
          <textarea
            placeholder="Share your thoughts (optional)"
            rows={3}
            value={body}
            onChange={(e) => setBody(e.target.value)}
          />
          {mutation.isError && <p className="error">{apiErrorMessage(mutation.error)}</p>}
          {mutation.isSuccess && <p className="success">Thanks for your review!</p>}
          <button type="submit" disabled={rating < 1 || mutation.isPending}>
            {mutation.isPending ? 'Submitting…' : 'Submit review'}
          </button>
        </form>
      ) : (
        <p className="muted">
          <Link to="/login" state={{ from: `/products/${productId}` }}>Sign in</Link> to write a review.
        </p>
      )}

      {isLoading && <p className="muted">Loading reviews…</p>}
      {data && data.content.length === 0 && (
        <p className="muted">No reviews yet — be the first!</p>
      )}
      <ul className="review-list">
        {data?.content.map((r) => (
          <li key={r.id}>
            <div className="review-meta">
              <Stars value={r.rating} size={15} />
              {r.mine && <span className="mine-badge">You</span>}
              <span className="muted small">{new Date(r.createdAt).toLocaleDateString('en-IN')}</span>
            </div>
            {r.title && <div className="review-title">{r.title}</div>}
            {r.body && <p className="muted">{r.body}</p>}
          </li>
        ))}
      </ul>
    </section>
  );
}
