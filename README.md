# 🏀 Hoops Showdown

A 1v1 NBA street-ball arcade game built in **JavaFX**. Pick an NBA star, take the court, and race your opponent to **11 points** with shooting, stealing, and shot-clock pressure.

Made by **Evan and Kris**.

---

## Table of Contents
- [Requirements](#requirements)
- [Project Layout](#project-layout)
- [Compile & Run](#compile--run)
- [How to Play](#how-to-play)
- [Controls](#controls)
- [Game Flow](#game-flow)
- [Troubleshooting](#troubleshooting)

---

## Requirements

| Tool   | Version       |
|--------|---------------|
| JDK    | **17 or newer** |
| Maven  | **3.6+**      |
| OS     | macOS, Windows, or Linux |

JavaFX itself is pulled in automatically as a Maven dependency — you do **not** need to install the JavaFX SDK separately.

Check what you have:

```bash
java -version
mvn -version
```

---

## Project Layout

```
FullFinalProject/
├── pom.xml                          # Maven build config (JavaFX plugin + deps)
├── src/main/java/com/example/
│   ├── FinalProj.java               # Main JavaFX Application (UI + animations)
│   ├── Data.java                    # Game state, possession, probabilities
│   └── Player.java                  # Player model (score, NBA pick, position)
├── src/main/java/module-info.java   # Java module declaration
└── src/main/resources/
    ├── styles.css                   # Visual styling (buttons, scoreboard, etc.)
    ├── BasketballBackground.png     # Court background
    ├── Characters.png               # Character select panel
    ├── Lebron.png, Curry.png, ...   # NBA player sprites
    ├── Basketball.png               # Window icon
    └── gametracks.mp3               # In-game music
```

---

## Compile & Run

From the project root (`FullFinalProject/`):

### Run the game (one command)
```bash
mvn javafx:run
```

This is the **normal way to play** — Maven downloads dependencies, compiles, and launches the JavaFX window.

### Compile only
```bash
mvn clean compile
```

### Clean rebuild
```bash
mvn clean compile && mvn javafx:run
```

> ⚠️ `mvn run` will **not** work — `run` isn't a Maven lifecycle phase. Always use `mvn javafx:run`.

---

## How to Play

The game is a **first-to-11** 1v1 basketball match between two players on the **same keyboard**.

### 1. Welcome screen
Press **PRESS TO START** to begin.

### 2. Character Select — Player 1
Press a number key **1–5** to pick your NBA star, then click **DONE**.

| Key | Character        |
|-----|------------------|
| 1   | LeBron James     |
| 2   | Stephen Curry    |
| 3   | Kevin Durant     |
| 4   | Giannis Antetokounmpo |
| 5   | Luka Dončić      |

Each character has slightly different shooting and stealing tendencies — pick your style.

### 3. Character Select — Player 2
Press an **unselected** number (you can't double up on the same star), then click **BEGIN MATCH**.

Player 2 starts with the ball.

### 4. The match
Move your character, dribble (the ball follows whoever has possession), and press your shoot key when you're in a good spot. The ball arcs toward the rim — make it for points, miss for a turnover.

**First to 11 points wins.**

### 5. Game Over
A final stat sheet appears showing each player's score, makes, misses, and total attempts.

---

## Controls

The two players share one keyboard.

### Player 1 (left side, green ring)
| Action | Key |
|--------|-----|
| Move up    | `W` |
| Move left  | `A` |
| Move down  | `S` |
| Move right | `D` |
| Shoot / Steal | `E` |

### Player 2 (right side, pink ring)
| Action | Key |
|--------|-----|
| Move up    | `I` |
| Move left  | `J` |
| Move down  | `K` |
| Move right | `L` |
| Shoot / Steal | `O` |

### Shoot vs. Steal
The `E` / `O` key is **context-sensitive**:
- **If you have the ball** → you shoot. Your character dips, leaps, releases, and the ball arcs to the rim.
- **If your opponent has the ball** → you attempt a steal. A successful steal is announced with a red **STEAL!!** banner.

---

## Game Flow

1. **Setup** — Each round resets both players to their starting positions, with the loser of the previous point on offense (winner's-out... actually, *loser's ball* — keep it fair).
2. **Possession** — The ball is glued to whoever has it. The other player can attempt a steal.
3. **Shot** — Press your shoot key:
   - Shooter **dips** then **leaps**
   - Ball is released at the peak and follows a **parabolic arc**
   - Ball **spins** in flight
   - On **make**: `SWISH!` popup + screen shake, scoreboard updates, the scorer keeps possession.
   - On **miss**: `BRICK!` popup, ball bounces off the rim, the other player gets the ball.
4. **End** — First to **11 points** wins. The Game Over screen shows full stats.

---

## Troubleshooting

### `Unknown lifecycle phase "run"`
You typed `mvn run`. Use `mvn javafx:run` instead.

### `Error: JavaFX runtime components are missing`
Run via Maven (`mvn javafx:run`), not by invoking `java` directly on the compiled class. The Maven plugin sets up the JavaFX module path for you.

### `release version 17 not supported`
Your JDK is older than 17. Install JDK 17+ and make sure `java -version` reports 17 or newer.

### Window opens but no music
Confirm `gametracks.mp3` is present in `src/main/resources/`. Some Linux systems also need GStreamer installed for JavaFX media.

### Native-access warnings on JDK 17+
Harmless. They come from JavaFX 21 internals and don't affect gameplay.

---

## Tech Notes

- **JavaFX 21** for rendering and animation
- Animations use `PathTransition`, `ParallelTransition`, `SequentialTransition`, `RotateTransition`, and `ScaleTransition`
- Visual styling lives in [`src/main/resources/styles.css`](src/main/resources/styles.css)
- Shot mechanics combine `QuadCurveTo` paths (parabolic arcs) with shooter jump animations for a realistic shooting motion

Have fun, and **first to 11 wins!** 🏆
