# Tidewater Freight: Shipping Cost Service

A Java service that calculates the cost of a freight shipment from its destination zone, its weight in kilograms, and its service level (STANDARD or EXPRESS). It was refactored from a single class that hid failures, hard-wired its rate source, and was covered by tests that proved very little.

## Overview

The service applies Tidewater's rate rules in a fixed order:

1. **Base cost:** the zone's rate per kg multiplied by the weight.
2. **Zone fee:** a flat fee per zone (DOMESTIC +0, REGIONAL +10, INTERNATIONAL +25).
3. **Heavy-item surcharge:** +20 when the weight is over 30 kg.
4. **Service level:** EXPRESS multiplies the running total by 1.5. It is applied last, so the surcharge is multiplied too.

The result is a `BigDecimal` rounded `HALF_UP` to two decimal places.

Invalid input is rejected with an exception, never priced as zero. An unknown zone, a missing field, or a non-positive weight all fail loudly.

The full analysis of the inherited code, its tests, and the design decisions is in [`ANALYSIS.md`](ANALYSIS.md).

## Features

- **Rate rules per zone:** each zone's pricing formula is an object implementing `ZoneRateRule`.
- **Validated requests:** `ShipmentRequest` rejects missing fields and non-positive weights when it is constructed, so an invalid request cannot exist.
- **Exact money:** `BigDecimal` throughout, with explicit rounding.
- **Typed failures:** a `ShippingException` hierarchy of unchecked exceptions, with causes preserved when wrapping.
- **Injected dependencies:** the service receives its rate repository and surcharge rule through its constructor, so tests can substitute either.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Language level (records, enums, `Map.of`) |
| Apache Maven 3.9.12 | Build and dependency management |
| JUnit Jupiter 5.13.4 | Automated tests, including parameterised tests |
| Mockito 5.18.0 | Test doubles for the repository |
| JaCoCo 0.8.12 | Line and branch coverage reports |
| PIT (pitest-maven 1.16.1) | Mutation testing |

The Maven Wrapper is included, so Maven 3.9 does not need to be installed separately. Use `./mvnw` on Linux or macOS, and `mvnw.cmd` on Windows.

## Project Structure

All classes are in one package, `com.tidewater.freight`, which matches the original project.

```text
src/main/java/com/tidewater/freight/
├── Demo.java                          wiring and console output
├── Zone.java                          enum: DOMESTIC, REGIONAL, INTERNATIONAL; parseZone()
├── ServiceLevel.java                  enum: STANDARD, EXPRESS
├── ShipmentRequest.java               record with validation
├── ShippingCostService.java           orchestrates the pricing steps
├── HeavyItemSurcharge.java            +20 above 30 kg
├── ZoneRateRule.java                  interface: base cost per zone
├── FlatRateZoneRule.java              rate per kg plus handling fee
├── ZoneRateRepository.java            interface: finds a rule for a zone
├── InMemoryZoneRateRepository.java    map-backed repository
├── ShippingException.java             base unchecked exception
├── UnknownZoneException.java          no rule configured for a zone
├── InvalidShipmentException.java      invalid request or unknown zone name
└── InvalidRateRuleException.java      invalid rate or fee configuration

src/test/java/com/tidewater/freight/
├── ShippingCostServiceTest.java
├── HeavyItemSurchargeTest.java
├── ServiceLevelTest.java
├── FlatRateZoneRuleTest.java
├── ShipmentRequestTest.java
├── InMemoryZoneRateRepositoryTest.java
├── ZoneTest.java
└── ShippingExceptionTest.java
```

## Design

**Single responsibility.** Each pricing step has its own class. `HeavyItemSurcharge` can be tested at its 30 kg boundary on its own, and `FlatRateZoneRule` can be tested without the service.

**Dependency injection.** `ShippingCostService` takes a `ZoneRateRepository` and a `HeavyItemSurcharge` in its constructor. Nothing inside the service calls `new` on a collaborator.

**Adding a zone.** A new zone with the same pricing shape takes three steps:

```java
// 1. Zone.java: add the constant
public enum Zone { DOMESTIC, REGIONAL, INTERNATIONAL, COASTAL; /* ... */ }

// 2. Only if COASTAL uses a different formula: implement ZoneRateRule.
//    Otherwise, reuse FlatRateZoneRule with new values.

// 3. Demo.java (the wiring): add one map entry.
Zone.COASTAL, new FlatRateZoneRule(new BigDecimal("6"), new BigDecimal("5"))
```

`ShippingCostService`, `HeavyItemSurcharge`, `ShipmentRequest`, and the exception classes are not edited. The one edit that is not avoided is `Zone.java`, because the enum is where a zone is recorded.

**Failure handling.** The service does not catch exceptions, because it cannot recover from them. `Zone.parseZone` converts the standard library's `IllegalArgumentException` into `InvalidShipmentException` and keeps the original as the cause. `Demo` catches `ShippingException` and reports it on stderr.

## Getting Started

### Prerequisites

- JDK 21 or later
- Maven 3.9 or later

### Build and Test

```bash
mvn clean verify
```

This compiles the code, runs the 34 tests, and writes a JaCoCo coverage report to `target/site/jacoco/index.html`.

### Run the Demo

```bash
mvn -q compile
java -cp target/classes com.tidewater.freight.Demo
```

Expected output:

```text
Result DOMESTIC 10kg STANDARD -> GHS 50.00
Result INTERNATIONAL 40kg EXPRESS -> GHS 967.50
Result REGIONAL with no rule configured -> REJECTED: No rate rule configured for zone: REGIONAL
Result DOMESTIC -5kg STANDARD -> REJECTED: Weight must be greater than zero: -5
```

The two `REJECTED` lines are written to stderr, so they may appear in a different colour or order in some terminals.

## Testing

The suite has 34 JUnit 5 tests in eight classes.

- **`ShippingCostServiceTest`** (7 tests): exact prices, the surcharge boundary, the order of steps, rounding, unknown zones, a null request, and one check against the original INTERNATIONAL EXPRESS price (967.50). Uses a Mockito mock of the repository for the failure case.
- **`HeavyItemSurchargeTest`**: parameterised around the 30 kg boundary (29.99, 30, 30.01, 31).
- **`ServiceLevelTest`**: multipliers, including zero cost.
- **`FlatRateZoneRuleTest`**: rate and fee arithmetic per zone, and rejection of missing or negative values.
- **`ShipmentRequestTest`**: validation of each field, and zero and negative weights.
- **`InMemoryZoneRateRepositoryTest`**: lookup, unknown zones, and that later changes to the source map do not leak in.
- **`ZoneTest`**: `parseZone` accepts a known name and rejects an unknown one with the cause kept.
- **`ShippingExceptionTest`**: message and cause preserved when wrapping.

### Mutation Testing

PIT changes the code in small ways and checks whether any test fails. Run it with:

```bash
mvn test-compile org.pitest:pitest-maven:mutationCoverage
```

The HTML report is written to `target/pit-reports/<timestamp>/index.html`.

| Suite | Mutation coverage | Test strength | Line coverage |
|---|---|---|---|
| Original (3 tests) | 17% (3 of 18 killed) | 27% | 64% |
| Refactored (`37` tests) | 94% (17 of 18 killed) | 94% (17 of 18 covered mutants killed) | 100% (46 of 46) |

Both runs have 18 mutants, so the mutation figures use the same denominator, although the mutants differ because the code changed. The refactored run has one surviving mutant, which is still open. `Demo` is excluded from PIT because it is a wiring entry point with no tests.

## Documentation

- [`ANALYSIS.md`](ANALYSIS.md): the findings on the original code, the hollow tests, the ranking, and the design decisions.
- [`ORIGINAL_OUTPUT.txt`](ORIGINAL_OUTPUT.txt): the output of the original program, kept as a baseline.

---

**Tidewater Freight**

Built with Java 21 and Apache Maven.