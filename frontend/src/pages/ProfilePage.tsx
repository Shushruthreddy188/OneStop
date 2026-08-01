import { useForm } from 'react-hook-form';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import {
  addAddress,
  listAddresses,
  updateProfile,
  type AddressPayload,
  type UpdateProfilePayload,
} from '../api/auth';
import { useAuth } from '../auth/AuthContext';
import { apiErrorMessage } from '../lib/apiError';
import './AuthForm.css';

export default function ProfilePage() {
  const { user, setUser, signOut } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const profileForm = useForm<UpdateProfilePayload>({
    defaultValues: {
      firstName: user?.firstName ?? '',
      lastName: user?.lastName ?? '',
      phone: user?.phone ?? '',
    },
  });

  const profileMutation = useMutation({
    mutationFn: updateProfile,
    onSuccess: (updated) => setUser(updated),
  });

  const addressesQuery = useQuery({
    queryKey: ['addresses'],
    queryFn: listAddresses,
  });

  const addressForm = useForm<AddressPayload>({
    defaultValues: { country: 'India', isDefault: false },
  });

  const addressMutation = useMutation({
    mutationFn: addAddress,
    onSuccess: () => {
      addressForm.reset({ country: 'India', isDefault: false });
      queryClient.invalidateQueries({ queryKey: ['addresses'] });
    },
  });

  function handleSignOut() {
    signOut();
    navigate('/');
  }

  if (!user) return null;

  return (
    <section className="auth-form wide">
      <div className="products-header">
        <h1>My profile</h1>
        <button type="button" className="link-button" onClick={handleSignOut}>
          Sign out
        </button>
      </div>
      <p className="muted">
        {user.email} · {user.roles.join(', ')}
      </p>

      <h2>Details</h2>
      <form onSubmit={profileForm.handleSubmit((v) => profileMutation.mutate(v))}>
        <div className="row">
          <label>
            First name
            <input {...profileForm.register('firstName')} />
          </label>
          <label>
            Last name
            <input {...profileForm.register('lastName')} />
          </label>
        </div>
        <label>
          Phone
          <input {...profileForm.register('phone')} />
        </label>
        {profileMutation.isError && (
          <p className="error">{apiErrorMessage(profileMutation.error)}</p>
        )}
        {profileMutation.isSuccess && <p className="success">Saved.</p>}
        <button type="submit" disabled={profileMutation.isPending}>
          {profileMutation.isPending ? 'Saving…' : 'Save changes'}
        </button>
      </form>

      <h2>Addresses</h2>
      {addressesQuery.isLoading && <p>Loading…</p>}
      {addressesQuery.data && addressesQuery.data.length === 0 && (
        <p className="muted">No addresses yet.</p>
      )}
      <ul className="address-list">
        {addressesQuery.data?.map((a) => (
          <li key={a.id}>
            {a.line1}
            {a.line2 ? `, ${a.line2}` : ''}, {a.city}
            {a.state ? `, ${a.state}` : ''} {a.postalCode}, {a.country}
            {a.isDefault && <span className="badge">Default</span>}
          </li>
        ))}
      </ul>

      <h3>Add an address</h3>
      <form onSubmit={addressForm.handleSubmit((v) => addressMutation.mutate(v))}>
        <label>
          Address line 1
          <input {...addressForm.register('line1', { required: true })} />
        </label>
        <label>
          Address line 2
          <input {...addressForm.register('line2')} />
        </label>
        <div className="row">
          <label>
            City
            <input {...addressForm.register('city', { required: true })} />
          </label>
          <label>
            State
            <input {...addressForm.register('state')} />
          </label>
        </div>
        <div className="row">
          <label>
            Postal code
            <input {...addressForm.register('postalCode')} />
          </label>
          <label>
            Country
            <input {...addressForm.register('country', { required: true })} />
          </label>
        </div>
        <label className="checkbox">
          <input type="checkbox" {...addressForm.register('isDefault')} />
          Make this my default address
        </label>
        {addressMutation.isError && (
          <p className="error">{apiErrorMessage(addressMutation.error)}</p>
        )}
        <button type="submit" disabled={addressMutation.isPending}>
          {addressMutation.isPending ? 'Adding…' : 'Add address'}
        </button>
      </form>
    </section>
  );
}
