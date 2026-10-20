/** Тот же набор ключей, что в русском словаре, с любыми строками — так typecheck ловит пропуски в переводе. */
export type Dictionary<T> = { [K in keyof T]: T[K] extends string ? string : Dictionary<T[K]> }
