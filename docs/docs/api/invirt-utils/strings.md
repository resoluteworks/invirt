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
```

### URL helpers
```kotlin
"example.com".isUrl()              // true
"example.com".httpUrl()            // "https://example.com"
"example.com".httpUrl(https=false) // "http://example.com"
"https://x.com".httpUrl()          // "https://x.com"
```
