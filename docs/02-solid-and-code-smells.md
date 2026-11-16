# SOLID and Code Smells: Spotting Them in the Interview

Method for a "works, but is conceptually flawed" snippet:
1. Read it and say **what it does** (one sentence).
2. Count the **reasons to change** of each class (SRP).
3. Look for `if/switch` on types or `instanceof` (OCP, polymorphism).
4. Look for `new` inside business logic and static calls (DIP, testability).
5. Name the smell (refactoring.guru) and the refactoring technique you would apply.

## SOLID with an example from this app

**Flawed example** (it works, but it smells):

```java
public class UserService {
    public void createUser(UserRequest r) {
        if (r.getEmail() == null || !r.getEmail().contains("@")) throw new RuntimeException("bad");
        double bmi = r.getWeight() / Math.pow(r.getHeight() / 100.0, 2);
        String plan;
        if (r.getGoal().equals("LOSE_WEIGHT")) plan = "cardio";
        else if (r.getGoal().equals("GAIN_WEIGHT")) plan = "weights";
        else plan = "general";
        Connection c = DriverManager.getConnection("jdbc:postgresql://...");
        c.createStatement().execute("INSERT ... '" + r.getEmail() + "'");   // SQL injection
        new EmailSender().send(r.getEmail(), "Welcome " + plan);
    }
}
```

| Principle | Violation here | Fix |
|-----------|----------------|-----|
| **S**RP | validates, computes BMI, picks a plan, persists and sends email | split into `UserValidator`, `HealthMetricsService`, `PlanRecommender`, `UserRepository`, `Notifier` |
| **O**CP | if/else chain per goal: a new goal means editing the class | one `Strategy` per goal (Replace Conditional with Polymorphism) |
| **L**SP | subclass throws `UnsupportedOperationException` for an inherited method | prefer composition / smaller interfaces |
| **I**SP | huge `UserOperations` interface with methods many clients don't use | role-based interfaces |
| **D**IP | `new EmailSender()`, direct `DriverManager` | inject abstractions through the constructor |

Other problems in the example: generic `RuntimeException`, magic strings (`"LOSE_WEIGHT"` → enum), concatenated SQL (use `PreparedStatement`/JPA), no transaction, mixed layers.

## Code smells (refactoring.guru) → technique
| Category | Smell | Technique |
|----------|-------|-----------|
| Bloaters | Long Method, Large Class, Primitive Obsession, Long Parameter List, Data Clumps | Extract Method, Extract Class, Replace Primitive with Object (value objects/records), Introduce Parameter Object |
| OO Abusers | Switch Statements, Refused Bequest, Temporary Field | Replace Conditional with Polymorphism, Replace Inheritance with Delegation |
| Change Preventers | Divergent Change, Shotgun Surgery | Extract Class, Move Method |
| Dispensables | Duplicate Code, Dead Code, Lazy Class, Comments | Extract Method, Inline Class |
| Couplers | Feature Envy, Message Chains, Middle Man | Move Method, Hide Delegate |

## Most-used patterns (we will apply them as we go)
Strategy (recommender per goal), Factory (plan creation), Builder (complex DTOs), Template Method (plan generation), Observer/events (`ApplicationEventPublisher`: "user created"), Adapter (integrations), Decorator (caching/logging), Repository (Spring Data).

## Exercise 1 (do it before the next session)
For the `UserService` example: write on paper the 5 resulting classes and which pattern the recommender would use. In step 1 we implement it and you compare it with your design.
