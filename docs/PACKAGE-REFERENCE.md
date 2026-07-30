# Package reference

All application code lives under `org.canopydb` (`src/main/java/org/canopydb`). Resources live in `src/main/resources`.

---

## `org.canopydb`

| Class | Role |
| --- | --- |
| `Launch` | Gradle entry point; delegates to `Main.main` |
| `Main` | JavaFX `Application`; creates `Renderer`, shuts down `ThreadPool` on stop |

---

## `org.canopydb.config`

Runtime infrastructure.

| Class | Role |
| --- | --- |
| `DatabasePool` | Static HikariCP lifecycle: `connect`, `testConnection`, `getConnection`, `disconnect`. JDBC URL `jdbc:mysql://host:port/` |
| `ThreadPool` | Shared fixed executor (4 threads) for async DB work |
| `AppLogger` | JUL setup under `org.canopydb`; level via `canopy.log.level` |
| `Profiler` | `logMemory` — used-heap MB logging |

---

## `org.canopydb.controllers`

Glue between UI events and services. Always marshal UI updates onto the FX thread.

| Class | Role |
| --- | --- |
| `TreeViewEventController` | DB/table expand handlers, double-click open, LOADING/FAILED placeholders, notifies tree search after data changes |
| `TableViewEventController` | `tableReRender` — reload session data/count after filter, sort, or page change |

---

## `org.canopydb.models`

| Class | Role |
| --- | --- |
| `ConnectionMeta` | Saved connection fields + UUID; Jackson-friendly |
| `ConnectionLabel` | LOCAL / DEV / PROD / TESTING + CSS style class helpers |
| `CellValue` | `ofNull()` / `of(text)`; `isNull`, `getText`, `toDisplayString()` |
| `ColumnMeta` | Column name + JDBC type / type name |
| `TableData` | Column list + row list; `getHeaders()` |
| `TableSession` | Open-tab state: names, data, query, total count; pagination / order / filter helpers; `emitQuery` / `emitCountQuery` |
| `TablePagination` | Record `(limit, offset, totalRows)` |

---

## `org.canopydb.queries`

| Class | Role |
| --- | --- |
| `TableQuery` | Builds SELECT * and COUNT SQL with WHERE map, ORDER BY, LIMIT 300, OFFSET |
| `Order` | Column name + `OrderDirection` (ASC / DESC) |

---

## `org.canopydb.repository`

| Class | Role |
| --- | --- |
| `MetadataDAO` | `SHOW DATABASES`; table list from `information_schema` |
| `TableActionDAO` | Execute browse SQL / count; build `TableData` via serializer |
| `ResultSetValueSerializer` | JDBC type → `CellValue` (temporals as strings, binary summarized, null-aware) |

---

## `org.canopydb.services`

| Class | Role |
| --- | --- |
| `ConnectionMetadataService` | Async database / table name loads |
| `TableActionService` | Async initial table open and session reload (data + count) |

---

## `org.canopydb.utils`

| Class | Role |
| --- | --- |
| `Constants` | LOADING / FAILED / SUCCESS; APPLY / APPLIED filter labels |
| `ExceptionMessages` | `userMessage(Throwable)` — unwrap for user-facing text |
| `TableUtilities` | `tablePath(database, table)` → `"database : table"` (tab identity) |

---

## `org.canopydb.ui`

| Class | Role |
| --- | --- |
| `Renderer` | Builds root `StackPane`, seeds `ConnectionView`, loads `/style.css` |

---

## `org.canopydb.ui.interfaces`

| Type | Role |
| --- | --- |
| `View` | `Parent getView()` |
| `TableOpenAction` | Open a new table session / tab |
| `TableUpdateAction` | Refresh an existing session’s UI |
| `TableActiveCheck` | Focus existing tab if already open; return whether active |
| `PushNotification` | Functional toast hook (available; most call sites use `NotificationManager` directly) |

---

## `org.canopydb.ui.singletons`

| Class | Role |
| --- | --- |
| `ViewManager` | Static view stack (`pushView` / `popView` / `replaceView`) |
| `NotificationManager` | Toast overlay + detail modal; INFO / SUCCESS / DANGER |

---

## `org.canopydb.ui.views`

| Class | Role |
| --- | --- |
| `ConnectionView` | SplitPane: manager \| form area |
| `WorkspaceView` | BorderPane: sidebar \| workspace; wires open/active callbacks |

---

## `org.canopydb.ui.organisms.connections`

| Class | Role |
| --- | --- |
| `ConnectionManager` | Sidebar list, search, cards, JSON load/save under `~/.canopydb/` |
| `ConnectionFormArea` | Welcome vs form; Test / Save / Connect orchestration |
| `ConnectionForm` | Fields, dirty tracking, password copy/cut blocked, name length limit |

---

## `org.canopydb.ui.organisms.workspace`

| Class | Role |
| --- | --- |
| `Sidebar` | Tree shell + controller wiring + search host |
| `ConnectionTreeSearch` | Debounced filter over loaded tree; projected tree while searching |
| `Workspace` | Tab pane of sessions; add / select / update |
| `TableTab` | Filter area + table + footer; sort policy → server re-query |

---

## `org.canopydb.ui.molecules`

| Class | Role |
| --- | --- |
| `ConnectionCard` | Connection list item with environment label |
| `TabFilterArea` | Filter toolbar + up to 10 `FilterBox` rows |
| `TableTabFooter` | Pagination chrome around `PaginationControls` |

---

## `org.canopydb.ui.atoms`

| Class | Role |
| --- | --- |
| `TextInput` | Styled text field used in forms and search |
| `FilterBox` | Single filter row (SQL snippet + Apply / clear) |
| `PaginationControls` | Prev / next + row range display |

---

## `org.canopydb.ui.utils`

| Class | Role |
| --- | --- |
| `TableComponent` | Build / update `TableView`; cell & header context menus (copy) |
| `ClipboardUtil` | Put string on system clipboard |
| `TreeViewComponent` | Tree helpers (e.g. detect table-depth nodes) |
| `TreeSearch` | Case-insensitive `contains` match over loaded DB/table nodes |

---

## Resources

| Path | Role |
| --- | --- |
| `src/main/resources/style.css` | App-wide dark theme |
| `src/main/resources/assets/logo.png` | Connection welcome / branding |

---

## Persistence

| Path | Contents |
| --- | --- |
| `~/.canopydb/connections.json` | Array of `ConnectionMeta` (includes passwords in plaintext) |

No other config files are written under `~/.canopydb` today.
