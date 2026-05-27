# Hoops Showdown

A 1v1 street-ball arcade game built with JavaFX. Pick a character, take the court, shoot from inside or outside the arc, time blocks on defense, and race your opponent to 11 points.

Made by Tommy Wang.

## Requirements

| Tool | Version |
| --- | --- |
| JDK | 17 or newer |
| Maven | 3.6+ |
| OS | macOS, Windows, or Linux |

JavaFX is installed through Maven dependencies, so you do not need to install the JavaFX SDK separately.

Check your tools:

```bash
java -version
mvn -version
```

## Quick Start

Run the game from the project root:

```bash
mvn javafx:run
```

Useful commands:

| Command | Purpose |
| --- | --- |
| `mvn javafx:run` | Compile and launch the JavaFX game |
| `mvn clean compile` | Clean and compile the source code |
| `mvn test` | Run the Maven test phase |
| `mvn javadoc:javadoc` | Generate JavaDoc HTML files |
| `open target/reports/apidocs/index.html` | Open JavaDocs on macOS |

On Windows, open JavaDocs with:

```powershell
Start-Process target/reports/apidocs/index.html
```

On Linux, open JavaDocs with:

```bash
xdg-open target/reports/apidocs/index.html
```

Important: `mvn run` is not a valid Maven command for this project. Use `mvn javafx:run`.

## Project Layout

```text
Basketball-game/
├── pom.xml
├── README.md
├── src/main/java/
│   ├── module-info.java
│   └── com/example/
│       ├── FinalProj.java      # JavaFX app, screens, controls, animations
│       ├── Data.java           # Game state and shot probability logic
│       └── Player.java         # Player model, scoring, shot zones
└── src/main/resources/
    ├── styles.css              # JavaFX CSS
    ├── BasketballBackground.png
    ├── Characters.png          # Character-select roster art
    ├── Lebron.png
    ├── Curry.png
    ├── Durant.png
    ├── Antetokounmpo.png
    ├── Doncic.png
    ├── Basketball.png
    └── gametracks.mp3
```

Generated build files live in `target/`. They are ignored by Git and should not be committed.

## How to Play

Hoops Showdown is a same-keyboard 1v1 game.

1. Press `PRESS TO START`.
2. Player 1 selects a character with number keys `1` through `5`, then clicks `DONE`.
3. Player 2 selects a different character, then clicks `BEGIN MATCH`.
4. Player 2 starts with the ball.
5. First player to reach 11 points wins.

## Characters

| Key | Character |
| --- | --- |
| `1` | LeBron James |
| `2` | Stephen Curry |
| `3` | Kevin Durant |
| `4` | Giannis Antetokounmpo |
| `5` | Luka Doncic |

Character art has been upgraded to detailed arcade-style transparent PNG sprites.

Special gameplay note: Kevin Durant is character 3 and currently has a 90% shot make chance.

## Controls

### Player 1

| Action | Key |
| --- | --- |
| Move up | `W` |
| Move left | `A` |
| Move down | `S` |
| Move right | `D` |
| Shoot / Block | `E` |

### Player 2

| Action | Key |
| --- | --- |
| Move up | `I` |
| Move left | `J` |
| Move down | `K` |
| Move right | `L` |
| Shoot / Block | `O` |

## Gameplay Rules

### Shooting

If you have the ball, press your shoot key to jump and release a shot. The ball follows a curved path to the rim.

Scoring:

| Shot location | Points |
| --- | --- |
| Inside the three-point line | 2 |
| Outside the three-point line | 3 |

Shot chance is based on distance from the rim, defender spacing, character stats, and special character rules.

### Blocking

If you do not have the ball, your shoot key becomes a block attempt.

A block succeeds only when:

- the defender presses within 50 ms of the offensive shot,
- the defender is in front of the shooter toward the rim,
- the defender is close enough to the shooting lane.

On a successful block, the shot is canceled, `BLOCK!` appears, and the defender starts the next round on offense.

### Round Flow

- Made shot: score updates and the shooter keeps possession.
- Missed shot: the other player gets possession.
- Blocked shot: the defender gets possession.
- Game over: first to 11 wins and the stats screen appears.

## JavaDoc and Annotations

The main classes now include JavaDoc annotations such as `@param` and `@return` on important public methods. These annotations are used by the JavaDoc generator to build readable API documentation.

Generate JavaDocs:

```bash
mvn javadoc:javadoc
```

Open the generated docs on macOS:

```bash
open target/reports/apidocs/index.html
```

The generated JavaDoc entry point is:

```text
target/reports/apidocs/index.html
```

## Architecture Notes

| File | Responsibility |
| --- | --- |
| `FinalProj.java` | JavaFX screens, keyboard input, animation loop, shots, blocks, visual effects |
| `Data.java` | Player storage, possession, distance and probability calculations |
| `Player.java` | Character selection value, score, position, shot type, point value |
| `styles.css` | Buttons, scoreboard, titles, stats screen styling |

Key implementation details:

- Movement uses `AnimationTimer` and held-key tracking for smooth motion.
- Shot animation uses `PathTransition` with `QuadCurveTo` for a parabolic arc.
- The three-point line is drawn in JavaFX and uses the same hoop/radius constants as scoring.
- Blocking uses a small timing window plus a geometric lane check.
- Visual polish includes player shadows, nameplates, court overlays, and generated sprite assets.

## Troubleshooting

### `Unknown lifecycle phase "run"`

Use:

```bash
mvn javafx:run
```

Do not use `mvn run`.

### `Error: JavaFX runtime components are missing`

Launch through Maven so the JavaFX module path is configured correctly:

```bash
mvn javafx:run
```

### `release version 17 not supported`

Your active JDK is older than 17. Install JDK 17 or newer and confirm:

```bash
java -version
```

### Music does not play

Confirm `src/main/resources/gametracks.mp3` exists. Some Linux systems may also need native media/GStreamer support for JavaFX media playback.

### JavaDoc command cannot find `javadoc`

Make sure you are using a full JDK, not just a JRE:

```bash
javadoc -version
```

## Git Notes

- Keep source files and resources committed.
- Do not commit `target/`; it is generated by Maven.
- The upgraded PNG character assets are project resources and should be committed.

Have fun. First to 11 wins.
