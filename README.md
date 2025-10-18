# metalog
Metalog.  A low impact and highly performant structured meta logging library for Java.
#### 1. Log messages are only generated if consumed
#### 2. If consumed by multiple subscribers, the generation only occurs once.
#### 3. Messages are based on CharSequence so everything does not have to be converted to a string
#### 4. By default, log messages are consumed on worker threads
#### 5. Channels represent things like 'info', 'debug', etc
#### 6. Opt-in sequencing keys to maintain order of messages and provide dedicated consumption thread
###  7. Opt-out of worker threads and do processing on current thread.
```
// light
publish(() -> "Hello World");
```
```
// heavy
            publish( () -> {
                StringBuilder builder = new StringBuilder();
                builder.append(e.getMessage());
                builder.append(System.lineSeparator());
                builder.append(someCostlyOperation());
                return builder;
            }, 
            b -> b  // Meta builder callback
            .thrown(e) // retain exception
            .thread() // retain current thread information
            .time()); // retain current time
```
```
// hidden heavy
            publish(this::someMethodToProduceTheMessage);
```

## Documentation and Reports
[Java API](https://jonloucks.github.io/metalog/javadoc/)

[Java Test Coverage](https://jonloucks.github.io/metalog/jacoco/)

## Badges
[![OpenSSF Best Practices](https://www.bestpractices.dev/projects/11312/badge)](https://www.bestpractices.dev/projects/11312)
[![Coverage Badge](https://raw.githubusercontent.com/jonloucks/metalog/refs/heads/badges/main-coverage.svg)](https://jonloucks.github.io/metalog/jacoco/)
[![Javadoc Badge](https://raw.githubusercontent.com/jonloucks/metalog/refs/heads/badges/main-javadoc.svg)](https://jonloucks.github.io/metalog/javadoc/)
