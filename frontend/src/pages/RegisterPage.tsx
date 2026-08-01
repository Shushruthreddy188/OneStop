import { useForm } from 'react-hook-form';
import { useMutation } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router';
import { register as registerUser, type RegisterPayload } from '../api/auth';
import { useAuth } from '../auth/AuthContext';
import { apiErrorMessage } from '../lib/apiError';
import './AuthForm.css';

export default function RegisterPage() {
  const { register, handleSubmit, formState: { errors } } = useForm<RegisterPayload>();
  const { signIn } = useAuth();
  const navigate = useNavigate();

  const mutation = useMutation({
    mutationFn: registerUser,
    onSuccess: (res) => {
      signIn(res);
      navigate('/profile', { replace: true });
    },
  });

  return (
    <section className="auth-form">
      <h1>Create your account</h1>
      <form onSubmit={handleSubmit((values) => mutation.mutate(values))}>
        <label>
          Email
          <input type="email" autoComplete="email"
            {...register('email', { required: 'Email is required' })} />
          {errors.email && <span className="field-error">{errors.email.message}</span>}
        </label>
        <label>
          Password
          <input type="password" autoComplete="new-password"
            {...register('password', {
              required: 'Password is required',
              minLength: { value: 8, message: 'At least 8 characters' },
            })} />
          {errors.password && <span className="field-error">{errors.password.message}</span>}
        </label>
        <div className="row">
          <label>
            First name
            <input {...register('firstName')} />
          </label>
          <label>
            Last name
            <input {...register('lastName')} />
          </label>
        </div>
        <label>
          Phone
          <input {...register('phone')} />
        </label>

        {mutation.isError && (
          <p className="error">{apiErrorMessage(mutation.error)}</p>
        )}

        <button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? 'Creating…' : 'Create account'}
        </button>
      </form>
      <p className="muted">
        Already have an account? <Link to="/login">Sign in</Link>
      </p>
    </section>
  );
}
