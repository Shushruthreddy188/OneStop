import { useEffect, useRef, useState, type FormEvent } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { fetchSuggestions } from '../api/search';
import './SearchBox.css';

interface Props {
  value: string;
  onChange: (v: string) => void;
  onSubmit: (e: FormEvent) => void;
}

/** Search input with a typo-tolerant autocomplete dropdown (search-service). */
export default function SearchBox({ value, onChange, onSubmit }: Props) {
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [debounced, setDebounced] = useState(value);
  const boxRef = useRef<HTMLDivElement>(null);

  // Debounce the query feeding the suggestions request.
  useEffect(() => {
    const t = setTimeout(() => setDebounced(value), 200);
    return () => clearTimeout(t);
  }, [value]);

  const { data: suggestions = [] } = useQuery({
    queryKey: ['suggest', debounced],
    queryFn: () => fetchSuggestions(debounced),
    enabled: debounced.trim().length >= 2,
    staleTime: 30_000,
  });

  // Close the dropdown on outside click.
  useEffect(() => {
    function onClick(e: MouseEvent) {
      if (boxRef.current && !boxRef.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener('mousedown', onClick);
    return () => document.removeEventListener('mousedown', onClick);
  }, []);

  return (
    <div className="searchbox" ref={boxRef}>
      <form
        onSubmit={(e) => {
          setOpen(false);
          onSubmit(e);
        }}
      >
        <input
          type="search"
          placeholder="Search products…"
          value={value}
          autoComplete="off"
          onChange={(e) => {
            onChange(e.target.value);
            setOpen(true);
          }}
          onFocus={() => setOpen(true)}
        />
      </form>

      {open && suggestions.length > 0 && (
        <ul className="suggestions">
          {suggestions.map((s) => (
            <li key={s.productId}>
              <button
                type="button"
                onMouseDown={() => {
                  setOpen(false);
                  navigate(`/products/${s.productId}`);
                }}
              >
                {s.name}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
