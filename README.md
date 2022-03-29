# Cpp4CIA

This project aims to parse C++ source code using Eclipse CDT and create a lightweight component and dependency tree that
suitable for Change Impact Analysis.

The current status of the project is **EXPERIMENTAL**, and any commit may change the API and/or the data structure.
Treat this project as an example of how to use Eclipse CDT rather than a library!

## License

MIT

## Found a bug?

Feel free to open an issue/create a discussion.

## The data structure

Located in `mrmathami.cia.cpp.ast`, the data structure is a component-level tree of the input source code.

| Class                                      | Functionality                                                                                                                                 |
|--------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `mrmathami.cia.cpp.ast.CppNode`            | The base abstract node, contains the tree structure logic, the dependency logic, the base comparison logic and the base serialization logic.  |
| `mrmathami.cia.cpp.ast.NamespaceNode`      | C++ namespace component.                                                                                                                      |
| `mrmathami.cia.cpp.ast.ClassNode`          | C++ class component.                                                                                                                          |
| `mrmathami.cia.cpp.ast.EnumNode`           | C++ enum component.                                                                                                                           |
| `mrmathami.cia.cpp.ast.FunctionNode`       | C++ function/method component.                                                                                                                |
| `mrmathami.cia.cpp.ast.VariableNode`       | C++ variable/field component.                                                                                                                 |
| `mrmathami.cia.cpp.ast.TypedefNode`        | C++ typedef component.                                                                                                                        |
| `mrmathami.cia.cpp.ast.IntegralNode`       | C++ built-in type/unknown component.                                                                                                          |
| `mrmathami.cia.cpp.ast.RootNode`           | The root component of the tree.                                                                                                               |
| `mrmathami.cia.cpp.ast.DependencyType`     | An enum contains types of dependency that the tree can represent.                                                                             |
| `mrmathami.cia.cpp.ast.DependencyMap`      | Used as a map between dependency types and their quantity. Read the dependency logic in `mrmathami.cia.cpp.ast.CppNode` for more information. |
| `mrmathami.cia.cpp.ast.IBodyContainer`     | An interface for any nodes that have a body.                                                                                                  |
| `mrmathami.cia.cpp.ast.ITypeContainer`     | An interface for any nodes that have a type.                                                                                                  |
| `mrmathami.cia.cpp.ast.IClassContainer`    | An interface for any nodes that contains classes.                                                                                             |
| `mrmathami.cia.cpp.ast.IEnumContainer`     | An interface for any nodes that contains enums.                                                                                               |
| `mrmathami.cia.cpp.ast.IFunctionContainer` | An interface for any nodes that contains functions.                                                                                           |
| `mrmathami.cia.cpp.ast.IVariableContainer` | An interface for any nodes that contains variables.                                                                                           |
| `mrmathami.cia.cpp.ast.ITypedefContainer`  | An interface for any nodes that contains typedefs.                                                                                            |
| `mrmathami.cia.cpp.ast.IIntegralContainer` | An interface for any nodes that contains integrals.                                                                                           |

## The builder

Located in `mrmathami.cia.cpp.builder`, the builder's job is to build the component tree. The builder's workflow is as
follows: Get the input source code files, preprocess them into one single blob of code, shove that blob into CDT, get
the source code structure information from CDT and build the tree base on that information.

| Class                                           | Functionality                                       |
|-------------------------------------------------|-----------------------------------------------------|
| `mrmathami.cia.cpp.builder.VersionBuilder`      | The entry-point class, implement the main workflow. |
| `mrmathami.cia.cpp.builder.PreprocessorBuilder` | Implement the preprocessing step.                   |
| `mrmathami.cia.cpp.builder.AstBuilder`          | Implement the building step.                        |
| `mrmathami.cia.cpp.builder.ProjectVersion`      | The output of the builder.                          |

## The differ

Located in `mrmathami.cia.cpp.differ`, the differ's job is to compare two version of the component tree.

| Class                                          | Functionality                                                                                                                                                                   |
|------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `mrmathami.cia.cpp.differ.VersionDiffer`       | The entry-point class, implement the main workflow of the differ. This work together with the comparison logic found in `mrmathami.cia.cpp.ast.CppNode`.                        |
| `mrmathami.cia.cpp.differ.ImpactWeightBuilder` | Implement an unproven ranking algorithm to rank the impact of changes between two versions. This algorithm has multiple problems, and doesn't always return meaningful ranking! |
| `mrmathami.cia.cpp.differ.VersionDifference`   | The output of the differ.                                                                                                                                                       |
