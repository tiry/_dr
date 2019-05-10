
### About

#### Goal

The goal of this repository is to provide example code to add some Web CMS features to a Nuxeo Server.

#### Features

**Dynamic Renditions**

The idea is to be able to configure and create a Rendition from a REST URL, a typically example being:

    GET /nuxeo/api/v1/{doc_uuid}/@cmsAdapter/dynamicCrop?converter=pictureCrop&width=300@height=300

This feature implementation is broken into 2 sub modules:

 - [nuxeo-cms-poc-core](nuxeo-cms-poc-core/) that implements the Core features
    - Rendition and storage logic
 - [nuxeo-cms-poc-jaxrs](nuxeo-cms-poc-jaxrs/) that implements the REST Bindings
 	- adapter and webobject

See ReadMe files in submodules for me details.

**Page Fragments**

TBD    

### Building

Build without tests

    mvn -DskipTests clean install

Build with the tests

    mvn clean install

Test coverage

    mvn cobertura:cobertura

