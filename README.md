# Tablespoon

Tablespoon *(creatively named after Dagger and Butterknife)* helps you bind attributes easily in your custom views using annotations to generate boilerplate code.
```kotlin
class CustomView (...) : View(...) {

  @ColorAttr(R.styleable.CustomView_bgColor)
  var bgColor: Int = Color.RED

  @DimensionAttr(R.styleable.CustomView_radius)
  var radius: Float = 0f

  var text: String by dynamicAttr("") // Auto updates View when being set

  init {
    TableSpoon.init(this, attrs, R.styleable.CustomView)
  }
}
```

## Features

 1. Avoid boilerplate code such as:
```kotlin
val typedArray = view.context.theme.obtainStyledAttributes(...)
try {
  radius = typedArray.getDimension(...)
  // do more with typedArray...
} finally {
  a.recycle()
}
```
2. Make your properties dynamic by using the `dynamicAttr` extension. When these properties are updated, the view is automatically updated by calling `requestLayout()` and `invalidate()` on the View.

## Install
You can install Tablespoon by adding this to your build.gradle file:

```groovy
dependencies {
  // TODO
}
```
#### Limitation
From Android Gradle Plugin 5.0 onwards (yet to be released), resource IDs (any `R.xyz` value) will be non final. To circumvent this, add [Butterknife's gradle plugin](https://github.com/JakeWharton/butterknife#library-projects) to your `buildscript`.
```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'com.jakewharton:butterknife-gradle-plugin:10.1.0'
  }
}
```

and then apply it in your module:

```groovy
apply plugin: 'com.android.library'
apply plugin: 'com.jakewharton.butterknife'
```
Now make sure you use R2 instead of R inside all Tablespoon annotations:

```kotlin
@ColorAttr(R2.styleable.CustomView_bgColor)
var bgColor: Int = Color.RED
```


## Usage

First, all the attributes must be declared in `res/values/attrs.xml`. For example:

```xml
<resources>
  <declare-styleable name="CustomView">
    <attr name="bgColor" format="color" />
    <attr name="radius" format="dimension" />
    <attr name="icon" format="reference" />
  </declare-styleable>
</resources>
```

Each of these attributes can then be used with their respective annotation.
- All annotations must be defined with the corresponding attribute ID as defined above.
- `Tablespoon.init()` must be called in the view constructor  with the parent styleable ID as defined above (`R.styleable.CustomView`)
- Default values can directly be defined on the property
- **Properties cannot be `private` or `protected`**.

### Example
```kotlin
class CustomView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : View(context, attrs, defStyleAttr) {

  @ColorAttr(R.styleable.CustomView_bgColor)
  var bgColor: Int = Color.RED // RED is default value

  @DimensionAttr(R.styleable.CustomView_radius)
  var radius: Float = 0f // 0f is default value

  // dynamic attributes auto update your view when they are updated
  @delegate:DrawableAttr(R.styleable.CustomView_icon)
  var icon: Drawable? by dynamicDrawableAttr(null)

  init {
    TableSpoon.init(this, attrs, R.styleable.CustomView)
  }
}
```

### Initializing
`Tablespoon.init()` can be called using all or some of the view constructor parameters.
Additionaly, you can also supply a lambda to it which will be called on the `TypedArray` before recycling it in case some custom operations need to be performed.

```kotlin
class CustomView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

  var fullName: String? = null

  init {
    TableSpoon.init(this, attrs, R.styleable.CustomView, defStyleAttr, defStyleRes) {
      // this block is called using: TypedArray.() -> Unit

      fullName = getString(R.styleable.CustomView_firstName) +
        getString(R.styleable.CustomView_lastName)

      // ... any other operation on TypedArray before it's recycled
    }
  }
}
```


### Dynamic Properties
These are properties that will automatically redraw the view when updated.
```kotlin
@delegate:ColorAttr(R.styleable.CustomView_bgColor)
var bgColor: Int by dynamicIntAttr(Color.RED) // RED is default value

fun makeBgGreen() {
  // updating bgColor here will automatically update the view by
  // calling the view's requestLayout and invalidate methods
  bgColor = Color.GREEN
}
```
> **Note:** When using dynamic properties with annotations, the prefix `@delegate:` must be used before the annotation so that the delegate can be targeted.

- For any property that is not annotated, you can simply use `dynamicAttr()`.

#### Additional parameters
- If you don't want **both** `invalidate()` and `requestLayout()` to be called, you can set either of them to false.
- You can also supply a lambda which will be called before updating the view (acts like an `Observable` delegate).
```kotlin
var stringAttr: String by dynamicStringAttr(
  initialValue = "default string",
  invalidate = true, // default is true
  requestLayout = false // default is true
) { newString ->
  // this block is called before the view is updated
  // in case some pre-operations need to be performed
}

```

## Annotations and Dynamic Properties

Here is a list of all the possible annotations and dynamic property delegates that can be used for each attribute and field type
|XML Attr Type| Field Type | Annotation | Dynamic propery delegate |
|--|--|--|--|
|boolean|Boolean|@BooleanAttr|dynamicBooleanAttr()|
|color|Int|@ColorAttr|dynamicIntAttr()|
|color|ColorStateList|@ColorAttr|dynamicColorStateAttr()|
|dimension|Int|@DimensionAttr|dynamicIntAttr()|
|dimension|Float|@DimensionAttr|dynamicFloatAttr()|
|enum|Int|@IntAttr|dynamicIntAttr()|
|flags|Int|@IntAttr|dynamicIntAttr()|
|float|Float|@FloatAttr|dynamicFloatAttr()|
|integer|Int|@IntAttr|dynamicIntAttr()|
|reference|Int|@ResourceIdAttr|dynamicIntAttr()|
|reference|Drawable|@DrawableAttr|dynamicDrawableAttr()|
|string|String|@StringAttr|dynamicStringAttr()|

#### DrawableAttr
`@DrawableAttr` is a convenience annotation for directly getting a `Drawable` from a reference/resource ID.
`dynamicDrawableAttr()` is the corresponding dynamic property delegate.


## Contributing

Pull requests are welcome! Feel free to browse through open issues to look for things that need work. If you have a feature request or bug, please open a new issue so we can track it.


## License


```
Copyright 2020 Nikhil Panju.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
