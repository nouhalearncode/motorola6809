# Motorola 6809 Processor Simulator / Simulateur de Processeur Motorola 6809

![Java](https://img.shields.io/badge/Language-Java-orange) ![Status](https://img.shields.io/badge/Status-Active-brightgreen) ![License](https://img.shields.io/badge/License-MIT-blue)

[English](#english-version) | [Français](#version-française)

---

<a name="english-version"></a>
## 🇺🇸 English Version

### Project Overview
This project is a detailed software simulator of the **Motorola 6809** microprocessor, written in **Java**. Ideally designed for educational purposes, it simulates the internal architecture of the processor, including registers (A, B, D, X, Y, U, S, CC, PC), memory management (RAM/ROM), and instruction execution cycles.

The simulator implements a robust **Von Neumann architecture** engine capable of fetching, decoding, and executing 6809 assembly instructions with high fidelity, including cycle-accurate timing and precise flag (CCR) management.

### Key Features

#### 1. Core Architecture
- **Registers**: Full simulation of 8-bit accumulators (A, B), 16-bit accumulator (D), Index registers (X, Y), Stack pointers (U, S), and Program Counter (PC).
- **Condition Code Register (CCR)**: Accurate bit-level manipulation of flags (EFHINZVC) affects branching and arithmetic logic.
- **Memory**: Configurable RAM and ROM address spaces.

#### 2. Instruction Set
The simulator supports a wide range of essential instructions including:
- **Load/Store**: `LDA`, `LDB`, `LDD`, `LDX`, `LDY`, `LDS`, `LDU`, `STA`, `STB`, `STD`, `STX`, `STY`, `STS`, `STU`.
- **Arithmetic**: `ADD`, `SUB`, `MUL`, `DAA`.
- **Logic**: `AND`, `OR`, `EOR`, `COM`, `NEG`.
- **Branching**: `JMP`, `BRA`, `BNE`, `BEQ`, etc.
- **Effective Address Load**: `LEAX`, `LEAY`, `LEAS`, `LEAU` (Essential for stack frame manipulation).

#### 3. Advanced Addressing Modes
We have implemented the most complex addressing modes of the 6809:
- **Immediate**: `#$1234`
- **Direct**: `<$12` (Page Zero optimization)
- **Extended**: `>$1234` (Full 16-bit address)
- **Indexed**:
    - **Zero Offset**: `,X`
    - **Constant Offset**: `5,Y`, `$10,X` (5-bit, 8-bit, 16-bit offsets)
    - **Accumulator Offset**: `A,X`, `B,Y`, `D,U`
    - **Auto-Increment**: `,X+` (Post-inc by 1), `,Y++` (Post-inc by 2)
    - **Auto-Decrement**: `,-U` (Pre-dec by 1), `,--S` (Pre-dec by 2)
    - **Program Counter Relative**: `TABLE,PCR`
- **Indirect Addressing**:
    - Recursive parsing for `[ ... ]` syntax.
    - Full support for indirect modes: `[,X]`, `[$10,Y]`, `[A,X]`, `[,X++]`, etc.

### Getting Started

#### Prerequisites
- **Java Development Kit (JDK)** 17 or higher.
- A Java IDE (VS Code, IntelliJ, Eclipse) or command line interface.

#### Installation
Clone the repository:
```bash
git clone https://github.com/your-username/motorola6809-simulator.git
cd motorola6809
```

#### Running the Simulator
Open the project in your IDE and run the `Main` class located in `src/processeur/main.java`.
The simulator currently accepts assembly code via input files or internal arrays for testing.

```java
// Example usage in Main.java
Instructions.add("LDX #$1000"); // Load X with HEX 1000
Instructions.add("LDA ,X+");    // Load A from [1000], then Increment X
Instructions.add("END");
```

### Technical Detail: Cycle Accuracy
Each instruction updates the "Cycle Count" depending on the addressing mode used, mimicking the real hardware delays (e.g., +1 cycle for Indexed, +4 cycles for Indirect).

---

<a name="version-française"></a>
## 🇫🇷 Version Française

### Présentation du Projet
Ce projet est un simulateur logiciel détaillé du microprocesseur **Motorola 6809**, écrit en **Java**. Conçu idéalement pour des fins pédagogiques, il simule l'architecture interne du processeur, y compris les registres (A, B, D, X, Y, U, S, CC, PC), la gestion de la mémoire (RAM/ROM) et les cycles d'exécution des instructions.

Le simulateur implémente un moteur **Von Neumann** robuste capable de lire, décoder et exécuter des instructions d'assemblage 6809 avec une grande fidélité, incluant le chronométrage précis des cycles et la gestion rigoureuse des drapeaux (flags CCR).

### Fonctionnalités Clés

#### 1. Architecture Cœur
- **Registres** : Simulation complète des accumulateurs 8 bits (A, B), accumulateur 16 bits (D), registres d'index (X, Y), pointeurs de pile (U, S) et compteur ordinal (PC).
- **Registre de Code de Condition (CCR)** : Manipulation précise des bits de drapeaux (EFHINZVC) affectant les branchements et la logique arithmétique.
- **Mémoire** : Espaces d'adressage RAM et ROM configurables.

#### 2. Jeu d'Instructions
Le simulateur supporte un large éventail d'instructions essentielles incluant :
- **Chargement/Stockage** : `LDA`, `LDB`, `LDD`, `LDX`, `LDY`, `LDS`, `LDU`, `STA`, `STB`, `STD`, `STX`, `STY`, `STS`, `STU`.
- **Arithmétique** : `ADD`, `SUB`, `MUL`, `DAA`.
- **Logique** : `AND`, `OR`, `EOR`, `COM`, `NEG`.
- **Branchement** : `JMP`, `BRA`, `BNE`, `BEQ`, etc.
- **Chargement d'Adresse Effective** : `LEAX`, `LEAY`, `LEAS`, `LEAU` (Essentiel pour la manipulation de cadres de pile).

#### 3. Modes d'Adressage Avancés
Nous avons implémenté les modes d'adressage les plus complexes du 6809 :
- **Immédiat** : `#$1234`
- **Direct** : `<$12` (Optimisation Page Zéro)
- **Étendu** : `>$1234` (Adresse complète 16 bits)
- **Indexé** :
    - **Déplacement Nul** : `,X`
    - **Déplacement Constant** : `5,Y`, `$10,X` (déplacements 5-bit, 8-bit, 16-bit)
    - **Déplacement Accumulateur** : `A,X`, `B,Y`, `D,U`
    - **Auto-Incrémentation** : `,X+` (Post-inc de 1), `,Y++` (Post-inc de 2)
    - **Auto-Décrémentation** : `,-U` (Pré-déc de 1), `,--S` (Pré-déc de 2)
    - **Relatif au Compteur Ordinal (PC)** : `TABLE,PCR`
- **Adressage Indirect** :
    - Parsing récursif pour la syntaxe `[ ... ]`.
    - Support complet des modes indirects : `[,X]`, `[$10,Y]`, `[A,X]`, `[,X++]`, etc.

### Pour Commencer

#### Pré-requis
- **Java Development Kit (JDK)** 17 ou supérieur.
- Un IDE Java (VS Code, IntelliJ, Eclipse) ou une interface en ligne de commande.

#### Installation
Clonez le dépôt :
```bash
git clone https://github.com/votre-username/motorola6809-simulator.git
cd motorola6809
```

#### Exécuter le Simulateur
Ouvrez le projet dans votre IDE et exécutez la classe `Main` située dans `src/processeur/main.java`.
Le simulateur accepte actuellement du code assembleur via des fichiers d'entrée ou des tableaux internes pour les tests.

```java
// Exemple d'utilisation dans Main.java
Instructions.add("LDX #$1000"); // Charge X avec HEX 1000
Instructions.add("LDA ,X+");    // Charge A depuis [1000], puis Incrémente X
Instructions.add("END");
```

### Détail Technique : Précision des Cycles
Chaque instruction met à jour le "Cycle Count" en fonction du mode d'adressage utilisé, imitant les délais matériels réels (ex: +1 cycle pour Indexé, +4 cycles pour Indirect).

---

## Project Structure / Structure du Projet

```
src/
└── processeur/
    ├── main.java       # Entry point / Point d'entrée
    ├── mode.java       # Logic Core (Instruction decoding) / Cœur Logique
    ├── registre.java   # Data structure for CPU registers / Structure des registres
    ├── ram.java        # Random Access Memory simulation
    ├── ROM.java        # Read-Only Memory simulation
    └── pas.java        # Step-by-step execution engine / Moteur pas-à-pas
```

---

© 2025 Motorola 6809 Simulator Project.
