# Java 8 → 27: What You Need to Know

> Feature lists come from my knowledge up to Java 25. For Java 26/27, verify at openjdk.org/projects/jdk/27. LTS releases are marked.

## Java 9–11
- **9**: modules (JPMS), `List.of/Set.of/Map.of`, `private` interface methods, JShell.
- **10**: `var` (local variable type inference).
- **11 (LTS)**: standard `HttpClient`, `String.isBlank/strip/lines/repeat`, `var` in lambdas, `Files.readString`.

## Java 12–17
- **14**: switch expressions (`case X ->`, `yield`).
- **15**: text blocks `"""`.
- **16**: **records**, pattern matching for `instanceof`.
- **17 (LTS)**: **sealed classes** (`sealed ... permits`). Minimum version required by Spring Boot 3/4.

## Java 18–21
- **18**: UTF-8 by default, simple web server.
- **21 (LTS)**: **virtual threads** (Project Loom), pattern matching in `switch` and **record patterns**, `SequencedCollection` (`getFirst/getLast/reversed`).

## Java 22–25
- **22**: unnamed variables `_`.
- **24/25**: Stream Gatherers, Class-File API, scoped values, flexible constructor bodies, compact object headers (opt-in), module import declarations, **simplified instance `main`** (`void main()`).
- **25 (LTS)**: the version many companies will adopt as their next LTS after 21.

## Java 26–27
- **27** (Sept 2026): according to press coverage, G1 as the default GC everywhere, compact object headers on by default, post-quantum hybrid TLS 1.3, redaction in JFR. Confirm the list on the official page.

## Before/after example

```java
// Java 8
public class Dto {
    private final String name; private final int age;
    public Dto(String n, int a){ name=n; age=a; }
    public String getName(){ return name; } /* getAge, equals, hashCode, toString... */
}
String desc;
switch (level) { case LOW: desc = "low"; break; case HIGH: desc = "high"; break; default: desc = "?"; }

// Java 21+
public record Dto(String name, int age) {}
String desc = switch (level) { case LOW -> "low"; case HIGH -> "high"; };
```

## Typical interview questions
1. What is the difference between a `record` and a class with Lombok `@Value`?
2. When should you use virtual threads, and when NOT (CPU-bound work, `synchronized` pinning)?
3. What are sealed classes for, and how do they pair with exhaustive `switch`?
4. What is `Optional`, and why shouldn't it be used as a field or parameter?
5. Streams: what happens if you use `parallelStream` with shared mutable state?

Where we saw this in the code: `HealthMetrics` (record, enum, switch expression), `HealthController.AssessmentRequest` (record + validation).
