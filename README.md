# Vivid

**Beautiful terminal output for Java.**

Vivid is a modern, lightweight library for rich terminal output — colored and styled text,
tables, panels, progress bars, spinners and live updates — behind a small, fluent API.
It is inspired by Python's [Rich](https://github.com/Textualize/rich) and designed for Java 17+
with **zero runtime dependencies**.

> **Status: pre-alpha.** Styled text, markup, panels, tables, terminal detection and color downgrade
> work. Progress bars, spinners and live display exist as API only and throw
> `UnsupportedOperationException`. The API will change before 1.0.

---

## Vision

Command-line tools written in Java deserve the same polish as those written in Python, Go or Rust.
Today that means hand-rolling ANSI escape codes or pulling in a heavyweight TUI framework. Vivid
aims for the middle ground:

- **Delightful by default.** Sensible styles, rounded borders and smooth animations out of the box.
  One line of code should produce output you are happy to show people.
- **Fluent and discoverable.** Builders and withers that read like the output they produce, so
  your IDE's autocomplete is the documentation.
- **Immutable and composable.** Everything visual is a `Renderable` value. Put a table in a panel,
  a progress bar in a table, or your own component anywhere.
- **Zero dependencies.** Just the JDK. Safe to add to any CLI, build plugin or server log.
- **Honest about terminals.** Detect color support, width and interactivity, and degrade gracefully
  — plain text when piped to a file, 16 colors on old terminals, `NO_COLOR` respected.

## Planned features

| Area              | Feature                                                            | Status  |
|-------------------|--------------------------------------------------------------------|---------|
| **Styling**       | 16 / 256 / true-color, bold, italic, underline, …                  | ✅       |
|                   | Style parsing: `"bold white on red"`                               | ✅       |
|                   | Automatic color downgrade by terminal capability                   | ✅       |
| **Text**          | Immutable styled `Text` with spans                                 | ✅       |
|                   | Inline markup: `[bold red]Error[/]`                                | ✅       |
|                   | Word wrapping, justification, wide-character (CJK / emoji) support | Planned |
| **Tables**        | Columns, rows, titles, alignment, width constraints                | ✅       |
|                   | Auto column sizing, box styles                                     | ✅       |
|                   | Cell wrapping                                                      | Planned |
| **Panels**        | Boxes with titles, subtitles and padding                           | ✅       |
| **Progress**      | Progress bars, multi-task live progress, `track(iterable)`         | API ✅   |
|                   | Spinners and `status("Working…")`                                  | API ✅   |
| **Live**          | In-place redrawing of any renderable                               | API ✅   |
| **Console**       | Width / color / TTY detection, `NO_COLOR`, `FORCE_COLOR`, printing | ✅       |
|                   | Rules, status messages                                             | API ✅   |
| **Later**         | Trees, columns layout, syntax highlighting, Markdown, logging      | Ideas   |

"✅" means implemented. "API ✅" means the public types and signatures exist; the behavior is not yet implemented.

## A taste of the API

This is the API Vivid is working towards. It all compiles today; styled text, markup, tables and
panels already print, progress bars, spinners and live display don't draw anything yet.

### Styled text and markup

```java
import io.github.dimazelinskyi.vivid.Vivid;

Vivid.println("[bold magenta]Vivid[/] makes your terminal [italic green]beautiful[/]!");

Vivid.success("Build finished in 4.2s");
Vivid.warning("3 deprecated APIs in use");
Vivid.error("Could not connect to [underline]db.internal:5432[/]");
```

Or build styles programmatically:

```java
import io.github.dimazelinskyi.vivid.style.Color;
import io.github.dimazelinskyi.vivid.style.Style;
import io.github.dimazelinskyi.vivid.text.Text;

Style ok = Style.builder().color(Color.GREEN).bold().build();

Text status = Text.of("Status: ").append("OK", ok);
Vivid.println(status);
```

### Tables

```java
import io.github.dimazelinskyi.vivid.table.Column;

var table = Vivid.table("Library", "Language")
        .column(Column.of("Stars").right())
        .title("Terminal libraries")
        .row("Rich", "Python", "[yellow]★ 50k[/]")
        .row("Vivid", "Java", "[dim]coming soon[/]")
        .build();

Vivid.println(table);
```

```
         Terminal libraries
╭─────────┬──────────┬─────────────╮
│ Library │ Language │       Stars │
├─────────┼──────────┼─────────────┤
│ Rich    │ Python   │       ★ 50k │
│ Vivid   │ Java     │ coming soon │
╰─────────┴──────────┴─────────────╯
```

### Panels

```java
Vivid.println(Vivid.panel("Deployed [bold]v1.4.2[/] to production")
        .title("Release")
        .subtitle("2 minutes ago")
        .padding(1, 2)
        .build());
```

### Progress bars

```java
try (var progress = Vivid.progress().start()) {
    var download = progress.addTask("Downloading", 1_024);
    var extract  = progress.addTask("Extracting", 300);

    while (!download.isFinished()) {
        download.advance(readChunk());
    }
    // ...
}

// Or simply wrap any Iterable:
for (var file : Vivid.track(files, "Uploading")) {
    upload(file);
}
```

### Spinners and live updates

```java
try (var status = Vivid.status("Connecting to database...")) {
    connect();
    status.update("Running migrations...");
    migrate();
}

try (var live = Vivid.live(dashboard())) {
    while (running) {
        live.update(dashboard());
        Thread.sleep(250);
    }
}
```

### Custom components

Anything that implements `Renderable` can be printed, nested in tables and panels, or shown live:

```java
Renderable divider = context -> List.of("·".repeat(context.maxWidth()));
Vivid.println(divider);
```

## Project layout

```
src/main/java/io/github/dimazelinskyi/vivid/
├── Vivid.java           Static facade — the one import you need
├── console/             Console (output + terminal detection), Live
├── style/               Color, Attribute, Style, StyleBuilder
├── text/                Text (immutable styled text), Markup
├── table/               Table, Column, Row
├── panel/               Panel
├── progress/            ProgressBar, Progress, Spinner, Status
└── render/              Renderable, BoxStyle, Justify
```

## Building

Requirements: **JDK 17 or newer**. Maven is provided through the wrapper.

```bash
./mvnw verify
```

Compiles against Java 17 (`--release 17`) and runs the JUnit 5 test suite.

To build everything that goes to Maven Central — sources jar, javadoc jar and GPG signatures —
without publishing:

```bash
./mvnw -P release verify -Dgpg.skip
```

### Releasing to Maven Central

Publishing goes through the [Central Portal](https://central.sonatype.com) using the `release` profile.
One-time setup:

1. Sign in to the Central Portal with GitHub — the `io.github.dimazelinskyi` namespace is verified automatically.
2. Generate a Portal user token and add it to `~/.m2/settings.xml` under server id `central`.
3. Have a GPG key available for signing, with its public key published to a keyserver.

Then:

```bash
./mvnw -P release deploy
```

The upload is staged for review (`autoPublish` is `false`); publish it from the Portal UI.

## Contributing

The project is at the design stage, so feedback on the API is the most valuable contribution right now.
Open an issue with the code you *wish* you could write.

## License

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
