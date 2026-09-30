# OpsPilot — Visual References

## Product context

OpsPilot is a B2B operational intelligence workspace for account-management teams. It consolidates account data and presents deterministic Risk, Potential, and Priority analytics so an operator can answer one question quickly: **Which accounts need attention right now?**

The visual direction must support fast comparison, confidence in the ranking, and clear explanations of how scores were produced. It must not imply that future AI analysis, automation, or RAG features are already available.

Reference notes use three evidence labels:

- **User-approved:** explicitly selected by the user as a desirable principle.
- **Verified:** independently confirmed from an accessible live reference or the existing OpsPilot product.
- **Proposal:** an original OpsPilot interpretation to be reviewed before implementation.

## Reference 01 — Orchid Security

**URL:** <https://www.orchid.security/>

### Approved visual principles

- **User-approved:** Tonal hierarchy built from stronger and softer variations of color.
- **User-approved:** Intentional contrast and layered visual intensity.
- **User-approved:** Selected animation used to create depth rather than constant movement.
- **Verified:** The live page organizes a complex technical subject into a progression from the core problem to measurable outcomes, process, use cases, and action.
- **Verified:** Explanations repeatedly connect a visible risk to the reason it exists, which is relevant to OpsPilot's explainability goal.

### Adaptation for OpsPilot

- **Proposal:** Use near-black, graphite, and elevated graphite surfaces to encode application hierarchy: canvas, shell, queue surface, selected row, and exceptional signal.
- **Proposal:** Reserve the strongest contrast for the current decision: account identity, Priority score, and the action target.
- **Proposal:** Use a single short entrance treatment for the queue or a restrained state transition after user interaction. Static operational data should remain still.
- **Proposal:** Connect scores to evidence spatially on account detail rather than separating the decision from its contributing factors.

### Elements to avoid

- Copying Orchid's branding, page sections, illustration language, or marketing composition.
- Translating a marketing-story scroll into a dashboard workflow.
- Layering effects, gradients, or motion that compete with account data.
- Turning every surface into a different color intensity without informational meaning.

## Reference 02 — WeEvolveIT

**URL:** <https://weevolveit.com/>

### Approved visual principles

- **User-approved:** A subtle dotted background used as atmosphere.
- **User-approved:** Minimalist typography, restrained composition, clear header treatment, and purposeful card styling.
- **Verified:** The live page provides a skip link and a structured header with explicit destinations.
- **Verified:** Its five-phase method is presented as a genuine sequence, so numbering carries meaning rather than acting as decoration.
- **Verified:** The copy uses short, direct statements and clear section transitions.

### Adaptation for OpsPilot

- **Proposal:** Confine a low-contrast dot field to exposed canvas areas and selected empty space around the dashboard introduction.
- **Proposal:** Use a compact application header containing only the implemented Dashboard destination, brand, monitoring status, and theme control.
- **Proposal:** Use cards only when they establish a distinct information boundary, such as the complete Attention Queue or a decision summary.
- **Proposal:** Keep interface language direct and operational. Labels should identify data, state, or action rather than decorate headings.

### Elements to avoid

- Copying the marketing hero, agency language, navigation density, or sequential storytelling.
- Repeating dot textures inside rows or every card.
- Using sequence numbers where no order exists. Queue rank is a valid use because it is backend-defined order.
- Repeating arrows, decorative labels, or rounded cards as a default visual signature.

## Reference 03 — Leo Studio

**Screenshot reference:** User-provided design concept. The supplied attachment for this task contains only the written brief; the referenced image was not available for independent inspection. The following points therefore remain **user-approved descriptions**, not independently verified observations.

### Approved visual principles

- **User-approved:** Near-black background and high-contrast light typography.
- **User-approved:** Bright yellow highlights used with restraint.
- **User-approved:** Minimal composition, precise spacing, and few decorative elements.

### Adaptation for OpsPilot

- **Proposal:** Treat yellow as a signal emitted by exceptional Priority states, focus, and the primary action—not as the default color of every score.
- **Proposal:** Use precise alignment and deliberate empty space inside a wide operational grid instead of centering a narrow content column.
- **Proposal:** Let the ranked queue be the memorable visual element; keep surrounding chrome quiet.

### Elements to avoid

- Reproducing the marketing hero or its exact layout.
- Reproducing any design-editor panels, tools, canvas controls, or editor chrome visible around the mockup.
- Using black and yellow as spectacle without operational meaning.
- Copying proprietary artwork, screenshots, or other copyrighted assets.

## Approved color palette

These colors replace the previous purple palette:

| Role | Color | Intended use |
| --- | --- | --- |
| Graphite | `#303841` | Primary light-mode text, strong structure, dark-mode supporting surface |
| Steel | `#3A4750` | Borders, dividers, secondary structure |
| Signal yellow | `#F6C90E` | Exceptional priority, focus, and primary emphasis |
| Soft white | `#EEEEEE` | Dark-mode principal text and light-mode foundation |

Yellow is not suitable for small text on a light background. When yellow is used as a filled light-mode control or status, its foreground must be dark graphite.

## Approved dark-mode foundation

| Role | Color |
| --- | --- |
| Primary application background | `#0B0D10` |
| Major surfaces and application header | `#11151A` |
| Elevated panels and cards | `#1A2128` |
| Secondary structure | `#303841` |
| Borders and supporting surfaces | `#3A4750` |
| Primary accent | `#F6C90E` |
| Principal text | `#EEEEEE` |

The approved concept is **black base + graphite layers + signal yellow**. Near-black establishes focus; graphite communicates depth; yellow identifies a decision signal.

## Shared visual principles

- The ranked operational queue is the primary visual event.
- Visual intensity must follow decision importance.
- Structure must encode information: rank, score family, current state, or action.
- Use a wide, left-aligned workspace at desktop resolutions.
- Preserve breathing room without leaving large inactive gutters.
- Prefer sentence case and readable operational labels over repeated uppercase kickers.
- Use typography, alignment, and tonal layering before adding decoration.
- Use cards selectively, with distinct responsibilities and varied hierarchy.
- Keep Risk, Potential, and Priority visually related but semantically distinct.
- Preserve exact backend ranking and values.
- Dark and light themes must share an identity while being designed independently.

## Previous design problems to avoid

- A 220px full-height sidebar for one implemented destination.
- A narrow, overly centered 1240px content area on large screens.
- Tiny metadata and labels below comfortable operational reading sizes.
- A generic administration-table appearance.
- Repeated pills and badges for every value.
- Yellow backgrounds or borders on every priority row.
- Equal visual weight for total counts, secondary metrics, and the primary decision.
- A light theme that looks like a simple inversion of dark mode.
- Decorative labels, texture, or motion that does not communicate state.
- Adding visible controls for unimplemented AI, automation, or RAG features.

## Reference usage rules

1. References supply principles, not layouts to copy.
2. User-approved observations may guide design even when visual verification is unavailable, but must remain labeled as user-provided.
3. Verified observations are limited to information independently observable from the accessible source.
4. New OpsPilot decisions must be labeled as proposals until approved.
5. No external screenshot, image, logo, illustration, font file, or other copyrighted asset is added to the repository without explicit approval and licensing review.
6. The palette, information model, accessibility requirements, and backend-defined ordering take precedence over reference aesthetics.
7. Future AI and automation capabilities may inform extensibility, but must not appear as active interface features before implementation.
