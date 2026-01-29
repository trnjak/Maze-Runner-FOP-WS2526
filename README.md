# Hayley's Curse

**Hayley's Curse** is a Java-based 2D maze runner game developed using the **LibGDX** framework. This project demonstrates the implementation of procedural content generation, object-oriented design patterns, and persistent state management. It requires players to navigate dynamic grid-based environments, manage character statistics, and utilise algorithmic pathfinding strategies to overcome autonomous agents.

## UML Diagrams [[PDF]](./UML/uml-diagram.pdf)

## Narrative Context

As defined in the `StoryScreen` class, the game presents a narrative centered on "Princess Hayley," who is imprisoned within the "Castle of Zoloft." The gameplay simulates an escape attempt where the user must navigate a labyrinthine environment.

Key mechanics derived from the narrative include:

* **Dynamic Exit:** The exit coordinates are randomised every 30 seconds, simulating a shifting environment.
* **Adversarial Agents:** The player encounters "Ghosts" (Chaser logic) and "Giant Spiders" (Patrol logic).
* **Environmental Hazards:** The grid contains spikes and sludge traps that hinder movement or deplete health.

## Core Architecture (`de.tum.cit.fop.maze`)

This package contains the foundational classes responsible for application lifecycle management, global state, and data persistence.

* **`MazeRunnerGame`**: Acts as the central application controller. It manages the `SpriteBatch` and UI `Skin` shared across all screens. It functions as a finite state machine, handling transitions between the Menu, Gameplay, and Settings states, while also managing global assets such as audio streams and textures.
* **`GameMap`**: The primary class for grid management. It manages the 2D array of tiles and parses `.properties` files to instantiate game objects. For the "Endless Mode," it implements a **Depth-First Search (DFS)** algorithm to procedurally generate solvable maze structures.
* **`PlayerStats`**: Handles data persistence. This class tracks volatile data (Level, EXP, Score) and serializes it to local storage using the LibGDX JSON library, ensuring state preservation between sessions.
* **`Achievement`**: Represents individual progression milestones. It encapsulates logic to monitor specific metrics and automatically update the `unlocked` boolean state when criteria are met.
* **`KeyBindings`**: Manages input configuration. It maps abstract user actions (e.g., "Attack," "Move Up") to specific hardware key codes, allowing for runtime remapping via the `SettingsScreen`.

## Entity Hierarchy (`de.tum.cit.fop.maze.objects`)

The game utilises a strict inheritance hierarchy to manage entities.

* **`GameObj` (Abstract Base)**: Defines the fundamental properties of all physical entities, including coordinate position (`x`, `y`), dimensions, and a `Rectangle` hitbox for collision detection. It enforces a standard 32-pixel tile rendering implementation.
* **`Player`**: Implements the user-controlled entity. This class handles complex locomotion logic (sprinting, deceleration via sludge), health state, and a Directional Combat System that calculates an attack vector based on the entity's orientation.
* **`Exit`**: Represents the win condition. In "Endless Mode," its position is programmatically updated by the `GameScreen` on a 30-second interval.
* **`Key`**: A requisite item for level completion, utilising overlap detection to trigger state changes in the `GameScreen`.

### Specialized Sub-Packages

#### Enemies (AI Behavior)

* **`ChaserEnemy`**: Implements a predator behaviour. It utilises **Ray-Casting** to determine line-of-sight; upon detection, it calculates a vector to intercept the player.
* **`PatrolEnemy`**: Implements a sentry behaviour, oscillating along a fixed horizontal or vertical axis.

#### Environmental Objects

* **Traps**: Abstract base for hazards. Includes `DamageTrap` (applies damage via a timer) and `SludgeTrap` (applies a velocity reduction coefficient).
* **Powerups**: Abstract base for beneficial items. Includes `Hpup` (restores health) and `Speedup` (temporarily increases velocity).

## User Interface Layer (`de.tum.cit.fop.maze.screens`)

The UI layer is decoupled from the game logic, handling rendering and user input.

* **`GameScreen`**: The primary gameplay environment. It manages the main game loop and five distinct UI stages: HUD, Pause Menu, Game Over/Victory overlays, and a Developer Console.
* **`StoryScreen`**: Renders the narrative context using a `ScrollPane` to display the "Hayley's Curse" lore.
* **`LeaderboardScreen`**: Implements file I/O to scan the save directory, deserializing JSON files to sort and display player rankings.
* **`StatsScreen`**: A functional upgrade menu allowing users to exchange accumulated EXP for permanent variable adjustments (e.g., `maxHp`, movement speed).
* **`SettingsScreen`**: Provides an interface for modifying `KeyBindings`, capturing raw keyboard input to update the configuration map.

## Map Architecture and File I/O

The application utilises a property-based system for world definition, managed by the `GameMap` class. Maps are stored in the `/maps` directory.

### Coordinate System

The parser interprets integer values mapped to specific object types:

* **0**: Wall (Collision object)
* **1**: Entry Point
* **2**: Exit
* **3**: Trap
* **4**: Enemy
* **5**: Key
* **6**: Powerup

### Map Types

* **Static Maps**: Pre-defined configuration files (`level-1.properties` through `level-5.properties`).
* **Procedural Maps**: `endless.properties`. This file is dynamically overwritten by the DFS generation algorithm at runtime before being loaded into memory.

## Asset Management

Assets are centralised to ensure memory efficiency.

* **Visuals**: Uses texture atlases (`main_tilemap.png` for environment, `hud_tilemap.png` for UI) sliced into `TextureRegion` arrays.
* **Audio**: Separates looping background music (`menu_bg.mp3`, `game_bg.mp3`) from event-triggered sound effects (combat noises, victory/failure cues, and the `witch_cackle.ogg` spatial cue).
* **UI Skinning**: Utilizes `craftacular-ui.json` to define global styles for fonts, buttons, and window elements.

## Controls & Navigation

Default key bindings are managed by the `KeyBindings` class. While movement and combat keys can be remapped via the Settings menu, the default configuration is as follows:

### Movement & Combat

* **Move Up**: `Arrow Up`
* **Move Down**: `Arrow Down`
* **Move Left**: `Arrow Left`
* **Move Right**: `Arrow Right`
* **Sprint**: `Left Shift` (Hold to increase velocity)
* **Attack**: `Space` (Deals damage in the direction faced)

### Camera & Debug

* **Zoom In**: `=` (Equal Sign)
* **Zoom Out**: `-` (Minus Sign)
* **Dev Console**: `F1`

## Deployment

1. Clone the repository.
2. Import the directory as a Gradle project.
3. Execute the `DesktopLauncher` class to initialise the application.