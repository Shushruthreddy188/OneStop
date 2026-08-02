#!/usr/bin/env bash
set -euo pipefail

REPO_DIR="${ONESTOP_REPO_DIR:-/home/ubuntu/OneStop}"
ENV_FILE="$REPO_DIR/.env.production"
COMPOSE_FILE="$REPO_DIR/compose.prod.yml"

if [[ ! -r "$ENV_FILE" ]]; then
  echo "Missing readable production environment file: $ENV_FILE" >&2
  exit 1
fi

bucket="${BACKUP_S3_BUCKET:-$(sed -n 's/^BACKUP_S3_BUCKET=//p' "$ENV_FILE" | tail -n 1)}"
if [[ -z "$bucket" ]]; then
  echo "BACKUP_S3_BUCKET is not configured" >&2
  exit 1
fi

for command in docker gzip sha256sum aws flock; do
  command -v "$command" >/dev/null || { echo "Required command missing: $command" >&2; exit 1; }
done

exec 9>/tmp/onestop-production-backup.lock
flock -n 9 || { echo "Another OneStop backup is already running" >&2; exit 1; }

work_dir="$(mktemp -d)"
trap 'rm -rf -- "$work_dir"' EXIT

timestamp="$(date -u +%Y%m%dT%H%M%SZ)"
filename="onestop-postgres-${timestamp}.sql.gz"
backup="$work_dir/$filename"
checksum="$backup.sha256"
destination="s3://$bucket/postgres"

cd "$REPO_DIR"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
  pg_dumpall -U onestop | gzip -9 > "$backup"

gzip -t "$backup"
(cd "$work_dir" && sha256sum "$filename" > "$filename.sha256")

aws s3 cp "$backup" "$destination/$filename" --only-show-errors
aws s3 cp "$checksum" "$destination/$filename.sha256" --only-show-errors
aws s3api head-object --bucket "$bucket" --key "postgres/$filename" >/dev/null

echo "Uploaded and verified $destination/$filename"
