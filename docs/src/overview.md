# Overview

**Mina** is a functional programming language for the Java Virtual Machine.

It is a strictly-evaluated, statically-typed language.

## Quickstart

### Syntax

The keywords `namespace`, `import`, `as`, `data`, `let`, `fun`, `return`, `if`, `then`, `else`, `match`, `with` and `case` are reserved.

`(` and `)` are delimiters for values. They are used for value parameter lists, and for applying values to functions.

`[` and `]` are delimiters for types. They are used for type parameter lists, and for applying types to type constructors.

`{` and `}` are delimiters for lexical scopes. They are used for blocks, imported symbols and constructor fields in pattern matching.

### Namespaces

**Mina** code is organised into `namespace`s:

```
namespace Mina/Example {}
```

**namespaces** are nothing more than containers for declarations.

### Declarations

`let` bindings can be used to declare values:

```
namespace Mina/Example {
   let one = 1
}
```

`data` declarations can be used to declare types:

```
namespace Mina/Example {
   data Choice {
      case Yes()
      case No()
   }
}
```

### Expressions

#### Literals
We can use literals to declare the [primitive types](https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html) of the Java virtual machine:

```
let boolean    = true  // Boolean
let int        = 1     // Int
let intSuff    = 1i    // Int with optional suffix
let long       = 1l    // Long
let double     = 1.1   // Double
let doubleSuff = 1.1d  // Double with optional suffix
let float      = 1.1f  // Float
let char       = 'a'   // Char
let string     = "abc" // String
```

#### Functions
We can declare functions using `fun` to introduce the function and `->` to separate the parameter list from the body of the function:

```
let id    = fun a -> a
let const = fun (a, b) -> a
```

### Application
We can call functions by supplying arguments within parentheses:

```
let id    = fun a -> a
let one   = id(1)
let const = fun (a, b) -> a
let two   = const(2, 1)
```

#### Selection
We can call any function with the preceding expression as the first argument using `.`:

```
let id    = fun a -> a
let one   = 1.id()
```

Any remaining arguments must be supplied to the function within parentheses as usual:

```
let const = fun (a, b) -> a
let two   = 2.const(1)
```

#### Choice
We can choose between expressions using `if`:

```
let choice = if true then 1 else 0
```

The `else` expression is mandatory, because `if` expressions produce a value.

#### Blocks

Local variables can declared using block expressions:

```
{
   let local = 1
   return local
}
```

A block can produce a value using `return`. If no value is returned the block produces the singleton `Unit` value.

If there are no local `let` bindings in the block, then `return` is optional:

```
let outer = { 1 }
```
