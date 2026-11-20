# Tidewater Freight: Analysis of the Inherited Code

## Legend

**Sources cited (all searchable)**
- **AmaliTech Coding Standards** = *AmaliTech Coding Standards: Java Edition* (the company standard). "§6.1" means section 6.1 of that document.
- **Google testable-code guide** = *Guide to Writing Testable Code*, Google Testing Blog (2008). It names four design flaws that make code hard to test:
  - flaw 1, a constructor that does real work
  - flaw 2, digging into collaborators
  - flaw 3, global state
  - flaw 4, a class that does too much
- **Fowler** = Ham Vocke, *The Practical Test Pyramid* (martinfowler.com).
- **Baeldung / dev.java** = *Custom Exceptions in Java* (Baeldung) and *Throwing Exceptions* (dev.java).

**Terms**
- **SOLID** = five object-oriented design principles. The three used here:
  - **SRP**, Single Responsibility Principle: a class has one reason to change.
  - **OCP**, Open-Closed Principle: new behaviour is added by adding code, not by editing existing code.
  - **DIP**, Dependency Inversion Principle: a class depends on an interface and is handed its collaborators, rather than creating them itself.
- **Code smell** = a symptom in code that hints at a deeper design problem. It is not a bug. Smells named here:
  - **Primitive Obsession**: plain `String` or `double` used where a proper type belongs.
  - **Shotgun Surgery**: one change forces small edits in many places.
  - **Switch Statements**: a type-based `if/else` chain that should be polymorphism.
- **Mutation testing / PIT** = a tool that deliberately breaks the code in small ways (each break is a **mutant**) and re-runs the tests. If no test fails, the mutant **survived**, which means the tests are weak.
- **C1 to C12** = my own numbering for the code findings. **T1 to T3** = my numbering for the three AI-written tests. Neither is part of any standard.

## 1. Baseline

- Environment: Java 21.0.13 (Temurin), Maven 3.9.12, JUnit 5.13.4, Mockito 5.18.0.
- `mvn clean test`: 3 tests run, 0 failures, BUILD SUCCESS.
- Original program output is saved in `ORIGINAL_OUTPUT.txt`:
  - DOMESTIC, 10 kg, STANDARD gives 50.0
  - INTERNATIONAL, 40 kg, EXPRESS gives 967.5
  - unknown zone MOON, 10 kg gives 0.0, with no error and no log line
- What the service does: it works out the cost of a shipment from its zone, weight and service level.
- Pricing rules found in the code, in the order they are applied:
  1. base = zone rate per kg x weight. The rates are DOMESTIC 5, REGIONAL 8, INTERNATIONAL 15.
  2. flat zone fee: REGIONAL +10, INTERNATIONAL +25, DOMESTIC +0
  3. heavy-item surcharge: +20 if weight is over 30 kg
  4. EXPRESS multiplies the running total by 1.5, after the fee and surcharge
- PIT mutation testing on the original code (PIT 1.16.1, before any change):
  - Line coverage: 64% (21/33 lines)
  - Mutation coverage: 17% (3 of 18 mutants killed, 15 survived)
  - Test strength: 27% (3 of the 11 mutants that the tests actually executed were killed)

## 2. Code findings

| # | Finding | Where | Rule broken | Why it matters |
|---|---|---|---|---|
| C1 | `catch (Exception e) { return 0; }` swallows every failure. An unknown zone, a null input or any bug becomes a valid-looking price of 0. The demo prints `0.0` for MOON with no error and no log. | `ShippingCalculator.calculateCost` | AmaliTech Coding Standards §6.1 (never swallow a failure) and §6.3 (add context, keep the cause) | A shipment is quoted for free and nobody is told. |
| C2 | No input validation. A negative weight gives a negative cost, zero weight still charges the zone fee, and NaN passes through. An unrecognised service level, such as lowercase `"express"`, is silently priced as standard. | `calculateCost` | AmaliTech Coding Standards §6.1. Not stated in the standard: it is ordinary defensive programming to reject bad input at the entry point. | Wrong money with no signal. |
| C3 | The rate source is hard-wired: `private final RateRepository repository = new InMemoryRateRepository();`. The `RateRepository` interface exists but nothing can inject it. The code's own comment admits this. | `ShippingCalculator` field | Dependency Inversion Principle (DIP); AmaliTech Coding Standards §12.5; Google testable-code guide, flaw 1 | No fake or mock can be substituted, which makes the class awkward to test. |
| C4 | One class does five jobs: rate lookup, zone fees, heavy surcharge, express multiplier and console output. | `ShippingCalculator` | Single Responsibility Principle (SRP); AmaliTech Coding Standards §12.1 and §4.2 (a method does one thing); Google testable-code guide, flaw 4 | It has several reasons to change, and no rule can be tested alone. |
| C5 | An `if / else if` chain on zone strings holds the zone fees, so zone knowledge is split across two classes. The rate is in the repository and the fee is in the calculator. A new zone added only to the repository would get a rate and silently no fee. | `calculateCost` | Open-Closed Principle (OCP); AmaliTech Coding Standards §12.2. Smells: Switch Statements and Shotgun Surgery. | This is exactly the coming change, and today it forces edits in two places. |
| C6 | `double` is used for money, and express is a floating-point `* 1.5`. | whole service | Not stated in the company standard. Common Java practice: `double` cannot represent many decimal fractions exactly, so money belongs in `BigDecimal`. | Rounding errors can creep into prices. |
| C7 | Magic numbers (`10`, `25`, `30`, `20`, `1.5`) and magic strings (`"DOMESTIC"`, `"EXPRESS"`). | `calculateCost` | AmaliTech Coding Standards §3.4 (searchable names, no magic numbers) | They cannot be searched or named, and a typo silently skips a rule. |
| C8 | `System.out.println` inside the business method. | `calculateCost` | AmaliTech Coding Standards §4.4 (no hidden side effects) and §8 (objects) | Presentation is mixed with logic, and the line prints only on success: MOON prints nothing. |
| C9 | Zone and service level are plain `String`s (Primitive Obsession), and the method takes three parameters. | method signature | AmaliTech Coding Standards §4.3 (few arguments) | A typo compiles fine and is only noticed at run time, if ever. |
| C10 | An empty `if (DOMESTIC) { // no extra fee }` branch, and comments that restate the code. | `calculateCost` | AmaliTech Coding Standards §10 (comment only the why; no dead code) | Noise that hides the real rules. |
| C11 | The repository throws a bare `IllegalArgumentException("Unknown zone")`. There is no custom exception, so a caller cannot tell an unknown zone from an unrelated bug. | `InMemoryRateRepository` | Baeldung *Custom Exceptions in Java* and dev.java *Throwing Exceptions*: write your own exception type when callers need to distinguish related business failures | Failures cannot be handled specifically. |
| C12 | Rates are hard-coded in the constructor, with no way to register a zone from outside. | `InMemoryRateRepository` | OCP, AmaliTech Coding Standards §12.2 | Adding a zone means editing this class. |

## 3. AI-written tests that prove nothing

| Test | What it claims | Why it proves nothing | Would breaking the rule turn it red? |
|---|---|---|---|
| T1 `calculatesExpressCost` | Express cost is 967.5 | It creates a Mockito mock of `ShippingCalculator` itself, tells the mock to return 967.5, then asserts the mock returns 967.5. The real pricing code never runs. | No. An empty method body would still pass. |
| T2 `returnsACostForAShipment` | A cost comes back | It calls `assertNotNull` on a primitive `double`. A primitive is boxed and can never be null, so this assertion cannot fail. It also never checks the value. | No. |
| T3 `calculatesACost` | Domestic cost is right | It asserts only `cost > 0`. That passes for 50.0, 51.0 or 9999.0, and it covers only the DOMESTIC path. | Only if the cost became zero or negative. A changed rate or fee stays green. |

Not tested at all:
- exact values
- the REGIONAL and INTERNATIONAL fees
- the 30 kg boundary (30 versus 30.01)
- express applied after the surcharge
- an unknown zone
- an invalid weight
- null inputs

Rule broken: AmaliTech Coding Standards §9 requires assertions that would fail if the behaviour were wrong. Fowler's *Practical Test Pyramid* says tests should check the observable result for a given input, not that a stub returns what it was told to.

Measured result: PIT broke the original code 18 times and the suite noticed only 3. Even
the lines the tests do execute are barely checked, since test strength is 27%. This
confirms that the suite proves very little, whatever its green build suggests.

## 4. Ranking

1. **C1, the swallowed failure, is the most serious problem in the code.** It turns every error into a plausible price of zero, so a bad zone ships free with no signal. No test covers it either, so nothing would ever reveal it.
2. **T1 to T3, the hollow tests, are the most serious problem in the safety net.** They are why C1 and C2 went unnoticed. They create false confidence, because a green build here says nothing about correctness.
3. C2 (no validation), C5 (blocks the coming zone) and C3 (blocks testing).
4. C4, C6, C8, C9, C11, C12.
5. C7 and C10.