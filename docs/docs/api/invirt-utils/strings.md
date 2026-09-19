---
sidebar_position: 2
---

# Strings

### Anonymisation
```kotlin
"john@example.com".anonymizeEmail()        // "****@*******.***"
"john@example.com".anonymizeEmail(2)       // "**hn@*****le.com"
"top-secret".anonymize(2)                  // "********et"
```

### Case conversion
```kotlin
"helloWorld".camelToKebabCase()  // "hello-world"
"hello-world".kebabToCamelCase() // "helloWorld"
"FOO_BAR".enumLabel()            // "Foo bar"
```

### Misc string helpers
```kotlin
"Hello world  again".cleanWhitespace()  // "Hello world again"
"this is a longer string".ellipsis(10)  // "this is..."
"  ".nullIfBlank()                      // null
"Lorem ipsum dolor".wordCount()         // 3
readingTimeMinutes(wordCount)           // ceil(words / 265.0), min 1
"hello".urlEncode()                     // "hello"
"hello".titleCaseFirstChar()            // "Hello"
"john@example.com".cleanEmail()         // "john@example.com" (lower-cased, trimmed)
"HTTPS://Foo.com/path".domain()         // "foo.com"
"Café Society!!".slugify()              // "caf-society" (diacritics dropped, not folded)
"a truly long label".slugify(6)         // "a-trul" (optional maxLength, no trailing hyphen)
```

### Names
```kotlin
"Rachel Kim, PhD".greetingName("there") // "Rachel" (first whitespace token, or the fallback when blank)
"Jan van der Berg".splitName()          // "Jan" to "van der Berg" (cuts after the first token)
```

### URL helpers
```kotlin
"example.com".isUrl()              // true
"example.com".httpUrl()            // "https://example.com"
"example.com".httpUrl(https=false) // "http://example.com"
"https://x.com".httpUrl()          // "https://x.com"
```
