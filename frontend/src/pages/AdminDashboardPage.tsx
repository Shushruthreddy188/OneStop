import { useForm } from 'react-hook-form';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createCoupon,
  fetchDashboard,
  listAdminCoupons,
  type CreateCouponPayload,
} from '../api/admin';
import { formatMoney } from '../lib/format';
import { apiErrorMessage } from '../lib/apiError';
import './AdminDashboardPage.css';

export default function AdminDashboardPage() {
  const queryClient = useQueryClient();
  const dashboard = useQuery({ queryKey: ['admin', 'dashboard'], queryFn: fetchDashboard });
  const coupons = useQuery({ queryKey: ['admin', 'coupons'], queryFn: listAdminCoupons });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<CreateCouponPayload>({
    defaultValues: { discountType: 'PERCENT' },
  });

  const createMutation = useMutation({
    mutationFn: (values: CreateCouponPayload) => createCoupon({
      ...values,
      discountValue: Number(values.discountValue),
      minOrderAmount: values.minOrderAmount ? Number(values.minOrderAmount) : undefined,
      maxDiscount: values.maxDiscount ? Number(values.maxDiscount) : undefined,
    }),
    onSuccess: () => {
      reset({ discountType: 'PERCENT', code: '', description: '', discountValue: undefined });
      queryClient.invalidateQueries({ queryKey: ['admin', 'coupons'] });
      queryClient.invalidateQueries({ queryKey: ['admin', 'dashboard'] });
    },
  });

  const d = dashboard.data;

  return (
    <section>
      <h1>Admin dashboard</h1>

      {dashboard.isLoading && <p>Loading…</p>}
      {d && (
        <div className="stat-grid">
          <Stat label="Products" value={d.productCount.toLocaleString('en-IN')} />
          <Stat label="Categories" value={d.categoryCount.toLocaleString('en-IN')} />
          <Stat label="Orders" value={d.orderCount.toLocaleString('en-IN')} />
          <Stat label="Confirmed" value={d.confirmedOrderCount.toLocaleString('en-IN')} />
          <Stat label="Revenue" value={formatMoney(d.revenue)} />
          <Stat label="Coupons" value={d.couponCount.toLocaleString('en-IN')} />
        </div>
      )}

      <h2>Recent orders</h2>
      <div className="table-wrap">
        <table className="admin-table">
          <thead>
            <tr><th>#</th><th>Customer</th><th>Status</th><th>Payment</th><th>Total</th></tr>
          </thead>
          <tbody>
            {d?.recentOrders.map((o) => (
              <tr key={o.id}>
                <td>{o.id}</td>
                <td>{o.customerId}</td>
                <td><span className={`status status-${o.status.toLowerCase()}`}>{o.status}</span></td>
                <td>{o.paymentMethod}</td>
                <td>{formatMoney(o.total)}</td>
              </tr>
            ))}
            {d && d.recentOrders.length === 0 && (
              <tr><td colSpan={5} className="muted">No orders yet.</td></tr>
            )}
          </tbody>
        </table>
      </div>

      <h2>Coupons</h2>
      <div className="table-wrap">
        <table className="admin-table">
          <thead>
            <tr><th>Code</th><th>Type</th><th>Value</th><th>Min order</th><th>Description</th></tr>
          </thead>
          <tbody>
            {coupons.data?.map((c) => (
              <tr key={c.code}>
                <td><strong>{c.code}</strong></td>
                <td>{c.discountType}</td>
                <td>{c.discountType === 'PERCENT' ? `${c.discountValue}%` : formatMoney(c.discountValue)}</td>
                <td>{c.minOrderAmount != null ? formatMoney(c.minOrderAmount) : '—'}</td>
                <td className="muted">{c.description}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <h3>Create a coupon</h3>
      <form className="coupon-create" onSubmit={handleSubmit((v) => createMutation.mutate(v))}>
        <input placeholder="CODE" {...register('code', { required: true })} style={{ textTransform: 'uppercase' }} />
        <select {...register('discountType')}>
          <option value="PERCENT">Percent</option>
          <option value="FIXED">Fixed ₹</option>
        </select>
        <input type="number" step="0.01" placeholder="Value"
          {...register('discountValue', { required: true })} />
        <input type="number" step="0.01" placeholder="Min order" {...register('minOrderAmount')} />
        <input type="number" step="0.01" placeholder="Max discount" {...register('maxDiscount')} />
        <input placeholder="Description" {...register('description')} />
        <button type="submit" disabled={createMutation.isPending}>
          {createMutation.isPending ? 'Creating…' : 'Create'}
        </button>
        {errors.code && <span className="field-error">Code is required</span>}
        {createMutation.isError && <span className="error">{apiErrorMessage(createMutation.error)}</span>}
      </form>
    </section>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="stat card">
      <div className="stat-value">{value}</div>
      <div className="stat-label muted">{label}</div>
    </div>
  );
}
