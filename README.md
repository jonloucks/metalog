# Metalog

A low-impact, highly performant structured meta-logging library for Java.

## Overview

Metalog is a modern logging framework designed for high-performance Java applications. It eliminates unnecessary overhead by generating log messages only when they are consumed, and supports both synchronous and asynchronous processing patterns with fine-grained control over message routing and ordering.

## Key Features

### Performance-First Design
1. **Lazy Message Generation** - Log messages are only generated if consumed by subscribers
2. **Single Generation, Multiple Consumers** - Messages generated once, shared across all subscribers
3. **CharSequence-Based** - No forced string conversions; work with StringBuilder, String, or custom CharSequence implementations
4. **Asynchronous by Default** - Messages consumed on configurable worker threads to minimize impact on application threads

### Flexible Routing & Ordering
5. **Channel-Based Routing** - Categorize messages using channels like 'info', 'debug', 'warn', 'error'
6. **Optional Message Sequencing** - Use sequencing keys to guarantee ordering with dedicated consumption threads
7. **Synchronous Processing Option** - Opt-out of worker threads for immediate processing when needed

## Version

Current release: **1.2.3**

## Project Structure

- **metalog-api** - Public API interfaces and contracts
- **metalog-impl** - Default implementation with ServiceLoader support
- **metalog-test** - Testing utilities and helpers
- **metalog-smoke** - Smoke tests for verification

## Quick Start

### Simple Usage
```java
// Minimal logging - message generated only if consumed
publish(() -> "Hello World");
```

### Advanced Usage with Meta Information
```java
// Include exception, thread info, and timestamp
publish(() -> {
    StringBuilder builder = new StringBuilder();
    builder.append(e.getMessage());
    builder.append(System.lineSeparator());
    builder.append(someCostlyOperation());
    return builder;
}, 
b -> b  // Meta builder callback
    .thrown(e)    // retain exception
    .thread()     // retain current thread information
    .time());     // retain current time
```

### Method Reference Pattern
```java
// Clean method reference for complex message generation
publish(this::someMethodToProduceTheMessage);
```

## Installation

Add Metalog to your project via Maven Central:

```xml
<dependency>
    <groupId>io.github.jonloucks.metalog</groupId>
    <artifactId>metalog</artifactId>
    <version>1.2.3</version>
</dependency>
```

Or Gradle:
```gradle
implementation 'io.github.jonloucks.metalog:metalog:1.2.3'
```

## Core Concepts

### Publisher
The `Publisher` interface is responsible for publishing log messages. Use `GlobalMetalog` for convenient global access or create dedicated `Metalog` instances.

### Subscriber
Implement `Subscriber` to consume log messages. Subscribers receive both the log message and associated metadata.

### Meta
Metadata attached to each log message includes:
- **Channel** - Message category (info, debug, warn, error, etc.)
- **Sequencing Key** - Optional key for guaranteed message ordering
- **Thread Information** - Originating thread details
- **Timestamp** - When the message was created
- **Exception** - Associated throwable if applicable

### Channels
Channels categorize log messages for routing. Common channels include:
- `info` - General information messages
- `debug` - Debugging information
- `warn` - Warning messages
- `error` - Error messages
- `trace` - Detailed trace information

## Configuration

Metalog supports extensive configuration through `Metalog.Config`:

```java
Metalog metalog = GlobalMetalog.create(builder -> builder
    .unkeyedThreadCount(20)           // Worker threads for unkeyed messages
    .keyedQueueLimit(1000)            // Queue limit for keyed messages
    .unkeyedFairness(false)           // FIFO processing for unkeyed messages
    .shutdownTimeout(Duration.ofSeconds(60))  // Graceful shutdown timeout
);
```

## Architecture

Metalog follows a clean separation between API and implementation:

1. **metalog-api** - Defines all public interfaces and contracts
2. **metalog-impl** - Provides the default implementation using ServiceLoader
3. **Module System** - Full Java Platform Module System (JPMS) support

The library uses dependency injection through the Contracts API for flexible component replacement and testing.

## Documentation and Reports

- [Java API Documentation](https://jonloucks.github.io/metalog/javadoc/)
- [Test Coverage Report](https://jonloucks.github.io/metalog/jacoco/)

## Badges

[![OpenSSF Best Practices](https://www.bestpractices.dev/projects/11312/badge)](https://www.bestpractices.dev/projects/11312)
[![Coverage Badge](https://raw.githubusercontent.com/jonloucks/metalog/refs/heads/badges/main-coverage.svg)](https://jonloucks.github.io/metalog/jacoco/)
[![Javadoc Badge](https://raw.githubusercontent.com/jonloucks/metalog/refs/heads/badges/main-javadoc.svg)](https://jonloucks.github.io/metalog/javadoc/)

## Building from Source

Metalog uses Gradle for building:

```bash
# Build and run all tests
./gradlew build

# Run tests with coverage
./gradlew test jacocoTestReport

# Publish to local Maven repository
./gradlew publishToMavenLocal

# Generate Javadoc
./gradlew javadoc
```

## Contributing

Contributions are welcome! Please see:
- [CONTRIBUTING.md](CONTRIBUTING.md) - Contribution guidelines
- [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) - Code of conduct
- [CODING_STANDARDS.md](CODING_STANDARDS.md) - Coding standards
- [STYLE_GUIDE.md](STYLE_GUIDE.md) - Style guide
- [PULL_REQUEST_TEMPLATE.md](PULL_REQUEST_TEMPLATE.md) - PR template

## Security

For security concerns, please see [SECURITY.md](SECURITY.md).

## License

See [LICENSE](LICENSE) file for details.

## Release Notes

Release notes are available in the [notes/](notes/) directory:
- [v1.2.3](notes/release-notes-v1.2.3.md) - Latest
- [v1.2.2](notes/release-notes-v1.2.2.md)
- [v1.2.1](notes/release-notes-v1.2.1.md)
- [v1.2.0](notes/release-notes-v1.2.0.md)
- [Earlier versions...](notes/)

## Requirements

- Java 11 or higher
- No required runtime dependencies (contracts and concurrency APIs included)
