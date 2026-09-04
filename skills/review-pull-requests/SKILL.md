---
name: review-pull-requests
description: Review one or more existing GitHub pull requests selected by number, URL, list, or inclusive numeric range, and use gh to publish actionable findings as inline review comments on exact changed lines. Use when the user asks to review PRs and post the findings; do not use for a local-only review or for creating pull requests.
---

# Review Pull Requests

Review every selected pull request completely. Post a comment only for a concrete,
actionable problem introduced by that pull request.

## Scope and authorization

- Treat a request to use this skill as authorization to read the selected pull
  requests and create inline review comments for valid findings.
- Do not modify code, check out a PR branch, push commits, merge, approve, request
  changes, or create a general timeline comment unless the user separately asks.
- Do not post praise, summaries, style preferences, speculative concerns, or findings
  that cannot be tied to a changed line.
- Review open PRs by default. Skip closed or merged PRs unless the user explicitly
  includes them with a clear request to review that state.

## Resolve the targets

Accept any combination of:

- a PR number: `42`
- a PR URL
- comma-separated or space-separated selectors: `42, 47 51`
- an inclusive numeric range: `42-47`
- the PR for the current branch when no selector is supplied

Expand ranges, preserve the user's order, and de-duplicate targets. A URL determines
its own repository; otherwise use the repository selected by `gh` in the current
directory. Never treat an issue with the same number as a PR.

Before reviewing, run `gh auth status`, resolve the repository, and verify every
target with `gh pr view`. For example:

```bash
gh repo view --json nameWithOwner --jq .nameWithOwner
gh pr view "$pr_number" --repo "$repo_name" \
  --json number,title,url,state,isDraft,baseRefName,headRefName,headRefOid,author,body,files,commits,additions,deletions
```

Skip missing or inaccessible targets and report them individually. Do not let one
invalid member of a list or range prevent review of the remaining PRs.

## Inspect each pull request

Use `gh` for GitHub data. Do not assume the local worktree represents the PR head.

1. Read the title, description, commits, changed-file list, checks, and full patch.
2. Inspect relevant surrounding code at both the base and head revisions when the
   patch alone is insufficient. Use `gh api` or existing local Git objects without
   switching the user's branch.
3. Read all existing inline review comments before forming the final findings:

   ```bash
   gh api --paginate "repos/$repo_name/pulls/$pr_number/comments"
   ```

4. Trace affected callers, contracts, persistence behavior, error paths, security
   boundaries, concurrency, and tests in proportion to the change. Prefer evidence
   from the repository over assumptions.
5. Review each PR independently, even when several PRs form a range or stack.

Focus on defects that can change correctness, reliability, security, data integrity,
compatibility, or materially important performance. A test-only finding is useful
only when it identifies a meaningful behavior or regression that the changed tests
fail to protect.

## Build findings

A publishable finding must have all of the following:

- The PR introduced it or made an existing problem materially worse.
- It is reproducible from a specific input, state, or execution path.
- Its impact is explained without requiring the author to infer the failure.
- It has one precise anchor in the PR diff.
- It is not already covered by an existing review comment or discussion.

Choose the changed line that introduces or controls the problem. Never attach a
finding to an unrelated nearby line merely because that line is commentable. If a
valid finding cannot be anchored accurately, report it to the user as unposted.

Keep each comment self-contained and concise. A useful shape is:

```markdown
**[P1] Short actionable title**

Under <specific condition>, this <incorrect behavior> because <reason>. This causes
<concrete impact>. <Focused fix direction, when useful>.
```

Use `P0` only for a release-blocking or catastrophic issue, `P1` for a high-impact
defect, `P2` for a normal actionable defect, and `P3` sparingly for a low-impact but
real defect. Do not inflate severity.

## Anchor comments correctly

Derive line numbers from patch hunk headers, not from the displayed diff offset:

- `RIGHT` addresses the head blob: additions and displayed context lines.
- `LEFT` addresses the base blob: deleted lines.
- `line` is the actual line number in that side's file.
- For a multi-line range, also provide `start_line` and `start_side`.
- Do not use the deprecated `position` field.

Confirm that the selected path and line are part of the PR diff. Immediately before
posting, fetch `headRefOid` again. If it changed, refresh the diff, revalidate the
finding, and recompute its anchor.

## Publish with `gh`

For each finding, create an inline review comment through the pull-request review
comments endpoint:

```bash
gh api --method POST "repos/$repo_name/pulls/$pr_number/comments" \
  -H "Accept: application/vnd.github+json" \
  -f body="$comment_body" \
  -f commit_id="$head_sha" \
  -f path="$file_path" \
  -F line="$line_number" \
  -f side="$line_side" \
  --jq '{html_url,path,line,side}'
```

For a multi-line comment, add typed `start_line` and string `start_side` fields. Post
comments sequentially and retain the returned URL as proof of success.

Before each post, compare the proposed finding with existing comments on the same
path and line. Skip semantic duplicates even when their wording differs. If posting
returns `422`, refresh the head SHA and diff once and retry only if the same finding
still applies at a valid anchor. On authentication, permission, abuse-limit, or
rate-limit errors, stop posting for that PR and report the error; do not retry in a
loop.

## Report the result

For every requested PR, report:

- its number and URL;
- whether it was reviewed or skipped;
- each posted finding with severity, path, line, and returned comment URL;
- valid but unanchored findings that were not posted;
- posting failures or duplicate findings that were skipped.

If there are no findings, post nothing and explicitly say the PR had no actionable
findings. Never claim that a comment was posted unless `gh api` returned success.
