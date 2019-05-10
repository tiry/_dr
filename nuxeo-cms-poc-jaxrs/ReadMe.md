
### About

#### Goal

This Nuxeo plugin implements the REST Bindings for the dynamic rendition exposedby `nuxeo-cms-poc-core`.

### REST bindings

    GET /nuxeo/api/v1/{doc_uuid}/@cmsAdapter/{name}

Common parameters:

 - `name`: name of the rendition 
 - `cache` (default: true): define if the rendion need to be stored in the DocumentModel
 - `create` (default: true): define if the rendion can be created on the fly
 - `converter`: name of the converter (if ommited, converter = rendition name)

Examples:

Creating a dynamic rendition using the `pictureCrop` converter.

    GET /nuxeo/api/v1/{doc_uuid}/@cmsAdapter/dynamicCrop?converter=pictureCrop&width=300@height=300

Creating a dynamic transient rendition using the `pictureCrop` converter:

    GET /nuxeo/api/v1/{doc_uuid}/@cmsAdapter/dynamicCrop?converter=pictureCrop&width=300@height=300&cache=false

Get a dynamic rendition if it has been created before:

    GET /nuxeo/api/v1/{doc_uuid}/@cmsAdapter/dynamicCrop?converter=pictureCrop&width=300@height=300&create=false

### Building

Build without tests

    mvn -DskipTests clean install

Build with the tests

    mvn clean install

Test coverage

    mvn cobertura:cobertura


