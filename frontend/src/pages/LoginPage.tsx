import { useForm } from 'react-hook-form';
import { useMutation } from '@tanstack/react-query';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { login, type LoginPayload } from '../api/auth';
import { useAuth } from '../auth/AuthContext';
import { apiErrorMessage } from '../lib/apiError';
import './AuthForm.css';

export default function LoginPage() {
  const { register, handleSubmit, formState: { errors } } = useForm<LoginPayload>();
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from ?? '/profile';

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: (res) => {
      signIn(res);
      navigate(from, { replace: true });
    },
  });

  return (
    <section className="auth-form">
      <h1>Sign in</h1>
      <form onSubmit={handleSubmit((values) => mutation.mutate(values))}>
        <label>
          Email
          <input type="email" autoComplete="email"
            {...register('email', { required: 'Email is required' })} />
          {errors.email && <span className="field-error">{errors.email.message}</span>}
        </label>
        <label>
          Password
          <input type="password" autoComplete="current-password"
            {...register('password', { required: 'Password is required' })} />
          {errors.password && <span className="field-error">{errors.password.message}</span>}
        </label>

        {mutation.isError && (
          <p className="error">{apiErrorMessage(mutation.error)}</p>
        )}

        <button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? 'Signing in…' : 'Sign in'}
        </button>
      </form>
      <p className="muted">
        No account? <Link to="/register">Create one</Link>
      </p>
    </section>
  );
}
