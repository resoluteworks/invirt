rootProject.name = "invirt"

include("invirt-bom")
include("invirt-data")
include("invirt-core")
include("invirt-security")
include("invirt-utils")
include("invirt-test")

include("invirt-mongodb")
project(":invirt-mongodb").projectDir = file("mongodb/invirt-mongodb")

include("invirt-mongodb-test")
project(":invirt-mongodb-test").projectDir = file("mongodb/invirt-mongodb-test")
