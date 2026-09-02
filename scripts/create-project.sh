#!/usr/bin/env bash
# create-project.sh — scaffold a new project from the Spring Next SaaS Starter
# template: clones the repo, then renames every "houndjo" reference (Java
# package, Maven artifacts, pnpm workspace packages, Docker/DB names, mobile
# bundle id, docs, ...) to the project name you choose.
#
# Usage:
#   curl -fsSL https://raw.githubusercontent.com/saliou673/spring-next-saas-starter/main/scripts/create-project.sh | bash
#
# Non-interactive:
#   curl -fsSL .../create-project.sh | bash -s -- --name acme-billing --dir ~/code/acme-billing -y
#
# Maintaining a fork of this template?
#   Your fork is already served at
#   https://raw.githubusercontent.com/<you>/<your-fork>/<branch>/scripts/create-project.sh
#   but this script still needs to know to clone YOUR fork, not upstream. Either:
#     - edit DEFAULT_REPO_URL below to your fork's clone URL before publishing your copy, or
#     - tell your users to pass --repo <your-fork-url> (or set SAAS_STARTER_REPO=<url>)
#       when running the one-liner.
#   Also update BRANCH below (or have users pass --branch) if your fork's default branch
#   isn't "main".
set -euo pipefail

DEFAULT_REPO_URL="https://github.com/saliou673/spring-next-saas-starter.git"
REPO_URL="${SAAS_STARTER_REPO:-$DEFAULT_REPO_URL}"
BRANCH="main"
PROJECT_NAME=""
TARGET_DIR=""
KEEP_HISTORY=0
ASSUME_YES=0

PLACEHOLDER_LOWER="houndjo"

usage() {
    cat <<'EOF'
Create a new project from the Spring Next SaaS Starter template.

Options:
  --name <name>     Project name (kebab-case, e.g. "acme-billing"). Prompted if omitted.
  --dir <path>       Directory to clone into. Defaults to ./<name>.
  --repo <url>       Template repository to clone. Defaults to the upstream starter
                      (or $SAAS_STARTER_REPO if set) — pass this to scaffold from a fork.
  --branch <name>    Branch to clone. Defaults to "main".
  --keep-history     Keep the template's git history instead of starting a fresh repo.
  -y, --yes          Don't ask for confirmation before writing.
  -h, --help         Show this help.
EOF
}

while [ $# -gt 0 ]; do
    case "$1" in
        --name) PROJECT_NAME="$2"; shift 2 ;;
        --dir) TARGET_DIR="$2"; shift 2 ;;
        --repo) REPO_URL="$2"; shift 2 ;;
        --branch) BRANCH="$2"; shift 2 ;;
        --keep-history) KEEP_HISTORY=1; shift ;;
        -y|--yes) ASSUME_YES=1; shift ;;
        -h|--help) usage; exit 0 ;;
        *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
    esac
done

# Prompt that works even when this script's own stdin is a pipe (curl | bash).
tty_read() {
    local __var_name="$1" __msg="$2" __default="${3:-}" __val=""
    if [ -r /dev/tty ]; then
        read -r -p "$__msg" __val < /dev/tty
    else
        read -r -p "$__msg" __val
    fi
    [ -n "$__val" ] || __val="$__default"
    printf -v "$__var_name" '%s' "$__val"
}

need() {
    command -v "$1" >/dev/null 2>&1 || { echo "error: '$1' is required but not found on PATH." >&2; exit 1; }
}

need git
command -v perl >/dev/null 2>&1 || { echo "error: 'perl' is required but not found on PATH." >&2; exit 1; }

# --- project name -------------------------------------------------------------
slugify() {
    printf '%s' "$1" | tr '[:upper:]' '[:lower:]' | tr -c 'a-z0-9' '-' | sed -E 's/-+/-/g; s/^-+//; s/-+$//'
}

if [ -z "$PROJECT_NAME" ]; then
    tty_read PROJECT_NAME "Project name (kebab-case, e.g. acme-billing): " ""
fi

KEBAB="$(slugify "$PROJECT_NAME")"
if [ -z "$KEBAB" ] || ! printf '%s' "$KEBAB" | grep -Eq '^[a-z][a-z0-9-]*$'; then
    echo "error: '$PROJECT_NAME' doesn't produce a valid project name (got '$KEBAB')." >&2
    exit 1
fi

# PascalCase, e.g. acme-billing -> AcmeBilling
PASCAL="$(printf '%s' "$KEBAB" | awk -F- '{ out=""; for (i=1;i<=NF;i++) { s=$i; out = out toupper(substr(s,1,1)) substr(s,2) } print out }')"

# Compact identifier for the Java package / Maven groupId / DB name / mobile
# bundle id — hyphens aren't legal in any of those, so this drops them.
COMPACT="$(printf '%s' "$KEBAB" | tr -d '-')"
case "$COMPACT" in [0-9]*) COMPACT="app${COMPACT}" ;; esac

echo
echo "  kebab-case:            $KEBAB"
echo "  PascalCase:             $PASCAL"
echo "  package/identifier:     $COMPACT   (Java package, DB name, mobile bundle id)"
echo

# --- target directory ----------------------------------------------------------
if [ -z "$TARGET_DIR" ]; then
    tty_read TARGET_DIR "Directory to create the project in [./$KEBAB]: " "./$KEBAB"
fi

if [ -e "$TARGET_DIR" ]; then
    if [ -d "$TARGET_DIR" ] && [ -z "$(ls -A "$TARGET_DIR" 2>/dev/null)" ]; then
        : # empty directory is fine to clone into
    else
        echo "error: '$TARGET_DIR' already exists and is not empty." >&2
        exit 1
    fi
fi

if [ "$ASSUME_YES" -ne 1 ]; then
    tty_read CONFIRM "Clone $REPO_URL into $TARGET_DIR and rename '$PLACEHOLDER_LOWER' -> '$KEBAB'? [Y/n] " "y"
    case "$CONFIRM" in [Yy]*) ;; *) echo "Aborted."; exit 1 ;; esac
fi

# --- clone -----------------------------------------------------------------
echo "==> Cloning $REPO_URL"
git clone --depth 1 --branch "$BRANCH" "$REPO_URL" "$TARGET_DIR"
cd "$TARGET_DIR"

FILES=()
while IFS= read -r f; do FILES+=("$f"); done < <(git ls-files)

# --- 1. drop the template's own "rename this" disclaimer in the README --------
if [ -f README.md ]; then
    perl -0pi -e 's/\n> This project started as a real product.*?for your own project\.\n//s' README.md
fi

# --- 2. rewrite file contents --------------------------------------------------
echo "==> Rewriting references ($PLACEHOLDER_LOWER -> $KEBAB)"
for f in "${FILES[@]}"; do
    [ -f "$f" ] || continue
    grep -Iqi "$PLACEHOLDER_LOWER" -- "$f" 2>/dev/null || continue
    perl -pi -e "
        s/com\\.houndjo/com.${COMPACT}/g;
        s/houndjodb/${COMPACT}db/g;
        s/houndjomobile/${COMPACT}mobile/g;
        s/Houndjo/${PASCAL}/g;
        s/houndjo/${KEBAB}/g;
    " -- "$f"
done

# --- 3. rename files & directories ---------------------------------------------
echo "==> Renaming files and directories"
transform_path() {
    local path="$1"
    local -a parts out
    IFS='/' read -ra parts <<< "$path"
    out=()
    local prev1="" prev2="" seg newseg
    for seg in "${parts[@]}"; do
        newseg="$seg"
        if [ "$seg" = "houndjo" ] && [ "$prev1" = "com" ] && [ "$prev2" = "java" ]; then
            newseg="$COMPACT"
        else
            newseg="${newseg//Houndjo/$PASCAL}"
            newseg="${newseg//houndjo/$KEBAB}"
        fi
        out+=("$newseg")
        prev2="$prev1"
        prev1="$seg"
    done
    local IFS='/'
    echo "${out[*]}"
}

for old in "${FILES[@]}"; do
    [ -e "$old" ] || continue
    case "$old" in
        *houndjo*|*Houndjo*) ;;
        *) continue ;;
    esac
    new="$(transform_path "$old")"
    if [ "$new" != "$old" ]; then
        mkdir -p "$(dirname "$new")"
        mv -- "$old" "$new"
    fi
done

find . -mindepth 1 -type d -empty -not -path './.git*' -delete

# --- 4. fresh git history (unless asked to keep the template's) ----------------
if [ "$KEEP_HISTORY" -ne 1 ]; then
    echo "==> Starting a fresh git history"
    rm -rf .git
    git init -q
    git add -A
    if ! git commit -q -m "Initial commit: scaffolded from spring-next-saas-starter as ${KEBAB}" 2>/dev/null; then
        echo "note: couldn't create the initial commit automatically (set 'git config user.name/user.email' and commit yourself)." >&2
    fi
else
    git add -A
    if ! git commit -q -m "chore: rename ${PLACEHOLDER_LOWER} -> ${KEBAB}" 2>/dev/null; then
        echo "note: couldn't create the rename commit automatically (set 'git config user.name/user.email' and commit yourself)." >&2
    fi
fi

# --- 5. safety net: flag anything the rename missed -----------------------------
REMAINING="$(grep -ril "$PLACEHOLDER_LOWER" --exclude-dir=.git . 2>/dev/null || true)"
if [ -n "$REMAINING" ]; then
    echo
    echo "warning: found remaining '${PLACEHOLDER_LOWER}' references — check these by hand:"
    echo "$REMAINING" | sed 's/^/  /'
fi

echo
echo "Done! '${KEBAB}' is ready in $(pwd)"
echo
echo "Next steps:"
echo "  cd $TARGET_DIR"
echo "  cp backend/${KEBAB}-restapi/.env.example backend/${KEBAB}-restapi/.env   # fill in secrets"
echo "  cd frontend && pnpm install"
echo "  git remote add origin <your-new-repo-url> && git push -u origin main"
