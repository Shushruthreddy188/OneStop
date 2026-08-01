import './Stars.css';

interface Props {
  value: number;
  onChange?: (v: number) => void;
  size?: number;
}

/** Star rating. Read-only when no onChange is given; interactive otherwise. */
export default function Stars({ value, onChange, size = 18 }: Props) {
  const readOnly = !onChange;
  return (
    <span className="stars" style={{ fontSize: `${size}px` }}>
      {[1, 2, 3, 4, 5].map((n) => {
        const filled = n <= Math.round(value);
        if (readOnly) {
          return (
            <span key={n} className={`star${filled ? ' on' : ''}`} aria-hidden="true">
              ★
            </span>
          );
        }
        return (
          <button
            key={n}
            type="button"
            className={`star star-btn${n <= value ? ' on' : ''}`}
            aria-label={`${n} star${n > 1 ? 's' : ''}`}
            onClick={() => onChange(n)}
          >
            ★
          </button>
        );
      })}
    </span>
  );
}
