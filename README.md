# freebuff1

A Paper Minecraft plugin.

## Requirements

- JDK 21+
- Maven 3.9+
- A Paper server (1.21.x)

## Build

```bash
mvn package
```

The plugin jar is written to `target/freebuff1-0.1.0.jar`.

## Install

1. Build the jar (or copy it from `target/`).
2. Drop it into your server's `plugins/` folder.
3. Restart the server.
4. Run `/freebuff1` in game.

## Project layout

```
pom.xml
src/main/java/com/example/freebuff1/Freebuff1Plugin.java
src/main/resources/plugin.yml
```

- `pom.xml` — Maven build, depends on `paper-api` with `provided` scope (the server ships
  the API at runtime, so it is not bundled into the jar).
- `plugin.yml` — plugin metadata, command and permission declarations.
- `Freebuff1Plugin.java` — main class, extends `JavaPlugin`.

## Development

- `mvn clean package` for a fresh build.
- Change the `name`, `group`/`package` and command names if you want a different plugin id.
- Bump `version` in `pom.xml` to release a new build; `plugin.yml` picks it up automatically.
