# Battle Arena Formulas

This document explains the formulas used in the Superhero Battle Arena engine (`BattleEngineUseCase.java`).

## 1. Speed (Turn Order)

**Stat:** `Speed`

Turn order is determined by sorting the heroes by `Speed` in descending order. When two heroes have the same speed, their `Combat` skill is used as a tie-breaker. The hero with the highest speed attacks first in each turn.

---

## 2. Dodge (Combat Skills)

**Stat:** `Combat`

When a hero is attacked, they calculate their probability to dodge based on their `Combat` skill relative to the attacker's `Combat` skill.

**Formula:**

```
Dodge Chance = Target.Combat / (Target.Combat + Attacker.Combat + 0.1)
```

*Note: The `0.1` is included to prevent division by zero in case both heroes have `0` combat skill.*

**Characteristics:**

* Maximum dodge chance is capped at `50%`.
* If a dodge is successful, a `DODGE` event is emitted, and `0` damage is dealt to the target.

**Example:**

* **Batman (Target)** Combat: 100
* **Superman (Attacker)** Combat: 85
* `Dodge Chance = 100 / (100 + 85 + 0.1) = 100 / 185.1 ≈ 54%`
* Since the max cap is 50%, Batman will have a `50%` chance to dodge Superman's attacks.

---

## 3. Critical Hits (Intelligence)

**Stat:** `Intelligence`

High intelligence gives the attacker a chance to find weak spots and land a critical hit.

**Formula:**

```
Critical Hit Chance = Attacker.Intelligence / 200.0
```

**Characteristics:**

* If a critical hit occurs, the final damage multiplier is increased by `1.5x`.
* A `CRITICAL_HIT` event is emitted instead of a regular `HIT` event.

**Example:**

* **Iron Man (Attacker)** Intelligence: 100
* `Critical Hit Chance = 100 / 200.0 = 50%`
* Iron Man has a `50%` chance to land a critical hit on each attack.

---

## 4. Damage Calculation (Power and Strength)

**Stats:**

* **Attack:** `Power`
* **Defense:** `Strength`

If the attack is not dodged, damage is dealt to the target's HP (Stamina, which starts equal to `Durability`).

**Formula:**

```
Damage Multiplier = 1.0 * TagModifiers * CriticalMultiplier(1.5 if crit)
Raw Damage = (Attacker.Power * Damage Multiplier) - (Target.Strength * 0.6)
Final Damage = Max(1, Raw Damage)
```

*Note: The damage multiplier starts at `1.0`. Tag modifiers are multiplied in based on the current RoundSpec. If the attack is a critical hit, the multiplier is multiplied by `1.5`.*

**Example:**

* **Superman (Attacker)** Power: 100
* **Hulk (Target)** Strength: 100
* **Modifiers**: None (1.0), Not a critical hit.
* `Raw Damage = (100 * 1.0) - (100 * 0.6) = 100 - 60 = 40`
* Superman deals `40` damage to Hulk.

**Example 2 (Critical Hit):**

* **Iron Man (Attacker)** Power: 100, lands a Critical Hit.
* **Captain America (Target)** Strength: 60
* **Modifiers**: Critical Hit (1.5x)
* `Raw Damage = (100 * 1.5) - (60 * 0.6) = 150 - 36 = 114`
* Iron Man deals `114` damage to Captain America.
