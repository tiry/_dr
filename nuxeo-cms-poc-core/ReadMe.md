
### About

#### Goal

This Nuxeo plugin implements Rendition and Storage logic to add the notion of dynamic rendition.

#### Why

Nuxeo allows to define renditions using extension points contributions.
The idea here is to be able to define dynamic renditions:

 - renditions that have not be contributed in advance at build time
 - rendirions that are defined on the fly at runtime

### Principles

#### DynamicRenditionHolder Adapter

Rendition definition need to be stored somewhere and since this is not inside an extension point registry, the idea is to store this inside a dynamic facet of the `DocumentModel`.

The `DynamicRenditionHolder` adapter encapsulate this storage logic.

#### DynamicRenditionProvider

The `DynamicRenditionProvider` and `DynamicRenditionDefinitionProvider` contain the logic to expose the new renditions based on Document meta-data to the global Rendition framework.

### Building

Build without tests

    mvn -DskipTests clean install

Build with the tests

    mvn clean install

Test coverage

    mvn cobertura:cobertura

