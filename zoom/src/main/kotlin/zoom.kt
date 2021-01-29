package zoom


public interface Getter<A, B> : (A) -> B {
    public val get: (A) -> B
    override fun invoke(a: A): B = get(a)
}

public interface Setter<A, B> {
    public val set: (A, B) -> A
}

public class Lens<A, B>(
    override val get: (A)    -> B,
    override val set: (A, B) -> A) : Getter<A, B>, Setter<A, B>

public class Optional<A, B>(
    override val get: (A)    -> B?,
    override val set: (A, B) -> A) : Getter<A, B?>, Setter<A, B>

public class Prism<A, B>(
    public val get: (A) -> B?,
    public val set: (B) -> A)


// Lens composition

public fun <A, B, C> Lens<A, B>.compose(other: Lens<B, C>): Lens<A, C> = Lens(
    get = { a: A       -> other.get(get(a)) },
    set = { a: A, c: C -> set(a, other.set(get(a), c)) }
)

public fun <A, B, C> Lens<A, B>.compose(other: Optional<B, C>): Optional<A, C> = Optional(
    get = { a: A       -> other.get(get(a)) },
    set = { a: A, c: C -> set(a, other.set(get(a), c)) }
)

public fun <A, B, C> Lens<A, B>.compose(other: Prism<B, C>): Optional<A, C> = Optional(
    get = { a: A       -> other.get(get(a)) },
    set = { a: A, c: C -> set(a, other.set(c)) }
)

public fun <A, B, C> Lens<A, B?>.compose(other: Lens<B, C>): Optional<A, C> = Optional(
    get = { a: A       -> get(a)?.let (other.get) },
    set = { a: A, c: C -> get(a)?.let { set(a, other.set(it, c)) } ?: a }
)

// Optional composition

public fun <A, B, C> Optional<A, B>.compose(other: Lens<B, C>): Optional<A, C> = Optional(
    get = { a: A       -> get(a)?.let (other.get) },
    set = { a: A, c: C -> get(a)?.let { set(a, other.set(it, c)) } ?: a }
)

public fun <A, B, C> Optional<A, B>.compose(other: Optional<B, C>): Optional<A, C> = Optional(
    get = { a: A       -> get(a)?.let (other.get) },
    set = { a: A, c: C -> get(a)?.let { set(a, other.set(it, c)) } ?: a }
)

public fun <A, B, C> Optional<A, B>.compose(other: Prism<B, C>): Optional<A, C> = Optional(
    get = { a: A       -> get(a)?.let (other.get) },
    set = { a: A, c: C -> get(a)?.let { set(a, other.set(c)) } ?: a }
)


// Collections

public fun <A, K, V> Lens<A, Map<K, V>>.at(key: K): Optional<A, V> = compose(Optional(
    get = { map: Map<K, V>            -> map[key] },
    set = { map: Map<K, V>, value: V? -> if(value == null) map - key else map + (key to value) }
))

public fun <A, K, V> Optional<A, Map<K, V>>.at(key: K): Optional<A, V> = compose(Optional(
    get = { map: Map<K, V>            -> map[key] },
    set = { map: Map<K, V>, value: V? -> if(value == null) map - key else map + (key to value) }
))

public fun <A, V> Lens<A, List<V>>.index(index: Int): Optional<A, V> = compose(Optional(
    get = { list: List<V>           -> list.getOrNull(index) },
    set = { list: List<V>, value: V -> list.mapIndexed { i, v -> if (i == index) value else v } }
))

public fun <A, V> Lens<A, List<V>>.first(predicate: (V) -> Boolean): Optional<A, V> = compose(Optional(
    get = { list: List<V>            -> list.firstOrNull(predicate) },
    set = { list: List<V>, value: V? -> if(value != null )
                                            list.indexOfFirst(predicate)
                                            .takeIf { it != -1 }
                                            ?.let { index -> list.mapIndexed { i, v -> if (i == index) value else v } } ?: list
                                         else
                                    list.indexOfFirst(predicate)
                                            .takeIf { it != -1 }
                                            ?.let { index -> list.filterIndexed { i, _ -> i != index } } ?: list
                                        }
))

public fun <A, V> Optional<A, List<V>>.first(predicate: (V) -> Boolean): Optional<A, V> = compose(Optional(
    get = { list: List<V>            -> list.firstOrNull(predicate) },
    set = { list: List<V>, value: V? -> if(value != null )
        list.indexOfFirst(predicate)
            .takeIf { it != -1 }
            ?.let { index -> list.mapIndexed { i, v -> if (i == index) value else v } } ?: list
    else
        list.indexOfFirst(predicate)
            .takeIf { it != -1 }
            ?.let { index -> list.filterIndexed { i, _ -> i != index } } ?: list
    }
))




public fun <A, B> Lens<A, B>.optional(): Optional<A, B> = Optional(get, set)

public fun <A, B, C> Getter<A, B>.map(f: (B) -> C): Getter<A, C> = object : Getter<A, C> {
    override val get: (A) -> C = { f(this@map.get(it)) }
}

public fun <A, B> Lens<A, B>    .modify(a: A, f: B.() -> B): A = set(a, f(get(a)))
public fun <A, B> Optional<A, B>.modify(a: A, f: B.() -> B): A = get(a)?.let{ set(a, f(it)) } ?: a

public infix fun <A, B> Lens<A, B>     .modify(update: B.() -> B): (A) -> A = { modify(it, update) }
public infix fun <A, B> Optional<A, B> .modify(update: B.() -> B): (A) -> A = { modify(it, update) }

@JvmName("factory")
public operator fun <A, B> Setter<A, B>.rem(value: A): (B) -> A = { b -> this.set(value, b) }
public operator fun <A, B> Setter<A, B>.rem(value: B): (A) -> A = { a -> this.set(a, value) }



// Annotation processor utils

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class Zoom(val name: String = "")

public fun <A, B> lens(            getter: (A) -> B,  setter: (A, B) -> A)  : Lens<A, B>      = Lens(getter, setter)
public fun <A, B> optional(        getter: (A) -> B?, setter: (A, B) -> A)  : Optional<A, B>  = Optional(getter, setter)
public fun <A, B> nullableLens(    getter: (A) -> B?, setter: (A, B?) -> A) : Lens<A, B?>     = Lens(getter, setter)
public fun <A, B> nullableOptional(getter: (A) -> B?, setter: (A, B?) -> A) : Optional<A, B?> = Optional(getter, setter)