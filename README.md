# jamsmodels – Model Component Libraries for JAMS

This repository contains the domain-specific component libraries of the
[JAMS framework](https://github.com/jamsframework/jams) — most notably the
J2000 family of process-based hydrological models — developed at
Friedrich Schiller University Jena and partner institutions since 2005.

**Website:** https://jamsframework.org

## Modules

| Module | Description |
|---|---|
| `J2K_base` | Core J2000 process components (basis of the J2K model family) |
| `J2K_ext` | Extended J2000 components (builds on `J2K_base`) |
| `J2K_iso` | Isotope mixing, unmixing and transport simulation |
| `J2K_highres` | High-resolution J2000 variants |
| `J2K_Africa`, `J2K_Himalaya`, `J2K_SASSCAL` | Regional J2000 adaptations |
| `J2K_Irstea`, `J2K_Irstea_dev` | J2000 components developed at Irstea/INRAE |
| `J2K_S_N` | J2000-S nitrogen/crop components |
| `J2Kg` | J2000g lumped model components |
| `DRYvER` | Components developed in the DRYvER project |
| `Thornthwaite` | Thornthwaite water balance model |

Projects that are no longer actively maintained are preserved under
[`archive/`](archive/) (sources only, not part of the build). Their full
development history — like that of everything else — is available in the
git history. To reactivate an archived project, give it a `pom.xml` and
add it to the module list in the parent POM.

## Building

The modules build against the JAMS framework artifacts. Build the
framework first — its standard build command installs the artifacts
into your local Maven repository (repeat after updating jams):

```
git clone https://github.com/jamsframework/jams.git
cd jams && ./mvnw install -pl jams-starter -am
```

Then, in this repository:

```
./mvnw package
```

Each module produces a component jar in its `target/` directory; all
built jars are additionally collected in the `components/` directory at
the repository root, ready to be used by JAMS. External dependencies of
the models (anything the JAMS runtime does not already provide) are
copied to `components/lib/`, where the component jars find them via
their manifest class path.

To build only a single model library (plus whatever it depends on), use:

```
./mvnw package -pl J2K_ext -am
```

Multiple modules can be listed comma-separated (`-pl DRYvER,Thornthwaite`).
In IntelliJ IDEA, the same is achieved by double-clicking a module's
`package` goal in the Maven tool window. Note for IDE builds of modules
that depend on other models (e.g. `J2Kg` on `J2K_base`): run
`./mvnw install -N` once at the repository root first — it puts the
parent POM into your local Maven repository, which Maven needs to
resolve inter-module dependencies outside the full reactor. Install the
depended-on modules (e.g. `J2K_base`) the same way when they change.
Single-module builds add their jars to `components/` without touching the
others; `./mvnw clean` at the repository root empties the directory
(cleaning a single module deliberately does not).
Building only requires a JDK, version 17 or newer — Maven is provided
by the included wrapper.

## Using the model components

Simply add the `components/` directory of this repository to the
semicolon-separated `libs` property of JAMS (`default.jap`, or via the
settings dialog in JUICE) — every model you build becomes available to
JAMS automatically. Components that depend on other model jars (e.g.
`J2K_ext` on `J2K_base`) find them automatically when both are there.

## History

The complete Subversion history of the model projects (2005–2026) is
preserved in this repository. Releases of the models built with the
previous Ant/NetBeans setup were published via https://jams.uni-jena.de.

## License

Like the JAMS framework itself, the model components are free software,
licensed under the [GNU Lesser General Public License v3](LICENSE)
(see also [COPYING.GPL](COPYING.GPL) for the GNU GPL v3 that accompanies it).
