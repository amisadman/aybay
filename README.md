<p align="center">
  <img src="./ExternalResources/aybay-animation.gif"/>
</p>

<h1 align="center">AyBay</h1>

<p align="center"><i>
A powerful and lightweight Android application for tracking income, expenses, budgets, savings, loans, and more.
</i></p>

---

## Features

- **Comprehensive Tracking**: Track income, expenses, savings, budgets, and loans/debts.
- **Smart AI Assistant**: "Walleo" - Built-in AI chatbot for personalized financial guidance and suggestions.
- **Data Visualization**: View clean statistics to understand your financial health.
- **Search & Filter**: Easily find transactions by date, type, or specific queries.
- **Backup & Restore**: Secure cloud backup via Google Drive and local storage options.
- **Offline First**: Fully functional offline with robust SQLite local storage.
- **Modern UI**: Clean, fast, and responsive user interface.

---

## Installation

You can install AyBay from:

[![GitHub](https://img.shields.io/badge/GitHub-Install-informational?logo=github)](https://github.com/mdtahmidrahman/AyBay-FinanceTracker/releases/tag/v1.0.0)
[![Google Drive](https://img.shields.io/badge/Google%20Drive-Download-green?logo=google-drive)](https://drive.google.com/file/d/1KUU8vM8rXXOHGKe-sdrNVdNx81QNTk0b/view?usp=sharing)

---

## Architecture & Design Patterns

AyBay is built using a **Pattern-Oriented MVC Architecture**, leveraging standard GoF design patterns to ensure scalability, maintainability, and clean code.

### Data Flow Diagram
```mermaid
graph TD
    subgraph "UI Layer"
        UI_Trans[Transaction Activities]
        UI_Auth[Login/Signup Activities]
        UI_Chat[Walleo Chatbot]
    end

    subgraph "Facade Layer"
        FinMgr[FinanceManager]
        AuthMgr[AuthFacade]
    end

    subgraph "Repository / Strategy Layer"
        Repo[DatabaseRepository]
        Strategy[Expense/Income Strategy]
    end

    subgraph "Business Logic / Patterns"
        Command[DeleteCommand]
        Memento[TransactionMemento]
        Observer[TransactionObserver]
        Factory[TransactionFactory]
    end

    subgraph "Data Layer"
        DB[DatabaseHelper]
        ExtAPI[Gemini API]
    end

    %% Transaction Flow
    UI_Trans -->|Calls| FinMgr
    FinMgr -->|Delegates to| Repo
    Repo -->|Uses| Strategy
    Strategy -->|Persists Insert Update| DB

    FinMgr -->|Executes| Command
    FinMgr -->|Uses| Factory
    FinMgr -->|Registers| Observer

    Command -->|Uses| Memento
    Command -->|Modifies Delete Restore| DB

    DB -->|Notifies| Observer
    Observer -->|Updates| UI_Trans

    %% Authentication Flow
    UI_Auth -->|Calls| AuthMgr
    AuthMgr -->|Queries/Updates| DB

    %% Chatbot Flow
    UI_Chat -->|Streams| ExtAPI
    UI_Chat -->|Saves History| DB

```

### Implemented Patterns:

- **Facade Pattern**: `FinanceManager` acts as a unified interface to the complex subsystem of data repositories and logic.
- **Strategy Pattern**: `DataOperationStrategy` interfaces with concrete implementations like `ExpenseOperationStrategy` and `IncomeOperationStrategy` to handle different transaction types dynamically.
- **Observer Pattern**: `TransactionObserver` allows the UI (`ShowExpense`, `ShowIncome`) to react automatically to data changes in the repository.
- **Command Pattern**: `DeleteCommand` encapsulates delete requests, enabling features like Undo operations.
- **Composite Pattern**: `CategoryComposite` treats individual transactions and groups of categories uniformly for calculating totals.
- **Iterator Pattern**: `TransactionIterator` provides a standard way to traverse collections of transactions without exposing underlying representations.
- **Factory Pattern**: `TransactionFactory` handles the creation of complex `Transaction` objects.
- **Adapter Pattern**: `CurrencyAdapter` and `DateAdapter` transform data formats to be compatible with UI requirements.
- **Memento Pattern**: Support for capturing and restoring object state (e.g., for Undo functionality).
- **Singleton Pattern**: Ensures crucial classes like `FinanceManager` and `DatabaseHelper` have a single shared instance.

### Data Flow Diagram
```mermaid
graph TD
    subgraph "UI Layer"
        UI_Trans[Transaction Activities]
        UI_Auth[Login/Signup Activities]
        UI_Chat[Walleo Chatbot]
    end

    subgraph "Facade Layer"
        FinMgr[FinanceManager]
        AuthMgr[AuthFacade]
    end

    subgraph "Repository / Strategy Layer"
        Repo[DatabaseRepository]
        Strategy[Expense/Income Strategy]
    end

    subgraph "Business Logic / Patterns"
        Command[DeleteCommand]
        Memento[TransactionMemento]
        Observer[TransactionObserver]
        Factory[TransactionFactory]
    end

    subgraph "Data Layer"
        DB[DatabaseHelper]
        ExtAPI[Gemini API]
    end

    %% Transaction Flow
    UI_Trans -->|Calls| FinMgr
    FinMgr -->|Delegates to| Repo
    Repo -->|Uses| Strategy
    Strategy -->|Persists Insert Update| DB

    FinMgr -->|Executes| Command
    FinMgr -->|Uses| Factory
    FinMgr -->|Registers| Observer

    Command -->|Uses| Memento
    Command -->|Modifies Delete Restore| DB

    DB -->|Notifies| Observer
    Observer -->|Updates| UI_Trans

    %% Authentication Flow
    UI_Auth -->|Calls| AuthMgr
    AuthMgr -->|Queries/Updates| DB

    %% Chatbot Flow
    UI_Chat -->|Streams| ExtAPI
    UI_Chat -->|Saves History| DB

```

---

## Built With

- **Language**: Java (Android SDK)
- **Database**: SQLite (Custom `DatabaseHelper`)
- **Testing**: JUnit 5, Mockito
- **Networking**: OkHttp (for AI Chatbot)

---

## Screenshots

<p align="center">
  <img src="ExternalResources/Screenshot_20250804_015223_AybayLite.jpg" width="200"/> &nbsp;
  <img src="ExternalResources/Screenshot_20251211_104144_AybayLite.jpg" width="200"/> &nbsp;
  <img src="ExternalResources/Screenshot_20251211_104214_AybayLite.jpg" width="200"/>
</p>

<p align="center">
  <img src="ExternalResources/Screenshot_20251211_103159_AybayLite.jpg" width="200"/> &nbsp;
  <img src="ExternalResources/Screenshot_20251211_103215_AybayLite.jpg" width="200"/> &nbsp;
  <img src="ExternalResources/Screenshot_20251211_103259_AybayLite.jpg" width="200"/>
</p>

<p align="center">
  <img src="ExternalResources/Screenshot_20251211_103330_AybayLite.jpg" width="200"/> &nbsp;
  <img src="ExternalResources/Screenshot_20251211_103347_AybayLite.jpg" width="200"/> &nbsp;
  <img src="ExternalResources/Screenshot_20251211_103446_AybayLite.jpg" width="200"/>
</p>


---

## ⚠️ Disclaimer

This app is for educational and personal finance management purposes only.  
It is not associated with any banking or financial institution.
