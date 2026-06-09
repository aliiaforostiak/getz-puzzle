# Concurrent Puzzle Solver

Конкурентная реализация решателя головоломки в духе примера из книги Брайана Гетца *Java Concurrency in Practice*.

Проект демонстрирует:
- параллельный поиск решения через `ExecutorService`;
- защиту от повторного обхода состояний;
- восстановление пути решения через цепочку узлов;
- отдельный контракт `Puzzle<P, M>` для переиспользования решателя с другими головоломками.

## Стек

- Java 21
- Maven
- JUnit 5

## Запуск

Сборка и тесты:

```bash
mvn test
```

Сборка без тестов:

```bash
mvn compile
```

Запуск примера после сборки:

```bash
java -cp target/classes org.example.Main
```

В IDE можно запускать `org.example.Main` напрямую.

## Что делает приложение

Точка входа `org.example.Main` создаёт стартовую позицию 8-puzzle, запускает `ConcurrentPuzzleSolver` и печатает найденный путь.

По умолчанию:
- поиск ограничен 15 секундами;
- при успешном решении возвращается список ходов;
- при таймауте поиск останавливается и выбрасывается исключение.

## Структура проекта

- `src/main/java/org/example/Main.java` - демонстрационный запуск.
- `src/main/java/org/example/puzzle/ConcurrentPuzzleSolver.java` - конкурентный поиск решения.
- `src/main/java/org/example/puzzle/SlidingPuzzle.java` - правила 8-puzzle.
- `src/main/java/org/example/puzzle/BoardPosition.java` - неизменяемое состояние доски.
- `src/main/java/org/example/puzzle/Direction.java` - возможные ходы.
- `src/main/java/org/example/puzzle/Puzzle.java` - контракт головоломки.
- `src/main/java/org/example/puzzle/PuzzleNode.java` - узел дерева поиска.
- `src/main/java/org/example/puzzle/ValueLatch.java` - однократная публикация результата.
- `src/test/java/org/example/puzzle/*Test.java` - тесты на критичные сценарии.

## Документация

- `docs/concurrent-puzzle-solver.html` - обзор для аналитика.
- `docs/developer-guide.html` - подробная инструкция для разработчика.

## Тесты

Тесты покрывают:
- корректность `BoardPosition`;
- правила `SlidingPuzzle`;
- поведение `ValueLatch` с ожиданием и таймаутом;
- happy path и timeout/cycle сценарии `ConcurrentPuzzleSolver`.

## Коммит и версия

Репозиторий уже инициализирован и связан с GitHub:

`https://github.com/aliiaforostiak/getz-puzlze`

## Примечание

Если хочешь, я могу добавить сюда:
- пример входной позиции и ожидаемого вывода;
- краткую UML-схему;
- раздел "Как расширить на другую головоломку".
