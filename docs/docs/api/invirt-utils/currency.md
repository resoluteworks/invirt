---
sidebar_position: 4
---

# Currency

Currency-symbol-aware formatting of `Long` minor-unit amounts (pence/cents). Symbol lookup uses
the `currency/currency-symbols.properties` resource bundled with `invirt-utils`; unknown currencies
fall back to the ISO code.

### Currency.minorUnitToString
Formats including the default fraction digits for the currency.

```kotlin
Currency.getInstance("GBP").minorUnitToString(134)            // "£1.34"
Currency.getInstance("GBP").minorUnitToString(-1_234_567_89)  // "-£12,345,678.99"
Currency.getInstance("EUR").minorUnitToString(-32_095_812_885)
// "-€320,958,128.85"
```

### Currency.minorUnitToStringRounded
Same as above, but rounds to the nearest major unit (`HALF_UP`), dropping the decimal portion. Useful
for compact display:

```kotlin
Currency.getInstance("USD").minorUnitToStringRounded(7_500)  // "$75"
Currency.getInstance("USD").minorUnitToStringRounded(7_550)  // "$76"
```

### Currencies.getSymbol
```kotlin
Currencies.getSymbol("GBP")  // "£"
Currencies.getSymbol("XYZ")  // "XYZ" (fallback when unknown)
```

The Pebble template engine has a matching
[`currencyFromMinorUnit`](/docs/api/invirt-core/pebble-functions#currencyfromminorunitminorunitamount-currency)
function.
