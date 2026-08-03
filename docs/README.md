# CanopyDB developer docs

Guides for contributing to the JavaFX MySQL client under `src/`.

| Doc | What it covers |
| --- | --- |
| [Architecture](ARCHITECTURE.md) | Layers, bootstrap, major flows, threading, persistence, styling |
| [Package reference](PACKAGE-REFERENCE.md) | Every package and class under `org.canopydb` |
| [Testing](TESTING.md) | How to think about tests, what to cover, how the suite is organized |

## Quick orientation

```
Launch → Main → Renderer → ConnectionView
                              ↓ Connect
                         WorkspaceView (Sidebar + Workspace tabs)
                              ↓ Double-click table
                         TableTab (filters / grid / pagination)
```

- **UI:** programmatic JavaFX (no FXML). Atomic design: atoms → molecules → organisms → views.
- **DB:** MySQL only today (`mysql-connector-j` + HikariCP). One active pool at a time.
- **Async:** JDBC on a fixed 4-thread pool; UI updates via `Platform.runLater`.
- **Data on disk:** `~/.canopydb/connections.json` (Jackson).

## Run

```bash
./gradlew run
```

Main class: `org.canopydb.Launch` (see root `build.gradle`).

## Where to change what

| Goal | Start here |
| --- | --- |
| Connection screen UX | `ui/organisms/connections/` |
| Sidebar / tree / search | `ui/organisms/workspace/Sidebar`, `ConnectionTreeSearch` |
| Open / expand tree nodes | `controllers/TreeViewEventController` |
| Table tab UI | `ui/organisms/workspace/TableTab` |
| Filter / sort / page reload | `controllers/TableViewEventController`, `queries/TableQuery` |
| Cell display / copy | `ui/utils/TableComponent`, `repository/ResultSetValueSerializer` |
| Toasts / error modals | `ui/singletons/NotificationManager` |
| Theme | `src/main/resources/styles/*.css` (loaded by `Renderer`) |
