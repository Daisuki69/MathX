# SageMath Formula Lab & Scientific Keyboard

An offline SageMath-compatible formula validation, token mutation, and interactive scientific calculator environment for Android built with Jetpack Compose.

> [!IMPORTANT]
> **Confirmed Architecture & UX Decisions from Phase 1:**
> - **Validation & Execution Strategy**: Fully offline SageMath-compatible symbolic lexer, recursive descent parser, and AST validator with exact SageMath grammar (implicit multiplication, power operators `^` / `**`, Python/Sage mathematical functions, symbolic variables, constants, and calculus notations).
> - **Formula Randomization Behavior**: Identifies detected symbolic variables, constants, and operators in the input formula; mutates constants and variables into valid alternative expressions while preserving mathematical syntactic integrity; displays token breakdowns alongside mutated expressions in the Output box.
> - **Scientific Keyboard Layout**: Custom embedded tabbed keyboard featuring dedicated tabs for **Algebra**, **Calculus & Analysis**, and **Greek & Symbols**, with seamless text cursor insertion and tactile haptic feedback.

---

### 1. Overview & Core Concept

- **What It Does**: Provides a dedicated mathematical laboratory featuring an interactive Formula input box. Tapping the formula box reveals a built-in scientific calculator keyboard with specialized tabs (Algebra, Calculus, Greek symbols, and Standard keypad). The engine parses and validates SageMath mathematical syntax offline, extracts all variables (e.g. $x, y, \theta$), numerical constants, and operators, and provides a one-tap randomizer that mutates these tokens into mathematically valid variant expressions displayed in a rich Output card.
- **Target Audience / Persona**: Students, mathematicians, educators, researchers, and competitive exam creators who need to quickly enter complex formulas, verify syntax compatibility with SageMath, and generate mutated variant problems and test cases.
- **Key Value**: Instant offline validation without server latency, effortless input of complex mathematical symbols without switching system keyboards, and intelligent AST-based formula mutation.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Formula Input & Keyboard Activation**:
   - The user views the clean mathematical workspace with the **Formula** container at the top and the **Output** container below.
   - Tapping the Formula field activates cursor focus and slides up the custom **Scientific Keyboard** anchored at the bottom, suppressing intrusive system soft-keyboards while providing specialized math keys.
   - Switching between tabs (**Algebra**, **Calculus**, **Greek**, **Numbers & Basic**) allows one-touch entry of symbols like $\int, \frac{d}{dx}, \sqrt{\cdot}, \pi, \infty, \sin, \cos, \theta, \lambda$.
2. **Real-time SageMath Validation**:
   - As expressions are entered, an inline badge indicates status: Valid Sage expression (Green with check), Syntax warning (Yellow), or Invalid (Red with exact error position and suggestion).
3. **Randomization & Output Generation**:
   - Tapping the **Randomize** action button triggers the AST mutation engine.
   - The detected tokens (variables, constants, operators) are highlighted in interactive chips.
   - The mutated formula is rendered in the Output box, complete with quick actions to Copy, Insert Back, or Inspect token mappings.

#### Visual Identity & Theme
- **Aesthetic Direction**: Modern High-Precision Engineering Lab — deep slate dark backdrop with electric indigo and cyan accents, clean border strokes, monospace mathematical typography, and high-contrast tactile keys.
- **Color Palette & Tokens**:
  - `Background`: Deep Midnight Slate (`#0B0F19`)
  - `Surface / Container`: Elevated Dark Slate (`#151D2E`)
  - `Surface Variant / Keycap`: Tactile Slate (`#1E293B`)
  - `Keycap Active / Highlight`: Electric Indigo (`#6366F1`)
  - `Primary Accent`: Bright Cyan (`#06B6D4`)
  - `Success / Valid`: Emerald Green (`#10B981`)
  - `Error / Invalid`: Crimson Coral (`#F43F5E`)
  - `Text Primary`: Crisp White (`#F8FAFC`)
  - `Text Secondary`: Muted Lavender (`#94A3B8`)
- **Typography & Hierarchy**:
  - Display Headers: JetBrains Mono / Space Grotesk styling for technical clarity.
  - Formula Fields: Tabular monospace font with bold mathematical glyphs, subtle brackets coloring, and large legible text sizes ($20\text{sp}$ to $24\text{sp}$).
  - Keycap Labels: Clean, legible centered mathematical symbols ($16\text{sp}$ to $20\text{sp}$).
- **Interactive Feedback & Motion**:
  - Smooth slide-in and slide-out keyboard animations with Compose `AnimatedVisibility`.
  - Spring-based key press ripples and micro-bounce on keyboard taps.
  - Haptic feedback tick on keypress.
  - Smooth crossfade transitions in the Output box when displaying mutated formula variants.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Offline SageMath AST Parser vs Heavy Python Binary**
  - *Chosen Approach*: A zero-dependency, high-performance recursive descent mathematical lexer and AST parser implemented directly in Kotlin that matches SageMath's syntax specifications (supporting Sage variable declarations, implicit multiplications like `2x` $\to$ `2*x`, power operators `^` and `**`, standard CAS functions `sin, cos, exp, log, diff, integrate`, Greek variable identifiers, and rational fractions).
  - *Why*: SageMath is a massive multi-gigabyte Python/C++ distribution that cannot run natively inside an Android client. An in-process Kotlin engine guarantees instant $0\text{ms}$ latency, zero network dependency, zero battery drain, and $100\%$ reliable offline behavior.
  - *Alternatives Considered*: Bundling a Python runtime via Chaquopy or termux-chroot was rejected because it balloons APK size by $>150\text{MB}$, incurs multi-second startup delays, and causes severe device compatibility crashes.
- **Decision 2: Token-Aware AST Mutation vs Naive String Replacement**
  - *Chosen Approach*: Syntax-tree-aware mutation that traverses expressions, detects constants (integers, floats, standard named constants), variables, and binary operators, and substitutes them with mathematically compatible random alternatives while preserving expression arity and parenthesization.
  - *Why*: Naive string regex replacement breaks syntax (e.g., replacing `x` inside `exp` would corrupt function names). AST mutation guarantees the mutated result remains a valid SageMath expression.
- **Decision 3: In-App Embedded Soft Keyboard vs System IME Service**
  - *Chosen Approach*: Built-in in-app Jetpack Compose tabbed scientific keyboard component integrated directly into the screen layout with custom text cursor state management.
  - *Why*: An in-app keyboard does not require the user to navigate to Android System Settings to enable an input method or grant system-wide permissions. It works immediately out of the box with zero setup.

---

### 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                       UI Layer (M3)                         │
│  ┌───────────────────────┐       ┌───────────────────────┐  │
│  │     Formula Box       │       │      Output Box       │  │
│  │ (Rich Editable Field) │       │ (Result & Token View) │  │
│  └───────────┬───────────┘       └───────────▲───────────┘  │
│              │                               │              │
│              ▼                               │              │
│  ┌───────────────────────┐                   │              │
│  │  Scientific Keyboard  │                   │              │
│  │ [Alg] [Calc] [Greek]  │                   │              │
│  └───────────┬───────────┘                   │              │
└──────────────┼───────────────────────────────┼──────────────┘
               ▼                               │
┌──────────────────────────────────────────────┴──────────────┐
│                    ViewModel & State                        │
│  - FormulaState (raw text, cursor selection)                │
│  - ValidationResult (isValid, ast, tokens, errorDetails)    │
│  - MutatedFormulaResult (outputFormula, substitutedTokens)  │
│  - ActiveKeyboardTab (Algebra, Calculus, Greek, Basic)      │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 Offline Sage Engine & AST                   │
│  ┌───────────────────┐               ┌───────────────────┐  │
│  │ SageMath Lexer &  │──────────────>│ AST Validator &   │  │
│  │ Tokenizer         │               │ Token Collector   │  │
│  └───────────────────┘               └─────────┬─────────┘  │
│                                                │            │
│                                                ▼            │
│                                      ┌───────────────────┐  │
│                                      │ AST Formula       │  │
│                                      │ Mutator Engine    │  │
│                                      └───────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

#### Data Model & State
- `FormulaToken`: Type (Variable, Constant, Operator, Function, Parenthesis), raw text, and position.
- `ValidationState`: `Valid(ast: SageNode, tokens: List<FormulaToken>)`, `Invalid(message: String, errorIndex: Int)`, or `Empty`.
- `MutationOption`: Configuration for mutation intensity (e.g. mutate constants, change variable names, swap complementary operators like `+`/`-` or `*`/`/`).
- `KeyboardKey`: Key definition with primary label, insert string, display symbol, category, and cursor offset adjustment.

#### Interactive Component & State Mapping
- **Formula Field Focus / Tap**: Updates `isKeyboardVisible = true`, positions cursor accurately, and highlights the active formula card with an animated indigo border.
- **Key Tap**: Inserts characters/functions (e.g. `sin()`, `^`, `\theta`) at the exact cursor position, advancing the cursor inside parentheses automatically.
- **Real-Time Parsing**: Debounced parsing via Kotlin StateFlow pipeline automatically emits token counts and validation status.
- **Randomize Button**: Triggers `viewModel.randomizeFormula()`, executing the AST mutator and posting the updated variant to the Output box.
- **Output Actions**: One-touch Copy to Clipboard, Use as Input, and Re-roll Mutation.
