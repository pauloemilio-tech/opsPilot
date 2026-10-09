# OpsPilot — Signal Workspace Design System

## 1. Product and UX principles

OpsPilot is an operational decision workspace, not a reporting portal or marketing surface. Its primary user needs to identify the next account requiring attention, compare the signals behind that ranking, and open the account record with confidence.

1. **Decision first.** The backend-ranked Attention Queue is the first substantial content on the dashboard.
2. **Order is authoritative.** Never sort, group, or visually reorder accounts independently of the API response.
3. **Explain, do not imply.** Display only data returned by existing endpoints. Do not invent reasons, trends, AI summaries, or recommended actions.
4. **Intensity follows urgency.** Strong color and contrast belong to exceptional states, not every row.
5. **Wide by design.** Desktop space should improve comparison and scanning rather than become empty outer margins.
6. **Readable under pressure.** Operational data uses comfortable sizes, stable alignment, and explicit labels.
7. **One identity, two themes.** Dark and light modes share proportions and hierarchy but have independently tuned surfaces and contrast.

## 2. Visual concept

**Signal Workspace** combines a near-black operational canvas, layered graphite surfaces, and rare yellow signals. The interface should feel like a precise working environment: quiet until something deserves attention.

The distinctive visual element is the Attention Queue itself. It is a broad ranked surface with strong horizontal alignment, a visible order, and a dedicated Priority reading zone. Dotted texture and tonal layers remain peripheral.

The direction deliberately avoids a generic collection of KPI cards. Summary counts form a compact status rail, while the queue carries the page's visual weight.

## 3. Brand colors

### Core palette

| Token | Value | Purpose |
| --- | --- | --- |
| `--brand-graphite` | `#303841` | Structural anchor and light-mode text |
| `--brand-steel` | `#3A4750` | Dividers and secondary surfaces |
| `--brand-signal` | `#F6C90E` | Priority signal and high-emphasis action |
| `--brand-light` | `#EEEEEE` | Dark-mode text and light-mode foundation |

### Supporting semantic colors

These colors communicate meaning that yellow cannot carry alone:

| Family | Dark theme | Light theme | Meaning |
| --- | --- | --- | --- |
| Risk | `#FF746C` | `#A93632` | Exposure, negative change, critical condition |
| Potential | `#69C6D0` | `#176B74` | Commercial opportunity |
| Success | `#58C99D` | `#18734F` | Healthy or active state |
| Information | `#79A9FF` | `#315FAD` | Neutral informational state |

Semantic colors must always be accompanied by text or an accessible name.

## 4. Dark-mode semantic tokens

```css
:root {
  color-scheme: dark;

  --canvas: #0b0d10;
  --shell: #11151a;
  --surface-primary: #11151a;
  --surface-elevated: #1a2128;
  --surface-strong: #303841;
  --surface-interactive: #222a32;

  --border-subtle: #303841;
  --border-strong: #3a4750;

  --text-primary: #eeeeee;
  --text-secondary: #bdc5ca;
  --text-muted: #929da5;
  --text-disabled: #6f7a82;

  --signal: #f6c90e;
  --signal-hover: #ffda3d;
  --signal-pressed: #d8ad00;
  --on-signal: #0b0d10;
  --focus-ring: #f6c90e;

  --risk: #ff746c;
  --potential: #69c6d0;
  --success: #58c99d;
  --info: #79a9ff;
}
```

Use translucent semantic backgrounds only as supporting fields. Text and key boundaries must use opaque tokens with sufficient contrast.

## 5. Light-mode semantic tokens

```css
:root[data-theme='light'] {
  color-scheme: light;

  --canvas: #eeeeee;
  --shell: #ffffff;
  --surface-primary: #ffffff;
  --surface-elevated: #f7f8f8;
  --surface-strong: #e1e5e7;
  --surface-interactive: #f0f2f3;

  --border-subtle: #cbd1d5;
  --border-strong: #89949c;

  --text-primary: #303841;
  --text-secondary: #4e5b65;
  --text-muted: #65727b;
  --text-disabled: #7b868e;

  --signal: #f6c90e;
  --signal-hover: #e7ba00;
  --signal-pressed: #cda500;
  --on-signal: #20262c;
  --focus-ring: #755f00;

  --risk: #a93632;
  --potential: #176b74;
  --success: #18734f;
  --info: #315fad;
}
```

Light mode uses soft gray as the canvas and crisp white for working surfaces. Graphite replaces near-black as the dominant text and structural color. Yellow is a fill, marker, or broad focus treatment with dark foreground text; it is never small yellow text on a light surface.

## 6. Tonal hierarchy and surface layering

Use four levels, each with a clear responsibility:

1. **Canvas:** page background and optional peripheral dot field.
2. **Shell:** compact application header and persistent global controls.
3. **Primary surface:** the Attention Queue and major account-detail work areas.
4. **Elevated surface:** queue header, selected or focused region, decision summary, and temporary state panels.

Do not place every section in an elevated card. Adjacent information that belongs to one task should share a surface and use internal spacing or dividers.

Use signal yellow only for:

- an `URGENT` Priority marker or filled badge;
- the primary focus ring;
- a small live-status or decision indicator when its meaning is explicit;
- the primary button where a primary button is necessary.

`HIGH`, `MEDIUM`, and `LOW` remain readable through typography, neutral structure, and restrained semantic markers. They do not all receive yellow panels.

## 7. Typography

### Family

Use a single operational sans-serif family throughout the pilot. Preferred stack:

```css
font-family: "Segoe UI Variable", "Segoe UI", Inter, system-ui, sans-serif;
```

This requires no new package or runtime dependency. If a distinctive hosted or self-hosted font is proposed later, it requires separate approval and a licensing/performance review.

Use `font-variant-numeric: tabular-nums` for ranks, scores, counts, percentages, revenue, and dates. Do not use a monospace face as decorative metadata.

### Scale

| Role | Desktop | Mobile | Weight | Line height |
| --- | --- | --- | --- | --- |
| Page title | `40px` | `32px` | 650 | 1.08 |
| Priority score | `32px` | `28px` | 700 | 1 |
| Section heading | `22px` | `20px` | 650 | 1.2 |
| Account name | `17px` | `16px` | 650 | 1.3 |
| Body | `16px` | `16px` | 400 | 1.5 |
| Data value | `16px` | `16px` | 650 | 1.25 |
| UI label | `13px` | `13px` | 600 | 1.35 |
| Supporting text | `13px` | `13px` | 400 | 1.45 |

Avoid routine text below `12px`. Use sentence case by default. Uppercase is reserved for short machine-like states such as an optional compact `URGENT` marker, never for every section label.

Body copy should remain below approximately 75 characters per line.

## 8. Layout and responsive grid

### Desktop

- Replace the sidebar-offset layout with a full-width application shell.
- Content width: `min(calc(100% - 64px), 1680px)`.
- Minimum desktop gutter: `32px`; increase to `48px` above 1440px and `64px` above 1920px.
- Use a 12-column grid with `24px` gutters for page composition.
- The Attention Queue spans all 12 columns.
- Keep content left aligned inside the workspace. Do not center headings or operational data.

At 1920px, a 1680px maximum leaves 120px on each side, allowing intentional atmosphere without sacrificing working width. At 2560px, the cap may remain 1680px for scanability; optional peripheral dot fields can occupy the unused canvas without implying missing content.

### Tablet

- At `768px–1199px`, use `24px` page gutters and an 8-column grid.
- Preserve a single top application header.
- Queue score columns may reduce spacing but remain side by side while labels and values stay legible.
- Switch to the mobile row composition based on available content width, not on assumptions created by a sidebar.

### Mobile

- Use `16px` page gutters and a 4-column grid.
- Header height may reduce from `64px` to `56px`.
- Queue rows stack account identity above a three-part score grid.
- Preserve backend rank and one clear account-navigation action.
- No horizontal page scrolling at `320px`.

### Spacing scale

Use a base 4px rhythm: `4`, `8`, `12`, `16`, `24`, `32`, `48`, and `64px`. Prefer `24px` within major panels and `32–48px` between distinct page regions.

## 9. Application shell and navigation

Use a compact top application header. It contains only:

- OpsPilot brand link to `/dashboard`;
- the implemented `Dashboard` destination with current-page state;
- a compact monitoring-status readout when real data is available;
- the existing dark/light theme control.

Do not display navigation for Account Analyst, automations, RAG, settings, or other unimplemented destinations. Do not retain a desktop sidebar merely to reserve space for possible future routes.

The header is `64px` high on desktop, uses the shell surface, and has a single bottom divider. It may remain sticky if browser review shows that persistent context helps longer queues. Stickiness must not reduce usable mobile height unnecessarily.

Preserve the current `ThemeService`, `opspilot-theme` storage key, system-preference fallback, and toggle behavior. Only its presentation and placement change.

## 10. Dashboard composition

The dashboard uses three regions:

1. **Decision header:** title, one-sentence description, and monitored-account status. Keep it compact.
2. **Queue status rail:** total, high/urgent, medium, and low counts in one continuous band. Counts summarize the queue; they are not separate cards and are not filters unless filtering is implemented later.
3. **Attention Queue:** the dominant full-width working surface, introduced by the question “Which accounts need attention right now?”

Suggested copy hierarchy:

- Page title: `Attention queue`
- Description: `Accounts ranked by operational risk and commercial potential.`
- Queue title: `Which accounts need attention right now?`
- Order note: `Backend-ranked order`

Avoid redundant statements such as showing the monitored total in multiple prominent locations.

## 11. Attention Queue

The queue remains an ordered list or an equivalently accessible table-like structure. The API response order is rendered unchanged.

### Desktop row

Use five aligned zones:

| Zone | Approximate share | Content |
| --- | ---: | --- |
| Rank | `6%` | Zero-padded visual rank; accessible natural number |
| Account | `44%` | Account name as the single primary link |
| Risk | `15%` | Score plus explicit level |
| Potential | `15%` | Score plus explicit level |
| Priority | `20%` | Dominant score, level, and restrained navigation affordance |

Rows should be approximately `80–88px` high. Use internal dividers sparingly. A hover or focus state may lift tonal intensity by one surface level, but should not animate position.

### Emphasis rules

- Rank communicates sequence; it is not decorative numbering.
- Priority uses the largest number in the row.
- Risk uses the risk semantic color only where needed, not as a full-cell fill.
- Potential uses its own semantic color and never borrows the Priority treatment.
- Only `URGENT` receives the strongest signal-yellow treatment.
- `HIGH` may use a narrow signal marker or stronger text, but not a full yellow row.
- `MEDIUM` and `LOW` remain predominantly neutral.
- Do not repeat “Account record” on every row.
- Avoid a separate “View” link when the account-name link or row-level accessible link already performs that action.

### Accessible labeling

Desktop column labels must remain available to assistive technology. If visual mobile labels are hidden at larger widths, retain equivalent screen-reader text for each score. Never make accessible score meaning depend on viewport width.

## 12. Risk, Potential and Priority presentation

The three measures have distinct jobs:

- **Risk:** current operational exposure. Semantic color: risk red/coral.
- **Potential:** commercial opportunity. Semantic color: cool cyan/teal.
- **Priority:** the combined backend decision metric that determines order. Semantic emphasis: typography first, signal yellow only for exceptional states.

Priority must be visually primary without appearing to be another kind of Risk. Use a larger numeric scale and dedicated alignment rather than simply making it redder or giving every value a yellow background.

Every score presentation includes:

- metric name;
- exact numeric value;
- backend-provided level;
- no locally inferred formula, trend, recommendation, or threshold.

## 13. Account details and explainability

Account Detail implements the explainability workspace for an individual account.

Use a 12-column desktop layout:

- Main evidence column: 8 columns.
- Decision summary: 4 columns, optionally sticky after browser review.

Recommended reading order:

1. Account identity, industry, region, and status.
2. Priority decision with supporting Risk and Potential scores.
3. Operational indicators grouped as commercial, operations, and relationship evidence.
4. Risk and Potential contribution lists.
5. Persisted orders, support tickets, and interactions.
6. On-demand AI Account Analyst interpretation.
7. Record context and update time.

Contribution graphics must not imply a false percentage. Prefer ranked point-contribution rows with exact `+N` values. A bar may be used only when its denominator or maximum is explicit and truthful.

The AI Account Analyst appears after deterministic evidence and operational records. It may display
only the structured summary, key concerns, recommended actions, and evidence returned by the
implemented backend contract. It must not visually replace or recalculate Risk, Potential, or Priority.

## 14. Dotted background treatment

The dot field is a peripheral canvas texture, not a card pattern.

```css
.workspace::before {
  content: "";
  position: fixed;
  inset: 64px 0 0;
  pointer-events: none;
  background-image: radial-gradient(
    circle,
    color-mix(in srgb, var(--border-subtle) 55%, transparent) 0.75px,
    transparent 0.9px
  );
  background-size: 20px 20px;
  opacity: 0.32;
  mask-image: linear-gradient(to bottom, black, transparent 48%);
}
```

Implementation requirements:

- No image asset or dependency.
- Render behind content and ignore pointer input.
- Keep it out of the Attention Queue and other dense data surfaces.
- Reduce or remove it in light mode if browser review shows visual noise.
- Disable it in forced-colors mode.

## 15. Cards, panels, borders and shadows

- Major panel radius: `12px`.
- Small controls and badges: `6–8px`.
- Avoid applying the same radius and shadow to every element.
- Primary operational surfaces use a one-pixel border and little or no shadow in dark mode.
- Light mode may use a restrained shadow such as `0 12px 32px rgba(26, 33, 40, 0.08)` for major elevated surfaces only.
- Internal groups should use spacing and dividers before nested cards.
- Cards require a distinct information boundary or interaction purpose.

## 16. Buttons and interaction states

### Primary button

- Signal-yellow fill with `--on-signal` text.
- Minimum height: `44px`.
- Used for one primary recovery or action per region, such as `Try again`.

### Secondary button

- Transparent or surface fill with a strong border and primary text.
- Hover raises the surface tone; it does not glow or move.

### Links and rows

- Account name is the primary navigation link.
- Underline or another non-color cue appears on hover where appropriate.
- Keyboard focus uses a `2px` visible ring with at least `2px` offset.
- Focus must remain clear in both themes; light mode uses the darker focus token rather than raw yellow.
- Interactive targets should be at least `44 × 44px` where practical.

Disabled states must reduce emphasis without becoming illegible. Do not use opacity alone on critical labels.

## 17. Motion

- Standard interaction transition: `140–180ms`, ease-out.
- Animate only color, border color, background color, and opacity for routine controls.
- Do not animate queue-row position on hover.
- One optional queue entrance may use a subtle opacity reveal for the complete surface, not staggered animation on every row.
- Loading skeleton motion remains subdued and is disabled by `prefers-reduced-motion`.
- Theme switching should be immediate or use a very short color transition; avoid a full-page flash or theatrical wipe.

Under `prefers-reduced-motion: reduce`, eliminate nonessential animation and reduce transitions to effectively immediate changes.

## 18. Accessibility

- Meet WCAG 2.2 AA contrast for text, focus indicators, controls, and meaningful graphics.
- Never use yellow as small foreground text on the light theme.
- Provide a skip link from the application header to the main workspace.
- Maintain one `h1` per page and a logical heading hierarchy.
- Preserve the ordered semantics of the queue.
- Associate every score with its metric name and level at every viewport size.
- Preserve textual labels for severity; color is supplementary.
- Use `aria-live` deliberately for loading and errors without announcing decorative skeletons.
- Ensure empty and error states explain what happened and, when possible, what the user can do next.
- Retain visible focus when a whole row or account link receives keyboard focus.
- Test at 200% zoom, `320px` width, Windows high-contrast/forced-colors mode, keyboard-only navigation, and reduced motion.
- Use locale-appropriate number and date formatting already supported by Angular pipes; do not add units the API does not define.

## 19. Desktop wireframe

```text
┌──────────────────────────────────────────────────────────────────────────────┐
│ OpsPilot   Dashboard                         10 monitored        Theme       │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│ Attention queue                                      Updated from analytics  │
│ Accounts ranked by operational risk and commercial potential.               │
│                                                                              │
│ 10 monitored        2 high / urgent        7 medium        1 low             │
│                                                                              │
│ ┌──────────────────────────────────────────────────────────────────────────┐ │
│ │ Which accounts need attention right now?       Backend-ranked order     │ │
│ ├──────┬──────────────────────────────┬───────────┬───────────┬─────────────┤ │
│ │ Rank │ Account                      │ Risk      │ Potential │ Priority    │ │
│ ├──────┼──────────────────────────────┼───────────┼───────────┼─────────────┤ │
│ │ 01   │ Horizon Supply               │ 95        │ 12        │ 62  High    │ │
│ │      │                              │ Critical  │ Low       │             │ │
│ ├──────┼──────────────────────────────┼───────────┼───────────┼─────────────┤ │
│ │ 02   │ Pulse Systems                │ 42 Medium │ 75 V.High │ 55  High    │ │
│ ├──────┼──────────────────────────────┼───────────┼───────────┼─────────────┤ │
│ │ 03   │ Vertex Health                │ 47 Medium │ 46 Medium │ 47  Medium  │ │
│ └──────┴──────────────────────────────┴───────────┴───────────┴─────────────┘ │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

The queue spans the workspace. The page uses horizontal room for comparison rather than adding a secondary sidebar or decorative KPI grid.

## 20. Mobile wireframe

```text
┌──────────────────────────────┐
│ OpsPilot          Theme      │
├──────────────────────────────┤
│ Attention queue              │
│ Accounts ranked by risk and  │
│ commercial potential.        │
│                              │
│ 10 monitored   2 need action │
│ 7 medium       1 low         │
│                              │
│ Which accounts need          │
│ attention right now?         │
│                              │
│ 01  Horizon Supply           │
│ ──────────────────────────── │
│ Risk       Potential Priority│
│ 95         12        62      │
│ Critical   Low       High    │
│                              │
│ 02  Pulse Systems            │
│ ──────────────────────────── │
│ Risk       Potential Priority│
│ 42         75        55      │
│ Medium     Very high High    │
└──────────────────────────────┘
```

Each account remains one coherent row/card unit in the ordered sequence. Score labels are visible, and account navigation remains a single clear action.

## 21. Explicit anti-patterns

- No permanent sidebar until multiple real top-level destinations justify it.
- No narrow centered dashboard surrounded by inactive desktop space.
- No generic four-card KPI grid.
- No yellow treatment on every Priority row.
- No repeated pill for every metric and state.
- No decorative uppercase eyebrow above every heading.
- No tiny metadata used to simulate information density.
- No gradient glow, glassmorphism, neon outline, or animated background.
- No dotted texture inside dense data surfaces.
- No invented trend arrows, recommendations, reasons, timestamps, or operational status.
- No chatbot input, sparkle treatment, automation action, or RAG control around the AI Account Analyst.
- No client-side reordering of the Attention Queue.
- No ambiguous contribution bar presented as a percentage when it represents points.
- No framework or animation library for effects achievable with modern CSS.

## 22. Current MVP surfaces

The approved Signal Workspace system now covers:

- the compact application shell and Home product story;
- account creation and operational data management;
- the backend-ranked Dashboard Attention Queue;
- Account Detail decision scores, operational evidence, and exact factor contributions;
- persisted order, support ticket, and interaction records;
- the on-demand AI Account Analyst below the deterministic decision layer;
- loading, empty, error, retry, light/dark theme, and responsive states.

The MVP still excludes speculative destinations and controls for automation, RAG, external
integrations, settings, authentication, and persistent AI history.
