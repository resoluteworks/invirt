rootProject.name = "invirt"

include("invirt-bom")
include("invirt-data")
include("invirt-core")
include("invirt-security")
include("invirt-utils")
include("invirt-test")

include("invirt-mongodb")
project(":invirt-mongodb").projectDir = file("experimental/invirt-mongodb")
