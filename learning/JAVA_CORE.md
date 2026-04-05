# Java Core — Complete Interview Guide

---

## 1. OOP Fundamentals

**Four Pillars:**
- **Encapsulation** — Bundling data + methods, access via getters/setters. Use `private` fields.
- **Inheritance** — `extends` for classes, `implements` for interfaces. Java = single inheritance only.
- **Polymorphism** — Compile-time (method overloading) vs Runtime (method overriding).
- **Abstraction** — Abstract classes (partial impl) vs Interfaces (contract only, default methods since Java 8).

**Key Concepts:**
- `this` vs `super` keyword
- Constructor chaining
- `instanceof` operator
- Upcasting vs Downcasting
- Covariant return types

**Interview Favorites:**
- Why Java doesn't support multiple inheritance? (Diamond problem)
- Can we override static methods? (No — method hiding)
- Abstract class vs Interface — when to use which?

---

## 2. Access Modifiers & Keywords

| Modifier | Class | Package | Subclass | World |
|----------|-------|---------|----------|-------|
| public | ✅ | ✅ | ✅ | ✅ |
| protected | ✅ | ✅ | ✅ | ❌ |
| default | ✅ | ✅ | ❌ | ❌ |
| private | ✅ | ❌ | ❌ | ❌ |

**`static` keyword:**
- Static variables — shared across all instances (class level)
- Static methods — can't access instance members, called via ClassName
- Static blocks — executed once when class is loaded
- Static inner classes — don't hold reference to outer class

**`final` keyword:**
- final variable → constant (must be initialized)
- final method → can't be overridden
- final class → can't be extended (e.g., String, Integer)

**`volatile`** — Ensures visibility across threads, no caching in CPU registers
**`transient`** — Excluded from serialization
**`synchronized`** — Thread-safe access to method/block

---

## 3. Strings

**String Pool & Immutability:**
```java
String s1 = "hello";        // Pool
String s2 = "hello";        // Same reference from pool
String s3 = new String("hello"); // Heap (new object)

s1 == s2;       // true (same pool reference)
s1 == s3;       // false (different objects)
s1.equals(s3);  // true (content comparison)
```

**StringBuilder vs StringBuffer:**
- StringBuilder — not thread-safe, faster (use in single-threaded)
- StringBuffer — thread-safe (synchronized), slower

**Important Methods:** charAt(), substring(), indexOf(), split(), trim(), strip(), replace(), matches(), compareTo()

**Interview:** Why is String immutable? → Security (used in classloading, networking), caching (hashcode), thread safety, string pool optimization.

---

## 4. Exception Handling

**Hierarchy:**
```
Throwable
├── Error (OutOfMemoryError, StackOverflowError) — Don't catch these
└── Exception
    ├── Checked (IOException, SQLException) — Must handle at compile time
    └── RuntimeException (Unchecked)
        ├── NullPointerException
        ├── ArrayIndexOutOfBoundsException
        ├── IllegalArgumentException
        └── ClassCastException
```

**Key Rules:**
- `finally` always executes (except System.exit())
- try-with-resources (Java 7+) — auto-closes `AutoCloseable` resources
- Multi-catch: `catch (IOException | SQLException e)`
- Custom exceptions: extend Exception (checked) or RuntimeException (unchecked)

```java
// Try-with-resources
try (BufferedReader br = new BufferedReader(new FileReader("file.txt"))) {
    return br.readLine();
} // br.close() called automatically
```

---

## 5. Collections Framework

**Hierarchy:**
```
Iterable
└── Collection
    ├── List (ordered, duplicates allowed)
    │   ├── ArrayList — O(1) random access, O(n) insert/delete middle
    │   ├── LinkedList — O(n) access, O(1) insert/delete at known position
    │   └── Vector — synchronized ArrayList (legacy)
    ├── Set (no duplicates)
    │   ├── HashSet — O(1) add/remove/contains, no order
    │   ├── LinkedHashSet — insertion order maintained
    │   └── TreeSet — sorted, O(log n) operations
    └── Queue
        ├── PriorityQueue — min-heap by default
        ├── ArrayDeque — double-ended queue (faster than Stack/LinkedList)
        └── LinkedList — implements both List and Deque

Map (separate hierarchy)
├── HashMap — O(1) avg, no order, allows 1 null key
├── LinkedHashMap — insertion order
├── TreeMap — sorted by keys, O(log n)
├── ConcurrentHashMap — thread-safe, no null keys/values
└── Hashtable — synchronized (legacy), no nulls
```

**HashMap Internals (MUST KNOW):**
- Array of buckets (Node<K,V>[])
- hash(key) → bucket index
- Collision handling: LinkedList → Red-Black Tree (when bucket size > 8, Java 8+)
- Load factor: 0.75 (default), resize at 75% capacity
- Initial capacity: 16, doubles on resize
- equals() and hashCode() contract — if two objects are equal, they MUST have same hashCode

**Comparable vs Comparator:**
```java
// Comparable — natural ordering (implement in the class itself)
class Employee implements Comparable<Employee> {
    public int compareTo(Employee other) {
        return this.name.compareTo(other.name);
    }
}

// Comparator — custom ordering (external)
employees.sort(Comparator.comparing(Employee::getSalary).reversed());
```

---

## 6. Generics

```java
// Generic class
public class Box<T> {
    private T value;
    public T getValue() { return value; }
}

// Bounded type
public <T extends Comparable<T>> T findMax(List<T> list) { ... }

// Wildcards
List<?> anything;                    // Unknown type
List<? extends Number> numbers;      // Number or subclass (read-only)
List<? super Integer> integers;      // Integer or superclass (write-only)
```

**Type Erasure:** Generics are compile-time only. At runtime, `List<String>` becomes `List<Object>`.

---

## 7. Java 8+ Features

**Lambda Expressions:**
```java
// Before
Comparator<String> comp = new Comparator<String>() {
    public int compare(String a, String b) { return a.compareTo(b); }
};

// After
Comparator<String> comp = (a, b) -> a.compareTo(b);
```

**Functional Interfaces:**
| Interface | Method | Use |
|-----------|--------|-----|
| Predicate\<T\> | test(T) → boolean | Filtering |
| Function\<T,R\> | apply(T) → R | Transformation |
| Consumer\<T\> | accept(T) → void | Side effects |
| Supplier\<T\> | get() → T | Factory/lazy eval |
| BiFunction\<T,U,R\> | apply(T,U) → R | Two-arg transform |

**Streams API:**
```java
List<String> result = employees.stream()
    .filter(e -> e.getSalary() > 50000)          // Predicate
    .map(Employee::getName)                       // Function
    .sorted()                                     // Natural order
    .distinct()                                   // Remove duplicates
    .collect(Collectors.toList());                // Terminal operation

// Grouping
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));

// Reducing
int totalSalary = employees.stream()
    .mapToInt(Employee::getSalary)
    .sum();
```

**Optional:**
```java
Optional<String> name = Optional.ofNullable(getName());
String result = name.orElse("Unknown");
String result2 = name.orElseThrow(() -> new RuntimeException("Not found"));
name.ifPresent(System.out::println);
```

---

## 8. Multithreading & Concurrency

**Creating Threads:**
```java
// 1. Extend Thread
class MyThread extends Thread { public void run() { ... } }

// 2. Implement Runnable (preferred)
Runnable task = () -> System.out.println("Running");
new Thread(task).start();

// 3. Callable + Future (returns result)
Callable<Integer> callable = () -> 42;
Future<Integer> future = executor.submit(callable);
int result = future.get(); // Blocks until done
```

**Thread Lifecycle:** NEW → RUNNABLE → RUNNING → BLOCKED/WAITING/TIMED_WAITING → TERMINATED

**Synchronization:**
```java
// synchronized method
public synchronized void increment() { count++; }

// synchronized block (finer control)
synchronized(lockObject) { count++; }

// ReentrantLock
Lock lock = new ReentrantLock();
lock.lock();
try { count++; } finally { lock.unlock(); }
```

**ExecutorService:**
```java
ExecutorService executor = Executors.newFixedThreadPool(4);
executor.submit(() -> doWork());
executor.shutdown();
```

**CompletableFuture (Java 8+):**
```java
CompletableFuture.supplyAsync(() -> fetchData())
    .thenApply(data -> process(data))
    .thenAccept(result -> save(result))
    .exceptionally(ex -> { log(ex); return null; });
```

**Key Concepts:** Deadlock, Livelock, Starvation, Race Condition, volatile, AtomicInteger, CountDownLatch, CyclicBarrier, Semaphore

---

## 9. JVM Internals

**Memory Model:**
```
JVM Memory
├── Heap (shared across threads)
│   ├── Young Generation (Eden + Survivor S0, S1)
│   └── Old Generation (Tenured)
├── Stack (per thread) — local variables, method calls
├── Method Area / Metaspace — class metadata, static variables
├── PC Register (per thread)
└── Native Method Stack
```

**Garbage Collection:**
- Minor GC — cleans Young Gen (fast, frequent)
- Major GC — cleans Old Gen (slow, infrequent)
- GC Algorithms: Serial, Parallel, CMS, G1 (default Java 9+), ZGC (Java 11+)
- Objects: Eden → Survivor → Old Gen (after surviving multiple GC cycles)

**ClassLoader:** Bootstrap → Extension → Application → Custom

---

## 10. Design Patterns (Top Interview Picks)

**Singleton:**
```java
public class Singleton {
    private static volatile Singleton instance;
    private Singleton() {}
    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) instance = new Singleton();
            }
        }
        return instance;
    }
}
```

**Factory, Builder, Observer, Strategy, Decorator, Proxy, Template Method** — know when to use each.

---

## 11. Java 11-17+ Features

- **var** (Java 10) — local variable type inference
- **Records** (Java 14) — immutable data carriers: `record Point(int x, int y) {}`
- **Sealed Classes** (Java 17) — restrict which classes can extend: `sealed class Shape permits Circle, Square {}`
- **Pattern Matching** — `if (obj instanceof String s) { use(s); }`
- **Text Blocks** — multi-line strings with `"""`
- **Switch Expressions** — `int result = switch(day) { case MON -> 1; default -> 0; };`

---

## Quick Reference: Most Asked Java Interview Topics

1. HashMap internals + equals/hashCode contract
2. String immutability + String pool
3. Checked vs Unchecked exceptions
4. ArrayList vs LinkedList — when to use which
5. synchronized vs ReentrantLock vs volatile
6. Stream API operations + parallel streams pitfalls
7. Singleton pattern — thread-safe implementation
8. Java memory model — heap vs stack, GC generations
9. CompletableFuture — chaining async operations
10. Generics — type erasure, wildcards (PECS)

---

*Convert to PDF: `pandoc JAVA_CORE.md -o JAVA_CORE.pdf`*
