package zoom

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Zoom(val name: String = "")

interface Getter<A, B> : (A) -> B {
    val get: (A) -> B
    override fun invoke(a: A): B = get(a)
}

interface Setter<A, B> { val set: (A, B) -> A }

class Prism<A, B>(
    val get: (A) -> B?,
    val set: (B) -> A)

interface Lens<A, B> : Getter<A, B>, Setter<A, B> {
    override val get: (A) -> B
    override val set: (A, B) -> A
}

interface Optional<A, B> : Getter<A, B?>, Setter<A, B> {
    override val get: (A) -> B?
    override val set: (A, B) -> A
}

fun <A, B, C> Getter<A, B>.map(f: (B) -> C) = object : Getter<A, C> {
    override val get: (A) -> C = { f(this@map.get(it)) }
}

fun <A, B> lens(getter: (A) -> B, setter: (A, B) -> A) = object: Lens<A, B> {
    override val get = getter
    override val set = setter
}

fun <A, B> optional(getter: (A) -> B?, setter: (A, B) -> A) = object: Optional<A, B> {
    override val get = getter
    override val set = setter
}

fun <A, B> nullableOptional(getter: (A) -> B?, setter: (A, B?) -> A) = object: Optional<A, B?> {
    override val get = getter
    override val set = setter
}

fun <A, B, C> Lens<A, B>.compose(other: Lens<B, C>) = object: Lens<A, C> {
    override val get = { a: A       -> other.get(this@compose.get(a)) }
    override val set = { a: A, c: C -> this@compose.set(a, other.set(this@compose.get(a), c)) }
}

fun <A, B, C> Lens<A, B>.compose(other: Prism<B, C>) = object: Optional<A, C> {
    override val get = { a: A       -> other.get(this@compose.get(a)) }
    override val set = { a: A, c: C -> this@compose.set(a, other.set(c)) }
}

fun <A, B, C> Lens<A, B>.compose(other: Optional<B, C>) = object: Optional<A, C> {
    override val get = { a: A       -> other.get(this@compose.get(a)) }
    override val set = { a: A, c: C -> this@compose.set(a, other.set(this@compose.get(a), c)) }
}

fun <A, B, C> Optional<A, B>.compose(other: Lens<B, C>) = object: Optional<A, C> {
    override val get = { a: A       -> this@compose.get(a)?.let { other.get(it) } }
    override val set = { a: A, c: C -> this@compose.get(a)?.let { this@compose.set(a, other.set(it, c)) } ?: a }
}

fun <A, B, C> Optional<A, B>.compose(other: Optional<B, C>) = object: Optional<A, C> {
    override val get = { a: A       -> this@compose.get(a)?.let { other.get(it) } }
    override val set = { a: A, c: C -> this@compose.get(a)?.let { this@compose.set(a, other.set(it, c)) } ?: a }
}

fun <A, B, C> Optional<A, B>.compose(other: Prism<B, C>) = object: Optional<A, C> {
    override val get = { a: A       -> this@compose.get(a)?.let { other.get(it) } }
    override val set = { a: A, c: C -> this@compose.get(a)?.let { this@compose.set(a, other.set(c)) } ?: a }
}

fun <A, K, V> Lens<A, Map<K, V>>.at(key: K) = compose(object: Optional<Map<K, V>, V> {
    override val get = { map: Map<K, V>            -> map[key] }
    override val set = { map: Map<K, V>, value: V? -> if(value == null) map - key else map + (key to value) }
})

fun <A, K, V> Optional<A, Map<K, V>>.at(key: K) = compose(object: Optional<Map<K, V>, V> {
    override val get = { map: Map<K, V>            -> map[key] }
    override val set = { map: Map<K, V>, value: V? -> if(value == null) map - key else map + (key to value) }
})

fun <A, V> Lens<A, List<V>>.index(index: Int) = compose(object: Optional<List<V>, V> {
    override val get = { list: List<V>           -> list.getOrNull(index) }
    override val set = { list: List<V>, value: V -> list.mapIndexed { i, v -> if (i == index) value else v } }
})

fun <A, V> Lens<A, List<V>>.first(predicate: (V) -> Boolean) = compose(object: Optional<List<V>, V> {
    override val get = { list: List<V>            -> list.firstOrNull(predicate) }
    override val set = { list: List<V>, value: V? -> if(value != null )
                                                        list.indexOfFirst(predicate)
                                                        .takeIf { it != -1 }
                                                        ?.let { index -> list.mapIndexed { i, v -> if (i == index) value else v } } ?: list
                                                     else
                                                list.indexOfFirst(predicate)
                                                        .takeIf { it != -1 }
                                                        ?.let { index -> list.filterIndexed { i, _ -> i != index } } ?: list
                                                    }
})

fun <A, V> Optional<A, List<V>>.first(predicate: (V) -> Boolean) = compose(object: Optional<List<V>, V> {
    override val get = { list: List<V>            -> list.firstOrNull(predicate) }
    override val set = { list: List<V>, value: V? -> if(value != null )
        list.indexOfFirst(predicate)
            .takeIf { it != -1 }
            ?.let { index -> list.mapIndexed { i, v -> if (i == index) value else v } } ?: list
    else
        list.indexOfFirst(predicate)
            .takeIf { it != -1 }
            ?.let { index -> list.filterIndexed { i, _ -> i != index } } ?: list
    }
    override fun toString() = "[$?]"
})

fun <A, B> Lens<A, B>.optional() = object: Optional<A, B> {
    override val get: (A) -> B?   = this@optional.get
    override val set: (A, B) -> A = this@optional.set
}

fun <A, B> Lens<A, B>    .modify(a: A, f: B.() -> B): A = set(a, f(get(a)))
fun <A, B> Optional<A, B>.modify(a: A, f: B.() -> B): A = get(a)?.let{ set(a, f(it)) } ?: a

infix fun <A, B> Lens<A, B>     .modify(update: B.() -> B): (A) -> A = { modify(it, update) }
infix fun <A, B> Optional<A, B> .modify(update: B.() -> B): (A) -> A = { modify(it, update) }

@JvmName("factory")
operator fun <A, B> Setter<A, B>.rem(value: A): (B) -> A = { b -> this.set(value, b) }
operator fun <A, B> Setter<A, B>.rem(value: B): (A) -> A = { a -> this.set(a, value) }