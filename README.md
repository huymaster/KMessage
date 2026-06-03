# KMessage - Secure Enterprise Messaging System
KMessage is a production-ready, highly secure enterprise messaging ecosystem designed around a client-server architecture. The primary focus of this project is to deliver extreme data privacy via Post-Quantum Cryptography (PQC) alongside low-level JVM memory optimizations for maximum platform scalability.
## Architectural Overview
```
huymaster-KMessage/
├── api/             # Domain contracts, security abstractions, core event pipelines
├── core/            # Ktor Server, persistence, cache, low-level utilities
├── android/         # Android Client built entirely on Jetpack Compose (MVI Architecture)
├── Dockerfile       # Infrastructure containerization scripts
└── docker-compose.yml
```
## Tech-stack
### Backend Core Server
- **Language & Runtime:** Pure **Kotlin** running on **Java 25** for modern language features and optimized execution.
- **Web Framework:** **Ktor Server** – Utilized for asynchronous, non-blocking routing, pipeline handling, and lightweight micro-services orchestration.
- **Asynchronous & Concurrency:** **Kotlin Coroutines & Flow** – Enforces lightweight, resource-efficient concurrency pipelines.
- **Logging & Monitoring:** **Logback** – High-performance production-grade logging infrastructure.

### Android Client
- **UI Framework:** **Jetpack Compose** – 100% declarative UI built with Material 3 design tokens, customizable themes, and robust state-hoisting components.
- **Architecture:** **MVI (Model-View-Intent)** – Standardizes a predictable Unidirectional Data Flow (UDF) combined with **Clean Architecture** patterns.
- **Dependency Injection:** **Koin Framework** – Pure Kotlin-based dependency injection container for dynamic configuration and modular compilation.
- **Core Engine Components:** **Jetpack Lifecycle & ViewModel** – Manages application scope states and lifecycle-aware configurations safely.

### Database, Caching & Infrastructure
- **Primary Database:** **PostgreSQL** – ACID-compliant enterprise relational store.
- **Database Engine Access:** **Exposed ORM** – A lightweight SQL library for Kotlin, utilizing continuous synchronization transactions mapped through explicit Type-safe table interfaces.
- **Memory & Session Caching:** **Redis** via **Lettuce Client** – Asynchronous, thread-safe Java Redis client configured to process high-throughput token metadata and active user cache with low latency.
- **Binary Object Store:** **MinIO Client** – High-performance, AWS S3-compatible decentralized storage server for file metadata attachment attachments (`FileMetadataTable`).
- **Containerization & Orchestration:** **Docker & Docker Compose** – Multi-stage build processes ensuring deterministic environment replica deployment.
- **Reverse Proxy:** **Nginx** – Edge router handling SSL/TLS encapsulation, compression pipelines, and security headers.

### Cryptography, Security & Network Protocols
- **Quantum-Resistant Key Exchange:** **ML-KEM** (NIST SP 800-203) – Lattice-based key encapsulation mechanism safeguarding device key exchanges.
- **Quantum-Resistant Digital Signatures:** **ML-DSA** (NIST SP 800-204) – Next-generation signing mechanism providing authentic, non-repudiation identities.
- **Asymmetric Key Authenticators:** **Ed25519** (RFC 8032) – Edwards-curve Digital Signature Algorithm ensuring fast and secure edge signature verifications.
- **Authentication Standards:** **JSON Web Tokens (JWT)** via **Auth0 JWT** – Secure stateless identity distribution engine.
- **Edge Security Storage:** **Android KeyStore Provider** – Secures application key pairs within a hardware-backed Trusted Execution Environment (TEE) or StrongBox module.

## 1. `api` Module (Core Contracts & Security Abstractions)
Acts as the Single Source of Truth (SSOT) shared between Client and Server to ensure protocol synchronization.
- Data Models / Requests: Rigid data contracts representing structural transfer objects.
- Event-Driven Architecture: Implements a decoupled Observer Pattern utilizing `EventPublisher`, `EventListener`, and `Subscription` interfaces to distribute asynchronous platform hooks cleanly.
- Security Inversion: Contains strict mathematical abstractions such as `KeyEncapsulation`, `DigitalSignature`, and `SymmetricCipher` to decouple business logic from concrete cryptographic implementations.
## 2. `core` Module (Ktor Backend)
The centralized transaction and routing brain powered by the [Ktor Framework](https://ktor.io/)
- Decoupled Persistence: Implements the Repository Pattern to shield core business services from direct database access.
- Relational mappings are bound explicitly via granular PostgreSQL tables (`UserTable`, `UserDeviceTable`, `FileMetadataTable`,...)
- Distributed Infrastructure:
  - RedisService: Low-latency, high-throughput session cache and memory store.
  - MinioService: Decentralized binary file storage abstracting AWS S3-compatible interfaces
  - JWTManager: Secure state-of-the-art authentication token generation.
## 3. `android` Module (Jetpack Compose Client)
A pure Kotlin Android application.
- Declarative UI Layer: Built 100% with Jetpack Compose utilizing a customizable, modern color-token and design system layout.
- Architecture Flow: Standardizes a unidirectional data flow (UDF) through a Clean/MVI implementation leveraging.
- Dependency Injection (DI): Enforces the Dependency Inversion Principle dynamically using [Koin](https://insert-koin.io/) to ensure maintainable decoupling.
## Advanced Security & International Standards
Security is baked directly into the engineering foundation of KMessage, enforcing cutting-edge federal and internet standards.
### Post-Quantum Cryptography Ready (PQC)
PostgreSQL identity schema is mathematically designed to hold and track cryptographic credentials capable of surviving quantum computing threats via:
- ML-KEM (NIST SP 800-203): Module-Lattice-Based Key-Encapsulation Mechanism utilized to protect asymmetric cryptographic key exchanges securely.
- ML-DSA (NIST SP 800-204): Module-Lattice-Based Digital Signature Algorithm used to confirm authentication integrity and provide quantum-resistant non-repudiation.
- Ed25519 (RFC 8032): Legacy Edwards-curve Digital Signature Algorithm optimized for traditional rapid edge authentication.
### Edge Vault Isolation (Android KeyStore)
TEE Protection: Local and critical state values are cryptographically handled through the `KeyStoreVault.kt` wrapper. Secrets are completely isolated into a hardware-backed Trusted Execution Environment (TEE) or StrongBox, making it immune to physical memory dumps or root exploits.
## Platform-Level Performance Tuning
- JVM Off-Heap Memory Buffering: Employs custom `DirectMemoryPool.kt` and `BufferUtils.kt` mechanisms. This enables the server core to allocate off-heap OS Native byte buffers directly. By moving payload manipulation away from the standard JVM Garbage Collector (GC) allocation space, it removes GC pressure and completely eliminates "Stop-The-World" pauses under extreme structural data throughput or large file operations.
- Fault-Tolerant Resilience: Includes a custom Circuit Breaker suite (`CircuitBreaker.kt`). If underlying persistent pipelines or external storage instances degrade or suffer out-of-memory cascades, the circuit automatically trips, containing the blast radius and preventing downstream system bottlenecks
