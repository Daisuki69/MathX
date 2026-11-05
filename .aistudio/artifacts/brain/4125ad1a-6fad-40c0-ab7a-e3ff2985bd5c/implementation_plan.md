# Photomath Parity: 2D Structural Math Engine & Keypad Refactoring

Comprehensive architectural plan to eliminate all range parser anomalies, out-of-order typing race conditions, baseline misalignment, and dead cursor zones, while introducing continuous press-drag-release popup navigation and native single-character Unicode mathematical Greek typesetting.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following core interaction and typesetting decisions have been confirmed and incorporated directly into this architectural blueprint:

- **Confirmed Decision 1: Continuous Press-Drag-Release Alternate Popups**:
  - The alternate key popup uses `PopupProperties(focusable = false)` so the touch pointer stream is never hijacked or interrupted.
  - When a key is held for ~250ms, the popup opens with the first alternate option highlighted by default.
  - The user can slide their finger horizontally across alternate options (`(deltaX / itemWidthPx).toInt().coerceIn(0, alternates.lastIndex)`) and release directly onto the desired keycap to commit the action, or slide vertically off-target to cancel cleanly.
- **Confirmed Decision 2: Single-Character Unicode Greek Typesetting**:
  - Keyboard layouts and formula state store single-character Unicode glyphs (`α`, `β`, `γ`, `θ`, `π`, `Δ`) instead of multi-character English spellings (`"alpha"`, `"theta"`).
  - Deletion, cursor navigation, and parsing become atomic 1-character operations without dead cursor zones or freeze glitches.
  - Translation to ASCII names for Symja computation occurs strictly at the engine boundary in `MathFormatter.toParseableString()` and is converted back via `MathFormatter.toDisplayString()`.

---

## 1. Overview & Core Concept

- **What It Does**: Upgrades the formula editing and keypad engine from a string-dependent parser to a true 2D Abstract Syntax Tree (AST) renderer with zero range drift, atomic cursor positioning, seamless structural template wrapping, aligned mathematical typography, and tactile two-thumb typing responsiveness.
- **Target Audience**: Students, researchers, and engineers demanding instant, textbook-accurate equation composition on mobile.
- **Key Value**: Delivers genuine Photomath-level fluid touch response, eliminating ghost parentheses, dead cursor gaps, out-of-order keystrokes, and text-baseline collisions.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  - **Fluid Two-Thumb Typing**: Every primary key triggers immediately on touch-down (`awaitFirstDown`), completely removing release latency and preventing race-condition keystroke reordering.
  - **Press-Drag-Release Alternates**: Holding any multi-function key (e.g., `√x`, `x²`, `=`, `÷`, `sin`) opens a floating selector above the key; sliding the finger smoothly highlights options, and lifting inserts the selection immediately.
  - **Pristine Structural Fractions & Exponents**:
    - Pressing `a/b` inserts clean empty structural slots $\frac{\square}{\square}$ without space-character traps (`"( )/( )"`).
    - Pressing `x²` or `xⁿ` on an empty slot renders pure visual placeholder boxes $\square^2$ and $\square^{\square}$ without ghost outer parentheses.
  - **Seamless Structural Navigation**:
    - Stepping with `←` and `→` moves fluidly across all visual boundaries with zero invisible dead zones.
    - Tapping directly on the left or right half of any token places the cursor accurately before or after that specific glyph.
    - Long formulas automatically scroll horizontally to keep the active cursor centered and visible.

- **Visual Identity & Mathematical Typesetting**:
  - **TeX-Grade Math Typography**:
    - Variables ($x, y, z, \theta, \alpha, \beta$) typeset in serif italics (`FontFamily.Serif`, `FontStyle.Italic`).
    - Operators ($+, -, \cdot, =$) rendered with standard 4.dp horizontal typographical breathing room.
    - Exponents aligned to the true mathematical baseline of the base expression rather than floating or colliding.
    - Square root vinculum extends dynamically from a fixed-geometry stem (`10.dp`) without diagonal slash distortion.
  - **Color Palette & Atmosphere**:
    - Keyboard background: deep slate `#0F131C` with border `#1E2433`.
    - Active placeholder box: glowing cyan border `#38BDF8` with pulsing cursor bar.
    - Variables: `#818CF8`; Operators: `#38BDF8`; Functions: `#C084FC`; Actions: `#E11D48`.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Zero-Space Structural Representation (`""` vs `"( )"`)**:
  - *Chosen Approach*: Structural slots without content will be represented as empty parentheses `""` or `"()"` internally without arbitrary space padding (`" "`).
  - *Why*: Eliminates phantom space character traps that broke `numRange`, corrupted click hit-testing, and caused cursors to vanish.
  - *Alternatives Considered*: Retaining space characters required fragile regex stripping and manual offset tracking that broke cursor stepping.

- **Decision 2: Universal Touch-Down Dispatch with Gesture Continuation**:
  - *Chosen Approach*: All primary keys invoke `onAction()` on pointer-down immediately. If a key has alternates, holding for 250ms enters drag-tracking mode without revoking the initial stroke unless an alternate is explicitly chosen.
  - *Why*: Eliminates race-condition inversion where fast alternating two-thumb typing entered characters in reverse order.

- **Decision 3: Bidirectional Coordinate Hit-Testing for Text Tokens**:
  - *Chosen Approach*: Tapping a text glyph measures whether the touch fell on the left or right 50% of the token width, assigning cursor index to `node.start` or `node.end`.
  - *Why*: Solves the defect where tapping the right side of a multi-digit number or variable jumped the cursor to the left.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                       MainActivity                          │
│  ┌───────────────────────┐       ┌───────────────────────┐  │
│  │      FormulaBox       │       │       OutputBox       │  │
│  │ (Auto-scroll & Tap)   │       │ (Symja Mutated View)  │  │
│  └───────────▲───────────┘       └───────────▲───────────┘  │
└──────────────┼───────────────────────────────┼──────────────┘
               │                               │
               │ State Flow                    │
┌──────────────▼───────────────────────────────▼──────────────┐
│                      MainViewModel                          │
│  - formulaInput: TextFieldValue (Unicode Display)           │
│  - Smart Wrapping (Fraction / Exponent / Radical)           │
│  - Structural Climbing Out & Function-Aware Backspace       │
│  - Boundary Normalization (MathFormatter)                   │
└──────────────▲───────────────────────────────▲──────────────┘
               │                               │
┌──────────────┴───────────────┐ ┌─────────────┴──────────────┐
│      MathLayoutEngine        │ │      SciCalcKeyboard       │
│  - AstParser (Pure AST)      │ │  - Instant Touch-Down      │
│  - Range-Accurate Cursor     │ │  - Press-Drag-Release Pop  │
│  - Baseline Math Alignment   │ │  - Scaled Mini-Templates   │
│  - Dynamic Canvas Sqrt       │ │  - Repeat Action Engine    │
└──────────────────────────────┘ └────────────────────────────┘
```

### Component & File Mapping

1. **`app/src/main/java/com/example/ui/keyboard/KeyboardLayouts.kt`**:
   - Replace English spellings in `greekRows` and `basicRows` with atomic Unicode characters (`α`, `β`, `γ`, `δ`, `ε`, `θ`, `λ`, `μ`, `π`, `ρ`, `σ`, `τ`, `φ`, `ω`, `Δ`).
   - Remove redundant inverse trigonometric duplicates from `functionRows` and align Calculus tab keys.
   - Attach alternate keys to all mathematical variants (`x³`, `x⁴`, `10ⁿ`, `eⁿ`, `∛`, `ⁿ√`).

2. **`app/src/main/java/com/example/ui/keyboard/SciCalcKeyboard.kt`**:
   - Refactor `KeyButton` to trigger `onAction()` on `awaitFirstDown` universally for zero release latency.
   - Implement continuous pointer tracking: when held for 250ms, open non-focusable `Popup`, track horizontal drag offset across alternate options, highlight hovered keycap, and commit on pointer release.

3. **`app/src/main/java/com/example/ui/math/MathLayoutEngine.kt`**:
   - Eliminate phantom space tokenization in `AstParser` (`"()"` and `""` directly map to `EmptyPlaceholder`).
   - Unwrap `ParenNode` in exponents and powers to prevent `([□])²` ghost parentheses.
   - Fix cursor dead zones: map cursor positions across function headers (`sin`, `sqrt`, `cbrt`) and operator boundaries so the cursor never vanishes.
   - Adjust `PowerNode` layout with baseline alignment so base characters align with preceding terms.
   - Add operator horizontal padding (`3.dp`) and auto-scroll correlation in `MathFormulaView`.
   - Implement width-fraction hit-testing in `RenderTextToken` (left half $\rightarrow$ start, right half $\rightarrow$ end).

4. **`app/src/main/java/com/example/MainViewModel.kt`**:
   - Adjust `handleFractionInput` to wrap without internal space characters.
   - Extend smart wrapping across all alternate power and root functions (`x³`, `x⁴`, `10ⁿ`, `eⁿ`, `∛`, `ⁿ√`).
   - Update `onBackspace` to handle clean removal of atomic Greek letters, factorial `!`, and space-free empty slots.

5. **`app/src/main/java/com/example/ui/MathFormatter.kt`**:
   - Map single-character Greek Unicode symbols to Symja ASCII identifiers in `toParseableString()` and restore them in `toDisplayString()`.
