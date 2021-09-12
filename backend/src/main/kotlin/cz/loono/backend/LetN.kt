package cz.loono.backend

inline fun <T1, T2, R> let2(t1: T1?, t2: T2?, block: (T1, T2) -> R): R? =
    if (t1 == null || t2 == null) null else block(t1, t2)

inline fun <T1, T2, T3, R> let3(t1: T1?, t2: T2?, t3: T3?, block: (T1, T2, T3) -> R): R? =
    if (t1 == null || t2 == null || t3 == null) null else block(t1, t2, t3)
