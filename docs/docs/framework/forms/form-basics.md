---
sidebar_position: 1
---
import formBasics from './assets/form-basics.png';

# Form basics
[Example application](https://github.com/resoluteworks/invirt/tree/main/examples/form-basics)

Invirt provides a simple `Request.toForm<FormType>()` utility to convert complex HTML form bodies to application model objects,
with support for arrays, maps and nested objects, similar to some other MVC frameworks.

Below is a (crude) example of a form with a variety of controls and inputs for collecting details
about an online order.

<img src={formBasics} width="600"/>

<br/>
<br/>
A potential Kotlin data model for this form can be something along these lines.
```kotlin
data class OrderForm(
    val name: String,
    val email: String,
    val deliveryDetails: DeliveryDetails,
    val notifications: Set<NotificationType>,
    val quantities: Map<String, Int>
)

data class DeliveryDetails(
    val whenNotAtHome: String,
    val deliveryDate: LocalDate
)

enum class NotificationType(val label: String) {
    DISPATCHED("Dispatched"),
    IN_TRANSIT("In transit"),
    DELIVERED("Delivered")
}
```

The HTML form inputs need to match the field names in our Kotlin data model, with nested fields using the dot notation
`deliveryDetails.deliveryDate`, and maps or arrays using the square brackets notation `quantities[Apples]` (no apostrophes
or quotes required).
```html
<form action="/save-order" method="POST">
    <input type="text" name="name"/>
    <input type="text" name="email"/>
    <input type="date" name="deliveryDetails.deliveryDate"/>

    <input type="radio" name="deliveryDetails.whenNotAtHome" value="Leave with neighbour"/>
    <input type="radio" name="deliveryDetails.whenNotAtHome" value="Leave in the back"/>

    <input type="checkbox" name="notifications" value="DISPATCHED"/>
    <input type="checkbox" name="notifications" value="IN_TRANSIT"/>
    <input type="checkbox" name="notifications" value="DELIVERED"/>

    <input type="text" name="quantities[Apples]"/>
    <input type="text" name="quantities[Oranges]"/>
    ...
</form>
```

Reading this form into the `OrderForm` object in an http4k handler is then as simple as:
```kotlin
"/save-order" POST { request ->
    val form = request.toForm<OrderForm>()
    // Process the form
}
```

For the complete working example checkout the [example application](https://github.com/resoluteworks/invirt/tree/main/examples/form-basics).
