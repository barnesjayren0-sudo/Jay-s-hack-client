---
name: Fabric build toolchain
description: Non-obvious Gradle and JVM requirements for this Fabric 1.21.11 project.
---

Fabric Loom 1.14.10 requires Gradle 9.2.0 and a Java 21 runtime. Older system Gradle or Java versions fail during project configuration before source compilation.

**Why:** The imported environment may expose Gradle 8.x and Java 19 even though the project declares Java 21; validating with those versions gives misleading dependency/toolchain failures.

**How to apply:** Use a temporary Gradle 9.2.0 distribution and Java 21 runtime for validation; do not lower the project’s Java or Loom versions.