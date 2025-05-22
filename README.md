# unused-class-maven-plugin

A simple Maven plugin to detect unused Java classes in your project. It analyzes compiled `.class` files and reports classes that are not referenced anywhere else in your codebase.

## Features
- Detects unused classes in your build output
- Ignores classes used by frameworks (e.g., Spring controllers, services, configs, JAX-RS resources)
- Easy to use with Maven

## Usage
Add the plugin to your `pom.xml`:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.nicolasholanda</groupId>
      <artifactId>unused-class-maven-plugin</artifactId>
      <version>1.0-SNAPSHOT</version>
      <executions>
        <execution>
          <goals>
            <goal>check</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

Then run:

```
mvn verify
```

Unused classes will be listed in the build log.

## How it works
- Scans all `.class` files in your output directory
- Tracks references between classes
- Excludes classes annotated with known framework annotations (like `@Controller`, `@RestController`, `@Service`, `@Configuration`, `@SpringBootApplication`, `@Path`, etc.)

## Requirements
- Java 11 or higher
- Maven 3.6+

